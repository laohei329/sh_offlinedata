package com.ssic.report

import com.alibaba.fastjson.JSON
import com.ssic.beans.{SchoolBean, StopFoodBean}
import com.ssic.report.Distribution.logger
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  * Created by 云 .
  * 禁止食品指标
  */

object StopFood {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def Stopupdate(filterData: String): (String, String, String, String, String, String, String, String) = {
    var stat_name = "null"
    val stopFood = JSON.parseObject(filterData, classOf[StopFoodBean])
    try {

      stat_name = stopFood.stat
    } catch {
      case e: Exception => {
        logger.error(s"parse json error: $stat_name", e)
        return ("null", "null", "null", "null", "null", "null", "null", "null")
      }
    }
    ("update", stopFood.id, stopFood.name, stopFood.`type`.toString, stopFood.note, stopFood.stat, stopFood.stat, stopFood.create_time.split(" ")(0))
  }

  /**
    *
    * * 分析，获取禁止食品数据
    *
    * * @param RDD[SchoolBean] binlog日志数据
    *
    */

  def Stop(filterData: RDD[SchoolBean]) = {

    val stopFood = filterData.filter(x => x != null && x.table.equals("t_edu_taboo")).map(x => (x.`type`, JSON.parseObject(x.data, classOf[StopFoodBean]), JSON.parseObject(x.old, classOf[StopFoodBean])))
    val StopData: RDD[(String, String, String, String, String, String, String, String)] = stopFood.distinct().map({
      case (k, v, z) =>

        var oldStat = "null"
        var oldCreateTime = "null"
        try {
          oldStat = z.stat
          oldCreateTime = z.create_time.split(" ")(0)
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $z", e)
          }
        }
        if ("insert".equals(k) && !"0".equals(v.stat)) {
          ("insert", v.id, v.name, v.`type`.toString, v.note, v.stat, "null", v.create_time.split(" ")(0))
        } else if ("delete".equals(k) && !"0".equals(v.stat)) {
          ("delete", v.id, v.name, v.`type`.toString, v.note, v.stat, "null", v.create_time.split(" ")(0))
        } else {
          ("update", z.id, z.name, z.`type`.toString, v.note, v.stat, oldStat, oldCreateTime)
        }
    })
    StopData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1)) {
              jedis.hset("stop-menu", x._2, "name" + "_" + x._3 + "_" + "type" + "_" + x._4 + "_" + "note" + "_" + x._5 + "_" + "time" + "_" + x._8)
            } else if ("delete".equals(x._1)) {
              jedis.hdel("stop-menu", x._2)
            } else {
              if (StringUtils.isEmpty(x._7)) {
                jedis.hset("stop-menu", x._2, "name" + "_" + x._3 + "_" + "type" + "_" + x._4 + "_" + "note" + "_" + x._5 + "_" + "time" + "_" + x._8)
              } else {
                if ("0".equals(x._6)) {
                  jedis.hdel("stop-menu", x._2)
                } else if ("1".equals(x._6)) {
                  jedis.hset("stop-menu", x._2, "name" + "_" + x._3 + "_" + "type" + "_" + x._4 + "_" + "note" + "_" + x._5 + "_" + "time" + "_" + x._8)
                } else {
                  logger.info("禁止物料的不符合信息-------------------" + x)
                }
              }
            }

        })
    })


  }
}
