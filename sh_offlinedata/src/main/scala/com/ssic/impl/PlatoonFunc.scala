package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import redis.clients.jedis.JedisCluster

import scala.collection.mutable

trait PlatoonFunc {

  /**

    * * 查询当天是否在系统学期设置内

    * * @param SparkSession

    * * @param RDD[(String, String)]  redis中存的排菜临时表数据

    * * @param String 当天的时间

    * * @param Broadcast[Map[String, Int]]  假期数据

    * * @param Broadcast[Map[String, String]]  供餐数据

    * * @param Broadcast[Map[String, String]] 学校设置的学期数据

    * * @param Broadcast[Map[String, String]] 系统设置的学期数据

    * * @param String 学期年

    * * @param mutable.Set[String] reidis的排菜表数据

    */

  def platoredis(data: (SparkSession, RDD[(String, String)],String, Broadcast[Map[String, Int]], Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, String]],String,mutable.Set[String]))

}
