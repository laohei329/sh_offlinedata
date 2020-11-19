package com.ssic.utils



import com.ssic.bean.PoiModel
import com.sun.webkit.perf.WCFontPerfLogger.log
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hssf.util.HSSFColor
import org.apache.poi.ss.usermodel._
import org.apache.poi.ss.util.{CellRangeAddress, CellUtil}

import scala.util.control.Breaks.{break, breakable}

object ExportExcelByPoiUtil {


  /**
   *
   * @param title      传入的表头列表信息
   * @param widthAttr  没列宽度
   * @param maps       表名映射的数据信息   表名-> list
   * @param mergeIndex 需要合并的列 如果不需要合并传入 一个空的array的数组即可
   * @param workbook   传入工作环境
   */
  def createExcel(title: Array[String], widthAttr: Array[Int],
                  maps: Map[String /*sheet名*/ , List[Map[String /*对应title的值*/ , String]]],
                  mergeIndex: Array[Int], workbook: Workbook) = {
    if (title.length == 0) null

    var sheet: Sheet = null
    for (elem <- maps) {
      try {
        sheet = workbook.createSheet(elem._1)
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

      /*初始化标题，填值标题行（第一行）*/

      //todo 从第三行还是创建row和cell 是数据的第一行不会出现合并操作
      var row2 = sheet.createRow(0);
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
        var index = 1
        while (iterator.hasNext) {

          val row = sheet.createRow(index)
          val map: Map[String, String] = iterator.next()
          //循环列数
          for (i <- 0 until title.length) {
            if (index == 1) {
              poiModels = poiModels :+ PoiModel(map.getOrElse(title(i),""), map.getOrElse(title(i),""), index, i)
              //  poiModels = poiModels :+ PoiModel(map.getOrElse(title(i),""), map.getOrElse(title(i),""), index, i)
              breakable(
                break()
              )
            }
            for (j <- mergeIndex) {

              if (j > 0 && i == j) {

                if (!poiModels(i - 1).oldContent.equals(map.getOrElse(title(i - 1),""))) {

                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  }
                } else if (!poiModels(i).content.equals(map.getOrElse(title(i),""))) {
                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  }
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, poiModels(i).rowIndex, i))
                }
              }
              //第一列 上下两行数据不一样是时
              if (j == 0 && i == j) {
                if (!poiModels(i).content.equals(map.getOrElse(title(i),""))) {
                  sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                  poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, poiModels(i).rowIndex, i))
                }

              }
              //最后几行处理
              if (i == j && index == list.size) {
                if (poiModels(i).rowIndex != index) {
                  val cra = new CellRangeAddress(poiModels(i).rowIndex /*从第二行开始*/ , index /*到第几行*/ , poiModels(i).cellIndex /*从某一列开始*/ , poiModels(i).cellIndex /*到第几列*/)
                  //在sheet里增加合并单元格
                  sheet.addMergedRegion(cra)
                  val poiModel: PoiModel = PoiModel(map.getOrElse(title(i),""), map.getOrElse(title(i),""), index, i)
                  poiModels = poiModels.updated(i, poiModel)
                }

              }
            }

            sheet.setColumnWidth(i, 4000)
            val cell: Cell = row.createCell(i, CellType.STRING)
            cell.setCellValue(map.getOrElse(title(i),""))
            cell.setCellStyle(cellStyle)
          }
          index += 1
        }
      }
    }
  }

  /***
   *
   * @param title
   * @param widthAttr
   * @param maps
   * @param mergeIndex
   * @param workbook
   * @param startIndex  从第几行开始建表  包含表头内容
   */
  def createExcel(title: Array[String], widthAttr: Array[Int],
                  maps: Map[String /*sheet名*/ , List[Map[String /*对应title的值*/ , String]]],
                  mergeIndex: Array[Int], workbook: Workbook,startIndex:Int) = {
    if (title.length == 0) null

    var sheet: Sheet = null
    for (elem <- maps) {
      try {
        sheet = workbook.createSheet(elem._1)
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

      /*初始化标题，填值标题行（第一行）*/

      //todo 从第三行还是创建row和cell 是数据的第一行不会出现合并操作
      var row2 = sheet.createRow(startIndex);
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
        var index = startIndex+1
        while (iterator.hasNext) {

          val row = sheet.createRow(index)
          val map: Map[String, String] = iterator.next()
          //循环列数
          for (i <- 0 until title.length) {
            if (index == startIndex+1) {
              poiModels = poiModels :+ PoiModel(map.getOrElse(title(i),""), map.getOrElse(title(i),""), index, i)
              breakable(
                break()
              )
            }
            for (j <- mergeIndex) {

              if (j > 0 && i == j) {

                if (!poiModels(i - 1).oldContent.equals(map.getOrElse(title(i - 1),""))) {

                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  }
                } else if (!poiModels(i).content.equals(map.getOrElse(title(i),""))) {
                  if (poiModels(i).rowIndex != index - 1) {
                    sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  } else {
                    poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                  }
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, poiModels(i).rowIndex, i))
                }
              }
              //第一列 上下两行数据不一样是时
              if (j == 0 && i == j) {
                if (!poiModels(i).content.equals(map.getOrElse(title(i),""))) {
                  sheet.addMergedRegion(new CellRangeAddress(poiModels(i).rowIndex, index - 1, poiModels(i).cellIndex, poiModels(i).cellIndex))
                  poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, index, i))
                } else {
                  poiModels = poiModels.updated(i, PoiModel(map.getOrElse(title(i),""), poiModels(i).content, poiModels(i).rowIndex, i))
                }

              }
              //最后几行处理
              if (i == j && index == list.size) {
                if (poiModels(i).rowIndex != index) {
                  val cra = new CellRangeAddress(poiModels(i).rowIndex /*从第二行开始*/ , index /*到第几行*/ , poiModels(i).cellIndex /*从某一列开始*/ , poiModels(i).cellIndex /*到第几列*/)
                  //在sheet里增加合并单元格
                  sheet.addMergedRegion(cra)
                  val poiModel: PoiModel = PoiModel(map.getOrElse(title(i),""), map.getOrElse(title(i),""), index, i)
                  poiModels = poiModels.updated(i, poiModel)
                }

              }
            }

            sheet.setColumnWidth(i, 4000)
            val cell: Cell = row.createCell(i, CellType.STRING)
            cell.setCellValue(map.getOrElse(title(i),""))
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
