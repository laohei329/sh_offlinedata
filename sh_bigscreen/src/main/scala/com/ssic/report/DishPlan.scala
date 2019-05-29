package com.ssic.report

import java.lang

import com.ssic.beans.SchoolBean
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  * Created by 云 .
  * 菜品计划功能指标
  */

object DishPlan {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def Dish(filterData: RDD[SchoolBean]) = {

    val packages = filterData.filter(x => x != null && x.table.equals("t_saas_package") && x.`type`.equals("insert") && !x.data.stat.equals("0") && x.data.is_publish != 0)
    val dishData = filterData.filter(x => x != null && x.table.equals("t_saas_package_dishes") && x.`type`.equals("insert") && !x.data.stat.equals("0"))

    val packageData = packages.distinct().map(x => {
      val id = x.data.id
      val supply_date = x.data.supply_date
      val replaceAll = supply_date.replaceAll("\\D", "-")
      val date = format.format(format.parse(replaceAll))
      (id, date)
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2))
    val dishFlitData = dishData.distinct().map(x => (x.data.package_id, x.data.dishes_name, x.data.dishes_number)).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2)).map(x => (x._1, x._2 + ";" + x._3))
    packageData.leftOuterJoin(dishFlitData).map(x => (x._2._1, x._2._2.getOrElse("null"))).filter(x => !x._2.equals("null")).filter(x => StringUtils.isNoneEmpty(x._2.split(";")(0)))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hincrBy(x._1 + "_" + "dish", x._2.split(";")(0), 1)
              if (jedis.exists(x._1 + "_" + "dish").equals(true)) {
                val total: lang.Long = jedis.hlen(x._1 + "_" + "dish")
                jedis.hset(x._1 + "_" + "dish-total", "dishtotal", total.toString)
              } else {
                logger.info("没有菜品的key--------------------------------")
              }

              if (StringUtils.isNoneEmpty(x._2.split(";")(1)) && !x._2.split(";")(1).equals("null")) {
                val v =x._2.split(";")(1).replaceAll("[\u4e00-\u9fa5 a-zA-Z]", "")
                if(StringUtils.isNoneEmpty(v) && !v.equals("null")){
                  jedis.hincrBy(x._1 + "_" + "dish-total", "dishnumber", v.toLong)
                }

              } else {
                logger.info("菜品不符合计算的--------------------------------")
              }

          })
      })

  }


}
