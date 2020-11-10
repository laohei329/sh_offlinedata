package com.ssic.report

import com.ssic.utils.Tools.{edu_school, _}
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * 将学校详情的部分字段数据更新到redis的学校详情中
  */

object SchoolDetail {
  private val format3 = FastDateFormat.getInstance("MM")
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("学校详情数据")
        .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
        .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession


    val db_edu_school = session.read.jdbc(url, edu_school, conn)

    db_edu_school.createTempView("t_edu_school")

    val jedis = JPools.getJedis
    val schoolDetail = jedis.hgetAll("schoolDetail")
    val schoolDetailaData = sc.parallelize(schoolDetail.asScala.toList)

    /**
      * 这是之前的原始代码
      */
        session.sql("select * from t_edu_school where stat=1 and reviewed =1 ").rdd.map({
      row =>
        //id;378e5034-d02a-4f3d-842b-2f1a064e498b;schoolname;上海市静安区余姚路幼儿园;
        // isbranchschool;0;parentid;null;area;10;address;上海市静安区余姚路170号;
        // socialcreditcode;123101064250388211;level;3;schoolnature;0;schoolnaturesub;null;departmentmasterid;3;
        // departmentslaveid;e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30;canteenmode;0;ledgertype;0;studentsamount;
        // 190;staffamount;40;corporation;谭妍文;corporationway;62538710;corporationtelephone;null;departmenthead;蔡春军;
        // departmentmobilephone;15921126225;departmenttelephone;62538710;departmentfax;0000000;departmentemail;ccjhhb162@126.com;
        // foodsafetypersion;null;foodsafetymobilephone;null;foodsafetytelephone;null;gongcan;null;slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null

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

        (id,

          ( schoolname, is_branch_school, parent_id, area, address, social_credit_code, level2,
          school_nature,school_nature_sub,department_master_id,department_slave_id,license_main_type,
          license_main_child,students_amount,staff_amount),

          (corporation,corporation_way,corporation_telephone,department_head,
            department_mobilephone,department_telephone,department_fax,
            department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone))

    }).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hget("schoolDetail",x._1 )

