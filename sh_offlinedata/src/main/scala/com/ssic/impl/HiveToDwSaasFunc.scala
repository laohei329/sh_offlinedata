package com.ssic.impl

import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.hive.HiveContext

trait HiveToDwSaasFunc {

  //将t_pro_images表，对配送图片和建议案图片进行处理
  def insertdwimages(data:(HiveContext,String,String))

  //处理用料详情表，dw_t_edu_material_detail
  def insertdwmaterialdetail(data:(HiveContext,String))

  //处理菜品，原料详情表 ，dw_t_edu_material_dish
  def insertmaterialdish(data:(HiveContext,String,String,String))

  //是否供餐表（dw_t_edu_calendar）
  def dweducalendar(data:(HiveContext,String,String,String,String,String,String))

  //是否供餐表（app_t_edu_calendar）
  def appeducalendar(data:(HiveContext,String))

  //是否排菜，用料表（dw_t_edu_platoon_detail）
  def dwplatoondetail(data:(HiveContext,String, String))

  //将学校证件预警，团餐公司证件各维度统计放入到dw库中预警统计表中
  def insertschoollicensewwarntotal(data:(HiveContext,String))

  //将人员证件预警各维度统计放入到dw库中预警统计表中
  def insertpeoplelicensedwwarntotal(data:(HiveContext,String))

  //将全部预警各维度统计放入到dw库中预警统计表中
  def insertalllicensedwwarntotal(data:(HiveContext,String))

  //将未处理预警单位各维度统计放入到dw库中预警统计表中，对预警进行去重操作
  def insertditinctdwwarntotal(data:(HiveContext,String))

  //将处理好的供餐数据，存储于saas_v1_dw库中
  def insertdwcalendar(data:(HiveContext,String))

}
