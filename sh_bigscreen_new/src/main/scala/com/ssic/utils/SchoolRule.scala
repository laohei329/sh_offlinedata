package com.ssic.utils

import com.ssic.beans.{DataBean, SchoolBean}
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  * 学校  0 自行加工 1 食品加工商
  * 外包  0 现场加工  1 快餐配送
  */

object SchoolRule {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def SchoolNew(filterData: (SchoolBean,String,String)): (String, String, String, String, String, String, String,String,String,String) = {
    val stat = filterData._1.data.stat
    //区号
    //  将上海市各区的新的district_id映射 => 旧的area的映射
    val area = NewSchoolToOldSchool.committeeToOldArea(filterData._1.data.seat_district_id)
    //学校类型
    var level_name = "null"
    val level = filterData._1.data.level
    val level2 = filterData._1.data.level2
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

    //GNGB(0, "公办"),GNMB(2, "民办"),OTHER(4, "其他");3, "外籍人员子女学校"
    var school_nature_name = "null"
    var school_nature_sub_name = "null"
    val school_nature = filterData._1.data.school_nature //公办，民办
    val school_nature_sub = filterData._1.data.school_nature_sub //CTIVE(1, "集体办"),TROOPS(2, "部队办"),INSTITUTION(3, "企事业办"),COMPANY(4, "企业合作")INTERNATIONAL(5, "国际办"),OTHER(9, "其它"),
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
    //食堂经营
    var license_main_type_name = "null"
    var license_main_child_name = "null"
    val license_main_type = filterData._1.data.license_main_type
    val license_main_child = filterData._1.data.license_main_child
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


    //区属
    val department_master_id = filterData._2
    val department_slave_id = filterData._3

    //是否审核通过
    val reviewed = filterData._1.data.reviewed

    (area, level_name, school_nature_name, license_main_type_name, stat, license_main_child_name, school_nature_sub_name,department_master_id,department_slave_id,reviewed)
  }


  def SchoolOld(filterData: (SchoolBean,Broadcast[Map[String, String]], Broadcast[Map[String, String]])): (String, String, String, String, String, String, String, String, String, String) = {
    var oldbean: DataBean = null
    var stat_name = "null"
    var area_name = "null"
    var level_name = "null"
    var school_nature_name = "null"
    var school_nature_sub_name = "null"
    var license_main_type_name = "null"
    var license_main_child_name = "null"
    var department_master_id_name ="null"
    var department_slave_id_name ="null"
    var committee_org_merchant_id_name="null"
    var reviewed_name ="null"
    try {
      oldbean = filterData._1.old
      stat_name = oldbean.stat
      area_name = NewSchoolToOldSchool.committeeToOldArea(oldbean.area)
      level_name = oldbean.level2
      school_nature_name = oldbean.school_nature
      school_nature_sub_name = oldbean.school_nature_sub
      license_main_type_name = oldbean.license_main_type
      license_main_child_name = oldbean.license_main_child
      committee_org_merchant_id_name = oldbean.committee_org_merchant_id
      reviewed_name = oldbean.reviewed
    } catch {
      case e: Exception => {
        logger.error(s"parse json error: $oldbean", e)
        return ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
      }

    }

    //区号
    if (StringUtils.isEmpty(oldbean.stat)) {
      stat_name
    } else {
      stat_name = oldbean.stat
    }

    if (StringUtils.isEmpty(oldbean.seat_district_id)) {
      area_name
    } else {
      area_name = NewSchoolToOldSchool.committeeToOldArea(oldbean.seat_district_id)
    }

    //学校类型

    val level = oldbean.level
    val level2 = oldbean.level2
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

    //GNGB(0, "公办"),GNMB(2, "民办"),OTHER(4, "其他");

    val school_nature = oldbean.school_nature //公办，民办
    val school_nature_sub = oldbean.school_nature_sub //CTIVE(1, "集体办"),TROOPS(2, "部队办"),INSTITUTION(3, "企事业办"),COMPANY(4, "企业合作")INTERNATIONAL(5, "国际办"),OTHER(9, "其它"),
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

    //食堂经营
    val license_main_type = oldbean.license_main_type
    val license_main_child = oldbean.license_main_child
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

    //区属
    val committee_org_merchant_id = oldbean.committee_org_merchant_id
    if(StringUtils.isNoneEmpty(committee_org_merchant_id)){
      committee_org_merchant_id_name=committee_org_merchant_id
    }else{
      committee_org_merchant_id_name
    }
    val master_id = filterData._2.value.getOrElse(committee_org_merchant_id_name, "null")
    department_master_id_name= NewSchoolToOldSchool.committeeToOldMasterId(master_id)  //将中台的关于教属的映射转到以前老的教属的映射

    val commite_name = filterData._3.value.getOrElse(committee_org_merchant_id_name, "null")
    department_slave_id_name = NewSchoolToOldSchool.committeeToOldSlaveId((master_id,commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id

    val reviewed = oldbean.reviewed
    if(StringUtils.isNoneEmpty(reviewed)){
      reviewed_name = reviewed
    }else{
      reviewed_name
    }


    (area_name, level_name, school_nature_name, license_main_type_name, stat_name, license_main_child_name, school_nature_sub_name,department_master_id_name,department_slave_id_name,reviewed_name)

  }



}
