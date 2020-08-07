package com.ssic.utils


import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  *
  */

object SchoolRule {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /**

    * * 学制

    * * @param level 老学制字段

    * * @param level2 新学制字段

    */
  def LevelName(level: String, level2: String): String = {
    var level_name = "null"
    if (StringUtils.isNoneEmpty(level2)) {
      level_name = level2
    } else {
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
    }
    return level_name;
  }

  /**

    * * 学校性质

    * * @param school_nature //公办，民办

    * * @param school_nature_sub //CTIVE(1, "集体办"),TROOPS(2, "部队办"),INSTITUTION(3, "企事业办"),COMPANY(4, "企业合作"),INTERNATIONAL(5, "国际办"),OTHER(9, "其它")

    */

  def SchoolNatrue(school_nature:String,school_nature_sub:String):(String,String)={
    //GNGB(0, "公办"),GNMB(2, "民办"),OTHER(4, "其他");3, "外籍人员子女学校"
    var school_nature_name = "null"
    var school_nature_sub_name = "null"
    if (StringUtils.isNoneEmpty(school_nature)) {
      if (school_nature.equals("1") || school_nature.equals("0")) {
        school_nature_name = "0"
      } else if (school_nature.equals("2")) {
        school_nature_name = "2"
      } else if (school_nature.equals("4")) {
        school_nature_name = "4"
      } else if (school_nature.equals("3")) {
        school_nature_name = "3"
      }
      else {
        school_nature_name = "4"
      }
    } else {
      school_nature_name
    }

    if (StringUtils.isNoneEmpty(school_nature_sub)) {
      school_nature_sub_name = school_nature_sub
    } else {
      school_nature_sub_name
    }
    return (school_nature_name,school_nature_sub_name)
  }

  /**

    * * 食堂性质

    * * 学校  0 自行加工 1 食品加工商
    *   外包  0 现场加工  1 快餐配送
    * * @param license_main_type //公办，民办

    * * @param license_main_child //CTIVE(1, "集体办"),TROOPS(2, "部队办"),INSTITUTION(3, "企事业办"),COMPANY(4, "企业合作"),INTERNATIONAL(5, "国际办"),OTHER(9, "其它")

    */

  def Cantoon(license_main_type:String,license_main_child:String): (String,String) ={
    var license_main_type_name = "null"
    var license_main_child_name = "null"
    if (StringUtils.isNoneEmpty(license_main_type)) {
      license_main_type_name = license_main_type
    } else {
      license_main_type_name
    }


    if (StringUtils.isNoneEmpty(license_main_child)) {
      license_main_child_name = license_main_child
    } else {
      license_main_child_name
    }
    return (license_main_type_name,license_main_child_name)
  }





}
