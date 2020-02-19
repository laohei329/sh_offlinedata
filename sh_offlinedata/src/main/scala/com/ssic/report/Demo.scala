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

    val db_edu_school = session.read.jdbc(url, edu_school, conn)

    db_edu_school.createTempView("t_edu_school")

    val jedis = JPools.getJedis
    val schoolDetail = jedis.hgetAll("schoolDetail")
    val schoolDetailaData = sc.parallelize(schoolDetail.asScala.toList)

    session.sql("select * from t_edu_school where stat=1 and reviewed =1 ").rdd.map({
      row =>
        //id;378e5034-d02a-4f3d-842b-2f1a064e498b;schoolname;上海市静安区余姚路幼儿园;isbranchschool;0;parentid;null;area;10;address;上海市静安区余姚路170号;socialcreditcode;123101064250388211;level;3;schoolnature;0;schoolnaturesub;null;departmentmasterid;3;departmentslaveid;e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30;canteenmode;0;ledgertype;0;studentsamount;190;staffamount;40;corporation;谭妍文;corporationway;62538710;corporationtelephone;null;departmenthead;蔡春军;departmentmobilephone;15921126225;departmenttelephone;62538710;departmentfax;0000000;departmentemail;ccjhhb162@126.com;foodsafetypersion;null;foodsafetymobilephone;null;foodsafetytelephone;null;gongcan;null;slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null
        val id = row.getAs[String]("id")
        val schoolname = row.getAs[String]("school_name")
        val is_branch_school = row.getAs[Int]("is_branch_school")
        val parent_id = Rule.emptyToNull(row.getAs[String]("parent_id"))
        val area = Rule.emptyToNull(row.getAs[String]("area"))
        val address = Rule.emptyToNull(row.getAs[String]("address"))
        val social_credit_code = Rule.emptyToNull(row.getAs[String]("social_credit_code"))
        val level2 =row.getAs[Int]("level2")
        val school_nature = Rule.emptyToNull(row.getAs[String]("school_nature"))
        val school_nature_sub = Rule.emptyToNull(row.getAs[String]("school_nature_sub"))
        val department_master_id = Rule.emptyToNull(row.getAs[String]("department_master_id"))
        val department_slave_id = Rule.emptyToNull(row.getAs[String]("department_slave_id"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = row.getAs[Int]("license_main_child")
        val students_amount = row.getAs[Int]("students_amount")
        val staff_amount = row.getAs[Int]("staff_amount")
        val corporation = Rule.emptyToNull(row.getAs[String]("corporation"))
        val corporation_way = Rule.emptyToNull(row.getAs[String]("corporation_way"))
        val corporation_telephone = Rule.emptyToNull(row.getAs[String]("corporation_telephone"))
        val department_head = Rule.emptyToNull(row.getAs[String]("department_head"))
        val department_mobilephone = Rule.emptyToNull(row.getAs[String]("department_mobilephone"))
        val department_telephone = Rule.emptyToNull(row.getAs[String]("department_telephone"))
        val department_fax = Rule.emptyToNull(row.getAs[String]("department_fax"))
        val department_email = Rule.emptyToNull(row.getAs[String]("department_email"))
        val food_safety_persion = Rule.emptyToNull(row.getAs[String]("food_safety_persion"))
        val food_safety_mobilephone = Rule.emptyToNull(row.getAs[String]("food_safety_mobilephone"))
        val food_safety_telephone = Rule.emptyToNull(row.getAs[String]("food_safety_telephone"))

        (id,( schoolname, is_branch_school, parent_id, area, address, social_credit_code, level2,school_nature,school_nature_sub,department_master_id,department_slave_id,license_main_type,license_main_child,students_amount,staff_amount),(corporation,corporation_way,corporation_telephone,department_head,department_mobilephone,department_telephone,department_fax,department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone))

    }).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hget("schoolDetail",x._1 )
            if (StringUtils.isNoneEmpty(v) && !"null".equals(v)) {
              jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2._1 + ";" + "isbranchschool" + ";" + x._2._2 + ";" + "parentid" + ";" +x._2._3 + ";" + "area" + ";" +x._2._4 + ";" + "address" + ";" + x._2._5 + ";" + "socialcreditcode" + ";" +x._2._6 + ";" + "level" + ";" +x._2._7 + ";" + "schoolnature" + ";" + x._2._8 + ";" + "schoolnaturesub" + ";" + x._2._9 + ";" + "departmentmasterid" + ";" + x._2._10 + ";" + "departmentslaveid" + ";" + x._2._11 + ";" + "canteenmode" + ";" + x._2._12 + ";" + "ledgertype" + ";" +x._2._13 + ";" + "studentsamount" + ";" +x._2._14+ ";" + "staffamount" + ";" + x._2._15 + ";" + "corporation" + ";" +x._3._1 + ";" + "corporationway" + ";" +x._3._2 + ";" + "corporationtelephone" + ";" + x._3._3 + ";" + "departmenthead" + ";" +x._3._4 + ";" + "departmentmobilephone" + ";" + x._3._5 + ";" + "departmenttelephone" + ";" +x._3._6 + ";" + "departmentfax" + ";" + x._3._7 + ";" + "departmentemail" + ";" +x._3._8 + ";" + "foodsafetypersion" + ";" +x._3._9 + ";" + "foodsafetymobilephone" + ";" +x._3._10 + ";" + "foodsafetytelephone" + ";" +x._3._11 + ";" + "gongcan" + ";" + "1" + ";" + "slictype" + v.split("slictype")(1))
            }

        })
    })


    sc.stop()
  }

}
