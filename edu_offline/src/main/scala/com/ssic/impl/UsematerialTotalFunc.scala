package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait UsematerialTotalFunc {

  //各区用料计划总数据统计
  def areausematerialtotal(data:(RDD[(String, String,String,String,String,String)],String))

  //按照权限管理部门各区用料计划总数据统计
  def departmentareausematerialtotal(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区用料计划各状态数据统计
  def areausmaterialstatustotal(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)]))

  //按照权限管理部门各区用料计划各状态数据统计
  def departmentareausmaterialstatustotal(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区用料计划各状态按照学校数据统计(对学校进行去重处理)
  def areausmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按照权限管理部门各区用料计划各状态按照学校数据统计(对学校进行去重处理)
  def departmentareausmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按所属教育部用料计划各状态数据统计
  def masteridusematerialtotal(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限管理部门所属教育部用料计划各状态数据统计
  def departmentmasteridusematerialtotal(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))


  //按所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
  def masteridusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限管理部门所属教育局料计划各状态按照学校数据统计(对学校进行去重处理)
  def departmentmasteridusmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按上海市办学性质来用料计划各状态数据统计（nature）
  def natureusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门办学性质来用料计划各状态数据统计（nature）
  def departmentnatureusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区办学性质来用料计划各状态数据统计（nature）
  def areanatureusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门各区办学性质来用料计划各状态数据统计（nature）
  def departmentareanatureusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def natureusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def departmentnatureusmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def areanatureusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门各区办学性质来用料计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def departmentareanatureusmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来用料计划各状态数据统计（level）
  def levelusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门各类型学校来用料计划各状态数据统计（level）
  def departmentlevelusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来用料计划各状态数据统计（level）
  def arealevelusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门各区各类型学校来用料计划各状态数据统计（level）
  def departmentarealevelusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
  def levelusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
  def departmentlevelusmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
  def arealevelusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限管理部门各区各类型学校来用料计划各状态按照学校数据统计（level,对学校进行去重处理）
  def departmentarealevelusmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按照各管理部门各状态用料计划数量
  def departmentusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限各管理部门各状态用料计划数量
  def departmentdepartmentusmaterialstatus(data:(RDD[(String, String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按照各管理部门各状态用料计划数量(对学校去重了)
  def departmentusmaterialschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限各管理部门各状态用料计划数量(对学校去重了)
  def departmentdepartmentusmaterialschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))


}
