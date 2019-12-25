package com.ssic.report

import java.util
import java.util.Date

import com.ssic.report.TargetTotal.format
import com.ssic.service.{DealDataStat, TargetChildStat}
import com.ssic.utils.Tools.{conn, edu_school, edu_school_supplier, url}
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

import scala.collection.JavaConverters._

object TargetChild {

  /**
    * 用料，配送，留样的子页面
  */
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val date = format.format(new Date())

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")

    val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session))  //项目点id获取学校id
    val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date))    //供餐学校数据
    val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
    val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号

    val jedis = JPools.getJedis
    val platoon: util.Map[String, String] = jedis.hgetAll(date + "_platoon-feed")
    val platoonData = sc.parallelize(platoon.asScala.toList)    //学校的供餐数据

    val useMaterialPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlan-Detail")
    val usematerialData = sc.parallelize(useMaterialPlanDetail.asScala.toList)    //已存在的用料计划数据

    val useMaterialChild: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlanTotal_child")
    val useMaterialChildData = sc.parallelize(useMaterialChild.asScala.toList)    //已存在的用料计划子页面数据

    val distributionPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_Distribution-Detail")
    val distributionData = sc.parallelize(distributionPlanDetail.asScala.toList)    //已存在的配送计划数据

    val distributionChild: util.Map[String, String] = jedis.hgetAll(date + "_DistributionTotal_child")
    val distributionChildData = sc.parallelize(distributionChild.asScala.toList)    //已存在的配送计划子页面数据

    val retentionDetail = jedis.hgetAll(date + "_gc-retentiondish")
    val retentionData = sc.parallelize(retentionDetail.asScala.toList)    //已存在的留样计划数据

    val retentionChild: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondishtotal_child")
    val retentionChildData = sc.parallelize(retentionChild.asScala.toList)    //已存在的留样计划子页面数据

    //用料计划的处理后的数据
    val usematerialDealData = new DealDataStat().usematerialdealdata(usematerialData,projid2schoolid,projid2schoolname,gongcanSchool,projid2Area)

    //用料计划的子页面,没有产生用料计划的的学校也要放入到子页面中
    val useValue = usematerialDealData.map(x => (x._1+"_"+x._6,List(x._4,x._3)))

    new TargetChildStat().usematerialchild(platoonData,useValue,date,useMaterialChildData)

    //配送计划处理后的数据
    val distributionDealData = new DealDataStat().distributiondealdata(distributionData,gongcanSchool,school2Area,date)
    //配送计划的子页面，没有产生配送计划的学校也要放入到子页面中
    val disValue = distributionDealData.map(x => (x._1+"_"+x._4,x._6+"_"+x._7))
    new TargetChildStat().distributionchild(platoonData,disValue,date,distributionChildData)

    //留样计划处理后的数据
    val reValue = new DealDataStat().retentiondealdata(retentionData)
    //留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
    new TargetChildStat().retentionchild(platoonData,reValue,date,retentionChildData)

    sc.stop()
  }

}
