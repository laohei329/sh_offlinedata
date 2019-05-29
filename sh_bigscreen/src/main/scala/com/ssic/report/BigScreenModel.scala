package com.ssic.report

import java.sql.{DriverManager, Statement}
import java.{io, lang, util}
import java.util.{Date, UUID}

import com.alibaba.fastjson.JSON
import com.ssic.beans.SchoolBean
import com.ssic.utils.Tools.{hiveurl,conn3}
import com.ssic.utils.{JPools, SaveOnRedis, Tools}
import org.apache.commons.lang.time.FastDateFormat
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory
import org.apache.commons.lang3._
import org.apache.kafka.common.TopicPartition
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.sql.hive.HiveContext



/**
  * Created by 云 on 2018/8/6.
  */
object BigScreenModel {

  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("上海大屏项目") //.setMaster("spark://172.16.10.17:7077")
    sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
    sparkConf.set("spark.streaming.kafka.maxRatePerPartition", "500")
    sparkConf.set("spark.driver.allowMultipleContexts", "true")
    sparkConf.set("spark.debug.maxToStringFields", "200")
    val session: SparkSession = SparkSession.builder().config(sparkConf).getOrCreate()
    val ssc = new StreamingContext(session.sparkContext, Seconds(8))



    val school2CommiteeBro = ssc.sparkContext.broadcast(Tools.school2Commitee(session))
    val projid2CommiteeBro = ssc.sparkContext.broadcast(Tools.projid2Area(session))
    val school2schoolNature = ssc.sparkContext.broadcast(Tools.school2school_nature(session))
    val school2canteenMode = ssc.sparkContext.broadcast(Tools.school2canteen_mode(session))
    val school2level = ssc.sparkContext.broadcast(Tools.school2level(session))
    val school2level2 = ssc.sparkContext.broadcast(Tools.school2level2(session))
    val supplierid2supplierName = ssc.sparkContext.broadcast(Tools.supplierid2supplierName(session))
    val supplierid2supplierName2 = ssc.sparkContext.broadcast(Tools.supplierid2supplierName2(session))
    val supplierid2Schoolmaster = ssc.sparkContext.broadcast(Tools.supplierid2Schoolmaster(session))
    val commiteeid2commiteename = ssc.sparkContext.broadcast(Tools.commiteeid2commiteename(session))
    val schoolid2masterid = ssc.sparkContext.broadcast(Tools.schoolid2masterid(session))
    val schoolsupplierid2schoolid = ssc.sparkContext.broadcast(Tools.schoolsupplierid2schoolid(session))
    val school2school_nature_sub = ssc.sparkContext.broadcast(Tools.school2school_nature_sub(session))
    val school2ledger_type = ssc.sparkContext.broadcast(Tools.school2ledger_type(session))
    val supplierid2district = ssc.sparkContext.broadcast(Tools.supplierid2district(session))
    val supplierid2companytype = ssc.sparkContext.broadcast(Tools.supplierid2companytype(session))
    val schoolid2schoolterm = ssc.sparkContext.broadcast(Tools.schoolid2schoolterm(session))
    //val supplierid2schoolid = ssc.sparkContext.broadcast(Tools.supplierid2schoolid(session))

