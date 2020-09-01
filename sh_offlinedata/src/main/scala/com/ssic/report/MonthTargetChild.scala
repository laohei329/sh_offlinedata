package com.ssic.report

import java.sql.Timestamp
import java.util
import java.util.{Calendar, Date}

import com.ssic.report.MonthTargetDetail.{format, format1, format2, format3}
import com.ssic.service.{DealDataStat, TargetChildStat}
import com.ssic.utils.{JPools, Rule, Tools}
import com.ssic.utils.Tools._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._


object MonthTargetChild {

  /**
    * 一个月留样对学校去重子页面数据
    */

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")
  private val format3 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("一个月留样对学校去重子页面数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession
    val hiveContext: HiveContext = new HiveContext(sc)

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val pro_suppliers = session.read.jdbc(url, pro_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    pro_suppliers.createTempView("t_pro_supplier")

    for (i <- -30 to -1) {
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)
      val year = format1.format(time)
      val month = format2.format(time)

      val jedis = JPools.getJedis
      val retentionChild: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondishtotal_child")
      val retentionChildData = sc.parallelize(retentionChild.asScala.toList) //已存在的留样计划子页面数据

      hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_detail where year='${year}' and month ='${month}' and use_date ='${date}' and have_class =1  ")
        .rdd.map({
        row =>
          val area = Rule.emptyToNull(row.getAs[String]("area"))
          val school_id = Rule.emptyToNull(row.getAs[String]("school_id"))
          val reserve_total = row.getAs[Integer]("reserve_total")
          val have_reserve_total = row.getAs[Integer]("have_reserve_total")
          val no_reserve_total = row.getAs[Integer]("no_reserve_total")
          val have_reserve = row.getAs[Integer]("have_reserve")
          var reserve_create_time = "null"
          if (have_reserve == 1) {
            reserve_create_time = format3.format(row.getAs[Timestamp]("reserve_create_time"))
          } else {
            reserve_create_time
          }

          val reserve_deal_status = Rule.emptyToNull(row.getAs[String]("reserve_deal_status"))

          val key = "area" + "_" + area + "_" + "id" + "_" + school_id
          val value = "total" + "_" + reserve_total + "_" + "reserve" + "_" + have_reserve_total + "_" + "noreserve" + "_" + no_reserve_total + "_" + "status" + "_" + have_reserve + "_" + "reservestatus" + "_" + reserve_deal_status + "_" + "createtime" + "_" + reserve_create_time
          (key, value)

      }).cogroup(retentionChildData).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hdel(date + "_gc-retentiondishtotal_child", k)
              } else {
                jedis.hset(date + "_gc-retentiondishtotal_child", k, v._1.head)
              }
          })
      })

      //      val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
      //      val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
      //      val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
      //      val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
      //      val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号
      //
      //      val jedis = JPools.getJedis
      //      val platoon: util.Map[String, String] = jedis.hgetAll(date + "_platoon-feed")
      //      val platoonData = sc.parallelize(platoon.asScala.toList) //学校的供餐数据
      //
      ////      val useMaterialPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlan-Detail")
      ////      val usematerialData = sc.parallelize(useMaterialPlanDetail.asScala.toList) //已存在的用料计划数据
      ////
      ////      val useMaterialChild: util.Map[String, String] = jedis.hgetAll(date + "_useMaterialPlanTotal_child")
      ////      val useMaterialChildData = sc.parallelize(useMaterialChild.asScala.toList) //已存在的用料计划子页面数据
      ////
      ////      val distributionPlanDetail: util.Map[String, String] = jedis.hgetAll(date + "_Distribution-Detail")
      ////      val distributionData = sc.parallelize(distributionPlanDetail.asScala.toList) //已存在的配送计划数据
      ////
      ////      val distributionChild: util.Map[String, String] = jedis.hgetAll(date + "_DistributionTotal_child")
      ////      val distributionChildData = sc.parallelize(distributionChild.asScala.toList)    //已存在的配送计划子页面数据
      //
      //      val retentionDetail = jedis.hgetAll(date + "_gc-retentiondish")
      //      val retentionData = sc.parallelize(retentionDetail.asScala.toList) //已存在的留样计划数据
      //

      //
      //      //用料计划的处理后的数据
      ////      val usematerialDealData = new DealDataStat().usematerialdealdata(usematerialData, projid2schoolid, projid2schoolname, gongcanSchool,projid2Area)
      ////
      ////      //用料计划的子页面,没有产生用料计划的的学校也要放入到子页面中
      ////      val useValue = usematerialDealData.map(x => (x._1 + "_" + x._6, List(x._4, x._3)))
      ////
      ////      new TargetChildStat().usematerialchild(platoonData, useValue, date, useMaterialChildData)
      ////
      ////      //配送计划处理后的数据
      ////      val distributionDealData = new DealDataStat().distributiondealdata(distributionData, gongcanSchool,school2Area,date)
      ////      //配送计划的子页面，没有产生配送计划的学校也要放入到子页面中
      ////      val disValue = distributionDealData.map(x => (x._1 + "_" + x._4, x._6+"_"+x._7))
      ////      new TargetChildStat().distributionchild(platoonData, disValue, date,distributionChildData)
      //
      //      //留样计划处理后的数据
      //      val reValue = new DealDataStat().retentiondealdata(retentionData)
      //      //留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
      //      new TargetChildStat().retentionchild(platoonData, reValue, date, retentionChildData)

    }

    sc.stop()
  }

}
