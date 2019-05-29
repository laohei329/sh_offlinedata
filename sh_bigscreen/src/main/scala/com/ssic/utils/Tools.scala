package com.ssic.utils

import java.util.Properties


import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3._
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

/**
  * Created by 云 on 2018/8/20.
  */
object Tools {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val config = ConfigFactory.load()

  val url = config.getString("db.default.url")
  val user = config.getString("db.default.user")
  val pwd = config.getString("db.default.password")
  val driver = config.getString("db.default.driver")
  val edu_school = config.getString("db.default.edu_school")
  val edu_committee = config.getString("db.default.edu_committee")
  val edu_schoolterm = config.getString("db.default.edu_schoolterm")

  val url2 = config.getString("db.default.url2")
  val edu_school_supplier = config.getString("db.default.edu_school_supplier")
  val edu_supplier = config.getString("db.default.edu_supplier")

  val hiveurl = config.getString("db.default.hiveurl")
  val hiveuse = config.getString("db.default.hiveuser")
  val hivepassword = config.getString("db.default.hivepassword")

  val driverName = "org.apache.hive.jdbc.HiveDriver"
  val conn3 = new Properties()
  conn3.setProperty("driver",driverName)
  conn3.setProperty("user",hiveuse)
  conn3.setProperty("password",hivepassword)

//  val url3 = config.getString("db.default.url3")
//  val user2 = config.getString("db.default.user2")
//  val pwd2 = config.getString("db.default.password2")
//  val edu_white_school = config.getString("db.default.edu_white_school")


  val conn = new Properties()
  conn.setProperty("user", user)
  conn.setProperty("password", pwd)
  conn.setProperty("driver", driver)

//  val conn2 = new Properties()
//  conn2.setProperty("user", user2)
//  conn2.setProperty("password", pwd2)
//  conn2.setProperty("driver", driver)

