package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.Platoon.{format, format1}
import com.ssic.service.LSDataStat
import com.ssic.utils.JPools
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._

/**
  * 大屏的原料，菜品，供应商对应学校的统计的数据从hive到redis中
  */

object LSData {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("排名数据从hive移到redis中")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)

    val hiveContext: HiveContext = new HiveContext(sc)

    for (i <- 0 to 1){
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)+" 00:00:00"
      val year = format1.format(time)
      val month = format2.format(time)

      val jedis = JPools.getJedis

      val material = jedis.hgetAll(date.split(" ")(0) + "_material_new")
      val materialData: RDD[(String, String)] = sc.parallelize(material.asScala.toList)  //原料排名表数据

      val dish = jedis.hgetAll(date.split(" ")(0) + "_dish_new")
      val dishData: RDD[(String, String)] = sc.parallelize(dish.asScala.toList)  //菜品排名数据

      val supplierToSchool = jedis.hgetAll(date.split(" ")(0) + "_supplierToSchool_new")
      val supplierToSchoolData: RDD[(String, String)] = sc.parallelize(supplierToSchool.asScala.toList)  //供应商供应学校排名数据

      //将原料排名数据放入到redis中
      new LSDataStat().materialtotaltoredis(hiveContext,date,year,month,materialData)

      //将菜品排名数据放入到redis中
      new LSDataStat().dishtotaltoredis(hiveContext,date,year,month,dishData)

      //将供应商供应学校排名的统计数据放入到redis中
      new LSDataStat().supplynametotaltoredis(hiveContext,date,year,month,supplierToSchoolData)

    }
    sc.stop()
  }
}
