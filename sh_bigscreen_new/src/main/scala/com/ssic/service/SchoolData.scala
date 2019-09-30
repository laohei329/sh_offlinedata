package com.ssic.service

import com.ssic.beans.SchoolBean
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

  //只计算上海的学校信息
  def SchoolInsert(filterData: (RDD[SchoolBean], String, Broadcast[Map[String, String]], Broadcast[Map[String, String]])) = {
    val schoolData = filterData._1.filter(x => x != null
      && x.database.equals("merchant")
      && x.table.equals("t_edu_school")
      && x.`type`.equals("insert")
      && x.data.stat.equals("1"))
      .filter(x => "8627".equals(x.data.seat_district_id)
      || "8637".equals(x.data.seat_district_id)
      || "8636".equals(x.data.seat_district_id)
      || "8638".equals(x.data.seat_district_id)
      || "8640".equals(x.data.seat_district_id)
      || "8639".equals(x.data.seat_district_id)
      || "8641".equals(x.data.seat_district_id)
      || "8642".equals(x.data.seat_district_id)
      || "8643".equals(x.data.seat_district_id)
      || "8630".equals(x.data.seat_district_id)
      || "8628".equals(x.data.seat_district_id)
      || "8629".equals(x.data.seat_district_id)
      || "8631".equals(x.data.seat_district_id)
      || "8633".equals(x.data.seat_district_id)
      || "8634".equals(x.data.seat_district_id)
      || "8635".equals(x.data.seat_district_id))
    val licenseData = filterData._1.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val school = schoolData.distinct().map({
      x =>

        val id = x.data.uuid
        val school_name = x.data.school_name
        val is_branch_school = x.data.is_branch_school //是否分校 0总校 1分校
      val parent_id = Rule.emptyToNull(x.data.parent_id)
        //x.data.parent_id //关联的总校
        val address = Rule.emptyToNull(x.data.address) //学校地址
      val social_credit_code = Rule.emptyToNull(x.data.social_credit_code) //统一社会信用代码
      val committee_org_merchant_id = x.data.committee_org_merchant_id //中台的字段，用来获取教属信息，需要转到以前老阳光午餐的逻辑
      val master_id = filterData._3.value.getOrElse(committee_org_merchant_id, "null")
        val department_master_id = NewSchoolToOldSchool.committeeToOldMasterId(master_id) //将中台的关于教属的映射转到以前老的教属的映射

        val commite_name = filterData._4.value.getOrElse(committee_org_merchant_id, "null")
        val department_slave_id = NewSchoolToOldSchool.committeeToOldSlaveId((master_id, commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id

        val students_amount = Rule.emptyToNull(x.data.students_amount) //学生人数
      val staff_amount = Rule.emptyToNull(x.data.staff_amount) //教职工人数
      val corporation = Rule.emptyToNull(x.data.corporation) //法人代表
      val corporation_way = Rule.emptyToNull(x.data.corporation_way) //法人代表手机
      val corporation_telephone = Rule.emptyToNull(x.data.corporation_telephone) //法人座机
      val department_head = Rule.emptyToNull(x.data.department_head) //部门负责人姓名
      val department_mobilephone = Rule.emptyToNull(x.data.department_mobilephone) //部门负责人手机
      val department_telephone = Rule.emptyToNull(x.data.department_telephone) //部门负责人座机
      val department_fax = Rule.emptyToNull(x.data.department_fax) //传真
      val department_email = Rule.emptyToNull(x.data.department_email) //电子邮件
      val food_safety_persion = Rule.emptyToNull(x.data.food_safety_persion) //项目负责人
      val food_safety_mobilephone = Rule.emptyToNull(x.data.food_safety_mobilephone) //项目负责人手机
      val food_safety_telephone = Rule.emptyToNull(x.data.food_safety_telephone) //项目负责人座机

        (id, List(SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._1, SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._2, SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._3, SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._4, SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._5, SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._6, SchoolRule.SchoolNew((x, department_master_id, department_slave_id))._7, "null", "null", "null", "null", "null", "null", "null", id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
    })

    val license = licenseData.distinct().filter(x => "0".equals(x.data.lic_type) || "1".equals(x.data.lic_type)).map({
      x =>
        val lic_type = x.data.lic_type
        val lic_pic = Rule.emptyToNull(x.data.lic_pic)
        val job_organization = Rule.emptyToNull(x.data.job_organization)
        val relation_id = Rule.emptyToNull(x.data.relation_id)
        val lic_no = Rule.emptyToNull(x.data.lic_no)
        val operation = Rule.emptyToNull(x.data.operation)
        val give_lic_date = Rule.emptyToNull(x.data.give_lic_date)
        val lic_end_date = Rule.emptyToNull(x.data.lic_end_date)
        val stat = x.data.stat
        (relation_id, List(lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat))
    }).filter(x => !x._1.equals("null"))

    school.leftOuterJoin(license).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).filter(x => !x._3.equals("null")).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if (x._3(0).equals("null")) {
              jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
            } else {
              if (jedis.hkeys("schoolDetail").contains(x._1).equals(true)) {
                val v = jedis.hget("schoolDetail", x._1)
                if ("0".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + v.split("slictype")(1).split("clictype")(0) + "clictype" + ";" + x._3(0) + ";" + "clicpic" + ";" + x._3(1) + ";" + "clicjob" + ";" + x._3(2) + ";" + "clicno" + ";" + x._3(3) + ";" + "coperation" + ";" + x._3(4) + ";" + "clicdate" + ";" + x._3(5) + ";" + "cenddate" + ";" + x._3(6))
                } else if ("1".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + ";" + x._3(0) + ";" + "slicpic" + ";" + x._3(1) + ";" + "slicjob" + ";" + x._3(2) + ";" + "slicno" + ";" + x._3(3) + ";" + "soperation" + ";" + x._3(4) + ";" + "slicdate" + ";" + x._3(5) + ";" + "senddate" + ";" + x._3(6) + ";" + "clictype" + v.split("clictype")(1))
                }
              } else {
                if ("0".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + ";" + x._3(0) + ";" + "clicpic" + ";" + x._3(1) + ";" + "clicjob" + ";" + x._3(2) + ";" + "clicno" + ";" + x._3(3) + ";" + "coperation" + ";" + x._3(4) + ";" + "clicdate" + ";" + x._3(5) + ";" + "cenddate" + ";" + x._3(6))
                } else if ("1".equals(x._3(0))) {
                  jedis.hset("schoolDetail", x._1, "id" + ";" + x._1 + ";" + "schoolname" + ";" + x._2(15) + ";" + "isbranchschool" + ";" + x._2(16) + ";" + "parentid" + ";" + x._2(17) + ";" + "area" + ";" + x._2(0) + ";" + "address" + ";" + x._2(18) + ";" + "socialcreditcode" + ";" + x._2(19) + ";" + "level" + ";" + x._2(1) + ";" + "schoolnature" + ";" + x._2(2) + ";" + "naturesub" + ";" + x._2(6) + ";" + "departmentmasterid" + ";" + x._2(20) + ";" + "departmentslaveid" + ";" + x._2(21) + ";" + "canteenmode" + ";" + x._2(3) + ";" + "ledgertype" + ";" + x._2(5) + ";" + "studentsamount" + ";" + x._2(22) + ";" + "staffamount" + ";" + x._2(23) + ";" + "corporation" + ";" + x._2(24) + ";" + "corporationway" + ";" + x._2(25) + ";" + "corporationtelephone" + ";" + x._2(26) + ";" + "departmenthead" + ";" + x._2(27) + ";" + "departmentmobilephone" + ";" + x._2(28) + ";" + "departmenttelephone" + ";" + x._2(29) + ";" + "departmentfax" + ";" + x._2(30) + ";" + "departmentemail" + ";" + x._2(31) + ";" + "foodsafetypersion" + ";" + x._2(32) + ";" + "foodsafetymobilephone" + ";" + x._2(33) + ";" + "foodsafetytelephone" + ";" + x._2(34) + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + ";" + x._3(0) + ";" + "slicpic" + ";" + x._3(1) + ";" + "slicjob" + ";" + x._3(2) + ";" + "slicno" + ";" + x._3(3) + ";" + "soperation" + ";" + x._3(4) + ";" + "slicdate" + ";" + x._3(5) + ";" + "senddate" + ";" + x._3(6) + ";" + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
                }
              }
            }


        })
    })
  }

  def School(filterData: (RDD[SchoolBean], String, Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, String]])) = {

    val schoolData = filterData._1.filter(x => x != null
      && x.database.equals("merchant")
      && x.table.equals("t_edu_school")).filter(x => "8627".equals(x.data.seat_district_id)
      || "8637".equals(x.data.seat_district_id)
      || "8636".equals(x.data.seat_district_id)
      || "8638".equals(x.data.seat_district_id)
      || "8640".equals(x.data.seat_district_id)
      || "8639".equals(x.data.seat_district_id)
      || "8641".equals(x.data.seat_district_id)
      || "8642".equals(x.data.seat_district_id)
      || "8643".equals(x.data.seat_district_id)
      || "8630".equals(x.data.seat_district_id)
      || "8628".equals(x.data.seat_district_id)
      || "8629".equals(x.data.seat_district_id)
      || "8631".equals(x.data.seat_district_id)
      || "8633".equals(x.data.seat_district_id)
      || "8634".equals(x.data.seat_district_id)
      || "8635".equals(x.data.seat_district_id))
    val schoolDataAll: RDD[(String, (String, String, String, String, String, String, String, String, String, String), (String, String, String, String, String, String, String, String, String, String), (String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String, String))] = schoolData.distinct().map({
      x =>
        val id = x.data.uuid
        val school_name = x.data.school_name
        val is_branch_school = x.data.is_branch_school //是否分校 0总校 1分校
      val parent_id = Rule.emptyToNull(x.data.parent_id) //关联的总校
      val address = Rule.emptyToNull(x.data.address) //学校地址
      val social_credit_code = Rule.emptyToNull(x.data.social_credit_code) //统一社会信用代码

        val committee_org_merchant_id = x.data.committee_org_merchant_id //中台的字段，用来获取教属信息，需要转到以前老阳光午餐的逻辑
      val master_id = filterData._4.value.getOrElse(committee_org_merchant_id, "null")
        val department_master_id = NewSchoolToOldSchool.committeeToOldMasterId(master_id) //将中台的关于教属的映射转到以前老的教属的映射

        val commite_name = filterData._5.value.getOrElse(committee_org_merchant_id, "null")
        val department_slave_id = NewSchoolToOldSchool.committeeToOldSlaveId((master_id, commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id
      val students_amount = Rule.emptyToNull(x.data.students_amount) //学生人数
      val staff_amount = Rule.emptyToNull(x.data.staff_amount) //教职工人数
      val corporation = Rule.emptyToNull(x.data.corporation) //法人代表
      val corporation_way = Rule.emptyToNull(x.data.corporation_way) //法人代表手机
      val corporation_telephone = x.data.corporation_telephone //法人座机
      val department_head = Rule.emptyToNull(x.data.department_head) //部门负责人姓名
      val department_mobilephone = Rule.emptyToNull(x.data.department_mobilephone) //部门负责人手机
      val department_telephone = Rule.emptyToNull(x.data.department_telephone) //部门负责人座机
      val department_fax = Rule.emptyToNull(x.data.department_fax) //传真
      val department_email = Rule.emptyToNull(x.data.department_email) //电子邮件
      val food_safety_persion = Rule.emptyToNull(x.data.food_safety_persion) //项目负责人
      val food_safety_mobilephone = Rule.emptyToNull(x.data.food_safety_mobilephone) //项目负责人手机
      val food_safety_telephone = Rule.emptyToNull(x.data.food_safety_telephone) //项目负责人座机


        if ("insert".equals(x.`type`) && "1".equals(x.data.stat) && "1".equals(x.data.reviewed)) {
          ("insert", SchoolRule.SchoolNew((x, department_master_id, department_slave_id)), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), (id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
        } else if ("delete".equals(x.`type`) && "1".equals(x.data.stat) && "1".equals(x.data.reviewed)) {
          ("delete", SchoolRule.SchoolNew((x, department_master_id, department_slave_id)), ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null"), (id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
        } else if ("update".equals(x.`type`)) {
          ("update", SchoolRule.SchoolNew((x, department_master_id, department_slave_id)), SchoolRule.SchoolOld((x, filterData._4, filterData._5)), (id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
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
              jedis.hincrBy("schoolData", "shanghai", 1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
              jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
              jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
              jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)

              if ("3".equals(x._2._8)) {
                jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
              } else {
                jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
              }

            } else if ("delete".equals(x._1)) {
              jedis.hincrBy("schoolData", "shanghai", -1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1, -1)
              jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
              jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
              jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, -1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
              jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)

              if ("3".equals(x._2._8)) {
                jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), -1)
              } else {
                jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, -1)
              }

              jedis.hdel("schoolDetail", x._4._1)

            } else if ("update".equals(x._1)) {
              logger.info("t_edu_school_update" + x._1 + "_" + x._2._1 + "_" + x._2._3 + "_" + x._2._4 + "_" + x._2._5 + "_" + x._2._10 + "_" + x._3._1 + "_" + x._3._2 + "_" + x._3._4 + "_" + x._3._5 + "_" + x._3._10)
              val v = jedis.hget("schoolDetail", x._4._1)
               if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
              if (StringUtils.isNoneEmpty(x._3._5) && !x._3._5.equals("null")) {
                if (x._3._5.equals("1") && x._2._5.equals("0") && x._2._10.equals("1")) {
                  jedis.hincrBy("schoolData", "shanghai", -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, -1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)

                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), -1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, -1)
                  }

                  // jedis.hdel("schoolDetail", x._4._1)

                } else if (x._3._5.equals("0") && x._2._5.equals("1") && x._2._10.equals("1")) {
                  jedis.hincrBy("schoolData", "shanghai", 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)

                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                  }
                  jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + v.split("slictype")(1))

                } else {
                  logger.info("stat发生变化的不符合标准-----------------" + x)
                }
              } else if (StringUtils.isNoneEmpty(x._3._10) && !x._3._10.equals("null")) {
                if ("0".equals(x._3._10) && "1".equals(x._2._10) && "1".equals(x._2._5)) {
                  jedis.hincrBy("schoolData", "shanghai", 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)

                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                  }
                } else if ("-1".equals(x._3._10) && "1".equals(x._2._10) && "1".equals(x._2._5)) {
                  jedis.hincrBy("schoolData", "shanghai", 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)

                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                  }
                } else if ("1".equals(x._3._10) && "2".equals(x._2._10) && "1".equals(x._2._5)) {
                  jedis.hincrBy("schoolData", "shanghai", -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, -1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)

                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), -1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, -1)
                  }
                } else if ("2".equals(x._3._10) && "1".equals(x._2._10) && "1".equals(x._2._5)) {
                  jedis.hincrBy("schoolData", "shanghai", 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1, 1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)

                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                  }
                }
              } else {
                jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + filterData._2 + ";" + "slictype" + v.split("slictype")(1))

                if (StringUtils.isNoneEmpty(x._3._2) && !x._3._2.equals("null") && x._2._5.equals("1") && x._2._10.equals("1")) {
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._3._2, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._2._2, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "level" + "_" + x._3._2, -1)

                } else if (StringUtils.isNoneEmpty(x._3._3) && StringUtils.isNoneEmpty(x._3._7) && !x._3._3.equals("null") && !x._3._7.equals("null") && x._2._5.equals("1") && x._2._10.equals("1")) {
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._3._7, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._3._7, -1)

                } else if (StringUtils.isNoneEmpty(x._3._3) && StringUtils.isEmpty(x._3._7) && !x._3._3.equals("null") && "null".equals(x._3._7) && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._2._7, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._2._7, -1)

                } else if (StringUtils.isEmpty(x._3._3) && StringUtils.isNoneEmpty(x._3._7) && "null".equals(x._3._3) && !x._3._7.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._3._7, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._3._7, -1)

                } else if (StringUtils.isNoneEmpty(x._3._4) && StringUtils.isNoneEmpty(x._3._6) && !x._3._4.equals("null") && !x._3._6.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._3._6, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._3._6, -1)

                } else if (StringUtils.isNoneEmpty(x._3._4) && StringUtils.isEmpty(x._3._6) && !x._3._4.equals("null") && "null".equals(x._3._6) && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._2._6, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._2._6, -1)

                } else if (StringUtils.isEmpty(x._3._4) && StringUtils.isNoneEmpty(x._3._6) && "null".equals(x._3._4) && !x._3._6.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._3._6, -1)

                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                  jedis.hincrBy("schoolData", "area" + "_" + x._2._1 + "_" + "canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._3._6, -1)

                } else if (StringUtils.isNoneEmpty(x._3._8) && StringUtils.isNoneEmpty(x._3._9) && !x._3._8.equals("null") && !x._3._9.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                  }

                  if ("3".equals(x._3._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._3._9, "null"), -1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._3._8 + "_slave" + "_" + x._3._9, -1)
                  }

                } else if (StringUtils.isEmpty(x._3._8) && StringUtils.isEmpty(x._3._9) && "null".equals(x._3._8) && x._3._9.equals("null") && "1".equals(x._2._5) && "1".equals(x._2._10)) {
                  if ("3".equals(x._2._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._2._9, "null"), 1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._2._8 + "_slave" + "_" + x._2._9, 1)
                  }

                  if ("3".equals(x._3._8)) {
                    jedis.hincrBy("schoolData", "masterid_" + "3" + "_slave" + "_" + filterData._3.value.getOrElse(x._3._9, "null"), -1)
                  } else {
                    jedis.hincrBy("schoolData", "masterid_" + x._3._8 + "_slave" + "_" + x._3._9, -1)
                  }
                } else {
                  logger.info("更新信息不符合的条件---------------" + x)
                }
              }
               }
            }
        })
    })

  }


  def schoolImage(filterData: RDD[SchoolBean]) = {

    val licenseData = filterData.filter(x => x != null && x.table.equals("t_pro_license"))
    val license = licenseData.distinct().filter(x => "0".equals(x.data.lic_type) || "1".equals(x.data.lic_type)).map({
      x =>
        val lic_type = x.data.lic_type
        val lic_pic = Rule.emptyToNull(x.data.lic_pic)
        val job_organization = Rule.emptyToNull(x.data.job_organization)
        val relation_id = Rule.emptyToNull(x.data.relation_id)
        val lic_no = Rule.emptyToNull(x.data.lic_no)
        val operation = Rule.emptyToNull(x.data.operation)
        val give_lic_date = Rule.emptyToNull(x.data.give_lic_date)
        val lic_end_date = Rule.emptyToNull(x.data.lic_end_date)
        val stat = x.data.stat
        if ("delete".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("delete", relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat, "null")
        } else if ("update".equals(x.`type`)) {
          ("update", relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat, x.old.stat)
        } else if ("insert".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("insert", relation_id, lic_type, lic_pic, job_organization, lic_no, operation, give_lic_date, lic_end_date, stat, "null")
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
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
                  if (StringUtils.isNoneEmpty(x._11) && !x._11.equals("null")) {
                    if ("1".equals(x._11) && "0".equals(x._10)) {
                      jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + "null" + ";" + "clicpic" + ";" + "null" + ";" + "clicjob" + ";" + "null" + ";" + "clicno" + ";" + "null" + ";" + "coperation" + ";" + "null" + ";" + "clicdate" + ";" + "null" + ";" + "cenddate" + ";" + "null")
                    } else if ("0".equals(x._11) && "1".equals(x._10)) {
                      jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + x._3 + ";" + "clicpic" + ";" + x._4 + ";" + "clicjob" + ";" + x._5 + ";" + "clicno" + ";" + x._6 + ";" + "coperation" + ";" + x._7 + ";" + "clicdate" + ";" + x._8 + ";" + "cenddate" + ";" + x._9)
                    }

                  } else {
                    jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + x._3 + ";" + "clicpic" + ";" + x._4 + ";" + "clicjob" + ";" + x._5 + ";" + "clicno" + ";" + x._6 + ";" + "coperation" + ";" + x._7 + ";" + "clicdate" + ";" + x._8 + ";" + "cenddate" + ";" + x._9)
                  }

                } else if ("insert".equals(x._1)) {
                  jedis.hset("schoolDetail", x._2, v.split("clictype")(0) + "clictype" + ";" + x._3 + ";" + "clicpic" + ";" + x._4 + ";" + "clicjob" + ";" + x._5 + ";" + "clicno" + ";" + x._6 + ";" + "coperation" + ";" + x._7 + ";" + "clicdate" + ";" + x._8 + ";" + "cenddate" + ";" + x._9)
                }
              } else if ("1".equals(x._3)) {
                if ("delete".equals(x._1)) {
                  jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + v.split("clictype")(1))
                } else if ("update".equals(x._1)) {
                  if (StringUtils.isNoneEmpty(x._11) && !x._11.equals("null")) {
                    if ("1".equals(x._11) && "0".equals(x._10)) {
                      jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + "null" + ";" + "slicpic" + ";" + "null" + ";" + "slicjob" + ";" + "null" + ";" + "slicno" + ";" + "null" + ";" + "soperation" + ";" + "null" + ";" + "slicdate" + ";" + "null" + ";" + "senddate" + ";" + "null" + ";" + "clictype" + v.split("clictype")(1))
                    } else if ("0".equals(x._11) && "1".equals(x._10)) {
                      jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + x._3 + ";" + "slicpic" + ";" + x._4 + ";" + "slicjob" + ";" + x._5 + ";" + "slicno" + ";" + x._6 + ";" + "soperation" + ";" + x._7 + ";" + "slicdate" + ";" + x._8 + ";" + "senddate" + ";" + x._9 + ";" + "clictype" + v.split("clictype")(1))
                    }

                  } else {
                    jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + x._3 + ";" + "slicpic" + ";" + x._4 + ";" + "slicjob" + ";" + x._5 + ";" + "slicno" + ";" + x._6 + ";" + "soperation" + ";" + x._7 + ";" + "slicdate" + ";" + x._8 + ";" + "senddate" + ";" + x._9 + ";" + "clictype" + v.split("clictype")(1))
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
