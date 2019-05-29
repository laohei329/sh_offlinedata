package com.ssic.report

import java.util
import java.util.{Calendar, Date}

import com.ssic.report.YesterRetentionTotal.format
import com.ssic.service.{DealDataStat, TargetChildStat}
import com.ssic.utils.JPools
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

import scala.collection.JavaConverters._

object YesterRetentionChild {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

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

    val jedis = JPools.getJedis
    val platoon: util.Map[String, String] = jedis.hgetAll(date + "_platoon-feed")
    val platoonData = sc.parallelize(platoon.asScala.toList)    //学校的供餐数据

    val retentionDetail = jedis.hgetAll(date + "_gc-retentiondish")
    val retentionData = sc.parallelize(retentionDetail.asScala.toList)    //已存在的留样计划数据

    val retentionChild: util.Map[String, String] = jedis.hgetAll(date + "_gc-retentiondishtotal_child")
    val retentionChildData = sc.parallelize(retentionChild.asScala.toList)    //已存在的留样计划子页面数据

    //留样计划处理后的数据
    val reValue = new DealDataStat().retentiondealdata(retentionData)
    //留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
    new TargetChildStat().retentionchild(platoonData,reValue,date,retentionChildData)

    sc.stop()
  }
}
