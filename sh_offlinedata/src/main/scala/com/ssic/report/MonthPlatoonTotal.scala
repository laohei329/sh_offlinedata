package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.service.{DealDataStat, PlatoonTotalStat}
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools.{conn, edu_committee, edu_school, url}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext

import scala.collection.JavaConverters._

/**
  * 一个月的排菜统计数据
  * */
object MonthPlatoonTotal {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val school = session.read.jdbc(url, edu_school, conn)
    val committee = session.read.jdbc(url, edu_committee, conn)

    school.createTempView("t_edu_school")
    committee.createTempView("t_edu_committee")

    for (i <- -30 to -1) {
      //查询一个月的排菜数据
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)

      val schoolData: Broadcast[Map[String, List[String]]] = sc.broadcast(Tools.schoolNew(session))
      val commiteeData: Broadcast[Map[String, String]] = sc.broadcast(Tools.school2Committee(session))

      val jedis = JPools.getJedis
      val total = jedis.hgetAll(date + "_platoonfeed-total")
      val platoonTotal = sc.parallelize(total.asScala.toList) //已存在的排菜统计表的数据

      val platoon_feed = jedis.hgetAll(date + "_platoon-feed")
      val platoon = sc.parallelize(platoon_feed.asScala.toList) //已存在的排菜表的详情数据，对这个详情数据进行各个维度的统计

      val platoonTotalData = new DealDataStat().platoontotal(platoonTotal)

      val platoonData = platoon.map({
        x =>
          //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
          if (x._2.split("_").size > 2) {
            (x._1, x._2.split("_create-time")(0))
          } else {
            (x._1, x._2)
          }
      })

      //计算各区的排菜统计情况
      val areaPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._1._1.equals("null")).map(_._1)
      new PlatoonTotalStat().areaplatoontotal(platoonData, areaPlatoonTotal, date)

      //计算上海市各类型学校的排菜统计 (level)
      val levelPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._2._1.equals("null")).map(_._2)
      new PlatoonTotalStat().levelplatoontotal(platoonData, levelPlatoonTotal, date, schoolData)

      //计算各区各类型学校的排菜统计 (level)
      val arealevelPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._5._1.equals("null")).map(_._5)
      new PlatoonTotalStat().arealevelplatoontotal(platoonData, arealevelPlatoonTotal, date, schoolData)

      //计算上海市按照学校性质的排菜统计 (nature)
      val naturePlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._3._1.equals("null")).map(_._3)
      new PlatoonTotalStat().natureplatoontotal(platoonData, naturePlatoonTotal, date, schoolData)

      //计算各区按照学校性质的排菜统计 (nature)
      val areaNaturePlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._6._1.equals("null")).map(_._6)
      new PlatoonTotalStat().areanatureplatoontotal(platoonData, areaNaturePlatoonTotal, date, schoolData)

      //计算上海市按照学校食堂性质排菜统计 (canteenmode)
      val canteenPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._4._1.equals("null")).map(_._4)
      new PlatoonTotalStat().canteenplatoontotal(platoonData, canteenPlatoonTotal, date, schoolData)

      //计算各区按照学校食堂性质排菜统计 (canteenmode)
      val areacanteenPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._7._1.equals("null")).map(_._7)
      new PlatoonTotalStat().areacanteenplatoontotal(platoonData, areacanteenPlatoonTotal, date, schoolData)

      //计算按照区属的排菜统计
      val masteridPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._8._1.equals("null")).map(_._8)
      new PlatoonTotalStat().masteridplatoontotal(platoonData, masteridPlatoonTotal, date, schoolData,commiteeData)

    }

    sc.stop()
  }


}
