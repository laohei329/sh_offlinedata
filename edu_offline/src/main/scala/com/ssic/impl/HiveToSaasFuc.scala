package com.ssic.impl

import org.apache.spark.sql.hive.HiveContext

trait HiveToSaasFuc {

  //插入t_saas_package的当年的分区表
  def insertpackage(data:(HiveContext,String))

  //插入t_saas_package_dishes的当年的分区表
  def insertpackagedishes(data:(HiveContext,String))

  //插入t_pro_ledger_master的当年的分区表
  def insertledgermaster(data:(HiveContext,String))

  //插入t_pro_ledger的当年的分区表
  def insertledger(data:(HiveContext,String))

  //插入t_pro_images的当年的分区表
  def insertimages(data:(HiveContext,String))

  //插入t_pro_warning_global_master的当年的分区表
  def insertwarnmaster(data:(HiveContext,String))

  //插入t_pro_warning_license的当年的分区表
  def insertwarnlicense(data:(HiveContext,String))

  //插入t_pro_warning_view_relation的当年的分区表
  def insertwarnviewrelation(data:(HiveContext,String))


}
