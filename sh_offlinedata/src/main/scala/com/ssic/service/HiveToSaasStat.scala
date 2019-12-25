package com.ssic.service

import com.ssic.impl.HiveToSaasFuc
import org.apache.spark.sql.hive.HiveContext

class HiveToSaasStat extends HiveToSaasFuc{
  override def insertpackage(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_saas_package partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_saas_package_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertpackagedishes(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_saas_package_dishes partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_saas_package_dishes_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertledgermaster(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_pro_ledger_master partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_pro_ledger_master_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertledger(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_pro_ledger partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_pro_ledger_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertimages(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_pro_images partition (year,month) select *,year(create_time) as year,month(create_time)
         |from saas_v1.t_pro_images_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertwarnmaster(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_pro_warning_global_master partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_pro_warning_global_master_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertwarnlicense(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_pro_warning_license partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_pro_warning_license_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }

  override def insertwarnviewrelation(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert overwrite table saas_v1.t_pro_warning_view_relation partition (year,month) select *,year(create_time) as year,month(create_time) as month
         |from saas_v1.t_pro_warning_view_relation_temp
         |where create_time like '${data._2}%'
       """.stripMargin)
  }
}
