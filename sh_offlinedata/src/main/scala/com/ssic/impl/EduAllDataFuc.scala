package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

trait EduAllDataFuc {

  //排菜，用料，配送，留样数据整合
   def platoonmaterialdetailresert(data:(RDD[(String, String)],RDD[(String, String)],RDD[(String, String)],RDD[(String, String)], Broadcast[Map[String, List[String]]],RDD[(String, String)],String,Broadcast[Map[String, String]],Broadcast[Map[String, String]]))
}
