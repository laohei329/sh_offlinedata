package com.ssic.utils

import java.util

import com.typesafe.config.ConfigFactory
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.{HostAndPort, Jedis, JedisCluster, JedisPool}


/**
  * Created by 云 on 2018/8/6.
  */
object JPools {

  private val load = ConfigFactory.load()

  private val poolConfig = new GenericObjectPoolConfig()
  poolConfig.setLifo(true)
  poolConfig.setFairness(true)
  poolConfig.setMaxIdle(1000)
  poolConfig.setMaxTotal(1000)
  poolConfig.setMinIdle(2)

  private val nodes = new util.LinkedHashSet[HostAndPort]()
  //nodes.add(new HostAndPort("172.16.10.18", 7000))
  nodes.add(new HostAndPort("172.16.10.18", 7001))
  nodes.add(new HostAndPort("172.16.10.37", 7002))
  nodes.add(new HostAndPort("172.16.10.37", 7003))
  nodes.add(new HostAndPort("172.16.10.52", 7004))
  nodes.add(new HostAndPort("172.16.10.52", 7005))

  private val cluster = new JedisCluster(nodes,5000,5000,5,"5O1ecOhLH6bFNlt6", poolConfig)

  private lazy val jedisPool = new JedisPool(
    poolConfig,
    load.getString("redis.host"),
    load.getInt("redis.port"),
    3000,
    null,
    8
  )


  def getJedis: JedisCluster = cluster
}
