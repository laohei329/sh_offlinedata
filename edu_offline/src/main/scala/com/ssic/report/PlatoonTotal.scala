package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.Platoon.{format, format1}
import com.ssic.service.{DealDataStat, PlatoonTotalStat}
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

import scala.collection.JavaConverters._

/*
* 加了供餐逻辑的排菜统计数据
* */
object PlatoonTotal {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val school = session.read.jdbc(url, edu_school, conn)
    val committee = session.read.jdbc(url, edu_committee, conn)
    val bd_department = session.read.jdbc(url, edu_bd_department, conn)

    school.createTempView("t_edu_school")
    committee.createTempView("t_edu_committee")
    bd_department.createTempView("t_edu_bd_department")

    for (i <- 0 to 6) {
      //查询一周的排菜数据
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)

      val schoolData: Broadcast[Map[String, List[String]]] = sc.broadcast(Tools.schoolNew(session))
      val commiteeData: Broadcast[Map[String, String]] = sc.broadcast(Tools.school2Committee(session))

      val jedis = JPools.getJedis

      val departmentid = Tools.departmentid(session)
      for (i <- 0 until departmentid.size){
        val id = departmentid(i)
        val jedis = JPools.getJedis
        val keys = jedis.del(date+"_platoonfeed-total"+"_"+"department"+"_"+id)
      }

      val total = jedis.hgetAll(date + "_platoonfeed-total")
      val platoonTotal = sc.parallelize(total.asScala.toList) //已存在的排菜统计表的数据

      val platoon_feed = jedis.hgetAll(date + "_platoon-feed")
      val platoon = sc.parallelize(platoon_feed.asScala.toList) //已存在的排菜表的详情数据，对这个详情数据进行各个维度的统计

      val platoonTotalData = new DealDataStat().platoontotal(platoonTotal)

      val platoonData = platoon.map({
        x =>
          //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4")
          if (x._2.split("_").size > 2) {
            (x._1, x._2.split("_create-time")(0))
          } else {
            (x._1, x._2)
          }
      })

