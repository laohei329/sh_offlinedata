package com.ssic.service

import com.ssic.impl.DistributionTotalFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DistributionTotalStat extends DistributionTotalFunc {

  override def areadistributionltotal(data: (RDD[(String, String, String, String, String, String,String)], String)): Unit = {
    data._1.map(x => (x._1, 1)).reduceByKey(_ + _).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_DistributionTotal", "area_" + x._1, x._2.toString)
        })
    })
  }

  override def departmentareadistributionltotal(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._4
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        ((department,area),1)
    }).reduceByKey(_+_).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1._1, "area_" + x._1._2, x._2.toString)
            jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1._1, 604800)
        })
    })
  }

  override def areadistributionstatustotal(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)])): Unit = {
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

  override def departmentareadistributionstatustotal(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._4
        val status = x._6
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        ((department,area,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"area_" + x._1._2 + "_status_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
        })
    })
  }

  override def areadistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("_")(9)
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

  override def departmentareadistributionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("_")(9)
        val school_id = x._1.split("_")(3)
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        ((department,area,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"school-area" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def masteriddistributiontotal(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

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

  override def departmentmasteriddistributiontotal(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._4
        val status = x._6
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), data._4.value.getOrElse(v(6), "null"), status), 1)
          } else {
            ((department,v(5), v(6), status), 1)
          }
        } else {
          ((department,"null", "null", status), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "status_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def masteriddistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)
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

  override def departmentmasteriddistributionschoolstatusdata (data:(RDD[(String, String)],String,Broadcast[Map[String, List[String]]],Broadcast[Map[String,String]]))= {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), data._4.value.getOrElse(v(6), "null"), status),1)
          } else {
            ((department,v(5), v(6),status),1)
          }
        } else {
          ((department,"null", "null", status),1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"school-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "status_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }
  override def naturedistributionstatus(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

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

  override def departmentnaturedistributionstatus(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._4
        val status = x._6
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department,v(1), v(2), "3"), 1)
          } else {
            ((department,"null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department,v(1), v(2), "2"), 1)
          } else {
            ((department,"null", "null", "2"), 1)
          }
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def areanaturedistributionstatus(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

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
        val status = x._2.split("_")(9)

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

  override def departmentnaturedistributionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if ("3".equals(status)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department,v(1), v(2), "3"), 1)
          } else {
            ((department,"null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department,v(1), v(2), "2"), 1)
          } else {
            ((department,"null", "null", "2"), 1)
          }
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"school-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def areanaturedistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)

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

  override def leveldistributionstatus(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

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

  override def departmentleveldistributionstatus(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val v = data._3.value.getOrElse(x._4, List("null"))
        val department =v(9)

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department,v(0), "3"), 1)
          } else {
            ((department,"null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department,v(0), "2"), 1)
          } else {
            ((department,"null", "2"), 1)
          }
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def arealeveldistributionstatus(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

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
        val status = x._2.split("_")(9)

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

  override def departmentleveldistributionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>

        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department =v(9)

        if ("3".equals(status)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department,v(0), "3"), 1)
          } else {
            ((department,"null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department,v(0), "2"), 1)
          } else {
            ((department,"null", "2"), 1)
          }
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"school-level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def arealeveldistributionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)
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

  override def areadisstatus(data: (RDD[(String, String, String, String, String, String,String)], String, RDD[(String, String)])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val disstatus = x._7
        ((area,disstatus),1)

    }).reduceByKey(_+_).map(x => ("dis-area"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentareadisstatus(data: (RDD[(String, String, String, String, String, String, String)], String,Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val disstatus = x._7
        val school_id = x._4
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        ((department,area,disstatus),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-area"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def areaschooldisstatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        ((area,disstatus),1)
    }).reduceByKey(_+_).map(x => ("dis-school-area"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentareaschooldisstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val department = data._3.value.getOrElse(schoolid, List("null"))(9)
        ((department,area,disstatus),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-area"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def naturedisstatus(data: (RDD[(String, String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._4.value.getOrElse(school_id, List("null"))
        val disstatus = x._7

          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), disstatus), 1)
          } else {
            (("null", "null", disstatus), 1)
          }

    }).reduceByKey(_+_).map(x => ("dis-nature"+"_"+x._1._1+"_"+"nature-sub"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentnaturedisstatus(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department=v(9)
        val disstatus = x._7

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,v(1), v(2), disstatus), 1)
        } else {
          ((department,"null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-nature"+"_"+x._1._2+"_"+"nature-sub"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def natureschooldisstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), disstatus), 1)
        } else {
          (("null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-school-nature"+"_"+x._1._1+"_"+"nature-sub"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentnatureschooldisstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,v(1), v(2), disstatus), 1)
        } else {
          ((department,"null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-nature"+"_"+x._1._2+"_"+"nature-sub"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def leveldisstatus(data: (RDD[(String, String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._4.value.getOrElse(school_id, List("null"))
        val disstatus = x._7

          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), disstatus), 1)
          } else {
            (("null", disstatus), 1)
          }
    }).reduceByKey(_+_).map(x => ("dis-level"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentleveldisstatus(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._3.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,v(0), disstatus), 1)
        } else {
          ((department,"null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-level"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def levelschooldisstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._4.value.getOrElse(schoolid, List("null"))

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), disstatus), 1)
        } else {
          (("null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-school-level"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentlevelschooldisstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department=v(9)

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,v(0), disstatus), 1)
        } else {
          ((department,"null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-level"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })


  }

  override def masteriddisstatus(data: (RDD[(String, String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val school_id = x._4
        val v = data._4.value.getOrElse(school_id, List("null"))
        val disstatus = x._7

        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((v(5), v(6), disstatus), 1)
          }
        } else {
          (("null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-masterid"+"_"+x._1._1+"_"+"slave"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentmasteriddisstatus(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._3.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)

        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), data._4.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((department,v(5), v(6), disstatus), 1)
          }
        } else {
          ((department,"null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-masterid"+"_"+x._1._2+"_"+"slave"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def masteridschooldisstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((v(5), v(6), disstatus), 1)
          }
        } else {
          (("null", "null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => ("dis-school-masterid"+"_"+x._1._1+"_"+"slave"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentmasteridschooldisstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), data._4.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((department,v(5), v(6), disstatus), 1)
          }
        } else {
          ((department,"null", "null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-masterid"+"_"+x._1._2+"_"+"slave"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def dedistributionstatustotal(data: (RDD[(String, String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._4.value.getOrElse(school_id, List("null"))
        val status = x._6
        val department=v(9)
        ((department,status),1)

    }).reduceByKey(_+_).map(x => ("department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def dedepartmentdistributionstatustotal(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._3.value.getOrElse(school_id, List("null"))
        val status = x._6
        val department=v(9)
        ((department,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def dedistributionschoolstatustotal(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        ((department,status),1)
    }).reduceByKey(_+_).map(x => ("school-department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def dedepartmentdistributionschoolstatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(9)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        ((department,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"school-department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def dedisstatustotal(data: (RDD[(String, String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._4.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x =>("dis-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def dedepartmentdisstatustotal(data: (RDD[(String, String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._4
        val v = data._3.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x =>(x._1._1,"dis-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }

  override def deschooldisstatustotal(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x => ("dis-school-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def dedepartmentschooldisstatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_DistributionTotal"+"_"+"department"+"_"+x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_DistributionTotal" + "_" + "department" + "_" + x._1, 604800)
          })
      })
  }
}
