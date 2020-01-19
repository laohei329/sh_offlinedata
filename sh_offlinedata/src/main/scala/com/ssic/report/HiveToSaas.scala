package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.Platoon.{format, format1}
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
  private val format1 = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)

    for (i <- 0 to 134) {
      //查询一周的排菜数据

      hiveContext.sql(
        s"""
           |insert into table saas_v1.t_edu_menu_group_cater_temp partition (year,month)
           |select date_sub(current_date ,${i}) as use_date,
           |id,
           |school_supplier_id ,
           |menu_group_id ,
           |menu_group_name,
           |cater_type_id ,
           |cater_type_name  ,
           |ledger_type ,
           |meals_count ,
           |meal_type ,
           |package_num ,
           |supplier_id ,
           |creator ,
           |create_time ,
           |updater ,
           |last_update_time ,
           |stat ,
           |data_source_id ,
           |meal_standard_price ,
           |govern_meals_count ,
           |govern_meal_standard_price  ,
           |year(date_sub(current_date ,${i})) as year,month(date_sub(current_date ,${i})) as month from saas_v1.t_edu_menu_group_cater
         """.stripMargin)

    }


//    val year =format.format(new Date())
//    //插入t_saas_package的当年的分区表
//    new HiveToSaasStat().insertpackage(hiveContext,year)
//
//    //插入t_saas_package_dishes的当年的分区表
//    new HiveToSaasStat().insertpackagedishes(hiveContext,year)
//
//    //插入t_pro_ledger_master的当年的分区表
//    new HiveToSaasStat().insertledgermaster(hiveContext,year)
//
//    //插入t_pro_ledger的当年的分区表
//    new HiveToSaasStat().insertledger(hiveContext,year)
//
//    //插入t_pro_images的当年的分区表
//    new HiveToSaasStat().insertimages(hiveContext,year)

    sc.stop()
  }

}
