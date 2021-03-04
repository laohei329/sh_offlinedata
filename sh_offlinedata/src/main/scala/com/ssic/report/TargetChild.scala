package com.ssic.report

import java.util
import java.util.Date

import com.ssic.report.TargetTotal.format
import com.ssic.service.{DealDataStat, TargetChildStat}
import com.ssic.utils.Tools._
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object TargetChild {

  /**
    * 用料，配送，留样的子页面
    */
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("今日用料，验收，留样对学校去重子页面数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val date = format.format(new Date())

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val pro_suppliers = session.read.jdbc(url, pro_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    pro_suppliers.createTempView("t_pro_supplier")

    val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
    val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
    val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
    val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号
    val schoolid2Projid = sc.broadcast(Tools.schoolid2Projid(session)) //学校id获取项目点id
    val schoolid2suppliername = sc.broadcast(Tools.schoolid2suppliername(session)) //学校id获取团餐公司名字

    val jedis = JPools.getJedis
    val platoon: util.Map[String, String] = jedis.hgetAll(date + "_platoon-feed")
    val platoonData = sc.parallelize(platoon.asScala.toList) //学校的供餐数据

    val useMaterialPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlanDetail")
   // val useMaterialPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlan-Detail")
    val usematerialData = sc.parallelize(useMaterialPlanDetail.asScala.toList) //已存在的用料计划数据

    val useMaterialChild: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlanTotal_child")
    val useMaterialChildData = sc.parallelize(useMaterialChild.asScala.toList) //已存在的用料计划子页面数据


    /**
     * Feild:
     * (id_主键id_type_配送类型（1 原料，2 成品菜）_schoolid_学校id_area_区号_sourceid_团餐公司id_batchno_发货批次号_delivery_统配/直配)
     * Value:
     * 配送状态_deliveryDate_验收上报时间_disstatus_配送规则_purchaseDate_进货时间_deliveryReDate_验收时间（手动）
     *   019-10-28 08:42:35_deliveryReDate_2019-10-28 08:42:35
     * (配送状态：-2 信息不完整  -1 未指派  0 已指派（未配送）  1 配送中  2待验收（已配送）3 已验收  4 已取消）
     * 验收操作状态：1 表示规范录入  2  表示补录  3 表示逾期补录  4 表示无数据
     */
    val distributionPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_Distribution-Detail")
    val distributionData = sc.parallelize(distributionPlanDetail.asScala.toList) //已存在的配送计划数据
    /**
     * Feild:
     * (area_区号_id_学校id)
     * Value:
     * (total_配送计划总数量_accept_已验收数量_assign_已指派数量_shipp_已配送数量_status_配送计划状态_disstatus_验收操作状态_deliveryDate_验收上报时间)
     * 未验收数量 = 总数 - 已验收
     * 未配送数量 = 总数  - 已配送
     * 未指派数量 = 总数  - 已指派
     * 指派 -》 配送 -》 验收   例如状态为未配送，那么指派状态为 是，配送状态为否，验收状态为否
     */
    val distributionChild: util.Map[String, String] = jedis.hgetAll(date + "_DistributionTotal_child")
    val distributionChildData = sc.parallelize(distributionChild.asScala.toList) //已存在的配送计划子页面数据

    val retentionDetail = jedis.hgetAll(date + "_gc-retentiondish")
    val retentionData = sc.parallelize(retentionDetail.asScala.toList) //已存在的留样计划数据

    val retentionChild: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondishtotal_child")
    val retentionChildData = sc.parallelize(retentionChild.asScala.toList) //已存在的留样计划子页面数据

    val b2bPlatoon =  sc.parallelize(jedis.hgetAll(date + "_b2b-platoon").asScala.toList) //redis中b2b的排菜数据

    //用料计划的处理后的数据
    val b2bPlatoonSchool: RDD[(String, Int)] = b2bPlatoon.map(x => (x._1.split("_")(1),2)).distinct() //b2b排菜的学校就算做用料确认
    //(区号，供餐，状态，key，学校名字，学校id,valuedata)
    //key "area" + "_" + 区 + "_" + "type" + 类型 + "name" + "_" + schoolname + "_" + "projid" + 项目点id
    val usematerialDealData = new DealDataStat()
      .usematerialdealdata(usematerialData, projid2schoolid, projid2schoolname, gongcanSchool, projid2Area,
        b2bPlatoonSchool,schoolid2Projid,schoolid2suppliername)

    //用料计划的子页面,没有产生用料计划的的学校也要放入到子页面中
    val useValue = usematerialDealData.map(x => (x._1 + "_" + x._6, List(x._4, x._3)))

    /**
     * platoonData (9_cfee5c7f-7c7a-4443-b97c-d138754d0f1c,供餐_已排菜_create-time_2019-04-11 17:16:25_reason_null_plastatus_1)
     */
    new TargetChildStat().usematerialchild(platoonData, useValue, date, useMaterialChildData)

    //配送计划处理后的数据
    /**
     * distributionData  （区号，供餐，处理后的value，学校id，key，状态，规范状态）
     */
    val distributionDealData = new DealDataStat().distributiondealdata(distributionData, gongcanSchool, school2Area, date)
    //配送计划的子页面，没有产生配送计划的学校也要放入到子页面中
    val disValue = distributionDealData.map(x => (x._1 + "_" + x._4, x._6 + "_" + x._7 +
      "_" + x._3.split("_")(2)))
    new TargetChildStat().distributionchild(platoonData, disValue, date, distributionChildData)

    //留样计划处理后的数据
    val reValue = new DealDataStat().retentiondealdata(retentionData)
    //留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
    new TargetChildStat().retentionchild(platoonData, reValue, date, retentionChildData)

    sc.stop()
  }

}
