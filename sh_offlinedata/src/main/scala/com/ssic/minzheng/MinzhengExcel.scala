package com.ssic.minzheng

import java.io.{File, FileOutputStream}
import java.net.URI

import com.ssic.service.{ExceltitleStat, MinzhengExcelStat}
import com.ssic.utils.Tools.{conn, edu_bd_department, url}
import com.ssic.utils.{Rule, Tools}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment}
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

object MinzhengExcel {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("民政项目点的excel")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")
    val sc = new SparkContext(sparkConf)

    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val sparkSession = sqlContext.sparkSession

    //"yyyy-MM-dd"格式的昨天时间
    val date = Rule.timeToStamp("yyyy-MM-dd", -1) //format.format(time)
    //昨天时间的年
    val year = Rule.timeToStamp("yyyy", -1) //format1.format(time)
    //昨天时间的月
    val month = Rule.timeToStamp("M", -1) //format3.format(time)
    //"yyyyMMdd"格式的昨天时间
    val datetime = Rule.timeToStamp("yyyyMMdd", -1) //format4.format(time)

    //"yyyy-MM-dd"格式的今天时间
    val date2 = Rule.timeToStamp("yyyy-MM-dd", 0) //format.format(time2)
    //"yyyyMMdd"格式的今天时间
    val date3 = Rule.timeToStamp("yyyyMMdd", 0) //format4.format(time2)

    val bd_department = sparkSession.read.jdbc(url, edu_bd_department, conn)
    bd_department.createTempView("t_edu_bd_department")
    val departmentidToName: Broadcast[Map[String, String]] = sc.broadcast(Tools.departmentidToName(sparkSession))

    //排菜数据日统计分析报表
    val platoonTotalFilename = year + "年排菜数据日统计分析报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val platoonTotalFile = new File("/data/" + platoonTotalFilename)

