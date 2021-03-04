package com.ssic.utils

import java.util.{Calendar, Date, Properties}

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory

object NewTools {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val config = ConfigFactory.load()

  val url = config.getString("db.default.url")
  val user = config.getString("db.default.user")
  val pwd = config.getString("db.default.password")
  val driver = config.getString("db.default.driver")
  val edu_competent_department = config.getString("db.default.edu_competent_department")
  val edu_school = config.getString("db.default.edu_school")
  val edu_school_supplier = config.getString("db.default.edu_school_supplier")
  val pro_supplier = config.getString("db.default.pro_supplier")
  val default_edu_area = config.getString("db.default.area")
  val default_committee = config.getString("db.default.committee")
  val default_groupon_customer = config.getString("db.default.groupon_customer")

  val conn = new Properties()
  conn.setProperty("user", user)
  conn.setProperty("password", pwd)
  conn.setProperty("driver", driver)

  /**

    * * 主管部门的id 对应的 映射

    * * @param SparkSession

    */

  def school2commitee(session: SparkSession): Map[String, String] = {
    val competent_department = session.read.jdbc(url, edu_competent_department, conn)
    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)
    val supplier = session.read.jdbc(url, pro_supplier, conn)
    val edu_area = session.read.jdbc(url, default_edu_area, conn)
    val committee = session.read.jdbc(url,default_committee,conn)
    val groupon_customer = session.read.jdbc(url,default_groupon_customer,conn)

    competent_department.createTempView("t_edu_competent_department")
    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")
    supplier.createTempView("t_pro_supplier")
    edu_area.createTempView("area")
    committee.createTempView("t_edu_committee")
    groupon_customer.createTempView("groupon_customer")

    val result = session.sql(
      """
select org_merchant_id,management_area_type from t_edu_competent_department
     """.stripMargin)

    result.rdd.map(x => (x.getAs[Long]("org_merchant_id").toString(), x.getAs[Int]("management_area_type").toString)).collect().toMap

  }
/**
 * @Description : TODO
 * @author : Mr.Sun
 * @date  2021/1/22  10:43
 * @param session
 * @return org.apache.spark.rdd.RDD<scala.Tuple2<java.lang.String,java.lang.Object>>
   */
  def getgroupon_customer(session: SparkSession) ={

    val result = session.sql(
      """
        |SELECT
        |school_id,catering_new_type
        |from ods.ods_erp_order_db_groupon_customer
        |where stat =1
        |""".stripMargin)
    result.rdd.map{
      row =>{
        val school_id: String = row.getAs[String]("school_id")
        val catering_new_type: Long = row.getAs[Long]("catering_new_type")
        (school_id,catering_new_type)
      }
    }.collect().toMap
  }




  /**

    * * 主管部门的id 对应的 主管部门名字

    * * @param SparkSession

    */

  def school2commiteename(session: SparkSession): Map[String, String] = {

    val result = session.sql(
      """
select org_merchant_id,name from t_edu_competent_department
     """.stripMargin)

    result.rdd.map(x => (x.getAs[Long]("org_merchant_id").toString(), x.getAs[String]("name").toString)).collect().toMap

  }

  /**

    * * 学校id 对应的区

    * * @param SparkSession

    */

  def school2Area(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
select id,area from t_edu_school
     """.stripMargin)

    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("area"))).collect().toMap
  }

  /**

    * * 项目点的id 对应的 区

    * * @param SparkSession

    */

  def projid2Area(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
        |SELECT
        |	a.bid AS did,
        |	c.area AS area
        |FROM
        |	(
        |		SELECT
        |			id AS bid,
        |			school_id AS school_id
        |		FROM
        |			t_edu_school_supplier
        |	) AS a
        |LEFT JOIN t_edu_school AS c ON a.school_id = c.id
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("did"), x.getAs[String]("area"))).collect().toMap
  }

  /**

    * * 团餐公司id 对应的 团餐公司名字

    * * @param SparkSession

    */
  def supplierid2supplierName(session: SparkSession): Map[String, String] = {
    val result = session.sql("select id,supplier_name from t_pro_supplier")
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("supplier_name"))).collect().toMap
  }

  /**

    * * 老主管部门的id 对应的 主管部门名字

    * * @param SparkSession

    */

  def commiteeid2commiteename(session: SparkSession): Map[String, String] = {

    val result = session.sql("select id,name from t_edu_committee where stat=1")

    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("name"))).collect().toMap

  }

  /**

    * * 团餐公司的id 对应的 区

    * * @param SparkSession

    */

  def supplierid2area(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
select id,area
from t_pro_supplier
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("area"))).filter(x => StringUtils.isNoneEmpty(x._2)).collect().toMap
  }

  /**

    * * 学校的id 对应的 主管部门的id

    * * @param SparkSession

    */

  def schoolid2masterid(session: SparkSession): Map[String, List[String]] = {

    val result = session.sql("SELECT id,school_name,department_master_id,department_slave_id FROM t_edu_school")

    result.rdd.map(x => (x.getAs[String]("id"), List(x.getAs[String]("school_name"), x.getAs[String]("department_master_id"), x.getAs[String]("department_slave_id")))).collect().toMap

  }

  /**

    * * 学校与团餐公司关联的id 对应的 学校id

    * * @param SparkSession

    */

  def schoolsupplierid2schoolid(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
SELECT id,school_id FROM t_edu_school_supplier WHERE stat =1
      """.stripMargin)
    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("school_id"))).collect().toMap
  }




}
