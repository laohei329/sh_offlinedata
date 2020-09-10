package com.ssic.report

import java.util
import java.util.{Calendar, Date}

import com.ssic.report.MonthPlatoon.format
import com.ssic.utils.{JPools, Rule, Tools}
import com.ssic.utils.Tools.{conn, edu_bd_department, url}
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

import scala.collection.JavaConverters._

/**
  * 定期删除过期的redis的key
  */

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


    val date = Rule.timeToStamp("yyyy-MM-dd",-45) //format.format(time)
    val yesterday = Rule.timeToStamp("yyyy-MM-dd",-1) //format.format(time)

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
    jedis.del(date+"_b2b-platoon")

    for (i <- 0 until departmentid.size){
      val id = departmentid(i)
      val jedis = JPools.getJedis
      jedis.del(date+"_platoonfeed-total"+"_"+"department"+"_"+id)
      jedis.del(date+"_useMaterialPlanTotal"+"_"+"department"+"_"+id)
      jedis.del(date+"_DistributionTotal"+"_"+"department"+"_"+id)
      jedis.del(date+"_gc-retentiondishtotal"+"_"+"department"+"_"+id)
    }

    //b2bDistribution存的是b2b配送表的主键id和配送时间，定期删除过期时间
    val b2bDistribution: RDD[(String, String)] = sc.parallelize(jedis.hgetAll("b2bDistribution").asScala.toList)
    b2bDistribution.map({
      x =>
        val deliveryDate = x._2.split("_")(2)
        if(StringUtils.isNoneEmpty(deliveryDate) && !"null".equals(deliveryDate)){
          if(format.parse(deliveryDate).getTime < format.parse(yesterday).getTime){
            x._1
          }else{
            "null"
          }
        }else{
          "null"
        }

    }).filter(x => !"null".equals(x)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hdel("b2bDistribution",x)
        })
    })

    sc.stop()

  }

}
