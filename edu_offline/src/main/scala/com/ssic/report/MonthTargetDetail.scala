package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.MonthPlatoon.format
import com.ssic.service.{DealDataStat, TargetDetailStat}
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools.{conn, edu_school, edu_school_supplier, url}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

import scala.collection.JavaConverters._

object MonthTargetDetail {

  /**
    * 一个月的用料计划，配送计划，留样计划的详情数据
    **/

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val session = sqlContext.sparkSession

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")

    for (i <- -30 to -1) {
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)
      val datetime = date + " 00:00:00"
      val year = format1.format(time)
      val month = format2.format(time)



      val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
      val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
      val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
      val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
      val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号

      val jedis = JPools.getJedis

      val useMaterialPlanDetail = jedis.hgetAll(date + "_useMaterialPlanDetail")
      val useMaterialPlanDetailData = sc.parallelize(useMaterialPlanDetail.asScala.toList) //用料计划临时表数据

      val useMaterials = jedis.hgetAll(date + "_useMaterialPlan-Detail")
      val useMaterialData = sc.parallelize(useMaterials.asScala.toList) //用料计划已经存在的数据

      val distributionDetail = jedis.hgetAll(date + "_DistributionDetail")
      val distributionDetailData = sc.parallelize(distributionDetail.asScala.toList) //配送计划临时表数据

      val distributions = jedis.hgetAll(date + "_Distribution-Detail")
      val distributionData = sc.parallelize(distributions.asScala.toList) //配送计划已经存在的数据

      val dishmenu: RDD[(String, String, String, String, String, String, String, String)] = Tools.hivedishmenu(hiveContext, datetime, year, month) //hive数据库的菜品数据

      val retentiondish = jedis.hgetAll(date + "_retention-dish")
      val retentiondishData: RDD[(String, String)] = sc.parallelize(retentiondish.asScala.toList) //留样计划临时表数据

      val gcretentiondish = jedis.hgetAll(date + "_gc-retentiondish")
      val gcretentiondishData: RDD[(String, String)] = sc.parallelize(gcretentiondish.asScala.toList) //留样计划已经存在的数据

      //用料计划的详情数据
      //处理好的用料计划数据
      val usematerialDealData = new DealDataStat().usematerialdealdata(useMaterialPlanDetailData, projid2schoolid, projid2schoolname, gongcanSchool,projid2Area)
      new TargetDetailStat().usematerial(usematerialDealData, useMaterialData, date)

      //配送计划的详情数据
      // 处理好的配送计划数据
      val distributiondealdata = new DealDataStat().distributiondealdata(distributionDetailData, gongcanSchool,school2Area)
      new TargetDetailStat().distribution(distributiondealdata, distributionData, date)

      //菜品留样的详情数据
      new TargetDetailStat().retentiondish(dishmenu, retentiondishData, date, gongcanSchool, gcretentiondishData)

    }

    sc.stop()
  }
}
