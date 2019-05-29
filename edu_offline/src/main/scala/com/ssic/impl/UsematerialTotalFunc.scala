package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait UsematerialTotalFunc {

  //各区用料计划总数据统计
  def areausematerialtotal(data:(RDD[(String, String,String,String,String,String)],String))

  //各区用料计划各状态数据统计
  def areausmaterialstatustotal(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)]))

  //各区用料计划各状态按照学校数据统计(对学校进行去重处理)
  def areausmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按所属教育部用料计划各状态数据统计
  def masteridusematerialtotal(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
  def masteridusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按上海市办学性质来用料计划各状态数据统计（nature）
  def natureusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //各区办学性质来用料计划各状态数据统计（nature）
  def areanatureusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def natureusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def areanatureusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来用料计划各状态数据统计（level）
  def levelusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来用料计划各状态数据统计（level）
  def arealevelusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
  def levelusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
  def arealevelusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))
}
