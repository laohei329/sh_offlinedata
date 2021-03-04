package com.ssic.service

import org.apache.commons.lang3._
import redis.clients.jedis.JedisCluster

import scala.collection.mutable

class GongcanStat {

  /**
    *
    * 判断在排菜临时表的是否供餐
    *
    * * platoondata 排菜表数据
    *
    * * @param key redis的排菜表数据的key
    *
    * * @param jedis:JedisCluster
    *
    * * @param date 排菜时间
    *
    * * @param b2bPlatoonSortData: b2b的排菜数据
    */

  def gongcan(platoondata: mutable.Set[String], key: String, jedis: JedisCluster, date: String, b2bPlatoonSortData: Map[String, String]): String = {
    if (platoondata.contains(key).equals(true)) {
      //在排菜临时表内
      val value = jedis.hget(date + "_platoon", key)
      if (value.size > 2) {
        return "供餐_已排菜" + "_" + "create-time" + value.split("create-time")(1)

      } else {
        return "供餐_已排菜" + "_" + "create-time" + "_" + "null"

      }
    } else if (platoondata.contains("null" + "_" + key.split("_")(1)).equals(true)) {
      //在排菜临时表内
      val value = jedis.hget(date + "_platoon", "null" + "_" + key.split("_")(1))
      if (value.size > 2) {
        return "供餐_已排菜" + "_" + "create-time" + value.split("create-time")(1)

      } else {
        return "供餐_已排菜" + "_" + "create-time" + "_" + "null"

      }
    } else {
      val value: String = b2bPlatoonSortData.getOrElse(key.split("_")(1), "0")
      if (!"0".equals(value)) {
        //在b2b排菜表内
        return "供餐_已排菜" + "_" + "create-time" + "_" + value
      } else {
        //不在排采临时表内
        return "供餐_未排菜" + "_" + "create-time" + "_" + "null"
      }


    }


  }

  def booleangongcan(platoondata: mutable.Set[String], key: String, jedis: JedisCluster, date: String): Boolean = {
    if (platoondata.contains(key).equals(true)) {
      //在排菜临时表内
      val value = jedis.hget(date + "_platoon", key)
      if (value.size > 2) {
        return true

      } else {
        return true

      }
    } else if (platoondata.contains("null" + "_" + key.split("_")(1)).equals(true)) {
      //在排菜临时表内
      val value = jedis.hget(date + "_platoon", "null" + "_" + key.split("_")(1))
      if (value.size > 2) {
        return true

      } else {
        return true

      }
    } else {
      //不在排采临时表内
      return false
    }
  }


  def nogongcan(): String = {

    return "不供餐_未排菜" + "_" + "create-time" + "_" + "null"

  }
}