  //schoolid -> districtid
  def school2Commitee(session: SparkSession): Map[String, String] = {

    val school = session.read.jdbc(url, edu_school, conn)
    val committeee = session.read.jdbc(url, edu_committee, conn)
    val school_supplier = session.read.jdbc(url2, edu_school_supplier, conn)
    val supplier = session.read.jdbc(url2, edu_supplier, conn)
    val schoolterm = session.read.jdbc(url, edu_schoolterm, conn)

    //val white_school = session.read.jdbc(url3, edu_white_school, conn2)

    school.createTempView("t_edu_school") //t_edu_school
    committeee.createTempView("t_edu_committee")
    school_supplier.createTempView("t_edu_school_supplier")
    supplier.createTempView("t_pro_supplier")
    schoolterm.createTempView("t_edu_schoolterm")

    //white_school.createTempView("t_edu_white_school")


    val result = session.sql(
      """
select id,area
from t_edu_school
     """.stripMargin)

    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("area"))).filter( x => StringUtils.isNoneEmpty(x._2)).collect().toMap

  }

  //项目点id -> districtid
  def projid2Area(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
select a.bid as did,c.area as area from
(select id as bid,school_id as school_id
from t_edu_school_supplier ) as a left join t_edu_school as c on a.school_id=c.id
      """.stripMargin
    )
    result.rdd.map(x => (x.getAs[String]("did"), x.getAs[String]("area"))).collect().toMap
  }

  //supplierid -> districtid
  def supplierid2Schoolmaster(session:SparkSession):Map[String,List[String]] ={
    val result= session.sql(
      """
SELECT d.aid as did,c.school_name as cname,c.department_master_id as cmasterid,c.department_slave_id as cslaveid FROM t_edu_school as c LEFT JOIN(
SELECT b.school_id as bid,a.id as aid FROM t_pro_supplier as a LEFT JOIN t_edu_school_supplier as b ON a.id =b.supplier_id where b.stat=1) as d on c.id =d.bid
      """.stripMargin
    )
    result.rdd.map(x => (x.getAs[String]("did"), List[String](x.getAs[String]("cname"),x.getAs[String]("cmasterid"),x.getAs[String]("cslaveid")))).collect().toMap
  }

  //schoolid -> 学校性质
  def school2school_nature(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
select id,school_nature from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("school_nature"))).collect().toMap
  }

  //schoolid -> school_nature_sub
  def school2school_nature_sub(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
select id,school_nature_sub from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("school_nature_sub"))).collect().toMap
  }

  //schoolid -> 食堂类型
  def school2canteen_mode(session: SparkSession):Map[String,Int] = {
    val result = session.sql(
      """
select id,canteen_mode from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[Int]("canteen_mode"))).collect().toMap
  }

  //schoolid -> ledger_type
  def school2ledger_type(session: SparkSession):Map[String,String] = {
    val result = session.sql(
      """
select id,ledger_type from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("ledger_type"))).collect().toMap
  }

  //schoolid -> 学校类型
  def school2level(session: SparkSession):Map[String,String] = {
    val result = session.sql(
      """
select id,level from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("level"))).collect().toMap
  }
  def school2level2(session: SparkSession):Map[String,Int] = {
    val result = session.sql(
      """
select id,level2 from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[Int]("level2"))).collect().toMap
  }

  //schoolid -> 学校名称
  def school2name(session: SparkSession):Map[String,String] = {
    val result = session.sql(
      """
select id,school_name from t_edu_school
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("school_name"))).collect().toMap
  }

  //supplierid -> supplierName
  def supplierid2supplierName(session: SparkSession):Map[String,String] = {
    val result = session.sql("select id,supplier_name from t_pro_supplier where stat=1")
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("supplier_name"))).collect().toMap
  }

  def supplierid2supplierName2(session: SparkSession):Map[String,String] = {
    val result = session.sql("select id,supplier_name from t_pro_supplier where stat=1 and supplier_type=2")
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("supplier_name"))).collect().toMap
  }


  // supplierid -> districid
  def supplierid2district(session: SparkSession) : Map[String,String] ={
    val result = session.sql(
      """
select id,district_id
from t_pro_supplier
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("district_id"))).filter( x => StringUtils.isNoneEmpty(x._2)).collect().toMap
  }

  // supplierid -> company_type
  def supplierid2companytype(session: SparkSession) : Map[String,Int] ={
    val result = session.sql(
      """
select id,company_type
from t_pro_supplier
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[Int]("company_type"))).collect().toMap
  }

  def schoolid2schoolterm(session: SparkSession): Map[String,Int] ={
    val result = session.sql(
      """
SELECT DISTINCT(school_id),stat FROM t_edu_schoolterm WHERE stat =1
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("school_id"), x.getAs[Int]("stat"))).collect().toMap
  }

  //团餐公司与学校的关联关系表
  def schoolsupplierid2schoolid(session: SparkSession):Map[String,String] ={
    val result = session.sql(
      """
SELECT id,school_id FROM t_edu_school_supplier WHERE stat =1
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("school_id"))).collect().toMap
  }

  def commiteeid2commiteename(session: SparkSession): Map[String, String] = {

    val result = session.sql("select id,name from t_edu_committee where stat=1")

    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("name"))).collect().toMap

  }

  def schoolid2masterid(session: SparkSession): Map[String, List[String]] = {

    val result = session.sql("SELECT id,school_name,department_master_id,department_slave_id FROM t_edu_school")

    result.rdd.map(x => (x.getAs[String]("id"), List(x.getAs[String]("school_name"),x.getAs[String]("department_master_id"),x.getAs[String]("department_slave_id")))).collect().toMap

  }

//  //白名单数据
//  def whiteschoolid(session: SparkSession): Map[String, String] = {
//
//    val result = session.sql("SELECT id,school_name FROM t_edu_white_school")
//
//    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("school_name"))).collect().toMap
//  }
//
//  //团餐公司id =》 学校id
//  def supplierid2schoolid(session: SparkSession) : Map[String,String] ={
//    val result = session.sql(
//      """
//        |SELECT a.id as aid,b.school_id as bschoolid FROM t_pro_supplier as a LEFT JOIN t_edu_school_supplier as b on a.id =b.supplier_id  where a.stat =1 and b.stat=1
//      """.stripMargin)
//    result.rdd.map(x => (x.getAs[String]("aid"),x.getAs[String]("bschoolid"))).collect().toMap
//  }

}
