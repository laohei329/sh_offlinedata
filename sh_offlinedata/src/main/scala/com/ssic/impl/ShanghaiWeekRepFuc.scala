package com.ssic.impl

import jxl.write.WritableSheet
import org.apache.poi.hssf.usermodel.{HSSFSheet, HSSFWorkbook}
import org.apache.spark.rdd.RDD

trait ShanghaiWeekRepFuc {

  //各管理部门排菜统计
  def areaplatoonweek(data:(HSSFSheet,Array[(String, String, String, String, String, String, String, Int, Int, Int)]))

  //各区验收统计（按区排序，再按学校分类排序）
  def arealedgerweek(data:(HSSFSheet,Array[(String, String, String, String, String, String, String, Int, Int, Int)]))

  //上海市统计
  def shangtotal(data:(HSSFSheet,RDD[(String, String, String, String, String, String, String, Int, Int, Int)],HSSFWorkbook,RDD[(String, String, String, String, String, String, String, Int, Int, Int)],String,String,RDD[(String, String, String, String, String, String, String, Integer, String, String, Int, String, Integer, String, String, String)],String,String))

  //各区证照预警统计
  def arealicensewarnweek(data:(HSSFSheet,Array[(String, String, String, String, String, String, String, Integer, String, String, Int, String, Integer, String, String, String)]))

  //各区统计
  def areatotal(data:(HSSFSheet,RDD[(String, String, String, String, String, String, String, Int, Int, Int)],HSSFWorkbook,RDD[(String, String, String, String, String, String, String, Int, Int, Int)],String,String,RDD[(String, String, String, String, String, String, String, Integer, String, String, Int, String, Integer, String, String, String)],String,String))

}
