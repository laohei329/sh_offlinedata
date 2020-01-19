package com.ssic.report

import java.util
import java.util.{Calendar, Date}

import com.ssic.report.HiveToDwSaas.format1
import com.ssic.utils.{JPools, Rule}
import com.ssic.utils.Tools.{pro_reserve_sample, _}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.execution.streaming.FileStreamSource.Timestamp
import redis.clients.jedis.JedisCluster

import scala.collection.JavaConverters._

object Demo {
  private val format3 = FastDateFormat.getInstance("MM")
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession


//    val pro_reserve_sample = session.read.jdbc(url, pro_reserve_sample, conn)
//    pro_reserve_sample.createTempView("t_pro_reserve_sample")
//    sqlContext.sql("select * from t_pro_reserve_sample where stat =1 and supply_date = '2020-01-16' ").rdd.map({
//      row =>
//        val supply_date = row.getAs[Timestamp]("supply_date")
//
//    })

//    val pro_license = session.read.jdbc(url, edu_bd_pro_license, conn)
//
//    pro_license.createTempView("t_pro_license")
//
//    val jedis = JPools.getJedis
//    val schoolDetail = jedis.hgetAll("schoolDetail")
//    val schoolDetailaData = sc.parallelize(schoolDetail.asScala.toList)
//
//    session.sql("select * from t_pro_license where stat=1 and reviewed =1 and lic_type =0 ").rdd.map({
//      row =>
//        //id;378e5034-d02a-4f3d-842b-2f1a064e498b;schoolname;上海市静安区余姚路幼儿园;isbranchschool;0;parentid;null;area;10;address;上海市静安区余姚路170号;socialcreditcode;123101064250388211;level;3;schoolnature;0;schoolnaturesub;null;departmentmasterid;3;departmentslaveid;e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30;canteenmode;0;ledgertype;0;studentsamount;190;staffamount;40;corporation;谭妍文;corporationway;62538710;corporationtelephone;null;departmenthead;蔡春军;departmentmobilephone;15921126225;departmenttelephone;62538710;departmentfax;0000000;departmentemail;ccjhhb162@126.com;foodsafetypersion;null;foodsafetymobilephone;null;foodsafetytelephone;null;gongcan;null;slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null
//        val lic_type = row.getAs[Int]("lic_type")
//        val lic_pic = Rule.emptyToNull(row.getAs[String]("lic_pic"))
//        val job_organization = Rule.emptyToNull(row.getAs[String]("job_organization"))
//        val lic_no = Rule.emptyToNull(row.getAs[String]("lic_no"))
//        val operation = Rule.emptyToNull(row.getAs[String]("operation"))
//        val give_lic_date = row.getAs[Date]("give_lic_date").toString
//        var give_lic_date_new = "null"
//        if (StringUtils.isEmpty(give_lic_date) || "0001-01-01".equals(give_lic_date)) {
//          give_lic_date_new
//        } else {
//          give_lic_date_new = give_lic_date
//        }
//        val lic_end_date = row.getAs[Date]("lic_end_date").toString
//        var lic_end_date_new = "null"
//        if (StringUtils.isEmpty(lic_end_date) || "0001-01-01".equals(lic_end_date)) {
//          lic_end_date_new
//        } else {
//          lic_end_date_new = lic_end_date
//        }
//        val relation_id = row.getAs[String]("relation_id")
//        (relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date_new, lic_end_date_new)
//
//    }).foreachPartition({
//      itr =>
//        val jedis = JPools.getJedis
//        itr.foreach({
//          x =>
//            val v = jedis.hget("schoolDetail", x._1)
//            if (StringUtils.isNoneEmpty(v) && !"null".equals(v)) {
//
//              if (x._2 == 0) {
//                jedis.hset("schoolDetail",x._1, v.split("clictype")(0) + "clictype" + ";" + x._2 + ";" + "clicpic" + ";" + x._3 + ";" + "clicjob" + ";" + x._4 + ";" + "clicno" + ";" + x._5 + ";" + "coperation" + ";" + x._6 + ";" + "clicdate" + ";" + x._7 + ";" + "cenddate" + ";" + x._8)
//              } else if (x._2 == 1) {
//                jedis.hset("schoolDetail",x._1, v.split("slictype")(0) + "slictype" + ";" + x._2 + ";" + "slicpic" + ";" + x._3 + ";" + "slicjob" + ";" + x._4 + ";" + "slicno" + ";" + x._5 + ";" + "soperation" + ";" + x._6 + ";" + "slicdate" + ";" + x._7 + ";" + "senddate" + ";" + x._8 + ";" + "clictype" + v.split("clictype")(1))
//              }
//            }
//
//        })
//    })


    sc.stop()
  }

}
