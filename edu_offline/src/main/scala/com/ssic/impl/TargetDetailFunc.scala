package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait TargetDetailFunc {

  //用料计划的详情数据
  def usematerial(data: (RDD[(String, String,String,String,String,String)], RDD[(String, String)], String))

  //配送计划的详情数据
  def distribution(data: (RDD[(String, String,String,String,String,String)], RDD[(String, String)], String))

  //菜品留样的详情数据
  def retentiondish(data:(RDD[(String, String, String, String, String, String, String, String)],RDD[(String, String)],String,Broadcast[Map[String, String]],RDD[(String, String)]))

}
