package com.ssic.service

import com.ssic.impl.UsematerialTotalFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class UsematerialTotalStat extends UsematerialTotalFunc {
  override def areausematerialtotal(data: (RDD[(String, String, String, String, String, String)], String)): Unit = {
    data._1.map(x => (x._1, 1)).reduceByKey(_ + _).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_useMaterialPlanTotal", "area_" + x._1, x._2.toString)
        })
    })
  }


  override def departmentareausematerialtotal(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        val area = x._1
        ((department, area), 1)
    }).reduceByKey(_ + _).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1._1, "area_" + x._1._2, x._2.toString)
            jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
        })
    })
  }

  override def areausmaterialstatustotal(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)])): Unit = {

    data._1.map({
      x =>
        if ("2".equals(x._3)) {
          ((x._1, "2"), 1)
        } else {
          ((x._1, "1"), 1)
        }

    }).reduceByKey(_ + _).map(x => ("area_" + x._1._1 + "_status_" + x._1._2, x._2)).cogroup(data._3)
      .filter(x => x._1.size > 7).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def departmentareausmaterialstatustotal(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        val area = x._1
        var status = "1"
        if ("2".equals(x._3)) {
          status = "2"
        } else {
          status
        }
        ((department, area, status), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "area_" + x._1._2 + "_status_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def areausmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(0)
        val status = x._2.split("status_")(1)
        (area, status)
    }).map((_, 1)).reduceByKey(_ + _).map(x => ("school-area" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }
        })
    })

  }

  override def departmentareausmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(0)
        val school_id = x._1.split("_")(1)
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        val status = x._2.split("status_")(1)
        ((department, area, status), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "school-area" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def masteridusematerialtotal(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._6, List("null"))
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), data._5.value.getOrElse(v(6), "null"), "2"), 1)
            } else {
              ((v(5), v(6), "2"), 1)
            }
          } else {
            (("null", "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), data._5.value.getOrElse(v(6), "null"), "1"), 1)
            } else {
              ((v(5), v(6), "1"), 1)
            }
          } else {
            (("null", "null", "1"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "status_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }
        })
    })
  }

  override def departmentmasteridusematerialtotal(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), data._4.value.getOrElse(v(6), "null"), "2"), 1)
            } else {
              ((department, v(5), v(6), "2"), 1)
            }
          } else {
            ((department, "null", "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), data._4.value.getOrElse(v(6), "null"), "1"), 1)
            } else {
              ((department, v(5), v(6), "1"), 1)
            }
          } else {
            ((department, "null", "null", "1"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "status_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def masteridusmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            (v(5), data._5.value.getOrElse(v(6), "null"), status)
          } else {
            (v(5), v(6), "status")
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
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })


  }

  override def departmentmasteridusmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {


    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), data._4.value.getOrElse(v(6), "null"), status), 1)
          } else {
            ((department, v(5), v(6), "status"), 1)
          }
        } else {
          ((department, "null", "null", status), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "status_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def natureusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._6, List("null"))
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "2"), 1)
          } else {
            (("null", "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "1"), 1)
          } else {
            (("null", "null", "1"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })

  }

  override def departmentnatureusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val school_id = x._6
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, v(1), v(2), "2"), 1)
          } else {
            ((department, "null", "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, v(1), v(2), "1"), 1)
          } else {
            ((department, "null", "null", "1"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def areanatureusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._6, List("null"))
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((x._1, v(1), v(2), "2"), 1)
          } else {
            ((x._1, "null", "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((x._1, v(1), v(2), "1"), 1)
          } else {
            ((x._1, "null", "null", "1"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("nat-area_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentareanatureusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val area = x._1
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, area, v(1), v(2), "2"), 1)
          } else {
            ((department, area, "null", "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, area, v(1), v(2), "1"), 1)
          } else {
            ((department, area, "null", "null", "1"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "nat-area_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + "status" + "_" + x._1._5, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def natureusmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), status), 1)
        } else {
          (("null", "null", status), 1)
        }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2)).reduceByKey(_ + _).map(x => ("school-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentnatureusmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), status), 1)
        } else {
          ((department, "null", "null", status), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def areanatureusmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), status), 1)
        } else {
          ((area, "null", "null", status), 1)
        }

    }).map(x => ((x._1._1, x._1._2, x._1._3, x._1._4), x._2)).reduceByKey(_ + _).map(x => ("school-nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentareanatureusmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, area, v(1), v(2), status), 1)
        } else {
          ((department, area, "null", "null", status), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-nat-area" + "_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + "status" + "_" + x._1._5, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def levelusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._6, List("null"))
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "2"), 1)
          } else {
            (("null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "1"), 1)
          } else {
            (("null", "1"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })

  }

  override def departmentlevelusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val school_id = x._6
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, v(0), "2"), 1)
          } else {
            ((department, "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, v(0), "1"), 1)
          } else {
            ((department, "null", "1"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def arealevelusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._6, List("null"))
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((x._1, v(0), "2"), 1)
          } else {
            ((x._1, "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((x._1, v(0), "1"), 1)
          } else {
            ((x._1, "null", "1"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentarealevelusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val area = x._1
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("2".equals(x._3)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, area, v(0), "2"), 1)
          } else {
            ((department, area, "null", "2"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, area, v(0), "1"), 1)
          } else {
            ((department, area, "null", "1"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "lev-area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def levelusmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), status), 1)
        } else {
          (("null", status), 1)
        }


    }).map(x => ((x._1._1, x._1._2), x._2)).reduceByKey(_ + _).map(x => ("school-level" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2))
      .cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentlevelusmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), status), 1)
        } else {
          ((department, "null", status), 1)
        }


    }).reduceByKey(_ + _).map(x => (x._1._1, "school-level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def arealevelusmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map({
      x =>

        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        val area = x._1.split("_")(0)

        val v = data._4.value.getOrElse(schoolid, List("null"))

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), status), 1)
        } else {
          ((area, "null", status), 1)
        }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2)).reduceByKey(_ + _).map(x => ("school-lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentarealevelusmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        val area = x._1.split("_")(0)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, area, v(0), status), 1)
        } else {
          ((department, area, "null", status), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-lev-area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def departmentusmaterialstatus(data: (RDD[(String, String,String,String,String,String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val department = data._4.value.getOrElse(school_id, List("null"))(9)
        var status = "1"
        if ("2".equals(x._3)) {
          status = "2"
        } else {
          status
        }
        ((department,status),1)
    }).reduceByKey(_+_).map(x => ("department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })
  }

  override def departmentdepartmentusmaterialstatus(data: (RDD[(String, String, String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._6
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        var status = "1"
        if ("2".equals(x._3)) {
          status = "2"
        } else {
          status
        }
        ((department,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def departmentusmaterialschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        val area = x._1.split("_")(0)
        val v = data._4.value.getOrElse(schoolid, List("null"))
        val department = v(9)

        ((department,status),1)
    }).reduceByKey(_+_).map(x => ("school-department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2)).cogroup(data._3).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, "0")
            } else {
              jedis.hset(data._2 + "_useMaterialPlanTotal", k, v._1.head.toString)
            }

        })
    })

  }

  override def departmentdepartmentusmaterialschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(1)
        val status = x._2.split("status_")(1)
        val area = x._1.split("_")(0)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)

        ((department,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"school-department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_useMaterialPlanTotal" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }
}
