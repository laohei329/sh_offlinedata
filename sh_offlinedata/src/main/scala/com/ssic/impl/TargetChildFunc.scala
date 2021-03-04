package com.ssic.impl

import org.apache.spark.rdd.RDD

trait TargetChildFunc{

  //用料计划的子页面,没有产生用料计划的的学校也要放入到子页面中
  def usematerialchild(platoonData:RDD[(String, String)],useValue:RDD[(String, List[String])],date:String,useMaterialChildData:RDD[(String, String)])

  //配送计划的子页面，没有产生配送计划的学校也要放入到子页面中
  def distributionchild(platoonData:RDD[(String, String)],disValue:RDD[(String, String)],date:String,distributionChildData:RDD[(String, String)])

  //留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
  def retentionchild(platoonData:RDD[(String, String)],reValue:RDD[(String, String,String,String,String,String,String)], date:String,retentionChildData:RDD[(String, String)])

}
