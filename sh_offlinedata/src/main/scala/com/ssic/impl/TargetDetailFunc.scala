package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait TargetDetailFunc {

  //用料计划的详情数据
  def usematerial(usematerialDealData:RDD[(String, String, String, String, String, String, String)],useMaterialData: RDD[(String, String)], date:String)

  //配送计划的详情数据
  def distribution(distributiondealdata:RDD[(String, String,String,String,String,String,String)], distributionData:RDD[(String, String)], date:String)

  //菜品留样的详情数据
  def retentiondish(data:(RDD[(String, String, String, String, String, String, String, String)],RDD[(String, String)],String,Broadcast[Map[String, String]],RDD[(String, String)]))

  //当天的菜品留样的详情数据
  def todayretentiondish(dishmenu:RDD[(String, String, String, String, String, String, String, String)],retentiondishData:RDD[(String, String)],date:String,gongcanSchool:Broadcast[Map[String, String]],gcretentiondishData:RDD[(String, String)],todaydishmenuData:RDD[(String, String)],school2Area:Broadcast[Map[String, String]])

}