      //计算市教委各区的排菜统计情况
      val areaPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._1._1.equals("null")).map(_._1)
      new PlatoonTotalStat().areaplatoontotal(platoonData, areaPlatoonTotal, date)

      //计算市教委各区的排菜操作规则数量统计
      val areaPlastatusTotal = platoonTotalData.filter(x => !x._10._1.equals("null")).map(_._10)
      new PlatoonTotalStat().areaplastatustotal(platoon,areaPlastatusTotal,date)

      //计算权限管理部门各区的排菜统计情况
      new PlatoonTotalStat().departmentareaplatoontotal(platoon,date,schoolData)

      //计算权限管理部门各区的排菜操作规则数量统计
      new PlatoonTotalStat().departmentareaplastatustotal(platoon,date,schoolData)

      //计算上海市各类型学校的排菜统计 (level)
      val levelPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._2._1.equals("null")).map(_._2)
      new PlatoonTotalStat().levelplatoontotal(platoonData, levelPlatoonTotal, date, schoolData)

      //计算上海市各类型学校的排菜操作规则数量统计
      val levelPlastatusTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._13._1.equals("null")).map(_._13)
      new PlatoonTotalStat().levelplastatustotal(platoon,levelPlastatusTotal,date,schoolData)

      //计算权限管理部门上海市各类型学校的排菜统计 (level)
      new PlatoonTotalStat().departmentlevelplatoontotal(platoon,date,schoolData)

      //计算权限管理部门上海市各类型学校的排菜操作规则数量统计(level)
      new PlatoonTotalStat().departmentlevelplastatustotal(platoon,date,schoolData)

      //计算各区各类型学校的排菜统计 (level)
      val arealevelPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._5._1.equals("null")).map(_._5)
      new PlatoonTotalStat().arealevelplatoontotal(platoonData, arealevelPlatoonTotal, date, schoolData)

      //计算权限管理部门各区各类型学校的排菜统计 (level)
      new PlatoonTotalStat().departmentarealevelplatoontotal(platoon,date,schoolData)

      //计算上海市按照学校性质的排菜统计 (nature)
      val naturePlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._3._1.equals("null")).map(_._3)
      new PlatoonTotalStat().natureplatoontotal(platoonData, naturePlatoonTotal, date, schoolData)

      //计算上海市按照学校性质的排菜操作规则数量统计 (nature)
      val naturePlastatusTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._14._1.equals("null")).map(_._14)
      new PlatoonTotalStat().natureplastatustotal(platoon,naturePlastatusTotal,date,schoolData)

      //计算权限管理部门上海市按照学校性质的排菜统计 (nature)
      new PlatoonTotalStat().departmentnatureplatoontotal(platoon,date,schoolData)

      //计算权限管理部门上海市按照学校性质的排菜操作规则数量统计 (nature)
      new PlatoonTotalStat().departmentnatureplastatustotal(platoon,date,schoolData)

      //计算各区按照学校性质的排菜统计 (nature)
      val areaNaturePlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._6._1.equals("null")).map(_._6)
      new PlatoonTotalStat().areanatureplatoontotal(platoonData, areaNaturePlatoonTotal, date, schoolData)

      //计算权限管理部门各区按照学校性质的排菜统计 (nature)
      new PlatoonTotalStat().departmentareanatureplatoontotal(platoon,date,schoolData)

      //计算上海市按照学校食堂性质排菜统计 (canteenmode)
      val canteenPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x => !x._4._1.equals("null")).map(_._4)
      new PlatoonTotalStat().canteenplatoontotal(platoonData, canteenPlatoonTotal, date, schoolData)

      //计算权限管理部门上海市按照学校食堂性质排菜统计 (canteenmode)
      new PlatoonTotalStat().departmentcanteenplatoontotal(platoon,date,schoolData)

      //计算各区按照学校食堂性质排菜统计 (canteenmode)
      val areacanteenPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._7._1.equals("null")).map(_._7)
      new PlatoonTotalStat().areacanteenplatoontotal(platoonData, areacanteenPlatoonTotal, date, schoolData)

      //计算权限管理部门各区按照学校食堂性质排菜统计 (canteenmode)
      new PlatoonTotalStat().departmentareacanteenplatoontotal(platoon,date,schoolData)

      //计算按照区属的排菜统计
      val masteridPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._8._1.equals("null")).map(_._8)
      new PlatoonTotalStat().masteridplatoontotal(platoonData, masteridPlatoonTotal, date, schoolData,commiteeData)

      //计算按照区属的排菜操作规则数量统计
      val masteridlPlastatusTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._11._1.equals("null")).map(_._11)
      new PlatoonTotalStat().masteridplastatustotal(platoon,masteridlPlastatusTotal,date,schoolData,commiteeData)

      //计算权限管理部门按照区属的排菜统计
      new PlatoonTotalStat().departmentmasteridplatoontotal(platoon,date,schoolData,commiteeData)

      //计算权限管理部门按照区属的排菜操作规则数量统计
      new PlatoonTotalStat().departmentmasteridplastatustotal(platoon,date,schoolData,commiteeData)

      //计算上海市按照管理部门的维度的统计数据
      val departmentPlatoonTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._9._1.equals("null")).map(_._9)
      new PlatoonTotalStat().shanghaidepartmentplatoontotal(platoonData,departmentPlatoonTotal,date,schoolData)

      //计算上海市按照管理部门的维度的排菜操作规则数量统计
      val departmentlPlastatusTotal: RDD[(String, String)] = platoonTotalData.filter(x=> !x._12._1.equals("null")).map(_._12)
      new PlatoonTotalStat().shanghaidepartmentplastatustotal(platoon,departmentlPlastatusTotal,date,schoolData)

      //计算权限管理部门按照管理部门的维度的统计数据
      new PlatoonTotalStat().departmentdepartmentplatoontotal(platoon,date,schoolData)

      //计算权限管理部门按照管理部门的排菜操作规则数量统计
      new PlatoonTotalStat().departmentdepartmentplastatustotal(platoon,date,schoolData)

    }

    sc.stop()
  }


}
