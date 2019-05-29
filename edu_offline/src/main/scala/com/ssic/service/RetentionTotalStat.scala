package com.ssic.service

import com.ssic.impl.RetentionTotalFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class RetentionTotalStat extends RetentionTotalFunc{
  override def arearetentiontotal(data: (RDD[(String, String, String)], String)): Unit = {

    data._1.map(x => (x._1, 1)).reduceByKey(_ + _).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal", x._1, x._2.toString)
        })

    })
  }

  override def arearetentionstatustotal(data: (RDD[(String, String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map(x => (x._1 + "_" + x._2, 1)).reduceByKey(_ + _).cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def arearetentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        var stat="null"
        if("0".equals(status)){
          stat="未留样"
        }else{
          stat="已留样"
        }
        (area,stat)
    }).map(x => ("school-area" + "_" + x._1 + "_" + x._2, 1)).reduceByKey(_ + _)
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def masteridretentiontotal(data: (RDD[(String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._3, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), x._2), 1)
          } else {
            ((v(5), v(5), x._2), 1)
          }
        } else {
          (("null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _)
      .map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def masteridretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))
        if("0".equals(status)){
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), data._5.value.getOrElse(v(6), "null"), "未留样"), 1)
            } else {
              ((v(5), v(6),  "未留样"), 1)
            }
          } else {
            (("null", "null", "未留样"), 1)
          }
        }else{
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), data._5.value.getOrElse(v(6), "null"), "已留样"), 1)
            } else {
              ((v(5), v(6), "已留样"), 1)
            }
          } else {
            (("null", "null", "已留样"), 1)
          }
        }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2)).reduceByKey(_ + _)
      .map(x => ("school-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def natureretentionstatus(data: (RDD[(String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((v(1), v(2), x._2), 1)
      } else {
        (("null", "null", x._2), 1)
      }
    })
      .reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def areanatureretentionstatus(data: (RDD[(String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((x._1,v(1), v(2), x._2), 1)
      } else {
        ((x._1,"null", "null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => ("nat-area"+"_"+x._1._1+"_"+"nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def natureretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("status_")(1)

      val v = data._4.value.getOrElse(schoolid, List("null"))

      if("0".equals(status)){
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), "未留样"), 1)
        } else {
          (("null", "null", "未留样"), 1)
        }
      }else{
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), "已留样"), 1)
        } else {
          (("null", "null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2))
      .reduceByKey(_ + _).map(x => ("school-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def areanatureretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("status_")(1)

      val v = data._4.value.getOrElse(schoolid, List("null"))

      if("0".equals(status)){
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area,v(1), v(2),  "未留样"), 1)
        } else {
          ((area,"null", "null",  "未留样"), 1)
        }
      }else{
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area,v(1), v(2),  "已留样"), 1)
        } else {
          ((area,"null", "null",  "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3,x._1._4), x._2))
      .reduceByKey(_ + _).map(x => ("school-nat-area"+"_"+x._1._1+"_"+"nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def levelreretentionstatus(data: (RDD[(String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((v(0), x._2), 1)
      } else {
        (("null", x._2), 1)
      }
    })
      .reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + x._1._2, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def arealevelreretentionstatus(data: (RDD[(String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((x._1,v(0), x._2), 1)
      } else {
        ((x._1,"null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => ("lev-area"+"_"+x._1._1+"_"+"level" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def levelreretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("status_")(1)

      val v = data._4.value.getOrElse(schoolid, List("null"))
      if("0".equals(status)){
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), "未留样"), 1)
        } else {
          (("null","未留样"), 1)
        }
      }else{
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), "已留样"), 1)
        } else {
          (("null","已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2), x._2))
      .reduceByKey(_ + _).map(x => ("school-level" + "_" + x._1._1 + "_" + x._1._2, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def arealevelreretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("status_")(1)
      val v = data._4.value.getOrElse(schoolid, List("null"))
      if("0".equals(status)){
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area,v(0), "未留样"), 1)
        } else {
          ((area,"null","未留样"), 1)
        }
      }else{
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area,v(0), "已留样"), 1)
        } else {
          ((area,"null","已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2,x._1._3), x._2))
      .reduceByKey(_ + _).map(x => ("school-lev-area"+"_"+x._1._1+"_"+"level" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .cogroup(data._3)
      .filter(x => x._1.size > 2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, "0")
            } else {
              jedis.hset(data._2 + "_gc-retentiondishtotal", k, v._1.head.toString)
            }
        })
    })
  }
}
