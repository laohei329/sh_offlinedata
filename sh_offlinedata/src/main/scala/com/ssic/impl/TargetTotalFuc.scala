package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait TargetTotalFuc {

  //各区业务总数据统计
  def areatotal(targetData:RDD[(String, String,String,String,String,String,String)],date:String): RDD[(String, String)]

  //按照权限管理部门各区业务总数据统计
  def departmentareatotal(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]

  //各区业务各状态数据统计
  def areastatustotal(targetData:RDD[(String, String,String,String,String,String,String)],date:String): RDD[(String, String)]

  //按照权限管理部门各区业务各状态数据统计
  def departmentareastatustotal(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]

  //按所属教育部业务各状态数据统计
  def masteridtotal(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]],commiteeid2commiteename:Broadcast[Map[String,String]]): RDD[(String, String)]

  //按照权限管理部门所属教育部业务各状态数据统计
  def departmentmasteridtotal(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]],commiteeid2commiteename:Broadcast[Map[String,String]]): RDD[(String, String, String)]

  //按上海市办学性质来业务各状态数据统计（nature）
  def naturestatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String)]

  //按照权限管理部门办学性质来业务各状态数据统计（nature）
  def departmentnaturestatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]

  //各区办学性质来业务各状态数据统计（nature）
  def areanaturestatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String)]

  //按照权限管理部门各区办学性质来业务各状态数据统计（nature）
  def departmentareanaturestatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]

  //按上海市各类型学校来业务各状态数据统计（level）
  def levelstatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String)]

  //按照权限管理部门各类型学校来业务各状态数据统计（level）
  def departmentlevelstatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]

  //按各区各类型学校来业务各状态数据统计（level）
  def arealevelstatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String)]

  //按照权限管理部门各区各类型学校来业务各状态数据统计（level）
  def departmentarealevelstatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]


  //按照各管理部门各状态业务数量
  def departmentstatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String)]

  //按照权限各管理部门各状态业务数量
  def departmentdepartmentstatus(targetData:RDD[(String, String,String,String,String,String,String)],date:String,schoolData:Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]


}