    val groupId = "shdata012"
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "172.16.10.18:9092,172.16.10.37:9092,172.16.10.52:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupId,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: lang.Boolean)
    )

    val topics = Array("shbigdata4")

    val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/kafka", "maxwell", "S3cret_ssic_Bi")
    var fromdbOffset = Map[TopicPartition, Long]()
    val pstmt = conn.prepareStatement(s"select * from consumer_offset where groupId=?")
    pstmt.setString(1, groupId)
    val resultSet = pstmt.executeQuery()
    while (resultSet.next()) {
      fromdbOffset += new TopicPartition(resultSet.getString("topic"), resultSet.getInt("partitionId")) -> resultSet.getLong("offset")
    }
    conn.close()

    val stream = if (fromdbOffset.size == 0) {
      //判断该消费者是不是第一次登陆
      KafkaUtils.createDirectStream[String, String](ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Subscribe[String, String](topics, kafkaParams)
      )
    } else {
      KafkaUtils.createDirectStream[String, String](ssc,
        LocationStrategies.PreferConsistent,
        ConsumerStrategies.Assign[String, String](fromdbOffset.keys, kafkaParams, fromdbOffset)
      )
    }

    stream.foreachRDD({
      rdd =>
        val offsetRanges: Array[OffsetRange] = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        val filterData: RDD[SchoolBean] = rdd.map(_.value()).map({
          x =>
            var schoolBean: SchoolBean = null
            try {
              schoolBean = JSON.parseObject(x, classOf[SchoolBean])
            } catch {
              case e: Exception => {
                logger.error(s"parse json error: $x", e)
              }
            }
            schoolBean
        })


        //排菜计划的详细的临时表
        PlatoonPlan.PlatoonRealTimeData(filterData, school2CommiteeBro, school2canteenMode, school2schoolNature, school2level, school2level2, school2school_nature_sub, school2ledger_type).map(x => (x._1, x._3, x._2, x._4, x._5, x._6, x._7, x._8, x._9, x._10))
          .foreachPartition({
            itr =>
              SaveOnRedis.PlatoonPlanReal(itr)
          })

        //学校排菜实时使用情况功能展现（上海）
        SchoolIdDisplay.SchoolIdShow(filterData).map(x => (x._1, x._2)).filter(x => StringUtils.isNoneEmpty(x._2))
          .foreachPartition({
            itr =>
              SaveOnRedis.DisplayRealTime(itr)
          })

        //用料计划的详细信息
        MaterialConfirm.useMaterialdish(filterData,projid2CommiteeBro,supplierid2supplierName)

        //配送计划的上海市，各区计算指标
        //        Distribution.DistributionIndex((filterData, school2CommiteeBro)).filter(x => StringUtils.isNoneEmpty(x._3)).filter(x => StringUtils.isNoneEmpty(x._1))
        //          .foreachPartition({
        //            itr =>
        //              SaveOnRedis.DistributionRealTime(itr)
        //          })

        //配送计划的详细信息
        Distribution.DistributionPlan(filterData).distinct().filter(x => !x._1.equals("null")).leftOuterJoin(Distribution.ProLedgerPlan(filterData).distinct())
          //id,配送时间，配送类型，学校ID，团餐公司ID，发货批次，配送状态，统配,区号,表的类型，是否有效
          .map(x => (x._1, x._2._1(0), x._2._1(1), x._2._1(2), x._2._1(3), x._2._1(4), x._2._1(5), x._2._2.getOrElse("null"), school2CommiteeBro.value.getOrElse(x._2._1(2), "null"), x._2._1(7), x._2._1(8), x._2._1(10)))
          .foreachPartition({
            itr =>
              SaveOnRedis.DistributionDetailRealTime(itr)
          })


        //学校详情
        SchoolData.SchoolInsert(filterData, schoolid2schoolterm)
        SchoolData.schoolImage(filterData)
        //学校按上海以及各区，各类型的数量
        SchoolData.School(filterData, schoolid2schoolterm,commiteeid2commiteename)

        //供应商总数
        SupplierData.SupplierBaseData(filterData)

        //供应商供应学校数量排名
       // SupplierData.SupplierToScholl(filterData, supplierid2supplierName2)

        //禁止食品
        StopFood.Stop(filterData)

        //预警计划的数量
//        WarnHive.masterlicenseinsert(filterData)
//        WarnHive.masterupdatedelete(filterData)
//        WarnHive.warnrelation(filterData)

        Warn.warnInsert(filterData, supplierid2district, supplierid2companytype,supplierid2Schoolmaster,commiteeid2commiteename)
        Warn.warnInsertPeople(filterData, supplierid2district, supplierid2companytype,schoolid2masterid,commiteeid2commiteename)
        Warn.warnUpdateDelete(filterData, supplierid2district, supplierid2companytype,supplierid2Schoolmaster,commiteeid2commiteename,schoolid2masterid)

        //原料供应统计
        //        B2bMaterial.Material2Suppllier(filterData, school2CommiteeBro)

        //        // 原料供应明细
        //        B2bMaterial.Material2SupplierDetailInsert(filterData, school2CommiteeBro)
        //        B2bMaterial.Material2SupplierDetailDelete(filterData)
        //        B2bMaterial.LedgerMasterUpdate(filterData)
        //        B2bMaterial.LedgerUpdate(filterData, school2CommiteeBro)
        //
        //        //  菜品详细信息
        //        RetentionDish.dishDetail(filterData, school2CommiteeBro)
        //        RetentionDish.dishUpdateDelete(filterData, school2CommiteeBro)
        //        RetentionDish.packageUpdateDelete(filterData, school2CommiteeBro)
        //       //留样信息
        RetentionDish.retentionDishDetail(filterData)
        RetentionDish.retentionDishUpdateDelte(filterData)
        RetentionDish.retentionUpdateDelte(filterData)

        //餐厨垃圾和油脂
        Receyler.wastedata(filterData, supplierid2district, school2CommiteeBro,schoolid2masterid,commiteeid2commiteename)

        //团餐公司信息
        GroupSupplierDetail.groupSupplierInsert(filterData)
        GroupSupplierDetail.groupSupplierUpDe(filterData)

        //供应商信息
        SupplierDetail.supplierDetail(filterData)


        //员工证件信息
        PeopleLicenseDetail.licenseInsert(filterData, schoolsupplierid2schoolid)
        PeopleLicenseDetail.licenseDelete(filterData, schoolsupplierid2schoolid)
        PeopleLicenseDetail.licenseUpdate(filterData)

        val conn1 = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/kafka", "maxwell", "S3cret_ssic_Bi")

        for (offsetRange <- offsetRanges) {
          val pstmt1 = conn1.prepareStatement("REPLACE INTO consumer_offset(groupId,topic,partitionId,`offset`) VALUES(?,?,?,?)")
          pstmt1.setString(1, groupId)
          pstmt1.setString(2, offsetRange.topic)
          pstmt1.setInt(3, offsetRange.partition)
          pstmt1.setLong(4, offsetRange.untilOffset)

          pstmt1.executeUpdate()

        }

        conn1.close()

    })

    ssc.start()
    ssc.awaitTermination()
  }

}
