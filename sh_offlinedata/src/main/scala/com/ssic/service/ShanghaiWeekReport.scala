package com.ssic.service

import com.ssic.impl.ShanghaiWeekRepFuc
import jxl.write.{Label, WritableSheet}
import org.apache.poi.hssf.usermodel.{HSSFSheet, HSSFWorkbook}
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment}
import org.apache.poi.ss.util.{CellRangeAddress, RegionUtil}
import org.apache.spark.rdd.RDD

class ShanghaiWeekReport extends ShanghaiWeekRepFuc {
  //各管理部门排菜统计
  override def areaplatoonweek(data: (HSSFSheet, Array[(String, String, String, String, String, String, String, Int, Int, Int)])): Unit = {

    val row = data._1.createRow(0)
    val cell = row.createCell(0)
    cell.setCellValue("序号")
    val cell1 = row.createCell(1)
    cell1.setCellValue("管理部门")
    val cell2 = row.createCell(2)
    cell2.setCellValue("学校名称")
    val cell3 = row.createCell(3)
    cell3.setCellValue("学校地址")
    val cell4 = row.createCell(4)
    cell4.setCellValue("联系人")
    val cell5 = row.createCell(5)
    cell5.setCellValue("联系电话")
    val cell6 = row.createCell(6)
    cell6.setCellValue("学校分类")
    val cell7 = row.createCell(7)
    cell7.setCellValue("学校类型")
    val cell8 = row.createCell(8)
    cell8.setCellValue("学期设置供餐天数")
    val cell9 = row.createCell(9)
    cell9.setCellValue("已排菜天数")
    val cell10 = row.createCell(10)
    cell10.setCellValue("未排菜天数")

    for (i <- 0 until data._2.length) {
      val row = data._1.createRow(i + 1)
      val cell = row.createCell(0)
      cell.setCellValue((i + 1).toString)
      val cell1 = row.createCell(1)
      cell1.setCellValue(data._2(i)._1)
      val cell2 = row.createCell(2)
      cell2.setCellValue(data._2(i)._2)
      val cell3 = row.createCell(3)
      cell3.setCellValue(data._2(i)._3)
      val cell4 = row.createCell(4)
      cell4.setCellValue(data._2(i)._4)
      val cell5 = row.createCell(5)
      cell5.setCellValue(data._2(i)._5)
      val cell6 = row.createCell(6)
      cell6.setCellValue(data._2(i)._6)
      val cell7 = row.createCell(7)
      cell7.setCellValue(data._2(i)._7)
      val cell8 = row.createCell(8)
      cell8.setCellValue(data._2(i)._8.toString)
      val cell9 = row.createCell(9)
      cell9.setCellValue(data._2(i)._9.toString)
      val cell10 = row.createCell(10)
      cell10.setCellValue(data._2(i)._10.toString)


    }
  }


  override def arealedgerweek(data: (HSSFSheet, Array[(String, String, String, String, String, String, String, Int, Int, Int)])): Unit = {
    val row = data._1.createRow(0)
    val cell = row.createCell(0)
    cell.setCellValue("序号")
    val cell1 = row.createCell(1)
    cell1.setCellValue("管理部门")
    val cell2 = row.createCell(2)
    cell2.setCellValue("学校名称")
    val cell3 = row.createCell(3)
    cell3.setCellValue("学校地址")
    val cell4 = row.createCell(4)
    cell4.setCellValue("联系人")
    val cell5 = row.createCell(5)
    cell5.setCellValue("联系电话")
    val cell6 = row.createCell(6)
    cell6.setCellValue("学校分类")
    val cell7 = row.createCell(7)
    cell7.setCellValue("学校类型")
    val cell8 = row.createCell(8)
    cell8.setCellValue("学期设置供餐天数")
    val cell9 = row.createCell(9)
    cell9.setCellValue("已验收天数")
    val cell10 = row.createCell(10)
    cell10.setCellValue("未验收天数")

    for (i <- 0 until data._2.length) {
      val row = data._1.createRow(i + 1)
      val cell = row.createCell(0)
      cell.setCellValue((i + 1).toString)
      val cell1 = row.createCell(1)
      cell1.setCellValue(data._2(i)._1)
      val cell2 = row.createCell(2)
      cell2.setCellValue(data._2(i)._2)
      val cell3 = row.createCell(3)
      cell3.setCellValue(data._2(i)._3)
      val cell4 = row.createCell(4)
      cell4.setCellValue(data._2(i)._4)
      val cell5 = row.createCell(5)
      cell5.setCellValue(data._2(i)._5)
      val cell6 = row.createCell(6)
      cell6.setCellValue(data._2(i)._6)
      val cell7 = row.createCell(7)
      cell7.setCellValue(data._2(i)._7)
      val cell8 = row.createCell(8)
      cell8.setCellValue(data._2(i)._8.toString)
      val cell9 = row.createCell(9)
      cell9.setCellValue(data._2(i)._9.toString)
      val cell10 = row.createCell(10)
      cell10.setCellValue(data._2(i)._10.toString)

    }

  }


