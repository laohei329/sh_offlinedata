package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.TargetDetail.format
import com.ssic.service.TargetDetailStat
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

import scala.collection.JavaConverters._

object YesterReteion {
  /**
    *昨天的留样数据，因为数仓的菜品数据没有当天产生的数据
    */
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

    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val time = calendar.getTime
    val date = format.format(time)
    val datetime = date+" 00:00:00"
    val year =format1.format(new Date())
    val month =format2.format(new Date())

    val jedis = JPools.getJedis

    val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date))    //供餐学校数据

    val retentiondish = jedis.hgetAll(date + "_retention-dish")
    val retentiondishData: RDD[(String, String)] = sc.parallelize(retentiondish.asScala.toList)  //留样计划临时表数据

    val gcretentiondish = jedis.hgetAll(date + "_gc-retentiondish")
    val gcretentiondishData: RDD[(String, String)] = sc.parallelize(gcretentiondish.asScala.toList)  //留样计划已经存在的数据

    val dishmenu: RDD[(String, String, String, String, String, String, String, String)] = Tools.hivedishmenu(hiveContext,datetime,year,month) //hive数据库的菜品数据

    //菜品留样的详情数据
    new TargetDetailStat().retentiondish(dishmenu,retentiondishData,date,gongcanSchool,gcretentiondishData)

    sc.stop()

  }


}
