package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.Platoon.format1
import com.ssic.service.{GongcanStat, PlatoonStat}
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3.time._
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory
import com.ssic.utils.Tools._

import scala.collection.JavaConverters._
import scala.collection.mutable

/*
* 产生加了供餐逻辑的排菜数据
* */

object Platoon {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val calenda = session.read.jdbc(url, edu_calendar, conn)
    val schoolterm = session.read.jdbc(url, edu_schoolterm, conn)
    val school = session.read.jdbc(url, edu_school, conn)
    val committeee = session.read.jdbc(url, edu_committee, conn)
    val schoolterm_system = session.read.jdbc(url, edu_schoolterm_system, conn)
    val holi = session.read.jdbc(url, edu_holiday, conn)

    calenda.createTempView("t_edu_calendar")
    schoolterm.createTempView("t_edu_schoolterm")
    school.createTempView("t_edu_school") //t_edu_school
    committeee.createTempView("t_edu_committee")
    schoolterm_system.createTempView("t_edu_schoolterm_system")
    holi.createTempView("t_edu_holiday")

    for (i <- 0 to 6) {
      //查询一周的排菜数据
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)
      val year = format1.format(time)

      var term_year="null"
      if (format.parse(date).getTime <= format.parse(year+"-08-31").getTime){
        term_year=(year.toInt-1).toString
      }else{
        term_year=year
      }


      val holiday = sc.broadcast(Tools.holiday(session, date))
      val calen = sc.broadcast(Tools.calenda(session, date))
      val schoolTerm = sc.broadcast(Tools.schoolTerm(session,date,term_year))
      val schoolTermSys = sc.broadcast(Tools.schoolTermSys(session,date,term_year))

      val jedis = JPools.getJedis
      val platoon_feed = jedis.hgetAll(date + "_platoon-feed")
      val plaData = sc.parallelize(platoon_feed.asScala.toList)//redis中存的供餐数据
      val platoondata: mutable.Set[String] = jedis.hkeys(date + "_platoon").asScala //排菜表的key

      new PlatoonStat().platoredis(session,plaData,date,holiday,calen,schoolTerm,schoolTermSys,term_year,platoondata)



    }


    sc.stop()


  }

}
