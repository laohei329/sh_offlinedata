package com.ssic.service

import java.sql.DriverManager

import com.ssic.beans.SchoolBean
import com.ssic.utils.{JPools, NewSchoolToOldSchool}
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

object ZhongTaiDetailToLocal {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //将中台的学校基础信息表转到本地的学校基础信息表
  def SchoolDetail(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, String]])) = {
    filterData._1.filter(x => x != null
      && x.database.equals("merchant")
      && x.table.equals("t_edu_school")).map({
      x =>
        val types = x.`type` //插入，删除，更新操作类型
      val org_merchant_id = x.data.org_merchant_id
        val org_parent_merchant_id = x.data.org_parent_merchant_id
        val uuid = x.data.uuid
        val school_parent_id = x.data.school_parent_id
        val parent_id = x.data.parent_id
        val school_id = x.data.school_id
        val committee_org_merchant_id = x.data.committee_org_merchant_id
        val master_id = filterData._2.value.getOrElse(committee_org_merchant_id, "null")
        val department_master_id = NewSchoolToOldSchool.committeeToOldMasterId(master_id) //将中台的关于教属的映射转到以前老的教属的映射

        val commite_name = filterData._3.value.getOrElse(committee_org_merchant_id, "null")

        val department_slave_id = NewSchoolToOldSchool.committeeToOldSlaveId((master_id, commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id
      val committee_id = x.data.committee_id
        val school_name = x.data.school_name
        val corporation = x.data.corporation
        val corporation_way = x.data.corporation_way
        val address = x.data.address
        val level = x.data.level
        val supplier_id = x.data.supplier_id
        val reviewed = x.data.reviewed
        val school_nature = x.data.school_nature
        val stat = x.data.stat
        val corporation_telephone = x.data.corporation_telephone
        val is_branch_school = x.data.is_branch_school
        val social_credit_code = x.data.social_credit_code
        val school_nature_sub = x.data.school_nature_sub

        val school_area_id = x.data.school_area_id
        val remark = x.data.remark
        val license_main_type = x.data.license_main_type
        val license_main_child = x.data.license_main_child
        val department_head = x.data.department_head
        val department_mobilephone = x.data.department_mobilephone
        val department_telephone = x.data.department_telephone
        val department_fax = x.data.department_fax
        val department_email = x.data.department_email
        val food_safety_persion = x.data.food_safety_persion
        val food_safety_mobilephone = x.data.food_safety_mobilephone
        val food_safety_telephone = x.data.food_safety_telephone
        val level2 = x.data.level2
        val students_amount = x.data.students_amount
        val staff_amount = x.data.staff_amount
        val seat_province_id = x.data.seat_province_id
        val seat_province_name = x.data.seat_province_name
        val seat_city_id = x.data.seat_city_id
        val seat_city_name = x.data.seat_city_name
        //  将上海市各区的新的district_id映射 => 旧的area的映射
        val district_id = NewSchoolToOldSchool.committeeToOldArea(x.data.seat_district_id)

        val seat_district_name = x.data.seat_district_name
        val creator = x.data.creator
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val canteen_mode="0"
        val ledger_type="0"

        var department_id="21"
        if("e6ee4acf-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "1"
        }else if("e6ee4e97-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "2"
        }else if("e6ee4eec-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "3"
        }else if("e6ee4f43-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "4"
        }else if("e6ee4fa4-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "5"
        }else if("e6ee4ffa-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "6"
        }else if("e6ee5054-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "7"
        }else if("e6ee50ac-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "8"
        }else if("e6ee5101-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "9"
        }else if("e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "10"
        }else if("e6ee4c4f-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "11"
        }else if("e6ee4cb2-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "12"
        }else if("e6ee4d17-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "13"
        }else if("e6ee4d78-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "14"
        }else if("e6ee4dd1-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "15"
        }else if("e6ee4e3f-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)){
          department_id = "16"
        }else{
          department_id
        }



        (types, (org_merchant_id, org_parent_merchant_id, uuid, school_parent_id, parent_id, school_id, committee_org_merchant_id, committee_id, school_name, corporation), (corporation_way, address, level, supplier_id, reviewed, school_nature, stat, corporation_telephone, is_branch_school, social_credit_code), (school_nature_sub, department_master_id, department_slave_id, school_area_id, remark, license_main_type, license_main_child, department_head, department_mobilephone, department_telephone), (department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone, level2, students_amount, staff_amount, seat_province_id, seat_province_name), (seat_city_id, seat_city_name, district_id, seat_district_name, creator, create_time, updater, last_update_time,canteen_mode,ledger_type,department_id))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_school (org_merchant_id,org_parent_merchant_id,id,school_parent_id,parent_id,school_id,committee_org_merchant_id,committee_id,school_name,corporation,corporation_way,address,level,supplier_id,reviewed,school_nature,stat,corporation_telephone,is_branch_school,social_credit_code,school_nature_sub,department_master_id,department_slave_id,school_area_id,remark,license_main_type,license_main_child,department_head,department_mobilephone,department_telephone,department_fax,department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone,level2,students_amount,staff_amount,seat_province_id,seat_province_name,seat_city_id,seat_city_name,area,seat_district_name,creator,create_time,updater,last_update_time,canteen_mode,ledger_type,department_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.setString(6, x._2._6)
          statement.setString(7, x._2._7)
          statement.setString(8, x._2._8)
          statement.setString(9, x._2._9)
          statement.setString(10, x._2._10)
          statement.setString(11, x._3._1)
          statement.setString(12, x._3._2)
          statement.setString(13, x._3._3)
          statement.setString(14, x._3._4)
          statement.setString(15, x._3._5)
          statement.setString(16, x._3._6)
          statement.setString(17, x._3._7)
          statement.setString(18, x._3._8)
          statement.setString(19, x._3._9)
          statement.setString(20, x._3._10)
          statement.setString(21, x._4._1)
          statement.setString(22, x._4._2)
          statement.setString(23, x._4._3)
          statement.setString(24, x._4._4)
          statement.setString(25, x._4._5)
          statement.setString(26, x._4._6)
          statement.setString(27, x._4._7)
          statement.setString(28, x._4._8)
          statement.setString(29, x._4._9)
          statement.setString(30, x._4._10)
          statement.setString(31, x._5._1)
          statement.setString(32, x._5._2)
          statement.setString(33, x._5._3)
          statement.setString(34, x._5._4)
          statement.setString(35, x._5._5)
          statement.setString(36, x._5._6)
          statement.setString(37, x._5._7)
          statement.setString(38, x._5._8)
          statement.setString(39, x._5._9)
          statement.setString(40, x._5._10)
          statement.setString(41, x._6._1)
          statement.setString(42, x._6._2)
          statement.setString(43, x._6._3)
          statement.setString(44, x._6._4)
          statement.setString(45, x._6._5)
          statement.setString(46, x._6._6)
          statement.setString(47, x._6._7)
          statement.setString(48, x._6._8)
          statement.setString(49,x._6._9)
          statement.setString(50,x._6._10)
          statement.setString(51,x._6._11)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(
            s"""
               |update t_edu_school
               |set org_parent_merchant_id =? ,
               |id = ? ,
               |school_parent_id =? ,
               |parent_id =? ,
               |school_id=? ,
               |committee_org_merchant_id =? ,
               |committee_id=? ,
               |school_name=? ,
               |corporation=? ,
               |corporation_way = ? ,
               |address =? ,
               |level = ? ,
               |supplier_id =? ,
               |reviewed =? ,
               |school_nature = ? ,
               |stat = ? ,
               |corporation_telephone =? ,
               |is_branch_school =? ,
               |social_credit_code =? ,
               |school_nature_sub =? ,
               |department_master_id =? ,
               |department_slave_id =? ,
               |school_area_id =? ,
               |remark =? ,
               |license_main_type =? ,
               |license_main_child =? ,
               |department_head =? ,
               |department_mobilephone =? ,
               |department_telephone =? ,
               |department_fax =? ,
               |department_email =? ,
               |food_safety_persion =? ,
               |food_safety_mobilephone =? ,
               |food_safety_telephone =? ,
               |level2 =? ,
               |students_amount =? ,
               |staff_amount =? ,
               |seat_province_id =? ,
               |seat_province_name =? ,
               |seat_city_id =? ,
               |seat_city_name =? ,
               |area =? ,
               |seat_district_name =? ,
               |creator =? ,
               |create_time =? ,
               |updater =? ,
               |last_update_time =? ,
               |canteen_mode =? ,
               |ledger_type =?
               |where org_merchant_id = '${x._2._1}'
             """.stripMargin)
          statement.setString(1, x._2._2)
          statement.setString(2, x._2._3)
          statement.setString(3, x._2._4)
          statement.setString(4, x._2._5)
          statement.setString(5, x._2._6)
          statement.setString(6, x._2._7)
          statement.setString(7, x._2._8)
          statement.setString(8, x._2._9)
          statement.setString(9, x._2._10)
          statement.setString(10, x._3._1)
          statement.setString(11, x._3._2)
          statement.setString(12, x._3._3)
          statement.setString(13, x._3._4)
          statement.setString(14, x._3._5)
          statement.setString(15, x._3._6)
          statement.setString(16, x._3._7)
          statement.setString(17, x._3._8)
          statement.setString(18, x._3._9)
          statement.setString(19, x._3._10)
          statement.setString(20, x._4._1)
          statement.setString(21, x._4._2)
          statement.setString(22, x._4._3)
          statement.setString(23, x._4._4)
          statement.setString(24, x._4._5)
          statement.setString(25, x._4._6)
          statement.setString(26, x._4._7)
          statement.setString(27, x._4._8)
          statement.setString(28, x._4._9)
          statement.setString(29, x._4._10)
          statement.setString(30, x._5._1)
          statement.setString(31, x._5._2)
          statement.setString(32, x._5._3)
          statement.setString(33, x._5._4)
          statement.setString(34, x._5._5)
          statement.setString(35, x._5._6)
          statement.setString(36, x._5._7)
          statement.setString(37, x._5._8)
          statement.setString(38, x._5._9)
          statement.setString(39, x._5._10)
          statement.setString(40, x._6._1)
          statement.setString(41, x._6._2)
          statement.setString(42, x._6._3)
          statement.setString(43, x._6._4)
          statement.setString(44, x._6._5)
          statement.setString(45, x._6._6)
          statement.setString(46, x._6._7)
          statement.setString(47, x._6._8)
          statement.setString(48,x._6._9)
          statement.setString(49,x._6._10)
          statement.execute()
          conn.close()
        } else {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_school where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })

  }

  //将中台的学校与团餐公司的关联表信息，转到本地
  def SchoolSupplier(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("order")
      && x.table.equals("groupon_customer")).map({
      x =>
        val types = x.`type` //插入，删除，更新操作类型
      val id = x.data.id
        val org_merchant_id = x.data.org_merchant_id
        val org_parent_merchant_id = x.data.org_parent_merchant_id
        var uuid="null"
        if(StringUtils.isNoneEmpty(x.data.uuid) && !"null".equals(x.data.uuid)){
          uuid=x.data.uuid
        }else{
          uuid
        }
        val school_id = x.data.school_id
        val supplier_id = x.data.supplier_id
        val stat = x.data.stat
        val company_id = x.data.company_id    //11 表示b2b的   12 表示阳光午餐

        (types, id, org_merchant_id, org_parent_merchant_id, uuid, school_id, supplier_id, stat,company_id)
    }).filter(x => ("12").equals(x._9)).filter(x => !"null".equals(x._5)).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_school_supplier (groupon_customer_id,org_merchant_id,org_parent_merchant_id,id,school_id,supplier_id,stat) values (?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_edu_school_supplier (groupon_customer_id,org_merchant_id,org_parent_merchant_id,id,school_id,supplier_id,stat) values (?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_school_supplier where groupon_customer_id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  //将中台的团餐公司信息表转到本地的t_pro_supplier表
  def GroupSupplier(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("merchant")
      && x.table.equals("t_edu_group_catering_company")).map({
      x =>
        val types = x.`type` //插入，删除，更新操作类型
      val org_merchant_id = x.data.org_merchant_id
        val org_parent_merchant_id = x.data.org_parent_merchant_id
        val parent_id = x.data.parent_id
        val uuid = x.data.uuid
        val supplier_name = x.data.supplier_name
        val company_type = x.data.company_type
        val supplier_classify = null
        val supplier_type = "1" //1团餐公司 2供应商
      val provinces = x.data.provinces
        val city = x.data.city

        val area = NewSchoolToOldSchool.committeeToOldArea(x.data.area)
        val address = x.data.address //详情地址
      val contacts = x.data.contacts //联系人
      val contact_way = x.data.contact_way //联系人联系方式
      val qa_person = x.data.qa_person //质量负责人
      val qa_way = x.data.qa_way //质量负责人联系电话
      val reg_address = x.data.reg_address //注册地址
      val reg_capital = x.data.reg_capital //注册资金
      val corporation = x.data.corporation //法人
      val reviewed = x.data.reviewed
        val stat = x.data.stat
        val creator = x.data.creator
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val is_available = null
        val is_deleted = null
        val audit_state = null
        (types, (org_merchant_id, org_parent_merchant_id, parent_id, uuid, supplier_name, company_type, address, provinces, city, area), (supplier_classify, supplier_type, contacts, contact_way, reviewed, qa_person, qa_way, reg_capital, corporation, stat), (creator, create_time, updater, last_update_time, is_available, is_deleted, audit_state))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_pro_supplier (org_merchant_id,org_parent_merchant_id,parent_id,id,supplier_name,company_type,address,provinces,city,area,supplier_classify,supplier_type,contacts,contact_way,reviewed,qa_person,qa_way,registered_capital,corporation,stat,creator,create_time,updater,last_update_time,is_available,is_deleted,audit_state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.setString(6, x._2._6)
          statement.setString(7, x._2._7)
          statement.setString(8, x._2._8)
          statement.setString(9, x._2._9)
          statement.setString(10, x._2._10)
          statement.setString(11, x._3._1)
          statement.setString(12, x._3._2)
          statement.setString(13, x._3._3)
          statement.setString(14, x._3._4)
          statement.setString(15, x._3._5)
          statement.setString(16, x._3._6)
          statement.setString(17, x._3._7)
          statement.setString(18, x._3._8)
          statement.setString(19, x._3._9)
          statement.setString(20, x._3._10)
          statement.setString(21, x._4._1)
          statement.setString(22, x._4._2)
          statement.setString(23, x._4._3)
          statement.setString(24, x._4._4)
          statement.setString(25, x._4._5)
          statement.setString(26, x._4._6)
          statement.setString(27, x._4._7)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_pro_supplier (org_merchant_id,org_parent_merchant_id,parent_id,id,supplier_name,company_type,address,provinces,city,area,supplier_classify,supplier_type,contacts,contact_way,reviewed,qa_person,qa_way,registered_capital,corporation,stat,creator,create_time,updater,last_update_time,is_available,is_deleted,audit_state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.setString(6, x._2._6)
          statement.setString(7, x._2._7)
          statement.setString(8, x._2._8)
          statement.setString(9, x._2._9)
          statement.setString(10, x._2._10)
          statement.setString(11, x._3._1)
          statement.setString(12, x._3._2)
          statement.setString(13, x._3._3)
          statement.setString(14, x._3._4)
          statement.setString(15, x._3._5)
          statement.setString(16, x._3._6)
          statement.setString(17, x._3._7)
          statement.setString(18, x._3._8)
          statement.setString(19, x._3._9)
          statement.setString(20, x._3._10)
          statement.setString(21, x._4._1)
          statement.setString(22, x._4._2)
          statement.setString(23, x._4._3)
          statement.setString(24, x._4._4)
          statement.setString(25, x._4._5)
          statement.setString(26, x._4._6)
          statement.setString(27, x._4._7)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_pro_supplier where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })
  }


  //中台的供应商信息转到本地的t_pro_supplier

  def SupplierInfo(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("opms")
      && x.table.equals("supplier_info") ).map({
      x =>
        val types = x.`type`
        val id = x.data.id
        val org_parent_merchant_id = null
        val parent_id = null
        var uuid ="-1"
        if("12".equals(x.data.company_id) && StringUtils.isNoneEmpty(x.data.uuid) && !"null".equals(x.data.uuid)){
         uuid = x.data.uuid
        }else{
         uuid =x.data.id
        }

        val supplier_name = x.data.supplier_name
        val company_type = null
        val address = x.data.address
        val provinces = x.data.province_id
        val city = x.data.city_id
        val area = x.data.region_id
        val supplier_classify = x.data.supplier_classify
        val supplier_type = "2"
        val contacts = x.data.supplier_contact_name
        val contact_way = x.data.supplier_contact_mobilephone
        val reviewed = null
        val qa_person = null
        val qa_way = null
        val reg_capital = x.data.registered_capital
        val corporation = x.data.legal_representative
        val stat = null
        val creator = x.data.create_username
        val create_time = x.data.create_time
        val updater = x.data.update_username
        val last_update_time = x.data.update_time
        val is_available = x.data.is_available
        val is_deleted = x.data.is_deleted
        val audit_state = x.data.audit_state

        (types, (id, org_parent_merchant_id, parent_id, uuid, supplier_name, company_type, address, provinces, city, area), (supplier_classify, supplier_type, contacts, contact_way, reviewed, qa_person, qa_way, reg_capital, corporation, stat), (creator, create_time, updater, last_update_time, is_available, is_deleted, audit_state))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_pro_supplier (org_merchant_id,org_parent_merchant_id,parent_id,id,supplier_name,company_type,address,provinces,city,area,supplier_classify,supplier_type,contacts,contact_way,reviewed,qa_person,qa_way,registered_capital,corporation,stat,creator,create_time,updater,last_update_time,is_available,is_deleted,audit_state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.setString(6, x._2._6)
          statement.setString(7, x._2._7)
          statement.setString(8, x._2._8)
          statement.setString(9, x._2._9)
          statement.setString(10, x._2._10)
          statement.setString(11, x._3._1)
          statement.setString(12, x._3._2)
          statement.setString(13, x._3._3)
          statement.setString(14, x._3._4)
          statement.setString(15, x._3._5)
          statement.setString(16, x._3._6)
          statement.setString(17, x._3._7)
          statement.setString(18, x._3._8)
          statement.setString(19, x._3._9)
          statement.setString(20, x._3._10)
          statement.setString(21, x._4._1)
          statement.setString(22, x._4._2)
          statement.setString(23, x._4._3)
          statement.setString(24, x._4._4)
          statement.setString(25, x._4._5)
          statement.setString(26, x._4._6)
          statement.setString(27, x._4._7)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_pro_supplier (org_merchant_id,org_parent_merchant_id,parent_id,id,supplier_name,company_type,address,provinces,city,area,supplier_classify,supplier_type,contacts,contact_way,reviewed,qa_person,qa_way,registered_capital,corporation,stat,creator,create_time,updater,last_update_time,is_available,is_deleted,audit_state) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.setString(6, x._2._6)
          statement.setString(7, x._2._7)
          statement.setString(8, x._2._8)
          statement.setString(9, x._2._9)
          statement.setString(10, x._2._10)
          statement.setString(11, x._3._1)
          statement.setString(12, x._3._2)
          statement.setString(13, x._3._3)
          statement.setString(14, x._3._4)
          statement.setString(15, x._3._5)
          statement.setString(16, x._3._6)
          statement.setString(17, x._3._7)
          statement.setString(18, x._3._8)
          statement.setString(19, x._3._9)
          statement.setString(20, x._3._10)
          statement.setString(21, x._4._1)
          statement.setString(22, x._4._2)
          statement.setString(23, x._4._3)
          statement.setString(24, x._4._4)
          statement.setString(25, x._4._5)
          statement.setString(26, x._4._6)
          statement.setString(27, x._4._7)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_pro_supplier where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })

  }

  //中台的地域信息转到本地的area
  def Area(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("misc")
      && x.table.equals("area")).map({
      x =>
        val types = x.`type`
        val ID = x.data.ID
        val code = x.data.code
        val abbreviation = x.data.abbreviation
        val name = x.data.name
        val is_available = x.data.is_available
        val isdeleted = x.data.IS_DELETED
        val Create_UserName = x.data.Create_UserName
        val create_time = x.data.create_time
        val Update_UserName = x.data.Update_UserName
        val update_time = x.data.update_time
        (types, ID, code, abbreviation, name, is_available, isdeleted, Create_UserName, create_time, Update_UserName, update_time)
    }).foreach({
      x =>
        if (x._1.equals("insert")) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into area (ID,code,abbreviation,name,is_available,IS_DELETED,Create_UserName,create_time,Update_UserName,update_time) values (?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.execute()
          conn.close()
        } else if (x._1.equals("update")) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into area (ID,code,abbreviation,name,is_available,IS_DELETED,Create_UserName,create_time,Update_UserName,update_time) values (?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from area where ID='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  //中台的t_edu_competent_department转到本地的t_edu_competent_department
  def EduCompetentDepartment(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("merchant")
      && x.table.equals("t_edu_competent_department")).map({
      x =>
        val types = x.`type`
        val org_merchant_id = x.data.org_merchant_id
        val org_parent_merchant_id = x.data.org_parent_merchant_id
        val name = x.data.name
        val code = x.data.code
        val uuid = x.data.uuid
        val parent_id = x.data.parent_id
        val management_area_type = x.data.management_area_type
        val create_username = x.data.create_username
        val create_time = x.data.create_time
        val update_username = x.data.update_username
        val update_time = x.data.update_time
        val is_deleted = x.data.is_deleted
        (types, org_merchant_id, org_parent_merchant_id, name, code, uuid, parent_id, management_area_type, create_username, create_time, update_username, update_time, is_deleted)

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_competent_department (org_merchant_id,org_parent_merchant_id,name,code,uuid,parent_id,management_area_type,create_username,create_time,update_username,update_time,is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.setString(12, x._13)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_edu_competent_department (org_merchant_id,org_parent_merchant_id,name,code,uuid,parent_id,management_area_type,create_username,create_time,update_username,update_time,is_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.setString(12, x._13)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_competent_department where org_merchant_id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  def EduCalendar(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.table.equals("t_edu_calendar")).map({
      x =>
        val types = x.`type`
        val id = x.data.id
        val the_day = x.data.the_day
        val have_class = x.data.have_class
        val create_id = x.data.create_id
        val create_name = x.data.create_name
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val stat = x.data.stat
        val school_id = x.data.school_id
        val reason = x.data.reason
        (types,id,the_day,have_class,create_id,create_name,create_time,updater,last_update_time,stat,school_id,reason)
    }).foreach({
      x =>
        if("insert".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_calendar (id,the_day,have_class,create_id,create_name,create_time,updater,last_update_time,stat,school_id,reason) values (?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.execute()
          conn.close()
        }else if("update".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_edu_calendar (id,the_day,have_class,create_id,create_name,create_time,updater,last_update_time,stat,school_id,reason) values (?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.execute()
          conn.close()
        }else{
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_calendar where id='${x._2}'")
          statement.execute()
          conn.close()
        }

    })
  }


  def EduSchooltrem(filterData: RDD[SchoolBean])={
    filterData.filter(x => x != null
      && x.table.equals("t_edu_schoolterm")).map({
      x =>
        val types = x.`type`
        val id = x.data.id
        val term_year = x.data.term_year
        val school_id = x.data.school_id
        val school_name = x.data.school_name
        val first_start_date = x.data.first_start_date
        val first_end_date = x.data.first_end_date
        val second_start_date = x.data.second_start_date
        val second_end_date = x.data.second_end_date
        val creator = x.data.creator
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val stat = x.data.stat
        (types,id,term_year,school_id,school_name,first_start_date,first_end_date,second_start_date,second_end_date,creator,create_time,updater,last_update_time,stat)
    }).foreach({
      x =>
        if("insert".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_schoolterm (id,term_year,school_id,school_name,first_start_date,first_end_date,second_start_date,second_end_date,creator,create_time,updater,last_update_time,stat) values (?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.setString(12, x._13)
          statement.setString(13, x._14)
          statement.execute()
          conn.close()
        }else if("update".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_edu_schoolterm (id,term_year,school_id,school_name,first_start_date,first_end_date,second_start_date,second_end_date,creator,create_time,updater,last_update_time,stat) values (?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.setString(12, x._13)
          statement.setString(13, x._14)
          statement.execute()
          conn.close()
        }else{
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_schoolterm where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  def EduSchooltermSystem(filterData: RDD[SchoolBean])={
    filterData.filter(x => x != null
      && x.table.equals("t_edu_schoolterm_system")).map({
      x =>
        val types = x.`type`
        val id = x.data.id
        val term_year = x.data.term_year
        val first_start_date = x.data.first_start_date
        val first_end_date = x.data.first_end_date
        val second_start_date = x.data.second_start_date
        val second_end_date = x.data.second_end_date
        val creator = x.data.creator
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val stat = x.data.stat
        (types,id,term_year,first_start_date,first_end_date,second_start_date,second_end_date,creator,create_time,updater,last_update_time,stat)
    }).foreach({
      x =>
        if("insert".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_schoolterm_system (id,term_year,first_start_date,first_end_date,second_start_date,second_end_date,creator,create_time,updater,last_update_time,stat) values (?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.execute()
          conn.close()
        }else if("update".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_edu_schoolterm_system (id,term_year,first_start_date,first_end_date,second_start_date,second_end_date,creator,create_time,updater,last_update_time,stat) values (?,?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.setString(11, x._12)
          statement.execute()
          conn.close()
        }else{
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_schoolterm_system where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  def EduHoliday(filterData: RDD[SchoolBean])={
    filterData.filter(x => x != null
      && x.table.equals("t_edu_holiday")).map({
      x =>
        val types = x.`type`
        val id = x.data.id
        val name = x.data.name
        val holiday_day = x.data.holiday_day
        val edutype = x.data.`type`
        val create_id = x.data.create_id
        val create_name = x.data.create_name
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val stat = x.data.stat
        (types,id,name,holiday_day,edutype,create_id,create_name,create_time,updater,last_update_time,stat)
    }).foreach({
      x =>
        if("insert".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("insert into t_edu_holiday (id,name,holiday_day,type,create_id,create_name,create_time,updater,last_update_time,stat) values (?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.execute()
          conn.close()
        }else if("update".equals(x._1)){
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement("replace into t_edu_holiday (id,name,holiday_day,type,create_id,create_name,create_time,updater,last_update_time,stat) values (?,?,?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._9)
          statement.setString(9, x._10)
          statement.setString(10, x._11)
          statement.execute()
          conn.close()
        }else{
          val conn = DriverManager.getConnection("jdbc:mysql://172.16.10.18:3306/saas_v1?characterEncoding=utf8", "maxwell", "S3cret_ssic_Bi")
          val statement = conn.prepareStatement(s"delete from t_edu_holiday where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

}
