package com.ssic.impl

import redis.clients.jedis.JedisCluster

import scala.collection.mutable

trait GongcanFunc {

    /**

      * * mutable.Set[String] 排菜表数据

      * * @param String redis的排菜表数据的key

      * * @param JedisCluster

      * * @param String 排菜时间

      */

    def gongcan(data:(mutable.Set[String],String,JedisCluster,String)):String

    def nogongcan():String


}
