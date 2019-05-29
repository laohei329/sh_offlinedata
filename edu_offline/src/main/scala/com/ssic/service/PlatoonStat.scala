package com.ssic.service

import java.util.Calendar

import com.ssic.impl.PlatoonFunc
import com.ssic.report.Platoon.format
import com.ssic.utils.{JPools, Tools}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession

import scala.collection.mutable

class PlatoonStat extends PlatoonFunc{

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  override def platoredis(data: (SparkSession, RDD[(String, String)],String, Broadcast[Map[String, Int]], Broadcast[Map[String, Int]], Broadcast[Map[String, String]], Broadcast[Map[String, String]],String,mutable.Set[String])): Unit = {

    val session = data._1
    val plaData = data._2
    val date = data._3
    val holiday = data._4
    val calen = data._5
    val schoolTerm = data._6
    val schoolTermSys = data._7
    val year = data._8
    val platoondata = data._9

    val week = format.parse(date)
    val calendar = Calendar.getInstance()
    calendar.setTime(week)
    val d = calendar.get(Calendar.DAY_OF_WEEK) - 1

    Tools.schoolid(session).cogroup(plaData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(date + "_platoon-feed", k)

            } else{

              val holidayData = holiday.value.getOrElse(date, -1) //假期信息表
              val haveClass = calen.value.getOrElse(v._1.head, -1) //供餐信息表
              val schoolTermData = schoolTerm.value.getOrElse(v._1.head, "null")   //学期设置信息表
              val schoolTermSysData = schoolTermSys.value.getOrElse(year, "null") //系统学期信息表

              if(haveClass == 0){
                //供餐表有数据，就参考供餐表数据，不供餐
                val value = new GongcanStat().nogongcan()

                jedis.hset(date + "_platoon-feed", k, value)
              }else if(haveClass == 1){
                //供餐表有数据，就参考供餐表数据，供餐
                val value = new GongcanStat().gongcan(platoondata,k,jedis,date)

                jedis.hset(date + "_platoon-feed", k, value)

              }else{
                //没有供餐表数据，先查看今天是否是假期
                if (holidayData == 1){
                  //在假期中,默认不供餐
                  val value = new GongcanStat().nogongcan()

                  jedis.hset(date + "_platoon-feed", k, value)
                }else if (holidayData == 2) {
                  //法定加班,默认供餐
                  //查看是否在学期设置内
                  if("1".equals(schoolTermData)){
                    //在学期设置内,供餐
                    val value = new GongcanStat().gongcan(platoondata,k,jedis,date)

                    jedis.hset(date + "_platoon-feed", k, value)
                  }else if("0".equals(schoolTermData)){
                    //有学期设置但是过期，不供餐
                    val value = new GongcanStat().nogongcan()

                    jedis.hset(date + "_platoon-feed", k, value)

                  }else{
                    //没有设置学期设置，查询系统学期设置
                    if("1".equals(schoolTermSysData)){
                      //在系统学期设置内，供餐
                      val value = new GongcanStat().gongcan(platoondata,k,jedis,date)

                      jedis.hset(date + "_platoon-feed", k, value)
                    }else {
                      //不在系统学期设内，不供餐
                      val value = new GongcanStat().nogongcan()

                      jedis.hset(date + "_platoon-feed", k, value)
                    }

                  }

                }else {
                  //假期表没有数据,需要查看今天周几，周一到周五默认供餐，周末默认不供餐
                  if (d == 1 || d == 2 || d == 3 || d == 4 || d == 5){
                    //默认供餐
                    if("1".equals(schoolTermData)){
                      //在学期设置内,供餐
                      val value = new GongcanStat().gongcan(platoondata,k,jedis,date)

                      jedis.hset(date + "_platoon-feed", k, value)
                    }else if("0".equals(schoolTermData)){
                      //有学期设置但是过期，不供餐
                      val value = new GongcanStat().nogongcan()

                      jedis.hset(date + "_platoon-feed", k, value)

                    }else{
                      //没有设置学期设置，查询系统学期设置
                      if("1".equals(schoolTermSysData)){
                        //在系统学期设置内，供餐
                        val value = new GongcanStat().gongcan(platoondata,k,jedis,date)

                        jedis.hset(date + "_platoon-feed", k, value)
                      }else {
                        //不在系统学期设内，不供餐
                        val value = new GongcanStat().nogongcan()

                        jedis.hset(date + "_platoon-feed", k, value)
                      }

                    }
                  }else{
                    //周末默认不供餐
                    val value = new GongcanStat().nogongcan()

                    jedis.hset(date + "_platoon-feed", k, value)
                  }
                }
              }

            }

        })
    })

  }
}
