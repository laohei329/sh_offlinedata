package com.ssic.service

import java.util.Calendar
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

class PlatoonStat {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  /**

    * * 排菜，供餐数据到redis

    * * @param SparkSession

    * * @param plaData redis已存在的排菜数据

    * * @param date 时间

    * * @param holiday 当天是否是假期

    * * @param calen 供餐表是否有数据，对应的学校id是否供餐

    * * @param schoolTerm 查询学校的学期设置是否是有效的

    * * @param schoolTermSys 查询当天是否在系统学期设置内

    * * @param term_year 学年

    * * @param platoondata 排菜表的key

    * * @param b2bPlatoonSortData b2b的排菜表数据

    */
  def platoredis(sparkSession:SparkSession, plaData:RDD[(String, String)],date:String, holiday:Broadcast[Map[String, Int]], calen:Broadcast[Map[String, String]], schoolTerm:Broadcast[Map[String, String]], schoolTermSys:Broadcast[Map[String, String]],term_year:String,platoondata:mutable.Set[String],b2bPlatoonSortData: Map[String, String]): Unit = {

    val week = format.parse(date)
    val calendar = Calendar.getInstance()
    calendar.setTime(week)
    val d = calendar.get(Calendar.DAY_OF_WEEK) - 1

    Tools.schoolid((sparkSession,date)).cogroup(plaData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(date + "_platoon-feed", k)

            } else{

              val holidayData = holiday.value.getOrElse(date, -1) //假期信息表
              var haveClass = -1  //供餐信息表
              var reason ="null"
              val calenData = calen.value.getOrElse(v._1.head, "null")
              if (!"null".equals(calenData)){
                haveClass= calenData.split("_")(0).toInt
                reason= calenData.split("_")(1)
              }else{
                haveClass
                reason
              }
              //val haveClass = calen.value.getOrElse(v._1.head, -1)

              val schoolTermData = schoolTerm.value.getOrElse(v._1.head, "null")   //学期设置信息表
              val schoolTermSysData = schoolTermSys.value.getOrElse(term_year, "null") //系统学期信息表

              if(haveClass == 0){
                //供餐表有数据，就参考供餐表数据，不供餐
                val value = new GongcanStat().nogongcan()
                jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")
              }else if(haveClass == 1){
                //供餐表有数据，就参考供餐表数据，供餐
                val value = new GongcanStat().gongcan(platoondata,k,jedis,date,b2bPlatoonSortData)
                val plastatus = new RuleStatusStat().platoonrulestatus(date,d,value)

                jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+"null"+"_"+"plastatus"+"_"+plastatus)

              }else{
                //没有供餐表数据，先查看是否有提前排菜，
                if(new GongcanStat().booleangongcan(platoondata,k,jedis,date)){
                  //有提前排菜，供餐
                  val value = new GongcanStat().gongcan(platoondata,k,jedis,date,b2bPlatoonSortData)
                  val plastatus = new RuleStatusStat().platoonrulestatus(date,d,value)

                  jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+"null"+"_"+"plastatus"+"_"+plastatus)
                }else{
                  //没有提前排菜，今天是否是假期
                  if (holidayData == 1){
                    //在假期中,默认不供餐
                    val value = new GongcanStat().nogongcan()

                    jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")
                  }else if (holidayData == 2) {
                    //法定加班,默认供餐
                    //查看是否在学期设置内
                    if("1".equals(schoolTermData)){
                      //在学期设置内,供餐
                      val value = new GongcanStat().gongcan(platoondata,k,jedis,date,b2bPlatoonSortData)
                      val plastatus = new RuleStatusStat().platoonrulestatus(date,d,value)

                      jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+"null"+"_"+"plastatus"+"_"+plastatus)
                    }else if("0".equals(schoolTermData)){
                      //有学期设置但是过期，不供餐
                      val value = new GongcanStat().nogongcan()
                      jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")

                    }else{
                      //没有设置学期设置，查询系统学期设置
                      if("1".equals(schoolTermSysData)){
                        //在系统学期设置内，供餐

                        val value = new GongcanStat().gongcan(platoondata,k,jedis,date,b2bPlatoonSortData)
                        val plastatus = new RuleStatusStat().platoonrulestatus(date,d,value)

                        jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+"null"+"_"+"plastatus"+"_"+plastatus)
                      }else {
                        //不在系统学期设内，不供餐
                        val value = new GongcanStat().nogongcan()

                        jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")
                      }

                    }

                  }else {
                    //假期表没有数据,需要查看今天周几，周一到周五默认供餐，周末默认不供餐
                    if (d == 1 || d == 2 || d == 3 || d == 4 || d == 5){
                      //默认供餐
                      if("1".equals(schoolTermData)){
                        //在学期设置内,供餐
                        val value = new GongcanStat().gongcan(platoondata,k,jedis,date,b2bPlatoonSortData)
                        val plastatus = new RuleStatusStat().platoonrulestatus(date,d,value)

                        jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+"null"+"_"+"plastatus"+"_"+plastatus)
                      }else if("0".equals(schoolTermData)){
                        //有学期设置但是过期，不供餐
                        val value = new GongcanStat().nogongcan()
                        jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")

                      } else{
                        //没有设置学期设置，查询系统学期设置
                        if("1".equals(schoolTermSysData)){
                          //在系统学期设置内，供餐
                          val value = new GongcanStat().gongcan(platoondata,k,jedis,date,b2bPlatoonSortData)
                          val plastatus = new RuleStatusStat().platoonrulestatus(date,d,value)

                          jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+"null"+"_"+"plastatus"+"_"+plastatus)
                        }else {
                          //不在系统学期设内，不供餐
                          val value = new GongcanStat().nogongcan()

                          jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")
                        }

                      }

                    }else{
                      //周末默认不供餐
                      val value = new GongcanStat().nogongcan()

                      jedis.hset(date + "_platoon-feed", k, value+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"5")
                    }
                  }
                }

              }

            }

        })
    })

  }
}
