package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.MonthTargetTotal.format
import com.ssic.report.Platoon.{format, format1}
import com.ssic.service.HiveToDwSaasStat
import com.ssic.utils.Tools._
import com.ssic.utils.{HiveTools, Tools}
import org.apache.commons.lang3.time._
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.{SQLContext, SparkSession}
import org.apache.spark.sql.hive.HiveContext
import org.slf4j.LoggerFactory

/**
  *  将hive的saas_v1的原始数据进行处理导入到saas_v1_dw库并进行分区,操作的是阳光午餐的数据
  */
object HiveToDwSaas {


  private val format = FastDateFormat.getInstance("M")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("yyyy-MM-dd")
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")


    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)

    val sparkSession = sqlContext.sparkSession

    val month = format.format(new Date())
    val year = format1.format(new Date())


    //将t_edu_school表学校基础信息表的数据进行处理放入saas_v1_dw库中
    //new HiveToDwSaasStat().insertdwschool(hiveContext)

    //将t_pro_images表，对配送图片和建议案图片进行处理
    //new HiveToDwSaasStat().insertdwimages(hiveContext,year,month)

    //将处理好的供餐数据，存储于saas_v1_dw库中
    for (i <- 0 to 32) {
      //查询一周的排菜数据
      val calendar = Calendar.getInstance()
      calendar.setTime(format2.parse("2018-05-01"))
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format2.format(time)
      val year1 = format1.format(time)

      var term_year = "null"
      if (format2.parse(date).getTime <= format2.parse(year + "-08-31").getTime) {
        term_year = (year1.toInt - 1).toString
      } else {
        term_year = year1
      }

      new HiveToDwSaasStat().insertdwcalendar(hiveContext,date)




    }

    sc.stop()
  }

}
