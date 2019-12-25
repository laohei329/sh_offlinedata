package com.ssic.impl

import org.apache.spark.sql.hive.HiveContext

trait WarnHiveDataFunc {

  //证件预警各区学校证件，团餐公司证件，人员证件，全部的统计数据
  def areawarntotal(data:HiveContext)

  //各区按照学校性质的维度统计，现只包含了预警的未处理单位，预警总数，未处理预警数量

  def areanaturewarntotal(data:HiveContext)

  //按照学制分类进行的统计，现只包含预警的未预警单位，预警总数，预警未处理数量

  def arealevelwarntotal(data:HiveContext)

  //大屏预警数据

  def dapingwarndata(data:HiveContext)
}
