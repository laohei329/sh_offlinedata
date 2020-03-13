package com.ssic.report

import java.sql.{DriverManager, Statement}
import java.{io, lang, util}

import com.alibaba.fastjson.JSON
import com.ssic.beans.SchoolBean
import com.ssic.service.{GroupSupplierDetail, SchoolData, SupplierDetail, ZhongTaiDetailToLocal}
import com.ssic.utils.{NewTools, SaveOnRedis}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory
import org.apache.commons.lang3._
import org.apache.kafka.common.TopicPartition
import org.apache.spark.sql.{SQLContext, SparkSession}


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
    ssc.sparkContext.setLogLevel("WARN")


    //    val school2CommiteeBro = ssc.sparkContext.broadcast(Tools.school2Commitee(session))
    //    val projid2CommiteeBro = ssc.sparkContext.broadcast(Tools.projid2Area(session))
    //    val school2schoolNature = ssc.sparkContext.broadcast(Tools.school2school_nature(session))
    //    val school2canteenMode = ssc.sparkContext.broadcast(Tools.school2canteen_mode(session))
    //    val school2level = ssc.sparkContext.broadcast(Tools.school2level(session))
    //    val school2level2 = ssc.sparkContext.broadcast(Tools.school2level2(session))
    //    //val supplierid2supplierName = ssc.sparkContext.broadcast(Tools.supplierid2supplierName(session))
    //    val supplierid2supplierName2 = ssc.sparkContext.broadcast(Tools.supplierid2supplierName2(session))
    //    val supplierid2Schoolmaster = ssc.sparkContext.broadcast(Tools.supplierid2Schoolmaster(session))
    //    val commiteeid2commiteename = ssc.sparkContext.broadcast(Tools.commiteeid2commiteename(session))
    //    val schoolid2masterid = ssc.sparkContext.broadcast(Tools.schoolid2masterid(session))
    //    val schoolsupplierid2schoolid = ssc.sparkContext.broadcast(Tools.schoolsupplierid2schoolid(session))
    //    val school2school_nature_sub = ssc.sparkContext.broadcast(Tools.school2school_nature_sub(session))
    //    val school2ledger_type = ssc.sparkContext.broadcast(Tools.school2ledger_type(session))
    //    val supplierid2district = ssc.sparkContext.broadcast(Tools.supplierid2district(session))
    //    val supplierid2companytype = ssc.sparkContext.broadcast(Tools.supplierid2companytype(session))
    //    val schoolid2schoolterm = ssc.sparkContext.broadcast(Tools.schoolid2schoolterm(session))

    val school2commitee = ssc.sparkContext.broadcast(NewTools.school2commitee(session))
    val school2commiteename = ssc.sparkContext.broadcast(NewTools.school2commiteename(session))
    val school2Area = ssc.sparkContext.broadcast(NewTools.school2Area(session))
    val projid2Area = ssc.sparkContext.broadcast(NewTools.projid2Area(session))
    val supplierid2supplierName = ssc.sparkContext.broadcast(NewTools.supplierid2supplierName(session))
    val commiteeid2commiteename = ssc.sparkContext.broadcast(NewTools.commiteeid2commiteename(session))
    val supplierid2area = ssc.sparkContext.broadcast(NewTools.supplierid2area(session))
    val schoolid2masterid = ssc.sparkContext.broadcast(NewTools.schoolid2masterid(session))
    val schoolsupplierid2schoolid = ssc.sparkContext.broadcast(NewTools.schoolsupplierid2schoolid(session))


    val groupId = "hw_edu_data_new"
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "172.18.14.38:9092,172.18.14.37:9092,172.18.14.39:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupId,
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (false: lang.Boolean)
    )

    val topics = Array("hw_edu_new")

    val conn = DriverManager.getConnection("jdbc:mysql://172.18.14.30:3306/kafka", "azkaban", "nn1.hadoop@ssic.cn")
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

        //        //将中台的所在地表信息 迁移到 本地的area表
        ZhongTaiDetailToLocal.Area(filterData)
        //将中台的所属表信息 迁移到 本地的t_edu_competent_department表
        ZhongTaiDetailToLocal.EduCompetentDepartment(filterData)
        //将中台的学校和团餐公司关联信息 迁移到 本地的t_edu_school_supplier表
        ZhongTaiDetailToLocal.SchoolSupplier(filterData)
        //将中台的团餐公司信息 迁移到 本地的t_pro_supplier表
        ZhongTaiDetailToLocal.GroupSupplier(filterData)
        //将中台的供应商公司信息 迁移到 本地的t_pro_supplier表
        ZhongTaiDetailToLocal.SupplierInfo(filterData)
        //将中台的学校信息 迁移到 本地的t_edu_school表
        ZhongTaiDetailToLocal.SchoolDetail(filterData, school2commitee, school2commiteename)
        //将华为云上的t_edu_calendar 迁移到 本地的t_edu_calendar表\
        ZhongTaiDetailToLocal.EduCalendar(filterData)
        //将华为云上的t_edu_schoolterm 迁移到 本地的t_edu_schoolterm表
        ZhongTaiDetailToLocal.EduSchooltrem(filterData)
        //将华为云上的t_edu_schoolterm_system 迁移到 本地的t_edu_schoolterm_system表
        ZhongTaiDetailToLocal.EduSchooltermSystem(filterData)
        //将华为云上的t_edu_holiday 迁移到 本地的t_edu_holiday表
        ZhongTaiDetailToLocal.EduHoliday(filterData)


        //上海市排菜计划的详细的临时表
        PlatoonPlan.PlatoonRealTimeData(filterData, school2Area, session)
          //时间，区号，schoolid，表操作类型，创建时间
          .map(x => (x._1, x._3, x._2, x._4, x._5,x._6))
          .foreachPartition({
            itr =>
              SaveOnRedis.PlatoonPlanReal(itr)
          })
        //
        //        //学校排菜实时使用情况功能展现（上海）
        SchoolIdDisplay.SchoolIdShow(filterData, school2Area).map(x => (x._1, x._2)).filter(x => StringUtils.isNoneEmpty(x._2))
          .foreachPartition({
            itr =>
              SaveOnRedis.DisplayRealTime(itr)
          })
        //
        //        //用料计划的详细信息
        MaterialConfirm.useMaterialdish(filterData, projid2Area, supplierid2supplierName)
        //
        //        //配送计划的上海市，各区计算指标
        //        //        Distribution.DistributionIndex((filterData, school2CommiteeBro)).filter(x => StringUtils.isNoneEmpty(x._3)).filter(x => StringUtils.isNoneEmpty(x._1))
        //        //          .foreachPartition({
        //        //            itr =>
        //        //              SaveOnRedis.DistributionRealTime(itr)
        //        //          })
        //
        //        //配送计划的详细信息
        Distribution.DistributionPlan(filterData).distinct().filter(x => !x._1.equals("null")).leftOuterJoin(Distribution.ProLedgerPlan(filterData).distinct())
          //id,配送时间，配送类型，学校ID，团餐公司ID，发货批次，配送状态，统配,区号，表类型，stat,验收上报日期,进货日期,验收规则,/验收日期
          .map(x => (x._1, x._2._1(0), x._2._1(1), x._2._1(2), x._2._1(3), x._2._1(4), x._2._1(5).toInt, x._2._2.getOrElse("null"), school2Area.value.getOrElse(x._2._1(2), "null"), x._2._1(7), x._2._1(8), x._2._1(10),x._2._1(11),x._2._1(12),x._2._1(13)))
          .sortBy(x => x._7, true)
          .foreachPartition({
            itr =>
              SaveOnRedis.DistributionDetailRealTime(itr)
          })
        //
        //
        //学校详情
        SchoolData.SchoolInsert(filterData, "null", school2commitee, school2commiteename)
        SchoolData.schoolImage(filterData)
        //学校按上海以及各区，各类型的数量
        SchoolData.School(filterData, "null", commiteeid2commiteename, school2commitee, school2commiteename)
        //学校基础信息的update和delete


        //供应商总数
        //SupplierData.SupplierBaseData(filterData)

        //供应商供应学校数量排名
        // SupplierData.SupplierToScholl(filterData, supplierid2supplierName2)

        //禁止食品
        StopFood.Stop(filterData)


        //
        //        //  当天排菜的菜品详细信息
        RetentionDish.dishDetail(filterData)
        RetentionDish.dishUpdateDelete(filterData)
        //        RetentionDish.packageUpdateDelete(filterData, school2CommiteeBro)

        //
        //       //留样信息
//        RetentionDish.retentionDishDetail(filterData)
//        RetentionDish.retentionDishUpdateDelte(filterData)
//        RetentionDish.retentionUpdateDelte(filterData)

        NewRetentionDish.retentionSampleDetail(filterData)
        NewRetentionDish.retentionDishDetail(filterData)



        //上海市餐厨垃圾和油脂
        Receyler.wastedata(filterData, supplierid2area, school2Area, schoolid2masterid, commiteeid2commiteename)

        //团餐公司信息
        GroupSupplierDetail.groupSupplierInsert(filterData)
        GroupSupplierDetail.groupSupplierUpDe(filterData)

        //供应商信息
        SupplierDetail.supplierDetail(filterData)


        //员工证件信息
        PeopleLicenseDetail.licenseInsert(filterData, schoolsupplierid2schoolid)
        PeopleLicenseDetail.licenseDelete(filterData, schoolsupplierid2schoolid)
        PeopleLicenseDetail.licenseUpdate(filterData)

        //将中台的学校基础数据迁移到本地mysql的t_edu_school


        val conn1 = DriverManager.getConnection("jdbc:mysql://172.18.14.30:3306/kafka", "azkaban", "nn1.hadoop@ssic.cn")

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
