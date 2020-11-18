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
    val sparkConf = new SparkConf().setAppName("学校基本情况报表")
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
    val sheet1 = workbook.createSheet("学校信息表")

    //area, school_name, social_credit_code, school_nature, level, committee_name,
    //canteen_mode, student_amount, staff_count, corporation, corporation_phone, department_head,
    // department_mobilephone, authorise, province, city, address, customer_name
    val sheetWidth = Array[Int](2500, 2500, 8000, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)

    /**
     * 创建sheet1
     */
    new ExceltitleStat().sheetname(sheet1, sheetWidth)

    /**
     * 设置表格的属性  字体等
     */
    val style = new ExceltitleStat().style(workbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)
    val excelTitleValue = Array[String]("序号", "区", "学校名称", "社会统一信用代码", "办学性质", "学制", "主管部门", "供餐模式", "学生人数", "教职工人数", "法定代表人", "法定代表人手机", "联系人", "授权交易平台", "所在省", "所在市", "详细地址", "关联单位名称")
    val row = sheet1.createRow(0)
    new ExceltitleStat().exceltitle(excelTitleValue, style, row)

    /**
     * 查询从数据库中查询学校的基本数据
     */
    val schoolArr: Array[(String, Array[String])] = hiveContext.sql(s"select * from ads.ads_report_school_info")
      .rdd.map({
      row =>
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
        val arr = Array[String](area, school_name, social_credit_code, school_nature, level, committee_name,
          canteen_mode, student_amount, staff_count, corporation, corporation_phone, department_head,
          department_mobilephone, authorise, province, city, address, customer_name)
        ("1", arr)
    }).collect()

    new ExceltitleStat().excelcontent(schoolArr, sheet1, style, 1, 1)


    val sheet2 = workbook.createSheet("学校信息汇总表1")
    val sheetWidth2 = Array[Int](3000, 3000, 3000, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    new ExceltitleStat().sheetname(sheet2, sheetWidth2)

    val excelTitleValue2 = Array[String]("区号", "学校总数量", "学生总人数", "教职工总人数", "办学性质", "学校数量", "学生数量", "教职数量", "学制小计", "学校数量", "学生人数", "教职工人数", "学制", "学校数量", "学生人数", "教职工人数")
    val schoolNature= hiveContext.sql(s"select * from ads.ads_report_school_group_nature ")
      .rdd.map({
      row =>
        var temp:Map[String,String] = Map()
        val area = row.getAs[String]("area")
        val area_sch_count = row.getAs[String]("area_sch_count")
        val area_stu_sum = row.getAs[String]("area_stu_sum")
        val area_staff_sum = row.getAs[String]("area_staff_sum")

        val school_nature = row.getAs[String]("school_nature")
        val natu_sch_count = row.getAs[String]("natu_sch_count")
        val natu_stu_sum = row.getAs[String]("natu_stu_sum")
        val natu_staff_sum = row.getAs[String]("natu_staff_sum")

        val level_code = row.getAs[String]("level_code")
        val code_sch_count = row.getAs[String]("code_sch_count")
        val code_stu_sum = row.getAs[String]("code_stu_sum")
        val code_staff_sum = row.getAs[String]("code_staff_sum")

        val level = row.getAs[String]("level")
        val level_sch_count = row.getAs[String]("level_sch_count")
        val level_stu_sum = row.getAs[String]("level_stu_sum")
        val level_staff_sum = row.getAs[String]("level_staff_sum")

        temp+=("区级"->area)
        temp+=("学校总数量"-> area_sch_count)
        temp+=("学生总人数"-> area_stu_sum)
        temp+=("教职工总人数"-> area_staff_sum)
        temp+=("办学性质"-> school_nature)
        temp+=("学校数量"-> natu_sch_count)
        temp+=("学生数量"-> natu_stu_sum)
        temp+=("教职数量"-> natu_staff_sum)
        temp+=("学制小计"-> level_code)
        temp+=("小计学校数量"-> code_sch_count)
        temp+=("小计学生人数"-> code_stu_sum)
        temp+=("小计教职工人数"->code_staff_sum)
        temp+=("学制"-> level)
        temp+=("学制学校数量"-> level_sch_count)
        temp+=("学制学生人数"-> level_stu_sum)
        temp+=("学制教职工人数"-> level_staff_sum)
        temp
    }).collect().toList
    //建Excel 表格
    ExportExcelByPoiUtil.createExcelMerge(excelTitleValue2,sheetWidth2,
      Map("学校信息汇总表1"->schoolNature),Array(0,1,2,3,4,5,6,7,8,9,10,11))

    val sheet3 = workbook.createSheet("学校信息汇总表2")
    val sheetWidth3 = Array[Int](100, 100, 100, 150, 150, 150, 150, 150, 150, 150, 150, 150, 150)
    new ExceltitleStat().sheetname(sheet3, sheetWidth3)
    val excelTitleValue3 = Array[String]("区号", "学制小计", "学校数量", "学生人数", "教职工人数", "学制", "学校数量", "学生人数", "教职工人数", "性质", "学校数量", "学生人数", "教职工人数")

    val schoolLevel= hiveContext.sql(s"select * from ads.ads_report_school_group_level ")
      .rdd.map({
      row =>
        val area = row.getAs[String]("area")
        val level_code = row.getAs[String]("level_code")
        val code_sch_count = row.getAs[String]("code_sch_count")
        val code_stu_sum = row.getAs[String]("code_stu_sum")
        val code_staff_sum = row.getAs[String]("code_staff_sum")

        val level = row.getAs[String]("level")
        val level_sch_count = row.getAs[String]("level_sch_count")
        val level_stu_sum = row.getAs[String]("level_stu_sum")
        val level_staff_sum = row.getAs[String]("level_staff_sum")

        val school_nature = row.getAs[String]("school_nature")
        val natu_sch_count = row.getAs[String]("natu_sch_count")
        val natu_stu_sum = row.getAs[String]("natu_stu_sum")
        val natu_staff_sum = row.getAs[String]("natu_staff_sum")
        var temp:Map[String,String] = Map()
        temp+=("区级"->area)
        temp+=("学制小计"-> level_code)
        temp+=("小计学校数量"-> code_sch_count)
        temp+=("小计学生人数"-> code_stu_sum)
        temp+=("小计教职工人数"-> code_staff_sum)
        temp+=("学制"-> level)
        temp+=("学制学校数量"-> level_sch_count)
        temp+=("学制学生人数"-> level_stu_sum)
        temp+=("学制教职工人数"-> level_staff_sum)
        temp+=("性质"-> school_nature)
        temp+=("性质学校数量"-> natu_sch_count)
        temp+=("性质学生人数"->natu_stu_sum)
        temp+=("性质教职工人数"-> natu_staff_sum)
        temp

    }).collect().toList
    ExportExcelByPoiUtil.createExcelMerge(excelTitleValue3,sheetWidth3,
      Map("学校信息汇总表2"->schoolNature),Array(0,1,2,3,4,5,6,7,8))

    val stream = new FileOutputStream(file)

    workbook.write(stream)
    stream.close()

    val fileSystem = FileSystem.get(new URI("hdfs://172.18.14.30:8020/"), new Configuration())
    fileSystem.moveFromLocalFile(new Path("/data/" + filename), new Path("/schoolReport/" + filename))
    sc.stop()

  }

}
