package com.ssic.report

import java.util
import java.util.Date

import com.ssic.service._
import com.ssic.utils.Tools._
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._

object TargetTotal {
  /**
    * 用料计划，配送计划，菜品留样的统计数据
    *
    */

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val date =format.format(new Date())

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val committee = session.read.jdbc(url, edu_committee, conn)
    val supplier =session.read.jdbc(url, pro_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    committee.createTempView("t_edu_committee")
    supplier.createTempView("t_pro_supplier")


    val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
    val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
    val schoolData = sc.broadcast(Tools.schoolNew(session)) //学校基础信息
    val commiteeid2commiteename = sc.broadcast(Tools.commiteeid2commiteename(session)) //教属名字信息
    val commiteeid2commiteeid = sc.broadcast(Tools.school2Committee2(session)) //教属id信息
    val schoolid2suppliername = sc.broadcast(Tools.schoolid2suppliername(session))  //团餐公司名字

    val jedis = JPools.getJedis
    val useMaterialPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlan-Detail")
    val usematerialData = sc.parallelize(useMaterialPlanDetail.asScala.toList) //已存在的用料计划数据

    val useMaterialTotal = jedis.hgetAll(date + "_useMaterialPlanTotal")
    val useMaterialTotalData = sc.parallelize(useMaterialTotal.asScala.toList) //已存在的用料计划统计数据

    val useMaterialChild: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlanTotal_child")
    val useMaterialChildData = sc.parallelize(useMaterialChild.asScala.toList) //已存在的用料计划子页面数据

    val distributionDetail: util.Map[String, String] = jedis.hgetAll(date + "_Distribution-Detail")
    val distributionData = sc.parallelize(distributionDetail.asScala.toList) //已存在的配送计划数据

    val distributionTotal = jedis.hgetAll(date + "_DistributionTotal")
    val distributionTotalData = sc.parallelize(distributionTotal.asScala.toList) //已存在的配送计划统计数据

    val distributionchild: util.Map[String, String] = jedis.hgetAll(date + "_DistributionTotal_child")
    val distributionchildData = sc.parallelize(distributionchild.asScala.toList) //已存在的配送计划子页面数据

    val retentionDetail: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondish")
    val retentionData = sc.parallelize(retentionDetail.asScala.toList) //已存在的留样计划数据

    val retentionTotal = jedis.hgetAll(date + "_gc-retentiondishtotal")
    val retentionTotalData = sc.parallelize(retentionTotal.asScala.toList) //已存在的留样计划统计数据

    val retentionchild: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondishtotal_child")
    val retentionchildData = sc.parallelize(retentionchild.asScala.toList) //已存在的留样计划子页面数据



    //处理好的用料计划数据
    val usematerialDealData = new DealDataStat().usematerialdealdata(usematerialData, projid2schoolid, projid2schoolname, gongcanSchool)
    //处理好的用料计划统计数据
    val usematerialdealtotaldata = new DealDataStat().usematerialdealtotaldata(useMaterialTotalData)

    //各区用料计划总数据统计
    new UsematerialTotalStat().areausematerialtotal(usematerialDealData, date)

    //各区用料计划各状态数据统计
    val useStatusData = usematerialdealtotaldata.filter(x => !x._1._1.equals("null")).map(_._1)
    new UsematerialTotalStat().areausmaterialstatustotal(usematerialDealData, date, useStatusData)

    //各区用料计划各状态按照学校数据统计(对学校进行去重处理)
    val useSchoolStatusData = usematerialdealtotaldata.filter(x => !x._3._1.equals("null")).map(_._3)
    new UsematerialTotalStat().areausmaterialschoolstatus(useMaterialChildData, date, useSchoolStatusData)

    //按所属教育部用料计划各状态数据统计
    val useMasteridStatusData = usematerialdealtotaldata.filter(x => !x._2._1.equals("null")).map(_._2)
    new UsematerialTotalStat().masteridusematerialtotal(usematerialDealData, date, useMasteridStatusData, schoolData, commiteeid2commiteename)

    //按所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
    val useMasteridSchoolStatusData = usematerialdealtotaldata.filter(x => !x._4._1.equals("null")).map(_._4)
    new UsematerialTotalStat().masteridusmaterialschoolstatus(useMaterialChildData, date, useMasteridSchoolStatusData, schoolData, commiteeid2commiteename)

    //按上海市办学性质来用料计划各状态数据统计（nature）
    val useNatureStatusData = usematerialdealtotaldata.filter(x => !x._5._1.equals("null")).map(_._5)
    new UsematerialTotalStat().natureusmaterialstatus(usematerialDealData, date, useNatureStatusData, schoolData)

    //各区办学性质来用料计划各状态数据统计（nature）
    val areauseNatureStatusData = usematerialdealtotaldata.filter(x => !x._9._1.equals("null")).map(_._9)
    new UsematerialTotalStat().areanatureusmaterialstatus(usematerialDealData, date, areauseNatureStatusData, schoolData)

    //按上海市办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val useNatureSchoolStatusData = usematerialdealtotaldata.filter(x => !x._6._1.equals("null")).map(_._6)
    new UsematerialTotalStat().natureusmaterialschoolstatus(useMaterialChildData, date, useNatureSchoolStatusData, schoolData)

    //按各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areauseNatureSchoolStatusData = usematerialdealtotaldata.filter(x => !x._10._1.equals("null")).map(_._10)
    new UsematerialTotalStat().areanatureusmaterialschoolstatus(useMaterialChildData, date, areauseNatureSchoolStatusData, schoolData)

    //按上海市各类型学校来用料计划各状态数据统计（level）
    val useLevelStatusData = usematerialdealtotaldata.filter(x => !x._7._1.equals("null")).map(_._7)
    new UsematerialTotalStat().levelusmaterialstatus(usematerialDealData, date, useLevelStatusData, schoolData)

    //按各区各类型学校来用料计划各状态数据统计（level）
    val areauseLevelStatusData = usematerialdealtotaldata.filter(x => !x._11._1.equals("null")).map(_._11)
    new UsematerialTotalStat().arealevelusmaterialstatus(usematerialDealData, date, areauseLevelStatusData, schoolData)

    //按上海市各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val useLevelSchoolStatusData = usematerialdealtotaldata.filter(x => !x._8._1.equals("null")).map(_._8)
    new UsematerialTotalStat().levelusmaterialschoolstatus(useMaterialChildData, date, useLevelSchoolStatusData, schoolData)

    //按各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val areauseLevelSchoolStatusData = usematerialdealtotaldata.filter(x => !x._12._1.equals("null")).map(_._12)
    new UsematerialTotalStat().arealevelusmaterialschoolstatus(useMaterialChildData, date, areauseLevelSchoolStatusData, schoolData)

    //处理好的配送计划数据
   val distributionDealData = new DealDataStat().distributiondealdata(distributionData, gongcanSchool)
    //处理好的配送计划统计数据
    val distributionDealTotalData = new DealDataStat().distributiondealtotaldata(distributionTotalData)

    //各区配送计划总数据统计
    new DistributionTotalStat().areadistributionltotal(distributionDealData,date)

    //各区配送计划各状态数据统计
    val disStatusData = distributionDealTotalData.filter(x => !x._1._1.equals("null")).map(_._1)
    new DistributionTotalStat().areadistributionstatustotal(distributionDealData,date,disStatusData)

    //各区配送计划各状态按照学校数据统计(对学校进行去重处理)
    val disSchoolStatusData = distributionDealTotalData.filter(x => !x._3._1.equals("null")).map(_._3)
    new DistributionTotalStat().areadistributionschoolstatus(distributionchildData,date,disSchoolStatusData)

    //按所属教育部配送计划各状态数据统计
    val disMasteridStatusData = distributionDealTotalData.filter(x => !x._2._1.equals("null")).map(_._2)
    new DistributionTotalStat().masteriddistributiontotal(distributionDealData,date,disMasteridStatusData, schoolData, commiteeid2commiteename)

    //按所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
    val disMasteridSchoolStatusData = distributionDealTotalData.filter(x => !x._4._1.equals("null")).map(_._4)
    new DistributionTotalStat().masteriddistributionschoolstatus(distributionchildData,date,disMasteridSchoolStatusData,schoolData,commiteeid2commiteename)

    //按上海市办学性质来配送计划各状态数据统计（nature）
    val disNatureStatusData = distributionDealTotalData.filter(x => !x._5._1.equals("null")).map(_._5)
    new DistributionTotalStat().naturedistributionstatus(distributionDealData,date,disNatureStatusData,schoolData)

    //各区办学性质来配送计划各状态数据统计（nature）
    val areadisNatureStatusData = distributionDealTotalData.filter(x => !x._9._1.equals("null")).map(_._9)
    new DistributionTotalStat().areanaturedistributionstatus(distributionDealData,date,areadisNatureStatusData,schoolData)

    //按上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val disNatureSchoolStatusData = distributionDealTotalData.filter(x => !x._6._1.equals("null")).map(_._6)
    new DistributionTotalStat().naturedistributionschoolstatus(distributionchildData,date,disNatureSchoolStatusData,schoolData)

    //按各区办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areadisNatureSchoolStatusData = distributionDealTotalData.filter(x => !x._10._1.equals("null")).map(_._10)
    new DistributionTotalStat().areanaturedistributionschoolstatus(distributionchildData,date,areadisNatureSchoolStatusData,schoolData)

    //按上海市各类型学校来配送计划各状态数据统计（level）
    val disLevelStatusData = distributionDealTotalData.filter(x => !x._7._1.equals("null")).map(_._7)
    new DistributionTotalStat().leveldistributionstatus(distributionDealData,date,disLevelStatusData,schoolData)

    //按各区各类型学校来配送计划各状态数据统计（level）
    val areadisLevelStatusData = distributionDealTotalData.filter(x => !x._11._1.equals("null")).map(_._11)
    new DistributionTotalStat().arealeveldistributionstatus(distributionDealData,date,areadisLevelStatusData,schoolData)

    //按上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val disLevelSchoolStatusData = distributionDealTotalData.filter(x => !x._8._1.equals("null")).map(_._8)
    new DistributionTotalStat().leveldistributionschoolstatus(distributionchildData,date,disLevelSchoolStatusData,schoolData)

    //按各区各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val areadisLevelSchoolStatusData = distributionDealTotalData.filter(x => !x._12._1.equals("null")).map(_._12)
    new DistributionTotalStat().arealeveldistributionschoolstatus(distributionchildData,date,areadisLevelSchoolStatusData,schoolData)

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

    val platoon_feed = jedis.hgetAll(date + "_platoon-feed")
    val plaData = sc.parallelize(platoon_feed.asScala.toList) //redis中存

    // 的供餐数据

    val edu_all = jedis.hgetAll(date + "_allUseData")
    val eduAllData = sc.parallelize(edu_all.asScala.toList) //redis中存的所有维度的数据

    new EduAllDataStat().platoonmaterialdetailresert(plaData,useMaterialChildData,distributionchildData,retentionchildData,schoolData,eduAllData,date,commiteeid2commiteeid,schoolid2suppliername)



    sc.stop()

  }

}
