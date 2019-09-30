package com.ssic.report

import com.ssic.service.WarnHiveDataStat
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

/**
  * 预警的hive数据汇总处理
  */

object WarnHiveData {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)

    val hiveContext: HiveContext = new HiveContext(sc)

    //证件预警各区学校证件，团餐公司证件，人员证件，全部的统计数据
    new WarnHiveDataStat().areawarntotal(hiveContext)

    //各区按照学校性质的维度统计，现只包含了预警的未处理单位，预警总数，未处理预警数量
    new WarnHiveDataStat().areanaturewarntotal(hiveContext)

    //按照学制分类进行的统计，现只包含预警的未预警单位，预警总数，预警未处理数量
    new WarnHiveDataStat().arealevelwarntotal(hiveContext)


    sc.stop()
  }

}
