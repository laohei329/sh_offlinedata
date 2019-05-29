package com.ssic.utils

import java.util.{Date, Properties}

import com.typesafe.config.ConfigFactory
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.internal.config
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.hive.HiveContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object Tools {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyyMMdd")
  private val format2 = FastDateFormat.getInstance("yyyy")

  private val config = ConfigFactory.load()

  val url = config.getString("db.default.url")
  val user = config.getString("db.default.user")
  val pwd = config.getString("db.default.password")
  val driver = config.getString("db.default.driver")

  val edu_calendar = config.getString("db.default.edu_calendar")
  val edu_schoolterm = config.getString("db.default.edu_schoolterm")
  val edu_school = config.getString("db.default.edu_school")
  val edu_committee = config.getString("db.default.edu_committee")
  val edu_schoolterm_system = config.getString("db.default.edu_schoolterm_system")
  val edu_holiday = config.getString("db.default.edu_holiday")
  val edu_school_supplier = config.getString("db.default.edu_school_supplier")

  val conn = new Properties()
  conn.setProperty("user", user)
  conn.setProperty("password", pwd)
  conn.setProperty("driver", driver)

  def calenda(session: (SparkSession, String)): Map[String, Int] = {

    //查询是供餐表是否有数据，对应的学校id是否供餐
    val date1 = session._2
    //format.format(new Date())
    val result = session._1.sql(s"select school_id,have_class from t_edu_calendar where stat=1 and the_day='$date1'")
    result.rdd.map({
      row =>
        val school_id = row.getAs[String]("school_id")
        val have_class = row.getAs[Int]("have_class")
        (school_id, have_class)
    }).collect().toMap
  }

  def holiday(session: (SparkSession, String)): Map[String, Int] = {
    //查询当天是否是假期
    session._1.sql(s"select holiday_day,type from t_edu_holiday where stat=1 and holiday_day='${session._2}'")
      .rdd.map(row => (row.getAs[String]("holiday_day"), row.getAs[Int]("type"))).collect().toMap
  }

  def schoolid(session: SparkSession): RDD[(String, String)] = {
    //查询有效的schoolid
    session.sql("select id ,area from t_edu_school where stat=1 and reviewed =1").rdd.map(row => {
      val district_id = row.getAs[String]("area")
      val id = row.getAs[String]("id")
      (district_id + "_" + id, id)
    })
  }

  def school2Committee(session: SparkSession):Map[String,String] ={
    //查询教属的基础信息
    val result = session.sql("select id,name from t_edu_committee where stat=1 ")

    result.rdd.map({
      row =>
        val id = row.getAs[String]("id")
        val name = row.getAs[String]("name")
        (id,name)
    }).collect().toMap
  }

  def projid2schoolid(session: SparkSession): Map[String, String] = {

    //查询学校与团餐公司关联表的id

    val result = session.sql("select id,school_id from t_edu_school_supplier")

    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("school_id"))).collect().toMap

  }

  //项目点id-》 学校名字
  def projid2schoolname(session: SparkSession): Map[String, String] = {
    val result = session.sql(
      """
         SELECT a.id as bid,b.school_name as bname FROM t_edu_school_supplier as a LEFT JOIN t_edu_school as b ON a.school_id = b.id where b.stat=1
     """.stripMargin)

    result.rdd.map(x => (x.getAs[String]("bid"), x.getAs[String]("bname"))).collect().toMap
  }

  def gongcanSchool(date:String):Map[String,String] ={
    val jedis = JPools.getJedis
    jedis.hgetAll(date+"_platoon-feed").asScala.toMap
  }

  def hivedishmenu(hivedata:(HiveContext,String,String,String) ):RDD[(String, String, String, String, String, String, String, String)] ={

    hivedata._1.sql(s"select package_id,school_id,area,menu_group_name,dishes_name,cater_type_name,dishes_number,supplier_id from app_saas_v1.app_t_edu_dish_menu where year ='${hivedata._3}' and month ='${hivedata._4}' and supply_date = '${hivedata._2}' and dishes_name is not null")
      .rdd.map(x => (x.getAs[String]("package_id"),x.getAs[String]("school_id"),x.getAs[String]("area"),x.getAs[String]("menu_group_name"),x.getAs[String]("dishes_name"),x.getAs[String]("cater_type_name"),x.getAs[String]("dishes_number"),x.getAs[String]("supplier_id")))
  }

  def commiteeid2commiteename(session: SparkSession): Map[String, String] = {

    //教属的名字

    val result = session.sql("select id,name from t_edu_committee where stat=1")

    result.rdd.map(x => (x.getAs[String]("id"), x.getAs[String]("name"))).collect().toMap

  }

  def schoolTerm(session: (SparkSession,String)):Map[String,String] ={
    //查询学校的学期设置是否是有效的的
    val schoolterm = session._1.sql("select school_id,first_start_date,first_end_date,second_start_date,second_end_date from t_edu_schoolterm where stat =1")
    schoolterm.rdd.map({
      row =>
        val school_id = row.getAs[String]("school_id")
        val first_start_date = row.getAs[String]("first_start_date")
        var first_start=""
        if(first_start_date.contains("月份").equals(true)){
          first_start=first_start_date.substring(0,7)+"-01"
        }else{
          first_start=first_start_date
        }
        val first_end_date = row.getAs[String]("first_end_date")
        var first_end=""
        if(first_end_date.contains("月份").equals(true)){
          first_end=first_end_date.substring(0,7)+"-01"
        }else if(first_end_date.equals("2019-0123")){
          first_end="2019-01-23"
        } else{
          first_end=first_end_date
        }
        val second_start_date = row.getAs[String]("second_start_date")
        val second_end_date = row.getAs[String]("second_end_date")
        var second_end=""
        if(second_end_date.equals("2019-01")){
          second_end=second_end_date+"-01"
        }else if(second_end_date.equals("20208-6-25")){
          second_end="2020-06-25"
        } else{
          second_end=second_end_date
        }
        (school_id, first_start, first_end, second_start_date, second_end)
    }).filter(x => !x._2.equals("20170901")).filter(x => !x._3.equals("2018.1")).filter(x => !x._2.equals("2017年2月13日")).filter(x => !x._2.equals("20170216")).map({
      x =>
        var first ="-"
        if(x._2.size <=7 && x._2.contains("-")){
          first=x._2+"-"+"01"
        }else{
          first=x._2
        }

        var firstend ="-"
        if(x._3.size <=7 && x._3.contains("-")){
          firstend=x._3+"-"+"01"
        }else{
          firstend=x._3
        }
        var second ="-"
        if(x._4.size <=7 && x._4.contains("-")){
          second=x._4+"-"+"01"
        }else{
          second=x._4
        }
        var secondend ="-"
        if(x._5.size <=7 && x._5.contains("-")){
          secondend=x._5+"-"+"01"
        }else{
          secondend=x._5
        }
        (x._1,first,firstend,second,secondend)
    })
      .map({
        x =>
          var first =0.toLong
          if(x._2.matches("[0-9]*").equals(true)){
            first=format1.parse(x._2).getTime
          } else{
            first=format.parse(x._2.replaceAll("\\D", "-")).getTime
          }

          var first_en =0.toLong
          if(x._3.matches("[0-9]*").equals(true)){
            first_en=format1.parse(x._3).getTime
          }else{
            first_en=format.parse(x._3.replaceAll("\\D", "-")).getTime
          }

          var second=0.toLong
          if(x._4.matches("[0-9]*").equals(true)){
            second=format1.parse(x._4).getTime
          }else{
            second=format.parse(x._4.replaceAll("\\D", "-")).getTime
          }

          var second_en=0.toLong
          if(x._5.matches("[0-9]*").equals(true)){
            second_en=format1.parse(x._5).getTime
          }else{
            second_en=format.parse(x._5.replaceAll("\\D", "-")).getTime
          }

          (x._1,first,first_en,second,second_en)

      }).map({
      x =>
        val date1 = session._2//format.format(new Date())
        if (x._2.toLong <= format.parse(date1).getTime && format.parse(date1).getTime <= x._3.toLong ){
          (x._1,"1")
        }else if(x._4.toLong <= format.parse(date1).getTime && format.parse(date1).getTime <= x._5.toLong){
          (x._1,"1")
        }else{
          (x._1,"0")
        }
    }).filter(x => !x._1.equals("null")).collect().toMap
  }


  def schoolTermSys(session: (SparkSession,String)):Map[String,String] = {
    //查询当天是否在系统学期设置内
    val date = new Date()
    val year = format2.format(date)
    val schoolTermSys = session._1.sql(s"select term_year,first_start_date,first_end_date,second_start_date,second_end_date from t_edu_schoolterm_system where stat=1 and term_year='$year'")
    schoolTermSys.rdd.map({
      row =>
        val term_year = row.getAs[String]("term_year")
        val first_start_date = row.getAs[String]("first_start_date")
        val first_end_date = row.getAs[String]("first_end_date")
        val second_start_date = row.getAs[String]("second_start_date")
        val second_end_date = row.getAs[String]("second_end_date")
        (term_year,first_start_date,first_end_date,second_start_date,second_end_date)

    }).map({
      x =>
        val date1 = session._2
        if (format.parse(x._2).getTime <= format.parse(date1).getTime && format.parse(date1).getTime <= format.parse(x._3).getTime ){
          (x._1,"1")
        }else if(format.parse(x._4).getTime <= format.parse(date1).getTime && format.parse(date1).getTime <= format.parse(x._5).getTime){
          (x._1,"1")
        }else{
          (x._1,"0")
        }
    }).filter(x => !x._1.equals("null")).collect().toMap

  }


  def schoolNew(session: SparkSession):Map[String,List[String]] ={

    //对学校的基础信息按照新规则进行清洗
    val result = session.sql("select id,level,IFNULL(level2,-1)as level2,school_nature,school_nature_sub,license_main_type,license_main_child,department_master_id,department_slave_id from t_edu_school where stat=1 and reviewed =1 ")

    result.rdd.map({
      row =>
        val id = row.getAs[String]("id")
        val level = row.getAs[String]("level")
        val level2 = row.getAs[Int]("level2")
        var level_name="null"
        if (level2 == -1) {
          if (StringUtils.isEmpty(level)) {
            level_name
          } else if ("0".equals(level)) {
            level_name = "3" //幼儿园
          } else if ("1".equals(level)) {
            level_name = "7" //小学
          } else if ("2".equals(level)) {
            level_name = "8" //初级中学
          } else if ("3".equals(level)) {
            level_name = "9" //高级中学
          } else if ("6".equals(level)) {
            level_name = "13" //职业初中
          } else if ("7".equals(level)) {
            level_name = "0" //托儿所（托班）
          } else if ("9".equals(level)) {
            level_name = "17" //其他
          } else if ("0,7".equals(level)) {
            level_name = "1" //托幼园（托儿所+幼儿园）
          } else if ("7,0".equals(level)) {
            level_name = "1" //托幼园（托儿所+幼儿园）
          } else if ("1,2".equals(level)) {
            level_name = "11" //九年一贯制学校
          } else if ("2,3".equals(level)) {
            level_name = "10" //完全中学
          } else if ("1,2,3".equals(level)) {
            level_name = "12" //十二年一贯制学校
          } else if ("1,2,3,6".equals(level)) {
            level_name = "12" //十二年一贯制学校
          } else if ("0,1".equals(level)) {
            level_name = "4" // 幼小（幼儿园+小学）
          } else if ("3,6".equals(level)) {
            level_name = "14" // 中等职业学校
          } else if ("0,6".equals(level)) {
            level_name = "14" //中等职业学校
          } else if ("0,1,2".equals(level)) {
            level_name = "5" //  幼小初（幼儿园+小学+初中）
          } else if ("0,1,2,3".equals(level)) {
            level_name = "6" // 幼小初高（幼儿园+小学+初中+高中）
          } else if ("0,1,2,3,6".equals(level)) {
            level_name = "6" // 幼小初高（幼儿园+小学+初中+高中）
          } else if ("0,1,2,3,6,7,9".equals(level)) {
            level_name = "6" // 幼小初高（幼儿园+小学+初中+高中）
          } else if ("1,9".equals(level)) {
            level_name = "17" // 其他
          } else if ("3,9".equals(level)) {
            level_name = "17" // 其他
          } else if ("0,1,7".equals(level)) {
            level_name = "2" // 托幼小（托儿所+幼儿园+小学）
          }
          else if ("0,1,2,3".equals(level)) {
            level_name = "6" // 幼小初高（幼儿园+小学+初中+高中
          } else if ("0,2".equals(level)) {
            level_name = "5" //  幼小初（幼儿园+小学+初中）
          } else {
            level_name
          }
        }else{
          level_name=level2.toString
        }
        val school_nature = row.getAs[String]("school_nature")
        val school_nature_sub = row.getAs[String]("school_nature_sub")
        //GNGB(0, "公办"),GNMB(2, "民办"),OTHER(4, "其他");3, "外籍人员子女学校"
        var school_nature_name = "null"
        var school_nature_sub_name= "null"
        if(StringUtils.isNoneEmpty(school_nature)){
          if (school_nature.equals("1")|| school_nature.equals("0")) {
            school_nature_name = "0"
          } else if (school_nature.equals("2")) {
            school_nature_name = "2"
          }else if(school_nature.equals("4")){
            school_nature_name="4"
          }else if(school_nature.equals("3")){
            school_nature_name="3"
          } else {
            school_nature_name="4"
          }
        }else{
          school_nature_name
        }

        if(school_nature_sub != null){
          school_nature_sub_name = school_nature_sub.toString
        }else{
          school_nature_sub_name
        }
        val license_main_type = row.getAs[String]("license_main_type")
        val license_main_child = row.getAs[Int]("license_main_child")
        //食堂经营
        var license_main_type_name="null"
        var license_main_child_name="null"

        if(StringUtils.isNoneEmpty(license_main_type)){
          license_main_type_name=license_main_type
        }else{
          license_main_type_name
        }


        if(license_main_child != null){
          license_main_child_name = license_main_child.toString
        }else{
          license_main_child_name
        }

        val department_master_id = row.getAs[String]("department_master_id")
        var department_master_id_name="null"
        if(StringUtils.isNoneEmpty(department_master_id)){
          department_master_id_name=department_master_id
        }else{
          department_master_id_name
        }

        val department_slave_id = row.getAs[String]("department_slave_id")
        var department_slave_id_name="null"
        if(StringUtils.isNoneEmpty(department_slave_id)){
          department_slave_id_name=department_slave_id
        }else{
          department_slave_id_name
        }

        (id,List(level_name,school_nature_name,school_nature_sub_name,license_main_type_name,license_main_child_name,department_master_id_name,department_slave_id_name))
    }).collect().toMap

  }

}
