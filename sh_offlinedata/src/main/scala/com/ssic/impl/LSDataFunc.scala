package com.ssic.impl

import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext

trait LSDataFunc {

  //将原料排名数据放入到redis中
  def materialtotaltoredis(hiveContext:HiveContext,date:String,year:String,month:String,materialData:RDD[(String, String)])

  //将菜品排名数据放入到redis中
  def dishtotaltoredis(hiveContext:HiveContext,date:String,year:String,month:String,dishData:RDD[(String, String)])

  //将供应商供应学校排名的统计数据放入到redis中
  def supplynametotaltoredis(hiveContext:HiveContext,date:String,year:String,month:String,supplierToSchoolData:RDD[(String, String)])

}
