package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait DistributionTotalFunc {

  //各区配送计划总数据统计
  def areadistributionltotal(data:(RDD[(String, String,String,String,String,String)],String))

  //各区配送计划各状态数据统计
  def areadistributionstatustotal (data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)]))

  //各区配送计划各状态按照学校数据统计(对学校进行去重处理)
  def areadistributionschoolstatus (data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按所属教育部配送计划各状态数据统计
  def masteriddistributiontotal (data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
  def masteriddistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按上海市办学性质来配送计划各状态数据统计（nature）
  def naturedistributionstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //各区办学性质来配送计划各状态数据统计（nature）
  def areanaturedistributionstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def naturedistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def areanaturedistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来配送计划各状态数据统计（level）
  def leveldistributionstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来配送计划各状态数据统计（level）
  def arealeveldistributionstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
  def leveldistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
  def arealeveldistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

}
