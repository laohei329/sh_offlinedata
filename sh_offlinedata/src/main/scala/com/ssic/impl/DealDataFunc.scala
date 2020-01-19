package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait DealDataFunc {

  //对排菜的统计数据进行数据处理
  def platoontotal(data: RDD[(String, String)]) :RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))]

  //对用料计划的详情数据进行数据处理
  def usematerialdealdata(data: (RDD[(String, String)],Broadcast[Map[String, String]],Broadcast[Map[String, String]],Broadcast[Map[String, String]],Broadcast[Map[String, String]])): RDD[(String, String,String,String,String,String)]

  //对用料的统计数据进行数据处理
  def usematerialdealtotaldata(data: RDD[(String, String)]) :RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))]

  //对配送计划的详情数据进行数据处理
  def distributiondealdata(data: (RDD[(String, String)],Broadcast[Map[String, String]],Broadcast[Map[String, String]],String)): RDD[(String, String,String,String,String,String,String)]

  //对配送的统计数据进行数据处理
  def distributiondealtotaldata (data: RDD[(String, String)]):RDD[(String,(String, String))]

  //对留样计划的详情数据进行数据处理
  def retentiondealdata(data:RDD[(String, String)]): RDD[(String, String,String,String,String)]

  //对留样的统计数据进行数据处理
  def retentiondealtotaldata (data: RDD[(String, String)]):RDD[(String,(String, String))]

  //对上海市学校数的统计数据进行数据处理
  def shanghaitotal(data: RDD[(String, String)]):RDD[(String,(String, String))]

  //对预警大屏统计数据进行处理
  def warntotal(data:RDD[(String, String)]):RDD[(String,(String, String))]



}
