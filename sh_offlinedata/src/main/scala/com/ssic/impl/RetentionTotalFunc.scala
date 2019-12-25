package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait RetentionTotalFunc {

  //各区留样计划总数据统计
  def arearetentiontotal(data:(RDD[(String, String,String,String)],String))

  //按照管理权限各区留样计划统计数据
  def departmentarearetentiontotal(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区留样计划各状态数据统计
  def arearetentionstatustotal(data:(RDD[(String, String,String,String)],String,RDD[(String, String)]))

  //按照权限各区留样计划各状态数据统计
  def departmentarearetentionstatustotal(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区留样操作规则数据统计
  def arearetentionrulestatustotal(data:(RDD[(String, String,String,String)],String,RDD[(String, String)]))

  //按照权限的各区留样操作规则数据统计
  def departmentarearetentionrulestatustotal(data:(RDD[(String, String,String,String)],String, Broadcast[Map[String, List[String]]]))

  //各区留样计划各状态按照学校数据统计(对学校进行去重处理)
  def arearetentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按照权限各区留样计划各状态按照学校数据统计(对学校进行去重处理)
  def departmentarearetentionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //各区留样规则各状态按照学校数据统计(对学校进行去重处理)
  def arearetentionschoolrulestatus(data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按照权限各区留样规则各状态按照学校数据统计(对学校进行去重处理)
  def departmentarearetentionschoolrulestatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按所属教育部留样计划各状态数据统计
  def masteridretentiontotal(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限的所属留样计划各状态数据统计
  def departmentmasteridretentiontotal(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育部留样操作规则数据统计
  def masteridretentionruletotal(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按权限所属教育部留样操作规则数据统计
  def departmentmasteridretentionruletotal(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
  def masteridretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按权限所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)
  def departmentmasteridretentionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)
  def masteridretentionschoolrulestatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按权限所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)
  def departmentmasteridretentionschoolrulestatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按上海市办学性质来留样计划各状态数据统计（nature）
  def natureretentionstatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市办学性质来留样计划各状态数据统计（nature）
  def departmentnatureretentionstatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来留样操作规则各状态数据统计（nature）
  def natureretentionrulestatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市办学性质来留样操作规则各状态数据统计（nature）
  def departmentnatureretentionrulestatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区办学性质来留样计划各状态数据统计（nature）
  def areanatureretentionstatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def natureretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def departmentnatureretentionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）
  def natureretentionschoolrulestatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）
  def departmentnatureretentionschoolrulestatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def areanatureretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来留样计划各状态数据统计（level）
  def levelreretentionstatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市各类型学校来留样计划各状态数据统计（level）
  def departmentlevelreretentionstatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来留样操作规则数据统计（level）
  def levelreretentionrulestatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市各类型学校来留样操作规则数据统计（level）
  def departmentlevelreretentionrulestatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来留样计划各状态数据统计（level）
  def arealevelreretentionstatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
  def levelreretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
  def departmentlevelreretentionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）
  def levelreretentionschoolrulestatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）
  def departmentlevelreretentionschoolrulestatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
  def arealevelreretentionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按管理部门留样计划各状态数据统计
  def departmentstatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限管理部门留样计划各状态数据统计
  def departmentdepartmentstatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按管理部门留样计划操作规则统计
  def departmentrulestatus(data:(RDD[(String, String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限管理部门留样计划操作规则统计
  def departmentdepartmentrulestatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按管理部门留样计划各状态数据统计 (对学校进行去重处理）
  def departmentschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限管理部门留样计划各状态数据统计 (对学校进行去重处理）
  def departmentdepartmentschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按管理部门留样操作规则统计数据统计 (对学校进行去重处理）
  def departmentschoolrulestatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按权限管理部门留样操作规则统计数据统计 (对学校进行去重处理）
  def departmentdepartmentschoolrulestatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //权限各区办学性质来留样计划各状态数据统计（nature）
  def departmentareanatureretentionstatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按权限各区各类型学校来留样计划各状态数据统计（level）

  def departmentarealevelreretentionstatus(data:(RDD[(String, String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按权限各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def departmentreanatureretentionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按权限各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）
  def departmentareanatureretentionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))



}
