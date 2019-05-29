package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait RetentionTotalFunc {

  //各区留样计划总数据统计
  def arearetentiontotal(data:(RDD[(String, String,String)],String))

  //各区留样计划各状态数据统计
  def arearetentionstatustotal(data:(RDD[(String, String,String)],String,RDD[(String, String)]))

  //各区留样计划各状态按照学校数据统计(对学校进行去重处理)
  def arearetentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按所属教育部留样计划各状态数据统计
  def masteridretentiontotal(data:(RDD[(String, String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
  def masteridretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按上海市办学性质来留样计划各状态数据统计（nature）
  def natureretentionstatus(data:(RDD[(String, String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //各区办学性质来留样计划各状态数据统计（nature）
  def areanatureretentionstatus(data:(RDD[(String, String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def natureretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def areanatureretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来留样计划各状态数据统计（level）
  def levelreretentionstatus(data:(RDD[(String, String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来留样计划各状态数据统计（level）
  def arealevelreretentionstatus(data:(RDD[(String, String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
  def levelreretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
  def arealevelreretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))



}
