package com.ssic.utils

import java.util.Properties

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3.StringUtils
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
  val warn_rule_seting = config.getString("db.default.opt_warn_rule_seting")
  val warn_rule = config.getString("db.default.opt_warn_rule")

  val conn = new Properties()
  conn.setProperty("user", user)
  conn.setProperty("password", pwd)
  conn.setProperty("driver", driver)

  //schoolid -> districtid
  def school2Commitee(session: SparkSession): Map[String, String] = {

    val school = session.read.jdbc(url, edu_school, conn)
    val committeee = session.read.jdbc(url, edu_committee, conn)
    val school_supplier = session.read.jdbc(url2, edu_school_supplier, conn)
    val supplier = session.read.jdbc(url2, edu_supplier, conn)
    val warn_rule_set = session.read.jdbc(url2, warn_rule_seting, conn)
    val o_warn_rule = session.read.jdbc(url2, warn_rule, conn)
    val schoolterm = session.read.jdbc(url, edu_schoolterm, conn)

    school.createTempView("t_edu_school") //t_edu_school
    committeee.createTempView("t_edu_committee")
    school_supplier.createTempView("t_edu_school_supplier")
    supplier.createTempView("t_pro_supplier")
    warn_rule_set.createTempView("t_opt_warn_rule_seting")
    o_warn_rule.createTempView("t_opt_warn_rule")
    schoolterm.createTempView("t_edu_schoolterm")


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
select d.did,b.district_id from
(select a.bid as did,c.committee_id as committee_id from
(select id as bid,school_id as school_id
from t_edu_school_supplier ) as a left join t_edu_school as c on a.school_id=c.id) as d
left join t_edu_committee as b
on d.committee_id = b.id
      """.stripMargin
    )
    result.rdd.map(x => (x.getAs[String]("did"), x.getAs[String]("district_id"))).collect().toMap
  }

  //supplierid -> districtid
  def supplierid2Area(session:SparkSession):Map[String,String] ={
    val result= session.sql(
      """
select d.did,b.district_id from
(select a.bid as did,c.committee_id as committee_id from
(select supplier_id as bid,school_id as school_id
from t_edu_school_supplier ) as a left join t_edu_school as c on a.school_id=c.id) as d
left join t_edu_committee as b
on d.committee_id = b.id
      """.stripMargin
    )
    result.rdd.map(x => (x.getAs[String]("did"), x.getAs[String]("district_id"))).collect().toMap
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
    val result = session.sql("select id,supplier_name from t_pro_supplier")
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("supplier_name"))).collect().toMap
  }
//
  //warnRuleId -> 推送对象
  def warnRule2PushObject(session: SparkSession) : Map[String,String] ={
    val result = session.sql(
      """
select warn_rule_id,push_object
from t_opt_warn_rule_seting
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("warn_rule_id"),x.getAs[String]("push_object"))).collect().toMap
  }

  //warnRuleId ->触发条件
  def warnRule2TriggerCondition(session: SparkSession) : Map[String,String] ={
    val result = session.sql(
      """
select id,trigger_condition
from t_opt_warn_rule
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("trigger_condition"))).collect().toMap
  }

  //warnRuleId ->提前预警天数
  def warnRule2Earlywarningdays(session: SparkSession) : Map[String,String] ={
    val result = session.sql(
      """
select id,early_warning_days
from t_opt_warn_rule
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"),x.getAs[String]("early_warning_days"))).collect().toMap
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

}
