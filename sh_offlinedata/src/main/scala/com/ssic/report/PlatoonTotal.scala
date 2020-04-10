package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.Platoon.{format, format1}
import com.ssic.service._
import com.ssic.utils.{JPools, Tools}
import com.ssic.utils.Tools._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/*
* 加了供餐逻辑的排菜统计数据
* */
object PlatoonTotal {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("今日至未来一周的排菜统计数据")
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

    for (i <- 0 to 9) {
      //查询一周的排菜数据
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)

      //对学校的基础信息按照新规则进行清洗
      val schoolData: Broadcast[Map[String, List[String]]] = sc.broadcast(Tools.schoolNew(session))
      //查询教属的基础信息
      val commiteeData: Broadcast[Map[String, String]] = sc.broadcast(Tools.school2Committee(session))

      val jedis = JPools.getJedis

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
      val areaplatoontotal = new PlatoonTotalStat().areatotal(platoonData, date)
      //计算上海市各类型学校的排菜统计 (level)
      val levelplatoontotal = new PlatoonTotalStat().leveltotal(platoonData, date, schoolData)
      //计算各区各类型学校的排菜统计 (level)
      val arealevelplatoontotal = new PlatoonTotalStat().arealeveltotal(platoonData, date, schoolData)
      //计算上海市按照学校性质的排菜统计 (nature)
      val natureplatoontotal = new PlatoonTotalStat().naturetotal(platoonData, date, schoolData)
      //计算各区按照学校性质的排菜统计 (nature)
      val areanatureplatoontotal = new PlatoonTotalStat().areanaturetotal(platoonData, date, schoolData)
      //计算上海市按照学校食堂性质排菜统计 (canteenmode)
      val canteenplatoontotal = new PlatoonTotalStat().canteentotal(platoonData, date, schoolData)
      //计算各区按照学校食堂性质排菜统计 (canteenmode)
      val areacanteenplatoontotal = new PlatoonTotalStat().areacanteentotal(platoonData, date, schoolData)
      //计算按照区属的排菜统计
      val masteridplatoontotal = new PlatoonTotalStat().masteridtotal(platoonData, date, schoolData, commiteeData)
      //计算上海市按照管理部门的维度的统计数据
      val shanghaidepartmentplatoontotal = new PlatoonTotalStat().shanghaidepartmenttotal(platoonData, date, schoolData)

      //计算市教委各区的排菜操作规则数量统计
      val areaplatoonruletotal = new PlatoonRuleTotalStat().areatotal(platoon, date)
      //计算上海市各类型学校的排菜操作规则数量统计(level)
      val levelplatoonruletotal = new PlatoonRuleTotalStat().leveltotal(platoon, date, schoolData)
      //计算上海市按照学校性质的排菜操作规则数量统计 (nature)
      val natureplatoonruletotal = new PlatoonRuleTotalStat().naturetotal(platoon, date, schoolData)
      //计算按照区属的排菜操作规则数量统计
      val masteridplatoonruletotal = new PlatoonRuleTotalStat().masteridtotal(platoon, date, schoolData, commiteeData)
      //计算上海市按照管理部门的维度的排菜操作规则数量统计
      val shanghaidepartmentplatoonruletotal = new PlatoonRuleTotalStat().shanghaidepartmenttotal(platoon, date, schoolData)

      //计算权限管理部门各区的排菜统计情况
      val departmentareaplatoontotal = new PlatoonTotalStat().departmentareatotal(platoon, date, schoolData)
      //计算权限管理部门上海市各类型学校的排菜统计 (level)
      val departmentlevelplatoontotal = new PlatoonTotalStat().departmentleveltotal(platoon, date, schoolData)
      //计算权限管理部门各区各类型学校的排菜统计 (level)
      val departmentarealevelplatoontotal = new PlatoonTotalStat().departmentarealeveltotal(platoon, date, schoolData)
      //计算权限管理部门上海市按照学校性质的排菜统计 (nature)
      val departmentnatureplatoontotal = new PlatoonTotalStat().departmentnaturetotal(platoon, date, schoolData)
      //计算权限管理部门各区按照学校性质的排菜统计 (nature)
      val departmentareanatureplatoontotal = new PlatoonTotalStat().departmentareanaturetotal(platoon, date, schoolData)
      //计算权限管理部门上海市按照学校食堂性质排菜统计 (canteenmode)
      val departmentcanteenplatoontotal = new PlatoonTotalStat().departmentcanteentotal(platoon, date, schoolData)
      //计算权限管理部门各区按照学校食堂性质排菜统计 (canteenmode)
      val departmentareacanteenplatoontotal = new PlatoonTotalStat().departmentareacanteentotal(platoon, date, schoolData)
      //计算权限管理部门按照区属的排菜统计
      val departmentmasteridplatoontotal = new PlatoonTotalStat().departmentmasteridtotal(platoon, date, schoolData, commiteeData)
      //计算权限管理部门按照管理部门的维度的统计数据
      val departmentdepartmentplatoontotal = new PlatoonTotalStat().departmentdepartmenttotal(platoon, date, schoolData)

