package com.ssic.impl

import redis.clients.jedis.JedisCluster

import scala.collection.mutable

trait GongcanFunc {

    def gongcan(data:(mutable.Set[String],String,JedisCluster,String)):String

    def nogongcan():String


}
