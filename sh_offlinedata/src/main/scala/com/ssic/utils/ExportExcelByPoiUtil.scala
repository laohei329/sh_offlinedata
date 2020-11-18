package com.ssic.utils

import java.io.{FileOutputStream, IOException}

import com.ssic.bean.PoiModel
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.{CellRangeAddress, CellUtil}

import scala.util.control.Breaks.{break, breakable}

object ExportExcelByPoiUtil {


  def createExcelMerge(title: Array[String], widthAttr: Array[Int],
                       maps: Map[String /*sheet名*/ , List[Map[String /*对应title的值*/ , String]]],
                       mergeIndex: Array[Int]) = {
    if (title.length == 0) null

    val workbook: Workbook = new HSSFWorkbook()
    var sheet: Sheet = null
    //记录统一张表中sheet 的数量
    var n: Int = 0
    //elem : (String,List[Map[String,String]])  sheet名 , 数据集合
    for (elem <- maps) {
      try {
        sheet = workbook.createSheet()

        workbook.setSheetName(n, elem._1)

        workbook.setSelectedTab(n)
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }

      // 设置样式 头 cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
      // 水平方向的对齐方式
      val cellStyle_head: CellStyle = style(0, workbook)
      // 导出时间
      val cellStyle_export: CellStyle = style(3, workbook)
      // 标题
      val cellStyle_title: CellStyle = style(1, workbook)
      // 正文
      val cellStyle: CellStyle = style(2, workbook)
      // 合并单元格CellRangeAddress构造参数依次表示起始行，截至行，起始列， 截至列

      /*第二行也是合并*/


      val c2 = new CellRangeAddress(0, 1, 0, 3)
      sheet.addMergedRegion(c2)
      val c3 = new CellRangeAddress(0, 1, 4, 15)
      sheet.addMergedRegion(c3)
      setRegionStyle(sheet, c2, cellStyle_title)
      setRegionStyle(sheet, c3, cellStyle_title)
      var row1 = sheet.createRow(0);
      for (i <- 0 until widthAttr.length) {

        val cell: Cell = row1.createCell(i, CellType.STRING)
        if (i < 4) {
          cell.setCellValue("按性质统计")
        } else {
          cell.setCellValue("按学制统计")
        }

        cell.setCellStyle(cellStyle)
      }

      /*初始化标题，填值标题行（第一行）*/

      //todo 从第三行还是创建row和cell 是数据的第一行不会出现合并操作
      var row2 = sheet.createRow(2);
      //设置这一行的每个单元格的格式
      for (i <- 0 until widthAttr.length) {
        /*创建单元格，指定类型*/
        val cell_1: Cell = row2.createCell(i, CellType.STRING)
        //设置标题的值
        cell_1.setCellValue(title(i))
        //设置标题样式
        cell_1.setCellStyle(cellStyle_title)
      }

      /*得到当前sheet下的数据集合*/
      val list: List[Map[String, String]] = elem._2
      //todo 这个数组是存储需要合并的信息
      var poiModels: List[PoiModel] = List()
      if (null != workbook) {
        val iterator: Iterator[Map[String, String]] = list.toIterator
        /* 因为标题行前还有2行  所以index从3开始    也就是第四行 表格数据的第二行*/
        var index = 3
        while (iterator.hasNext) {

          val row = sheet.createRow(index)
          val map: Map[String, String] = iterator.next()
          //循环列数
          for (i <- 0 until title.length) {
            if (index == 3) {
              poiModels = poiModels :+ PoiModel(map.get(title(i)).getOrElse(""), map.get(title(i)).getOrElse(""), index, i)
              breakable(
                break()
              )
            }
            for (j <- mergeIndex) {

              if (j > 0 && i == j) {

                if (!poiModels(i - 1).oldContent.equals(map.get(title(i)).getOrElse(""))) {

                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  }
                } else if (!poiModels(i).content.equals(map.get(title(i)).getOrElse(""))) {
                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  }
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, poiModels(i).rowIndex, i))
                }
              }
              //第一列 上下两行数据不一样是时
              if (j == 0 && i == j) {
                if (!poiModels(i).content.equals(map.get(title(i)).getOrElse(""))) {
                  sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                  poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, poiModels(i).rowIndex, i))
                }

              }

