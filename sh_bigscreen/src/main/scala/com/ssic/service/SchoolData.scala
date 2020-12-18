package com.ssic.service

import com.alibaba.fastjson.JSON
import com.ssic.beans.{License, School, SchoolBean}
import com.ssic.report.BigScreenModel.logger
import com.ssic.utils.{JPools, NewSchoolToOldSchool, Rule, SchoolRule}
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
 * Created by 云 .
 * 学校数据指标
 * 3, "区级")        t_edu_committee表数据的主键id
 * (2, "市级")
 * 市教委——  jiaowei(7, "市教委"),
 * jingxinwei(6, "市经信委"),
 * shangwuwei(5, "市商务委"),
 * kewei(4, "市科委"),
 * jiaotongwei(3, "市交通委"),
 * nongwei(2, "市农委"),
 * shuiwuju(1, "市水务局（海洋局）"),
 * Other(0, "其他");
 * *
 * Other(0, "其他")   0 其他
 * *
 * Ministerial(1, "部级")
 * Education(1, "教育部"),
 * Other(0, "其他");
 *
 */

object SchoolData {
  private val logger = LoggerFactory.getLogger(this.getClass)


  /**
   *
   * 将学校基础信息存入redis中
   *
   * @param filterData          mysql的业务binlgog日志
   * @param data
   * @param school2commitee     主管部门映射
   * @param school2commiteename 主管部门映射的名字
   */

