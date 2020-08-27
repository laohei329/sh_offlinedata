package com.ssic.report

import java.io.{File, FileOutputStream}
import java.net.URI
import java.util.{Calendar, Date}

import com.ssic.service.ExceltitleStat
import com.ssic.utils.Rule
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

/**
  * 医院的周报表
  */


object HospitalWeekToExcel {
//  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
//  private val format1 = FastDateFormat.getInstance("M")
//  private val format2 = FastDateFormat.getInstance("yyyy")
//  private val format3 = FastDateFormat.getInstance("yyyyMMdd")
//  private val format4 = FastDateFormat.getInstance("yyyy.MM.dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("医院周报表数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")


    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)

//    val calendar = Calendar.getInstance()
//    calendar.setTime(new Date())
//    calendar.add(Calendar.DAY_OF_MONTH, -8)
//    val time = calendar.getTime
    val monday = Rule.timeToStamp("yyyy-MM-dd", -8) //format.format(time)
    val month = Rule.timeToStamp("M", -8) //format1.format(time)
    val year = Rule.timeToStamp("yyyy",-8) //format2.format(time)
    val monday1 = Rule.timeToStamp("yyyyMMdd",-8) //format3.format(time)
    val monday2 = Rule.timeToStamp("yyyy.MM.dd",-8) //format4.format(time)

//    val calendar1 = Calendar.getInstance()
//    calendar1.setTime(new Date())
//    calendar1.add(Calendar.DAY_OF_MONTH, -2)
//    val time1 = calendar1.getTime
    val sunday = Rule.timeToStamp("yyyy-MM-dd",-2) //format.format(time1)
    val sunday1 = Rule.timeToStamp("yyyyMMdd",-2) //format3.format(time1)
    val sunday2 = Rule.timeToStamp("yyyy.MM.dd",-2) //format4.format(time1)

