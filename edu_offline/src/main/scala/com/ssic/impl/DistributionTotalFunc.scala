package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait DistributionTotalFunc {

  //各区配送计划总数据统计
  def areadistributionltotal(data:(RDD[(String, String,String,String,String,String,String)],String))

  //按照权限的各区配送计划总数据统计
  def departmentareadistributionltotal(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区配送计划各状态数据统计
  def areadistributionstatustotal (data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)]))

  //按照权限的各区配送计划各状态数据统计
  def departmentareadistributionstatustotal(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区配送计划各状态按照学校数据统计(对学校进行去重处理)
  def areadistributionschoolstatus (data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按照权限的各区配送计划各状态按照学校数据统计(对学校进行去重处理)
  def departmentareadistributionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按所属教育部配送计划各状态数据统计
  def masteriddistributiontotal (data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限的所属教育部配送计划各状态数据统计
  def departmentmasteriddistributiontotal(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
  def masteriddistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限的所属教育局配送计划各状态按照学校数据统计(对学校进行去重处理)
  def departmentmasteriddistributionschoolstatusdata(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按上海市办学性质来配送计划各状态数据统计（nature）
  def naturedistributionstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市办学性质来配送计划各状态数据统计（nature）
  def departmentnaturedistributionstatus(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //各区办学性质来配送计划各状态数据统计（nature）
  def areanaturedistributionstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def naturedistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def departmentnaturedistributionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按各区办学性质来配送计划各状态按照学校数据统计（nature,对学校进行去重处理）
  def areanaturedistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来配送计划各状态数据统计（level）
  def leveldistributionstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市各类型学校来配送计划各状态数据统计（level）
  def departmentleveldistributionstatus(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来配送计划各状态数据统计（level）
  def arealeveldistributionstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
  def leveldistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
  def departmentleveldistributionschoolstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按各区各类型学校来配送计划各状态按照学校数据统计（level,对学校进行去重处理）
  def arealeveldistributionschoolstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照各区的验收操作规范的统计
  def areadisstatus(data:(RDD[(String, String, String, String, String, String,String)],String,RDD[(String, String)]))

  //按照权限的各区的验收操作规范的统计
  def departmentareadisstatus(data:(RDD[(String, String, String, String, String, String,String)],String,Broadcast[Map[String, List[String]]]))

  //按照各区的验收操作规范的统计(对学校去重)
  def areaschooldisstatus(data:(RDD[(String, String)],String,RDD[(String, String)]))

  //按照权限的各区的验收操作规范的统计(对学校去重)
  def departmentareaschooldisstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市办学性质的验收操作规范的统计
  def naturedisstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市办学性质的验收操作规范的统计
  def departmentnaturedisstatus(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市办学性质的验收操作规范的统计(对学校去重)
  def natureschooldisstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市办学性质的验收操作规范的统计(对学校去重)
  def departmentnatureschooldisstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市办学学制的验收操作规范的统计
  def leveldisstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市办学学制的验收操作规范的统计
  def departmentleveldisstatus(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市办学学制的验收操作规范的统计(对学校去重)
  def levelschooldisstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市办学学制的验收操作规范的统计(对学校去重)
  def departmentlevelschooldisstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市教属的验收操作规范的统计
  def masteriddisstatus(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限的上海市教属的验收操作规范的统计
  def departmentmasteriddisstatus(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照上海市教属的验收操作规范的统计(对学校去重)
  def masteridschooldisstatus(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照权限的上海市教属的验收操作规范的统计(对学校去重)
  def departmentmasteridschooldisstatus(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))

  //按照上海市管理部门的配送各状态数量
  def dedistributionstatustotal(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市管理部门的配送各状态数量
  def dedepartmentdistributionstatustotal(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市管理部门的配送各状态数量(对学校去重)
  def dedistributionschoolstatustotal(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市管理部门的配送各状态数量(对学校去重)
  def dedepartmentdistributionschoolstatustotal(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市管理部门的验收操作规范数量
  def dedisstatustotal(data:(RDD[(String, String,String,String,String,String,String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市管理部门的验收操作规范数量
  def dedepartmentdisstatustotal(data:(RDD[(String, String,String,String,String,String,String)],String,Broadcast[Map[String, List[String]]]))

  //按照上海市管理部门的验收操作规范数量(对学校去重)
  def deschooldisstatustotal(data:(RDD[(String, String)],String,RDD[(String, String)],Broadcast[Map[String, List[String]]]))

  //按照权限的上海市管理部门的验收操作规范数量(对学校去重)
  def dedepartmentschooldisstatustotal(data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

}
