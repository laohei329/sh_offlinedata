package com.ssic.service

import com.ssic.impl.GongcanFunc
import redis.clients.jedis.JedisCluster

import scala.collection.mutable

class GongcanStat extends GongcanFunc {

  override def gongcan(data: (mutable.Set[String], String, JedisCluster, String)): String = {
    if (data._1.contains(data._2).equals(true)) {
      //在排菜临时表内
      val value = data._3.hget(data._4 + "_platoon", data._2)
      if (value.size > 2) {
        return "供餐_已排菜" + "_" + "create-time" + value.split("create-time")(1)

      } else {
        return "供餐_已排菜"

      }
    } else {
      //不在排采临时表内
      return "供餐_未排菜"
    }


  }

  override def nogongcan(): String = {

    return "不供餐_未排菜"

  }
}
