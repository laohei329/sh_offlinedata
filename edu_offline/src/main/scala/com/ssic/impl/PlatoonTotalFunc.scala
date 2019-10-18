package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait PlatoonTotalFunc {

  //计算按照市教委各区的排菜统计数据
  def areaplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String))

  //计算按照权限管理部门的各区的排菜统计数据
  def departmentareaplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市各类型学校的排菜情况 (level)
  def levelplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门上海市各类型学校的排菜统计 (level)
  def departmentlevelplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区各类型学校的排菜情况 (level)
  def arealevelplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门各区各类型学校的排菜统计 (level)
  def departmentarealevelplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市按照学校性质的排菜统计 (nature)
  def natureplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门上海市按照学校性质的排菜统计 (nature)
  def departmentnatureplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区按照学校性质的排菜统计 (nature)
  def areanatureplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门各区按照学校性质的排菜统计 (nature)
  def departmentareanatureplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市按照学校食堂性质排菜统计 (canteenmode)
  def canteenplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门上海市按照学校食堂性质排菜统计 (canteenmode)
  def departmentcanteenplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区按照学校食堂性质排菜统计 (canteenmode)
  def areacanteenplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门各区按照学校食堂性质排菜统计 (canteenmode)
  def departmentareacanteenplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算按照区属的排菜统计
  def masteridplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String, String]]))

  //计算权限管理部门按照区属的排菜统计
  def departmentmasteridplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String, String]]))

  //计算上海市按照管理部门的维度的统计数据
  def shanghaidepartmentplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门按照管理部门的维度的统计数据
  def departmentdepartmentplatoontotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区的排菜操作规则数量统计
  def areaplastatustotal(data: (RDD[(String, String)],RDD[(String, String)],String))

  //计算权限管理部门各区的排菜操作规则数量统计
  def departmentareaplastatustotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市各类型学校的排菜操作规则数量统计
  def levelplastatustotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门上海市各类型学校的排菜操作规则数量统计(level)
  def departmentlevelplastatustotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市按照学校性质的排菜操作规则数量统计 (nature)
  def natureplastatustotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门上海市按照学校性质的排菜操作规则数量统计 (nature)
  def departmentnatureplastatustotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算按照区属的排菜操作规则数量统计
  def masteridplastatustotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String, String]]))

  //计算权限管理部门按照区属的排菜操作规则数量统计
  def departmentmasteridplastatustotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String, String]]))

  //计算上海市按照管理部门的维度的排菜操作规则数量统计
  def shanghaidepartmentplastatustotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算权限管理部门按照管理部门的排菜操作规则数量统计
  def departmentdepartmentplastatustotal(data: (RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))
}
