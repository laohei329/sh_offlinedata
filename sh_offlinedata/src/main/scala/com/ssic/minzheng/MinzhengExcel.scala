package com.ssic.minzheng

import java.io.{File, FileOutputStream}
import java.net.URI

import com.ssic.service.{ExceltitleStat, MinzhengExcelStat}
import com.ssic.utils.Tools.{conn, edu_bd_department, url}
import com.ssic.utils.{Rule, Tools}
import org.apache.commons.lang3.StringUtils
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment}
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**民政报表**/
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

    //连接hdfs
    val fileSystem = FileSystem.get(new URI("hdfs://172.18.14.30:8020/"), new Configuration())
    val localPath = "/data/"
    val hdfsPath = "/minzheng_day_report/shanghai/" + date + "/"

    if (fileSystem.exists(new Path(hdfsPath)).equals(false)) {
      fileSystem.mkdirs(new Path(hdfsPath))
    }



    //排菜数据日统计分析报表
    val platoonTotalFilename = year + "年排菜数据日统计分析报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val platoonTotalFile = new File(localPath + platoonTotalFilename)

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

    val platoonTotalContent = new MinzhengExcelStat().platoonTotal(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(platoonTotalContent, platoonTotalSheet, platoonTotalContentStyle, 2, 0)

    val platoonTotalStream = new FileOutputStream(platoonTotalFile)
    platoonTotalWorkbook.write(platoonTotalStream)
    platoonTotalStream.close()


    fileSystem.moveFromLocalFile(new Path(localPath + platoonTotalFilename), new Path(hdfsPath + platoonTotalFilename))

    //排菜数据日报表
    val platoonFilename = year + "年排菜数据日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val platoonFile = new File(localPath + platoonFilename)

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

    val platoonContent = new MinzhengExcelStat().platoon(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(platoonContent, platoonSheet, platoonContentStyle, 2, 0)

    val platoonStream = new FileOutputStream(platoonFile)
    platoonWorkbook.write(platoonStream)
    platoonStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + platoonFilename), new Path(hdfsPath + platoonFilename))


    //排菜菜品明细表
    val dishmenuFilename = year + "年排菜明细数据日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val dishmenuFile = new File(localPath + dishmenuFilename)

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

    val dishmenuContent = new MinzhengExcelStat().dishmenu(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(dishmenuContent, dishmenuSheet, dishmenuContentStyle, 2, 0)

    val dishmenuStream = new FileOutputStream(dishmenuFile)
    dishmenuWorkbook.write(dishmenuStream)
    dishmenuStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + dishmenuFilename), new Path(hdfsPath + dishmenuFilename))

    //验收日统计报表
    val ledegeTotalFilename = year + "年验收数据日统计_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegeTotalFile = new File(localPath + ledegeTotalFilename)

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

    val ledegeTotalContent = new MinzhengExcelStat().ledegeTotal(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(ledegeTotalContent, ledegeTotalSheet, ledegeTotalContentStyle, 2, 0)

    val ledegeTotalStream = new FileOutputStream(ledegeTotalFile)
    ledegeTotalWorkbook.write(ledegeTotalStream)
    ledegeTotalStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + ledegeTotalFilename), new Path(hdfsPath + ledegeTotalFilename))

    //验收数据日报表
    val ledegeFilename = year + "年验收数据日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegeFile = new File(localPath + ledegeFilename)

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

    val ledegeContent = new MinzhengExcelStat().ledege(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(ledegeContent, ledegeSheet, ledegeContentStyle, 2, 0)

    val ledegeStream = new FileOutputStream(ledegeFile)
    ledegeWorkbook.write(ledegeStream)
    ledegeStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + ledegeFilename), new Path(hdfsPath + ledegeFilename))

    //原料明细
    val ledegeDetailFilename = year + "年原料明细日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegeDetailFile = new File(localPath + ledegeDetailFilename)
    val ledegeDetailSheetWidth = Array[Int](2500, 3500, 4500, 3500, 3500, 3500, 3500, 6500, 3500, 6500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
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

    val ledegeDetailContent = new MinzhengExcelStat().ledegeDetail(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(ledegeDetailContent, ledegeDetailSheet, ledegeDetailContentStyle, 2, 0)

    val ledegeDetailStream = new FileOutputStream(ledegeDetailFile)
    ledegeDetailWorkbook.write(ledegeDetailStream)
    ledegeDetailStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + ledegeDetailFilename), new Path(hdfsPath + ledegeDetailFilename))

    //留样日统计报表
    val reserveTotalFilename = year + "年留样统计日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val reserveTotalFile = new File(localPath + reserveTotalFilename)
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

    val reserveTotalContent = new MinzhengExcelStat().reserveTotal(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(reserveTotalContent, reserveTotalSheet, reserveTotalContentStyle, 2, 0)

    val reserveTotalStream = new FileOutputStream(reserveTotalFile)
    reserveTotalWorkbook.write(reserveTotalStream)
    reserveTotalStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + reserveTotalFilename), new Path(hdfsPath + reserveTotalFilename))


    //留样日报表
    val reserveFilename = year + "年留样日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val reserveFile = new File(localPath + reserveFilename)
    val reserveSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 6500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val reserveWorkbook = new HSSFWorkbook()
    val reserveTitleStyle = new ExceltitleStat().style(reserveWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val reserveTitleValueStyle = new ExceltitleStat().style(reserveWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val reserveContentStyle = new ExceltitleStat().style(reserveWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val reserveSheet = reserveWorkbook.createSheet("上海市留样数据日报表")
    new ExceltitleStat().sheetname(reserveSheet, reserveSheetWidth)

    val reserveCellRangeAddress = new CellRangeAddress(0, 0, 0, 18)
    reserveSheet.addMergedRegion(reserveCellRangeAddress)
    val reserveTitle = Array[String]("留样数据日报表" + "         " + "日期：" + date)
    val reserveRow = reserveSheet.createRow(0)
    reserveRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(reserveTitle, reserveTitleStyle, reserveRow)

    val reserveTitleValue = Array[String]("序号", "就餐日期", "区", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "供餐模式", "餐别", "菜单名称", "菜品名称", "菜品份数", "是否留样", "留样数量", "留样时间", "留样说明", "留样人", "留样操作时间")
    val reserveRow1 = reserveSheet.createRow(1)
    reserveRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(reserveTitleValue, reserveTitleValueStyle, reserveRow1)

    val reserveContent = new MinzhengExcelStat().reserve(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(reserveContent, reserveSheet, reserveContentStyle, 2, 0)

    val reserveStream = new FileOutputStream(reserveFile)
    reserveWorkbook.write(reserveStream)
    reserveStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + reserveFilename), new Path(hdfsPath + reserveFilename))

    //餐厨垃圾回收日报表
    val recyclerWasteFilename = year + "年餐厨垃圾回收日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val recyclerWasteFile = new File(localPath + recyclerWasteFilename)
    val recyclerWasteSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 4500, 3500, 3500, 3500, 3500)
    val recyclerWasteWorkbook = new HSSFWorkbook()
    val recyclerWasteTitleStyle = new ExceltitleStat().style(recyclerWasteWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val recyclerWasteTitleValueStyle = new ExceltitleStat().style(recyclerWasteWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val recyclerWasteContentStyle = new ExceltitleStat().style(recyclerWasteWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val recyclerWasteSheet = recyclerWasteWorkbook.createSheet("上海市餐厨垃圾回收日报表")
    new ExceltitleStat().sheetname(recyclerWasteSheet, recyclerWasteSheetWidth)

    val recyclerWasteCellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
    recyclerWasteSheet.addMergedRegion(recyclerWasteCellRangeAddress)
    val recyclerWasteTitle = Array[String]("餐厨垃圾回收日报表" + "         " + "日期：" + date)
    val recyclerWasteRow = recyclerWasteSheet.createRow(0)
    recyclerWasteRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(recyclerWasteTitle, recyclerWasteTitleStyle, recyclerWasteRow)

    val recyclerWasteTitleValue = Array[String]("序号", "回收日期", "区", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "回收单位", "回收人", "数量（桶)", "回收单据")
    val recyclerWasteRow1 = recyclerWasteSheet.createRow(1)
    recyclerWasteRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(recyclerWasteTitleValue, recyclerWasteTitleValueStyle, recyclerWasteRow1)

    val recyclerWasteContent = new MinzhengExcelStat().recyclerData(hiveContext, year, month, date, 1)
    new ExceltitleStat().excelcontent(recyclerWasteContent, recyclerWasteSheet, recyclerWasteContentStyle, 2, 0)

    val recyclerWasteStream = new FileOutputStream(recyclerWasteFile)
    recyclerWasteWorkbook.write(recyclerWasteStream)
    recyclerWasteStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + recyclerWasteFilename), new Path(hdfsPath + recyclerWasteFilename))

    //废弃油脂回收日报表
    val recyclerOilFilename = year + "年废弃油脂回收日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val recyclerOilFile = new File(localPath + recyclerOilFilename)
    val recyclerOilSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 4500, 3500, 3500, 3500, 3500, 3500)
    val recyclerOilWorkbook = new HSSFWorkbook()
    val recyclerOilTitleStyle = new ExceltitleStat().style(recyclerOilWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val recyclerOilTitleValueStyle = new ExceltitleStat().style(recyclerOilWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val recyclerOilContentStyle = new ExceltitleStat().style(recyclerOilWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val recyclerOilSheet = recyclerOilWorkbook.createSheet("上海市油脂回收日报表")
    new ExceltitleStat().sheetname(recyclerOilSheet, recyclerOilSheetWidth)

    val recyclerOilCellRangeAddress = new CellRangeAddress(0, 0, 0, 12)
    recyclerOilSheet.addMergedRegion(recyclerOilCellRangeAddress)
    val recyclerOilTitle = Array[String]("油脂回收日报表" + "         " + "日期：" + date)
    val recyclerOilRow = recyclerOilSheet.createRow(0)
    recyclerOilRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(recyclerOilTitle, recyclerOilTitleStyle, recyclerOilRow)

    val recyclerOilTitleValue = Array[String]("序号", "回收日期", "区", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "种类", "回收单位", "回收人", "数量（公斤)", "回收单据")
    val recyclerOilRow1 = recyclerOilSheet.createRow(1)
    recyclerOilRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(recyclerOilTitleValue, recyclerOilTitleValueStyle, recyclerOilRow1)

    val recyclerOilContent = new MinzhengExcelStat().recyclerData(hiveContext, year, month, date, 2)
    new ExceltitleStat().excelcontent(recyclerOilContent, recyclerOilSheet, recyclerOilContentStyle, 2, 0)

    val recyclerOilStream = new FileOutputStream(recyclerOilFile)
    recyclerOilWorkbook.write(recyclerOilStream)
    recyclerOilStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + recyclerOilFilename), new Path(hdfsPath + recyclerOilFilename))


    //人员健康证逾期日报表
    val peopleLicenseFilename = year + "年人员健康证日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val peopleLicenseFile = new File(localPath + peopleLicenseFilename)
    val peopleLicenseSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 4500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val peopleLicenseWorkbook = new HSSFWorkbook()
    val peopleLicenseTitleStyle = new ExceltitleStat().style(peopleLicenseWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val peopleLicenseTitleValueStyle = new ExceltitleStat().style(peopleLicenseWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val peopleLicenseContentStyle = new ExceltitleStat().style(peopleLicenseWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val peopleLicenseSheet = peopleLicenseWorkbook.createSheet("上海市人员健康证日报表")
    new ExceltitleStat().sheetname(peopleLicenseSheet, peopleLicenseSheetWidth)

    val peopleLicenseCellRangeAddress = new CellRangeAddress(0, 0, 0, 14)
    peopleLicenseSheet.addMergedRegion(peopleLicenseCellRangeAddress)
    val peopleLicenseTitle = Array[String]("人员健康证日报表" + "         " + "日期：" + date)
    val peopleLicenseRow = peopleLicenseSheet.createRow(0)
    peopleLicenseRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(peopleLicenseTitle, peopleLicenseTitleStyle, peopleLicenseRow)

    val peopleLicenseTitleValue = Array[String]("序号", "预警日期", "区", "主管部门", "类型", "创办主体", "单位名称", "触发预警单位", "证件名称", "证件主体", "证件号码", "有效日期", "证件状况", "状态", "消除日期")
    val peopleLicenseRow1 = peopleLicenseSheet.createRow(1)
    peopleLicenseRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(peopleLicenseTitleValue, peopleLicenseTitleValueStyle, peopleLicenseRow1)

    val peopleLicenseContent = new MinzhengExcelStat().peopleLicense(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(peopleLicenseContent, peopleLicenseSheet, peopleLicenseContentStyle, 2, 0)

    val peopleLicenseStream = new FileOutputStream(peopleLicenseFile)
    peopleLicenseWorkbook.write(peopleLicenseStream)
    peopleLicenseStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + peopleLicenseFilename), new Path(hdfsPath + peopleLicenseFilename))


    //物料过保预警报表
    val materialWarnFilename = year + "年物料过保预警日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val materialWarnFile = new File(localPath + materialWarnFilename)
    val materialWarnSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 4500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val materialWarnWorkbook = new HSSFWorkbook()
    val materialWarnTitleStyle = new ExceltitleStat().style(materialWarnWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val materialWarnTitleValueStyle = new ExceltitleStat().style(materialWarnWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val materialWarnContentStyle = new ExceltitleStat().style(materialWarnWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val materialWarnSheet = materialWarnWorkbook.createSheet("上海市物料过保预警日报表")
    new ExceltitleStat().sheetname(materialWarnSheet, materialWarnSheetWidth)

    val materialWarnCellRangeAddress = new CellRangeAddress(0, 0, 0, 16)
    materialWarnSheet.addMergedRegion(materialWarnCellRangeAddress)
    val materialWarnTitle = Array[String]("物料过保预警日报表" + "         " + "日期：" + date)
    val materialWarnRow = materialWarnSheet.createRow(0)
    materialWarnRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(materialWarnTitle, materialWarnTitleStyle, materialWarnRow)

    val materialWarnTitleValue = Array[String]("序号", "预警日期", "区", "主管部门", "类型", "创办主体", "单位名称", "触发预警单位", "配送批次号", "过保物料", "生产日期", "保质期", "车辆", "司机", "配送日期", "状态", "消除日期")
    val materialWarnRow1 = materialWarnSheet.createRow(1)
    materialWarnRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(materialWarnTitleValue, materialWarnTitleValueStyle, materialWarnRow1)

    val materialWarnContent = new MinzhengExcelStat().materialWarn(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(materialWarnContent, materialWarnSheet, materialWarnContentStyle, 2, 0)

    val materialWarnStream = new FileOutputStream(materialWarnFile)
    materialWarnWorkbook.write(materialWarnStream)
    materialWarnStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + materialWarnFilename), new Path(hdfsPath + materialWarnFilename))

    //物流配送预警日报表
    val ledegerWarnFilename = year + "年物流配送预警日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val ledegerWarnFile = new File(localPath + ledegerWarnFilename)
    val ledegerWarnSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 4500, 6500, 3500, 3500, 3500, 3500, 3500, 3500)
    val ledegerWarnWorkbook = new HSSFWorkbook()
    val ledegerWarnTitleStyle = new ExceltitleStat().style(ledegerWarnWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegerWarnTitleValueStyle = new ExceltitleStat().style(ledegerWarnWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val ledegerWarnContentStyle = new ExceltitleStat().style(ledegerWarnWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    val ledegerWarnSheet = ledegerWarnWorkbook.createSheet("上海市物流配送预警日报表")
    new ExceltitleStat().sheetname(ledegerWarnSheet, ledegerWarnSheetWidth)

    val ledegerWarnCellRangeAddress = new CellRangeAddress(0, 0, 0, 14)
    ledegerWarnSheet.addMergedRegion(ledegerWarnCellRangeAddress)
    val ledegerWarnTitle = Array[String]("物流配送预警日报表" + "         " + "日期：" + date)
    val ledegerWarnRow = ledegerWarnSheet.createRow(0)
    ledegerWarnRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(ledegerWarnTitle, ledegerWarnTitleStyle, ledegerWarnRow)

    val ledegerWarnTitleValue = Array[String]("序号", "预警日期", "区", "主管部门", "类型", "创办主体", "单位名称", "预警类型", "触发预警单位", "配送批次号", "车辆", "司机", "配送日期", "状态", "消除日期")
    val ledegerWarnRow1 = ledegerWarnSheet.createRow(1)
    ledegerWarnRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(ledegerWarnTitleValue, ledegerWarnTitleValueStyle, ledegerWarnRow1)

    val ledegerWarnContent = new MinzhengExcelStat().ledegerWarn(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(ledegerWarnContent, ledegerWarnSheet, ledegerWarnContentStyle, 2, 0)

    val ledegerWarnStream = new FileOutputStream(ledegerWarnFile)
    ledegerWarnWorkbook.write(ledegerWarnStream)
    ledegerWarnStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + ledegerWarnFilename), new Path(hdfsPath + ledegerWarnFilename))

    //证照预警处理数据日统计
    val warnTotalFilename = year + "年预警处理统计日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val warnTotalFile = new File(localPath + warnTotalFilename)
    val warnTotalSheetWidth = Array[Int](2500, 4500, 3500, 3500, 3500, 3500, 3500, 3500)
    val warnTotalWorkbook = new HSSFWorkbook()
    val warnTotalTitleStyle = new ExceltitleStat().style(warnTotalWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val warnTotalTitleValueStyle = new ExceltitleStat().style(warnTotalWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val warnTotalContentStyle = new ExceltitleStat().style(warnTotalWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val warnTotalNoContentStyle = new ExceltitleStat().style(warnTotalWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)

    val warnTotalSheet = warnTotalWorkbook.createSheet("上海市预警处理统计日报表")
    new ExceltitleStat().sheetname(warnTotalSheet, warnTotalSheetWidth)

    val warnTotalCellRangeAddress = new CellRangeAddress(0, 0, 0, 7)
    warnTotalSheet.addMergedRegion(warnTotalCellRangeAddress)
    val warnTotalTitle = Array[String]("每日预警处理统计分析报表" + "         " + "日期：" + date)
    val warnTotalRow = warnTotalSheet.createRow(0)
    warnTotalRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(warnTotalTitle, warnTotalTitleStyle, warnTotalRow)

    val warnTotalTitleValue = Array[String]("序号", "主管部门", "预警总数", "未处理数", "已驳回数", "审核中数", "已消除数", "预警处理率")
    val warnTotalRow1 = warnTotalSheet.createRow(1)
    warnTotalRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(warnTotalTitleValue, warnTotalTitleValueStyle, warnTotalRow1)

    val warnTotalContent = new MinzhengExcelStat().warnTotal(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(warnTotalContent._1, warnTotalSheet, warnTotalContentStyle, 2, 0)

    if (warnTotalContent._1.size != 0) {
      val warnTotalCellRangeAddress1 = new CellRangeAddress(warnTotalContent._1.size + 1, warnTotalContent._1.size + 1, 0, 1)
      warnTotalSheet.addMergedRegion(warnTotalCellRangeAddress1)
      val warnTotalWei1 = Array[String]("合计", "合计")
      val warnTotalWei = Array[String](warnTotalContent._2, warnTotalContent._3, warnTotalContent._4, warnTotalContent._5, warnTotalContent._6)
      val warnTotalRow2 = warnTotalSheet.createRow(warnTotalContent._1.size + 1)
      new ExceltitleStat().exceltitle(warnTotalWei1, warnTotalNoContentStyle, warnTotalRow2)
      new ExceltitleStat().exceltitleOtherStat(warnTotalWei, warnTotalNoContentStyle, warnTotalRow2, 2)
    }


    val warnTotalStream = new FileOutputStream(warnTotalFile)
    warnTotalWorkbook.write(warnTotalStream)
    warnTotalStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + warnTotalFilename), new Path(hdfsPath + warnTotalFilename))


    //业务操作数据汇总报表
    val useAllFilename = year + "年业务操作数据汇总日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val useAllFile = new File(localPath + useAllFilename)
    val useAllSheetWidth = Array[Int](2500, 3500, 3500, 3500, 3500, 3500, 6500, 6500, 3500, 3500, 3500, 3500, 3500, 3500, 3500)
    val useAllWorkbook = new HSSFWorkbook()
    val useAllTitleStyle = new ExceltitleStat().style(useAllWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val useAllTitleValueStyle = new ExceltitleStat().style(useAllWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val useAllContentStyle = new ExceltitleStat().style(useAllWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val useAllNoContentStyle = new ExceltitleStat().style(useAllWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)

    val useAllSheet = useAllWorkbook.createSheet("上海市业务操作数据汇总日报表")
    new ExceltitleStat().sheetname(useAllSheet, useAllSheetWidth)

    val useAllCellRangeAddress = new CellRangeAddress(0, 0, 0, 14)
    useAllSheet.addMergedRegion(useAllCellRangeAddress)
    val useAllTitle = Array[String]("业务操作数据汇总日报表" + "         " + "日期：" + date + "√已操作 ×未操作")
    val useAllRow = useAllSheet.createRow(0)
    useAllRow.setHeightInPoints(30)
    new ExceltitleStat().exceltitle(useAllTitle, useAllTitleStyle, useAllRow)

    val useAllTitleValue = Array[String]("序号", "日期", "所在地", "主管单位", "类型", "创办主体", "单位名称", "团餐公司", "供餐", "排菜", "用料确认", "指派", "配送", "验收", "留样")
    val useAllRow1 = useAllSheet.createRow(1)
    useAllRow1.setHeightInPoints(20)
    new ExceltitleStat().exceltitle(useAllTitleValue, useAllTitleValueStyle, useAllRow1)

    val useAllContent = new MinzhengExcelStat().allUse(hiveContext, year, month, date)
    new ExceltitleStat().excelcontent(useAllContent._1, useAllSheet, useAllContentStyle, 2, 0)

    val useAllCellRangeAddress1 = new CellRangeAddress(useAllContent._1.size + 1, useAllContent._1.size + 1, 0, 7)
    useAllSheet.addMergedRegion(useAllCellRangeAddress1)
    val useAllWei1 = Array[String]("合计")
    val useAllWei = Array[String](useAllContent._2, useAllContent._3, useAllContent._4, useAllContent._5, useAllContent._6, useAllContent._7, useAllContent._8)
    val useAllRow2 = useAllSheet.createRow(useAllContent._1.size + 1)
    new ExceltitleStat().exceltitle(useAllWei1, useAllNoContentStyle, useAllRow2)
    new ExceltitleStat().exceltitleOtherStat(useAllWei, useAllNoContentStyle, useAllRow2, 8)

    val useAllStream = new FileOutputStream(useAllFile)
    useAllWorkbook.write(useAllStream)
    useAllStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + useAllFilename), new Path(hdfsPath + useAllFilename))


    //一键追溯
    val keyBackFilename = year + "年一键追溯查询日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val keyBackFile = new File(localPath + keyBackFilename)
    val keyBackSheetWidth = Array[Int](2500, 3500, 4500, 3500, 3500, 3500, 6500, 4500, 6500, 3500, 3500, 3500, 3500)
    val keyBackWorkbook = new HSSFWorkbook()
    val keyBackTitleStyle = new ExceltitleStat().style(keyBackWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)
    val keyBackTitleValueStyle = new ExceltitleStat().style(keyBackWorkbook, 11, true, "宋体", HorizontalAlignment.LEFT, BorderStyle.NONE)
    val keyBackTitleValueStyle1 = new ExceltitleStat().style(keyBackWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val keyBackContentStyle = new ExceltitleStat().style(keyBackWorkbook, 12, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)


    //民政的基础信息
    val gerocomiumDetail = new MinzhengExcelStat().gerocomiumDetailAll(hiveContext, year, month, date)

    for (i <- 0 until gerocomiumDetail.size) {

      val keyBackSheet = keyBackWorkbook.createSheet(gerocomiumDetail(i)._3)
      new ExceltitleStat().sheetname(keyBackSheet, keyBackSheetWidth)

      val keyBackCellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
      keyBackSheet.addMergedRegion(keyBackCellRangeAddress)
      val keyBackTitle = Array[String]("一键追溯查询结果报告")
      val keyBackRow = keyBackSheet.createRow(0)
      keyBackRow.setHeightInPoints(40)
      new ExceltitleStat().exceltitle(keyBackTitle, keyBackTitleStyle, keyBackRow)

      val keyBackValue = Array[String]("一、用餐日期：" + date + "-" + date)
      val keyBackRow1 = keyBackSheet.createRow(1)
      keyBackRow1.setHeightInPoints(20)
      new ExceltitleStat().exceltitle(keyBackValue, keyBackTitleValueStyle, keyBackRow1)

      val keyBackValue1 = Array[String]("二、关联单位：1所（含1个区）")
      val keyBackRow2 = keyBackSheet.createRow(3)
      keyBackRow2.setHeightInPoints(20)
      new ExceltitleStat().exceltitle(keyBackValue1, keyBackTitleValueStyle, keyBackRow2)

      val keyBackValue2 = Array[String]("序号", "所在地", "类型", "单位名称", "地址", "法人代表", "联系人", "联系人电话")
      val keyBackRow3 = keyBackSheet.createRow(4)
      keyBackRow3.setHeightInPoints(20)
      new ExceltitleStat().exceltitle(keyBackValue2, keyBackTitleValueStyle1, keyBackRow3)

      val keyBackRow4 = keyBackSheet.createRow(5)
      val gerocomiumDetailContent = Array[String]("1", gerocomiumDetail(i)._1, gerocomiumDetail(i)._2, gerocomiumDetail(i)._3, gerocomiumDetail(i)._4, gerocomiumDetail(i)._5, gerocomiumDetail(i)._6, gerocomiumDetail(i)._7)
      new ExceltitleStat().exceltitle(gerocomiumDetailContent, keyBackContentStyle, keyBackRow4)


      val keyBackValue4 = Array[String]("三、关联单位详情")
      val keyBackRow5 = keyBackSheet.createRow(8)
      keyBackRow5.setHeightInPoints(20)
      new ExceltitleStat().exceltitle(keyBackValue4, keyBackTitleValueStyle, keyBackRow5)

      val keyBackValue5 = Array[String]("1、单位1:" + gerocomiumDetail(i)._3)
      val keyBackRow6 = keyBackSheet.createRow(9)
      keyBackRow6.setHeightInPoints(20)
      new ExceltitleStat().exceltitle(keyBackValue5, keyBackTitleValueStyle, keyBackRow6)

      val keyBackValue6 = Array[String]("①菜谱")
      val keyBackRow7 = keyBackSheet.createRow(10)
      keyBackRow6.setHeightInPoints(20)
      new ExceltitleStat().exceltitle(keyBackValue6, keyBackTitleValueStyle, keyBackRow7)

      val keyBackValue7 = Array[String]("序号", "项目点", "团餐公司", "菜单名称", "餐别", date)
      val keyBackRow8 = keyBackSheet.createRow(11)
      new ExceltitleStat().exceltitle(keyBackValue7, keyBackTitleValueStyle1, keyBackRow8)

      val gerocomiumDishDetail = new MinzhengExcelStat().gerocomiumDishDetail(hiveContext, year, month, date, gerocomiumDetail(i)._13)
      new ExceltitleStat().excelcontent(gerocomiumDishDetail._1, keyBackSheet, keyBackContentStyle, 12, 0)

      val keyBackValue8 = Array[String]("②单位信息")
      val keyBackRow9 = keyBackSheet.createRow(12 + gerocomiumDishDetail._1.size + 2)
      new ExceltitleStat().exceltitle(keyBackValue8, keyBackTitleValueStyle, keyBackRow9)

      val keyBackValue9 = Array[String]("序号", "单位名称", "区", "地址", "统一社会信用代码证", "主管部门", "类型", "创办主体", "经营方式", "法人代表", "联系人", "联系电话")
      val keyBackRow10 = keyBackSheet.createRow(13 + gerocomiumDishDetail._1.size + 2)
      new ExceltitleStat().exceltitle(keyBackValue9, keyBackTitleValueStyle1, keyBackRow10)

      val keyBackRow11 = keyBackSheet.createRow(14 + gerocomiumDishDetail._1.size + 2)
      val gerocomiumDetailContent1 = Array[String]("1", gerocomiumDetail(i)._3, gerocomiumDetail(i)._1, gerocomiumDetail(i)._4, gerocomiumDetail(i)._10, gerocomiumDetail(i)._11, gerocomiumDetail(i)._2, gerocomiumDetail(i)._8, gerocomiumDetail(i)._9, gerocomiumDetail(i)._5, gerocomiumDetail(i)._6, gerocomiumDetail(i)._7)
      new ExceltitleStat().exceltitle(gerocomiumDetailContent1, keyBackContentStyle, keyBackRow11)

      val supplierLicenseData = new MinzhengExcelStat().supplierLicense(hiveContext, gerocomiumDetail(i)._12)
      if (StringUtils.isEmpty(gerocomiumDetail(i)._12)) {
        val keyBackValue10 = Array[String]("③人员证照信息：健康证0个    A1证0个   B证0个   C证 0个   A2证0个")
        val keyBackRow12 = keyBackSheet.createRow(17 + gerocomiumDishDetail._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValue10, keyBackTitleValueStyle, keyBackRow12)

        val keyBackValues = Array[String]("序号", "姓名", "证件类型", "证件号码", "发证日期", "有效日期", "证件状态", "关联团餐公司")
        val keyBackRows = keyBackSheet.createRow(18 + gerocomiumDishDetail._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValues, keyBackTitleValueStyle1, keyBackRows)
      } else {

        val keyBackValue10 = Array[String]("③人员证照信息：健康证" + supplierLicenseData._2 + "个    A1证" + supplierLicenseData._3 + "个   B证" + supplierLicenseData._4 + "个   C证" + supplierLicenseData._5 + "个   A2证" + supplierLicenseData._6 + "个")
        val keyBackRow12 = keyBackSheet.createRow(17 + gerocomiumDishDetail._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValue10, keyBackTitleValueStyle, keyBackRow12)

        val keyBackValues = Array[String]("序号", "姓名", "证件类型", "证件号码", "发证日期", "有效日期", "证件状态", "关联团餐公司")
        val keyBackRows = keyBackSheet.createRow(18 + gerocomiumDishDetail._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValues, keyBackTitleValueStyle1, keyBackRows)

        new ExceltitleStat().excelcontent(supplierLicenseData._1, keyBackSheet, keyBackContentStyle, 19 + gerocomiumDishDetail._1.size + 2, 0)
      }

      val keyBackValue11 = Array[String]("④团餐公司信息")
      val keyBackRow13 = keyBackSheet.createRow(19 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
      new ExceltitleStat().exceltitle(keyBackValue11, keyBackTitleValueStyle, keyBackRow13)

      val keyBackValue12 = Array[String]("序号", "团餐公司", "服务起止时间", "统一社会信用代码证", "食品经营许可证", "食品经营许可证有效日期", "区", "地址", "法人代表", "联系人", "联系电话")
      val keyBackRow14 = keyBackSheet.createRow(20 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
      new ExceltitleStat().exceltitle(keyBackValue12, keyBackTitleValueStyle1, keyBackRow14)

      if (gerocomiumDishDetail._2.size != 0) {
        val supplier_id = gerocomiumDishDetail._2(0)
        val b2bSupplierLicense = new MinzhengExcelStat().b2bSupplierLicense(hiveContext, supplier_id)
        val keyBackRow15 = keyBackSheet.createRow(21 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
//        ("1", Array[String]
//  ("1", company_name, "", credit_code, qualification_code, end_effective_date, name, office_address, legal_representative, contact_person, contact_phone))
        if (b2bSupplierLicense.length>0){
          new ExceltitleStat().exceltitle(b2bSupplierLicense(0)._2, keyBackContentStyle, keyBackRow15)
        }
      }

      val keyBackValue13 = Array[String]("⑤供应商信息")
      val keyBackRow16 = keyBackSheet.createRow(24 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
      new ExceltitleStat().exceltitle(keyBackValue13, keyBackTitleValueStyle, keyBackRow16)

      val gerocomiumLedegeDetail = new MinzhengExcelStat().gerocomiumLedegeDetail(hiveContext, year, month, date, gerocomiumDetail(i)._13)
      if (gerocomiumLedegeDetail.size == 0) {
        val keyBackValue15 = Array[String]("序号", "供应商名称", "服务起止时间", "统一社会信用代码证", "食品经营许可证", "食品经营许可证有效日期", "区", "地址", "法人代表", "联系人", "联系电话")
        val keyBackRow17 = keyBackSheet.createRow(25 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValue15, keyBackTitleValueStyle1, keyBackRow17)

        val keyBackValue14 = Array[String]("⑥配送单明细")
        val keyBackRow18 = keyBackSheet.createRow(28 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValue14, keyBackTitleValueStyle, keyBackRow18)
      } else {
        val keyBackValue15 = Array[String]("序号", "供应商名称", "服务起止时间", "统一社会信用代码证", "食品经营许可证", "食品经营许可证有效日期", "区", "地址", "法人代表", "联系人", "联系电话")
        val keyBackRow17 = keyBackSheet.createRow(25 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2)
        new ExceltitleStat().exceltitle(keyBackValue15, keyBackTitleValueStyle1, keyBackRow17)

        val keyBackValue16 = Array[String]("⑥配送单明细")
        val keyBackRow18 = keyBackSheet.createRow(28 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2 + gerocomiumLedegeDetail.size)
        new ExceltitleStat().exceltitle(keyBackValue16, keyBackTitleValueStyle, keyBackRow18)

        var j = 0
        for (i <- 0 until gerocomiumLedegeDetail.size) {
          val supply_id = gerocomiumLedegeDetail(i)._1._1
          val b2bSupplierLicense1 = new MinzhengExcelStat().b2bSupplierLicense(hiveContext, supply_id)
          val keyBackRow19 = keyBackSheet.createRow(26 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2 + i)
          val arr = Array[String]((i + 1).toString, b2bSupplierLicense1(i)._2(1), b2bSupplierLicense1(i)._2(2), b2bSupplierLicense1(i)._2(3), b2bSupplierLicense1(i)._2(4), b2bSupplierLicense1(i)._2(5), b2bSupplierLicense1(i)._2(6), b2bSupplierLicense1(i)._2(7), b2bSupplierLicense1(i)._2(8), b2bSupplierLicense1(i)._2(9), b2bSupplierLicense1(i)._2(10))
          new ExceltitleStat().exceltitle(arr, keyBackContentStyle, keyBackRow19)

          val keyBackValue18 = Array[String](gerocomiumLedegeDetail(i)._1._2 + "(" + gerocomiumLedegeDetail(i)._2 ++ "条)")
          val keyBackRow20 = keyBackSheet.createRow(30 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2 + gerocomiumLedegeDetail.size + j)
          new ExceltitleStat().exceltitle(keyBackValue18, keyBackTitleValueStyle, keyBackRow20)


          val keyBackValue19 = Array[String]("序号", "用料日期", "团餐公司", "收货日期", "验收日期", "配货批次号", "物料名称", "标准名称", "规格", "类别", "换算关系", "换算后数量", "批号", "生产日期", "保质期", "是否验收", "验收数量")
          val keyBackRow21 = keyBackSheet.createRow(31 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2 + gerocomiumLedegeDetail.size)
          new ExceltitleStat().exceltitle(keyBackValue18, keyBackTitleValueStyle1, keyBackRow21)

          val arr1 = Array[(String, Array[String])](("1", gerocomiumLedegeDetail(i)._3))
          new ExceltitleStat().excelcontent(arr1, keyBackSheet, keyBackContentStyle, 32 + gerocomiumDishDetail._1.size + 2 + supplierLicenseData._1.size + 2 + gerocomiumLedegeDetail.size, 0)

          j += gerocomiumLedegeDetail(i)._3.size + 4

        }


      }

    }

    val keyBackStream = new FileOutputStream(keyBackFile)
    keyBackWorkbook.write(keyBackStream)
    keyBackStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + keyBackFilename), new Path(hdfsPath + keyBackFilename))

    //一键排查
    val checkFilename = year + "年一键排查日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
    val checkFile = new File(localPath + checkFilename)
    val checkSheetWidth = Array[Int](2500, 3500, 5500, 5500, 3500, 3500, 3500, 5500, 3500, 3500, 3500, 3500)
    val checkWorkbook = new HSSFWorkbook()
    val checkTitleStyle = new ExceltitleStat().style(checkWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.NONE)
    val checkTitleValueStyle = new ExceltitleStat().style(checkWorkbook, 11, true, "宋体", HorizontalAlignment.LEFT, BorderStyle.NONE)
    val checkTitleValueStyle1 = new ExceltitleStat().style(checkWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
    val checkContentStyle = new ExceltitleStat().style(checkWorkbook, 12, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)

    //原料信息
    val materailData = new MinzhengExcelStat().gerocomiumMaterialDetail(hiveContext, year, month, date)

    if (materailData.size == 0) {
      val checkSheet = checkWorkbook.createSheet()
      new ExceltitleStat().sheetname(checkSheet, checkSheetWidth)

      val checkCellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
      checkSheet.addMergedRegion(checkCellRangeAddress)
      val checkTitle = Array[String]("一键排查结果报告")
      val checkRow = checkSheet.createRow(0)
      checkRow.setHeightInPoints(40)
      new ExceltitleStat().exceltitle(checkTitle, checkTitleStyle, checkRow)
    } else {
      for (i <- 0 until materailData.size) {
        val checkSheet = checkWorkbook.createSheet(materailData(i)._1)
        new ExceltitleStat().sheetname(checkSheet, checkSheetWidth)

        val checkCellRangeAddress = new CellRangeAddress(0, 0, 0, 11)
        checkSheet.addMergedRegion(checkCellRangeAddress)
        val checkTitle = Array[String]("一键排查结果报告")
        val checkRow = checkSheet.createRow(0)
        checkRow.setHeightInPoints(40)
        new ExceltitleStat().exceltitle(checkTitle, checkTitleStyle, checkRow)

        val checkValue = Array[String]("一、原料名称：" + materailData(i)._1)
        val checkRow1 = checkSheet.createRow(1)
        checkRow1.setHeightInPoints(20)
        new ExceltitleStat().exceltitle(checkValue, checkTitleValueStyle, checkRow1)

        val checkValue1 = Array[String]("二、用餐日期：" + date + "-" + date)
        val checkRow2 = checkSheet.createRow(2)
        checkRow2.setHeightInPoints(20)
        new ExceltitleStat().exceltitle(checkValue1, checkTitleValueStyle, checkRow2)

        var schoolList = List[String]()
        var areaList = List[String]()

        var gerocomiumIndex = 0
        var guanlaianIndex = 0
        materailData(i)._2.foreach({
          x =>
            schoolList = schoolList.+:(x._6)
            areaList = areaList.+:(x._5)

            //关联单位基础信息
            val gerocomiumDetail = new MinzhengExcelStat().gerocomiumDetail(hiveContext, x._6)
            gerocomiumIndex += 1
            val gerocomiumDetailData = Array[String](gerocomiumIndex.toString, gerocomiumDetail(0)._1, gerocomiumDetail(0)._3, gerocomiumDetail(0)._4, gerocomiumDetail(0)._5, gerocomiumDetail(0)._6, gerocomiumDetail(0)._7, x._4, x._1, x._7)
            val checkRow5 = checkSheet.createRow(5 + gerocomiumIndex)
            new ExceltitleStat().exceltitle(gerocomiumDetailData, checkContentStyle, checkRow5)

            //四、原料关联单位信息详情
            val checkValue5 = Array[String](gerocomiumIndex + "、单位" + gerocomiumIndex + "：" + gerocomiumDetail(0)._3)
            val checkRow7 = checkSheet.createRow(7 + materailData(i)._2.size + 2 + guanlaianIndex)
            checkRow7.setHeightInPoints(20)
            new ExceltitleStat().exceltitle(checkValue5, checkTitleValueStyle, checkRow7)

            val checkValue6 = Array[String]("① 单位信息")
            val checkRow8 = checkSheet.createRow(9 + materailData(i)._2.size + 2 + guanlaianIndex)
            checkRow8.setHeightInPoints(20)
            new ExceltitleStat().exceltitle(checkValue6, checkTitleValueStyle, checkRow8)

            val checkValue7 = Array[String]("序号", "单位名称", "区", "地址", "统一社会信用代码证", "主管部门", "类型", "创办主体", "供餐模式", "法人代表", "联系人", "联系电话")
            val checkRow9 = checkSheet.createRow(10 + materailData(i)._2.size + 2 + guanlaianIndex)
            checkRow9.setHeightInPoints(20)
            new ExceltitleStat().exceltitle(checkValue7, checkTitleValueStyle1, checkRow9)

            val checkValue8 = Array[String]("1", gerocomiumDetail(0)._3, gerocomiumDetail(0)._1, gerocomiumDetail(0)._4, gerocomiumDetail(0)._10, gerocomiumDetail(0)._11, gerocomiumDetail(0)._2, gerocomiumDetail(0)._8, gerocomiumDetail(0)._9, gerocomiumDetail(0)._5, gerocomiumDetail(0)._6, gerocomiumDetail(0)._7)
            val checkRow10 = checkSheet.createRow(11 + materailData(i)._2.size + 2 + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue8, checkTitleValueStyle1, checkRow10)

            //人员证件信息
            val supplierLicenseData = new MinzhengExcelStat().supplierLicense(hiveContext, gerocomiumDetail(i)._12)
            if (StringUtils.isEmpty(gerocomiumDetail(i)._12)) {
              val checkValue9 = Array[String]("②人员证照信息：健康证0个    A1证0个   B证0个   C证 0个   A2证0个")
              val checkRow11 = checkSheet.createRow(13 + materailData(i)._2.size + 2 + guanlaianIndex)
              new ExceltitleStat().exceltitle(checkValue9, checkTitleValueStyle, checkRow11)

              val checkValue11 = Array[String]("序号", "姓名", "证件类型", "证件号码", "发证日期", "有效日期", "证件状态", "关联团餐公司")
              val checkRow12 = checkSheet.createRow(14 + materailData(i)._2.size + 2 + guanlaianIndex)
              new ExceltitleStat().exceltitle(checkValue11, checkTitleValueStyle1, checkRow12)
            } else {

              val checkValue9 = Array[String]("②人员证照信息：健康证" + supplierLicenseData._2 + "个    A1证" + supplierLicenseData._3 + "个   B证" + supplierLicenseData._4 + "个   C证" + supplierLicenseData._5 + "个   A2证" + supplierLicenseData._6 + "个")
              val checkRow11 = checkSheet.createRow(13 + materailData(i)._2.size + 2 + guanlaianIndex)
              new ExceltitleStat().exceltitle(checkValue9, checkTitleValueStyle, checkRow11)

              val checkValue11 = Array[String]("序号", "姓名", "证件类型", "证件号码", "发证日期", "有效日期", "证件状态", "关联团餐公司")
              val checkRow12 = checkSheet.createRow(14 + materailData(i)._2.size + 2 + guanlaianIndex)
              new ExceltitleStat().exceltitle(checkValue11, checkTitleValueStyle1, checkRow12)

              new ExceltitleStat().excelcontent(supplierLicenseData._1, checkSheet, checkContentStyle, 15 + materailData(i)._2.size + 2 + guanlaianIndex, 0)
            }

            //团餐公司信息
            val checkValue12 = Array[String]("③团餐公司信息")
            val checkRow13 = checkSheet.createRow(17 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue12, checkTitleValueStyle, checkRow13)

            val checkValue13 = Array[String]("序号", "团餐公司", "服务起止时间", "统一社会信用代码证", "食品经营许可证", "食品经营许可证有效日期", "区", "地址", "法人代表", "联系人", "联系电话")
            val checkRow14 = checkSheet.createRow(18 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue13, checkTitleValueStyle1, checkRow14)

            val supplier_id = x._8
            val b2bSupplierLicense = new MinzhengExcelStat().b2bSupplierLicense(hiveContext, supplier_id)
            val checkRow15 = checkSheet.createRow(19 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(b2bSupplierLicense(0)._2, checkContentStyle, checkRow15)

            //供应商信息
            val checkValue14 = Array[String]("④供应商信息")
            val checkRow16 = checkSheet.createRow(21 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue14, checkTitleValueStyle, checkRow16)

            val supplyid = x._3
            val checkValue15 = Array[String]("序号", "供应商名称", "服务起止时间", "统一社会信用代码证", "食品经营许可证", "食品经营许可证有效日期", "区", "地址", "法人代表", "联系人", "联系电话")
            val checkRow17 = checkSheet.createRow(22 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue15, checkTitleValueStyle1, checkRow17)

            val b2bSupplyLicense = new MinzhengExcelStat().b2bSupplierLicense(hiveContext, supplier_id)
            val checkRow18 = checkSheet.createRow(23 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(b2bSupplyLicense(0)._2, checkContentStyle, checkRow18)

            //配送单
            val checkValue18 = Array[String]("⑤配送单明细")
            val checkRow19 = checkSheet.createRow(26 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue18, checkTitleValueStyle, checkRow19)

            val checkValue19 = Array[String](x._4 + "（1条）")
            val checkRow20 = checkSheet.createRow(28 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue19, checkTitleValueStyle, checkRow20)

            val checkValue20 = Array[String]("序号", "用料日期", "团餐公司", "收货日期", "验收日期", "配货批次号", "物料名称", "标准名称", "规格", "类别", "换算关系", "换算后数量", "批号", "生产日期", "保质期", "是否验收", "验收数量")
            val checkRow21 = checkSheet.createRow(29 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex)
            new ExceltitleStat().exceltitle(checkValue20, checkTitleValueStyle1, checkRow21)

            val arr1 = Array[(String, Array[String])](("1", x._9))
            new ExceltitleStat().excelcontent(arr1, checkSheet, checkContentStyle, 30 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + guanlaianIndex, 0)

            guanlaianIndex += 30 + materailData(i)._2.size + 2 + supplierLicenseData._1.size + x._9.size + 2
        })

        val schoolCount = schoolList.distinct.size
        val areaCount = areaList.distinct.size
        val checkValue2 = Array[String]("三、原料关联单位：" + schoolCount + "所" + "（含" + areaCount + "个区）")
        val checkRow3 = checkSheet.createRow(4)
        checkRow3.setHeightInPoints(20)
        new ExceltitleStat().exceltitle(checkValue2, checkTitleValueStyle, checkRow3)

        val checkValue3 = Array[String]("序号", "所在地", "单位", "地址", "法人代表", "联系人", "联系人电话", "供应商", "原料", "关联菜品")
        val checkRow4 = checkSheet.createRow(5)
        checkRow4.setHeightInPoints(20)
        new ExceltitleStat().exceltitle(checkValue3, checkTitleValueStyle1, checkRow4)

        val checkValue4 = Array[String]("四、原料关联单位信息详情")
        val checkRow6 = checkSheet.createRow(6 + materailData(i)._2.size + 2)
        checkRow6.setHeightInPoints(20)
        new ExceltitleStat().exceltitle(checkValue4, checkTitleValueStyle, checkRow6)

      }
    }
    val checkStream = new FileOutputStream(checkFile)
    checkWorkbook.write(checkStream)
    checkStream.close()

    fileSystem.moveFromLocalFile(new Path(localPath + checkFilename), new Path(hdfsPath + checkFilename))



//    //五级预警：排菜信息未上报汇总
//    val noPlatoonWarnCollectFilename = year + "年排菜信息未上报信息汇总日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
//    val noPlatoonWarnCollectFile = new File(localPath + noPlatoonWarnCollectFilename)
//    val noPlatoonWarnCollectSheetWidth = Array[Int](2500, 3500, 4500, 3500, 3500, 3500, 6500, 4500, 6500)
//    val noPlatoonWarnCollectWorkbook = new HSSFWorkbook()
//    val noPlatoonWarnCollectTitleStyle = new ExceltitleStat().style(noPlatoonWarnCollectWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//    val noPlatoonWarnCollectTitleValueStyle = new ExceltitleStat().style(noPlatoonWarnCollectWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//    val noPlatoonWarnCollectContentStyle = new ExceltitleStat().style(noPlatoonWarnCollectWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//
//    val noPlatoonWarnCollectSheet = noPlatoonWarnCollectWorkbook.createSheet("上海市排菜信息未上报信息汇总日报表")
//    new ExceltitleStat().sheetname(noPlatoonWarnCollectSheet, noPlatoonWarnCollectSheetWidth)
//
//    val noPlatoonWarnCollectCellRangeAddress = new CellRangeAddress(0, 0, 0, 8)
//    noPlatoonWarnCollectSheet.addMergedRegion(noPlatoonWarnCollectCellRangeAddress)
//    val noPlatoonWarnCollectTitle = Array[String]("排菜信息未上报信息汇总日报表 " + "         " + "日期：" + date)
//    val noPlatoonWarnCollectRow = noPlatoonWarnCollectSheet.createRow(0)
//    noPlatoonWarnCollectRow.setHeightInPoints(30)
//    new ExceltitleStat().exceltitle(noPlatoonWarnCollectTitle, noPlatoonWarnCollectTitleStyle, noPlatoonWarnCollectRow)
//
//    val noPlatoonWarnCollectTitleValue = Array[String]("序号", "主管部门", "单位名称", "截止时间14点（提示)", "截止时间16点（提醒）", "截止时间17点（预警）", "截止时间9点（督办）", "截止时间11点（追责）", "备注")
//    val noPlatoonWarnCollectRow1 = noPlatoonWarnCollectSheet.createRow(1)
//    noPlatoonWarnCollectRow1.setHeightInPoints(20)
//    new ExceltitleStat().exceltitle(noPlatoonWarnCollectTitleValue, noPlatoonWarnCollectTitleValueStyle, noPlatoonWarnCollectRow1)
//
//    val noPlatoonWarnCollectContent = new MinzhengExcelStat().noWarnCollect(hiveContext, year, month, date, "app_t_gerocomium_no_platoon_collect_d")
//    new ExceltitleStat().excelcontent(noPlatoonWarnCollectContent, noPlatoonWarnCollectSheet, noPlatoonWarnCollectContentStyle, 2, 0)
//
//    val noPlatoonWarnCollectStream = new FileOutputStream(noPlatoonWarnCollectFile)
//    noPlatoonWarnCollectWorkbook.write(noPlatoonWarnCollectStream)
//    noPlatoonWarnCollectStream.close()
//
//    fileSystem.moveFromLocalFile(new Path(localPath + noPlatoonWarnCollectFilename), new Path(wujiHdfsPath + noPlatoonWarnCollectFilename))
//
//
//    //五级预警：验收信息未上报汇总
//    val noLedgerWarnCollectFilename = year + "年验收信息未上报信息汇总日报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
//    val noLedgerWarnCollectFile = new File(localPath + noLedgerWarnCollectFilename)
//    val noLedgerWarnCollectSheetWidth = Array[Int](2500, 3500, 4500, 3500, 3500, 3500, 6500, 4500, 6500)
//    val noLedgerWarnCollectWorkbook = new HSSFWorkbook()
//    val noLedgerWarnCollectTitleStyle = new ExceltitleStat().style(noLedgerWarnCollectWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//    val noLedgerWarnCollectTitleValueStyle = new ExceltitleStat().style(noLedgerWarnCollectWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//    val noLedgerWarnCollectContentStyle = new ExceltitleStat().style(noLedgerWarnCollectWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//
//    val noLedgerWarnCollectSheet = noLedgerWarnCollectWorkbook.createSheet("上海市验收信息未上报信息汇总日报表")
//    new ExceltitleStat().sheetname(noLedgerWarnCollectSheet, noLedgerWarnCollectSheetWidth)
//
//    val noLedgerWarnCollectCellRangeAddress = new CellRangeAddress(0, 0, 0, 8)
//    noLedgerWarnCollectSheet.addMergedRegion(noLedgerWarnCollectCellRangeAddress)
//    val noLedgerWarnCollectTitle = Array[String]("验收信息未上报信息汇总日报表 " + "         " + "日期：" + date)
//    val noLedgerWarnCollectRow = noLedgerWarnCollectSheet.createRow(0)
//    noLedgerWarnCollectRow.setHeightInPoints(30)
//    new ExceltitleStat().exceltitle(noLedgerWarnCollectTitle, noLedgerWarnCollectTitleStyle, noLedgerWarnCollectRow)
//
//    val noLedgerWarnCollectTitleValue = Array[String]("序号", "主管部门", "单位名称", "截止时间14点（提示)", "截止时间16点（提醒）", "截止时间17点（预警）", "截止时间9点（督办）", "截止时间11点（追责）", "备注")
//    val noLedgerWarnCollectRow1 = noLedgerWarnCollectSheet.createRow(1)
//    noLedgerWarnCollectRow1.setHeightInPoints(20)
//    new ExceltitleStat().exceltitle(noLedgerWarnCollectTitleValue, noLedgerWarnCollectTitleValueStyle, noLedgerWarnCollectRow1)
//
//    val noLedgerWarnCollectContent = new MinzhengExcelStat().noWarnCollect(hiveContext, year, month, date, "app_t_gerocomium_no_ledger_collect_d")
//    new ExceltitleStat().excelcontent(noLedgerWarnCollectContent, noLedgerWarnCollectSheet, noLedgerWarnCollectContentStyle, 2, 0)
//
//    val noLedgerWarnCollectStream = new FileOutputStream(noLedgerWarnCollectFile)
//    noLedgerWarnCollectWorkbook.write(noLedgerWarnCollectStream)
//    noLedgerWarnCollectStream.close()
//
//    fileSystem.moveFromLocalFile(new Path(localPath + noLedgerWarnCollectFilename), new Path(wujiHdfsPath + noLedgerWarnCollectFilename))
//
//
//    //五级预警
//    val noWarnName = Array[String]("督办", "追责", "提示", "提醒", "预警")
//    val noWarnTime = Array[String]("05", "11", "14", "16", "17")
//    for (i <- 0 until noWarnName.size) {
//
//      //排菜信息未上报警报报表
//      val noPlatoonWarnFilename = year + "年排菜信息未上报" + noWarnName(i) + "报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
//      val noPlatoonWarnFile = new File(localPath + noPlatoonWarnFilename)
//      val noPlatoonWarnSheetWidth = Array[Int](2500, 3500, 5500, 4500, 3500)
//      val noPlatoonWarnWorkbook = new HSSFWorkbook()
//      val noPlatoonWarnTitleStyle = new ExceltitleStat().style(noPlatoonWarnWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//      val noPlatoonWarnTitleValueStyle = new ExceltitleStat().style(noPlatoonWarnWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//      val noPlatoonWarnContentStyle = new ExceltitleStat().style(noPlatoonWarnWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//
//      val noPlatoonWarnSheet = noPlatoonWarnWorkbook.createSheet("上海市排菜信息未上报" + noWarnName(i) + "报表")
//      new ExceltitleStat().sheetname(noPlatoonWarnSheet, noPlatoonWarnSheetWidth)
//
//      val noPlatoonWarnCellRangeAddress = new CellRangeAddress(0, 0, 0, 4)
//      noPlatoonWarnSheet.addMergedRegion(noPlatoonWarnCellRangeAddress)
//      val noPlatoonWarnTitle = Array[String]("排菜信息未上报" + noWarnName(i) + "报表" + "         " + "日期：" + date)
//      val noPlatoonWarnRow = noPlatoonWarnSheet.createRow(0)
//      noPlatoonWarnRow.setHeightInPoints(30)
//      new ExceltitleStat().exceltitle(noPlatoonWarnTitle, noPlatoonWarnTitleStyle, noPlatoonWarnRow)
//
//      val noPlatoonWarnTitleValue = Array[String]("序号", "主管部门", "单位名称", "截止时间", "备注")
//      val noPlatoonWarnRow1 = noPlatoonWarnSheet.createRow(1)
//      noPlatoonWarnRow1.setHeightInPoints(20)
//      new ExceltitleStat().exceltitle(noPlatoonWarnTitleValue, noPlatoonWarnTitleValueStyle, noPlatoonWarnRow1)
//
//      val noPlatoonWarnContent = new MinzhengExcelStat().noWarnDetail(hiveContext, year, month, date + " " + noWarnTime(i) + ":00:00", 6, 1)
//      new ExceltitleStat().excelcontent(noPlatoonWarnContent, noPlatoonWarnSheet, noPlatoonWarnContentStyle, 2, 0)
//
//      val noPlatoonWarnStream = new FileOutputStream(noPlatoonWarnFile)
//      noPlatoonWarnWorkbook.write(noPlatoonWarnStream)
//      noPlatoonWarnStream.close()
//
//      fileSystem.moveFromLocalFile(new Path(localPath + noPlatoonWarnFilename), new Path(wujiHdfsPath + noPlatoonWarnFilename))
//
//      //验收信息未上报警报报表
//      val noLedgerWarnFilename = year + "年验收信息未上报" + noWarnName(i) + "报表_上海市_" + datetime + "__" + datetime + "__" + date3 + "000000" + ".xls"
//      val noLedgerWarnFile = new File(localPath + noLedgerWarnFilename)
//      val noLedgerWarnSheetWidth = Array[Int](2500, 3500, 5500, 4500, 3500)
//      val noLedgerWarnWorkbook = new HSSFWorkbook()
//      val noLedgerWarnTitleStyle = new ExceltitleStat().style(noLedgerWarnWorkbook, 14, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//      val noLedgerWarnTitleValueStyle = new ExceltitleStat().style(noLedgerWarnWorkbook, 11, true, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//      val noLedgerWarnContentStyle = new ExceltitleStat().style(noLedgerWarnWorkbook, 14, false, "宋体", HorizontalAlignment.CENTER, BorderStyle.THIN)
//
//      val noLedgerWarnSheet = noLedgerWarnWorkbook.createSheet("上海市验收信息未上报" + noWarnName(i) + "报表")
//      new ExceltitleStat().sheetname(noLedgerWarnSheet, noLedgerWarnSheetWidth)
//
//      val noLedgerWarnCellRangeAddress = new CellRangeAddress(0, 0, 0, 4)
//      noLedgerWarnSheet.addMergedRegion(noLedgerWarnCellRangeAddress)
//      val noLedgerWarnTitle = Array[String]("验收信息未上报" + noWarnName(i) + "报表" + "         " + "日期：" + date)
//      val noLedgerWarnRow = noLedgerWarnSheet.createRow(0)
//      noLedgerWarnRow.setHeightInPoints(30)
//      new ExceltitleStat().exceltitle(noLedgerWarnTitle, noLedgerWarnTitleStyle, noLedgerWarnRow)
//
//      val noLedgerWarnTitleValue = Array[String]("序号", "主管部门", "单位名称", "截止时间", "备注")
//      val noLedgerWarnRow1 = noLedgerWarnSheet.createRow(1)
//      noLedgerWarnRow1.setHeightInPoints(20)
//      new ExceltitleStat().exceltitle(noLedgerWarnTitleValue, noLedgerWarnTitleValueStyle, noLedgerWarnRow1)
//
//      val noLedgerWarnContent = new MinzhengExcelStat().noWarnDetail(hiveContext, year, month, date + " " + noWarnTime(i) + ":00:00", 6, 2)
//      new ExceltitleStat().excelcontent(noLedgerWarnContent, noLedgerWarnSheet, noLedgerWarnContentStyle, 2, 0)
//
//      val noLedgerWarnStream = new FileOutputStream(noLedgerWarnFile)
//      noLedgerWarnWorkbook.write(noLedgerWarnStream)
//      noLedgerWarnStream.close()
//
//      fileSystem.moveFromLocalFile(new Path(localPath + noLedgerWarnFilename), new Path(wujiHdfsPath + noLedgerWarnFilename))
//
//    }


    fileSystem.close()
    sc.stop()
  }

}