            if (StringUtils.isNoneEmpty(v) && !"null".equals(v)) {
              jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";"
                + x._2._1 + ";" + "isbranchschool" + ";"
                + x._2._2 + ";" + "parentid" + ";"
                +x._2._3 + ";" + "area" + ";"
                +x._2._4 + ";" + "address" + ";"
                + x._2._5 + ";" + "socialcreditcode" + ";"
                +x._2._6 + ";" + "level" + ";"
                +x._2._7 + ";" + "schoolnature" + ";"
                + x._2._8 + ";" + "schoolnaturesub" + ";"
                + x._2._9 + ";" + "departmentmasterid" + ";"
                + x._2._10 + ";" + "departmentslaveid" + ";"
                + x._2._11 + ";" + "canteenmode" + ";"
                + x._2._12 + ";" + "ledgertype" + ";"
                +x._2._13 + ";" + "studentsamount" + ";"
                +x._2._14+ ";" + "staffamount" + ";"
                + x._2._15 + ";" + "corporation" + ";"
                +x._3._1 + ";" + "corporationway" + ";"
                +x._3._2 + ";" + "corporationtelephone" + ";"
                + x._3._3 + ";" + "departmenthead" + ";"
                +x._3._4 + ";" + "departmentmobilephone" + ";"
                + x._3._5 + ";" + "departmenttelephone" + ";"
                +x._3._6 + ";" + "departmentfax" + ";"
                + x._3._7 + ";" + "departmentemail" + ";"
                +x._3._8 + ";" + "foodsafetypersion" + ";"
                +x._3._9 + ";" + "foodsafetymobilephone" + ";"
                +x._3._10 + ";" + "foodsafetytelephone" + ";"
                +x._3._11 + ";" + "gongcan" + ";" + "1" + ";" + "slictype" + v.split("slictype")(1))
            }

        })
    })



    val t_pro_license = session.read.jdbc(url, edu_bd_pro_license, conn)
    t_pro_license.createTempView("t_pro_license")

    /**
      * 获取1:食品经营许可证
      */
    val sLicenseRDD = session.sql("SELECT relation_id,lic_type,lic_pic,job_organization,lic_no,operation,give_lic_date,lic_end_date," +
        "stat from t_pro_license where stat = 1  and lic_type=1").rdd.map(row => {
      val relation_id = row.getAs[String]("relation_id")
      val lic_type = row.getAs[Int]("lic_type")
      val lic_pic = row.getAs[String]("lic_pic")
      val job_organization = row.getAs[String]("job_organization")
      val lic_no = row.getAs[String]("lic_no")
      val operation = row.getAs[String]("operation")
      val give_lic_date = row.getAs[String]("give_lic_date")
      val lic_end_date = row.getAs[String]("lic_end_date")
      val stat = row.getAs[Int]("stat")

      (relation_id, "slictype;" + lic_type + ";slicpic;" + lic_pic + ";slicjob;" + job_organization + ";slicno;" + lic_no + ";soperation;"
          + operation + ";slicdate;" + give_lic_date + ";senddate;" + lic_end_date + ";")
    }
    )
    /**
      * 获取所有的0:餐饮服务许可证
      */
    val cLicenseRDD = session.sql("SELECT relation_id,lic_type,lic_pic,job_organization,lic_no,operation,give_lic_date,lic_end_date," +
        "stat from t_pro_license where stat = 1  and lic_type= 0").rdd.map(row => {
      val relation_id = row.getAs[String]("relation_id")
      val lic_type = row.getAs[Int]("lic_type")
      val lic_pic = row.getAs[String]("lic_pic")
      val job_organization = row.getAs[String]("job_organization")
      val lic_no = row.getAs[String]("lic_no")
      val operation = row.getAs[String]("operation")
      val give_lic_date = row.getAs[String]("give_lic_date")
      val lic_end_date = row.getAs[String]("lic_end_date")
      val stat = row.getAs[Int]("stat")

      (relation_id, "clictype;" + lic_type + ";clicpic;" + lic_pic + ";clicjob;" + job_organization + ";clicno;" + lic_no + ";coperation;"
          + operation + ";clicdate;" + give_lic_date + ";cenddate;" + lic_end_date)
    }

    )
    /**
      * 将同一个项目点的证件进行拼接起来  0:餐饮服务许可证 1:食品经营许可证
      */
    val licenseRDD: RDD[(String, String)] = sLicenseRDD.fullOuterJoin(cLicenseRDD).map {
      case (id, (slic, clic)) => {
        if (None.equals(slic)) {
          if (None.equals(clic)) {
            (id, "slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;"
                + "clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null")
          } else {
            (id, "slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;"
                + clic.get)
          }
        } else if (None.equals(clic)) {
          (id, slic.get + "clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null")
        } else {
          (id, slic.get + clic.get)
        }
      }
    }

    val schoolRDD: RDD[(String, String)] = session.sql("select * from t_edu_school where stat=1 and reviewed =1 ").rdd.map({
      row =>
        //id;378e5034-d02a-4f3d-842b-2f1a064e498b;schoolname;上海市静安区余姚路幼儿园;
        // isbranchschool;0;parentid;null;area;10;address;上海市静安区余姚路170号;
        // socialcreditcode;123101064250388211;level;3;schoolnature;0;schoolnaturesub;null;departmentmasterid;3;
        // departmentslaveid;e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30;canteenmode;0;ledgertype;0;studentsamount;
        // 190;staffamount;40;corporation;谭妍文;corporationway;62538710;corporationtelephone;null;departmenthead;蔡春军;
        // departmentmobilephone;15921126225;departmenttelephone;62538710;departmentfax;0000000;departmentemail;ccjhhb162@126.com;
        // foodsafetypersion;null;foodsafetymobilephone;null;foodsafetytelephone;null;gongcan;null;slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null

        val id = row.getAs[String]("id")

        val schoolname = row.getAs[String]("school_name")
        val is_branch_school = row.getAs[Int]("is_branch_school")
        val parent_id = Rule.emptyToNull(row.getAs[String]("parent_id"))
        val area = Rule.emptyToNull(row.getAs[String]("area"))
        val address = Rule.emptyToNull(row.getAs[String]("address"))
        val social_credit_code = Rule.emptyToNull(row.getAs[String]("social_credit_code"))
        val level2 = row.getAs[Int]("level2")
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


        (id, "id;" + id + ";schoolname;" + schoolname + ";isbranchschool;" + is_branch_school + ";parentid;" + parent_id +
            ";area;" + area + ";address;" + address + ";socialcreditcode;" + social_credit_code + ";level;" + level2 + ";schoolnature;"
            + school_nature + ";schoolnaturesub;" + school_nature_sub + ";departmentmasterid;" + department_master_id +
            ";departmentslaveid;" + department_slave_id + ";canteenmode;" + license_main_type + ";ledgertype;"
            + license_main_child + ";studentsamount;" + students_amount + ";staffamount;" + staff_amount + ";corporation;" + corporation + ";corporationway;"
            + corporation_way + ";corporationtelephone;" + corporation_telephone + ";departmenthead;" + department_head +
            ";departmentmobilephone;" + department_mobilephone + ";departmenttelephone;" + department_telephone +
            ";departmentfax;" + department_fax + ";departmentemail;" + department_email + ";foodsafetypersion;" + food_safety_persion + ";foodsafetymobilephone;"
            + food_safety_mobilephone + ";foodsafetytelephone;" + food_safety_telephone + ";gongcan;1;")

    })

    /**
      * 将拼接后的证件与学校信息进行拼接,并写入到redis
      */
    schoolRDD.leftOuterJoin(licenseRDD).foreachPartition {

      itr =>
        var jedis = JPools.getJedis
        itr.foreach{
          case (id,(school,lic))=>
            if (None.equals(lic)){
              jedis.hset("schoolDetailTest",id,school+"slictype;null;slicpic;null;slicjob;null;slicno;null;soperation;null;slicdate;null;senddate;null;"
                  + "clictype;null;clicpic;null;clicjob;null;clicno;null;coperation;null;clicdate;null;cenddate;null")
            }else{
              jedis.hset("schoolDetailTest",id,school+lic.get)
            }
        }
    }
    sc.stop()
  }

}