      //计算权限管理部门各区的排菜操作规则数量统计
      val departmentareaplatoonruletotal = new PlatoonRuleTotalStat().departmentareatotal(platoon, date, schoolData)
      //计算权限管理部门上海市各类型学校的排菜操作规则数量统计(level)
      val departmentlevelplatoonruletotal = new PlatoonRuleTotalStat().departmentleveltotal(platoon, date, schoolData)
      //计算权限管理部门上海市按照学校性质的排菜操作规则数量统计 (nature)
      val departmentnatureplatoonruletotal = new PlatoonRuleTotalStat().departmentnaturetotal(platoon, date, schoolData)
      //计算权限管理部门按照区属的排菜操作规则数量统计
      val departmentmasteridplatoonruletotal = new PlatoonRuleTotalStat().departmentmasteridtotal(platoon, date, schoolData, commiteeData)
      //计算权限管理部门按照管理部门的排菜操作规则数量统计
      val departmentdepartmentplatoonruletotal = new PlatoonRuleTotalStat().departmentdepartmenttotal(platoon, date, schoolData)

      //上海市的排菜统计
      areaplatoontotal
        .union(levelplatoontotal)
        .union(arealevelplatoontotal)
        .union(natureplatoontotal)
        .union(areanatureplatoontotal)
        .union(canteenplatoontotal)
        .union(areacanteenplatoontotal)
        .union(masteridplatoontotal)
        .union(shanghaidepartmentplatoontotal)
        .union(areaplatoonruletotal)
        .union(levelplatoonruletotal)
        .union(natureplatoonruletotal)
        .union(masteridplatoonruletotal)
        .union(shanghaidepartmentplatoonruletotal).cogroup(platoonTotal).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date + "_platoonfeed-total", k, "0")
              } else {
                jedis.hset(date + "_platoonfeed-total", k, v._1.head.toString)
              }
          })
      })

      //各管理部门的排菜统计
      val departmentid = Tools.departmentid(session)
      for (i <- 0 until departmentid.size) {
        val id = departmentid(i)
        val jedis = JPools.getJedis
        val departmenttotal = jedis.hgetAll(date + "_platoonfeed-total" + "_" + "department" + "_" + id)
        val departmentPlatoonTotal = sc.parallelize(departmenttotal.asScala.toList)

        departmentareaplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3))
          .union(departmentlevelplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentarealevelplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentnatureplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentareanatureplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentcanteenplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentareacanteenplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentmasteridplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentdepartmentplatoontotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentareaplatoonruletotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentlevelplatoonruletotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentnatureplatoonruletotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentmasteridplatoonruletotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .union(departmentdepartmentplatoonruletotal.filter(x => x._1.equals(id)).map(x => (x._2, x._3)))
          .cogroup(departmentPlatoonTotal).foreachPartition({
          itr =>
            val jedis = JPools.getJedis
            itr.foreach({
              case (k, v) =>
                //表示左边没有，右边有
                if (v._1.size == 0) {
                  jedis.hset(date + "_platoonfeed-total" + "_" + "department" + "_" + id, k, "0")
                  jedis.expire(date + "_platoonfeed-total" + "_" + "department" + "_" + id, 604800)
                } else {
                  jedis.hset(date + "_platoonfeed-total" + "_" + "department" + "_" + id, k, v._1.head.toString)
                  jedis.expire(date + "_platoonfeed-total" + "_" + "department" + "_" + id, 604800)
                }

            })
        })
      }

    }

    sc.stop()
  }


}
