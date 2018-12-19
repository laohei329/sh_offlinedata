package com.ssic.report

import com.ssic.beans.SchoolBean
import com.ssic.utils.{JPools, SchoolRule}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  * Created by 云 .
  * 学校数据指标
  * 3, "区级")        t_edu_committee表数据的主键id
  * (2, "市级")
  *市教委——  jiaowei(7, "市教委"),
  jingxinwei(6, "市经信委"),
  shangwuwei(5, "市商务委"),
  kewei(4, "市科委"),
  jiaotongwei(3, "市交通委"),
  nongwei(2, "市农委"),
  shuiwuju(1, "市水务局（海洋局）"),
  Other(0, "其他");

  Other(0, "其他")   0 其他
  *
  */

object SchoolData {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def SchoolInsert(filterData:(RDD[SchoolBean],Broadcast[Map[String, Int]])) = {
    val schoolData = filterData._1.filter(x => x != null && x.table.equals("t_edu_school") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val licenseData = filterData._1.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val school = schoolData.distinct().map({
      x =>
        val id = x.data.id
        val school_name = x.data.school_name
        val is_branch_school = x.data.is_branch_school //是否分校 0总校 1分校
      val parent_id = x.data.parent_id //关联的总校
      val address = x.data.address //学校地址
      val social_credit_code = x.data.social_credit_code //统一社会信用代码
      val department_master_id = x.data.department_master_id //所属部门  District(3, "区级"), Municipal(2, "市级"), Ministerial(1, "部级"), Other(0, "其他");
      val department_slave_id = x.data.department_slave_id //主管部门
      val students_amount = x.data.students_amount //学生人数
      val staff_amount = x.data.staff_amount //教职工人数
      val corporation = x.data.corporation //法人代表
      val corporation_way = x.data.corporation_way //法人代表手机
      val corporation_telephone = x.data.corporation_telephone //法人座机
      val department_head = x.data.department_head //部门负责人姓名
      val department_mobilephone = x.data.department_mobilephone //部门负责人手机
      val department_telephone = x.data.department_telephone //部门负责人座机
      val department_fax = x.data.department_fax //传真
      val department_email = x.data.department_email //电子邮件
      val food_safety_persion = x.data.food_safety_persion //项目负责人
      val food_safety_mobilephone = x.data.food_safety_mobilephone //项目负责人手机
      val food_safety_telephone = x.data.food_safety_telephone //项目负责人座机
        (id, List(SchoolRule.SchoolNew(x)._1,SchoolRule.SchoolNew(x)._2,SchoolRule.SchoolNew(x)._3,SchoolRule.SchoolNew(x)._4,SchoolRule.SchoolNew(x)._5,SchoolRule.SchoolNew(x)._6,SchoolRule.SchoolNew(x)._7, "null", "null", "null", "null", "null", "null", "null", id, school_name, is_branch_school, parent_id, address, social_credit_code, department_master_id, department_slave_id, students_amount, staff_amount, corporation, corporation_way, corporation_telephone, department_head, department_mobilephone, department_telephone, department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone))
    })

    val license = licenseData.distinct().filter(x => "0".equals(x.data.lic_type) || "1".equals(x.data.lic_type)).map({
      x =>
        val lic_type = x.data.lic_type
        var lic_pic_name="null"
        val lic_pic = x.data.lic_pic
        if(StringUtils.isNoneEmpty(lic_pic) && !lic_pic.equals("null")){
          lic_pic_name=lic_pic
        }else{
          lic_pic_name
        }

        var job_organization_name="null"
        val job_organization = x.data.job_organization
        if(StringUtils.isNoneEmpty(job_organization) && !job_organization.equals("null")){
          job_organization_name=job_organization
        }else{
          job_organization_name
        }
        val relation_id = x.data.relation_id

        var lic_no_name="null"
        val lic_no = x.data.lic_no
        if(StringUtils.isNoneEmpty(lic_no) && !lic_no.equals("null")){
          lic_no_name=lic_no
        }else{
          lic_no_name
        }

        var operation_name="null"
        val operation = x.data.operation
        if(StringUtils.isNoneEmpty(operation) && !operation.equals("null")){
          operation_name=operation
        }else{
          operation_name
        }

        var give_lic_date_name="null"
        val give_lic_date = x.data.give_lic_date
        if(StringUtils.isNoneEmpty(give_lic_date) && !give_lic_date.equals("null")){
          give_lic_date_name=give_lic_date
        }else{
          give_lic_date_name
        }

        var lic_end_date_name="null"
        val lic_end_date = x.data.lic_end_date
        if(StringUtils.isNoneEmpty(lic_end_date) && !lic_end_date.equals("null")){
          lic_end_date_name=lic_end_date
        }else{
          lic_end_date_name
        }

        val stat = x.data.stat
        (relation_id, List(lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name, stat))
    })

    school.leftOuterJoin(license).map(x => (x._1,x._2._1,x._2._2.getOrElse(List("null")))).filter(x=> !x._3.equals("null")).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if(x._3(0).equals("null")){
              jedis.hset("schoolDetail",x._1,"id"+";"+x._1+";"+"schoolname"+";"+x._2(15)+";"+"isbranchschool"+";"+x._2(16)+";"+"parentid"+";"+x._2(17)+";"+"area"+";"+x._2(0)+";"+"address"+";"+x._2(18)+";"+"socialcreditcode"+";"+x._2(19)+";"+"level"+";"+x._2(1)+";"+"schoolnature"+";"+x._2(2)+";"+"naturesub"+";"+x._2(6)+";"+"departmentmasterid"+";"+x._2(20)+";"+"departmentslaveid"+";"+x._2(21)+";"+"canteenmode"+";"+x._2(3)+";"+"ledgertype"+";"+x._2(5)+";"+"studentsamount"+";"+x._2(22)+";"+"staffamount"+";"+x._2(23)+";"+"corporation"+";"+x._2(24)+";"+"corporationway"+";"+x._2(25)+";"+"corporationtelephone"+";"+x._2(26)+";"+"departmenthead"+";"+x._2(27)+";"+"departmentmobilephone"+";"+x._2(28)+";"+"departmenttelephone"+";"+x._2(29)+";"+"departmentfax"+";"+x._2(30)+";"+"departmentemail"+";"+x._2(31)+";"+"foodsafetypersion"+";"+x._2(32)+";"+"foodsafetymobilephone"+";"+x._2(33)+";"+"foodsafetytelephone"+";"+x._2(34)+";"+"gongcan"+";"+filterData._2.value.getOrElse(x._1,0)+";"+"slictype"+";"+"null"+";"+"slicpic"+";"+"null"+";"+"slicjob"+";"+"null"+";"+"slicno"+";"+"null"+";"+"soperation"+";"+"null"+";"+"slicdate"+";"+"null"+";"+"senddate"+";"+"null"+";"+"clictype"+";"+"null"+";"+"clicpic"+";"+"null"+";"+"clicjob"+";"+"null"+";"+"clicno"+";"+"null"+";"+"coperation"+";"+"null"+";"+"clicdate"+";"+"null"+";"+"cenddate"+";"+"null")
            }else{
              if(jedis.hkeys("schoolDetail").contains(x._1).equals(true)){
                val v = jedis.hget("schoolDetail",x._1)
                if("0".equals(x._3(0))){
                  jedis.hset("schoolDetail",x._1,"id"+";"+x._1+";"+"schoolname"+";"+x._2(15)+";"+"isbranchschool"+";"+x._2(16)+";"+"parentid"+";"+x._2(17)+";"+"area"+";"+x._2(0)+";"+"address"+";"+x._2(18)+";"+"socialcreditcode"+";"+x._2(19)+";"+"level"+";"+x._2(1)+";"+"schoolnature"+";"+x._2(2)+";"+"naturesub"+";"+x._2(6)+";"+"departmentmasterid"+";"+x._2(20)+";"+"departmentslaveid"+";"+x._2(21)+";"+"canteenmode"+";"+x._2(3)+";"+"ledgertype"+";"+x._2(5)+";"+"studentsamount"+";"+x._2(22)+";"+"staffamount"+";"+x._2(23)+";"+"corporation"+";"+x._2(24)+";"+"corporationway"+";"+x._2(25)+";"+"corporationtelephone"+";"+x._2(26)+";"+"departmenthead"+";"+x._2(27)+";"+"departmentmobilephone"+";"+x._2(28)+";"+"departmenttelephone"+";"+x._2(29)+";"+"departmentfax"+";"+x._2(30)+";"+"departmentemail"+";"+x._2(31)+";"+"foodsafetypersion"+";"+x._2(32)+";"+"foodsafetymobilephone"+";"+x._2(33)+";"+"foodsafetytelephone"+";"+x._2(34)+";"+"gongcan"+";"+filterData._2.value.getOrElse(x._1,0)+";"+"slictype"+v.split("slictype")(1).split("clictype")(0)+"clictype"+";"+x._3(0)+";"+"clicpic"+";"+x._3(1)+";"+"clicjob"+";"+x._3(2)+";"+"clicno"+";"+x._3(3)+";"+"coperation"+";"+x._3(4)+";"+"clicdate"+";"+x._3(5)+";"+"cenddate"+";"+x._3(6))
                }else if("1".equals(x._3(0))){
                  jedis.hset("schoolDetail",x._1,"id"+";"+x._1+";"+"schoolname"+";"+x._2(15)+";"+"isbranchschool"+";"+x._2(16)+";"+"parentid"+";"+x._2(17)+";"+"area"+";"+x._2(0)+";"+"address"+";"+x._2(18)+";"+"socialcreditcode"+";"+x._2(19)+";"+"level"+";"+x._2(1)+";"+"schoolnature"+";"+x._2(2)+";"+"naturesub"+";"+x._2(6)+";"+"departmentmasterid"+";"+x._2(20)+";"+"departmentslaveid"+";"+x._2(21)+";"+"canteenmode"+";"+x._2(3)+";"+"ledgertype"+";"+x._2(5)+";"+"studentsamount"+";"+x._2(22)+";"+"staffamount"+";"+x._2(23)+";"+"corporation"+";"+x._2(24)+";"+"corporationway"+";"+x._2(25)+";"+"corporationtelephone"+";"+x._2(26)+";"+"departmenthead"+";"+x._2(27)+";"+"departmentmobilephone"+";"+x._2(28)+";"+"departmenttelephone"+";"+x._2(29)+";"+"departmentfax"+";"+x._2(30)+";"+"departmentemail"+";"+x._2(31)+";"+"foodsafetypersion"+";"+x._2(32)+";"+"foodsafetymobilephone"+";"+x._2(33)+";"+"foodsafetytelephone"+";"+x._2(34)+";"+"gongcan"+";"+filterData._2.value.getOrElse(x._1,0)+";"+"slictype"+";"+x._3(0)+";"+"slicpic"+";"+x._3(1)+";"+"slicjob"+";"+x._3(2)+";"+"slicno"+";"+x._3(3)+";"+"soperation"+";"+x._3(4)+";"+"slicdate"+";"+x._3(5)+";"+"senddate"+";"+x._3(6)+";"+"clictype"+v.split("clictype")(1))
                }
              }else{
                if("0".equals(x._3(0))){
                  jedis.hset("schoolDetail",x._1,"id"+";"+x._1+";"+"schoolname"+";"+x._2(15)+";"+"isbranchschool"+";"+x._2(16)+";"+"parentid"+";"+x._2(17)+";"+"area"+";"+x._2(0)+";"+"address"+";"+x._2(18)+";"+"socialcreditcode"+";"+x._2(19)+";"+"level"+";"+x._2(1)+";"+"schoolnature"+";"+x._2(2)+";"+"naturesub"+";"+x._2(6)+";"+"departmentmasterid"+";"+x._2(20)+";"+"departmentslaveid"+";"+x._2(21)+";"+"canteenmode"+";"+x._2(3)+";"+"ledgertype"+";"+x._2(5)+";"+"studentsamount"+";"+x._2(22)+";"+"staffamount"+";"+x._2(23)+";"+"corporation"+";"+x._2(24)+";"+"corporationway"+";"+x._2(25)+";"+"corporationtelephone"+";"+x._2(26)+";"+"departmenthead"+";"+x._2(27)+";"+"departmentmobilephone"+";"+x._2(28)+";"+"departmenttelephone"+";"+x._2(29)+";"+"departmentfax"+";"+x._2(30)+";"+"departmentemail"+";"+x._2(31)+";"+"foodsafetypersion"+";"+x._2(32)+";"+"foodsafetymobilephone"+";"+x._2(33)+";"+"foodsafetytelephone"+";"+x._2(34)+";"+"gongcan"+";"+filterData._2.value.getOrElse(x._1,0)+";"+"slictype"+";"+"null"+";"+"slicpic"+";"+"null"+";"+"slicjob"+";"+"null"+";"+"slicno"+";"+"null"+";"+"soperation"+";"+"null"+";"+"slicdate"+";"+"null"+";"+"senddate"+";"+"null"+";"+"clictype"+";"+x._3(0)+";"+"clicpic"+";"+x._3(1)+";"+"clicjob"+";"+x._3(2)+";"+"clicno"+";"+x._3(3)+";"+"coperation"+";"+x._3(4)+";"+"clicdate"+";"+x._3(5)+";"+"cenddate"+";"+x._3(6))
                }else if("1".equals(x._3(0))){
                  jedis.hset("schoolDetail",x._1,"id"+";"+x._1+";"+"schoolname"+";"+x._2(15)+";"+"isbranchschool"+";"+x._2(16)+";"+"parentid"+";"+x._2(17)+";"+"area"+";"+x._2(0)+";"+"address"+";"+x._2(18)+";"+"socialcreditcode"+";"+x._2(19)+";"+"level"+";"+x._2(1)+";"+"schoolnature"+";"+x._2(2)+";"+"naturesub"+";"+x._2(6)+";"+"departmentmasterid"+";"+x._2(20)+";"+"departmentslaveid"+";"+x._2(21)+";"+"canteenmode"+";"+x._2(3)+";"+"ledgertype"+";"+x._2(5)+";"+"studentsamount"+";"+x._2(22)+";"+"staffamount"+";"+x._2(23)+";"+"corporation"+";"+x._2(24)+";"+"corporationway"+";"+x._2(25)+";"+"corporationtelephone"+";"+x._2(26)+";"+"departmenthead"+";"+x._2(27)+";"+"departmentmobilephone"+";"+x._2(28)+";"+"departmenttelephone"+";"+x._2(29)+";"+"departmentfax"+";"+x._2(30)+";"+"departmentemail"+";"+x._2(31)+";"+"foodsafetypersion"+";"+x._2(32)+";"+"foodsafetymobilephone"+";"+x._2(33)+";"+"foodsafetytelephone"+";"+x._2(34)+";"+"gongcan"+";"+filterData._2.value.getOrElse(x._1,0)+";"+"slictype"+";"+x._3(0)+";"+"slicpic"+";"+x._3(1)+";"+"slicjob"+";"+x._3(2)+";"+"slicno"+";"+x._3(3)+";"+"soperation"+";"+x._3(4)+";"+"slicdate"+";"+x._3(5)+";"+"senddate"+";"+x._3(6)+";"+"clictype"+";"+"null"+";"+"clicpic"+";"+"null"+";"+"clicjob"+";"+"null"+";"+"clicno"+";"+"null"+";"+"coperation"+";"+"null"+";"+"clicdate"+";"+"null"+";"+"cenddate"+";"+"null")
                }
              }
            }


        })
    })
  }

  def School(filterData:(RDD[SchoolBean],Broadcast[Map[String, Int]])) = {

    val schoolData = filterData._1.filter(x => x != null && x.table.equals("t_edu_school"))
    val schoolDataAll: RDD[(String, (String, String, String, String, String, String, String), (String, String, String, String, String, String,String ),(String,String,String,String,String,String,String,String,String,String,String,String,String,String,String,String,String,String,String,String,String))] = schoolData.distinct().map({
      x =>
        val id = x.data.id
        val school_name = x.data.school_name
        val is_branch_school = x.data.is_branch_school //是否分校 0总校 1分校
      val parent_id = x.data.parent_id  //关联的总校
      val address = x.data.address  //学校地址
      val social_credit_code = x.data.social_credit_code  //统一社会信用代码
      val department_master_id = x.data.department_master_id   //所属部门  District(3, "区级"), Municipal(2, "市级"), Ministerial(1, "部级"), Other(0, "其他");
      val department_slave_id = x.data.department_slave_id   //主管部门
      val students_amount = x.data.students_amount   //学生人数
      val staff_amount = x.data.staff_amount     //教职工人数
      val corporation = x.data.corporation     //法人代表
      val corporation_way = x.data.corporation_way  //法人代表手机
      val corporation_telephone = x.data.corporation_telephone   //法人座机
      val department_head = x.data.department_head   //部门负责人姓名
      val department_mobilephone = x.data.department_mobilephone   //部门负责人手机
      val department_telephone = x.data.department_telephone   //部门负责人座机
      val department_fax = x.data.department_fax    //传真
      val department_email = x.data.department_email  //电子邮件
      val food_safety_persion = x.data.food_safety_persion  //项目负责人
      val food_safety_mobilephone = x.data.food_safety_mobilephone //项目负责人手机
      val food_safety_telephone = x.data.food_safety_telephone  //项目负责人座机

        if ("insert".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("insert", SchoolRule.SchoolNew(x), ("null", "null", "null", "null", "null", "null", "null"),(id,school_name,is_branch_school,parent_id,address,social_credit_code,department_master_id,department_slave_id,students_amount,staff_amount,corporation,corporation_way,corporation_telephone,department_head,department_mobilephone,department_telephone,department_fax,department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone))
        } else if ("delete".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("delete", SchoolRule.SchoolNew(x), ("null", "null", "null", "null", "null", "null", "null"),(id,school_name,is_branch_school,parent_id,address,social_credit_code,department_master_id,department_slave_id,students_amount,staff_amount,corporation,corporation_way,corporation_telephone,department_head,department_mobilephone,department_telephone,department_fax,department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone))
        } else if("update".equals(x.`type`)) {
          ("update", SchoolRule.SchoolNew(x), SchoolRule.SchoolOld(x),(id,school_name,is_branch_school,parent_id,address,social_credit_code,department_master_id,department_slave_id,students_amount,staff_amount,corporation,corporation_way,corporation_telephone,department_head,department_mobilephone,department_telephone,department_fax,department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone))
        }else{
          ("null", ("null","null","null","null","null","null","null"), ("null", "null", "null", "null", "null", "null", "null"),("null","null","null","null","null","null","null","null","null","null","null","null","null","null","null","null","null","null","null","null","null"))
        }
    })

    schoolDataAll.filter(x => !x._1.equals("null")).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1)) {
              jedis.hincrBy("schoolData", "shanghai", 1)
              jedis.hincrBy("schoolData","area"+"_"+x._2._1,1)
              jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
              jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
              jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
              jedis.hincrBy("schoolData","area"+"_"+x._2._1+"_"+"level"+"_"+x._2._2,1)

              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1, 1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1 + ";" + "level" + ";" + x._2._2, 1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1 + ";" + "nature" + ";" + x._2._3 + ";" + "nature-sub" + ";" + x._2._7, 1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6, 1)
            } else if ("delete".equals(x._1)) {
              jedis.hincrBy("schoolData", "shanghai", -1)
              jedis.hincrBy("schoolData","area"+"_"+x._2._1,-1)
              jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
              jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
              jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
              jedis.hincrBy("schoolData","area"+"_"+x._2._1+"_"+"level"+"_"+x._2._2,-1)
              jedis.hdel("schoolDetail",x._4._1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1, -1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1 + ";" + "level" + ";" + x._2._2, -1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1 + ";" + "nature" + ";" + x._2._3 + ";" + "nature-sub" + ";" + x._2._7, -1)
              //              jedis.hincrBy("schoolData", "area" + ";" + x._2._1 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6, -1)
            } else if("update".equals(x._1)) {
              logger.info("t_edu_school_update"+x._1+"_"+x._2._1+"_"+x._2._3+"_"+x._2._4+"_"+x._2._5+"_"+x._3._1+"_"+x._3._2+"_"+x._3._4+"_"+x._3._5)
              val v = jedis.hget("schoolDetail", x._4._1)
              if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
                if (StringUtils.isNoneEmpty(x._3._5) && !x._3._5.equals("null")) {
                  if (x._3._5.equals("1") && x._2._5.equals("0")) {
                    jedis.hincrBy("schoolData", "shanghai", -1)
                    jedis.hincrBy("schoolData","area"+"_"+x._2._1,-1)
                    jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, -1)
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, -1)
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, -1)
                    jedis.hincrBy("schoolData","area"+"_"+x._2._1+"_"+"level"+"_"+x._2._2,-1)
                    jedis.hdel("schoolDetail", x._4._1)

                  } else if (x._3._5.equals("0") && x._2._5.equals("1")) {
                    jedis.hincrBy("schoolData", "shanghai", 1)
                    jedis.hincrBy("schoolData","area"+"_"+x._2._1,1)
                    jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                    jedis.hincrBy("schoolData","area"+"_"+x._2._1+"_"+"level"+"_"+x._2._2,1)

                    jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + filterData._2.value.getOrElse(x._4._1, 0) + ";" + "slictype" + v.split("slictype")(1))

                  } else {
                    logger.info("stat发生变化的不符合标准-----------------" + x)
                  }
                } else {
                  jedis.hset("schoolDetail", x._4._1, "id" + ";" + x._4._1 + ";" + "schoolname" + ";" + x._4._2 + ";" + "isbranchschool" + ";" + x._4._3 + ";" + "parentid" + ";" + x._4._4 + ";" + "area" + ";" + x._2._1 + ";" + "address" + ";" + x._4._5 + ";" + "socialcreditcode" + ";" + x._4._6 + ";" + "level" + ";" + x._2._2 + ";" + "schoolnature" + ";" + x._2._3 + ";" + "schoolnaturesub" + ";" + x._2._7 + ";" + "departmentmasterid" + ";" + x._4._7 + ";" + "departmentslaveid" + ";" + x._4._8 + ";" + "canteenmode" + ";" + x._2._4 + ";" + "ledgertype" + ";" + x._2._6 + ";" + "studentsamount" + ";" + x._4._9 + ";" + "staffamount" + ";" + x._4._10 + ";" + "corporation" + ";" + x._4._11 + ";" + "corporationway" + ";" + x._4._12 + ";" + "corporationtelephone" + ";" + x._4._13 + ";" + "departmenthead" + ";" + x._4._14 + ";" + "departmentmobilephone" + ";" + x._4._15 + ";" + "departmenttelephone" + ";" + x._4._16 + ";" + "departmentfax" + ";" + x._4._17 + ";" + "departmentemail" + ";" + x._4._18 + ";" + "foodsafetypersion" + ";" + x._4._19 + ";" + "foodsafetymobilephone" + ";" + x._4._20 + ";" + "foodsafetytelephone" + ";" + x._4._21 + ";" + "gongcan" + ";" + filterData._2.value.getOrElse(x._4._1, 0) + ";" + "slictype" + v.split("slictype")(1))

                  if (StringUtils.isNoneEmpty(x._3._2) && !x._3._2.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._2._2, 1)
                    jedis.hincrBy("schoolData", "shanghai-level" + "_" + x._3._2, -1)

                    jedis.hincrBy("schoolData","area"+"_"+x._2._1+"_"+"level"+"_"+x._2._2,1)
                    jedis.hincrBy("schoolData","area"+"_"+x._2._1+"_"+"level"+"_"+x._3._2,-1)

                  } else if (StringUtils.isNoneEmpty(x._3._3) && StringUtils.isNoneEmpty(x._3._7) && !x._3._3.equals("null") && !x._3._7.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._3._7, -1)

                  } else if (StringUtils.isNoneEmpty(x._3._3) && StringUtils.isEmpty(x._3._7) && !x._3._3.equals("null") && x._3._7.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._3._3 + "_" + "nature-sub" + "_" + x._2._7, -1)

                  } else if (StringUtils.isEmpty(x._3._3) && StringUtils.isNoneEmpty(x._3._7) && x._3._3.equals("null") && !x._3._7.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._2._7, 1)
                    jedis.hincrBy("schoolData", "shanghai-nature" + "_" + x._2._3 + "_" + "nature-sub" + "_" + x._3._7, -1)

                  } else if (StringUtils.isNoneEmpty(x._3._4) && StringUtils.isNoneEmpty(x._3._6) && !x._3._4.equals("null") && !x._3._6.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._3._6, -1)

                  } else if (StringUtils.isNoneEmpty(x._3._4) && StringUtils.isEmpty(x._3._6) && !x._3._4.equals("null") && x._3._6.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._3._4 + "_" + "ledgertype" + "_" + x._2._6, -1)

                  } else if (StringUtils.isEmpty(x._3._4) && StringUtils.isNoneEmpty(x._3._6) && x._3._4.equals("null") && !x._3._6.equals("null")) {
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._2._6, 1)
                    jedis.hincrBy("schoolData", "shanghai-canteenmode" + "_" + x._2._4 + "_" + "ledgertype" + "_" + x._3._6, -1)

                  } else {
                    logger.info("更新信息不符合的条件---------------" + x)
                  }
                }
              }
            }
        })
    })

  }


  def schoolImage(filterData:RDD[SchoolBean]) = {

    val licenseData = filterData.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert"))
    val license = licenseData.distinct().filter(x => "0".equals(x.data.lic_type) || "1".equals(x.data.lic_type)).map({
      x =>
        val lic_type = x.data.lic_type
        var lic_pic_name="null"
        val lic_pic = x.data.lic_pic
        if(StringUtils.isNoneEmpty(lic_pic) && !lic_pic.equals("null")){
          lic_pic_name=lic_pic
        }else{
          lic_pic_name
        }

        var job_organization_name="null"
        val job_organization = x.data.job_organization
        if(StringUtils.isNoneEmpty(job_organization) && !job_organization.equals("null")){
          job_organization_name=job_organization
        }else{
          job_organization_name
        }
        val relation_id = x.data.relation_id

        var lic_no_name="null"
        val lic_no = x.data.lic_no
        if(StringUtils.isNoneEmpty(lic_no) && !lic_no.equals("null")){
          lic_no_name=lic_no
        }else{
          lic_no_name
        }

        var operation_name="null"
        val operation = x.data.operation
        if(StringUtils.isNoneEmpty(operation) && !operation.equals("null")){
          operation_name=operation
        }else{
          operation_name
        }

        var give_lic_date_name="null"
        val give_lic_date = x.data.give_lic_date
        if(StringUtils.isNoneEmpty(give_lic_date) && !give_lic_date.equals("null")){
          give_lic_date_name=give_lic_date
        }else{
          give_lic_date_name
        }

        var lic_end_date_name="null"
        val lic_end_date = x.data.lic_end_date
        if(StringUtils.isNoneEmpty(lic_end_date) && !lic_end_date.equals("null")){
          lic_end_date_name=lic_end_date
        }else{
          lic_end_date_name
        }

        val stat = x.data.stat
        if ("delete".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("delete", relation_id, lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name, stat,"null")
        } else if ("update".equals(x.`type`)) {
          ("update", relation_id, lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name, stat,x.old.stat)
        }else if("insert".equals(x.`type`) && "1".equals(x.data.stat)){
          ("insert", relation_id, lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name, stat,"null")
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
        }
    }).filter(x => !x._1.equals("null"))

    license.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hget("schoolDetail", x._2)
            if(StringUtils.isNoneEmpty(v) && !v.equals("null")) {
              if ("0".equals(x._3))

              {
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

                }else if("insert".equals(x._1)){
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

                }else if("insert".equals(x._1)){
                  jedis.hset("schoolDetail", x._2, v.split("slictype")(0) + "slictype" + ";" + x._3 + ";" + "slicpic" + ";" + x._4 + ";" + "slicjob" + ";" + x._5 + ";" + "slicno" + ";" + x._6 + ";" + "soperation" + ";" + x._7 + ";" + "slicdate" + ";" + x._8 + ";" + "senddate" + ";" + x._9 + ";" + "clictype" + v.split("clictype")(1))
                }
              }
            }
        })
    })

  }
}
