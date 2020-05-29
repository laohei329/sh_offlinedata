package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait SchoolAllUseFunc {

  // 计算按照市教委各区的业务统计数据
  def areatotal(businessData:RDD[(String, String)],date:String):RDD[(String, String)]

  //计算按照权限管理部门的各区的业务数据
  def departmentareatotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算上海市各类型学校的业务情况 (level)
  def leveltotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]

  //计算权限管理部门上海市各类型学校的业务统计 (level)
  def departmentleveltotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算各区各类型学校的业务情况 (level)
  def arealeveltotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]
  //计算权限管理部门各区各类型学校的业务统计 (level)
  def departmentarealeveltotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算上海市按照学校性质的业务统计 (nature)
  def naturetotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]

  //计算权限管理部门上海市按照学校性质的业务统计 (nature)
  def departmentnaturetotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算各区按照学校性质的业务统计 (nature)
  def areanaturetotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]

  //计算权限管理部门各区按照学校性质的业务统计 (nature)
  def departmentareanaturetotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算上海市按照学校食堂性质业务统计 (canteenmode)
  def canteentotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]

  //计算权限管理部门上海市按照学校食堂性质业务统计 (canteenmode)
  def departmentcanteentotal(businessData :RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算各区按照学校食堂性质业务统计 (canteenmode)
  def areacanteentotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]

  //计算权限管理部门各区按照学校食堂性质业务统计 (canteenmode)
  def departmentareacanteentotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

  //计算按照区属的业务统计
  def masteridtotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]],commiteeData:Broadcast[Map[String, String]]):RDD[(String, String)]

  //计算权限管理部门按照区属的业务统计
  def departmentmasteridtotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]],commiteeData:Broadcast[Map[String, String]]):RDD[(String, String,String)]

  //计算上海市按照管理部门的维度的统计数据
  def shanghaidepartmenttotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String)]

  //计算权限管理部门按照管理部门的维度的统计数据
  def departmentdepartmenttotal(businessData:RDD[(String, String)],date:String,schoolData:Broadcast[Map[String, List[String]]]):RDD[(String, String,String)]

}
