package com.ssic.report

import com.ssic.beans.{DataBean, SchoolBean}
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

  def Stopupdate(filterData: SchoolBean): (String, String, String, String, String, String, String, String) = {
    var stat_name = "null"
    try {
      stat_name = filterData.old.stat
    } catch {
      case e: Exception => {
        logger.error(s"parse json error: $stat_name", e)
        return ("null", "null", "null", "null", "null", "null", "null", "null")
      }
    }
    ("update", filterData.data.id, filterData.data.name, filterData.data.`type`.toString, filterData.data.note, filterData.data.stat, filterData.old.stat,filterData.data.create_time.split(" ")(0))
  }

  /**

    * * 分析，获取禁止食品数据

    * * @param RDD[SchoolBean] binlog日志数据

    */

  def Stop(filterData: RDD[SchoolBean]) = {

    val stopFood = filterData.filter(x => x != null && x.table.equals("t_edu_taboo"))
    val StopData: RDD[(String, String, String, String, String, String, String, String)] = stopFood.distinct().map({
      x =>
        if ("insert".equals(x.`type`) && !"0".equals(x.data.stat)) {
          ("insert", x.data.id, x.data.name, x.data.`type`.toString, x.data.note, x.data.stat, "null",x.data.create_time.split(" ")(0))
        } else if ("delete".equals(x.`type`) && !"0".equals(x.data.stat)) {
          ("delete", x.data.id, x.data.name, x.data.`type`.toString, x.data.note, x.data.stat, "null",x.data.create_time.split(" ")(0))
        } else {
          Stopupdate(x)
        }
    })
    StopData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1)) {
              jedis.hset("stop-menu", x._2, "name" + "_" + x._3 + "_" + "type" + "_" + x._4 + "_" + "note" + "_" + x._5+"_"+"time"+"_"+x._8)
            } else if ("delete".equals(x._1)) {
              jedis.hdel("stop-menu", x._2)
            } else {
              if (StringUtils.isEmpty(x._7)) {
                jedis.hset("stop-menu", x._2, "name" + "_" + x._3 + "_" + "type" + "_" + x._4 + "_" + "note" + "_" + x._5+"_"+"time"+"_"+x._8)
              } else {
                if ("1".equals(x._7) && "0".equals(x._6)) {
                  jedis.hdel("stop-menu", x._2)
                } else if ("0".equals(x._7) && "1".equals(x._6)) {
                  jedis.hset("stop-menu", x._2, "name" + "_" + x._3 + "_" + "type" + "_" + x._4 + "_" + "note" + "_" + x._5+"_"+"time"+"_"+x._8)
                } else {
                  logger.info("禁止物料的不符合信息-------------------" + x)
                }
              }
            }

        })
    })


  }
}
