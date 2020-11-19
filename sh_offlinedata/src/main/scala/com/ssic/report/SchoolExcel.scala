package com.ssic.report

import java.io.{File, FileOutputStream}
import java.net.URI

import com.ssic.service.ExceltitleStat
import com.ssic.utils.{ExportExcelByPoiUtil, Rule}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

object SchoolExcel {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("运营后台一期改造-学校信息概括")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)

    /**
     * 时间日期格式
     */
    val nowday = Rule.timeToStamp("yyyy-MM-dd", 0) //format.format(time2)

    val filename = "运营后台一期改造-学校信息概括" + nowday + ".xls"
    val file = new File("/data/" + filename)
    val workbook = new HSSFWorkbook()

    val sheetWidth = Array[Int](1500, 3500, 6000, 12500, 5500, 5500, 6500, 3500, 3500, 3500, 3500, 9500, 4500, 9500, 3500, 3500, 3500, 13500,13500)
    val excelTitleValue = Array[String]("序号", "区", "学校名称", "社会统一信用代码",
      "办学性质", "学制", "主管部门", "供餐模式",
      "学生人数", "教职工人数", "法定代表人", "法定代表人手机",
      "联系人","联系手机", "授权交易平台", "所在省", "所在市",
      "详细地址", "关联单位名称")

    /**
     * 查询从数据库中查询学校的基本数据
     */

    val schoolArr = hiveContext.sql(s"select * from ads.ads_report_school_info")
      .rdd.map({

      row =>
        val rn = row.getAs[Long]("rn")
        val area = row.getAs[String]("area")
        val school_name = row.getAs[String]("school_name")
        val social_credit_code = row.getAs[String]("social_credit_code")
        val school_nature = row.getAs[String]("school_nature")
        val level = row.getAs[String]("level")
        val committee_name = row.getAs[String]("committee_name")
        val canteen_mode = row.getAs[String]("canteen_mode")
        val student_amount = row.getAs[String]("student_amount")
        val staff_count = row.getAs[String]("staff_count")
        val corporation = row.getAs[String]("corporation")
        val corporation_phone = row.getAs[String]("corporation_phone")
        val department_head = row.getAs[String]("department_head")
        val department_mobilephone = row.getAs[String]("department_mobilephone")
        val authorise = row.getAs[String]("authorise")
        val province = row.getAs[String]("province")
        val city = row.getAs[String]("city")
        val address = row.getAs[String]("address")
        val customer_name = row.getAs[String]("customer_name")
        //
        var temp: Map[String, String] = Map()
        temp += ("序号" -> rn.toString)
        temp += ("区" -> area)
        temp += ("学校名称" -> school_name)
        temp += ("社会统一信用代码" -> social_credit_code)
        temp += ("办学性质" -> school_nature)
        temp += ("学制" -> level)
        temp += ("主管部门" -> committee_name)
        temp += ("供餐模式" -> canteen_mode)
        temp += ("联系手机" -> department_mobilephone)
        temp += ("学生人数" -> student_amount)
        temp += ("教职工人数" -> staff_count)
        temp += ("法定代表人" -> corporation)
        temp += ("法定代表人手机" -> corporation_phone)
        temp += ("联系人" -> department_head)
        temp += ("授权交易平台" -> authorise)
        temp += ("所在省" -> province)
        temp += ("所在市" -> city)
        temp += ("详细地址" -> address)
        temp += ("关联单位名称" -> customer_name)

        temp

    }).collect().toList
    ExportExcelByPoiUtil.createExcel(excelTitleValue, sheetWidth,
      Map("学校基础表" -> schoolArr),Array[Int](), workbook)

    val sheetWidth2 = Array[Int](3000, 3000, 3000, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 5500, 3500, 3500, 3500)
    val excelTitleValue2 = Array[String]("区", "学校总数量", "学生总人数", "教职工总人数",
      "办学性质", "学校数量", "学生数量", "教职数量",
      "学制小计", "小计学校数量", "小计学生人数", "小计教职工人数",
      "学制", "学制学校数量", "学制学生人数", "学制教职工人数")
    val schoolNature = hiveContext.sql(s"select * from ads.ads_report_school_group_nature ")
      .rdd.map({
      row =>
        var temp: Map[String, String] = Map()
        val area = row.getAs[String]("area")
        val area_sch_count = row.getAs[Long]("area_sch_count")
        val area_stu_sum = row.getAs[Long]("area_stu_sum")
        val area_staff_sum = row.getAs[Long]("area_staff_sum")

        val school_nature = row.getAs[String]("school_nature")
        val natu_sch_count = row.getAs[Long]("natu_sch_count")
        val natu_stu_sum = row.getAs[Long]("natu_stu_sum")
        val natu_staff_sum = row.getAs[Long]("natu_staff_sum")

        val level_code = row.getAs[String]("level_code")
        val code_sch_count = row.getAs[Long]("code_sch_count")
        val code_stu_sum = row.getAs[Long]("code_stu_sum")
        val code_staff_sum = row.getAs[Long]("code_staff_sum")

        val level = row.getAs[String]("level")
        val level_sch_count = row.getAs[Long]("level_sch_count")
        val level_stu_sum = row.getAs[Long]("level_stu_sum")
        val level_staff_sum = row.getAs[Long]("level_staff_sum")

        temp += ("区" -> area)
        temp += ("学校总数量" -> area_sch_count.toString)
        temp += ("学生总人数" -> area_stu_sum.toString)
        temp += ("教职工总人数" -> area_staff_sum.toString)
        temp += ("办学性质" -> school_nature)
        temp += ("学校数量" -> natu_sch_count.toString)
        temp += ("学生数量" -> natu_stu_sum.toString)
        temp += ("教职数量" -> natu_staff_sum.toString)
        temp += ("学制小计" -> level_code)
        temp += ("小计学校数量" -> code_sch_count.toString)
        temp += ("小计学生人数" -> code_stu_sum.toString)
        temp += ("小计教职工人数" -> code_staff_sum.toString)
        temp += ("学制" -> level)
        temp += ("学制学校数量" -> level_sch_count.toString)
        temp += ("学制学生人数" -> level_stu_sum.toString)
        temp += ("学制教职工人数" -> level_staff_sum.toString)
        temp
    }).collect().toList

    //建Excel 表格
    ExportExcelByPoiUtil.createExcel(excelTitleValue2, sheetWidth2,
      Map("学校信息汇总表1" -> schoolNature), Array(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11), workbook)


    val sheetWidth3 = Array[Int](3000, 3000, 3000, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val excelTitleValue3 = Array[String]("区号",
      "学制小计", "小计学校数量", "小计学生人数", "小计教职工人数",
      "学制", "学制学校数量", "学制学生人数", "学制教职工人数",
      "性质", "性质学校数量", "性质学生人数", "性质教职工人数")

    val schoolLevel = hiveContext.sql(s"select * from ads.ads_report_school_group_level ")
      .rdd.map({
      row =>
        var temp: Map[String, String] = Map()
        val area = row.getAs[String]("area")
        val level_code = row.getAs[String]("level_code")
        val code_sch_count = row.getAs[Long]("code_sch_count")
        val code_stu_sum = row.getAs[Long]("code_stu_sum")
        val code_staff_sum = row.getAs[Long]("code_staff_sum")

        val level = row.getAs[String]("level")
        val level_sch_count = row.getAs[Long]("level_sch_count")
        val level_stu_sum = row.getAs[Long]("level_stu_sum")
        val level_staff_sum = row.getAs[Long]("level_staff_sum")

        val school_nature = row.getAs[String]("school_nature")
        val natu_sch_count = row.getAs[Long]("natu_sch_count")
        val natu_stu_sum = row.getAs[Long]("natu_stu_sum")
        val natu_staff_sum = row.getAs[Long]("natu_staff_sum")

        temp += ("区号" -> area)
        temp += ("学制小计" -> level_code)
        temp += ("小计学校数量" -> code_sch_count.toString)
        temp += ("小计学生人数" -> code_stu_sum.toString)
        temp += ("小计教职工人数" -> code_staff_sum.toString)
        temp += ("学制" -> level)
        temp += ("学制学校数量" -> level_sch_count.toString)
        temp += ("学制学生人数" -> level_stu_sum.toString)
        temp += ("学制教职工人数" -> level_staff_sum.toString)
        temp += ("性质" -> school_nature)
        temp += ("性质学校数量" -> natu_sch_count.toString)
        temp += ("性质学生人数" -> natu_stu_sum.toString)
        temp += ("性质教职工人数" -> natu_staff_sum.toString)
        temp

    }).collect().toList
    ExportExcelByPoiUtil.createExcel(excelTitleValue3, sheetWidth3,
      Map("学校信息汇总表2" -> schoolLevel), Array(0, 1, 2, 3, 4, 5, 6, 7, 8), workbook)

    val stream = new FileOutputStream(file)

    workbook.write(stream)
    stream.close()

    val fileSystem = FileSystem.get(new URI("hdfs://172.18.14.30:8020/"), new Configuration())
    fileSystem.moveFromLocalFile(new Path("/data/" + filename), new Path("/schoolReport/" + filename))
    sc.stop()

  }

}
