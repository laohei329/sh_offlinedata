package com.ssic.service

import com.ssic.impl.DistributionTotalFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DistributionTotalStat extends DistributionTotalFunc {

  override def areadistributionltotal(data: (RDD[(String, String, String, String, String, String)], String)): Unit = {
    data._1.map(x => (x._1, 1)).reduceByKey(_ + _).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_DistributionTotal", "area_" + x._1, x._2.toString)
        })
    })
  }

  override def areadistributionstatustotal(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map(x => ((x._1, x._6), 1)).reduceByKey(_ + _).map(x => ("area_" + x._1._1 + "_status_" + x._1._2, x._2)).cogroup(data._3)
      .filter(x => x._1.size > 7).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def areadistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        (area, status)
      //        if("3".equals(status)){
      //          (area,"3")
      //        }else{
      //          (area,"2")
      //        }
    }).map(x => ((x._1, x._2), 1)).reduceByKey(_ + _).map(x => ("school-area" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def masteriddistributiontotal(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>

        val v = data._4.value.getOrElse(x._4, List("null"))

        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), x._6), 1)
          } else {
            ((v(5), v(6), x._6), 1)
          }
        } else {
          (("null", "null", x._6), 1)
        }
    }).reduceByKey(_ + _)
      .map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "status_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 7).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def masteriddistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("status_")(1)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            (v(5), data._5.value.getOrElse(v(6), "null"), status)
          } else {
            (v(5), v(6),status)
          }
        } else {
          ("null", "null", status)
        }


    }).map(x => ((x._1, x._2, x._3), 1)).reduceByKey(_ + _).map(x => ("school-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "status_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def naturedistributionstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "3"), 1)
          } else {
            (("null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "2"), 1)
          } else {
            (("null", "null", "2"), 1)
          }
        }
    })
      .reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def areanaturedistributionstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((x._1, v(1), v(2), "3"), 1)
          } else {
            ((x._1, "null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((x._1, v(1), v(2), "2"), 1)
          } else {
            ((x._1, "null", "null", "2"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })

  }

  override def naturedistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if ("3".equals(status)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "3"), 1)
          } else {
            (("null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "2"), 1)
          } else {
            (("null", "null", "2"), 1)
          }
        }
    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2)).reduceByKey(_ + _).map(x => ("school-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def areanaturedistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if ("3".equals(status)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((area, v(1), v(2), "3"), 1)
          } else {
            ((area, "null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((area, v(1), v(2), "2"), 1)
          } else {
            ((area, "null", "null", "2"), 1)
          }
        }
    }).map(x => ((x._1._1, x._1._2, x._1._3, x._1._4), x._2)).reduceByKey(_ + _).map(x => ("school-nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def leveldistributionstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "3"), 1)
          } else {
            (("null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "2"), 1)
          } else {
            (("null", "2"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def arealeveldistributionstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((x._1, v(0), "3"), 1)
          } else {
            ((x._1, "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((x._1, v(0), "2"), 1)
          } else {
            ((x._1, "null", "2"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def leveldistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>

        val schoolid = x._1.split("_")(3)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if ("3".equals(status)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "3"), 1)
          } else {
            (("null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "2"), 1)
          } else {
            (("null", "2"), 1)
          }
        }
    }).map(x => ((x._1._1, x._1._2), x._2)).reduceByKey(_ + _).map(x => ("school-level" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def arealeveldistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("status_")(1)
        val v = data._4.value.getOrElse(schoolid, List("null"))

        if ("3".equals(status)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((area, v(0), "3"), 1)
          } else {
            ((area, "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((area, v(0), "2"), 1)
          } else {
            ((area, "null", "2"), 1)
          }
        }
    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2)).reduceByKey(_ + _).map(x => ("school-lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_DistributionTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_DistributionTotal", k, v._1.head.toString)
            }
        })
    })
  }
}
