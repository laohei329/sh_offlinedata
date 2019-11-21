package com.ssic.report

import java.util
import java.util.Date

import com.ssic.service.{RetentionTotalStat, _}
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
    val bd_department = session.read.jdbc(url, edu_bd_department, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    committee.createTempView("t_edu_committee")
    supplier.createTempView("t_pro_supplier")
    bd_department.createTempView("t_edu_bd_department")

    val departmentid = Tools.departmentid(session)
    for (i <- 0 until departmentid.size){
      val id = departmentid(i)
      val jedis = JPools.getJedis
      jedis.del(date+"_useMaterialPlanTotal"+"_"+"department"+"_"+id)
      jedis.del(date+"_DistributionTotal"+"_"+"department"+"_"+id)
      jedis.del(date+"_gc-retentiondishtotal"+"_"+"department"+"_"+id)
    }


    val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
    val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
    val schoolData = sc.broadcast(Tools.schoolNew(session)) //学校基础信息
    val commiteeid2commiteename = sc.broadcast(Tools.commiteeid2commiteename(session)) //教属名字信息
    val commiteeid2commiteeid = sc.broadcast(Tools.school2Committee2(session)) //教属id信息
    val schoolid2suppliername = sc.broadcast(Tools.schoolid2suppliername(session))  //团餐公司名字
    val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
    val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号


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
    val usematerialDealData = new DealDataStat().usematerialdealdata(usematerialData, projid2schoolid, projid2schoolname, gongcanSchool,projid2Area)
    //处理好的用料计划统计数据
    val usematerialdealtotaldata = new DealDataStat().usematerialdealtotaldata(useMaterialTotalData)

    //各区用料计划总数据统计
    new UsematerialTotalStat().areausematerialtotal(usematerialDealData, date)

    //按照权限管理部门各区用料计划总数据统计
    new UsematerialTotalStat().departmentareausematerialtotal(usematerialDealData,date,schoolData)

    //市教委各区用料计划各状态数据统计
    val useStatusData = usematerialdealtotaldata.filter(x => !x._1._1.equals("null")).map(_._1)
    new UsematerialTotalStat().areausmaterialstatustotal(usematerialDealData, date, useStatusData)

    //按照权限管理部门各区用料计划各状态数据统计
    new UsematerialTotalStat().departmentareausmaterialstatustotal(usematerialDealData,date,schoolData)

    //各区用料计划各状态按照学校数据统计(对学校进行去重处理)
    val useSchoolStatusData = usematerialdealtotaldata.filter(x => !x._3._1.equals("null")).map(_._3)
    new UsematerialTotalStat().areausmaterialschoolstatus(useMaterialChildData, date, useSchoolStatusData)

    //按照权限管理部门各区用料计划各状态按照学校数据统计(对学校进行去重处理)
    new UsematerialTotalStat().departmentareausmaterialschoolstatus(useMaterialChildData,date,schoolData)

    //按所属教育部用料计划各状态数据统计
    val useMasteridStatusData = usematerialdealtotaldata.filter(x => !x._2._1.equals("null")).map(_._2)
    new UsematerialTotalStat().masteridusematerialtotal(usematerialDealData, date, useMasteridStatusData, schoolData, commiteeid2commiteename)

    //按照权限管理部门所属教育部用料计划各状态数据统计
    new UsematerialTotalStat().departmentmasteridusematerialtotal(usematerialDealData,date,schoolData,commiteeid2commiteename)

    //按所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
    val useMasteridSchoolStatusData = usematerialdealtotaldata.filter(x => !x._4._1.equals("null")).map(_._4)
    new UsematerialTotalStat().masteridusmaterialschoolstatus(useMaterialChildData, date, useMasteridSchoolStatusData, schoolData, commiteeid2commiteename)

    //按照权限管理部门所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
    new UsematerialTotalStat().departmentmasteridusmaterialschoolstatus(useMaterialChildData,date,schoolData,commiteeid2commiteename)

    //按上海市办学性质来用料计划各状态数据统计（nature）
    val useNatureStatusData = usematerialdealtotaldata.filter(x => !x._5._1.equals("null")).map(_._5)
    new UsematerialTotalStat().natureusmaterialstatus(usematerialDealData, date, useNatureStatusData, schoolData)

    //按照权限管理部门办学性质来用料计划各状态数据统计（nature）
    new UsematerialTotalStat().departmentnatureusmaterialstatus(usematerialDealData,date,schoolData)

    //各区办学性质来用料计划各状态数据统计（nature）
    val areauseNatureStatusData = usematerialdealtotaldata.filter(x => !x._9._1.equals("null")).map(_._9)
    new UsematerialTotalStat().areanatureusmaterialstatus(usematerialDealData, date, areauseNatureStatusData, schoolData)

    //按照权限管理部门各区办学性质来用料计划各状态数据统计（nature）
    new UsematerialTotalStat().departmentareanatureusmaterialstatus(usematerialDealData,date,schoolData)

    //按上海市办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val useNatureSchoolStatusData = usematerialdealtotaldata.filter(x => !x._6._1.equals("null")).map(_._6)
    new UsematerialTotalStat().natureusmaterialschoolstatus(useMaterialChildData, date, useNatureSchoolStatusData, schoolData)


    //按照权限管理部门办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    new UsematerialTotalStat().departmentnatureusmaterialschoolstatus(useMaterialChildData,date,schoolData)

    //按各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areauseNatureSchoolStatusData = usematerialdealtotaldata.filter(x => !x._10._1.equals("null")).map(_._10)
    new UsematerialTotalStat().areanatureusmaterialschoolstatus(useMaterialChildData, date, areauseNatureSchoolStatusData, schoolData)

    //按照权限管理部门各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    new UsematerialTotalStat().departmentareanatureusmaterialschoolstatus(useMaterialChildData,date,schoolData)

    //按上海市各类型学校来用料计划各状态数据统计（level）
    val useLevelStatusData = usematerialdealtotaldata.filter(x => !x._7._1.equals("null")).map(_._7)
    new UsematerialTotalStat().levelusmaterialstatus(usematerialDealData, date, useLevelStatusData, schoolData)

    //按照权限管理部门各类型学校来用料计划各状态数据统计（level）
    new UsematerialTotalStat().departmentlevelusmaterialstatus(usematerialDealData,date,schoolData)

    //按各区各类型学校来用料计划各状态数据统计（level）
    val areauseLevelStatusData = usematerialdealtotaldata.filter(x => !x._11._1.equals("null")).map(_._11)
    new UsematerialTotalStat().arealevelusmaterialstatus(usematerialDealData, date, areauseLevelStatusData, schoolData)

    //按照权限管理部门各区各类型学校来用料计划各状态数据统计（level）
    new UsematerialTotalStat().departmentarealevelusmaterialstatus(usematerialDealData,date,schoolData)

    //按上海市各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val useLevelSchoolStatusData = usematerialdealtotaldata.filter(x => !x._8._1.equals("null")).map(_._8)
    new UsematerialTotalStat().levelusmaterialschoolstatus(useMaterialChildData, date, useLevelSchoolStatusData, schoolData)

    //按照权限管理部门各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    new UsematerialTotalStat().departmentlevelusmaterialschoolstatus(useMaterialChildData,date,schoolData)

    //按各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val areauseLevelSchoolStatusData = usematerialdealtotaldata.filter(x => !x._12._1.equals("null")).map(_._12)
    new UsematerialTotalStat().arealevelusmaterialschoolstatus(useMaterialChildData, date, areauseLevelSchoolStatusData, schoolData)

    //按照权限管理部门各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    new UsematerialTotalStat().departmentarealevelusmaterialschoolstatus(useMaterialChildData,date,schoolData)

    //按照各管理部门各状态用料计划数量
    val departmentUseStatusTotal = usematerialdealtotaldata.filter(x => !x._13.equals("null")).map(_._13)
    new UsematerialTotalStat().departmentusmaterialstatus(usematerialDealData,date,departmentUseStatusTotal,schoolData)

    //按照权限各管理部门各状态用料计划数量
    new UsematerialTotalStat().departmentdepartmentusmaterialstatus(usematerialDealData,date,schoolData)

    //按照各管理部门各状态用料计划数量(对学校去重了)
    val departmentUseSchoolStatusTotal = usematerialdealtotaldata.filter(x => !x._14.equals("null")).map(_._14)
    new UsematerialTotalStat().departmentusmaterialschoolstatus(useMaterialChildData,date,departmentUseSchoolStatusTotal,schoolData)

    //按照权限各管理部门各状态用料计划数量(对学校去重了)
    new UsematerialTotalStat().departmentdepartmentusmaterialschoolstatus(useMaterialChildData,date,schoolData)

    //处理好的配送计划数据
   val distributionDealData = new DealDataStat().distributiondealdata(distributionData, gongcanSchool,school2Area,date)
    //处理好的配送计划统计数据
    val distributionDealTotalData = new DealDataStat().distributiondealtotaldata(distributionTotalData)

    //各区配送计划总数据统计
    new DistributionTotalStat().areadistributionltotal(distributionDealData,date)

    //按照权限的各区配送计划总数据统计
    new DistributionTotalStat().departmentareadistributionltotal(distributionDealData,date,schoolData)

    //各区配送计划各状态数据统计
    val disStatusData = distributionDealTotalData.filter(x => x._1.equals("area")).map(_._2)
    new DistributionTotalStat().areadistributionstatustotal(distributionDealData,date,disStatusData)

    //按照权限的各区配送计划各状态数据统计
    new DistributionTotalStat().departmentareadistributionstatustotal(distributionDealData,date,schoolData)

    //各区配送计划各状态按照学校数据统计(对学校进行去重处理)
    val disSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-area")).map(_._2)
    new DistributionTotalStat().areadistributionschoolstatus(distributionchildData,date,disSchoolStatusData)

    //按照权限的各区配送计划各状态按照学校数据统计(对学校进行去重处理)
    new DistributionTotalStat().departmentareadistributionschoolstatus(distributionchildData,date,schoolData)

    //按所属教育部配送计划各状态数据统计
    val disMasteridStatusData = distributionDealTotalData.filter(x => x._1.equals("masterid")).map(_._2)
    new DistributionTotalStat().masteriddistributiontotal(distributionDealData,date,disMasteridStatusData, schoolData, commiteeid2commiteename)

    //按照权限的所属教育部配送计划各状态数据统计
    new DistributionTotalStat().departmentmasteriddistributiontotal(distributionDealData,date,schoolData,commiteeid2commiteename)

    //按所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
    val disMasteridSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-masterid")).map(_._2)
    new DistributionTotalStat().masteriddistributionschoolstatus(distributionchildData,date,disMasteridSchoolStatusData,schoolData,commiteeid2commiteename)

    //按照权限的所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
    new DistributionTotalStat().departmentmasteriddistributionschoolstatusdata(distributionchildData,date,schoolData,commiteeid2commiteename)

    //按上海市办学性质来配送计划各状态数据统计（nature）
    val disNatureStatusData = distributionDealTotalData.filter(x => x._1.equals("nature")).map(_._2)
    new DistributionTotalStat().naturedistributionstatus(distributionDealData,date,disNatureStatusData,schoolData)

    //按照权限的上海市办学性质来配送计划各状态数据统计（nature）
    new DistributionTotalStat().departmentnaturedistributionstatus(distributionDealData,date,schoolData)

    //各区办学性质来配送计划各状态数据统计（nature）
    val areadisNatureStatusData = distributionDealTotalData.filter(x => x._1.equals("nat-area")).map(_._2)
    new DistributionTotalStat().areanaturedistributionstatus(distributionDealData,date,areadisNatureStatusData,schoolData)

    //按上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val disNatureSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-nature")).map(_._2)
    new DistributionTotalStat().naturedistributionschoolstatus(distributionchildData,date,disNatureSchoolStatusData,schoolData)

    //按照权限的上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    new DistributionTotalStat().departmentnaturedistributionschoolstatus(distributionchildData,date,schoolData)

    //按各区办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areadisNatureSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-nat-area")).map(_._2)
    new DistributionTotalStat().areanaturedistributionschoolstatus(distributionchildData,date,areadisNatureSchoolStatusData,schoolData)

    //按上海市各类型学校来配送计划各状态数据统计（level）
    val disLevelStatusData = distributionDealTotalData.filter(x => x._1.equals("level")).map(_._2)
    new DistributionTotalStat().leveldistributionstatus(distributionDealData,date,disLevelStatusData,schoolData)

    //按照权限的上海市各类型学校来配送计划各状态数据统计（level）
    new DistributionTotalStat().departmentleveldistributionstatus(distributionDealData,date,schoolData)

    //按各区各类型学校来配送计划各状态数据统计（level）
    val areadisLevelStatusData = distributionDealTotalData.filter(x => x._1.equals("lev-area")).map(_._2)
    new DistributionTotalStat().arealeveldistributionstatus(distributionDealData,date,areadisLevelStatusData,schoolData)

    //按上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val disLevelSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-level")).map(_._2)
    new DistributionTotalStat().leveldistributionschoolstatus(distributionchildData,date,disLevelSchoolStatusData,schoolData)

    //按照权限的上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    new DistributionTotalStat().departmentleveldistributionschoolstatus(distributionchildData,date,schoolData)

    //按各区各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val areadisLevelSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-lev-area")).map(_._2)
    new DistributionTotalStat().arealeveldistributionschoolstatus(distributionchildData,date,areadisLevelSchoolStatusData,schoolData)

    //按照各区的验收操作规范的统计
    val areaDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-area")).map(_._2)
    new DistributionTotalStat().areadisstatus(distributionDealData,date,areaDisStatusData)

    //按照权限的各区的验收操作规范的统计
    new DistributionTotalStat().departmentareadisstatus(distributionDealData,date,schoolData)

    //按照各区的验收操作规范的统计(对学校去重)
    val areaSchoolDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-school-area")).map(_._2)
    new DistributionTotalStat().areaschooldisstatus(distributionchildData,date,areaSchoolDisStatusData)

    //按照权限的各区的验收操作规范的统计(对学校去重)
    new DistributionTotalStat().departmentareaschooldisstatus(distributionchildData,date,schoolData)

    //按照上海市办学性质的验收操作规范的统计
    val natureDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-nature")).map(_._2)
    new DistributionTotalStat().naturedisstatus(distributionDealData,date,natureDisStatusData,schoolData)

    //按照权限的上海市办学性质的验收操作规范的统计
    new DistributionTotalStat().departmentnaturedisstatus(distributionDealData,date,schoolData)

    //按照上海市办学性质的验收操作规范的统计(对学校去重)
    val natureSchoolDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-school-nature")).map(_._2)
    new DistributionTotalStat().natureschooldisstatus(distributionchildData,date,natureSchoolDisStatusData,schoolData)

    //按照权限的上海市办学性质的验收操作规范的统计(对学校去重)
    new DistributionTotalStat().departmentnatureschooldisstatus(distributionchildData,date,schoolData)

    //按照上海市办学学制的验收操作规范的统计
    val levelDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-level")).map(_._2)
    new DistributionTotalStat().leveldisstatus(distributionDealData,date,levelDisStatusData,schoolData)

    //按照权限的上海市办学学制的验收操作规范的统计
    new DistributionTotalStat().departmentleveldisstatus(distributionDealData,date,schoolData)

    //按照上海市办学学制的验收操作规范的统计(对学校去重)
    val levelSchoolDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-school-level")).map(_._2)
    new DistributionTotalStat().levelschooldisstatus(distributionchildData,date,levelSchoolDisStatusData,schoolData)

    //按照权限的上海市办学学制的验收操作规范的统计(对学校去重)
    new DistributionTotalStat().departmentlevelschooldisstatus(distributionchildData,date,schoolData)

    //按照上海市教属的验收操作规范的统计
    val masteridDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-masterid")).map(_._2)
    new DistributionTotalStat().masteriddisstatus(distributionDealData,date,masteridDisStatusData,schoolData,commiteeid2commiteename)

    //按照权限的上海市教属的验收操作规范的统计
    new DistributionTotalStat().departmentmasteriddisstatus(distributionDealData,date,schoolData,commiteeid2commiteename)

    //按照上海市教属的验收操作规范的统计(对学校去重)
    val masteridSchoolDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-school-masterid")).map(_._2)
    new DistributionTotalStat().masteridschooldisstatus(distributionchildData,date,masteridSchoolDisStatusData,schoolData,commiteeid2commiteename)

    //按照权限的上海市教属的验收操作规范的统计(对学校去重)
    new DistributionTotalStat().departmentmasteridschooldisstatus(distributionchildData,date,schoolData,commiteeid2commiteename)

    //按照上海市管理部门的配送各状态数量
    val departStatusData = distributionDealTotalData.filter(x => x._1.equals("department")).map(_._2)
    new DistributionTotalStat().dedistributionstatustotal(distributionDealData,date,departStatusData,schoolData)

    //按照权限的上海市管理部门的配送各状态数量
    new DistributionTotalStat().dedepartmentdistributionstatustotal(distributionDealData,date,schoolData)

    //按照上海市管理部门的配送各状态数量(对学校去重)
    val departSchoolStatusData = distributionDealTotalData.filter(x => x._1.equals("school-department")).map(_._2)
    new DistributionTotalStat().dedistributionschoolstatustotal(distributionchildData,date,departSchoolStatusData,schoolData)

    //按照权限的上海市管理部门的配送各状态数量(对学校去重)
    new DistributionTotalStat().dedepartmentdistributionschoolstatustotal(distributionchildData,date,schoolData)

    //按照上海市管理部门的验收操作规范数量
    val departDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-department")).map(_._2)
    new DistributionTotalStat().dedisstatustotal(distributionDealData,date,departDisStatusData,schoolData)

    //按照权限的上海市管理部门的验收操作规范数量
    new DistributionTotalStat().dedepartmentdisstatustotal(distributionDealData,date,schoolData)

    //按照上海市管理部门的验收操作规范数量(对学校去重)
    val departSchoolDisStatusData = distributionDealTotalData.filter(x => x._1.equals("dis-school-department")).map(_._2)
    new DistributionTotalStat().deschooldisstatustotal(distributionchildData,date,departSchoolDisStatusData,schoolData)

    //按照权限的上海市管理部门的验收操作规范数量(对学校去重)
    new DistributionTotalStat().dedepartmentschooldisstatustotal(distributionchildData,date,schoolData)

    //处理好的留样计划数据
    val retentionDealData = new DealDataStat().retentiondealdata(retentionData)
    //处理好的留样计划统计数据
    val retentiondealtotaldata = new DealDataStat().retentiondealtotaldata(retentionTotalData)

    //各区留样计划总数据统计
    new RetentionTotalStat().arearetentiontotal(retentionDealData,date)

    //按照管理权限各区留样计划统计数据
    new RetentionTotalStat().departmentarearetentiontotal(retentionDealData,date,schoolData)

    //各区留样计划各状态数据统计
    val reStatusData = retentiondealtotaldata.filter(x => x._1.equals("area")).map(_._2)
    new RetentionTotalStat().arearetentionstatustotal(retentionDealData,date,reStatusData)

    //按照权限各区留样计划各状态数据统计
    new RetentionTotalStat().departmentarearetentionstatustotal(retentionDealData,date,schoolData)

    //各区留样操作规则数据统计
    val reRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-area")).map(_._2)
    new RetentionTotalStat().arearetentionrulestatustotal(retentionDealData,date,reRuleStatusData)

    //按照权限的各区留样操作规则数据统计
    new RetentionTotalStat().departmentarearetentionrulestatustotal(retentionDealData,date,schoolData)

    //各区留样计划各状态按照学校数据统计(对学校进行去重处理)
    val reSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-area")).map(_._2)
    new RetentionTotalStat().arearetentionschoolstatus(retentionchildData,date,reSchoolStatusData)

    //按照权限各区留样计划各状态按照学校数据统计(对学校进行去重处理)
    new RetentionTotalStat().departmentarearetentionschoolstatus(retentionchildData,date,schoolData)

    //各区留样规则各状态按照学校数据统计(对学校进行去重处理)
    val reSchoolRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-school-area")).map(_._2)
    new RetentionTotalStat().arearetentionschoolrulestatus(retentionchildData,date,reSchoolRuleStatusData)

    //按照权限各区留样规则各状态按照学校数据统计(对学校进行去重处理)
    new RetentionTotalStat().departmentarearetentionschoolrulestatus(retentionchildData,date,schoolData)

    //按所属教育部留样计划各状态数据统计
    val reMasteridStatusData = retentiondealtotaldata.filter(x => x._1.equals("masterid")).map(_._2)
    new RetentionTotalStat().masteridretentiontotal(retentionDealData,date,reMasteridStatusData,schoolData,commiteeid2commiteename)

    //按照权限的所属留样计划各状态数据统计
    new RetentionTotalStat().departmentmasteridretentiontotal(retentionDealData,date,schoolData,commiteeid2commiteename)

    //按所属教育部留样操作规则数据统计
    val reMasteridRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-masterid")).map(_._2)
    new RetentionTotalStat().masteridretentionruletotal(retentionDealData,date,reMasteridRuleStatusData,schoolData,commiteeid2commiteename)

    //按权限所属教育部留样操作规则数据统计
    new RetentionTotalStat().departmentmasteridretentionruletotal(retentionDealData,date,schoolData,commiteeid2commiteename)

    //按所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
    val reMasteridSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-masterid")).map(_._2)
    new RetentionTotalStat().masteridretentionschoolstatus(retentionchildData,date,reMasteridSchoolStatusData,schoolData,commiteeid2commiteename)

    //按权限所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
    new RetentionTotalStat().departmentmasteridretentionschoolstatus(retentionchildData,date,schoolData,commiteeid2commiteename)

    //按所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)
    val reMasteridSchoolRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-school-masterid")).map(_._2)
    new RetentionTotalStat().masteridretentionschoolrulestatus(retentionchildData,date,reMasteridSchoolRuleStatusData,schoolData,commiteeid2commiteename)

    //按权限所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)
    new RetentionTotalStat().departmentmasteridretentionschoolrulestatus(retentionchildData,date,schoolData,commiteeid2commiteename)

    //按上海市办学性质来留样计划各状态数据统计（nature）
    val reNatureStatusData = retentiondealtotaldata.filter(x => x._1.equals("nature")).map(_._2)
    new RetentionTotalStat().natureretentionstatus(retentionDealData,date,reNatureStatusData,schoolData)

    //按权限上海市办学性质来留样计划各状态数据统计（nature）
    new RetentionTotalStat().departmentnatureretentionstatus(retentionDealData,date,schoolData)

    //按上海市办学性质来留样操作规则各状态数据统计（nature）
    val reNatureRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-nature")).map(_._2)
    new RetentionTotalStat().natureretentionrulestatus(retentionDealData,date,reNatureRuleStatusData,schoolData)

    //按权限上海市办学性质来留样操作规则各状态数据统计（nature）
    new RetentionTotalStat().departmentnatureretentionrulestatus(retentionDealData,date,schoolData)

    //各区办学性质来留样计划各状态数据统计（nature）
    val areareNatureStatusData = retentiondealtotaldata.filter(x => x._1.equals("nat-area")).map(_._2)
    new RetentionTotalStat().areanatureretentionstatus(retentionDealData,date,areareNatureStatusData,schoolData)

    //权限各区办学性质来留样计划各状态数据统计（nature）
    new RetentionTotalStat().departmentareanatureretentionstatus(retentionDealData,date,schoolData)

    //按上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val reNatureSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-nature")).map(_._2)
    new RetentionTotalStat().natureretentionschoolstatus(retentionchildData,date,reNatureSchoolStatusData,schoolData)

    //按权限上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    new RetentionTotalStat().departmentnatureretentionschoolstatus(retentionchildData,date,schoolData)

    //按上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）
    val reNatureSchoolRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-school-nature")).map(_._2)
    new RetentionTotalStat().natureretentionschoolrulestatus(retentionchildData,date,reNatureSchoolRuleStatusData,schoolData)

    //按权限上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）
    new RetentionTotalStat().departmentnatureretentionschoolrulestatus(retentionchildData,date,schoolData)

    //按各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areareNatureSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-nat-area")).map(_._2)
    new RetentionTotalStat().areanatureretentionschoolstatus(retentionchildData,date,areareNatureSchoolStatusData,schoolData)

    //按权限各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    new RetentionTotalStat().departmentreanatureretentionschoolstatus(retentionchildData,date,schoolData)


    //按上海市各类型学校来留样计划各状态数据统计（level）
    val reLevelStatusData = retentiondealtotaldata.filter(x => x._1.equals("level")).map(_._2)
    new RetentionTotalStat().levelreretentionstatus(retentionDealData,date,reLevelStatusData,schoolData)

    //按权限上海市各类型学校来留样计划各状态数据统计（level）
    new RetentionTotalStat().departmentlevelreretentionstatus(retentionDealData,date,schoolData)

    //按上海市各类型学校来留样操作规则数据统计（level）
    val reLevelRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-level")).map(_._2)
    new RetentionTotalStat().levelreretentionrulestatus(retentionDealData,date,reLevelRuleStatusData,schoolData)

    //按权限上海市各类型学校来留样操作规则数据统计（level）
    new RetentionTotalStat().departmentlevelreretentionrulestatus(retentionDealData,date,schoolData)

    //按各区各类型学校来留样计划各状态数据统计（level）
    val areareLevelStatusData = retentiondealtotaldata.filter(x => x._1.equals("lev-area")).map(_._2)
    new RetentionTotalStat().arealevelreretentionstatus(retentionDealData,date,areareLevelStatusData,schoolData)

    //按权限各区各类型学校来留样计划各状态数据统计（level）
    new RetentionTotalStat().departmentarealevelreretentionstatus(retentionDealData,date,schoolData)

    //按上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val reLevelSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-level")).map(_._2)
    new RetentionTotalStat().levelreretentionschoolstatus(retentionchildData,date,reLevelSchoolStatusData,schoolData)

    //按权限上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    new RetentionTotalStat().departmentlevelreretentionschoolstatus(retentionchildData,date,schoolData)

    //按上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）
    val reLevelSchoolRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-school-level")).map(_._2)
    new RetentionTotalStat().levelreretentionschoolrulestatus(retentionchildData,date,reLevelSchoolRuleStatusData,schoolData)

    //按权限上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）
    new RetentionTotalStat().departmentlevelreretentionschoolrulestatus(retentionchildData,date,schoolData)

    //按各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val areareLevelSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-lev-area")).map(_._2)
    new RetentionTotalStat().arealevelreretentionschoolstatus(retentionchildData,date,areareLevelSchoolStatusData,schoolData)

    //按权限各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    new RetentionTotalStat().departmentareanatureretentionschoolstatus(retentionchildData,date,schoolData)


    //按管理部门留样计划各状态数据统计
    val departmentStatusData = retentiondealtotaldata.filter(x => x._1.equals("department")).map(_._2)
    new RetentionTotalStat().departmentstatus(retentionDealData,date,departmentStatusData,schoolData)

    //按权限管理部门留样计划各状态数据统计
    new RetentionTotalStat().departmentdepartmentstatus(retentionDealData,date,schoolData)

    //按管理部门留样计划操作规则统计
    val departmentRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-department")).map(_._2)
    new RetentionTotalStat().departmentrulestatus(retentionDealData,date,departmentRuleStatusData,schoolData)

    //按权限管理部门留样计划操作规则统计
    new RetentionTotalStat().departmentdepartmentrulestatus(retentionDealData,date,schoolData)

    //按管理部门留样计划各状态数据统计 (对学校进行去重处理）
    val departmentSchoolStatusData = retentiondealtotaldata.filter(x => x._1.equals("school-department")).map(_._2)
    new RetentionTotalStat().departmentschoolstatus(retentionchildData,date,departmentSchoolStatusData,schoolData)

    //按权限管理部门留样计划各状态数据统计 (对学校进行去重处理）
    new RetentionTotalStat().departmentdepartmentschoolstatus(retentionchildData,date,schoolData)

    //按管理部门留样操作规则统计数据统计 (对学校进行去重处理）
    val departmentSchoolRuleStatusData = retentiondealtotaldata.filter(x => x._1.equals("re-school-department")).map(_._2)
    new RetentionTotalStat().departmentschoolrulestatus(retentionchildData,date,departmentSchoolRuleStatusData,schoolData)

    //按权限管理部门留样操作规则统计数据统计 (对学校进行去重处理）
    new RetentionTotalStat().departmentdepartmentschoolrulestatus(retentionchildData,date,schoolData)

    val platoon_feed = jedis.hgetAll(date + "_platoon-feed")
    val plaData = sc.parallelize(platoon_feed.asScala.toList) //redis中存

    // 排菜，用料，配送操作情况

    val edu_all = jedis.hgetAll(date + "_allUseData")
    val eduAllData = sc.parallelize(edu_all.asScala.toList) //redis中存的所有维度的数据

    new EduAllDataStat().platoonmaterialdetailresert(plaData,useMaterialChildData,distributionchildData,retentionchildData,schoolData,eduAllData,date,commiteeid2commiteeid,schoolid2suppliername)



    sc.stop()

  }

}
