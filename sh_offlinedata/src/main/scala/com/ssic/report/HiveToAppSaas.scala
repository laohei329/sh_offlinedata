package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.HiveToDwSaasNext.{format, format1, format3}
import com.ssic.service.HiveToAppSaasStat
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

/**
  * 产生app端的数据，教委数据
  * */

object HiveToAppSaas {
  private val format = FastDateFormat.getInstance("M")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format3 = FastDateFormat.getInstance("MM")
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")


    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)

    val sparkSession = sqlContext.sparkSession

    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH,-1)
    val yesterday = calendar.getTime
    val month = format.format(yesterday)
    val year = format1.format(yesterday)

    val calendar1 = Calendar.getInstance()
    calendar1.setTime(yesterday)
    calendar1.add(Calendar.MONTH,-3)
    val day = calendar.getTime
    val month1 = format3.format(day)
    val year1 = format1.format(day)
    val use_date =year1+"-"+month1+"-01"

    //综合分析菜品,留样的详细表（app_t_edu_dish_menu）
    if(year.equals(year1)) {
      new HiveToAppSaasStat().appedudishmenu(hiveContext,use_date,year1)
    }else{
      new HiveToAppSaasStat().appedudishmenu(hiveContext,use_date,year1)
      new HiveToAppSaasStat().appedudishmenu(hiveContext,year+"-"+"01"+"-01",year)
    }

    //综合分析的原料供应明细表（app_t_edu_ledege_detail）


    sc.stop()
  }
}
