package com.ssic.impl

import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import redis.clients.jedis.JedisCluster

import scala.collection.mutable

trait PlatoonFunc {

  def platoredis(data: (SparkSession, RDD[(String, String)],String, Broadcast[Map[String, Int]], Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, String]],String,mutable.Set[String]))

}
