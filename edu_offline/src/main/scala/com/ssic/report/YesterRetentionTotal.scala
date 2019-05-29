package com.ssic.report

import java.util
import java.util.{Calendar, Date}

import com.ssic.service.{DealDataStat, RetentionTotalStat}
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._
/**\
  * 昨天的留样统计数据，因为数仓的菜品数据没有当天产生的数据
  */

object YesterRetentionTotal {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val session = sqlContext.sparkSession

    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH,-1)
    val time = calendar.getTime
    val date = format.format(time)

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val committee = session.read.jdbc(url, edu_committee, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    committee.createTempView("t_edu_committee")

    val schoolData = sc.broadcast(Tools.schoolNew(session)) //学校基础信息
    val commiteeid2commiteename = sc.broadcast(Tools.commiteeid2commiteename(session)) //教属名字信息

    val jedis = JPools.getJedis
    val retentionDetail: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondish")
    val retentionData = sc.parallelize(retentionDetail.asScala.toList) //已存在的留样计划数据

    val retentionTotal = jedis.hgetAll(date + "_gc-retentiondishtotal")
    val retentionTotalData = sc.parallelize(retentionTotal.asScala.toList) //已存在的留样计划统计数据

    val retentionchild: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondishtotal_child")
    val retentionchildData = sc.parallelize(retentionchild.asScala.toList) //已存在的留样计划子页面数据

    //处理好的留样计划数据
    val retentionDealData = new DealDataStat().retentiondealdata(retentionData)
    //处理好的留样计划统计数据
    val retentiondealtotaldata = new DealDataStat().retentiondealtotaldata(retentionTotalData)

    //各区留样计划总数据统计
    new RetentionTotalStat().arearetentiontotal(retentionDealData,date)

    //各区留样计划各状态数据统计
    val reStatusData = retentiondealtotaldata.filter(x => !x._3._1.equals("null")).map(_._3)
    new RetentionTotalStat().arearetentionstatustotal(retentionDealData,date,reStatusData)

    //各区留样计划各状态按照学校数据统计(对学校进行去重处理)
    val reSchoolStatusData = retentiondealtotaldata.filter(x => !x._4._1.equals("null")).map(_._4)
    new RetentionTotalStat().arearetentionschoolstatus(retentionchildData,date,reSchoolStatusData)

    //按所属教育部留样计划各状态数据统计
    val reMasteridStatusData = retentiondealtotaldata.filter(x => !x._2._2.equals("null")).map(_._2)
    new RetentionTotalStat().masteridretentiontotal(retentionDealData,date,reMasteridStatusData,schoolData,commiteeid2commiteename)

    //按所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
    val reMasteridSchoolStatusData = retentiondealtotaldata.filter(x => !x._5._2.equals("null")).map(_._5)
    new RetentionTotalStat().masteridretentionschoolstatus(retentionchildData,date,reMasteridSchoolStatusData,schoolData,commiteeid2commiteename)

    //按上海市办学性质来留样计划各状态数据统计（nature）
    val reNatureStatusData = retentiondealtotaldata.filter(x => !x._6._1.equals("null")).map(_._6)
    new RetentionTotalStat().natureretentionstatus(retentionDealData,date,reNatureStatusData,schoolData)

    //各区办学性质来留样计划各状态数据统计（nature）
    val areareNatureStatusData = retentiondealtotaldata.filter(x => !x._10._1.equals("null")).map(_._10)
    new RetentionTotalStat().areanatureretentionstatus(retentionDealData,date,areareNatureStatusData,schoolData)

    //按上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val reNatureSchoolStatusData = retentiondealtotaldata.filter(x => !x._7._1.equals("null")).map(_._7)
    new RetentionTotalStat().natureretentionschoolstatus(retentionchildData,date,reNatureSchoolStatusData,schoolData)

    //按各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areareNatureSchoolStatusData = retentiondealtotaldata.filter(x => !x._11._1.equals("null")).map(_._11)
    new RetentionTotalStat().areanatureretentionschoolstatus(retentionchildData,date,areareNatureSchoolStatusData,schoolData)

    //按上海市各类型学校来留样计划各状态数据统计（level）
    val reLevelStatusData = retentiondealtotaldata.filter(x => !x._8._1.equals("null")).map(_._8)
    new RetentionTotalStat().levelreretentionstatus(retentionDealData,date,reLevelStatusData,schoolData)

    //按各区各类型学校来留样计划各状态数据统计（level）
    val areareLevelStatusData = retentiondealtotaldata.filter(x => !x._12._1.equals("null")).map(_._12)
    new RetentionTotalStat().arealevelreretentionstatus(retentionDealData,date,areareLevelStatusData,schoolData)

    //按上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val reLevelSchoolStatusData = retentiondealtotaldata.filter(x => !x._9._1.equals("null")).map(_._9)
    new RetentionTotalStat().levelreretentionschoolstatus(retentionchildData,date,reLevelSchoolStatusData,schoolData)

    //按各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val areareLevelSchoolStatusData = retentiondealtotaldata.filter(x => !x._13._1.equals("null")).map(_._13)
    new RetentionTotalStat().arealevelreretentionschoolstatus(retentionchildData,date,areareLevelSchoolStatusData,schoolData)
    sc.stop()
  }

}