  override def shangtotal(data: (HSSFSheet, RDD[(String, String, String, String, String, String, String, Int, Int, Int)], HSSFWorkbook, RDD[(String, String, String, String, String, String, String, Int, Int, Int)], String, String, RDD[(String, String, String, String, String, String, String, Integer, String, String, Int, String, Integer, String, String, String)],String,String)): Unit = {

    data._1.setColumnWidth(0, 2500)
    data._1.setColumnWidth(1, 5000)
    data._1.setColumnWidth(2, 3500)
    data._1.setColumnWidth(3, 3500)
    data._1.setColumnWidth(4, 3500)
    data._1.setColumnWidth(5, 3500)
    data._1.setColumnWidth(6, 3500)
    data._1.setColumnWidth(7, 3500)
    data._1.setColumnWidth(8, 3500)
    data._1.setColumnWidth(9, 3500)
    data._1.setColumnWidth(10, 5800)

    val style = data._3.createCellStyle()
    val font = data._3.createFont()
    font.setFontHeightInPoints(12)
    font.setBold(true)
    font.setFontName("宋体")
    style.setAlignment(HorizontalAlignment.CENTER)
    style.setBorderBottom(BorderStyle.THIN)
    style.setBorderLeft(BorderStyle.THIN)
    style.setBorderRight(BorderStyle.THIN)
    style.setBorderTop(BorderStyle.THIN)
    style.setFont(font)

    val style1 = data._3.createCellStyle()
    val font1 = data._3.createFont()
    font1.setFontHeightInPoints(22)
    font1.setBold(true)
    font1.setFontName("宋体")
    style1.setAlignment(HorizontalAlignment.CENTER)
    style1.setFont(font1)

    val style2 = data._3.createCellStyle()
    val font2 = data._3.createFont()
    font2.setFontHeightInPoints(12)
    font2.setBold(true)
    font2.setFontName("宋体")
    style2.setAlignment(HorizontalAlignment.LEFT)
    style2.setFont(font2)

    val style3 = data._3.createCellStyle()
    val font3 = data._3.createFont()
    font3.setFontHeightInPoints(13)
    font3.setBold(false)
    font3.setFontName("宋体")
    style3.setAlignment(HorizontalAlignment.CENTER)
    style3.setBorderBottom(BorderStyle.THIN)
    style3.setBorderLeft(BorderStyle.THIN)
    style3.setBorderRight(BorderStyle.THIN)
    style3.setBorderTop(BorderStyle.THIN)
    style3.setFont(font3)

    val style4 = data._3.createCellStyle()
    val font4 = data._3.createFont()
    font4.setFontHeightInPoints(12)
    font4.setBold(true)
    font4.setFontName("宋体")
    style4.setAlignment(HorizontalAlignment.CENTER)
    style4.setFont(font4)

    val style5 = data._3.createCellStyle()
    val font5 = data._3.createFont()
    font5.setFontHeightInPoints(16)
    font5.setBold(true)
    font5.setFontName("宋体")
    style5.setAlignment(HorizontalAlignment.CENTER)
    style5.setBorderBottom(BorderStyle.THIN)
    style5.setBorderLeft(BorderStyle.THIN)
    style5.setBorderRight(BorderStyle.THIN)
    style5.setBorderTop(BorderStyle.THIN)
    style5.setFont(font5)

    val row = data._1.createRow(0)
    val cellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
    data._1.addMergedRegion(cellRangeAddress)
    val cell = row.createCell(0)
    cell.setCellValue(data._9+"年食安管理平台使用、验收及证照逾期处理情况")
    cell.setCellStyle(style1)

    val row1 = data._1.createRow(1)
    val cellRangeAddress1 = new CellRangeAddress(1, 1, 0, 11)
    data._1.addMergedRegion(cellRangeAddress1)
    val cell1 = row1.createCell(0)
    cell1.setCellValue("表格内请注明学校总数、托幼数、中小学数，学校使用、验收总数、托幼、中小学使用和验收数，证照逾期总数和已处理数。")
    cell1.setCellStyle(style4)

    val row2 = data._1.createRow(2)
    row2.setHeight(500)
    val cellRangeAddress2 = new CellRangeAddress(2, 3, 0, 0)
    data._1.addMergedRegion(cellRangeAddress2)
    val cell2 = row2.createCell(0)
    cell2.setCellValue("序号")
    cell2.setCellStyle(style5)

    val cellRangeAddress3 = new CellRangeAddress(2, 3, 1, 1)
    data._1.addMergedRegion(cellRangeAddress3)
    val cell3 = row2.createCell(1)
    cell3.setCellValue("所在区")
    cell3.setCellStyle(style5)

    val cellRangeAddress4 = new CellRangeAddress(2, 2, 2, 5)
    data._1.addMergedRegion(cellRangeAddress4)
    RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress4, data._1)
    RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress4, data._1)
    RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress4, data._1)
    RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress4, data._1)
    val cell4 = row2.createCell(2)
    cell4.setCellValue("使用率")
    cell4.setCellStyle(style5)

    val cellRangeAddress5 = new CellRangeAddress(2, 2, 6, 9)
    data._1.addMergedRegion(cellRangeAddress5)
    RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress5, data._1)
    RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress5, data._1)
    RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress5, data._1)
    RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress5, data._1)
    val cell5 = row2.createCell(6)
    cell5.setCellValue("验收率")
    cell5.setCellStyle(style5)

    val cell6 = row2.createCell(10)
    cell6.setCellValue("证照逾期处理率")
    cell6.setCellStyle(style5)

    val row3 = data._1.createRow(3)
    val cell7 = row3.createCell(2)
    cell7.setCellValue("托幼")
    cell7.setCellStyle(style)
    val cell8 = row3.createCell(3)
    cell8.setCellValue("中小学")
    cell8.setCellStyle(style)
    val cell9 = row3.createCell(4)
    cell9.setCellValue("其他")
    cell9.setCellStyle(style)
    val cell10 = row3.createCell(5)
    cell10.setCellValue("总")
    cell10.setCellStyle(style)
    val cell11 = row3.createCell(6)
    cell11.setCellValue("托幼")
    cell11.setCellStyle(style)
    val cell12 = row3.createCell(7)
    cell12.setCellValue("中小学")
    cell12.setCellStyle(style)
    val cell13 = row3.createCell(8)
    cell13.setCellValue("其他")
    cell13.setCellStyle(style)
    val cell14 = row3.createCell(9)
    cell14.setCellValue("总")
    cell14.setCellStyle(style)
    val cell15 = row3.createCell(10)
    cell15.setCellValue("总")
    cell15.setCellStyle(style)

    val should_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).map(x => (x._1, x._8)).reduceByKey(_ + _)
    val have_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).map(x => (x._1, x._9)).reduceByKey(_ + _)
    val you_should_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val you_have_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_should_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_have_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_should_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_have_platoon_total = data._2.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val should_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).map(x => (x._1, x._8)).reduceByKey(_ + _)
    val have_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).map(x => (x._1, x._9)).reduceByKey(_ + _)
    val you_should_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val you_have_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_should_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_have_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_should_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_have_ledger_total = data._4.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val deal_license_data = data._7.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "逾期".equals(x._16)).filter(x => x._11 == 4).map(x => (x._1, 1)).reduceByKey(_ + _)
    val license_data_total = data._7.filter(x => !"其他".equals(x._1)).filter(x => !"外籍人员子女学校".equals(x._1)).filter(x => !"市属中职校".equals(x._1)).filter(x => "逾期".equals(x._16)).map(x => (x._1, 1)).reduceByKey(_ + _)


    val totals = should_platoon_total.leftOuterJoin(have_platoon_total).leftOuterJoin(you_should_platoon_total).leftOuterJoin(you_have_platoon_total).leftOuterJoin(zhong_should_platoon_total).leftOuterJoin(zhong_have_platoon_total).leftOuterJoin(qi_should_platoon_total).leftOuterJoin(qi_have_platoon_total).leftOuterJoin(should_ledger_total).leftOuterJoin(have_ledger_total).leftOuterJoin(you_should_ledger_total).leftOuterJoin(you_have_ledger_total).leftOuterJoin(zhong_should_ledger_total).leftOuterJoin(zhong_have_ledger_total).leftOuterJoin(qi_should_ledger_total).leftOuterJoin(qi_have_ledger_total).leftOuterJoin(deal_license_data).leftOuterJoin(license_data_total)
      .map(x => {
        val qu = x._1

        val should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1
        val have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val you_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val you_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val qi_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val qi_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2

        val should_ledger_total = x._2._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val have_ledger_total = x._2._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val you_should_ledger_total = x._2._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val you_have_ledger_total = x._2._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_should_ledger_total = x._2._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_have_ledger_total = x._2._1._1._1._1._2.getOrElse("null", 0)._2
        val qi_should_ledger_total = x._2._1._1._1._2.getOrElse("null", 0)._2
        val qi_have_ledger_total = x._2._1._1._2.getOrElse("null", 0)._2

        val deal_license_data = x._2._1._2.getOrElse(0)
        val license_data_total = x._2._2.getOrElse(0)

        var platoon_lv = 0.00
        if (should_platoon_total == 0) {
          platoon_lv
        } else {
          platoon_lv = (have_platoon_total.toDouble / should_platoon_total.toDouble) * 100
        }

        var you_platoon_lv = 0.00
        if (you_should_platoon_total == 0) {
          you_platoon_lv
        } else {
          you_platoon_lv = (you_have_platoon_total.toDouble / you_should_platoon_total.toDouble) * 100
        }

        var you_platoon_count = 0
        if (you_should_platoon_total == 0) {
          you_platoon_count
        } else {
          you_platoon_count = 1
        }

        var zhong_platoon_lv = 0.00
        if (zhong_should_platoon_total == 0) {
          zhong_platoon_lv
        } else {
          zhong_platoon_lv = (zhong_have_platoon_total.toDouble / zhong_should_platoon_total.toDouble) * 100
        }

        var zhong_platoon_count = 0
        if (zhong_should_platoon_total == 0) {
          zhong_platoon_count
        } else {
          zhong_platoon_count = 1
        }

        var qi_platoon_lv = 0.00
        if (qi_should_platoon_total == 0) {
          qi_platoon_lv
        } else {
          qi_platoon_lv = (qi_have_platoon_total.toDouble / qi_should_platoon_total.toDouble) * 100
        }

        var qi_platoon_count = 0
        if (qi_should_platoon_total == 0) {
          qi_platoon_count
        } else {
          qi_platoon_count = 1
        }

        var ledger_lv = 0.00
        if (should_ledger_total == 0) {
          ledger_lv
        } else {
          ledger_lv = ((have_ledger_total.toDouble / should_ledger_total.toDouble) * 100)
        }

        var you_ledger_lv = 0.00
        if (you_should_ledger_total == 0) {
          you_ledger_lv
        } else {
          you_ledger_lv = (you_have_ledger_total.toDouble / you_should_ledger_total.toDouble) * 100
        }

        var you_ledger_count = 0
        if (you_should_ledger_total == 0) {
          you_ledger_count
        } else {
          you_ledger_count = 1
        }

        var zhong_ledger_lv = 0.00
        if (zhong_should_ledger_total == 0) {
          zhong_ledger_lv
        } else {
          zhong_ledger_lv = (zhong_have_ledger_total.toDouble / zhong_should_ledger_total.toDouble) * 100
        }

        var zhong_ledger_count = 0
        if (zhong_should_ledger_total == 0) {
          zhong_ledger_count
        } else {
          zhong_ledger_count = 1
        }

        var qi_ledger_lv = 0.00
        if (qi_should_ledger_total == 0) {
          qi_ledger_lv
        } else {
          qi_ledger_lv = (qi_have_ledger_total.toDouble / qi_should_ledger_total.toDouble) * 100
        }

        var qi_ledger_count = 0
        if (qi_should_ledger_total == 0) {
          qi_ledger_count
        } else {
          qi_ledger_count = 1
        }

        var license_warn_lv = 0.00
        if (license_data_total == 0) {
          license_warn_lv
        } else {
          license_warn_lv = (deal_license_data.toDouble / license_data_total.toDouble) * 100
        }

        var license_warn_count = 0
        if (license_data_total == 0) {
          license_warn_count
        } else {
          license_warn_count = 1
        }

        (qu, you_platoon_lv, zhong_platoon_lv, qi_platoon_lv, platoon_lv, you_ledger_lv, zhong_ledger_lv, qi_ledger_lv, ledger_lv, you_platoon_count, zhong_platoon_count, qi_platoon_count, you_ledger_count, zhong_ledger_count, qi_ledger_count, license_warn_lv, license_warn_count)
      }).sortBy(x => x._5, false)

    val total_lv = totals.collect()

    for (i <- 0 until total_lv.length) {
      val row4 = data._1.createRow(i + 4)
      row4.setHeight(400)
      val cell16 = row4.createCell(0)
      cell16.setCellValue((i + 1).toString)
      cell16.setCellStyle(style3)
      val cell17 = row4.createCell(1)
      cell17.setCellValue(total_lv(i)._1)
      cell17.setCellStyle(style3)
      val cell18 = row4.createCell(2)
      cell18.setCellValue(total_lv(i)._2.formatted("%.2f") + "%")
      cell18.setCellStyle(style3)
      val cell19 = row4.createCell(3)
      cell19.setCellValue(total_lv(i)._3.formatted("%.2f") + "%")
      cell19.setCellStyle(style3)
      val cell20 = row4.createCell(4)
      cell20.setCellValue(total_lv(i)._4.formatted("%.2f") + "%")
      cell20.setCellStyle(style3)
      val cell21 = row4.createCell(5)
      cell21.setCellValue(total_lv(i)._5.formatted("%.2f") + "%")
      cell21.setCellStyle(style3)
      val cell22 = row4.createCell(6)
      cell22.setCellValue(total_lv(i)._6.formatted("%.2f") + "%")
      cell22.setCellStyle(style3)
      val cell23 = row4.createCell(7)
      cell23.setCellValue(total_lv(i)._7.formatted("%.2f") + "%")
      cell23.setCellStyle(style3)
      val cell24 = row4.createCell(8)
      cell24.setCellValue(total_lv(i)._8.formatted("%.2f") + "%")
      cell24.setCellStyle(style3)
      val cell25 = row4.createCell(9)
      cell25.setCellValue(total_lv(i)._9.formatted("%.2f") + "%")
      cell25.setCellStyle(style3)
      val cell26 = row4.createCell(10)
      cell26.setCellValue(total_lv(i)._16.formatted("%.2f") + "%")
      cell26.setCellStyle(style3)

    }

    //小计
    val row5 = data._1.createRow(total_lv.length + 4)
    row5.setHeight(400)
    val cell27 = row5.createCell(0)
    cell27.setCellValue((total_lv.length + 1).toString)
    cell27.setCellStyle(style3)

    val cell28 = row5.createCell(1)
    cell28.setCellValue("小计")
    cell28.setCellStyle(style3)

    val cell29 = row5.createCell(2)
    cell29.setCellValue((totals.map(_._2).sum() / totals.map(_._10).sum()).formatted("%.2f") + "%")
    cell29.setCellStyle(style3)

    val cell30 = row5.createCell(3)
    cell30.setCellValue((totals.map(_._3).sum() / totals.map(_._11).sum()).formatted("%.2f") + "%")
    cell30.setCellStyle(style3)

    val cell31 = row5.createCell(4)
    cell31.setCellValue((totals.map(_._4).sum() / totals.map(_._12).sum()).formatted("%.2f") + "%")
    cell31.setCellStyle(style3)

    val cell32 = row5.createCell(5)
    cell32.setCellValue((totals.map(_._5).sum() / total_lv.length).formatted("%.2f") + "%")
    cell32.setCellStyle(style3)

    val cell33 = row5.createCell(6)
    cell33.setCellValue((totals.map(_._6).sum() / totals.map(_._13).sum()).formatted("%.2f") + "%")
    cell33.setCellStyle(style3)

    val cell34 = row5.createCell(7)
    cell34.setCellValue((totals.map(_._7).sum() / totals.map(_._14).sum()).formatted("%.2f") + "%")
    cell34.setCellStyle(style3)

    val cell35 = row5.createCell(8)
    cell35.setCellValue((totals.map(_._8).sum() / totals.map(_._15).sum()).formatted("%.2f") + "%")
    cell35.setCellStyle(style3)

    val cell36 = row5.createCell(9)
    cell36.setCellValue((totals.map(_._9).sum() / total_lv.length).formatted("%.2f") + "%")
    cell36.setCellStyle(style3)

    val cell37 = row5.createCell(10)
    cell37.setCellValue((totals.map(_._16).sum() / totals.map(_._17).sum()).formatted("%.2f") + "%")
    cell37.setCellStyle(style3)


    //外籍人员子女学校 的排菜，配送 ,证照预警
    val wai_should_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).map(x => (x._1, x._8)).reduceByKey(_ + _)
    val wai_have_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).map(x => (x._1, x._9)).reduceByKey(_ + _)
    val wai_you_should_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_you_have_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_zhong_should_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_zhong_have_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_qi_should_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_qi_have_platoon_total = data._2.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val wai_should_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).map(x => (x._1, x._8)).reduceByKey(_ + _)
    val wai_have_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).map(x => (x._1, x._9)).reduceByKey(_ + _)
    val wai_you_should_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_you_have_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_zhong_should_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_zhong_have_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_qi_should_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val wai_qi_have_ledger_total = data._4.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val wai_deal_license_data = data._7.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "逾期".equals(x._16)).filter(x => x._11 == 4).map(x => (x._1, 1)).reduceByKey(_ + _)
    val wai_license_data_total = data._7.filter(x => "外籍人员子女学校".equals(x._1)).filter(x => "逾期".equals(x._16)).map(x => (x._1, 1)).reduceByKey(_ + _)

    val wai_totals = wai_should_platoon_total.leftOuterJoin(wai_have_platoon_total).leftOuterJoin(wai_you_should_platoon_total).leftOuterJoin(wai_you_have_platoon_total).leftOuterJoin(wai_zhong_should_platoon_total).leftOuterJoin(wai_zhong_have_platoon_total).leftOuterJoin(wai_qi_should_platoon_total).leftOuterJoin(wai_qi_have_platoon_total).leftOuterJoin(wai_should_ledger_total).leftOuterJoin(wai_have_ledger_total).leftOuterJoin(wai_you_should_ledger_total).leftOuterJoin(wai_you_have_ledger_total).leftOuterJoin(wai_zhong_should_ledger_total).leftOuterJoin(wai_zhong_have_ledger_total).leftOuterJoin(wai_qi_should_ledger_total).leftOuterJoin(wai_qi_have_ledger_total).leftOuterJoin(wai_deal_license_data).leftOuterJoin(wai_license_data_total)
      .map(x => {
        val qu = x._1

        val wai_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1
        val wai_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val wai_you_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_you_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_zhong_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_zhong_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_qi_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_qi_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2

        val wai_should_ledger_total = x._2._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val wai_have_ledger_total = x._2._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val wai_you_should_ledger_total = x._2._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_you_have_ledger_total = x._2._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_zhong_should_ledger_total = x._2._1._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_zhong_have_ledger_total = x._2._1._1._1._1._2.getOrElse("null", 0)._2
        val wai_qi_should_ledger_total = x._2._1._1._1._2.getOrElse("null", 0)._2
        val wai_qi_have_ledger_total = x._2._1._1._2.getOrElse("null", 0)._2

        val wai_deal_license_data = x._2._1._2.getOrElse(0)
        val wai_license_data_total = x._2._2.getOrElse(0)

        var wai_platoon_lv = 0.00
        if (wai_should_platoon_total == 0) {
          wai_platoon_lv
        } else {
          wai_platoon_lv = (wai_have_platoon_total.toDouble / wai_should_platoon_total.toDouble) * 100
        }

        var wai_you_platoon_lv = 0.00
        if (wai_you_should_platoon_total == 0) {
          wai_you_platoon_lv
        } else {
          wai_you_platoon_lv = (wai_you_have_platoon_total.toDouble / wai_you_should_platoon_total.toDouble) * 100
        }

        var wai_you_platoon_count = 0
        if (wai_you_should_platoon_total == 0) {
          wai_you_platoon_count
        } else {
          wai_you_platoon_count = 1
        }

        var wai_zhong_platoon_lv = 0.00
        if (wai_zhong_should_platoon_total == 0) {
          wai_zhong_platoon_lv
        } else {
          wai_zhong_platoon_lv = (wai_zhong_have_platoon_total.toDouble / wai_zhong_should_platoon_total.toDouble) * 100
        }

        var wai_zhong_platoon_count = 0
        if (wai_zhong_should_platoon_total == 0) {
          wai_zhong_platoon_count
        } else {
          wai_zhong_platoon_count = 1
        }

        var wai_qi_platoon_lv = 0.00
        if (wai_qi_should_platoon_total == 0) {
          wai_qi_platoon_lv
        } else {
          wai_qi_platoon_lv = (wai_qi_have_platoon_total.toDouble / wai_qi_should_platoon_total.toDouble) * 100
        }

        var wai_qi_platoon_count = 0
        if (wai_qi_should_platoon_total == 0) {
          wai_qi_platoon_count
        } else {
          wai_qi_platoon_count = 1
        }

        var wai_ledger_lv = 0.00
        if (wai_should_ledger_total == 0) {
          wai_ledger_lv
        } else {
          wai_ledger_lv = (wai_have_ledger_total.toDouble / wai_should_ledger_total.toDouble) * 100
        }

        var wai_you_ledger_lv = 0.00
        if (wai_you_should_ledger_total == 0) {
          wai_you_ledger_lv
        } else {
          wai_you_ledger_lv = (wai_you_have_ledger_total.toDouble / wai_you_should_ledger_total.toDouble) * 100
        }

        var wai_you_ledger_count = 0
        if (wai_you_should_ledger_total == 0) {
          wai_you_ledger_count
        } else {
          wai_you_ledger_count = 1
        }

        var wai_zhong_ledger_lv = 0.00
        if (wai_zhong_have_ledger_total == 0) {
          wai_zhong_ledger_lv
        } else {
          wai_zhong_ledger_lv = (wai_zhong_have_ledger_total.toDouble / wai_zhong_have_ledger_total.toDouble) * 100
        }

        var wai_zhong_ledger_count = 0.00
        if (wai_zhong_have_ledger_total == 0) {
          wai_zhong_ledger_count
        } else {
          wai_zhong_ledger_count = 1
        }

        var wai_qi_ledger_lv = 0.00
        if (wai_qi_should_ledger_total == 0) {
          wai_qi_ledger_lv
        } else {
          wai_qi_ledger_lv = (wai_qi_have_ledger_total.toDouble / wai_qi_should_ledger_total.toDouble) * 100
        }

        var wai_qi_ledger_count = 0
        if (wai_qi_should_ledger_total == 0) {
          wai_qi_ledger_count
        } else {
          wai_qi_ledger_count = 1
        }

        var wai_license_warn_lv = 0.00
        if (wai_license_data_total == 0) {
          wai_license_warn_lv
        } else {
          wai_license_warn_lv = (wai_deal_license_data.toDouble / wai_license_data_total.toDouble) * 100
        }

        var wai_license_warn_count = 0
        if (wai_license_data_total == 0) {
          wai_license_warn_count
        } else {
          wai_license_warn_count = 1
        }

        (qu, wai_you_platoon_lv, wai_zhong_platoon_lv, wai_qi_platoon_lv, wai_platoon_lv, wai_you_ledger_lv, wai_zhong_ledger_lv, wai_qi_ledger_lv, wai_ledger_lv, wai_you_platoon_count, wai_zhong_platoon_count, wai_qi_platoon_count, wai_you_ledger_count, wai_zhong_ledger_count, wai_qi_ledger_count,wai_license_warn_lv,wai_license_warn_count)
      })

    val wai_total_lv = wai_totals.collect()

    for (i <- 0 until wai_total_lv.length) {
      val row6 = data._1.createRow(i + 4 + 1 + total_lv.length)
      row6.setHeight(400)
      val cell38 = row6.createCell(0)
      cell38.setCellValue((i + 1 + 1 + total_lv.length).toString)
      cell38.setCellStyle(style3)
      val cell39 = row6.createCell(1)
      cell39.setCellValue(wai_total_lv(i)._1)
      cell39.setCellStyle(style3)
      val cell40 = row6.createCell(2)
      cell40.setCellValue(wai_total_lv(i)._2.formatted("%.2f") + "%")
      cell40.setCellStyle(style3)
      val cell41 = row6.createCell(3)
      cell41.setCellValue(wai_total_lv(i)._3.formatted("%.2f") + "%")
      cell41.setCellStyle(style3)
      val cell42 = row6.createCell(4)
      cell42.setCellValue(wai_total_lv(i)._4.formatted("%.2f") + "%")
      cell42.setCellStyle(style3)
      val cell43 = row6.createCell(5)
      cell43.setCellValue(wai_total_lv(i)._5.formatted("%.2f") + "%")
      cell43.setCellStyle(style3)
      val cell44 = row6.createCell(6)
      cell44.setCellValue(wai_total_lv(i)._6.formatted("%.2f") + "%")
      cell44.setCellStyle(style3)
      val cell45 = row6.createCell(7)
      cell45.setCellValue(wai_total_lv(i)._7.formatted("%.2f") + "%")
      cell45.setCellStyle(style3)
      val cell46 = row6.createCell(8)
      cell46.setCellValue(wai_total_lv(i)._8.formatted("%.2f") + "%")
      cell46.setCellStyle(style3)
      val cell47 = row6.createCell(9)
      cell47.setCellValue(wai_total_lv(i)._9.formatted("%.2f") + "%")
      cell47.setCellStyle(style3)
      val cell48 = row6.createCell(10)
      cell48.setCellValue(wai_total_lv(i)._16.formatted("%.2f") + "%")
      cell48.setCellStyle(style3)

    }

    //市属中职校的 排菜，配送
    val zhi_should_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).map(x => (x._1, x._8)).reduceByKey(_ + _)
    val zhi_have_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).map(x => (x._1, x._9)).reduceByKey(_ + _)
    val zhi_you_should_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_you_have_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_zhong_should_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_zhong_have_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_qi_should_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_qi_have_platoon_total = data._2.filter(x => "市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val zhi_should_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).map(x => (x._1, x._8)).reduceByKey(_ + _)
    val zhi_have_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).map(x => (x._1, x._9)).reduceByKey(_ + _)
    val zhi_you_should_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_you_have_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_zhong_should_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_zhong_have_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_qi_should_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhi_qi_have_ledger_total = data._4.filter(x => "市属中职校".equals(x._1)).filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val zhi_deal_license_data = data._7.filter(x => "市属中职校".equals(x._1)).filter(x => "逾期".equals(x._16)).filter(x => x._11 == 4).map(x => (x._1, 1)).reduceByKey(_ + _)
    val zhi_license_data_total = data._7.filter(x => "市属中职校".equals(x._1)).filter(x => "逾期".equals(x._16)).map(x => (x._1, 1)).reduceByKey(_ + _)

    val zhi_totals = zhi_should_platoon_total.leftOuterJoin(zhi_have_platoon_total).leftOuterJoin(zhi_you_should_platoon_total).leftOuterJoin(zhi_you_have_platoon_total).leftOuterJoin(zhi_zhong_should_platoon_total).leftOuterJoin(zhi_zhong_have_platoon_total).leftOuterJoin(zhi_qi_should_platoon_total).leftOuterJoin(zhi_qi_have_platoon_total).leftOuterJoin(zhi_should_ledger_total).leftOuterJoin(zhi_have_ledger_total).leftOuterJoin(zhi_you_should_ledger_total).leftOuterJoin(zhi_you_have_ledger_total).leftOuterJoin(zhi_zhong_should_ledger_total).leftOuterJoin(zhi_zhong_have_ledger_total).leftOuterJoin(zhi_qi_should_ledger_total).leftOuterJoin(zhi_qi_have_ledger_total).leftOuterJoin(zhi_deal_license_data).leftOuterJoin(zhi_license_data_total)
      .map(x => {
        val qu = x._1

        val zhong_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1
        val zhong_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val zhong_you_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_you_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_zhong_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_zhong_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_qi_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_qi_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2

        val zhong_should_ledger_total = x._2._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val zhong_have_ledger_total = x._2._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val zhong_you_should_ledger_total = x._2._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_you_have_ledger_total = x._2._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_zhong_should_ledger_total = x._2._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_zhong_have_ledger_total = x._2._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_qi_should_ledger_total = x._2._1._1._1._2.getOrElse("null", 0)._2
        val zhong_qi_have_ledger_total = x._2._1._1._2.getOrElse("null", 0)._2

        val zhong_deal_license_data = x._2._1._2.getOrElse(0)
        val zhong_license_data_total = x._2._2.getOrElse(0)

        var zhong_platoon_lv = 0.00
        if (zhong_should_platoon_total == 0) {
          zhong_platoon_lv
        } else {
          zhong_platoon_lv = (zhong_have_platoon_total.toDouble / zhong_should_platoon_total.toDouble) * 100
        }

        var zhong_you_platoon_lv = 0.00
        if (zhong_you_should_platoon_total == 0) {
          zhong_you_platoon_lv
        } else {
          zhong_you_platoon_lv = (zhong_you_have_platoon_total.toDouble / zhong_you_should_platoon_total.toDouble) * 100
        }

        var zhong_you_platoon_count = 0
        if (zhong_you_should_platoon_total == 0) {
          zhong_you_platoon_count
        } else {
          zhong_you_platoon_count = 1
        }

        var zhong_zhong_platoon_lv = 0.00
        if (zhong_zhong_should_platoon_total == 0) {
          zhong_zhong_platoon_lv
        } else {
          zhong_zhong_platoon_lv = (zhong_zhong_have_platoon_total.toDouble / zhong_zhong_should_platoon_total.toDouble) * 100
        }

        var zhong_zhong_platoon_count = 0
        if (zhong_zhong_should_platoon_total == 0) {
          zhong_zhong_platoon_count
        } else {
          zhong_zhong_platoon_count = 1
        }

        var zhong_qi_platoon_lv = 0.00
        if (zhong_qi_should_platoon_total == 0) {
          zhong_qi_platoon_lv
        } else {
          zhong_qi_platoon_lv = (zhong_qi_have_platoon_total.toDouble / zhong_qi_should_platoon_total.toDouble) * 100
        }

        var zhong_qi_platoon_count = 0.00
        if (zhong_qi_should_platoon_total == 0) {
          zhong_qi_platoon_count
        } else {
          zhong_qi_platoon_count = 1
        }

        var zhong_ledger_lv = 0.00
        if (zhong_should_ledger_total == 0) {
          zhong_ledger_lv
        } else {
          zhong_ledger_lv = (zhong_have_ledger_total.toDouble / zhong_should_ledger_total.toDouble) * 100
        }

        var zhong_you_ledger_lv = 0.00
        if (zhong_you_should_ledger_total == 0) {
          zhong_you_ledger_lv
        } else {
          zhong_you_ledger_lv = (zhong_you_have_ledger_total.toDouble / zhong_you_should_ledger_total.toDouble) * 100
        }

        var zhong_you_ledger_count = 0
        if (zhong_you_should_ledger_total == 0) {
          zhong_you_ledger_count
        } else {
          zhong_you_ledger_count = 1
        }

        var zhong_zhong_ledger_lv = 0.00
        if (zhong_zhong_should_ledger_total == 0) {
          zhong_zhong_ledger_lv
        } else {
          zhong_zhong_ledger_lv = (zhong_zhong_have_ledger_total.toDouble / zhong_zhong_should_ledger_total.toDouble) * 100
        }

        var zhong_zhong_ledger_count = 0
        if (zhong_zhong_should_ledger_total == 0) {
          zhong_zhong_ledger_count
        } else {
          zhong_zhong_ledger_count = 1
        }

        var zhong_qi_ledger_lv = 0.00
        if (zhong_qi_should_ledger_total == 0) {
          zhong_qi_ledger_lv
        } else {
          zhong_qi_ledger_lv = (zhong_qi_have_ledger_total.toDouble / zhong_qi_should_ledger_total.toDouble) * 100
        }

        var zhong_qi_ledger_count = 0
        if (zhong_qi_should_ledger_total == 0) {
          zhong_qi_ledger_count
        } else {
          zhong_qi_ledger_count = 1
        }

        var zhong_license_warn_lv = 0.00
        if (zhong_license_data_total == 0) {
          zhong_license_warn_lv
        } else {
          zhong_license_warn_lv = (zhong_deal_license_data.toDouble / zhong_license_data_total.toDouble) * 100
        }

        var zhong_license_warn_count = 0
        if (zhong_license_data_total == 0) {
          zhong_license_warn_count
        } else {
          zhong_license_warn_count = 1
        }

        (qu, zhong_you_platoon_lv, zhong_zhong_platoon_lv, zhong_qi_platoon_lv, zhong_platoon_lv, zhong_you_ledger_lv, zhong_zhong_ledger_lv, zhong_qi_ledger_lv, zhong_ledger_lv, zhong_you_platoon_count, zhong_zhong_platoon_count, zhong_qi_platoon_count, zhong_you_ledger_count, zhong_zhong_ledger_count, zhong_qi_ledger_count,zhong_license_warn_lv,zhong_license_warn_count)
      })

    val zhi_total_lv = zhi_totals.collect()

    for (i <- 0 until zhi_total_lv.length) {
      val row7 = data._1.createRow(i + 4 + 1 + total_lv.length + wai_total_lv.length)
      row7.setHeight(400)
      val cell49 = row7.createCell(0)
      cell49.setCellValue((i + 1 + 1 + total_lv.length + wai_total_lv.length).toString)
      cell49.setCellStyle(style3)
      val cell50 = row7.createCell(1)
      cell50.setCellValue(zhi_total_lv(i)._1)
      cell50.setCellStyle(style3)
      val cell51 = row7.createCell(2)
      cell51.setCellValue(zhi_total_lv(i)._2.formatted("%.2f") + "%")
      cell51.setCellStyle(style3)
      val cell52 = row7.createCell(3)
      cell52.setCellValue(zhi_total_lv(i)._3.formatted("%.2f") + "%")
      cell52.setCellStyle(style3)
      val cell53 = row7.createCell(4)
      cell53.setCellValue(zhi_total_lv(i)._4.formatted("%.2f") + "%")
      cell53.setCellStyle(style3)
      val cell54 = row7.createCell(5)
      cell54.setCellValue(zhi_total_lv(i)._5.formatted("%.2f") + "%")
      cell54.setCellStyle(style3)
      val cell55 = row7.createCell(6)
      cell55.setCellValue(zhi_total_lv(i)._6.formatted("%.2f") + "%")
      cell55.setCellStyle(style3)
      val cell56 = row7.createCell(7)
      cell56.setCellValue(zhi_total_lv(i)._7.formatted("%.2f") + "%")
      cell56.setCellStyle(style3)
      val cell57 = row7.createCell(8)
      cell57.setCellValue(zhi_total_lv(i)._8.formatted("%.2f") + "%")
      cell57.setCellStyle(style3)
      val cell58 = row7.createCell(9)
      cell58.setCellValue(zhi_total_lv(i)._9.formatted("%.2f") + "%")
      cell58.setCellStyle(style3)
      val cell59 = row7.createCell(10)
      cell59.setCellValue(zhi_total_lv(i)._16.formatted("%.2f") + "%")
      cell59.setCellStyle(style3)

    }

    //合计
    val row8 = data._1.createRow(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 5)
    row8.setHeight(400)
    val cell60 = row8.createCell(0)
    cell60.setCellValue((1 + 1 + total_lv.length + wai_total_lv.length + zhi_total_lv.length).toString)
    cell60.setCellStyle(style3)

    val cell61 = row8.createCell(1)
    cell61.setCellValue("合计")
    cell61.setCellStyle(style3)

    val cell62 = row8.createCell(2)
    cell62.setCellValue(((totals.map(_._2).sum() + wai_totals.map(_._2).sum() + zhi_totals.map(_._2).sum()) / (totals.map(_._10).sum() + wai_totals.map(_._10).sum() + zhi_totals.map(_._10).sum())).formatted("%.2f") + "%")
    cell62.setCellStyle(style3)

    val cell63 = row8.createCell(3)
    cell63.setCellValue(((totals.map(_._3).sum() + wai_totals.map(_._3).sum() + zhi_totals.map(_._3).sum()) / (totals.map(_._11).sum() + wai_totals.map(_._11).sum() + zhi_totals.map(_._11).sum())).formatted("%.2f") + "%")
    cell63.setCellStyle(style3)

    val cell64 = row8.createCell(4)
    cell64.setCellValue(((totals.map(_._4).sum() + wai_totals.map(_._4).sum() + zhi_totals.map(_._4).sum()) / (totals.map(_._12).sum() + wai_totals.map(_._12).sum() + zhi_totals.map(_._12).sum())).formatted("%.2f") + "%")
    cell64.setCellStyle(style3)

    val cell65 = row8.createCell(5)
    cell65.setCellValue(((totals.map(_._5).sum() + wai_totals.map(_._5).sum() + zhi_totals.map(_._5).sum()) / (total_lv.length + wai_total_lv.length + zhi_total_lv.length)).formatted("%.2f") + "%")
    cell65.setCellStyle(style3)

    val cell66 = row8.createCell(6)
    cell66.setCellValue(((totals.map(_._6).sum() + wai_totals.map(_._6).sum() + zhi_totals.map(_._6).sum()) / (totals.map(_._13).sum() + wai_totals.map(_._13).sum() + zhi_totals.map(_._13).sum())).formatted("%.2f") + "%")
    cell66.setCellStyle(style3)

    val cell67 = row8.createCell(7)
    cell67.setCellValue(((totals.map(_._7).sum() + wai_totals.map(_._7).sum() + zhi_totals.map(_._7).sum()) / (totals.map(_._14).sum() + wai_totals.map(_._14).sum() + zhi_totals.map(_._14).sum())).formatted("%.2f") + "%")
    cell67.setCellStyle(style3)

    val cell68 = row8.createCell(8)
    cell68.setCellValue(((totals.map(_._8).sum() + wai_totals.map(_._8).sum() + zhi_totals.map(_._8).sum()) / (totals.map(_._15).sum() + wai_totals.map(_._15).sum() + zhi_totals.map(_._15).sum())).formatted("%.2f") + "%")
    cell68.setCellStyle(style3)

    val cell69 = row8.createCell(9)
    cell69.setCellValue(((totals.map(_._9).sum() + wai_totals.map(_._9).sum() + zhi_totals.map(_._9).sum()) / (total_lv.length + wai_total_lv.length + zhi_total_lv.length)).formatted("%.2f") + "%")
    cell69.setCellStyle(style3)

    val cell70 = row8.createCell(10)
    cell70.setCellValue(((totals.map(_._16).sum() + wai_totals.map(_._16).sum() + zhi_totals.map(_._16).sum()) / (totals.map(_._17).sum() + wai_totals.map(_._17).sum() + zhi_totals.map(_._17).sum())).formatted("%.2f") + "%")
    cell70.setCellStyle(style3)

    val row9 = data._1.createRow(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 7)
    val cellRangeAddress6 = new CellRangeAddress(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 7, total_lv.length + wai_total_lv.length + zhi_total_lv.length + 7, 0, 11)
    data._1.addMergedRegion(cellRangeAddress6)
    val cell72 = row9.createCell(0)
    cell72.setCellValue("1.使用率起止日期：" + data._5 + "日 00:00 至 " + data._6 + "日 23:59:59")
    cell72.setCellStyle(style2)

    val row10 = data._1.createRow(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 8)
    val cellRangeAddress7 = new CellRangeAddress(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 8, total_lv.length + wai_total_lv.length + zhi_total_lv.length + 8, 0, 11)
    data._1.addMergedRegion(cellRangeAddress7)
    val cell73 = row10.createCell(0)
    cell73.setCellValue("2.验收率起止日期：" + data._5 + "日 00:00 至 " + data._6 + "日 23:59:59")
    cell73.setCellStyle(style2)

    val row11 = data._1.createRow(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 9)
    val cellRangeAddress8 = new CellRangeAddress(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 9, total_lv.length + wai_total_lv.length + zhi_total_lv.length + 9, 0, 11)
    data._1.addMergedRegion(cellRangeAddress8)
    val cell74 = row11.createCell(0)
    cell74.setCellValue("3.证照逾期处理率起止日期：" + data._5 + "日 00:00 至 " + data._6 + "日 23:59:59")
    cell74.setCellStyle(style2)

    val row12 = data._1.createRow(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 10)
    val cellRangeAddress9 = new CellRangeAddress(total_lv.length + wai_total_lv.length + zhi_total_lv.length + 10, total_lv.length + wai_total_lv.length + zhi_total_lv.length + 10, 0, 11)
    data._1.addMergedRegion(cellRangeAddress9)
    val cell75 = row12.createCell(0)
    cell75.setCellValue("3.数据获取时间：" + data._8 + "日 00:00 ")
    cell75.setCellStyle(style2)


  }

  override def arealicensewarnweek(data: (HSSFSheet, Array[(String, String, String, String, String, String, String, Integer, String, String, Int, String, Integer, String, String, String)])): Unit = {
    val row = data._1.createRow(0)
    val cell = row.createCell(0)
    cell.setCellValue("序号")
    val cell1 = row.createCell(1)
    cell1.setCellValue("管理部门")
    val cell2 = row.createCell(2)
    cell2.setCellValue("学校名称")
    val cell3 = row.createCell(3)
    cell3.setCellValue("学校地址")
    val cell4 = row.createCell(4)
    cell4.setCellValue("联系人")
    val cell5 = row.createCell(5)
    cell5.setCellValue("联系电话")
    val cell6 = row.createCell(6)
    cell6.setCellValue("学校分类")
    val cell7 = row.createCell(7)
    cell7.setCellValue("学校类型")
    val cell8 = row.createCell(8)
    cell8.setCellValue("触发预警单位")
    val cell9 = row.createCell(9)
    cell9.setCellValue("证件类型")
    val cell10 = row.createCell(10)
    cell10.setCellValue("证件主体")
    val cell11 = row.createCell(11)
    cell11.setCellValue("证件编号")
    val cell12 = row.createCell(12)
    cell12.setCellValue("证件情况")
    val cell13 = row.createCell(13)
    cell13.setCellValue("状态")
    val cell14 = row.createCell(14)
    cell14.setCellValue("失效日期")
    val cell15 = row.createCell(15)
    cell15.setCellValue("预警日期")

    for (i <- 0 until data._2.length) {
      val row = data._1.createRow(i + 1)
      val cell = row.createCell(0)
      cell.setCellValue((i + 1).toString)
      val cell1 = row.createCell(1)
      cell1.setCellValue(data._2(i)._1)
      val cell2 = row.createCell(2)
      cell2.setCellValue(data._2(i)._2)
      val cell3 = row.createCell(3)
      cell3.setCellValue(data._2(i)._3)
      val cell4 = row.createCell(4)
      cell4.setCellValue(data._2(i)._4)
      val cell5 = row.createCell(5)
      cell5.setCellValue(data._2(i)._5)
      val cell6 = row.createCell(6)
      cell6.setCellValue(data._2(i)._6)
      val cell7 = row.createCell(7)
      cell7.setCellValue(data._2(i)._7)
      val cell8 = row.createCell(8)
      cell8.setCellValue(data._2(i)._14)
      val cell9 = row.createCell(9)
      if (data._2(i)._8 == 0) {
        cell9.setCellValue("餐饮服务许可证")
      } else if (data._2(i)._8 == 1) {
        cell9.setCellValue("食品经营许可证")
      } else if (data._2(i)._8 == 4) {
        cell9.setCellValue("营业执照")
      } else if (data._2(i)._8 == 20) {
        cell9.setCellValue("健康证")
      } else if (data._2(i)._8 == 22) {
        cell9.setCellValue("A1证")
      } else if (data._2(i)._8 == 23) {
        cell9.setCellValue("B证")
      } else if (data._2(i)._8 == 24) {
        cell9.setCellValue("C证")
      } else if (data._2(i)._8 == 25) {
        cell9.setCellValue("A2证")
      } else {
        cell9.setCellValue("")
      }

      val cell10 = row.createCell(10)
      if (data._2(i)._8 == 20 || data._2(i)._8 == 22 || data._2(i)._8 == 23 || data._2(i)._8 == 24 || data._2(i)._8 == 25) {
        cell10.setCellValue(data._2(i)._15)
      } else if (data._2(i)._13 == 1) {
        cell10.setCellValue(data._2(i)._2)
      } else {
        cell10.setCellValue(data._2(i)._14)
      }

      val cell11 = row.createCell(11)
      cell11.setCellValue(data._2(i)._9)

      val cell12 = row.createCell(12)
      cell12.setCellValue(data._2(i)._16)

      val cell13 = row.createCell(13)
      if (data._2(i)._11 == 1) {
        cell13.setCellValue("待处理")
      } else if (data._2(i)._11 == 2) {
        cell13.setCellValue("处理中")
      } else if (data._2(i)._11 == 3) {
        cell13.setCellValue("驳回")
      } else if (data._2(i)._11 == 4) {
        cell13.setCellValue("消除")
      } else {
        cell13.setCellValue("")
      }

      val cell14 = row.createCell(14)
      cell14.setCellValue((data._2(i)._10))

      val cell15 = row.createCell(15)
      cell15.setCellValue(data._2(i)._12)

    }
  }

  override def areatotal(data: (HSSFSheet, RDD[(String, String, String, String, String, String, String, Int, Int, Int)], HSSFWorkbook, RDD[(String, String, String, String, String, String, String, Int, Int, Int)], String, String, RDD[(String, String, String, String, String, String, String, Integer, String, String, Int, String, Integer, String, String, String)], String,String)): Unit = {
    data._1.setColumnWidth(0, 2500)
    data._1.setColumnWidth(1, 5000)
    data._1.setColumnWidth(2, 3500)
    data._1.setColumnWidth(3, 3500)
    data._1.setColumnWidth(4, 3500)
    data._1.setColumnWidth(5, 3500)
    data._1.setColumnWidth(6, 3500)
    data._1.setColumnWidth(7, 3500)
    data._1.setColumnWidth(8, 3500)
    data._1.setColumnWidth(9, 3500)
    data._1.setColumnWidth(10, 5800)

    val style = data._3.createCellStyle()
    val font = data._3.createFont()
    font.setFontHeightInPoints(12)
    font.setBold(true)
    style.setAlignment(HorizontalAlignment.CENTER)
    style.setBorderBottom(BorderStyle.THIN)
    style.setBorderLeft(BorderStyle.THIN)
    style.setBorderRight(BorderStyle.THIN)
    style.setBorderTop(BorderStyle.THIN)
    style.setFont(font)

    val style1 = data._3.createCellStyle()
    val font1 = data._3.createFont()
    font1.setFontHeightInPoints(22)
    font1.setBold(true)
    style1.setAlignment(HorizontalAlignment.CENTER)
    style1.setFont(font1)

    val style2 = data._3.createCellStyle()
    val font2 = data._3.createFont()
    font2.setFontHeightInPoints(12)
    font2.setBold(true)
    style2.setAlignment(HorizontalAlignment.LEFT)
    style2.setFont(font2)

    val style3 = data._3.createCellStyle()
    val font3 = data._3.createFont()
    font3.setFontHeightInPoints(13)
    font3.setBold(false)
    style3.setAlignment(HorizontalAlignment.CENTER)
    style3.setBorderBottom(BorderStyle.THIN)
    style3.setBorderLeft(BorderStyle.THIN)
    style3.setBorderRight(BorderStyle.THIN)
    style3.setBorderTop(BorderStyle.THIN)
    style3.setFont(font3)

    val style4 = data._3.createCellStyle()
    val font4 = data._3.createFont()
    font4.setFontHeightInPoints(12)
    font4.setBold(true)
    font4.setFontName("宋体")
    style4.setAlignment(HorizontalAlignment.CENTER)
    style4.setFont(font4)

    val style5 = data._3.createCellStyle()
    val font5 = data._3.createFont()
    font5.setFontHeightInPoints(16)
    font5.setBold(true)
    font5.setFontName("宋体")
    style5.setAlignment(HorizontalAlignment.CENTER)
    style5.setBorderBottom(BorderStyle.THIN)
    style5.setBorderLeft(BorderStyle.THIN)
    style5.setBorderRight(BorderStyle.THIN)
    style5.setBorderTop(BorderStyle.THIN)
    style5.setFont(font5)

    val row = data._1.createRow(0)
    val cellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
    data._1.addMergedRegion(cellRangeAddress)
    val cell = row.createCell(0)
    cell.setCellValue(data._9+"年食安管理平台使用、验收及证照逾期处理情况")
    cell.setCellStyle(style1)

    val row1 = data._1.createRow(1)
    val cellRangeAddress1 = new CellRangeAddress(1, 1, 0, 11)
    data._1.addMergedRegion(cellRangeAddress1)
    val cell1 = row1.createCell(0)
    cell1.setCellValue("表格内请注明学校总数、托幼数、中小学数，学校使用、验收总数、托幼、中小学使用和验收数，证照逾期总数和已处理数。")
    cell1.setCellStyle(style4)

    val row2 = data._1.createRow(2)
    row2.setHeight(500)
    val cellRangeAddress2 = new CellRangeAddress(2, 3, 0, 0)
    data._1.addMergedRegion(cellRangeAddress2)
    val cell2 = row2.createCell(0)
    cell2.setCellValue("序号")
    cell2.setCellStyle(style5)

    val cellRangeAddress3 = new CellRangeAddress(2, 3, 1, 1)
    data._1.addMergedRegion(cellRangeAddress3)
    val cell3 = row2.createCell(1)
    cell3.setCellValue("所在区")
    cell3.setCellStyle(style5)

    val cellRangeAddress4 = new CellRangeAddress(2, 2, 2, 5)
    data._1.addMergedRegion(cellRangeAddress4)
    RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress4, data._1)
    RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress4, data._1)
    RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress4, data._1)
    RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress4, data._1)
    val cell4 = row2.createCell(2)
    cell4.setCellValue("使用率")
    cell4.setCellStyle(style5)

    val cellRangeAddress5 = new CellRangeAddress(2, 2, 6, 9)
    data._1.addMergedRegion(cellRangeAddress5)
    RegionUtil.setBorderTop(BorderStyle.THIN, cellRangeAddress5, data._1)
    RegionUtil.setBorderLeft(BorderStyle.THIN, cellRangeAddress5, data._1)
    RegionUtil.setBorderRight(BorderStyle.THIN, cellRangeAddress5, data._1)
    RegionUtil.setBorderBottom(BorderStyle.THIN, cellRangeAddress5, data._1)
    val cell5 = row2.createCell(6)
    cell5.setCellValue("验收率")
    cell5.setCellStyle(style5)

    val cell6 = row2.createCell(10)
    cell6.setCellValue("证照逾期处理率")
    cell6.setCellStyle(style5)

    val row3 = data._1.createRow(3)
    val cell7 = row3.createCell(2)
    cell7.setCellValue("托幼")
    cell7.setCellStyle(style)
    val cell8 = row3.createCell(3)
    cell8.setCellValue("中小学")
    cell8.setCellStyle(style)
    val cell9 = row3.createCell(4)
    cell9.setCellValue("其他")
    cell9.setCellStyle(style)
    val cell10 = row3.createCell(5)
    cell10.setCellValue("总")
    cell10.setCellStyle(style)
    val cell11 = row3.createCell(6)
    cell11.setCellValue("托幼")
    cell11.setCellStyle(style)
    val cell12 = row3.createCell(7)
    cell12.setCellValue("中小学")
    cell12.setCellStyle(style)
    val cell13 = row3.createCell(8)
    cell13.setCellValue("其他")
    cell13.setCellStyle(style)
    val cell14 = row3.createCell(9)
    cell14.setCellValue("总")
    cell14.setCellStyle(style)
    val cell15 = row3.createCell(10)
    cell15.setCellValue("总")
    cell15.setCellStyle(style)

    val should_platoon_total = data._2.map(x => (x._1, x._8)).reduceByKey(_ + _)
    val have_platoon_total = data._2.map(x => (x._1, x._9)).reduceByKey(_ + _)
    val you_should_platoon_total = data._2.filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val you_have_platoon_total = data._2.filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_should_platoon_total = data._2.filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_have_platoon_total = data._2.filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_should_platoon_total = data._2.filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_have_platoon_total = data._2.filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val should_ledger_total = data._4.map(x => (x._1, x._8)).reduceByKey(_ + _)
    val have_ledger_total = data._4.map(x => (x._1, x._9)).reduceByKey(_ + _)
    val you_should_ledger_total = data._4.filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val you_have_ledger_total = data._4.filter(x => "幼托".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_should_ledger_total = data._4.filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val zhong_have_ledger_total = data._4.filter(x => "中小学".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_should_ledger_total = data._4.filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._8)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))
    val qi_have_ledger_total = data._4.filter(x => "其他".equals(x._6)).map(x => ((x._1, x._6), x._9)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    val deal_license_data = data._7.filter(x => "逾期".equals(x._16)).filter(x => x._11 == 4).map(x => (x._1, 1)).reduceByKey(_ + _)
    val license_data_total = data._7.filter(x => "逾期".equals(x._16)).map(x => (x._1, 1)).reduceByKey(_ + _)


    val totals = should_platoon_total.leftOuterJoin(have_platoon_total).leftOuterJoin(you_should_platoon_total).leftOuterJoin(you_have_platoon_total).leftOuterJoin(zhong_should_platoon_total).leftOuterJoin(zhong_have_platoon_total).leftOuterJoin(qi_should_platoon_total).leftOuterJoin(qi_have_platoon_total).leftOuterJoin(should_ledger_total).leftOuterJoin(have_ledger_total).leftOuterJoin(you_should_ledger_total).leftOuterJoin(you_have_ledger_total).leftOuterJoin(zhong_should_ledger_total).leftOuterJoin(zhong_have_ledger_total).leftOuterJoin(qi_should_ledger_total).leftOuterJoin(qi_have_ledger_total).leftOuterJoin(deal_license_data).leftOuterJoin(license_data_total)
      .map(x => {
        val qu = x._1

        val should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1
        val have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val you_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val you_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val qi_should_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val qi_have_platoon_total = x._2._1._1._1._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2

        val should_ledger_total = x._2._1._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val have_ledger_total = x._2._1._1._1._1._1._1._1._1._2.getOrElse(0)
        val you_should_ledger_total = x._2._1._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val you_have_ledger_total = x._2._1._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_should_ledger_total = x._2._1._1._1._1._1._2.getOrElse("null", 0)._2
        val zhong_have_ledger_total = x._2._1._1._1._1._2.getOrElse("null", 0)._2
        val qi_should_ledger_total = x._2._1._1._1._2.getOrElse("null", 0)._2
        val qi_have_ledger_total = x._2._1._1._2.getOrElse("null", 0)._2

        val deal_license_data = x._2._1._2.getOrElse(0)
        val license_data_total = x._2._2.getOrElse(0)

        var platoon_lv = 0.00
        if (should_platoon_total == 0) {
          platoon_lv
        } else {
          platoon_lv = (have_platoon_total.toDouble / should_platoon_total.toDouble) * 100
        }

        var you_platoon_lv = 0.00
        if (you_should_platoon_total == 0) {
          you_platoon_lv
        } else {
          you_platoon_lv = (you_have_platoon_total.toDouble / you_should_platoon_total.toDouble) * 100
        }

        var you_platoon_count = 0
        if (you_should_platoon_total == 0) {
          you_platoon_count
        } else {
          you_platoon_count = 1
        }

        var zhong_platoon_lv = 0.00
        if (zhong_should_platoon_total == 0) {
          zhong_platoon_lv
        } else {
          zhong_platoon_lv = (zhong_have_platoon_total.toDouble / zhong_should_platoon_total.toDouble) * 100
        }

        var zhong_platoon_count = 0
        if (zhong_should_platoon_total == 0) {
          zhong_platoon_count
        } else {
          zhong_platoon_count = 1
        }

        var qi_platoon_lv = 0.00
        if (qi_should_platoon_total == 0) {
          qi_platoon_lv
        } else {
          qi_platoon_lv = (qi_have_platoon_total.toDouble / qi_should_platoon_total.toDouble) * 100
        }

        var qi_platoon_count = 0
        if (qi_should_platoon_total == 0) {
          qi_platoon_count
        } else {
          qi_platoon_count = 1
        }

        var ledger_lv = 0.00
        if (should_ledger_total == 0) {
          ledger_lv
        } else {
          ledger_lv = (have_ledger_total.toDouble / should_ledger_total.toDouble) * 100
        }

        var you_ledger_lv = 0.00
        if (you_should_ledger_total == 0) {
          you_ledger_lv
        } else {
          you_ledger_lv = (you_have_ledger_total.toDouble / you_should_ledger_total.toDouble) * 100
        }

        var you_ledger_count = 0
        if (you_should_ledger_total == 0) {
          you_ledger_count
        } else {
          you_ledger_count = 1
        }

        var zhong_ledger_lv = 0.00
        if (zhong_should_ledger_total == 0) {
          zhong_ledger_lv
        } else {
          zhong_ledger_lv = (zhong_have_ledger_total.toDouble / zhong_should_ledger_total.toDouble) * 100
        }

        var zhong_ledger_count = 0
        if (zhong_should_ledger_total == 0) {
          zhong_ledger_count
        } else {
          zhong_ledger_count = 1
        }

        var qi_ledger_lv = 0.00
        if (qi_should_ledger_total == 0) {
          qi_ledger_lv
        } else {
          qi_ledger_lv = (qi_have_ledger_total.toDouble / qi_should_ledger_total.toDouble) * 100
        }

        var qi_ledger_count = 0
        if (qi_should_ledger_total == 0) {
          qi_ledger_count
        } else {
          qi_ledger_count = 1
        }

        var license_warn_lv = 0.00
        if (license_data_total == 0) {
          license_warn_lv
        } else {
          license_warn_lv = (deal_license_data.toDouble / license_data_total.toDouble) * 100
        }

        var license_warn_count = 0
        if (license_data_total == 0) {
          license_warn_count
        } else {
          license_warn_count = 1
        }

        (qu, you_platoon_lv, zhong_platoon_lv, qi_platoon_lv, platoon_lv, you_ledger_lv, zhong_ledger_lv, qi_ledger_lv, ledger_lv, you_platoon_count, zhong_platoon_count, qi_platoon_count, you_ledger_count, zhong_ledger_count, qi_ledger_count, license_warn_lv, license_warn_count)
      }).sortBy(x => x._5, false)

    val total_lv = totals.collect()

    for (i <- 0 until total_lv.length) {
      val row4 = data._1.createRow(i + 4)
      row4.setHeight(400)
      val cell16 = row4.createCell(0)
      cell16.setCellValue((i + 1).toString)
      cell16.setCellStyle(style3)
      val cell17 = row4.createCell(1)
      cell17.setCellValue(total_lv(i)._1)
      cell17.setCellStyle(style3)
      val cell18 = row4.createCell(2)
      cell18.setCellValue(total_lv(i)._2.formatted("%.2f") + "%")
      cell18.setCellStyle(style3)
      val cell19 = row4.createCell(3)
      cell19.setCellValue(total_lv(i)._3.formatted("%.2f") + "%")
      cell19.setCellStyle(style3)
      val cell20 = row4.createCell(4)
      cell20.setCellValue(total_lv(i)._4.formatted("%.2f") + "%")
      cell20.setCellStyle(style3)
      val cell21 = row4.createCell(5)
      cell21.setCellValue(total_lv(i)._5.formatted("%.2f") + "%")
      cell21.setCellStyle(style3)
      val cell22 = row4.createCell(6)
      cell22.setCellValue(total_lv(i)._6.formatted("%.2f") + "%")
      cell22.setCellStyle(style3)
      val cell23 = row4.createCell(7)
      cell23.setCellValue(total_lv(i)._7.formatted("%.2f") + "%")
      cell23.setCellStyle(style3)
      val cell24 = row4.createCell(8)
      cell24.setCellValue(total_lv(i)._8.formatted("%.2f") + "%")
      cell24.setCellStyle(style3)
      val cell25 = row4.createCell(9)
      cell25.setCellValue(total_lv(i)._9.formatted("%.2f") + "%")
      cell25.setCellStyle(style3)
      val cell26 = row4.createCell(10)
      cell26.setCellValue(total_lv(i)._16.formatted("%.2f") + "%")
      cell26.setCellStyle(style3)

    }

    val row5 = data._1.createRow(total_lv.length + 5)
    val cellRangeAddress6 = new CellRangeAddress(total_lv.length + 5, total_lv.length + 5, 0, 11)
    data._1.addMergedRegion(cellRangeAddress6)
    val cell27 = row5.createCell(0)
    cell27.setCellValue("1.使用率起止日期：" + data._5 + "日 00:00 至 " + data._6 + "日 23:59:59")
    cell27.setCellStyle(style2)

    val row6 = data._1.createRow(total_lv.length + 6)
    val cellRangeAddress7 = new CellRangeAddress(total_lv.length + 6, total_lv.length + 6, 0, 11)
    data._1.addMergedRegion(cellRangeAddress7)
    val cell28 = row6.createCell(0)
    cell28.setCellValue("2.验收率起止日期：" + data._5 + "日 00:00 至 " + data._6 + "日 23:59:59")
    cell28.setCellStyle(style2)

    val row11 = data._1.createRow(total_lv.length + 7)
    val cellRangeAddress8 = new CellRangeAddress(total_lv.length + 7, total_lv.length + 7, 0, 11)
    data._1.addMergedRegion(cellRangeAddress8)
    val cell29 = row11.createCell(0)
    cell29.setCellValue("3.证照逾期处理率起止日期：" + data._5 + "日 00:00 至 " + data._6 + "日 23:59:59")
    cell29.setCellStyle(style2)

    val row12 = data._1.createRow(total_lv.length + 8)
    val cellRangeAddress9 = new CellRangeAddress(total_lv.length + 8,total_lv.length + 8, 0, 11)
    data._1.addMergedRegion(cellRangeAddress9)
    val cell30 = row12.createCell(0)
    cell30.setCellValue("3.数据获取时间：" + data._8 + "日 00:00 ")
    cell30.setCellStyle(style2)

  }
}
