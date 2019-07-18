package com.ssic.report

import com.ssic.beans.SchoolBean
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD


/**
  * Created by 云 on 2018/8/22.
  * 排菜计划功能指标
  */
object PlatoonPlan {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def PlatoonRealTimeData(plaData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, Int]], Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, Int]], Broadcast[Map[String, String]], Broadcast[Map[String, String]])): RDD[(String, String, String, String, String, String, String, String, String,String)] = {
    val platoonData = plaData._1.filter(x => x != null && x.table.equals("t_saas_package")  && !x.data.stat.equals("0") && x.data.is_publish != 0)
    val platoonDataFil = platoonData.distinct().filter(x => StringUtils.isNoneEmpty(x.data.supply_date)).filter(x => StringUtils.isNoneEmpty(x.data.school_id)).map({
      x =>
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val school_id = x.data.school_id
        val menu_id = x.data.menu_id //'菜谱ID(一个项目点一天的排菜)'
      val quhao = plaData._2.value.getOrElse(school_id, "null")
        val create_time = x.data.create_time

        var school_nature_name = "null"
        val school_nature = plaData._4.value.getOrElse(school_id, "null")
        if (StringUtils.isNoneEmpty(school_nature)) {
          if (school_nature.equals("1") || school_nature.equals("0") || school_nature.equals("0,1,2,3") || school_nature.equals("0,2")) {
            school_nature_name = "0"
          } else if (school_nature.equals("3") || school_nature.equals("2") || school_nature.equals("2,3")) {
            school_nature_name = "2"
          } else if (school_nature.equals("4")) {
            school_nature_name = "4"
          } else {
            school_nature_name = school_nature
          }
        } else {
          school_nature_name
        }

        var school_nature_sub_name="null"
        val school_nature_sub = plaData._7.value.getOrElse(school_id,"null")
        if(StringUtils.isNoneEmpty(school_nature_sub)){
          school_nature_sub_name=school_nature
        }else{
          school_nature_sub_name
        }

        var canteen_mode_name = "null"
        val canteen_mode = plaData._3.value.getOrElse(school_id, "null")
        if(canteen_mode != null && !canteen_mode.equals("null")){
          if(canteen_mode.toString.equals("0") || canteen_mode.toString.equals("2")){
            canteen_mode_name="0"
          }else if(canteen_mode.toString.equals("1") || canteen_mode.toString.equals("2")){
            canteen_mode_name="1"
          } else{
            canteen_mode_name
          }
        } else{
          canteen_mode_name
        }

        var ledger_type_name ="null"
        val ledger_type = plaData._8.value.getOrElse(school_id,"null")
        if(StringUtils.isNoneEmpty(ledger_type)){
          ledger_type_name=ledger_type
        }else{
          ledger_type_name
        }

        var level_name = "null"
        val level = plaData._5.value.getOrElse(school_id, "null")
        val level2 = plaData._6.value.getOrElse(school_id, "null")
        if(level2 != null && !level2.equals("null")){
          level_name=level2.toString
        } else{
          if(StringUtils.isEmpty(level)) {
            level_name
          }else if ("0".equals(level)) {
            level_name = "3" //幼儿园
          } else if ("1".equals(level)) {
            level_name = "7" //小学
          } else if ("2".equals(level)) {
            level_name = "8" //初级中学
          } else if ("3".equals(level)) {
            level_name = "9" //高级中学
          } else if ("6".equals(level)) {
            level_name = "13" //职业初中
          } else if ( "7".equals(level)) {
            level_name = "0" //托儿所（托班）
          } else if ("9".equals(level)) {
            level_name = "17" //其他
          } else if ("0,7".equals(level)) {
            level_name = "1" //托幼园（托儿所+幼儿园）
          } else if ( "7,0".equals(level)) {
            level_name = "1" //托幼园（托儿所+幼儿园）
          } else if ("1,2".equals(level)) {
            level_name = "11" //九年一贯制学校
          } else if ( "2,3".equals(level)) {
            level_name = "10" //完全中学
          } else if ("1,2,3".equals(level)) {
            level_name = "12" //十二年一贯制学校
          } else if ("1,2,3,6".equals(level)) {
            level_name = "12" //十二年一贯制学校
          } else if ( "0,1".equals(level)) {
            level_name = "4" // 幼小（幼儿园+小学）
          } else if ("3,6".equals(level)) {
            level_name = "14" // 中等职业学校
          } else if ( "0,6".equals(level)) {
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
          } else if ( "3,9".equals(level)) {
            level_name = "17" // 其他
          } else if ("0,1,7".equals(level)) {
            level_name = "2" // 托幼小（托儿所+幼儿园+小学）
          }
          else if ("0,1,2,3".equals(level)) {
            level_name = "6" // 幼小初高（幼儿园+小学+初中+高中
          } else if ("0,2".equals(level)) {
            level_name = "5" //  幼小初（幼儿园+小学+初中）
          } else{
            level_name
          }
        }

        //时间，学校id，区号，经营模式，学校性质，学校类型
        (date, school_id, quhao, canteen_mode_name, school_nature_name, level_name,school_nature_sub_name,ledger_type_name,x.`type`,create_time)
    })
    platoonDataFil
  }
}