              //最后几行处理
              if (i == j && index == list.size + 2) {
                if (poiModels(i).rowIndex != index) {
                  val cra = new CellRangeAddress(poiModels(i).rowIndex /*从第二行开始*/ , index /*到第几行*/ , poiModels(i).cellIndex /*从某一列开始*/ , poiModels(i).cellIndex /*到第几列*/)
                  //在sheet里增加合并单元格
                  sheet.addMergedRegion(cra)
                  val poiModel: PoiModel = PoiModel(map.get(title(i)).getOrElse(""), map.get(title(i)).getOrElse(""), index, i)
                  poiModels = poiModels.updated(i, poiModel)
                }


              }

            }


            sheet.setColumnWidth(i, 4000)
            val cell: Cell = row.createCell(i, CellType.STRING)
            cell.setCellValue(map.get(title(i)).getOrElse(""))
            cell.setCellStyle(cellStyle)

          }
          index += 1
        }
      }

    }

  }




  /**
   *
   * @param title   传入的title数组
   * @param widthAttr  设置的每列的宽度设置
   * @param maps
   * @param mergeIndex
   */
  def createExcelMerge2(title: Array[String], widthAttr: Array[Int],
                        maps: Map[String /*sheet名*/ , List[Map[String /*对应title的值*/ , String]]],
                        mergeIndex: Array[Int]) = {
    if (title.length == 0) null

    val workbook: Workbook = new HSSFWorkbook()
    var sheet: Sheet = null
    //记录统一张表中sheet 的数量
    var n: Int = 0
    //elem : (String,List[Map[String,String]])  sheet名 , 数据集合
    for (elem <- maps) {
      try {
        sheet = workbook.createSheet()

        workbook.setSheetName(n, elem._1)

        workbook.setSelectedTab(n)
      } catch {
        case e: Exception =>
          e.printStackTrace()
      }

      // 设置样式 头 cellStyle.setAlignment(HSSFCellStyle.ALIGN_LEFT);
      // 水平方向的对齐方式
      val cellStyle_head: CellStyle = style(0, workbook)
      // 导出时间
      val cellStyle_export: CellStyle = style(3, workbook)
      // 标题
      val cellStyle_title: CellStyle = style(1, workbook)
      // 正文
      val cellStyle: CellStyle = style(2, workbook)
      // 合并单元格CellRangeAddress构造参数依次表示起始行，截至行，起始列， 截至列

      /*第二行也是合并*/


      val c2 = new CellRangeAddress(0, 1, 0, 15)
      sheet.addMergedRegion(c2)
      setRegionStyle(sheet, c2, cellStyle_title)

      var row1 = sheet.createRow(0);
      for (i <- 0 until widthAttr.length) {

        val cell: Cell = row1.createCell(i, CellType.STRING)

          cell.setCellValue("按学制进行统计统计")

        cell.setCellStyle(cellStyle)
      }

      /*初始化标题，填值标题行（第一行）*/

      //todo 从第三行还是创建row和cell 是数据的第一行不会出现合并操作
      var row2 = sheet.createRow(2);
      //设置这一行的每个单元格的格式
      for (i <- 0 until widthAttr.length) {
        /*创建单元格，指定类型*/
        val cell_1: Cell = row2.createCell(i, CellType.STRING)
        //设置标题的值
        cell_1.setCellValue(title(i))
        //设置标题样式
        cell_1.setCellStyle(cellStyle_title)
      }

      /*得到当前sheet下的数据集合*/
      val list: List[Map[String, String]] = elem._2
      //todo 这个数组是存储需要合并的信息
      var poiModels: List[PoiModel] = List()
      if (null != workbook) {
        val iterator: Iterator[Map[String, String]] = list.toIterator
        /* 因为标题行前还有2行  所以index从3开始    也就是第四行 表格数据的第二行*/
        var index = 3
        while (iterator.hasNext) {

          val row = sheet.createRow(index)
          val map: Map[String, String] = iterator.next()
          //循环列数
          for (i <- 0 until title.length) {
            if (index == 3) {
              poiModels = poiModels :+ PoiModel(map.get(title(i)).getOrElse(""), map.get(title(i)).getOrElse(""), index, i)
            }
            for (j <- mergeIndex) {

              if (j > 0 && i == j) {

                if (!poiModels(i - 1).oldContent.equals(map.get(title(i - 1)).getOrElse(""))) {

                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  }
                } else if (!poiModels(i).content.equals(map.get(title(i)).getOrElse(""))) {
                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                  }
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, poiModels(i).rowIndex, i))
                }
              }
              //第一列 上下两行数据不一样是时
              if (j == 0 && i == j) {
                if (!poiModels(i).content.equals(map.get(title(i)).getOrElse(""))) {
                  sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                  poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, index, i))
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.get(title(i)).getOrElse(""), poiModels(i).content, poiModels(i).rowIndex, i))
                }

              }

              //最后几行处理
              if (i == j && index == list.size + 2) {
                if (poiModels(i).rowIndex != index) {
                  val cra = new CellRangeAddress(poiModels(i).rowIndex /*从第二行开始*/ , index /*到第几行*/ , poiModels(i).cellIndex /*从某一列开始*/ , poiModels(i).cellIndex /*到第几列*/)
                  //在sheet里增加合并单元格
                  sheet.addMergedRegion(cra)
                  val poiModel: PoiModel = PoiModel(map.get(title(i)).getOrElse(""), map.get(title(i)).getOrElse(""), index, i)
                  poiModels = poiModels.updated(i, poiModel)
                }


              }

            }


            sheet.setColumnWidth(i, 4000)
            val cell: Cell = row.createCell(i, CellType.STRING)
            cell.setCellValue(map.get(title(i)).getOrElse(""))
            cell.setCellStyle(cellStyle)

          }
          index += 1
        }
      }

    }

  }


  /**
   * 单元格的字体颜色等格式
   *
   * @param index
   * @param workbook
   * @return
   */
  def style(index: Int, workbook: Workbook) = {
    var cellStyle: CellStyle = null
    if (index == 0) {

      // 设置头部样式
      cellStyle = workbook.createCellStyle

      // 设置字体大小 位置
      cellStyle.setAlignment(HorizontalAlignment.CENTER)

      // 生成一个字体
      val font = workbook.createFont

      //设置字体
      font.setFontName("微软雅黑")
      //字体颜色
      font.setColor(HSSFColor.BLACK.index) // HSSFColor.VIOLET.index
      font.setFontHeightInPoints(12.toShort)
      //      font.setBold();
      font.setBold(true) // 字体增粗
      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER) // 上下居中
      cellStyle.setFillForegroundColor(HSSFColor.GREEN.index) //背景白色
      cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
      cellStyle.setBorderBottom(BorderStyle.THIN)
      cellStyle.setBorderLeft(BorderStyle.THIN)
      cellStyle.setBorderRight(BorderStyle.THIN)
      cellStyle.setBorderTop(BorderStyle.THIN)
      cellStyle.setAlignment(HorizontalAlignment.CENTER)

    }
    //标题
    if (index == 1) {
      cellStyle = workbook.createCellStyle
      // 设置字体大小 位置
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      // 生成一个字体
      val font_title = workbook.createFont
      //设置字体
      font_title.setFontName("微软雅黑")
      font_title.setColor(HSSFColor.BLACK.index) // HSSFColor.VIOLET.index

      //字体颜色
      font_title.setFontHeightInPoints(10.toShort)
      font_title.setBold(true) // 字体增粗

      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER) // 上下居中
      cellStyle.setFillForegroundColor(HSSFColor.BLUE_GREY.index) //背景白色
      cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
      cellStyle.setBorderBottom(BorderStyle.THIN)
      cellStyle.setBorderLeft(BorderStyle.THIN)
      cellStyle.setBorderRight(BorderStyle.THIN)
      cellStyle.setWrapText(true) // 自动换行

      cellStyle.setBorderTop(BorderStyle.THIN)
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      cellStyle.setFont(font_title)

    }
    if (index == 2) {
      cellStyle = workbook.createCellStyle
      // 设置字体大小 位置
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      // 生成一个字体// 生成一个字体

      val font_title = workbook.createFont
      //设置字体
      font_title.setFontName("微软雅黑")
      cellStyle.setWrapText(true) // 自动换行
      cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND)
      cellStyle.setFillForegroundColor(HSSFColor.WHITE.index) //背景白色

      cellStyle.setBorderBottom(BorderStyle.THIN)
      cellStyle.setBorderLeft(BorderStyle.THIN)
      cellStyle.setBorderRight(BorderStyle.THIN)
      cellStyle.setBorderTop(BorderStyle.THIN)
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER) // 上下居中
      cellStyle.setFont(font_title)
    }
    //时间
    if (index == 3) {
      cellStyle = workbook.createCellStyle
      // 设置字体大小 位置
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      // 生成一个字体// 生成一个字体
      val font_title = workbook.createFont
      //设置字体
      font_title.setFontName("微软雅黑")
      font_title.setColor(HSSFColor.BLACK.index) // HSSFColor.VIOLET.index
      font_title.setFontHeightInPoints(10.toShort)
      font_title.setBold(false) // 字体增粗
      cellStyle.setBorderBottom(BorderStyle.THIN)
      cellStyle.setBorderLeft(BorderStyle.THIN)
      cellStyle.setBorderRight(BorderStyle.THIN)
      cellStyle.setBorderTop(BorderStyle.THIN)
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER) // 上下居中
      cellStyle.setFont(font_title)
    }

    if (index == 4) {
      // 设置样式
      cellStyle = workbook.createCellStyle
      // 居中
      // cellStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
      cellStyle.setBorderBottom(BorderStyle.THIN)
      cellStyle.setBorderLeft(BorderStyle.THIN)
      cellStyle.setBorderRight(BorderStyle.THIN)
      cellStyle.setBorderTop(BorderStyle.THIN)
      cellStyle.setAlignment(HorizontalAlignment.CENTER)
      cellStyle.setVerticalAlignment(VerticalAlignment.CENTER) // 上下居中
    }
    cellStyle

  }


  /**
   *
   * @param sheet
   * @param region
   * @param cs 设定文件 文件的类型
   */
  def setRegionStyle(sheet: Sheet, region: CellRangeAddress, cs: CellStyle): Unit = {
    val firstColumn: Int = region.getFirstColumn
    val laseColum: Int = region.getLastColumn
    for (i <- firstColumn to laseColum) {
      val row: Row = CellUtil.getRow(i, sheet)
      for (j <- region.getFirstColumn to region.getLastColumn) {
        val cell: Cell = CellUtil.getCell(row, j)
        cell.setCellStyle(cs);
      }
    }
  }

}
