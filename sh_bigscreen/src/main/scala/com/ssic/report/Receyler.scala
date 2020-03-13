package com.ssic.report

import java.sql.DriverManager
import java.util

import com.ssic.beans.SchoolBean
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.kafka.common.TopicPartition
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory
import redis.clients.jedis.exceptions.JedisDataException

import scala.collection.JavaConverters._

/**
  * 回收垃圾，油脂的计算指标
  */
object Receyler {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)

  def waste(filterData: RDD[SchoolBean]): RDD[(String, String, Int, String, String, Int, Int, String, String, String, String, String, String, Int, Int, String)] = {
    val recycler = filterData.filter(x => x != null && x.table.equals("t_pro_recycler_waste"))

    val data = recycler.map({
      x =>
        val id = x.data.id
        val source_id = x.data.source_id //团餐公司Id或者学校Id
      val district_id = x.data.district_id
        val platform_type = x.data.platform_type //'1为教委端 2为团餐端'
      val types = x.data.`type`.toInt
        //'1餐厨垃圾，2废弃油脂',
        val secont_type = x.data.secont_type //'针对大类 2(1废油，2 含油废水)  默认为0(无特别含义)'
      val receiver_name = x.data.recycler_name //回收单位
      var recycler_number = "0"
        if (StringUtils.isNoneEmpty(x.data.recycler_number) && !x.data.recycler_number.equals("null") && !x.data.recycler_number.equals("")) {
          recycler_number = x.data.recycler_number //回收单据数量
        } else {
          recycler_number
        }

        val recycler_date = x.data.recycler_date //回收日期
      val replaceAll = recycler_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val contact = x.data.contact //回收人
      val stat = x.data.stat
        val recycler_documents = x.data.recycler_documents //回收单据数量
        if (platform_type == 1) {
          if ("insert".equals(x.`type`) && "1".equals(x.data.stat)) {
            ("insert", id, platform_type, source_id, district_id, types, secont_type, receiver_name, recycler_number, date, contact, stat, recycler_documents, 0, 0, "null")
          } else if ("delete".equals(x.`type`)) {
            ("delete", id, platform_type, source_id, district_id, types, secont_type, receiver_name, recycler_number, date, contact, stat, recycler_documents, 0, 0, "null")
          } else if ("update".equals(x.`type`)) {
            ("update", id, platform_type, source_id, district_id, types, secont_type, receiver_name, recycler_number, date, contact, stat, recycler_documents, Rule.nullToZero(x.old.`type`), x.old.secont_type, x.old.stat)
          } else {
            ("null", "null", 0, "null", "null", 0, 0, "null", "null", "null", "null", "null", "null", 0, 0, "null")
          }
        } else {
          var supplier_type = Map[String, Int]()
          val conn = DriverManager.getConnection("jdbc:mysql://172.18.14.23:3306/saas_v1", "azkaban", "nn1.hadoop@ssic.cn")
          val pstmt = conn.prepareStatement(s"select * from t_edu_school_supplier where stat=1 and supplier_id=?")
          pstmt.setString(1, source_id)
          val resultSet = pstmt.executeQuery()
          while (resultSet.next()) {
            supplier_type += resultSet.getString("supplier_id") -> resultSet.getInt("industry_type")
          }
          conn.close()
          if (supplier_type.size == 0) {
            ("null", "null", 0, "null", "null", 0, 0, "null", "null", "null", "null", "null", "null", 0, 0, "null")
          } else {
            val industry_type = supplier_type.head._2
            if (industry_type == 1) {
              if ("insert".equals(x.`type`) && "1".equals(x.data.stat)) {
                ("insert", id, platform_type, source_id, district_id, types, secont_type, receiver_name, recycler_number, date, contact, stat, recycler_documents, 0, 0, "null")
              } else if ("delete".equals(x.`type`)) {
                ("delete", id, platform_type, source_id, district_id, types, secont_type, receiver_name, recycler_number, date, contact, stat, recycler_documents, 0, 0, "null")
              } else if ("update".equals(x.`type`)) {
                ("update", id, platform_type, source_id, district_id, types, secont_type, receiver_name, recycler_number, date, contact, stat, recycler_documents, Rule.nullToZero(x.old.`type`), x.old.secont_type, x.old.stat)
              } else {
                ("null", "null", 0, "null", "null", 0, 0, "null", "null", "null", "null", "null", "null", 0, 0, "null")
              }
            } else {
              ("null", "null", 0, "null", "null", 0, 0, "null", "null", "null", "null", "null", "null", 0, 0, "null")
            }
          }

        }


    })
    data
  }

  /**
    *
    * * 餐厨垃圾和废弃油脂数据存入到redis的表中
    *
    * * @param RDD[SchoolBean] binlog日志数据
    *
    */

  def wastedata(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])) = {
    val data = waste(filterData._1).distinct().filter(x => !x._1.equals("null")).map({
      x =>
        var recyclerNumber = 0.toDouble
        try {
          recyclerNumber = x._9.toDouble
        } catch {
          case e: Exception =>
            recyclerNumber
        }
        (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, recyclerNumber, x._10, x._11, x._12, x._13, x._14, x._15, x._16, x._9)
      //        if (x._9.matches("[0-9]*").equals(true)) {
      //          (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, x._9, x._10, x._11, x._12, x._13, x._14, x._15, x._16)
      //        } else {
      //          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
      //        }
    }).filter(x => !x._1.equals("null"))
    data.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            //按照区将上海市的过滤出来
            if ("insert".equals(x._1) && "1".equals(x._12)) {
              //餐厨垃圾
              if (x._6 == 1) {
                //教委端
                if (x._3 == 1) {
                  val area = filterData._3.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area)) {

                    jedis.hincrBy(x._10 + "_" + "schoolwastetotal", area + "_" + "total", 1)
                    logger.info(x._10 + "_" + x._9 + "_" + x._4 + "_" + area + "学校回收垃圾的插入数据-------------")
                    jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area, x._9.toDouble)
                    jedis.hset(x._10 + "_" + "schoolwaste", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)

                    //教属的垃圾回收数据
                    val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                    if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                      if ("3".equals(masterid(1))) {
                        jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", x._9.toDouble)
                      } else {
                        jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", x._9.toDouble)
                      }
                    }
                  }


                } else {
                  //将团餐公司的区号为上海市的和区数据为空的过滤出来
                  val area = filterData._2.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area) || "null".equals(area)) {

                    //团餐公司端
                    jedis.hincrBy(x._10 + "_" + "supplierwastetotal", area + "_" + "total", 1)

                    jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area, x._9.toDouble)
                    jedis.hset(x._10 + "_" + "supplierwaste", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)
                  }

                }
              } else {
                //废弃油脂
                if (x._3 == 1) {

                  val area = filterData._3.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area)) {
                    //教委端
                    jedis.hincrBy(x._10 + "_" + "schooloiltotal", area + "_" + "total", 1)

                    jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", area, x._9.toDouble)
                    jedis.hset(x._10 + "_" + "schooloil", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)

                    //教属的废弃油脂回收数据
                    val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                    if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                      if ("3".equals(masterid(1))) {
                        jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", x._9.toDouble)
                      } else {
                        jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", x._9.toDouble)
                      }
                    }
                  }


                } else {

                  val area = filterData._2.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area) || "null".equals(area)) {
                    //团餐公司端
                    jedis.hincrBy(x._10 + "_" + "supplieroiltotal", area + "_" + "total", 1)

                    jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area, x._9.toDouble)
                    jedis.hset(x._10 + "_" + "supplieroil", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)
                  }

                }
              }
            } else if ("delete".equals(x._1)) {
              //餐厨垃圾
              if (x._6 == 1) {
                //教委端
                if (x._3 == 1) {
                  val area = filterData._3.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area)) {
                    val hkeys: util.Set[String] = jedis.hkeys(x._10 + "_" + "schoolwaste")
                    if (hkeys.asScala.contains(x._2).equals(true)) {
                      jedis.hincrBy(x._10 + "_" + "schoolwastetotal", area + "_" + "total", -1)

                      jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area, -x._9.toDouble)
                      jedis.hdel(x._10 + "_" + "schoolwaste", x._2)

                      //教属的垃圾回收数据
                      val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", -x._9.toDouble)
                        } else {
                          jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", -x._9.toDouble)
                        }
                      }
                    } else {
                      logger.info("这个删除的学校餐厨垃圾信息之前的redis里没有计算，所以不纳入计算")
                    }
                  }

                } else {
                  //团餐公司端
                  val area = filterData._2.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area) || "null".equals(area)) {
                    val hkeys1: util.Set[String] = jedis.hkeys(x._10 + "_" + "supplierwaste")
                    if (hkeys1.asScala.contains(x._2).equals(true)) {
                      jedis.hincrBy(x._10 + "_" + "supplierwastetotal", area + "_" + "total", -1)

                      jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area, -x._9.toDouble)
                      jedis.hdel(x._10 + "_" + "supplierwaste", x._2)
                    } else {
                      logger.info("这个删除的团餐公司餐厨垃圾信息之前的redis里没有计算，所以不纳入计算")
                    }
                  }

                }
              } else {
                //废弃油脂
                val hkeys2 = jedis.hkeys(x._10 + "_" + "schooloil")
                if (hkeys2.asScala.contains(x._2).equals(true)) {
                  if (x._3 == 1) {
                    //教委端
                    val area = filterData._3.value.getOrElse(x._4, "null")
                    if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area)) {
                      jedis.hincrBy(x._10 + "_" + "schooloiltotal", area + "_" + "total", -1)

                      jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", area, -x._9.toDouble)
                      jedis.hdel(x._10 + "_" + "schooloil", x._2)

                      //教属的废弃油脂回收数据
                      val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", -x._9.toDouble)
                        } else {
                          jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", -x._9.toDouble)
                        }
                      }
                    }

                  } else {
                    val area = filterData._2.value.getOrElse(x._4, "null")
                    if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area) || "null".equals(area)) {
                      //团餐公司端
                      jedis.hincrBy(x._10 + "_" + "supplieroiltotal", area + "_" + "total", -1)

                      jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area, -x._9.toDouble)
                      jedis.hdel(x._10 + "_" + "supplieroil", x._2)
                    }

                  }

                } else {
                  logger.info("这个删除的团餐公司废弃油脂信息之前的redis里没有计算，所以不纳入计算")
                }
              }
            } else if ("update".equals(x._1)) {
              //餐厨垃圾
              if (x._6 == 1) {
                //教委端
                if (x._3 == 1) {
                  val area = filterData._3.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area)) {
                    val hkeys: util.Set[String] = jedis.hkeys(x._10 + "_" + "schoolwaste")
                    if (hkeys.asScala.contains(x._2).equals(true)) {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("1".equals(x._16) && "0".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "schoolwastetotal", area + "_" + "total", -1)

                          jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area, -x._9.toDouble)
                          jedis.hdel(x._10 + "_" + "schoolwaste", x._2)


                          //教属的垃圾回收数据
                          val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                          if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                            if ("3".equals(masterid(1))) {
                              jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", -x._9.toDouble)
                            } else {
                              jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", -x._9.toDouble)
                            }
                          }
                        } else {
                          logger.info("这个学校餐厨垃圾更新不计算，不是无效数据的更新")
                        }
                      } else {
                        jedis.hset(x._10 + "_" + "schoolwaste", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)
                      }
                    } else {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("0".equals(x._16) && "1".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "schoolwastetotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "schoolwaste", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)


                          //教属的垃圾回收数据
                          val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                          if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                            if ("3".equals(masterid(1))) {
                              jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", x._9.toDouble)
                            } else {
                              jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", x._9.toDouble)
                            }
                          }
                        } else {
                          logger.info("这个学校餐厨垃圾更新不计算，是无效数据到有效数据的更新")
                        }
                      } else {
                        //从废弃油脂转成餐厨垃圾
                        if (x._14 == 2 && x._6 == 1) {
                          jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "schoolwaste", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)

                          //教属的垃圾回收数据
                          val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                          if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                            if ("3".equals(masterid(1))) {
                              jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", x._9.toDouble)
                            } else {
                              jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", x._9.toDouble)
                            }
                          }

                          val hkeys4 = jedis.hkeys(x._10 + "_" + "schooloil")
                          if (hkeys4.asScala.contains(x._2).equals(true)) {
                            jedis.hincrBy(x._10 + "_" + "schooloiltotal", area + "_" + "total", -1)
                            jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", area, -x._9.toDouble)
                            jedis.hdel(x._10 + "_" + "schooloil", x._2)


                            //教属的废弃油脂回收数据
                            val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                            if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                              if ("3".equals(masterid(1))) {
                                jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", -x._9.toDouble)
                              } else {
                                jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", -x._9.toDouble)
                              }
                            }
                          }

                        } else {
                          logger.info("这个学校餐厨垃圾更新不计算，是不在之前的计算的key中")
                        }

                      }
                    }
                  }

                } else {
                  //团餐公司端
                  val area = filterData._2.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area) || "null".equals(area)) {
                    val hkeys2: util.Set[String] = jedis.hkeys(x._10 + "_" + "supplierwaste")
                    if (hkeys2.asScala.contains(x._2).equals(true)) {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("1".equals(x._16) && "0".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "supplierwastetotal", area + "_" + "total", -1)

                          jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area, -x._9.toDouble)
                          jedis.hdel(x._10 + "_" + "supplierwaste", x._2)
                        } else {
                          logger.info("这个团餐公司餐厨垃圾更新不计算，不是无效数据的更新")
                        }
                      } else {
                        jedis.hset(x._10 + "_" + "supplierwaste", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)
                      }
                    } else {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("0".equals(x._16) && "1".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "supplierwastetotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "supplierwaste", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)
                        } else {
                          logger.info("这个团餐公司餐厨垃圾更新不计算，是无效数据到有效数据的更新")
                        }
                      } else {
                        //从废弃油脂转成餐厨垃圾
                        if (x._14 == 2 && x._6 == 1) {
                          jedis.hincrBy(x._10 + "_" + "supplierwastetotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "supplierwaste", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13)

                          val hkeys3 = jedis.hkeys(x._10 + "_" + "supplieroil")
                          if (hkeys3.asScala.contains(x._2).equals(true)) {
                            jedis.hincrBy(x._10 + "_" + "supplieroiltotal", area + "_" + "total", -1)

                            jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area, -x._9.toDouble)
                            jedis.hdel(x._10 + "_" + "supplieroil", x._2)
                          } else {
                            logger.info("从废弃油脂转成餐厨垃圾的更新数据，之前的key中没有这个数据")
                          }
                        } else {
                          logger.info("这个学校餐厨垃圾更新不计算，是不在之前的计算的key中")
                        }

                      }
                    }
                  }

                }
              } else {
                //废弃油脂
                if (x._3 == 1) {
                  //教委端
                  val area = filterData._3.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area)) {
                    val strings = jedis.hkeys(x._10 + "_" + "schooloil")
                    if (strings.asScala.contains(x._2).equals(true)) {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("1".equals(x._16) && "0".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "schooloiltotal", area + "_" + "total", -1)

                          jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", area, -x._9.toDouble)
                          jedis.hdel(x._10 + "_" + "schooloil", x._2)


                          //教属的废弃油脂回收数据
                          val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                          if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                            if ("3".equals(masterid(1))) {
                              jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", -x._9.toDouble)
                            } else {
                              jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", -x._9.toDouble)
                            }
                          }
                        } else {
                          logger.info("这个团餐公司废弃油脂更新不计算，是有效数据到无效数据")
                        }
                      } else {
                        jedis.hset(x._10 + "_" + "schooloil", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)
                      }
                    } else {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("0".equals(x._16) && "1".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "schooloiltotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "schooloil", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)


                          //教属的废弃油脂回收数据
                          val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                          if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                            if ("3".equals(masterid(1))) {
                              jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", x._9.toDouble)
                            } else {
                              jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", x._9.toDouble)
                            }
                          }
                        } else {
                          logger.info("这个团餐公司废弃油脂更新不计算，是有效数据到无效数据")
                        }
                      } else {
                        //从餐厨垃圾变成废弃油脂
                        if (x._14 == 1 && x._6 == 2) {
                          jedis.hincrBy(x._10 + "_" + "schooloiltotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "schooloil", x._2, "area" + "_" + area + "_" + "schoolid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)


                          //教属的废弃油脂回收数据
                          val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                          if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                            if ("3".equals(masterid(1))) {
                              jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", x._9.toDouble)
                            } else {
                              jedis.hincrByFloat(x._10 + "_" + "schooloiltotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", x._9.toDouble)
                            }
                          }

                          val hkeys6 = jedis.hkeys(x._10 + "_" + "schoolwaste")
                          if (hkeys6.asScala.contains(x._2).equals(true)) {
                            jedis.hincrBy(x._10 + "_" + "schoolwastetotal", area + "_" + "total", -1)

                            jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", area, -x._9.toDouble)
                            jedis.hdel(x._10 + "_" + "schoolwaste", x._2)


                            //教属的垃圾回收数据
                            val masterid = filterData._4.value.getOrElse(x._4, List("null"))
                            if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                              if ("3".equals(masterid(1))) {
                                jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._5.value.getOrElse(masterid(2), "null") + "_" + "total", -x._9.toDouble)
                              } else {
                                jedis.hincrByFloat(x._10 + "_" + "schoolwastetotal", "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "total", -x._9.toDouble)
                              }
                            }
                          } else {
                            logger.info("从废弃油脂转成餐厨垃圾的更新数据，之前的key中没有这个数据")
                          }
                        } else {
                          logger.info("这个学校餐厨垃圾更新不计算，是不在之前的计算的key中")
                        }
                      }
                    }
                  }


                } else {
                  //团餐公司端
                  val area = filterData._2.value.getOrElse(x._4, "null")
                  if ("1".equals(area) || "2".equals(area) || "3".equals(area) || "4".equals(area) || "5".equals(area) || "6".equals(area) || "7".equals(area) || "8".equals(area) || "9".equals(area) || "10".equals(area) || "11".equals(area) || "12".equals(area) || "13".equals(area) || "14".equals(area) || "15".equals(area) || "16".equals(area) || "null".equals(area)) {
                    val strings = jedis.hkeys(x._10 + "_" + "supplieroil")
                    if (strings.asScala.contains(x._2).equals(true)) {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("1".equals(x._16) && "0".equals(x._12)) {
                          jedis.hincrBy(x._10 + "_" + "supplieroiltotal", area + "_" + "total", -1)

                          jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area, -x._9.toDouble)
                          jedis.hdel(x._10 + "_" + "supplieroil", x._2)
                        } else {
                          logger.info("这个团餐公司废弃油脂更新不计算，是有效数据到无效数据")
                        }
                      } else {
                        jedis.hset(x._10 + "_" + "supplieroil", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)
                      }
                    } else {
                      if (StringUtils.isNoneEmpty(x._16) && !x._16.equals("null")) {
                        if ("0".equals(x._16) && "1".equals(x._12)) {
                          jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "supplieroil", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)
                        } else {
                          logger.info("这个团餐公司废弃油脂更新不计算，是有效数据到无效数据")
                        }
                      } else {
                        //从餐厨垃圾变成废弃油脂
                        if (x._14 == 1 && x._6 == 2) {
                          jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area + "_" + "total", 1)

                          jedis.hincrByFloat(x._10 + "_" + "supplieroiltotal", area, x._9.toDouble)
                          jedis.hset(x._10 + "_" + "supplieroil", x._2, "area" + "_" + area + "_" + "supplierid" + "_" + x._4 + "_" + "number" + "_" + x._17 + "_" + "receivername" + "_" + x._8 + "_" + "contact" + "_" + x._11 + "_" + "documents" + "_" + x._13 + "_" + "seconttype" + "_" + x._7)

                          val hkeys4 = jedis.hkeys(x._10 + "_" + "supplierwastetotal")
                          if (hkeys4.asScala.contains(x._2).equals(true)) {
                            jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area + "_" + "total", -1)

                            jedis.hincrByFloat(x._10 + "_" + "supplierwastetotal", area, -x._9.toDouble)
                            jedis.hdel(x._10 + "_" + "supplierwaste", x._2)
                          } else {
                            logger.info("从废弃油脂转成餐厨垃圾的更新数据，之前的key中没有这个数据")
                          }
                        } else {
                          logger.info("这个学校餐厨垃圾更新不计算，是不在之前的计算的key中")
                        }
                      }
                    }
                  }

                }

              }
            }
        })
    })
  }

  def wasteSchool(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]], Broadcast[Map[String, String]])) = {
    //platform_type, source_id, types,date
    waste(filterData._1).filter(x => x._1.equals("insert")).map(x => (x._3, x._4, x._6, x._10)).distinct() //.map(x=> ((x._1,x._2,x._3),1)).reduceByKey(_+_)
      .foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            //教委端
            if (x._1 == 3) {
              //餐厨垃圾
              if (x._3 == 1) {
                jedis.hincrBy(x._4 + "_" + "schoolwastetotal", "school" + "_area_" + filterData._2.value.getOrElse(x._4, "null"), 1)
                //教属的
                val masterid = filterData._3.value.getOrElse(x._2, List("null"))
                if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                  if ("3".equals(masterid(1))) {
                    jedis.hincrBy(x._4 + "_" + "schoolwastetotal", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._4.value.getOrElse(masterid(2), "null"), 1)
                  } else {
                    jedis.hincrBy(x._4 + "_" + "schoolwastetotal", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2), 1)
                  }
                }
              } else {
                //废弃油脂
                jedis.hincrBy(x._4 + "_" + "schooloiltotal", "school" + "_area_" + filterData._2.value.getOrElse(x._4, "null"), 1)
                //教属的
                val masterid = filterData._3.value.getOrElse(x._2, List("null"))
                if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                  if ("3".equals(masterid(1))) {
                    jedis.hincrBy(x._4 + "_" + "schooloiltotal", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + filterData._4.value.getOrElse(masterid(2), "null"), 1)
                  } else {
                    jedis.hincrBy(x._4 + "_" + "schooloiltotal", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2), 1)
                  }
                }
              }

            } else {
              //团餐端
              //餐厨垃圾
              if (x._3 == 1) {
                jedis.hincrBy(x._4 + "_" + "supplierwastetotal", "supplier" + "_area_" + filterData._5.value.getOrElse(x._4, "null"), 1)
              } else {
                //废弃油脂
                jedis.hincrBy(x._4 + "_" + "supplieroiltotal", "supplier" + "_area_" + filterData._5.value.getOrElse(x._4, "null"), 1)

              }
            }
        })
    })
  }

}
