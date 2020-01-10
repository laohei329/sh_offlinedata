package com.ssic.service

import com.ssic.impl.RetentionTotalFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class RetentionTotalStat extends RetentionTotalFunc {
  override def arearetentiontotal(data: (RDD[(String, String, String, String)], String)): Unit = {

    data._1.map(x => (x._1, 1)).reduceByKey(_ + _).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal", x._1, x._2.toString)
        })

    })
  }

  override def departmentarearetentiontotal(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._3
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        ((department, area), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, x._1._2, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
        })
    })
  }

  override def arearetentionstatustotal(data: (RDD[(String, String, String, String)], String, RDD[(String, String)])): Unit = {
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

  override def departmentarearetentionstatustotal(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._3
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        val status = x._2
        ((department, area, status), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, x._1._2 + "_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
        })
    })
  }

  override def arearetentionrulestatustotal(data: (RDD[(String, String, String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._3
        val reservestatus = x._4
        ((area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => ("re-area" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentarearetentionrulestatustotal(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1
        val school_id = x._3
        val reservestatus = x._4
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        ((department, area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-area" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
        })
    })

  }

  override def arearetentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {

    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("_")(7)
        var stat = "null"
        if ("0".equals(status)) {
          stat = "未留样"
        } else {
          stat = "已留样"
        }
        (area, stat)
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

  override def departmentarearetentionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("_")(7)
        val school_id = x._1.split("_")(3)
        val department = data._3.value.getOrElse(school_id, List("null"))(9)
        var stat = "null"
        if ("0".equals(status)) {
          stat = "未留样"
        } else {
          stat = "已留样"
        }
        ((department, area, stat), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "school-area" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def arearetentionschoolrulestatus(data: (RDD[(String, String)], String, RDD[(String, String)])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val reservestatus = x._2.split("_")(9)

        ((area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => ("re-school-area" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2)).cogroup(data._3)
      .foreachPartition({
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

  override def departmentarearetentionschoolrulestatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val area = x._1.split("_")(1)
        val reservestatus = x._2.split("_")(9)
        val school_id = x._1.split("_")(3)
        val department = data._3.value.getOrElse(school_id, List("null"))(9)

        ((department, area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-area" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def masteridretentiontotal(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val v = data._4.value.getOrElse(x._3, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), x._2), 1)
          } else {
            ((v(5), v(6), x._2), 1)
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

  override def departmentmasteridretentiontotal(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._3
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), data._4.value.getOrElse(v(6), "null"), x._2), 1)
          } else {
            ((department, v(5), v(6), x._2), 1)
          }
        } else {
          ((department, "null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def masteridretentionruletotal(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._3
        val v = data._4.value.getOrElse(school_id, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), x._4), 1)
          } else {
            ((v(5), v(6), x._4), 1)
          }
        } else {
          (("null", "null", x._4), 1)
        }
    }).reduceByKey(_ + _).map(x => ("re-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentmasteridretentionruletotal(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val school_id = x._3
        val v = data._3.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), data._4.value.getOrElse(v(6), "null"), x._4), 1)
          } else {
            ((department, v(5), v(6), x._4), 1)
          }
        } else {
          ((department, "null", "null", x._4), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def masteridretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {

    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(7)

        val v = data._4.value.getOrElse(schoolid, List("null"))
        if ("0".equals(status)) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), data._5.value.getOrElse(v(6), "null"), "未留样"), 1)
            } else {
              ((v(5), v(6), "未留样"), 1)
            }
          } else {
            (("null", "null", "未留样"), 1)
          }
        } else {
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


  override def departmentmasteridretentionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(7)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if ("0".equals(status)) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), data._4.value.getOrElse(v(6), "null"), "未留样"), 1)
            } else {
              ((department, v(5), v(6), "未留样"), 1)
            }
          } else {
            ((department, "null", "null", "未留样"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), data._4.value.getOrElse(v(6), "null"), "已留样"), 1)
            } else {
              ((department, v(5), v(6), "已留样"), 1)
            }
          } else {
            ((department, "null", "null", "已留样"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def masteridretentionschoolrulestatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val reservestatus = x._2.split("_")(9)

        val v = data._4.value.getOrElse(schoolid, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), reservestatus), 1)
          } else {
            ((v(5), v(6), reservestatus), 1)
          }
        } else {
          (("null", "null", reservestatus), 1)
        }


    }).reduceByKey(_ + _).map(x => ("re-school-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentmasteridretentionschoolrulestatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val reservestatus = x._2.split("_")(9)

        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), data._4.value.getOrElse(v(6), "null"), reservestatus), 1)
          } else {
            ((department, v(5), v(6), reservestatus), 1)
          }
        } else {
          ((department, "null", "null", reservestatus), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def natureretentionstatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

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

  override def departmentnatureretentionstatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((department, v(1), v(2), x._2), 1)
      } else {
        ((department, "null", "null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def natureretentionrulestatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((v(1), v(2), x._4), 1)
      } else {
        (("null", "null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentnatureretentionrulestatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((department, v(1), v(2), x._4), 1)
      } else {
        ((department, "null", "null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def areanatureretentionstatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((x._1, v(1), v(2), x._2), 1)
      } else {
        ((x._1, "null", "null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => ("nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2))
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
      val status = x._2.split("_")(7)

      val v = data._4.value.getOrElse(schoolid, List("null"))

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), "未留样"), 1)
        } else {
          (("null", "null", "未留样"), 1)
        }
      } else {
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

  override def departmentnatureretentionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = data._3.value.getOrElse(schoolid, List("null"))
      val department = v(9)

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), "未留样"), 1)
        } else {
          ((department, "null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), "已留样"), 1)
        } else {
          ((department, "null", "null", "已留样"), 1)
        }
      }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def natureretentionschoolrulestatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = data._4.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((v(1), v(2), reservestatus), 1)
      } else {
        (("null", "null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-school-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentnatureretentionschoolrulestatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = data._3.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((department, v(1), v(2), reservestatus), 1)
      } else {
        ((department, "null", "null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
        })
    })
  }

  override def areanatureretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = data._4.value.getOrElse(schoolid, List("null"))

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), "未留样"), 1)
        } else {
          ((area, "null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), "已留样"), 1)
        } else {
          ((area, "null", "null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3, x._1._4), x._2))
      .reduceByKey(_ + _).map(x => ("school-nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2))
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

  override def levelreretentionstatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

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

  override def departmentlevelreretentionstatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((department, v(0), x._2), 1)
      } else {
        ((department, "null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "level" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def levelreretentionrulestatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((v(0), x._4), 1)
      } else {
        (("null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-level" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentlevelreretentionrulestatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((department, v(0), x._4), 1)
      } else {
        ((department, "null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-level" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def arealevelreretentionstatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((x._1, v(0), x._2), 1)
      } else {
        ((x._1, "null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => ("lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + x._1._3, x._2))
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
      val status = x._2.split("_")(7)

      val v = data._4.value.getOrElse(schoolid, List("null"))
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), "未留样"), 1)
        } else {
          (("null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), "已留样"), 1)
        } else {
          (("null", "已留样"), 1)
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

  override def departmentlevelreretentionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = data._3.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), "未留样"), 1)
        } else {
          ((department, "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), "已留样"), 1)
        } else {
          ((department, "null", "已留样"), 1)
        }
      }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-level" + "_" + x._1._2 + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def levelreretentionschoolrulestatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = data._4.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((v(0), reservestatus), 1)
      } else {
        (("null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-school-level" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentlevelreretentionschoolrulestatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = data._3.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((department, v(0), reservestatus), 1)
      } else {
        ((department, "null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-level" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def arealevelreretentionschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val v = data._4.value.getOrElse(schoolid, List("null"))
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), "未留样"), 1)
        } else {
          ((area, "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), "已留样"), 1)
        } else {
          ((area, "null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2))
      .reduceByKey(_ + _).map(x => ("school-lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + x._1._3, x._2))
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

  override def departmentstatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._4.value.getOrElse(x._3, List("null"))
      val department = v(9)
      ((department, x._2), 1)
    }).reduceByKey(_ + _).map(x => ("department" + "_" + x._1._1 + "_" + x._1._2, x._2)).cogroup(data._3).foreachPartition({
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

  override def departmentdepartmentstatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      ((department, x._2), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "department" + "_" + x._1._1 + "_" + x._1._2, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def departmentrulestatus(data: (RDD[(String, String, String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._3
      val reservestatus = x._4
      val department = data._4.value.getOrElse(schoolid, List("null"))(9)
      ((department, reservestatus), 1)

    }).reduceByKey(_ + _).map(x => ("re-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2)).cogroup(data._3).foreachPartition({
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

  override def departmentdepartmentrulestatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val schoolid = x._3
      val reservestatus = x._4
      val department = data._3.value.getOrElse(schoolid, List("null"))(9)
      ((department, reservestatus), 1)

    }).reduceByKey(_ + _).map(x => (x._1._1, "re-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def departmentschoolstatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val department = data._4.value.getOrElse(schoolid, List("null"))(9)
      if ("0".equals(status)) {
        ((department, "未留样"), 1)
      } else {
        ((department, "已留样"), 1)
      }

    }).reduceByKey(_ + _).map(x => ( "school-department" + "_" + x._1._1 + "_" + x._1._2, x._2)).cogroup(data._3).foreachPartition({
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

  override def departmentdepartmentschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val department = data._3.value.getOrElse(schoolid, List("null"))(9)
      if ("0".equals(status)) {
        ((department, "未留样"), 1)
      } else {
        ((department, "已留样"), 1)
      }

    }).reduceByKey(_ + _).map(x => ( x._1._1,"school-department" + "_" + x._1._1 + "_" + x._1._2, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def departmentschoolrulestatus(data: (RDD[(String, String)], String, RDD[(String, String)], Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)
      val department = data._4.value.getOrElse(schoolid, List("null"))(9)
      ((department,reservestatus),1)

    }).reduceByKey(_+_).map(x => ("re-school-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2))
      .cogroup(data._3).foreachPartition({
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

  override def departmentdepartmentschoolrulestatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)
      val department = data._3.value.getOrElse(schoolid, List("null"))(9)
      ((department,reservestatus),1)

    }).reduceByKey(_+_).map(x => (x._1._1,"re-school-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def departmentareanatureretentionstatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((department,x._1, v(1), v(2), x._2), 1)
      } else {
        ((department,x._1, "null", "null", x._2), 1)
      }

    }).reduceByKey(_ + _).map(x => (x._1._1,"nat-area" + "_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + x._1._5, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
        })
    })

  }

  override def departmentarealevelreretentionstatus(data: (RDD[(String, String, String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {
      val v = data._3.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((department,x._1, v(0), x._2), 1)
      } else {
        ((department,x._1, "null", x._2), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1,"lev-area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }

  override def departmentreanatureretentionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = data._3.value.getOrElse(schoolid, List("null"))
      val department = v(9)

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,area, v(1), v(2), "未留样"), 1)
        } else {
          ((department,area, "null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,area, v(1), v(2), "已留样"), 1)
        } else {
          ((department,area, "null", "null", "已留样"), 1)
        }
      }

    })
      .reduceByKey(_ + _).map(x => (x._1._1,"school-nat-area" + "_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + x._1._5, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
        })
    })
  }

  override def departmentareanatureretentionschoolstatus(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {

    data._1.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val v = data._3.value.getOrElse(schoolid, List("null"))
      val department =v(9)
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,area, v(0), "未留样"), 1)
        } else {
          ((department,area, "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,area, v(0), "已留样"), 1)
        } else {
          ((department,area, "null", "已留样"), 1)
        }
      }

    }).reduceByKey(_+_).map(x => (x._1._1,"school-lev-area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_gc-retentiondishtotal" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
          })
      })
  }
}
