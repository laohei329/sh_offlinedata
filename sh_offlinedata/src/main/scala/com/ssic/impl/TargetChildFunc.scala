package com.ssic.impl

import org.apache.spark.rdd.RDD

trait TargetChildFunc{

  //用料计划的子页面,没有产生用料计划的的学校也要放入到子页面中
  def usematerialchild(data:(RDD[(String, String)],RDD[(String, List[String])],String,RDD[(String, String)]))

  //配送计划的子页面，没有产生配送计划的学校也要放入到子页面中
  def distributionchild(data:(RDD[(String, String)],RDD[(String, String)],String,RDD[(String, String)]))

  //留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
  def retentionchild(data: (RDD[(String, String)],RDD[(String, String, String, String, String)], String,RDD[(String, String)]))

}
