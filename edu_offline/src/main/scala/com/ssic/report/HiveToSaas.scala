package com.ssic.report

import java.util.Date

import com.ssic.report.TargetDetail.format1
import com.ssic.service.HiveToSaasStat
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

/**
  *  将hive临时表数据导入到saas_v1库并进行分区,操作的是阳光午餐的数据
  */

object HiveToSaas {

  private val format = FastDateFormat.getInstance("yyyy")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)


    val year =format.format(new Date())
    //插入t_saas_package的当年的分区表
    new HiveToSaasStat().insertpackage(hiveContext,year)

    //插入t_saas_package_dishes的当年的分区表
    new HiveToSaasStat().insertpackagedishes(hiveContext,year)

    //插入t_pro_ledger_master的当年的分区表
    new HiveToSaasStat().insertledgermaster(hiveContext,year)

    //插入t_pro_ledger的当年的分区表
    new HiveToSaasStat().insertledger(hiveContext,year)

    //插入t_pro_images的当年的分区表
    new HiveToSaasStat().insertimages(hiveContext,year)

    sc.stop()
  }

}
