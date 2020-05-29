package com.ssic.yesreport

import java.util
import java.util.{Calendar, Date}

import com.ssic.service._
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object YesTargetTotal {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("昨日用料，验收，留样统计数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val time = calendar.getTime
    val date = format.format(time)

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val committee = session.read.jdbc(url, edu_committee, conn)
    val supplier = session.read.jdbc(url, pro_supplier, conn)
    val bd_department = session.read.jdbc(url, edu_bd_department, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    committee.createTempView("t_edu_committee")
    supplier.createTempView("t_pro_supplier")
    bd_department.createTempView("t_edu_bd_department")


    val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
    val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
    val schoolData = sc.broadcast(Tools.schoolNew(session)) //学校基础信息
    val commiteeid2commiteename = sc.broadcast(Tools.commiteeid2commiteename(session)) //教属名字信息
    val commiteeid2commiteeid = sc.broadcast(Tools.school2Committee2(session)) //教属id信息
    val schoolid2suppliername = sc.broadcast(Tools.schoolid2suppliername(session)) //团餐公司名字
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


    //用料计划
    //处理好的用料计划数据
    val usematerialDealData = new DealDataStat().usematerialdealdata(usematerialData, projid2schoolid, projid2schoolname, gongcanSchool,projid2Area)

    //各区用料计划总数据统计
    val areausematerialtotal = new UsematerialTotalStat().areatotal(usematerialDealData, date)
    //市教委各区用料计划各状态数据统计
    val areausematerialstatustotal = new UsematerialTotalStat().areastatustotal(usematerialDealData, date)
    //按所属教育部用料计划各状态数据统计
    val masteridusematerialtotal = new UsematerialTotalStat().masteridtotal(usematerialDealData, date, schoolData, commiteeid2commiteename)
    //按上海市办学性质来用料计划各状态数据统计（nature）
    val natureusematerialstatus = new UsematerialTotalStat().naturestatus(usematerialDealData, date, schoolData)
    //各区办学性质来用料计划各状态数据统计（nature）
    val areanatureusematerialstatus = new UsematerialTotalStat().areanaturestatus(usematerialDealData, date, schoolData)
    //按上海市各类型学校来用料计划各状态数据统计（level）
    val levelusematerialstatus = new UsematerialTotalStat().levelstatus(usematerialDealData, date, schoolData)
    //按各区各类型学校来用料计划各状态数据统计（level）
    val arealevelusematerialstatus = new UsematerialTotalStat().arealevelstatus(usematerialDealData, date, schoolData)
    //按照各管理部门各状态用料计划数量
    val departmentusematerialstatus = new UsematerialTotalStat().departmentstatus(usematerialDealData,date,schoolData)

    //各区用料计划各状态按照学校数据统计(对学校进行去重处理)
    val areausematerialschooltotal = new UsematerialSchoolTotalStat().areatotal(useMaterialChildData, date)
    //按所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
    val masteridusematerialschooltotal = new UsematerialSchoolTotalStat().masteridtotal(useMaterialChildData, date, schoolData, commiteeid2commiteename)
    //按上海市办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val natureusematerialschooltotal = new UsematerialSchoolTotalStat().naturetotal(useMaterialChildData, date, schoolData)
    //按各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areanatureusematerialschooltotal = new UsematerialSchoolTotalStat().areanaturetotal(useMaterialChildData, date, schoolData)
    //按上海市各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val levelusematerialschooltotal = new UsematerialSchoolTotalStat().leveltotal(useMaterialChildData, date, schoolData)
    //按各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val arealevelusematerialschooltotal = new UsematerialSchoolTotalStat().arealeveltotal(useMaterialChildData, date, schoolData)
    //按照各管理部门各状态用料计划数量(对学校去重了)
    val shanghaidepartmentusematerialschooltotal = new UsematerialSchoolTotalStat().shanghaidepartmenttotal(useMaterialChildData, date, schoolData)

    //按照权限管理部门各区用料计划总数据统计
    val dpareatotal = new UsematerialTotalStat().departmentareatotal(usematerialDealData, date,schoolData)
    //按照权限管理部门各区用料计划各状态数据统计
    val dpareastatustotal = new UsematerialTotalStat().departmentareastatustotal(usematerialDealData, date,schoolData)
    //按照权限管理部门所属教育部用料计划各状态数据统计
    val dpmasteridtotal = new UsematerialTotalStat().departmentmasteridtotal(usematerialDealData, date, schoolData, commiteeid2commiteename)
    //按照权限管理部门办学性质来用料计划各状态数据统计（nature）
    val dpnaturestatus = new UsematerialTotalStat().departmentnaturestatus(usematerialDealData, date, schoolData)
    //按照权限管理部门各区办学性质来用料计划各状态数据统计（nature）
    val dpareanatureusematerialstatus = new UsematerialTotalStat().departmentareanaturestatus(usematerialDealData, date, schoolData)
    //按照权限管理部门各类型学校来用料计划各状态数据统计（level）
    val dplevelstatus = new UsematerialTotalStat().departmentlevelstatus(usematerialDealData, date, schoolData)
    //按照权限管理部门各区各类型学校来用料计划各状态数据统计（level）
    val dparealevelstatus = new UsematerialTotalStat().departmentarealevelstatus(usematerialDealData, date, schoolData)
    //按照权限各管理部门各状态用料计划数量
    val dpdepartmentstatus = new UsematerialTotalStat().departmentdepartmentstatus(usematerialDealData,date,schoolData)

    //按照权限管理部门各区用料计划各状态按照学校数据统计(对学校进行去重处理)
    val dpareausematerialschooltotal = new UsematerialSchoolTotalStat().departmentareatotal(useMaterialChildData, date,schoolData)
    //按照权限管理部门所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
    val dpmasteridusematerialschooltotal = new UsematerialSchoolTotalStat().departmentmasteridtotal(useMaterialChildData, date, schoolData, commiteeid2commiteename)
    //按照权限管理部门办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val dpnatureusematerialschooltotal = new UsematerialSchoolTotalStat().departmentnaturetotal(useMaterialChildData, date, schoolData)
    //按照权限管理部门各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val dpareanatureusematerialschooltotal = new UsematerialSchoolTotalStat().departmentareanaturetotal(useMaterialChildData, date, schoolData)
    //按照权限管理部门各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val dplevelusematerialschooltotal = new UsematerialSchoolTotalStat().departmentleveltotal(useMaterialChildData, date, schoolData)
    //按照权限管理部门各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
    val dparealevelusematerialschooltotal = new UsematerialSchoolTotalStat().departmentarealeveltotal(useMaterialChildData, date, schoolData)
    //按照权限各管理部门各状态用料计划数量(对学校去重了)
    val dpshanghaidepartmentusematerialschooltotal = new UsematerialSchoolTotalStat().departmentdepartmenttotal(useMaterialChildData, date, schoolData)


    //验收计划
    //处理好的配送计划数据
    val distributionDealData = new DealDataStat().distributiondealdata(distributionData, gongcanSchool,school2Area,date)

    //各区配送计划总数据统计
    val areadistributiontotal = new DistributionTotalStat().areatotal(distributionDealData,date)
    //各区配送计划各状态数据统计
    val areadistributionstatustotal = new DistributionTotalStat().areastatustotal(distributionDealData,date)
    //按所属教育部配送计划各状态数据统计
    val masteriddistributiontotal = new DistributionTotalStat().masteridtotal(distributionDealData,date, schoolData, commiteeid2commiteename)
    //按上海市办学性质来配送计划各状态数据统计（nature）
    val naturedistributionstatus = new DistributionTotalStat().naturestatus(distributionDealData,date,schoolData)
    //各区办学性质来配送计划各状态数据统计（nature）
    val areanaturedistributionstatus = new DistributionTotalStat().areanaturestatus(distributionDealData,date,schoolData)
    //按上海市各类型学校来配送计划各状态数据统计（level）
    val leveldistributionstatus = new DistributionTotalStat().levelstatus(distributionDealData,date,schoolData)
    //按各区各类型学校来配送计划各状态数据统计（level）
    val arealeveldistributionstatus = new DistributionTotalStat().arealevelstatus(distributionDealData,date,schoolData)
    //按照上海市管理部门的配送各状态数量
    val departmentdistributionstatus = new DistributionTotalStat().departmentstatus(distributionDealData,date,schoolData)


    //各区配送计划各状态按照学校数据统计(对学校进行去重处理)
    val areaschooldistributiontotal = new DistributionSchoolTotalStat().areatotal(distributionchildData,date)
    //按所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
    val masteridschooldistributiontotal = new DistributionSchoolTotalStat().masteridtotal(distributionchildData,date,schoolData,commiteeid2commiteename)
    //按上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val natureschooldistributiontotal = new DistributionSchoolTotalStat().naturetotal(distributionchildData,date,schoolData)
    //按各区办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areanatureschooldistributiontotal = new DistributionSchoolTotalStat().areanaturetotal(distributionchildData,date,schoolData)
    //按上海市经营模式来配送计划各状态按照学校数据统计（canteenmode,对学校进行去重处理）
    val canteenschooldistributiontotal = new DistributionSchoolTotalStat().canteentotal(distributionchildData,date,schoolData)
    //按上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val levelschooldistributiontotal = new DistributionSchoolTotalStat().leveltotal(distributionchildData,date,schoolData)
    //按各区各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val arealevelschooldistributiontotal = new DistributionSchoolTotalStat().arealeveltotal(distributionchildData,date,schoolData)
    //按照上海市管理部门的配送各状态数量(对学校去重)
    val shdepartmentdistributiontotal = new DistributionSchoolTotalStat().shanghaidepartmenttotal(distributionchildData,date,schoolData)

    //按照各区的验收操作规范的统计
    val areadistributionruletotal = new DistributionRuleTotalStat().areatotal(distributionDealData,date)
    //按照上海市办学性质的验收操作规范的统计
    val naturedistributionrulestatus = new DistributionRuleTotalStat().naturestatus(distributionDealData,date,schoolData)
    //按照上海市办学学制的验收操作规范的统计
    val leveldistributionrulestatus = new DistributionRuleTotalStat().levelstatus(distributionDealData,date,schoolData)
    //按照上海市教属的验收操作规范的统计
    val masteriddistributionruletotal = new DistributionRuleTotalStat().masteridtotal(distributionDealData,date,schoolData,commiteeid2commiteename)
    //按照上海市管理部门的验收操作规范数量
    val departmentdistributionrulestatus = new DistributionRuleTotalStat().departmentstatus(distributionDealData,date,schoolData)

    //按照各区的验收操作规范的统计(对学校去重)
    val areadistributionschoolruletotal = new DistributionRuleSchoolTotalStat().areatotal(distributionchildData,date)
    //按照上海市办学性质的验收操作规范的统计(对学校去重)
    val naturedistributionschoolruletotal = new DistributionRuleSchoolTotalStat().naturetotal(distributionchildData,date,schoolData)
    //按照上海市办学学制的验收操作规范的统计(对学校去重)
    val leveldistributionschoolruletotal = new DistributionRuleSchoolTotalStat().leveltotal(distributionchildData,date,schoolData)
    //按照上海市教属的验收操作规范的统计(对学校去重)
    val masteriddistributionschoolruletotal = new DistributionRuleSchoolTotalStat().masteridtotal(distributionchildData,date,schoolData,commiteeid2commiteename)
    //按照上海市管理部门的验收操作规范数量(对学校去重)
    val shdepartmentdistributionschoolruletotal = new DistributionRuleSchoolTotalStat().shanghaidepartmenttotal(distributionchildData,date,schoolData)

    //按照权限的各区配送计划总数据统计
    val dpareadistributiontotal = new DistributionTotalStat().departmentareatotal(distributionDealData,date,schoolData)
    //按照权限的各区配送计划各状态数据统计
    val dpareadistributionstatustotal = new DistributionTotalStat().departmentareastatustotal(distributionDealData,date,schoolData)
    //按照权限的所属教育部配送计划各状态数据统计
    val dpmasteriddistributiontotal = new DistributionTotalStat().departmentmasteridtotal(distributionDealData,date,schoolData,commiteeid2commiteename)
    //按照权限的上海市办学性质来配送计划各状态数据统计（nature）
    val dpnaturedistributionstatus = new DistributionTotalStat().departmentnaturestatus(distributionDealData,date,schoolData)
    //按照权限的上海市各类型学校来配送计划各状态数据统计（level）
    val dpleveldistributionstatus = new DistributionTotalStat().departmentlevelstatus(distributionDealData,date,schoolData)
    //按照权限的上海市管理部门的配送各状态数量
    val dpdepartmentdistributionstatus = new DistributionTotalStat().departmentdepartmentstatus(distributionDealData,date,schoolData)

    //按照权限的各区配送计划各状态按照学校数据统计(对学校进行去重处理)
    val dpareadistributionschooltotal = new DistributionSchoolTotalStat().departmentareatotal(distributionchildData,date,schoolData)
    //按照权限的所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
    val dpmasteriddistributionschooltotal = new DistributionSchoolTotalStat().departmentmasteridtotal(distributionchildData,date,schoolData,commiteeid2commiteename)
    //按照权限的上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val dpnaturedistributionschooltotal = new DistributionSchoolTotalStat().departmentnaturetotal(distributionchildData,date,schoolData)
    //按权限的上海市经营模式来配送计划各状态按照学校数据统计（canteenmode,对学校进行去重处理）
    val dpcanteendistributionschooltotal = new DistributionSchoolTotalStat().departmentcanteentotal(distributionchildData,date,schoolData)
    //按照权限的上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
    val dpleveldistributionschooltotal = new DistributionSchoolTotalStat().departmentleveltotal(distributionchildData,date,schoolData)
    //按照权限的上海市管理部门的配送各状态数量(对学校去重)
    val dpdepartmentdistributionschooltotal = new DistributionSchoolTotalStat().departmentdepartmenttotal(distributionchildData,date,schoolData)

    //按照权限的各区的验收操作规范的统计
    val dpareadistributionruletotal = new DistributionRuleTotalStat().departmentareatotal(distributionDealData,date,schoolData)
    //按照权限的上海市办学性质的验收操作规范的统计
    val dpnaturedistributionrulestatus = new DistributionRuleTotalStat().departmentnaturestatus(distributionDealData,date,schoolData)
    //按照权限的上海市办学学制的验收操作规范的统计
    val dpleveldistributionrulestatus = new DistributionRuleTotalStat().departmentlevelstatus(distributionDealData,date,schoolData)
    //按照权限的上海市教属的验收操作规范的统计
    val dpmasteriddistributionruletotal = new DistributionRuleTotalStat().departmentmasteridtotal(distributionDealData,date,schoolData,commiteeid2commiteename)
    //按照权限的上海市管理部门的验收操作规范数量
    val dpdepartmentdistributionrulestatus = new DistributionRuleTotalStat().departmentdepartmentstatus(distributionDealData,date,schoolData)

    //按照权限的各区的验收操作规范的统计(对学校去重)
    val dpareadistributionschoolruletotal = new DistributionRuleSchoolTotalStat().departmentareatotal(distributionchildData,date,schoolData)
    //按照权限的上海市办学性质的验收操作规范的统计(对学校去重)
    val dpnaturedistributionschoolruletotal = new DistributionRuleSchoolTotalStat().departmentnaturetotal(distributionchildData,date,schoolData)
    //按照权限的上海市办学学制的验收操作规范的统计(对学校去重)
    val dpleveldistributionschoolruletotal = new DistributionRuleSchoolTotalStat().departmentleveltotal(distributionchildData,date,schoolData)
    //按照权限的上海市教属的验收操作规范的统计(对学校去重)
    val dpmasteriddistributionschoolruletotal = new DistributionRuleSchoolTotalStat().departmentmasteridtotal(distributionchildData,date,schoolData,commiteeid2commiteename)
    //按照权限的上海市管理部门的验收操作规范数量(对学校去重)
    val dpdepartmentdistributionschoolruletotal = new DistributionRuleSchoolTotalStat().departmentdepartmenttotal(distributionchildData,date,schoolData)


    //留样
    //处理好的留样计划数据
    val retentionDealData = new DealDataStat().retentiondealdata(retentionData)

    //各区留样计划总数据统计
    val arearetentiontotal = new RetentionTotalStat().areatotal(retentionDealData,date)
    //各区留样计划各状态数据统计
    val arearetentionstatustotal = new RetentionTotalStat().areastatustotal(retentionDealData,date)
    //按所属教育部留样计划各状态数据统计
    val masteridretentiontotal = new RetentionTotalStat().masteridtotal(retentionDealData,date,schoolData,commiteeid2commiteename)
    //按上海市办学性质来留样计划各状态数据统计（nature）
    val natureretentionstatus = new RetentionTotalStat().naturestatus(retentionDealData,date,schoolData)
    //各区办学性质来留样计划各状态数据统计（nature）
    val areanatureretentionstatus = new RetentionTotalStat().areanaturestatus(retentionDealData,date,schoolData)
    //按上海市各类型学校来留样计划各状态数据统计（level）
    val levelretentionstatus = new RetentionTotalStat().levelstatus(retentionDealData,date,schoolData)
    //按各区各类型学校来留样计划各状态数据统计（level）
    val arealevelretentionstatus = new RetentionTotalStat().arealevelstatus(retentionDealData,date,schoolData)
    //按管理部门留样计划各状态数据统计
    val departmentretentionstatus = new RetentionTotalStat().departmentstatus(retentionDealData,date,schoolData)

    //各区留样计划各状态按照学校数据统计(对学校进行去重处理)
    val arearetentionschooltotal = new RetentionSchoolTotalStat().areatotal(retentionchildData,date)
    //按所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
    val masteridretentionschooltotal = new RetentionSchoolTotalStat().masteridtotal(retentionchildData,date,schoolData,commiteeid2commiteename)
    //按上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val natureretentionschooltotal = new RetentionSchoolTotalStat().naturetotal(retentionchildData,date,schoolData)
    //按各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val areanatureretentionschooltotal = new RetentionSchoolTotalStat().areanaturetotal(retentionchildData,date,schoolData)
    //按上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val levelretentionschooltotal = new RetentionSchoolTotalStat().leveltotal(retentionchildData,date,schoolData)
    //按各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val arealevelretentionschooltotal = new RetentionSchoolTotalStat().arealeveltotal(retentionchildData,date,schoolData)
    //按管理部门留样计划各状态数据统计 (对学校进行去重处理）
    val shdepartmentretentionschooltotal = new RetentionSchoolTotalStat().shanghaidepartmenttotal(retentionchildData,date,schoolData)

    //各区留样操作规则数据统计
    val arearetentionruletotal = new RetentionRuleTotalStat().areatotal(retentionDealData,date)
    //按所属教育部留样操作规则数据统计
    val masteridretentionruletotal = new RetentionRuleTotalStat().masteridtotal(retentionDealData,date,schoolData,commiteeid2commiteename)
    //按上海市办学性质来留样操作规则各状态数据统计（nature）
    val natureretentionrulestatus = new RetentionRuleTotalStat().naturestatus(retentionDealData,date,schoolData)
    //按上海市各类型学校来留样操作规则数据统计（level）
    val levelretentionrulestatus = new RetentionRuleTotalStat().levelstatus(retentionDealData,date,schoolData)
    //按管理部门留样计划操作规则统计
    val departmentretentionrulestatus = new RetentionRuleTotalStat().departmentstatus(retentionDealData,date,schoolData)

    //各区留样规则各状态按照学校数据统计(对学校进行去重处理)
    val arearetentionschoolruletotal = new RetentionRuleSchoolTotalStat().areatotal(retentionchildData,date)
    //按所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)
    val masteridretentionschoolruletotal = new RetentionRuleSchoolTotalStat().masteridtotal(retentionchildData,date,schoolData,commiteeid2commiteename)
    //按上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）
    val natureretentionschoolruletotal = new RetentionRuleSchoolTotalStat().naturetotal(retentionchildData,date,schoolData)
    //按上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）
    val levelretentionschoolruletotal = new RetentionRuleSchoolTotalStat().leveltotal(retentionchildData,date,schoolData)
    //按管理部门留样操作规则统计数据统计 (对学校进行去重处理）
    val shdepartmentretentionschoolruletotal = new RetentionRuleSchoolTotalStat().shanghaidepartmenttotal(retentionchildData,date,schoolData)

    //按照管理权限各区留样计划统计数据
    val dparearetentiontotal = new RetentionTotalStat().departmentareatotal(retentionDealData,date,schoolData)
    //按照权限各区留样计划各状态数据统计
    val dparearetentionstatustotal = new RetentionTotalStat().departmentareastatustotal(retentionDealData,date,schoolData)
    //按照权限的所属留样计划各状态数据统计
    val dpmasteridretentiontotal = new RetentionTotalStat().departmentmasteridtotal(retentionDealData,date,schoolData,commiteeid2commiteename)
    //按权限上海市办学性质来留样计划各状态数据统计（nature）
    val dpnatureretentionstatus = new RetentionTotalStat().departmentnaturestatus(retentionDealData,date,schoolData)
    //权限各区办学性质来留样计划各状态数据统计（nature）
    val dpareanatureretentionstatus = new RetentionTotalStat().departmentareanaturestatus(retentionDealData,date,schoolData)
    //按权限上海市各类型学校来留样计划各状态数据统计（level）
    val dplevelretentionstatus = new RetentionTotalStat().departmentlevelstatus(retentionDealData,date,schoolData)
    //按权限各区各类型学校来留样计划各状态数据统计（level）
    val dparealevelretentionstatus = new RetentionTotalStat().departmentarealevelstatus(retentionDealData,date,schoolData)
    //按权限管理部门留样计划各状态数据统计
    val dpdepartmentretentionstatus = new RetentionTotalStat().departmentdepartmentstatus(retentionDealData,date,schoolData)

    //按照权限各区留样计划各状态按照学校数据统计(对学校进行去重处理)
    val dparearetentionschooltotal = new RetentionSchoolTotalStat().departmentareatotal(retentionchildData,date,schoolData)
    //按权限所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
    val dpmasteridretentionschooltotal = new RetentionSchoolTotalStat().departmentmasteridtotal(retentionchildData,date,schoolData,commiteeid2commiteename)
    //按权限上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val dpnatureretentionschooltotal = new RetentionSchoolTotalStat().departmentnaturetotal(retentionchildData,date,schoolData)
    //按权限各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
    val dpareanatureretentionschooltotal = new RetentionSchoolTotalStat().departmentareanaturetotal(retentionchildData,date,schoolData)
    //按权限上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val dplevelretentionschooltotal = new RetentionSchoolTotalStat().departmentleveltotal(retentionchildData,date,schoolData)
    //按权限各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
    val dparealevelretentionschooltotal = new RetentionSchoolTotalStat().departmentarealeveltotal(retentionchildData,date,schoolData)
    //按权限管理部门留样计划各状态数据统计 (对学校进行去重处理）
    val dpdepartmentretentionschooltotal = new RetentionSchoolTotalStat().departmentdepartmenttotal(retentionchildData,date,schoolData)

    //按照权限的各区留样操作规则数据统计
    val dparearetentionruletotal = new RetentionRuleTotalStat().departmentareatotal(retentionDealData,date,schoolData)
    //按权限所属教育部留样操作规则数据统计
    val dpmasteridretentionruletotal = new RetentionRuleTotalStat().departmentmasteridtotal(retentionDealData,date,schoolData,commiteeid2commiteename)
    //按权限上海市办学性质来留样操作规则各状态数据统计（nature）
    val dpnatureretentionrulestatus = new RetentionRuleTotalStat().departmentnaturestatus(retentionDealData,date,schoolData)
    //按权限上海市各类型学校来留样操作规则数据统计（level）
    val dplevelretentionrulestatus = new RetentionRuleTotalStat().departmentlevelstatus(retentionDealData,date,schoolData)
    //按权限管理部门留样计划操作规则统计
    val dpdepartmentretentionrulestatus = new RetentionRuleTotalStat().departmentdepartmentstatus(retentionDealData,date,schoolData)

    //按照权限各区留样规则各状态按照学校数据统计(对学校进行去重处理)
    val dparearetentionschoolruletotal = new RetentionRuleSchoolTotalStat().departmentareatotal(retentionchildData,date,schoolData)
    //按权限所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)
    val dpmasteridretentionschoolruletotal = new RetentionRuleSchoolTotalStat().departmentmasteridtotal(retentionchildData,date,schoolData,commiteeid2commiteename)
    //按权限上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）
    val dpnatureretentionschoolruletotal = new RetentionRuleSchoolTotalStat().departmentnaturetotal(retentionchildData,date,schoolData)
    //按权限上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）
    val dplevelretentionschoolruletotal = new RetentionRuleSchoolTotalStat().departmentleveltotal(retentionchildData,date,schoolData)
    //按权限管理部门留样操作规则统计数据统计 (对学校进行去重处理）
    val dpdepartmentretentionschoolruletotal = new RetentionRuleSchoolTotalStat().departmentdepartmenttotal(retentionchildData,date,schoolData)


    areausematerialtotal
      .union(areausematerialstatustotal)
      .union(masteridusematerialtotal)
      .union(natureusematerialstatus)
      .union(areanatureusematerialstatus)
      .union(levelusematerialstatus)
      .union(arealevelusematerialstatus)
      .union(departmentusematerialstatus)
      .union(areausematerialschooltotal)
      .union(masteridusematerialschooltotal)
      .union(natureusematerialschooltotal)
      .union(areanatureusematerialschooltotal)
      .union(levelusematerialschooltotal)
      .union(arealevelusematerialschooltotal)
      .union(shanghaidepartmentusematerialschooltotal).cogroup(useMaterialTotalData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(date + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(date + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })


    areadistributiontotal
      .union(areadistributionstatustotal)
      .union(masteriddistributiontotal)
      .union(naturedistributionstatus)
      .union(areanaturedistributionstatus)
      .union(leveldistributionstatus)
      .union(arealeveldistributionstatus)
      .union(departmentdistributionstatus)
      .union(areaschooldistributiontotal)
      .union(masteridschooldistributiontotal)
      .union(natureschooldistributiontotal)
      .union(areanatureschooldistributiontotal)
      .union(canteenschooldistributiontotal)
      .union(levelschooldistributiontotal)
      .union(arealevelschooldistributiontotal)
      .union(shdepartmentdistributiontotal)
      .union(areadistributionruletotal)
      .union(naturedistributionrulestatus)
      .union(leveldistributionrulestatus)
      .union(masteriddistributionruletotal)
      .union(departmentdistributionrulestatus)
      .union(areadistributionschoolruletotal)
      .union(naturedistributionschoolruletotal)
      .union(leveldistributionschoolruletotal)
      .union(masteriddistributionschoolruletotal)
      .union(shdepartmentdistributionschoolruletotal).cogroup(distributionTotalData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(date + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(date + "_DistributionTotal", k, v._1.head.toString)
            }

        })
    })

    arearetentiontotal
      .union(arearetentionstatustotal)
      .union(masteridretentiontotal)
      .union(natureretentionstatus)
      .union(areanatureretentionstatus)
      .union(levelretentionstatus)
      .union(arealevelretentionstatus)
      .union(departmentretentionstatus)
      .union(arearetentionschooltotal)
      .union(masteridretentionschooltotal)
      .union(natureretentionschooltotal)
      .union(areanatureretentionschooltotal)
      .union(levelretentionschooltotal)
      .union(arealevelretentionschooltotal)
      .union(shdepartmentretentionschooltotal)
      .union(arearetentionruletotal)
      .union(masteridretentionruletotal)
      .union(natureretentionrulestatus)
      .union(levelretentionrulestatus)
      .union(departmentretentionrulestatus)
      .union(arearetentionschoolruletotal)
      .union(masteridretentionschoolruletotal)
      .union(natureretentionschoolruletotal)
      .union(levelretentionschoolruletotal)
      .union(shdepartmentretentionschoolruletotal).cogroup(retentionTotalData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(date + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(date + "_gc-retentiondishtotal", k, v._1.head.toString)
            }

        })
    })

    val departmentid = Tools.departmentid(session)
    for (i <- 0 until departmentid.size) {
      val id = departmentid(i)
      val jedis = JPools.getJedis
      val departmentUseMaterialPlanTotal = sc.parallelize(jedis.hgetAll(date + "_useMaterialPlanTotal" + "_" + "department" + "_" + id).asScala.toList)
      val departmentDistributionPlanTotal = sc.parallelize(jedis.hgetAll(date + "_DistributionTotal" + "_" + "department" + "_" + id).asScala.toList)
      val departmentRetentionPlanTotal = sc.parallelize(jedis.hgetAll(date + "_gc-retentiondishtotal" + "_" + "department" + "_" + id).asScala.toList)

      dpareatotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3))
        .union(dpareastatustotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteridtotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnaturestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareanatureusematerialstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dplevelstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparealevelstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareausematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteridusematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnatureusematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareanatureusematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dplevelusematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparealevelusematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpshanghaidepartmentusematerialschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .cogroup(departmentUseMaterialPlanTotal).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date + "_useMaterialPlanTotal"+ "_" + "department" + "_" + id, k, "0")
              } else {
                jedis.hset(date + "_useMaterialPlanTotal"+ "_" + "department" + "_" + id, k, v._1.head.toString)
              }

          })
      })


      dpareadistributiontotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3))
        .union(dpareadistributionstatustotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteriddistributiontotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnaturedistributionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpleveldistributionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentdistributionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareadistributionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteriddistributionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnaturedistributionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpcanteendistributionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpleveldistributionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentdistributionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareadistributionruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnaturedistributionrulestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpleveldistributionrulestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteriddistributionruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentdistributionrulestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareadistributionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnaturedistributionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpleveldistributionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteriddistributionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentdistributionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .cogroup(departmentDistributionPlanTotal).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date + "_DistributionTotal"+ "_" + "department" + "_" + id, k, "0")
              } else {
                jedis.hset(date + "_DistributionTotal"+ "_" + "department" + "_" + id, k, v._1.head.toString)
              }

          })
      })


      dparearetentiontotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3))
        .union(dparearetentionstatustotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteridretentiontotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnatureretentionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareanatureretentionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dplevelretentionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparealevelretentionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentretentionstatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparearetentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteridretentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnatureretentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpareanatureretentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dplevelretentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparealevelretentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentretentionschooltotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparearetentionruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteridretentionruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnatureretentionrulestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dplevelretentionrulestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentretentionrulestatus.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dparearetentionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpmasteridretentionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpnatureretentionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dplevelretentionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .union(dpdepartmentretentionschoolruletotal.filter(x => x._1.equals(id)).map(x => (x._2,x._3)))
        .cogroup(departmentRetentionPlanTotal).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date + "_gc-retentiondishtotal"+ "_" + "department" + "_" + id, k, "0")
              } else {
                jedis.hset(date + "_gc-retentiondishtotal"+ "_" + "department" + "_" + id, k, v._1.head.toString)
              }

          })
      })

    }

    val platoon_feed = jedis.hgetAll(date + "_platoon-feed")
    val plaData = sc.parallelize(platoon_feed.asScala.toList) //redis中存

    // 排菜，用料，配送操作情况

    val edu_all = jedis.hgetAll(date + "_allUseData")
    val eduAllData = sc.parallelize(edu_all.asScala.toList) //redis中存的所有维度的数据

    new EduAllDataStat().platoonmaterialdetailresert(plaData,useMaterialChildData,distributionchildData,retentionchildData,schoolData,eduAllData,date,commiteeid2commiteeid,schoolid2suppliername)

    sc.stop()
  }

}
