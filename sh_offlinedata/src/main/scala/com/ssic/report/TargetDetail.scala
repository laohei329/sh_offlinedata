package com.ssic.report

import java.util.Date

import com.ssic.service.{DealDataStat, TargetDetailStat}
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
* 用料计划，配送计划，留样计划的详情数据
* */
object TargetDetail {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")

  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("今日用料，验收，留样详情数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val session = sqlContext.sparkSession

    val date = format.format(new Date())
    val datetime = date+" 00:00:00"
    val year =format1.format(new Date())
    val month =format2.format(new Date())

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val pro_suppliers = session.read.jdbc(url, pro_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    pro_suppliers.createTempView("t_pro_supplier")

    val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session))  //项目点id获取学校id
    val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date))    //供餐学校数据
    val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
    val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号
    val schoolid2Projid = sc.broadcast(Tools.schoolid2Projid(session)) //学校id获取项目点id
    val schoolid2suppliername = sc.broadcast(Tools.schoolid2suppliername(session)) //学校id获取团餐公司名字

    val jedis = JPools.getJedis

    val useMaterialPlanDetail = jedis.hgetAll(date + "_useMaterialPlanDetail")
    val useMaterialPlanDetailData = sc.parallelize(useMaterialPlanDetail.asScala.toList) //用料计划临时表数据

    val useMaterials = jedis.hgetAll(date + "_useMaterialPlan-Detail")
    val useMaterialData = sc.parallelize(useMaterials.asScala.toList) //用料计划已经存在的数据

    val distributionDetail = jedis.hgetAll(date + "_DistributionDetail")
    val distributionDetailData = sc.parallelize(distributionDetail.asScala.toList) //配送计划临时表数据

    val b2bLedgerDetail = jedis.hgetAll(date + "_B2b-DistributionDetail")
    val b2bLedgerDetailData = sc.parallelize(b2bLedgerDetail.asScala.toList) //b2b配送计划主表

    val b2bLedgerExtraDetail = jedis.hgetAll("b2bDistribution")
    val b2bLedgerExtraDetailData = sc.parallelize(b2bLedgerExtraDetail.asScala.toList) //b2b配送计划子表

    val distributions = jedis.hgetAll(date + "_Distribution-Detail")
    val distributionData = sc.parallelize(distributions.asScala.toList)  //配送计划已经存在的数据

    val dishmenu: RDD[(String, String, String, String, String, String, String, String)] = Tools.hivedishmenu(hiveContext,datetime,year,month) //hive数据库的菜品数据
    val todaydishmenu = jedis.hgetAll(date + "_dish-menu")
    val todaydishmenuData: RDD[(String, String)] = sc.parallelize(todaydishmenu.asScala.toList)  //当天排菜菜品临时表数据

    val retentiondish = jedis.hgetAll(date + "_retention-dish")
    val retentiondishData: RDD[(String, String)] = sc.parallelize(retentiondish.asScala.toList)  //留样计划临时表数据

    //172.18.14.20:7001> hget 2020-10-21_gc-retentiondishtotal_child area_3_id_c5149174-b743-4a6a-ae47-47b2cdd3f5bf
    //total_14_reserve_14_noreserve_0_status_1_reservestatus_1_createtime_2020-10-21 07:16:52
    val gcretentiondish = jedis.hgetAll(date + "_gc-retentiondish")
    val gcretentiondishData: RDD[(String, String)] = sc.parallelize(gcretentiondish.asScala.toList)  //留样计划已经存在的数据

    val b2bPlatoon =  sc.parallelize(jedis.hgetAll(date + "_b2b-platoon").asScala.toList) //redis中b2b的排菜数据

    //用料计划的详情数据
    //处理好的用料计划数据
    val b2bPlatoonSchool: RDD[(String, Int)] = b2bPlatoon.map(x => (x._1.split("_")(1),2)).distinct() //b2b排菜的学校就算做用料确认

    val usematerialDealData = new DealDataStat().usematerialdealdata(useMaterialPlanDetailData,projid2schoolid,projid2schoolname,gongcanSchool,projid2Area,b2bPlatoonSchool,schoolid2Projid,schoolid2suppliername)
    new TargetDetailStat().usematerial(usematerialDealData,useMaterialData,date)

    //配送计划的详情数据
          // 处理好的配送计划数据

    val distributionDetailAllData = b2bLedgerDetailData.leftOuterJoin(b2bLedgerExtraDetailData)
      .map({
        x =>
          (x._2._1, x._2._2.getOrElse("null"))
      }).filter(x => !"null".equals(x._2)).union(distributionDetailData)

    val distributiondealdata = new DealDataStat().distributiondealdata(distributionDetailAllData,gongcanSchool,school2Area,date)

    new TargetDetailStat().distribution(distributiondealdata,distributionData,date)

    //当天的菜品留样的详情数据
    new TargetDetailStat().todayretentiondish(dishmenu,retentiondishData,date,gongcanSchool,gcretentiondishData,todaydishmenuData,school2Area)

    sc.stop()
  }

}
