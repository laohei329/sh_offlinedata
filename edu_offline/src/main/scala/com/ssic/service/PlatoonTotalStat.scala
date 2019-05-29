package com.ssic.service

import com.ssic.impl.PlatoonTotalFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class PlatoonTotalStat extends PlatoonTotalFunc {

  override def areaplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String)): Unit = {
    data._1.map({
      x =>
        //(16,供餐_已排菜)
        ((x._1.split("_")(0), x._2), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1 + "_" + x._1._2, x._2)).cogroup(data._2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._3 + "_platoonfeed-total", k, "0")
            } else {
              jedis.hset(data._3 + "_platoonfeed-total", k, v._1.head.toString)
            }

        })

    })
  }

  override def levelplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), x._2), 1)
        } else {
          (("null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + x._1._2, x._2)).cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(data._3 + "_platoonfeed-total", k, "0")
              } else {
                jedis.hset(data._3 + "_platoonfeed-total", k, v._1.head.toString)
              }

          })
      })
  }

  override def arealevelplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val area = x._1.split("_")(0)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), x._2), 1)
        } else {
          ((area, "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + x._1._3, x._2)).cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case(k,v) =>
              //表示左边没有，右边有
              if(v._1.size ==0){
                jedis.hset(data._3+"_platoonfeed-total",k,"0")
              }else{
                jedis.hset(data._3+"_platoonfeed-total",k,v._1.head.toString)
              }

          })
      })
  }

  override def natureplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), x._2), 1)
        } else {
          (("null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + x._1._3, x._2)).cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case(k,v) =>
              //表示左边没有，右边有
              if(v._1.size ==0){
                jedis.hset(data._3+"_platoonfeed-total",k,"0")
              }else{
                jedis.hset(data._3+"_platoonfeed-total",k,v._1.head.toString)
              }

          })
      })
  }

  override def areanatureplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val area = x._1.split("_")(0)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), x._2), 1)
        } else {
          ((area, "null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2)).cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(data._4 + "_platoonfeed-total", k, "0")
              } else {
                jedis.hset(data._4 + "_platoonfeed-total", k, v._1.head.toString)
              }

          })
      })
  }

  override def canteenplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
          ((v(3), v(4), x._2), 1)
        } else {
          (("null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("canteenmode" + "_" + x._1._1 + "_" + "ledgertype" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(data._3 + "_platoonfeed-total", k, "0")
              } else {
                jedis.hset(data._3 + "_platoonfeed-total", k, v._1.head.toString)
              }

          })
      })
  }

  override def areacanteenplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val area = x._1.split("_")(0)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
          ((area, v(3), v(4), x._2), 1)
        } else {
          ((area, "null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "canteenmode" + "_" + x._1._2 + "_" + "ledgertype" + "_" + x._1._3 + "_" + x._1._4, x._2)).cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(data._3 + "_platoonfeed-total", k, "0")
              } else {
                jedis.hset(data._3 + "_platoonfeed-total", k, v._1.head.toString)
              }

          })
      })
  }

  override def masteridplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]],Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), x._2), 1)
          } else {
            ((v(5), v(6), x._2), 1)
          }
        } else {
          (("null", "null", x._2), 0)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + x._1._3, x._2)).cogroup(data._2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(data._3 + "_platoonfeed-total", k, "0")
              } else {
                jedis.hset(data._3 + "_platoonfeed-total", k, v._1.head.toString)
              }
          })
      })
  }
}
