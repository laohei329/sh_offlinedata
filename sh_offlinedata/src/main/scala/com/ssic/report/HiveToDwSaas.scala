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

    //将t_pro_images表，对配送图片和建议案图片进行处理
    new HiveToDwSaasStat().insertdwimages(hiveContext,year,month)

    val calendar1 = Calendar.getInstance()
    calendar1.setTime(yesterday)
    calendar1.add(Calendar.MONTH,-3)
    val day = calendar.getTime
    val month1 = format3.format(day)
    val year1 = format1.format(day)
    val use_date =year1+"-"+month1+"-01"

    //处理用料详情表，dw_t_edu_material_detail
    new HiveToDwSaasStat().insertdwmaterialdetail(hiveContext,use_date)

    //处理菜品，原料详情表 ，dw_t_edu_material_dish
    val supply_date =use_date+" 00:00:00"
    new HiveToDwSaasStat().insertmaterialdish(hiveContext,supply_date,year1,year)

    //是否供餐表（dw_t_edu_calendar）

    val calendar3 = Calendar.getInstance()
    calendar3.setTime(yesterday)
    calendar3.add(Calendar.MONTH,-3)
    val month3 = format.format(calendar3.getTime)
    val year3 = format1.format(calendar3.getTime)

    val calendar4 = Calendar.getInstance()
    calendar4.setTime(yesterday)
    calendar4.add(Calendar.MONTH,-2)
    val month4 = format.format(calendar4.getTime)

    val calendar5 = Calendar.getInstance()
    calendar5.setTime(yesterday)
    calendar5.add(Calendar.MONTH,-1)
    val month5 = format.format(calendar5.getTime)

    val calendar6 = Calendar.getInstance()
    calendar6.setTime(yesterday)
    calendar6.add(Calendar.MONTH,0)
    val month6 = format.format(calendar6.getTime)
    val year6 = format1.format(calendar6.getTime)

    if("9".equals(month6)){
      val term_year = year6
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month6,month6,month6)
    }else if("10".equals(month6)){
      val term_year = year6
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month5,month6,month6)
    }else if("11".equals(month6)){
      val term_year = year6
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month5,month4,month6)
    }else if("12".equals(month6)){
      val term_year = year6
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month5,month4,month3)
    }else if("1".equals(month6)) {
      val term_year = year3
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year3,month5,month5,month4,month3)
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month6,month6,month6)
    }else if("2".equals(month6)) {
      val term_year = year3
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year3,month4,month4,month4,month3)
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month6,month6,month5)
    }else if("3".equals(month6)) {
      val term_year = year3
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year3,month3,month3,month3,month3)
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month5,month4,month4)
    }else{
      val term_year = year3
      new HiveToDwSaasStat().dweducalendar(hiveContext,term_year,year6,month6,month5,month4,month3)
    }



    sc.stop()
  }

}
