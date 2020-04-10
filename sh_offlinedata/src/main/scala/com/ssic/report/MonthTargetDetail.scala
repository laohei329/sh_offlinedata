package com.ssic.report

import java.sql.Timestamp
import java.util.{Calendar, Date}

import com.ssic.report.MonthPlatoon.format
import com.ssic.service.{DealDataStat, TargetDetailStat}
import com.ssic.utils.{JPools, Rule, Tools}
import com.ssic.utils.Tools.{conn, edu_school, edu_school_supplier, url}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object MonthTargetDetail {

  /**
    * 一个月的留样计划的详情数据
    **/

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")
  private val format3 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("一个月的留样计划的详情数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val session = sqlContext.sparkSession

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")

    for (i <- -30 to -1) {
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)
      val datetime = date + " 00:00:00"
      val year = format1.format(time)
      val month = format2.format(time)


      val jedis = JPools.getJedis
      val gcretentiondish = jedis.hgetAll(date + "_gc-retentiondish")
      val gcretentiondishData: RDD[(String, String)] = sc.parallelize(gcretentiondish.asScala.toList) //留样计划已经存在的数据

      hiveContext.sql(s"select * from app_saas_v1.app_t_edu_dish_menu where year='${year}' and month ='${month}' and supply_date ='${datetime}' and have_class='1' ")
        .rdd.map({
        row =>
          val package_id = Rule.emptyToNull(row.getAs[String]("package_id"))
          val area = Rule.emptyToNull(row.getAs[String]("area"))
          val supplier_id = Rule.emptyToNull(row.getAs[String]("supplier_id"))
          val school_id = Rule.emptyToNull(row.getAs[String]("school_id"))
          val menu_group_name = Rule.emptyToNull(row.getAs[String]("menu_group_name"))
          val dishes_name = Rule.emptyToNull(row.getAs[String]("dishes_name"))
          val dishes_names = dishes_name.replaceAll("_","")
          val dishes_number = row.getAs[Integer]("dishes_number")
          val cater_type_name = Rule.emptyToNull(row.getAs[String]("cater_type_name"))
          val reserve_create_time = row.getAs[Timestamp]("reserve_create_time")
          val quantity = row.getAs[Integer]("quantity")
          val remark = Rule.emptyToNull(row.getAs[String]("remark"))
          val supply_date = row.getAs[Timestamp]("supply_date")
          val reserve_hour = row.getAs[Integer]("reserve_hour")
          val reserve_minute = row.getAs[Integer]("reserve_minute")
          val reserve_deal_status = row.getAs[String]("reserve_deal_status")
          val have_reserve = Rule.emptyToNull(row.getAs[Integer]("have_reserve").toString)
          val reserve_creator = Rule.emptyToNull(row.getAs[String]("reserve_creator"))
          val is_consistent = row.getAs[Integer]("is_consistent")
          val is_consistent_remark = Rule.emptyToNull(row.getAs[String]("is_consistent_remark"))


          if ("1".equals(have_reserve)) {

            val value = "area" + "_" + area + "_" + "supplierid" + "_" + supplier_id + "_" + "schoolid" + "_" + school_id + "_" + "groupname" + "_" + menu_group_name + "_" + "dishesname" + "_" + dishes_names + "_" + "dishesnumber" + "_" + dishes_number + "_" + "catertypename" + "_" + cater_type_name + "_" + "已留样" + "_" + "createtime" + "_" + format3.format(reserve_create_time) + "_" + "creator" + "_" + reserve_creator + "_" + "quantity" + "_" + quantity + "_" + "remark" + "_" + remark + "_" + "reservedata" + "_" + datetime + " " + reserve_hour + ":" + reserve_minute + ":" + "00" + "_" + "reservestatus" + "_" + reserve_deal_status+ "_" + "consistent" + "_" + is_consistent + "_" + "cremark" + "_" + is_consistent_remark

            (package_id+"_"+menu_group_name+"_"+"catertypename"+"_"+cater_type_name+"_"+"dishesname"+"_"+dishes_names,value)

          } else {

            val value = "area" + "_" + area + "_" + "supplierid" + "_" + supplier_id + "_" + "schoolid" + "_" + school_id + "_" + "groupname" + "_" + menu_group_name + "_" + "dishesname" + "_" + dishes_names + "_" + "dishesnumber" + "_" + dishes_number + "_" + "catertypename" + "_" + cater_type_name + "_" + "未留样" + "_" + "createtime" + "_" + "null" + "_" + "creator" + "_" + "null" + "_" + "quantity" + "_" + "null" + "_" + "remark" + "_" + remark + "_" + "reservedata" + "_" +"null" + "_" + "reservestatus" + "_" + reserve_deal_status + "_" + "consistent" + "_" + "null" + "_" + "cremark" + "_" + "null"
            (package_id+"_"+menu_group_name+"_"+"catertypename"+"_"+cater_type_name+"_"+"dishesname"+"_"+dishes_names,value)
          }

      }).cogroup(gcretentiondishData).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hdel(date + "_gc-retentiondish", k)
              } else {
                jedis.hset(date + "_gc-retentiondish", k, v._1.head)
              }
          })
      })


    }

    sc.stop()
  }
}
