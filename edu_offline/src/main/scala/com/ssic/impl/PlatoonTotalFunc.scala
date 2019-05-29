package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait PlatoonTotalFunc {

  //计算按照区的排菜统计数据
  def areaplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String))

  //计算上海市各类型学校的排菜情况 (level)
  def levelplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区各类型学校的排菜情况 (level)
  def arealevelplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市按照学校性质的排菜统计 (nature)
  def natureplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区按照学校性质的排菜统计 (nature)
  def areanatureplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算上海市按照学校食堂性质排菜统计 (canteenmode)
  def canteenplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算各区按照学校食堂性质排菜统计 (canteenmode)
  def areacanteenplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]]))

  //计算按照区属的排菜统计
  def masteridplatoontotal(data: (RDD[(String, String)],RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String, String]]))
}
