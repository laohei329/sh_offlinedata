package com.ssic.utils

import com.ssic.utils.Tools.format
import org.apache.commons.lang3.time._
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.hive.HiveContext

object HiveTools {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def hiveschoolid(data:HiveContext) :RDD[String]={
    data.sql("select id from saas_v1.t_edu_school where stat=1 and reviewed =1").rdd.map(row => {
      val id = row.getAs[String]("id")
      id
    })
  }


  def holiday(data:(HiveContext,String)): Map[String, Int] = {
    //查询当天是否是假期
    data._1.sql(s"select holiday_day,type from saas_v1.t_edu_holiday where stat=1 and holiday_day='${data._2}'")
      .rdd.map(row => (row.getAs[String]("holiday_day"), row.getAs[Int]("type"))).collect().toMap
  }

  def calenda(data:(HiveContext,String)): Map[String, Int] = {

    //查询是供餐表是否有数据，对应的学校id是否供餐

    val result = data._1.sql(s"select school_id,have_class from saas_v1.t_edu_calendar where stat=1 and the_day='${data._2}'")
    result.rdd.map({
      row =>
        val school_id = row.getAs[String]("school_id")
        val have_class = row.getAs[Int]("have_class")
        (school_id, have_class)
    }).collect().toMap
  }

  def schoolTerm(data: (HiveContext,String,String)):Map[String,String] ={
    data._1.sql(s"select school_id,first_start_date,first_end_date,second_start_date,second_end_date,term_year from saas_v1.t_edu_schoolterm where stat =1 and term_year='${data._3}'").rdd.map({
      row =>
        val school_id = row.getAs[String]("school_id")
        val first_start_date = row.getAs[String]("first_start_date")
        val first_end_date = row.getAs[String]("first_end_date")
        val second_start_date = row.getAs[String]("second_start_date")
        val second_end_date = row.getAs[String]("second_end_date")

        if (format.parse(first_start_date).getTime <= format.parse(data._2).getTime && format.parse(data._2).getTime <= format.parse(first_end_date).getTime ){
          (school_id,"1")
        }else if(format.parse(second_start_date).getTime <= format.parse(data._2).getTime && format.parse(data._2).getTime <= format.parse(second_end_date).getTime){
          (school_id,"1")
        }else{
          (school_id,"0")
        }
    }).collect().toMap
  }

  def schoolTermSys(data: (HiveContext,String,String)):Map[String,String] ={
    val schoolTermSys = data._1.sql(s"select term_year,first_start_date,first_end_date,second_start_date,second_end_date from saas_v1.t_edu_schoolterm_system where stat=1 and term_year='${data._3}'")
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

        if (format.parse(x._2).getTime <= format.parse(data._2).getTime && format.parse(data._2).getTime <= format.parse(x._3).getTime ){
          (x._1,"1")
        }else if(format.parse(x._4).getTime <= format.parse(data._2).getTime && format.parse(data._2).getTime <= format.parse(x._5).getTime){
          (x._1,"1")
        }else{
          (x._1,"0")
        }
    }).collect().toMap
  }

}
