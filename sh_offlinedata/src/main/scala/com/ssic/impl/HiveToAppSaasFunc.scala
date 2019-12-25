package com.ssic.impl

import org.apache.spark.sql.hive.HiveContext

trait HiveToAppSaasFunc {

  //综合分析菜品,留样的详细表（app_t_edu_dish_menu）
  def appedudishmenu(data:(HiveContext,String,String))

  //综合分析的原料供应明细表（app_t_edu_ledege_detail）
  def appeduledegedetail(data:(HiveContext,String,String))

}