  def SchoolInsert(filterData: RDD[SchoolBean], data: String, school2commitee: Broadcast[Map[String, String]], school2commiteename: Broadcast[Map[String, String]]) = {
    val schoolData = filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("t_edu_school")
      && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data, classOf[School]))
      .filter(x => "8627".equals(x.seat_district_id)
        || "8637".equals(x.seat_district_id)
        || "8636".equals(x.seat_district_id)
        || "8638".equals(x.seat_district_id)
        || "8640".equals(x.seat_district_id)
        || "8639".equals(x.seat_district_id)
        || "8641".equals(x.seat_district_id)
        || "8642".equals(x.seat_district_id)
        || "8643".equals(x.seat_district_id)
        || "8630".equals(x.seat_district_id)
        || "8628".equals(x.seat_district_id)
        || "8629".equals(x.seat_district_id)
        || "8631".equals(x.seat_district_id)
        || "8633".equals(x.seat_district_id)
        || "8634".equals(x.seat_district_id)
        || "8635".equals(x.seat_district_id))
    val licenseData = filterData.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data, classOf[License]))
      .filter(x => !"1".equals(x.stat))

    val school = schoolData.distinct().map({
      x =>

        val id = x.uuid
        val school_name = x.school_name
        val is_branch_school = x.is_branch_school //是否分校 0总校 1分校
        val parent_id = Rule.emptyToNull(x.parent_id)
        //x.data.parent_id //关联的总校
        val address = Rule.emptyToNull(x.address) //学校地址
        val social_credit_code = Rule.emptyToNull(x.social_credit_code) //统一社会信用代码
        val committee_org_merchant_id = x.committee_org_merchant_id //中台的字段，用来获取教属信息，需要转到以前老阳光午餐的逻辑
        val master_id = school2commitee.value.getOrElse(committee_org_merchant_id, "null")
        val department_master_id = NewSchoolToOldSchool.committeeToOldMasterId(master_id) //将中台的关于教属的映射转到以前老的教属的映射

        val commite_name = school2commiteename.value.getOrElse(committee_org_merchant_id, "null")
        val department_slave_id = NewSchoolToOldSchool.committeeToOldSlaveId((master_id, commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id

        val students_amount = Rule.emptyToNull(x.students_amount) //学生人数
        val staff_amount = Rule.emptyToNull(x.staff_amount) //教职工人数
        val corporation = Rule.emptyToNull(x.corporation) //法人代表
        val corporation_way = Rule.emptyToNull(x.corporation_way) // 法人座机
        val corporation_telephone = Rule.emptyToNull(x.corporation_mobile) // 法人代表手机
        val department_head = Rule.emptyToNull(x.department_head) //部门负责人姓名
        val department_mobilephone = Rule.emptyToNull(x.department_mobilephone) //部门负责人手机
        val department_telephone = Rule.emptyToNull(x.department_telephone) //部门负责人座机
        val department_fax = Rule.emptyToNull(x.department_fax) //传真
        val department_email = Rule.emptyToNull(x.department_email) //电子邮件
        val food_safety_persion = Rule.emptyToNull(x.food_safety_persion) //项目负责人
        val food_safety_mobilephone = Rule.emptyToNull(x.food_safety_mobilephone) //项目负责人手机
        val food_safety_telephone = Rule.emptyToNull(x.food_safety_telephone) //项目负责人座机

        // 将上海市各区的新的district_id映射 => 旧的area的映射
        val area = NewSchoolToOldSchool.committeeToOldArea(x.seat_district_id)
        val level = x.level
        val level2 = x.level2
        val school_nature = x.school_nature
        val school_nature_sub = x.school_nature_sub

        (id, List(area, SchoolRule.LevelName(level, level2), SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._1, SchoolRule.Cantoon(x.license_main_type, x.license_main_child)._1, x.stat, SchoolRule.Cantoon(x.license_main_type, x.license_main_child)._2, SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._2, "null", "null", "null", "null", "null", "null", "null", id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
    })

    val license = licenseData.distinct().filter(x => "0".equals(x.lic_type) || "1".equals(x.lic_type)).map({
      x =>
        val lic_type = x.lic_type
        val lic_pic = Rule.emptyToNull(x.lic_pic)
        val job_organization = Rule.emptyToNull(x.job_organization)
        val relation_id = Rule.emptyToNull(x.relation_id)
        val lic_no = Rule.emptyToNull(x.lic_no)
        val operation = Rule.emptyToNull(x.operation)
        val give_lic_date = Rule.emptyToNull(x.give_lic_date)
        val lic_end_date = Rule.emptyToNull(x.lic_end_date)
        val stat = x.stat
        (relation_id, List(lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat))
    }).filter(x => !x._1.equals("null"))

    school.leftOuterJoin(license).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>

            if (("null").equals(x._3(0))) {
              jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + data + ";" + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
            } else {
              if (jedis.hkeys("schoolDetail").contains(x._1).equals(true)) {
                val v = jedis.hget("schoolDetail", x._1)
                if ("0".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + data + ";" + "slictype" + v.split("slictype")(1).split("clictype")(0) + "clictype" + ";" + x._3(0) + ";" + "clicpic" + ";" + x._3(1) + ";" + "clicjob" + ";" + x._3(2) + ";" + "clicno" + ";" + x._3(3) + ";" + "coperation" + ";" + x._3(4) + ";" + "clicdate" + ";" + x._3(5) + ";" + "cenddate" + ";" + x._3(6))
                } else if ("1".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + data + ";" + "slictype" + ";" + x._3(0) + ";" + "slicpic" + ";" + x._3(1) + ";" + "slicjob" + ";" + x._3(2) + ";" + "slicno" + ";" + x._3(3) + ";" + "soperation" + ";" + x._3(4) + ";" + "slicdate" + ";" + x._3(5) + ";" + "senddate" + ";" + x._3(6) + ";" + "clictype" + v.split("clictype")(1))
                }
              } else {
                if ("0".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + data + ";" + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + ";" + x._3(0) + ";" + "clicpic" + ";" + x._3(1) + ";" + "clicjob" + ";" + x._3(2) + ";" + "clicno" + ";" + x._3(3) + ";" + "coperation" + ";" + x._3(4) + ";" + "clicdate" + ";" + x._3(5) + ";" + "cenddate" + ";" + x._3(6))
                } else if ("1".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + data + ";" + "slictype" + ";" + x._3(0) + ";" + "slicpic" + ";" + x._3(1) + ";" + "slicjob" + ";" + x._3(2) + ";" + "slicno" + ";" + x._3(3) + ";" + "soperation" + ";" + x._3(4) + ";" + "slicdate" + ";" + x._3(5) + ";" + "senddate" + ";" + x._3(6) + ";" + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
                }
              }
            }


        })
    })
  }

  /**
   *
   * 将学校基础信息的更新，删除 存入redis中
   *
   * @param filterData          mysql的业务binlgog日志
   * @param data
   * @param commiteeid2commiteename
   * @param school2commitee     主管部门映射
   * @param school2commiteename 主管部门映射的名字
   */

  def School(filterData: RDD[SchoolBean], data: String, commiteeid2commiteename: Broadcast[Map[String, String]], school2commitee: Broadcast[Map[String, String]], school2commiteename: Broadcast[Map[String, String]]) = {

    val schoolData = filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("t_edu_school"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[School])))
      .filter(x => "8627".equals(x._2.seat_district_id)
        || "8637".equals(x._2.seat_district_id)
        || "8636".equals(x._2.seat_district_id)
        || "8638".equals(x._2.seat_district_id)
        || "8640".equals(x._2.seat_district_id)
        || "8639".equals(x._2.seat_district_id)
        || "8641".equals(x._2.seat_district_id)
        || "8642".equals(x._2.seat_district_id)
        || "8643".equals(x._2.seat_district_id)
        || "8630".equals(x._2.seat_district_id)
        || "8628".equals(x._2.seat_district_id)
        || "8629".equals(x._2.seat_district_id)
        || "8631".equals(x._2.seat_district_id)
        || "8633".equals(x._2.seat_district_id)
        || "8634".equals(x._2.seat_district_id)
        || "8635".equals(x._2.seat_district_id))
    val schoolDataAll: RDD[(String, (String, String, String, String, String, String, String, String, String, String), (String, String, String, String, String, String, String, String, String, String), (String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String))] = schoolData.distinct().map({
      case (k, v) =>
        val id = v.uuid
        val school_name = v.school_name
        val is_branch_school = v.is_branch_school //是否分校 0总校 1分校
        val parent_id = Rule.emptyToNull(v.parent_id) //关联的总校
        val address = Rule.emptyToNull(v.address) //学校地址
        val social_credit_code = Rule.emptyToNull(v.social_credit_code) //统一社会信用代码

        val committee_org_merchant_id = v.committee_org_merchant_id //中台的字段，用来获取教属信息，需要转到以前老阳光午餐的逻辑
        val master_id = school2commitee.value.getOrElse(committee_org_merchant_id, "null")
        val department_master_id = NewSchoolToOldSchool.committeeToOldMasterId(master_id) //将中台的关于教属的映射转到以前老的教属的映射

        val commite_name = school2commiteename.value.getOrElse(committee_org_merchant_id, "null")
        val department_slave_id = NewSchoolToOldSchool.committeeToOldSlaveId((master_id, commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id
        val students_amount = Rule.emptyToNull(v.students_amount) //学生人数
        val staff_amount = Rule.emptyToNull(v.staff_amount) //教职工人数
        val corporation = Rule.emptyToNull(v.corporation) //法人代表
        val corporation_way = Rule.emptyToNull(v.corporation_way) //法人代表手机
        val corporation_telephone = v.corporation_telephone //法人座机
        val department_head = Rule.emptyToNull(v.department_head) //部门负责人姓名
        val department_mobilephone = Rule.emptyToNull(v.department_mobilephone) //部门负责人手机
        val department_telephone = Rule.emptyToNull(v.department_telephone) //部门负责人座机
        val department_fax = Rule.emptyToNull(v.department_fax) //传真
        val department_email = Rule.emptyToNull(v.department_email) //电子邮件
        val food_safety_persion = Rule.emptyToNull(v.food_safety_persion) //项目负责人
        val food_safety_mobilephone = Rule.emptyToNull(v.food_safety_mobilephone) //项目负责人手机
        val food_safety_telephone = Rule.emptyToNull(v.food_safety_telephone) //项目负责人座机

        // 将上海市各区的新的district_id映射 => 旧的area的映射
        val area = NewSchoolToOldSchool.committeeToOldArea(v.seat_district_id)

        val level = v.level
        val level2 = v.level2
        val school_nature = v.school_nature
        val school_nature_sub = v.school_nature_sub

        if ("insert".equals(k) && "1".equals(v.stat) && "1".equals(v.reviewed)) {
          ("insert", (area, SchoolRule.LevelName(level, level2), SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._1, SchoolRule.Cantoon(v.license_main_type, v.license_main_child)._1, v.stat, SchoolRule.Cantoon(v.license_main_type, v.license_main_child)._2, SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._2, department_master_id, department_slave_id, v.reviewed), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), (id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
        } else if ("delete".equals(k) && "1".equals(v.stat) && "1".equals(v.reviewed)) {
          ("delete", (area, SchoolRule.LevelName(level, level2), SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._1, SchoolRule.Cantoon(v.license_main_type, v.license_main_child)._1, v.stat, SchoolRule.Cantoon(v.license_main_type, v.license_main_child)._2, SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._2, department_master_id, department_slave_id, v.reviewed), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), (id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
        } else if ("update".equals(k)) {
          ("update", (area, SchoolRule.LevelName(level, level2), SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._1, SchoolRule.Cantoon(v.license_main_type, v.license_main_child)._1, v.stat, SchoolRule.Cantoon(v.license_main_type, v.license_main_child)._2, SchoolRule.SchoolNatrue(school_nature, school_nature_sub)._2, department_master_id, department_slave_id, v.reviewed), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), (id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
        } else {
          ("null", ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"))
        }
    })

    schoolDataAll.filter(x => !x._1.equals("null")).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1)) {
              //              jedis.hincrBy("schoolData", "shanghai", 1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
              //              jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
              //              jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
              //              jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
              //
              //              if ("3".equals(x._2._8)) {
              //                jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
              //              } else {
              //                jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
              //              }

            } else if ("delete".equals(x._1)) {
              //              jedis.hincrBy("schoolData", "shanghai", -1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1, -1)
              //              jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
              //              jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
              //              jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, -1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
              //              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
              //
              //              if ("3".equals(x._2._8)) {
              //                jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), -1)
              //              } else {
              //                jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, -1)
              //              }

              jedis.hdel("schoolDetail", x._4._1)

            } else if ("update".equals(x._1)) {
              logger.info("t_edu_school_update" + x._1 + "_" + x._2._1 + "_" + x._2._3 + "_" + x._2._4 + "_" + x._2._5 + "_" + x._2._10 + "_" + x._3._1 + "_" + x._3._2 + "_" + x._3._4 + "_" + x._3._5 + "_" + x._3._10)
              val v = jedis.hget("schoolDetail", x._4._1)
              if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
                jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + data + ";" + "slictype" + v.split("slictype")(1))

                //              if (StringUtils.isNoneEmpty(x._3._5) && !x._3._5.equals("null")) {
                //                if (x._3._5.equals("1") && x._2._5.equals("0") && x._2._10.equals("1")) {
                //                  jedis.hincrBy("schoolData", "shanghai", -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, -1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                //
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), -1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, -1)
                //                  }

                // jedis.hdel("schoolDetail", x._4._1)

                //                } else if (x._3._5.equals("0") && x._2._5.equals("1") && x._2._10.equals("1")) {
                //                  jedis.hincrBy("schoolData", "shanghai", 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                //                  }
                //                  jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + v.split("slictype")(1))
                //
                //                } else {
                //                  logger.info("stat发生变化的不符合标准-----------------" + x)
                //                }
                //              } else if (StringUtils.isNoneEmpty(x._3._10) && !x._3._10.equals("null")) {
                //                if ("0".equals(x._3._10) && "1".equals(x._2._10) && "1".equals(x._2._5)) {
                //                  jedis.hincrBy("schoolData", "shanghai", 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                //                  }
                //                } else if ("-1".equals(x._3._10) && "1".equals(x._2._10) && "1".equals(x._2._5)) {
                //                  jedis.hincrBy("schoolData", "shanghai", 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                //                  }
                //               } else if ("1".equals(x._3._10) && "2".equals(x._2._10) && "1".equals(x._2._5)) {
                //                  jedis.hincrBy("schoolData", "shanghai", -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, -1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                //
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), -1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, -1)
                //                  }
                //               } else if ("2".equals(x._3._10) && "1".equals(x._2._10) && "1".equals(x._2._5)) {
                //                  jedis.hincrBy("schoolData", "shanghai", 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                //                  }
                //                }
                //              } else {
                //                jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + v.split("slictype")(1))

                //                if (StringUtils.isNoneEmpty(x._3._2) && !x._3._2.equals("null") && x._2._5.equals("1") && x._2._10.equals("1")) {
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._3._2, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._3._2, -1)
                //
                //                } else if (StringUtils.isNoneEmpty(x._3._3) && StringUtils.isNoneEmpty(x._3._7) && !x._3._3.equals("null") && !x._3._7.equals("null") && x._2._5.equals("1") && x._2._10.equals("1")) {
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._3._7, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._3._7, -1)
                //
                //                } else if (StringUtils.isNoneEmpty(x._3._3) && StringUtils.isEmpty(x._3._7) && !x._3._3.equals("null") && "null".equals(x._3._7) && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                //
                //                } else if (StringUtils.isEmpty(x._3._3) && StringUtils.isNoneEmpty(x._3._7) && "null".equals(x._3._3) && !x._3._7.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._3._7, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._3._7, -1)
                //
                //                } else if (StringUtils.isNoneEmpty(x._3._4) && StringUtils.isNoneEmpty(x._3._6) && !x._3._4.equals("null") && !x._3._6.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._3._6, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._3._6, -1)
                //
                //                } else if (StringUtils.isNoneEmpty(x._3._4) && StringUtils.isEmpty(x._3._6) && !x._3._4.equals("null") && "null".equals(x._3._6) && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                //
                //                } else if (StringUtils.isEmpty(x._3._4) && StringUtils.isNoneEmpty(x._3._6) && "null".equals(x._3._4) && !x._3._6.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._3._6, -1)
                //
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                //                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._3._6, -1)
                //
                //                } else if (StringUtils.isNoneEmpty(x._3._8) && StringUtils.isNoneEmpty(x._3._9) && !x._3._8.equals("null") && !x._3._9.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                //                  }
                //
                //                  if ("3".equals(x._3._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._3._9, "null"), -1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._3._8 + "_slave" + "_" + x._3._9, -1)
                //                  }
                //
                //                } else if (StringUtils.isEmpty(x._3._8) && StringUtils.isEmpty(x._3._9) && "null".equals(x._3._8) && x._3._9.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                //                  if ("3".equals(x._2._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                //                  }
                //
                //                  if ("3".equals(x._3._8)) {
                //                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._3._9, "null"), -1)
                //                  } else {
                //                    jedis.hincrBy("schoolData", "masterid_" + x._3._8 + "_slave" + "_" + x._3._9, -1)
                //                  }
                //                } else {
                //                  logger.info("更新信息不符合的条件---------------" + x)
                //                }
                //             }
              } else {

                jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + data + ";" + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
              }
            }
        })
    })

  }


  /**
   * 将学校证件信息存入redis中的学校基础信息表中
   *
   * @param filterData [SchoolBean]  mysql的业务binlgog日志
   */

  def schoolImage(filterData: RDD[SchoolBean]) = {

    val licenseData = filterData.filter(x => x != null && x.table.equals("t_pro_license")).map(x => (x.`type`, JSON.parseObject(x.data, classOf[License])))
    val license = licenseData.distinct().filter(x => "0".equals(x._2.lic_type) || "1".equals(x._2.lic_type)).map({
      case (k, v) =>
        val lic_type = v.lic_type
        val lic_pic = Rule.emptyToNull(v.lic_pic)
        val job_organization = Rule.emptyToNull(v.job_organization)
        val relation_id = Rule.emptyToNull(v.relation_id)
        val lic_no = Rule.emptyToNull(v.lic_no)
        val operation = Rule.emptyToNull(v.operation)
        val give_lic_date = Rule.emptyToNull(v.give_lic_date)
        val lic_end_date = Rule.emptyToNull(v.lic_end_date)
        val stat = v.stat
        if ("delete".equals(k) && "1".equals(v.stat)) {
          ("delete", relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat)
        } else if ("update".equals(k)) {
          ("update", relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat)
        } else if ("insert".equals(k) && "1".equals(v.stat)) {
          ("insert", relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat)
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
        }
    }).filter(x => !x._1.equals("null")).filter(x => !x._2.equals("null"))

    license.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hget("schoolDetail", x._2)
            if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
              if ("0".equals(x._3)) {
                if ("delete".equals(x._1)) {
                  jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
                } else if ("update".equals(x._1)) {
                  if ("1".equals(x._10)) {
                    jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + x._3 + ";" + "clicpic" + ";" + x._4 + ";" + "clicjob" + ";" + x._5 + ";" + "clicno" + ";" + x._6 + ";" + "coperation" + ";" + x._7 + ";" + "clicdate" + ";" + x._8 + ";" + "cenddate" + ";" + x._9)
                  } else {
                    jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
                  }

                } else if ("insert".equals(x._1)) {
                  jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + x._3 + ";" + "clicpic" + ";" + x._4 + ";" + "clicjob" + ";" + x._5 + ";" + "clicno" + ";" + x._6 + ";" + "coperation" + ";" + x._7 + ";" + "clicdate" + ";" + x._8 + ";" + "cenddate" + ";" + x._9)
                }
              } else if ("1".equals(x._3)) {
                if ("delete".equals(x._1)) {
                  jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + v.split("clictype")(1))
                } else if ("update".equals(x._1)) {

                  if ("1".equals(x._10)) {
                    jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + x._3 + ";" + "slicpic" + ";" + x._4 + ";" + "slicjob" + ";" + x._5 + ";" + "slicno" + ";" + x._6 + ";" + "soperation" + ";" + x._7 + ";" + "slicdate" + ";" + x._8 + ";" + "senddate" + ";" + x._9 + ";" + "clictype" + v.split("clictype")(1))
                  } else {
                    jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + v.split("clictype")(1))
                  }

                } else if ("insert".equals(x._1)) {
                  jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + x._3 + ";" + "slicpic" + ";" + x._4 + ";" + "slicjob" + ";" + x._5 + ";" + "slicno" + ";" + x._6 + ";" + "soperation" + ";" + x._7 + ";" + "slicdate" + ";" + x._8 + ";" + "senddate" + ";" + x._9 + ";" + "clictype" + v.split("clictype")(1))
                }
              }
            }
        })
    })

  }

}
