package com.ssic.service

import org.apache.poi.hssf.usermodel.{HSSFCellStyle, HSSFRow, HSSFSheet, HSSFWorkbook}
import org.apache.poi.ss.formula.functions.T
import org.apache.poi.ss.usermodel.{BorderStyle, HorizontalAlignment}
import org.apache.poi.ss.util.CellRangeAddress


class ExceltitleStat {

  /**
    *
    * * 表头格式
    * * @param Array[String] 表头名
    * * @param HSSFCellStyle  字体格式
    * * @param HSSFRow 行
    */
  def exceltitle(array: Array[String], hSSFCellStyle: HSSFCellStyle, hSSFRow: HSSFRow): Unit = {
    for (i <- 0 until array.length) {
      val cell = hSSFRow.createCell(i)
      cell.setCellValue(array(i).toString)
      cell.setCellStyle(hSSFCellStyle)
    }
  }
  /**
    *
    * * 表头格式
    * * @param Array[String] 表头名
    * * @param HSSFCellStyle  字体格式
    * * @param HSSFRow 行
    *   @param cells 列
    *
    */
  def exceltitleOtherStat(array: Array[String], hSSFCellStyle: HSSFCellStyle, hSSFRow: HSSFRow,cells:Int):Unit={
    for (i <- 0 until array.length) {
      val cell = hSSFRow.createCell(i + cells)
      cell.setCellValue(array(i).toString)
      cell.setCellStyle(hSSFCellStyle)
    }
  }

  /**
    * * 表中内容
    * * @param Array[(String,Array[String])] 表内容
    * * @param HSSFSheet 表
    * * @param HSSFCellStyle  字体格式
    * * @param int 表内容需要加的行数
    * * @param orderNum 表自增序号的列
    */

  def excelcontent(array: Array[(String, Array[String])], hSSFSheet: HSSFSheet, hSSFCellStyle: HSSFCellStyle, int: Int, orderNum: Int): Unit = {
    for (i <- 0 until array.length) {
      val row = hSSFSheet.createRow(i + int)
      val data = array(i)._2
      for (j <- 0 until data.length) {
        if (j == orderNum) {
          val cell = row.createCell(j)
          cell.setCellValue((i + 1).toString)
          cell.setCellStyle(hSSFCellStyle)
        } else {
          val cell = row.createCell(j)
          cell.setCellValue(data(j))
          cell.setCellStyle(hSSFCellStyle)
        }

      }
    }
  }

  /**
   *
   * * 对标中的内容进行合并
   * * @param Array[(String,Array[String])] 表内容
   * * @param HSSFSheet 表
   * * @param HSSFCellStyle  字体格式
   * * @param int 表内容需要加的行数 这里没什么用都是加一行
   * * @param orderNum 表自增序号的列
   */
  def excelcontentMerge(array: Array[(Array[String])], hSSFSheet: HSSFSheet, hSSFCellStyle: HSSFCellStyle, int: Int, orderNum: Int): Unit = {

    for (i <- 0 until array.length) {
      val row = hSSFSheet.createRow(i + 1)
      val data = array(i)

      for (j <- 0 until data.length) {

        val oldConten: String = array(i)(j)
        val newConten: String = array(i + 1)(j)
        if (newConten==oldConten) {
          val region = new CellRangeAddress(i, i + 1, 1, 1)
          hSSFSheet.addMergedRegion(region)
        }
        if (j == orderNum) {
          val cell = row.createCell(j)
          cell.setCellValue((i + 1).toString)
          cell.setCellStyle(hSSFCellStyle)
        } else {
          val cell = row.createCell(j)
          cell.setCellValue(data(j))
          cell.setCellStyle(hSSFCellStyle)
        }
      }
    }
  }

  /**
    *
    * * 创建sheet
    * * @param HSSFSheet
    * * @param  Array[Int]  列宽
    */
  def sheetname(hSSFSheet: HSSFSheet, width: Array[Int]): Unit = {
    for (i <- 0 until width.length) {
      hSSFSheet.setColumnWidth(i, width(i))
    }
  }

  /**
    *
    * * 创建格式
    * * @param HSSFWorkbook
    * * @param  Short  字体大小
    * * @param Boolean 是否加粗
    * * @param String 字体类型
    * * @param HorizontalAlignment 是否居中
    * * @param BorderStyle 边框线
    */
  def style(workbook: HSSFWorkbook, fontheight: Short, boo: Boolean, footname: String, align: HorizontalAlignment, border: BorderStyle): HSSFCellStyle = {
    val style = workbook.createCellStyle()
    val font = workbook.createFont()
    font.setFontHeightInPoints(fontheight)
    font.setBold(boo)
    font.setFontName(footname)
    style.setFont(font)
    style.setAlignment(align)
    style.setBorderBottom(border)
    style.setBorderLeft(border)
    style.setBorderRight(border)
    style.setBorderTop(border)

    return style
  }



}
