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

  override def departmentareaplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val department = data._3.value.getOrElse(schoolid, List("null"))(9)
        if (x._2.split("_").size > 2) {
          ((area, department, x._2.split("_create-time")(0)), 1)
        } else {
          ((area, department, x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._2, x._1._1 + "_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
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

  override def departmentlevelplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, v(0), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, v(0), x._2), 1)
          } else {
            ((department, "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "level" + "_" + x._1._2 + "_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
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

  override def departmentarealevelplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, area, v(0), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, area, "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, area, v(0), x._2), 1)
          } else {
            ((department, area, "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + x._1._4, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
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

  override def departmentnatureplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, v(1), v(2), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, v(1), v(2), x._2), 1)
          } else {
            ((department, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
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

  override def departmentareanatureplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, area, v(1), v(2), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, area, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, area, v(1), v(2), x._2), 1)
          } else {
            ((department, area, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + x._1._5, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
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

  override def departmentcanteenplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, v(3), v(4), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, v(3), v(4), x._2), 1)
          } else {
            ((department, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "canteenmode" + "_" + x._1._2 + "_" + "ledgertype" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
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

  override def departmentareacanteenplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, area, v(3), v(4), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, area, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, area, v(3), v(4), x._2), 1)
          } else {
            ((department, area, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "canteenmode" + "_" + x._1._3 + "_" + "ledgertype" + "_" + x._1._4 + "_" + x._1._5, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def masteridplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
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

  override def departmentmasteridplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), data._4.value.getOrElse(v(6), "null"), x._2.split("_create-time")(0)), 1)
            } else {
              ((department, v(5), v(6), x._2.split("_create-time")(0)), 1)
            }

          } else {
            ((department, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), data._4.value.getOrElse(v(6), "null"), x._2), 1)
            } else {
              ((department, v(5), v(6), x._2), 1)
            }

          } else {
            ((department, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def shanghaidepartmentplatoontotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(9)) && !v(9).equals("null")) {
          ((v(9), x._2), 1)
        } else {
          ((v(9), x._2), 1)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("department" + "_" + x._1._1 + "_" + x._1._2, x._2))
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

  override def departmentdepartmentplatoontotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          ((department, x._2.split("_create-time")(0)), 1)

        } else {
          ((department, x._2), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "department" + "_" + x._1._1 + "_" + x._1._2, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def areaplastatustotal(data: (RDD[(String, String)], RDD[(String, String)], String)): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val plastatus = x._2.split("_")(7)
        ((area, plastatus), 1)
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2)).cogroup(data._2).foreachPartition({
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

  override def departmentareaplastatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val department = data._3.value.getOrElse(schoolid, List("null"))(9)
        val plastatus = x._2.split("_")(7)
        ((department, area, plastatus), 1)

    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
        })
    })
  }

  override def levelplastatustotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val plastatus = x._2.split("_")(7)
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), plastatus), 1)
        } else {
          (("null", plastatus), 1)
        }
    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2)).cogroup(data._2)
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

  override def departmentlevelplastatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val plastatus = x._2.split("_")(7)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), plastatus), 1)
        } else {
          ((department, "null", plastatus), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "level" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
        })
    })
  }

  override def natureplastatustotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val id = x._1.split("_")(1)
        val plastatus = x._2.split("_")(7)
        val v = data._4.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), plastatus), 1)
        } else {
          (("null", "null", plastatus), 1)
        }
    }).reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2)).cogroup(data._2)
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

  override def departmentnatureplastatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        val plastatus = x._2.split("_")(7)

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), plastatus), 1)
        } else {
          ((department, "null", "null", plastatus), 1)
        }


    }).reduceByKey(_ + _).map(x => (x._1._1, "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "plastatus" + "_" + x._1._4, x._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
            jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
        })
    })
  }

  override def masteridplastatustotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        val plastatus = x._2.split("_")(7)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), data._5.value.getOrElse(v(6), "null"), plastatus), 1)
          } else {
            ((v(5), v(6), plastatus), 1)
          }
        } else {
          (("null", "null", plastatus), 0)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2)).cogroup(data._2)
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

  override def departmentmasteridplastatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        val plastatus = x._2.split("_")(7)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), data._4.value.getOrElse(v(6), "null"), plastatus), 1)
          } else {
            ((department, v(5), v(6), plastatus), 1)
          }

        } else {
          ((department, "null", "null", plastatus), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "plastatus" + "_" + x._1._4, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }

  override def shanghaidepartmentplastatustotal(data: (RDD[(String, String)], RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        val id = x._1.split("_")(1)
        val v = data._4.value.getOrElse(id, List("null"))
        val plastatus = x._2.split("_")(7)
        if (StringUtils.isNoneEmpty(v(9)) && !v(9).equals("null")) {
          ((v(9), plastatus), 1)
        } else {
          ((v(9), plastatus), 1)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("department" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2))
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

  override def departmentdepartmentplastatustotal(data: (RDD[(String, String)], String, Broadcast[Map[String, List[String]]])): Unit = {
    data._1.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = data._3.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        val plastatus = x._2.split("_")(7)

        ((department, plastatus), 1)


    }).reduceByKey(_ + _).map(x => (x._1._1, "department" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, x._2, x._3.toString)
              jedis.expire(data._2 + "_platoonfeed-total" + "_" + "department" + "_" + x._1, 194400)
          })
      })
  }
}