    val platoonTotalSheetWidth = Array[Int](3000, 5500, 5500, 5500, 5500, 5500, 4500)
    val platoonTotalWorkbook = new HSSFWorkbook()
    val platoonTotalTitleStyle = new ExceltitleStat().style(platoonTotalWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val platoonTotalTitleValueStyle = new ExceltitleStat().style(platoonTotalWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val platoonTotalContentStyle = new ExceltitleStat().style(platoonTotalWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val platoonTotalSheet = platoonTotalWorkbook.createSheet("上海市排菜数据日统计分析报表")
    new ExceltitleStat().sheetname(platoonTotalSheet, platoonTotalSheetWidth)

    val platoonTotalCellRangeAddress = new CellRangeAddress(0, 0, 0, 6)
    platoonTotalSheet.addMergedRegion(platoonTotalCellRangeAddress)
    val platoonTotalTitle = Array[String]("排菜数据日统计分析报表" + "         " + "日期：" + date)
    val platoonTotalRow = platoonTotalSheet.createRow(0)
    platoonTotalRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(platoonTotalTitle, platoonTotalTitleStyle, platoonTotalRow)

    val platoonTotalTitleValue = Array[String]("序号", "主管部门", "食堂数量", "应排菜食堂", "已排菜食堂", "未排菜食堂", "排菜率")
    val platoonTotalRow1 = platoonTotalSheet.createRow(1)
    platoonTotalRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(platoonTotalTitleValue, platoonTotalTitleValueStyle, platoonTotalRow1)

    val platoonTotalContent = new MinzhengExcelStat().platoonTotal(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(platoonTotalContent, platoonTotalSheet, platoonTotalContentStyle, 2, 0)

    val platoonTotalStream = new FileOutputStream(platoonTotalFile)
    platoonTotalWorkbook.write(platoonTotalStream)
    platoonTotalStream.close()

    val fileSystem = FileSystem.get(new URI("hdfs://172.18.14.30:8020/"), new Configuration())
    fileSystem.moveFromLocalFile(new Path("/data/" + platoonTotalFilename), new Path("/minzheng_day_report/shanghai/" + platoonTotalFilename))

    //排菜数据日报表
    val platoonFilename = year + "年排菜数据日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val platoonFile = new File("/data/" + platoonFilename)

    val platoonSheetWidth = Array[Int](2500, 4500, 4500, 4500, 4500, 4500, 6500, 6500, 4500, 4500, 4500, 5000)
    val platoonWorkbook = new HSSFWorkbook()
    val platoonTitleStyle = new ExceltitleStat().style(platoonWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)
    val platoonTitleValueStyle = new ExceltitleStat().style(platoonWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val platoonContentStyle = new ExceltitleStat().style(platoonWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val platoonSheet = platoonWorkbook.createSheet("上海市排菜数据日报表")
    new ExceltitleStat().sheetname(platoonSheet, platoonSheetWidth)

    val platoonCellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
    platoonSheet.addMergedRegion(platoonCellRangeAddress)
    val platoonTitle = Array[String]("排菜数据日报表" + "         " + "日期：" + date)
    val platoonRow = platoonSheet.createRow(0)
    platoonRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(platoonTitle, platoonTitleStyle, platoonRow)

    val platoonTitleValue = Array[String]("序号", "用餐日期", "区", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "供餐模式", "是否供餐", "是否排菜", "排菜上报时间")
    val platoonRow1 = platoonSheet.createRow(1)
    platoonRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(platoonTitleValue, platoonTitleValueStyle, platoonRow1)

    val platoonContent = new MinzhengExcelStat().platoon(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(platoonContent, platoonSheet, platoonContentStyle, 2, 0)

    val platoonStream = new FileOutputStream(platoonFile)
    platoonWorkbook.write(platoonStream)
    platoonStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + platoonFilename), new Path("/minzheng_day_report/shanghai/" + platoonFilename))


    //排菜菜品明细表
    val dishmenuFilename = year + "年排菜明细数据日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val dishmenuFile = new File("/data/" + dishmenuFilename)

    val dishSheetWidth = Array[Int](2500, 4500, 4500, 4500, 4500, 4500, 6500, 5500, 6500, 4500, 5500, 5000, 4500)
    val dishmenuWorkbook = new HSSFWorkbook()
    val dishmenuTitleStyle = new ExceltitleStat().style(dishmenuWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val dishmenuTitleValueStyle = new ExceltitleStat().style(dishmenuWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val dishmenuContentStyle = new ExceltitleStat().style(dishmenuWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val dishmenuSheet = dishmenuWorkbook.createSheet("上海市排菜明细数据日报表")
    new ExceltitleStat().sheetname(dishmenuSheet, dishSheetWidth)

    val dishmenuCellRangeAddress = new CellRangeAddress(0, 0, 0, 12)
    dishmenuSheet.addMergedRegion(dishmenuCellRangeAddress)
    val dishmenuTitle = Array[String]("排菜明细数据日报表" + "         " + "日期：" + date)
    val dishmenuRow = dishmenuSheet.createRow(0)
    dishmenuRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(dishmenuTitle, dishmenuTitleStyle, dishmenuRow)

    val dishmenuTitleValue = Array[String]("序号", "用餐日期", "区", "主管单位", "类型", "创办主体", "单位名称", "供餐类型", "团餐公司", "餐别", "菜单名称", "菜品名称", "菜品份数")
    val dishmenuRow1 = dishmenuSheet.createRow(1)
    dishmenuRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(dishmenuTitleValue, dishmenuTitleValueStyle, dishmenuRow1)

    val dishmenuContent = new MinzhengExcelStat().dishmenu(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(dishmenuContent, dishmenuSheet, dishmenuContentStyle, 2, 0)

    val dishmenuStream = new FileOutputStream(dishmenuFile)
    dishmenuWorkbook.write(dishmenuStream)
    dishmenuStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + dishmenuFilename), new Path("/minzheng_day_report/shanghai/" + dishmenuFilename))

    //验收日统计报表
    val ledegeTotalFilename = year + "年验收数据日统计_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegeTotalFile = new File("/data/" + ledegeTotalFilename)

    val ledegeTotalSheetWidth = Array[Int](2500, 4500, 4500, 4500, 4500, 4500, 3500)
    val ledegeTotalWorkbook = new HSSFWorkbook()
    val ledegeTotalTitleStyle = new ExceltitleStat().style(ledegeTotalWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeTotalTitleValueStyle = new ExceltitleStat().style(ledegeTotalWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeTotalContentStyle = new ExceltitleStat().style(ledegeTotalWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val ledegeTotalSheet = ledegeTotalWorkbook.createSheet("上海市验收数据日统计")
    new ExceltitleStat().sheetname(ledegeTotalSheet, ledegeTotalSheetWidth)

    val ledegeTotalCellRangeAddress = new CellRangeAddress(0, 0, 0, 6)
    ledegeTotalSheet.addMergedRegion(ledegeTotalCellRangeAddress)
    val ledegeTotalTitle = Array[String]("验收数据日统计报表" + "         " + "日期：" + date)
    val ledegeTotalRow = ledegeTotalSheet.createRow(0)
    ledegeTotalRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(ledegeTotalTitle, ledegeTotalTitleStyle, ledegeTotalRow)

    val ledegeTotalTitleValue = Array[String]("序号", "主管单位", "食堂数量", "应验收食堂", "已验收食堂", "未验收食堂", "验收率")
    val ledegeTotalRow1 = ledegeTotalSheet.createRow(1)
    ledegeTotalRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(ledegeTotalTitleValue, ledegeTotalTitleValueStyle, ledegeTotalRow1)

    val ledegeTotalContent = new MinzhengExcelStat().ledegeTotal(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(ledegeTotalContent, ledegeTotalSheet, ledegeTotalContentStyle, 2, 0)

    val ledegeTotalStream = new FileOutputStream(ledegeTotalFile)
    ledegeTotalWorkbook.write(ledegeTotalStream)
    ledegeTotalStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + ledegeTotalFilename), new Path("/minzheng_day_report/shanghai/" + ledegeTotalFilename))

    //验收数据日报表
    val ledegeFilename = year + "年验收数据日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegeFile = new File("/data/" + ledegeFilename)

    val ledegeSheetWidth = Array[Int](2500, 4500, 4500, 4500, 4500, 4500, 6500, 6500, 4500, 4500, 4500, 5000)
    val ledegeWorkbook = new HSSFWorkbook()
    val ledegeTitleStyle = new ExceltitleStat().style(ledegeWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeTitleValueStyle = new ExceltitleStat().style(ledegeWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeContentStyle = new ExceltitleStat().style(ledegeWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeSheet = ledegeWorkbook.createSheet("上海市验收数据日报表")
    new ExceltitleStat().sheetname(ledegeSheet, ledegeSheetWidth)

    val ledegeCellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
    ledegeSheet.addMergedRegion(ledegeCellRangeAddress)
    val ledegeTitle = Array[String]("验收数据日报表" + "         " + "日期：" + date)
    val ledegeRow = ledegeSheet.createRow(0)
    ledegeRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(ledegeTitle, ledegeTitleStyle, ledegeRow)

    val ledegeTitleValue = Array[String]("序号", "验收日期", "区", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "供餐模式", "是否供餐", "是否验收", "验收上报时间")
    val ledegeRow1 = ledegeSheet.createRow(1)
    ledegeRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(ledegeTitleValue, ledegeTitleValueStyle, ledegeRow1)

    val ledegeContent = new MinzhengExcelStat().ledege(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(ledegeContent, ledegeSheet, ledegeContentStyle, 2, 0)

    val ledegeStream = new FileOutputStream(ledegeFile)
    ledegeWorkbook.write(ledegeStream)
    ledegeStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + ledegeFilename), new Path("/minzheng_day_report/shanghai/" + ledegeFilename))

    //原料明细
    val ledegeDetailFilename = year + "年原料明细日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegeDetailFile = new File("/data/" + ledegeDetailFilename)
    val ledegeDetailSheetWidth = Array[Int](2500, 3500, 4500, 3500, 3500, 3500, 6500, 4500, 6500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val ledegeDetailWorkbook = new HSSFWorkbook()
    val ledegeDetailTitleStyle = new ExceltitleStat().style(ledegeDetailWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeDetailTitleValueStyle = new ExceltitleStat().style(ledegeDetailWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegeDetailContentStyle = new ExceltitleStat().style(ledegeDetailWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val ledegeDetailSheet = ledegeDetailWorkbook.createSheet("上海市原料明细数据日报表")
    new ExceltitleStat().sheetname(ledegeDetailSheet, ledegeDetailSheetWidth)

    val ledegeDetailCellRangeAddress = new CellRangeAddress(0, 0, 0, 21)
    ledegeDetailSheet.addMergedRegion(ledegeDetailCellRangeAddress)
    val ledegeDetailTitle = Array[String]("原料明细数据日报表" + "         " + "日期：" + date)
    val ledegeDetailRow = ledegeDetailSheet.createRow(0)
    ledegeDetailRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(ledegeDetailTitle, ledegeDetailTitleStyle, ledegeDetailRow)

    val ledegeDetailTitleValue = Array[String]("序号", "用餐日期", "配送批次号", "区", "主管单位", "类型", "创办主体", "单位名称", "供餐类型", "团餐公司", "配送类型", "物料名称", "标准名称", "原料类别", "数量", "换算关系", "换算数量", "供应商", "是否验收", "验收数量", "配送单图片", "验收日期")
    val ledegeDetailRow1 = ledegeDetailSheet.createRow(1)
    ledegeDetailRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(ledegeDetailTitleValue, ledegeDetailTitleValueStyle, ledegeDetailRow1)

    val ledegeDetailContent = new MinzhengExcelStat().ledegeDetail(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(ledegeDetailContent, ledegeDetailSheet, ledegeDetailContentStyle, 2, 0)

    val ledegeDetailStream = new FileOutputStream(ledegeDetailFile)
    ledegeDetailWorkbook.write(ledegeDetailStream)
    ledegeDetailStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + ledegeDetailFilename), new Path("/minzheng_day_report/shanghai/" + ledegeDetailFilename))

    //留样日统计报表
    val reserveTotalFilename = year + "年留样统计日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val reserveTotalFile = new File("/data/" + reserveTotalFilename)
    val reserveTotalSheetWidth = Array[Int](2500, 4500, 4500, 4500, 4500, 4500, 3500)
    val reserveTotalWorkbook = new HSSFWorkbook()
    val reserveTotalTitleStyle = new ExceltitleStat().style(reserveTotalWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val reserveTotalTitleValueStyle = new ExceltitleStat().style(reserveTotalWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val reserveTotalContentStyle = new ExceltitleStat().style(reserveTotalWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val reserveTotalSheet = reserveTotalWorkbook.createSheet("上海市留样统计数据日报表")
    new ExceltitleStat().sheetname(reserveTotalSheet, reserveTotalSheetWidth)

    val reserveTotalCellRangeAddress = new CellRangeAddress(0, 0, 0, 6)
    reserveTotalSheet.addMergedRegion(reserveTotalCellRangeAddress)
    val reserveTotalTitle = Array[String]("留样统计数据日报表" + "         " + "日期：" + date)
    val reserveTotalRow = reserveTotalSheet.createRow(0)
    reserveTotalRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(reserveTotalTitle, reserveTotalTitleStyle, reserveTotalRow)

    val reserveTotalTitleValue = Array[String]("序号", "主管部门", "食堂数量", "应留样食堂", "已留样食堂", "未留样食堂", "留样率")
    val reserveTotalRow1 = reserveTotalSheet.createRow(1)
    reserveTotalRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(reserveTotalTitleValue, reserveTotalTitleValueStyle, reserveTotalRow1)

    val reserveTotalContent = new MinzhengExcelStat().reserveTotal(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(reserveTotalContent, reserveTotalSheet, reserveTotalContentStyle, 2, 0)

    val reserveTotalStream = new FileOutputStream(reserveTotalFile)
    reserveTotalWorkbook.write(reserveTotalStream)
    reserveTotalStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + reserveTotalFilename), new Path("/minzheng_day_report/shanghai/" + reserveTotalFilename))


    //留样日报表
    val reserveFilename = year + "年留样日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val reserveFile = new File("/data/" + reserveFilename)
    val reserveSheetWidth = Array[Int](2500, 3500, 4500, 3500, 3500, 3500, 6500, 4500, 6500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val reserveWorkbook = new HSSFWorkbook()
    val reserveTitleStyle = new ExceltitleStat().style(reserveWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val reserveTitleValueStyle = new ExceltitleStat().style(reserveWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val reserveContentStyle = new ExceltitleStat().style(reserveWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val reserveSheet = reserveWorkbook.createSheet("上海市留样数据日报表")
    new ExceltitleStat().sheetname(reserveSheet, reserveSheetWidth)

    val reserveCellRangeAddress = new CellRangeAddress(0, 0, 0, 18)
    reserveSheet.addMergedRegion(reserveCellRangeAddress)
    val reserveTitle = Array[String]("留样数据日报表" + "         " + "日期：" + date)
    val reserveRow = reserveTotalSheet.createRow(0)
    reserveRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(reserveTitle, reserveTitleStyle, reserveRow)

    val reserveTitleValue = Array[String]("序号", "就餐日期", "区", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "供餐模式", "餐别", "菜单名称", "菜品名称","菜品份数","是否留样","留样数量","留样时间","留样说明","留样人","留样操作时间")
    val reserveRow1 = reserveSheet.createRow(1)
    reserveRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(reserveTitleValue, reserveTitleValueStyle, reserveRow1)

    val reserveContent = new MinzhengExcelStat().reserve(hiveContext, year, month, date, departmentidToName)
    new ExceltitleStat().excelcontent(reserveContent, reserveSheet, reserveContentStyle, 2, 0)

    val reserveStream = new FileOutputStream(reserveFile)
    reserveWorkbook.write(reserveStream)
    reserveStream.close()

    fileSystem.moveFromLocalFile(new Path("/data/" + reserveFilename), new Path("/minzheng_day_report/shanghai/" + reserveFilename))

    sc.stop()
  }

}
