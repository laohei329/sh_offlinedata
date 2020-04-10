package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.MonthPlatoon.format
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools.{conn, edu_bd_department, url}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

object DelRedisData {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setAppName("删除历史redis数据脚本")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val bd_department = session.read.jdbc(url, edu_bd_department, conn)
    bd_department.createTempView("t_edu_bd_department")


    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH, -45)
    val time = calendar.getTime
    val date = format.format(time)

    val departmentid = Tools.departmentid(session)


    val jedis = JPools.getJedis

    jedis.del(date+"_platoonfeed-total")
    jedis.del(date+"_platoon-feed")
    jedis.del(date+"_dish_new")
    jedis.del(date+"_material_new")
    jedis.del(date+"_useMaterialPlanTotal")
    jedis.del(date+"_useMaterialPlan-Detail")
    jedis.del(date+"_DistributionTotal")
    jedis.del(date+"_Distribution-Detail")
    jedis.del(date+"_gc-retentiondish")
    jedis.del(date+"_gc-retentiondishtotal")
    jedis.del(date+"_supplierwaste")
    jedis.del(date+"_supplierwastetotal")
    jedis.del(date+"schoolwaste")
    jedis.del(date+"_schoolwastetotal")
    jedis.del(date+"_schooloil")
    jedis.del(date+"_schooloiltotal")
    jedis.del(date+"_supplieroil")
    jedis.del(date+"_supplieroiltotal")
    jedis.del(date+"_useMaterialPlanTotal_child")
    jedis.del(date+"_DistributionTotal_child")
    jedis.del(date+"_gc-retentiondishtotal_child")
    jedis.del(date+"_allUseData")
    jedis.del(date+"_platoon")
    jedis.del(date+"_useMaterialPlanDetail")
    jedis.del(date+"_DistributionDetail")
    jedis.del(date+"_retention-dish")
    jedis.del(date+"_dish-menu")
    jedis.del(date+"_retentionsample")
    jedis.del(date+"_retentiondish")

    for (i <- 0 until departmentid.size){
      val id = departmentid(i)
      val jedis = JPools.getJedis
      jedis.del(date+"_platoonfeed-total"+"_"+"department"+"_"+id)
      jedis.del(date+"_useMaterialPlanTotal"+"_"+"department"+"_"+id)
      jedis.del(date+"_DistributionTotal"+"_"+"department"+"_"+id)
      jedis.del(date+"_gc-retentiondishtotal"+"_"+"department"+"_"+id)
    }

    sc.stop()

  }

}