//    val calendar2 = Calendar.getInstance()
//    calendar2.setTime(new Date())
//    calendar2.add(Calendar.DAY_OF_MONTH, 0)
//    val time2 = calendar2.getTime
    val nowday = Rule.timeToStamp("yyyy-MM-dd",0) //format.format(time2)
    val nowday1 = Rule.timeToStamp("yyyyMMdd",0) //format3.format(time2)

    //医院周基础数据
    val hospital_data = hiveContext.sql(s"select * from app_saas_v1.t_med_hospital_w where year='${year}' and month ='${month}' and start_use_date ='${monday}' and end_use_date='${sunday}'")
      .rdd.map({
      row =>
        val hospital_id = row.getAs[String]("hospital_id")
        val hospital_name = row.getAs[String]("hospital_name")
        val level_name = row.getAs[String]("level_name")
        val have_class_day = row.getAs[Int]("have_class_day")
        (hospital_id, (hospital_name, level_name, have_class_day))
    })


    //医院排菜，验收数据
    val platoon_data = hiveContext.sql(s"select use_date,hospital_id,have_platoon,haul_status,driver_app_day from saas_v1_dw.dw_t_hospital_platoon_detail where use_date >= '${monday}' and use_date <= '${sunday}' ").rdd.map({
      row =>
        val use_date = row.getAs[String]("use_date")
        val hospital_id = row.getAs[String]("hospital_id")
        val have_platoon = row.getAs[Int]("have_platoon")
        val haul_status = row.getAs[Int]("haul_status")
        val driver_app_day = row.getAs[Int]("driver_app_day")
        (use_date, hospital_id, have_platoon, haul_status, driver_app_day)
    })

    //医院菜品数
    val hospital_dish = hiveContext.sql(s"select * from app_saas_v1.app_t_hospital_dish_menu where  supply_date >='${monday}' and supply_date <='${sunday}' ").rdd.map({
      row =>
        val hospital_id = row.getAs[String]("hospital_id")
        val hospital_name = row.getAs[String]("hospital_name")
        val dish_exact = row.getAs[Int]("dish_exact")
        val have_reserve = row.getAs[Int]("have_reserve")
        (hospital_id, dish_exact, have_reserve)
    })

    //医院原料数
    val hospital_material = hiveContext.sql(s"select * from app_saas_v1.app_t_hospital_ledege_detail where use_date >='${monday}' and use_date <='${sunday}' ").rdd.map({
      row =>
        val hospital_id = row.getAs[String]("hospital_id")
        val hospital_name = row.getAs[String]("hospital_name")
        val material_exact = row.getAs[Int]("material_exact")
        val driver_app_use = row.getAs[Int]("driver_app_use")
        (hospital_id, material_exact, driver_app_use)
    })


    //已排菜天数
    val have_platoon_total = platoon_data.filter(x => x._3 == 1).map(x => (x._2, 1)).reduceByKey(_ + _)
    //配送天数
    val ledger_day = platoon_data.filter(x => x._4 != -5).map(x => (x._2, 1)).reduceByKey(_ + _)
    //已验收天数
    val have_ledger_total = platoon_data.filter(x => x._4 == 3).map(x => (x._2, 1)).reduceByKey(_ + _)
    //医院菜品总数
    val hospital_dish_total = hospital_dish.map(x => (x._1, 1)).reduceByKey(_ + _)
    //医院准确菜品总数
    val hospital_dish_exact_total = hospital_dish.filter(x => x._2 == 1).map(x => (x._1, 1)).reduceByKey(_ + _)
    //医院不准确菜品总数
    val hospital_dish_noexact_total = hospital_dish.filter(x => x._2 == 0).map(x => (x._1, 1)).reduceByKey(_ + _)
    //医院原料总数
    val hospital_material_total = hospital_material.map(x => (x._1, 1)).reduceByKey(_ + _)
    //医院原料准确总数
    val hospital_material_exact_total = hospital_material.filter(x => x._2 == 1).map(x => (x._1, 1)).reduceByKey(_ + _)
    //医院不准确原料总数
    val hospital_material_noexact_total = hospital_material.filter(x => x._2 == 0).map(x => (x._1, 1)).reduceByKey(_ + _)
    //医院使用司机app的天数
    val driver_app_use_day = platoon_data.filter(x => x._5 == 1).map(x => (x._2, 1)).reduceByKey(_ + _)
    //医院已留样菜品数
    val hospital_reserve = hospital_dish.filter(x => x._3 == 1).map(x => (x._1, 1)).reduceByKey(_ + _)

    val data = hospital_data.leftOuterJoin(have_platoon_total).leftOuterJoin(have_ledger_total).leftOuterJoin(hospital_dish_total).leftOuterJoin(hospital_dish_exact_total).leftOuterJoin(hospital_dish_noexact_total).leftOuterJoin(hospital_material_total).leftOuterJoin(hospital_material_exact_total).leftOuterJoin(hospital_material_noexact_total).leftOuterJoin(driver_app_use_day).leftOuterJoin(ledger_day)
      .map({
        x =>
          val hospital_id = x._1 //医院id
        val hospital_name = x._2._1._1._1._1._1._1._1._1._1._1._1 //医院名字
        val level_name = x._2._1._1._1._1._1._1._1._1._1._1._2 //是否公立
        val have_class_day = x._2._1._1._1._1._1._1._1._1._1._1._3 //应排菜天数
        val have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._2.getOrElse(0) //已排菜天数
        val have_ledger_total = x._2._1._1._1._1._1._1._1._1._2.getOrElse(0) //已验收天数
        val hospital_dish_total = x._2._1._1._1._1._1._1._1._2.getOrElse(0) //菜品总数
        val hospital_dish_exact_total = x._2._1._1._1._1._1._1._2.getOrElse(0) //菜品准确总数
        val hospital_dish_noexact_total = x._2._1._1._1._1._1._2.getOrElse(0) //菜品未准确总数
        val hospital_material_total = x._2._1._1._1._1._2.getOrElse(0) //原料总数
        val hospital_material_exact_total = x._2._1._1._1._2.getOrElse(0) //原料准确总数
        val hospital_material_noexact_total = x._2._1._1._2.getOrElse(0) //原料不准确总数
        val driver_app_use_day = x._2._1._2.getOrElse(0) //司机app使用天数
        val ledger_day = x._2._2.getOrElse(0) //配送天数
        val platoon_lv = (have_platoon_total.toDouble / have_class_day.toDouble) * 100
          var dish_lv = 0.00
          if (hospital_dish_total == 0) {
            dish_lv
          } else {
            dish_lv = (hospital_dish_exact_total.toDouble / hospital_dish_total.toDouble) * 100
          }
          val ledgeraccept_lv = (have_ledger_total.toDouble / have_class_day.toDouble) * 100
          var material_lv = 0.00
          if (hospital_material_total == 0) {
            material_lv
          } else {
            material_lv = (hospital_material_exact_total.toDouble / hospital_material_total.toDouble) * 100
          }
          val app_lv = (driver_app_use_day.toDouble / have_class_day.toDouble) * 100
          val ledger_lv = (ledger_day.toDouble / have_class_day.toDouble) * 100

          var grade = "优秀"
          if (ledgeraccept_lv >= 90) {
            grade
          } else if (ledgeraccept_lv >= 80 && ledgeraccept_lv < 90) {
            grade = "优良"
          } else if (ledgeraccept_lv >= 60 && ledgeraccept_lv < 80) {
            grade = "良"
          } else if (ledgeraccept_lv > 0 && ledgeraccept_lv < 70) {
            grade = "较差"
          } else {
            grade = "未使用"
          }

          (hospital_id, hospital_name, level_name, have_class_day, have_platoon_total, have_ledger_total, hospital_dish_total, hospital_dish_exact_total, hospital_dish_noexact_total, hospital_material_total, hospital_material_exact_total, hospital_material_noexact_total, platoon_lv, ledgeraccept_lv, dish_lv, material_lv, app_lv, driver_app_use_day, ledger_day, ledger_lv, grade)
      })

    val filename = year + "医院食安平台数据汇总_上海市_" + monday1 + "__" + sunday1 + "__" + nowday1 + "000000" + ".xls"
    val file = new File("/data/" + filename)
    val workbook = new HSSFWorkbook()

    val sheet1 = workbook.createSheet("运行情况")
    val sheetWidth = Array[Int](2500, 2500, 8000, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 6000)
    new ExceltitleStat().sheetname(sheet1, sheetWidth)

    val style = new ExceltitleStat().style(workbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)

    val excelTitleValue = Array[String]("性质", "序号", "医院名称", "应排菜天数", "已排菜天数", "排菜操作率", "菜品总数", "准确菜品数", "不准确菜品数", "排菜准确率", "配送天数", "配送率", "验收天数", "验收操作率", "物料总数", "准确物料总数", "不准确物料总数", "验收准确率", "司机app使用天数", "司机app使用率", "运行评价（以验收操作率为准，" + monday2 + "-" + sunday2 + "）")


    val row = sheet1.createRow(0)
    new ExceltitleStat().exceltitle(excelTitleValue, style, row)

    val gongli_data = data.filter(x => x._3.equals("公立")).sortBy(x => x._14, false)
      .map({
        x =>
          val arr = Array[String]("公立", "0", x._2.toString, x._4.toString, x._5.toString, x._13.formatted("%.2f") + "%", x._7.toString, x._8.toString, x._9.toString, x._15.formatted("%.2f") + "%", x._19.toString, x._20.formatted("%.2f") + "%", x._6.toString, x._14.formatted("%.2f") + "%", x._10.toString, x._11.toString, x._12.toString, x._16.formatted("%.2f") + "%", x._18.toString, x._17.formatted("%.2f") + "%", x._21.toString)
          ("1", arr)

      }).collect()


    new ExceltitleStat().excelcontent(gongli_data, sheet1, style, 1, 1)

    val mingyin_data = data.filter(x => x._3.equals("民营")).sortBy(x => x._14, false)
      .map({
        x =>
          val arr = Array[String]("民营", "0", x._2.toString, x._4.toString, x._5.toString, x._13.formatted("%.2f") + "%", x._7.toString, x._8.toString, x._9.toString, x._15.formatted("%.2f") + "%", x._19.toString, x._20.formatted("%.2f") + "%", x._6.toString, x._14.formatted("%.2f") + "%", x._10.toString, x._11.toString, x._12.toString, x._16.formatted("%.2f") + "%", x._18.toString, x._17.formatted("%.2f") + "%", x._21.toString)
          ("1", arr)
      })
      .collect()

    new ExceltitleStat().excelcontent(mingyin_data, sheet1, style, gongli_data.length + 3, 1)

    val shequ_data = data.filter(x => x._3.equals("社区")).sortBy(x => x._14, false)
      .map({
        x =>
          val arr = Array[String]("社区", "0", x._2.toString, x._4.toString, x._5.toString, x._13.formatted("%.2f") + "%", x._7.toString, x._8.toString, x._9.toString, x._15.formatted("%.2f") + "%", x._19.toString, x._20.formatted("%.2f") + "%", x._6.toString, x._14.formatted("%.2f") + "%", x._10.toString, x._11.toString, x._12.toString, x._16.formatted("%.2f") + "%", x._18.toString, x._17.formatted("%.2f") + "%", x._21.toString)
          ("1", arr)
      })
      .collect()

    new ExceltitleStat().excelcontent(shequ_data, sheet1, style, gongli_data.length + mingyin_data.length + 5, 1)

    val qita_data = data.filter(x => x._3.equals("无")).sortBy(x => x._14, false)
      .map({
        x =>
          val arr = Array[String]("无", "0", x._2.toString, x._4.toString, x._5.toString, x._13.formatted("%.2f") + "%", x._7.toString, x._8.toString, x._9.toString, x._15.formatted("%.2f") + "%", x._19.toString, x._20.formatted("%.2f") + "%", x._6.toString, x._14.formatted("%.2f") + "%", x._10.toString, x._11.toString, x._12.toString, x._16.formatted("%.2f") + "%", x._18.toString, x._17.formatted("%.2f") + "%", x._21.toString)
          ("1", arr)
      })
      .collect()
    new ExceltitleStat().excelcontent(qita_data, sheet1, style, gongli_data.length + mingyin_data.length + shequ_data.length + 7, 1)

    //留样
    val sheet2 = workbook.createSheet("留样情况")
    val sheetWidth2 = Array[Int](2500, 2500, 8000, 5000, 5000)
    new ExceltitleStat().sheetname(sheet2, sheetWidth2)

    val excelTitleValue1 = Array[String]("性质", "序号", "医院名称", "已留样条数", "留样情况")
    val row2 = sheet2.createRow(0)
    new ExceltitleStat().exceltitle(excelTitleValue1, style, row2)

    val reserve_data = hospital_data.leftOuterJoin(hospital_reserve).map(x => (x._2._1._1, x._2._1._2, x._2._2.getOrElse(0)))
    val gongli_reserve_data = reserve_data.filter(x => x._2.equals("公立")).sortBy(x => x._3, false)
      .map({
        x =>
          val arr = Array[String]("公立", "0", x._1, x._3.toString, "")
          ("1", arr)
      }).collect()
    new ExceltitleStat().excelcontent(gongli_reserve_data, sheet2, style, 1, 1)

    val mimgyin_reserve_data = reserve_data.filter(x => x._2.equals("民营")).sortBy(x => x._3, false)
      .map({
        x =>
          val arr = Array[String]("民营", "0", x._1, x._3.toString, "")
          ("1", arr)
      }).collect()
    new ExceltitleStat().excelcontent(mimgyin_reserve_data, sheet2, style, gongli_reserve_data.length + 3, 1)

    val shequ_reserve_data = reserve_data.filter(x => x._2.equals("社区")).sortBy(x => x._3, false)
      .map({
        x =>
          val arr = Array[String]("社区", "0", x._1, x._3.toString, "")
          ("1", arr)
      }).collect()
    new ExceltitleStat().excelcontent(shequ_reserve_data, sheet2, style, gongli_reserve_data.length + mingyin_data.length + 5, 1)

    val qita_reserve_data = reserve_data.filter(x => x._2.equals("无")).sortBy(x => x._3, false)
      .map({
        x =>
          val arr = Array[String]("无", "0", x._1, x._3.toString, "")
          ("1", arr)
      }).collect()
    new ExceltitleStat().excelcontent(qita_reserve_data, sheet2, style, gongli_reserve_data.length + mingyin_data.length + shequ_reserve_data.length + 7, 1)

    val stream = new FileOutputStream(file)

    workbook.write(stream)
    stream.close()

    val fileSystem = FileSystem.get(new URI("hdfs://172.18.14.30:8020/"), new Configuration())
    fileSystem.moveFromLocalFile(new Path("/data/" + filename), new Path("/hospital_week_report/shanghai/" + filename))

    sc.stop()
  }
}
