package com.ssic.service

import java.sql.Timestamp

import com.alibaba.fastjson.JSON
import com.ssic.beans._
import com.ssic.utils._
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

object ZhongTaiDetailToLocal {
  private val logger = LoggerFactory.getLogger(this.getClass)

  //将中台的学校基础信息表转到本地的学校基础信息表

  /**
    *  写数据到学校基础信息表
    * @param filterData  RDD[SchoolBean] binlog日志数据
    *  //@param   //Broadcast[Map[String, String]]  主管部门映射
    * //@param   Broadcast[Map[String, String]]  主管部门映射的名字
    */

  def SchoolDetail(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, String]])) = {
    filterData._1.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("t_edu_school")).map({
      x =>
        val schoolBean = JSON.parseObject(x.data, classOf[School])
        val types = x.`type` //插入，删除，更新操作类型
      val org_merchant_id = schoolBean.org_merchant_id
        val org_parent_merchant_id = schoolBean.org_parent_merchant_id
        val uuid = schoolBean.uuid
        val school_parent_id = schoolBean.school_parent_id
        val parent_id = schoolBean.parent_id
        val school_id = schoolBean.school_id
        val committee_org_merchant_id = schoolBean.committee_org_merchant_id
        val master_id = filterData._2.value.getOrElse(committee_org_merchant_id, "null")
        val department_master_id = NewSchoolToOldSchool.committeeToOldMasterId(master_id) //将中台的关于教属的映射转到以前老的教属的映射

        val commite_name = filterData._3.value.getOrElse(committee_org_merchant_id, "null")

        val department_slave_id = NewSchoolToOldSchool.committeeToOldSlaveId((master_id, commite_name)) //将中台的关于教属的映射转到以前老的教属的映射department_slave_id
        val committee_id = schoolBean.committee_id
        val school_name = schoolBean.school_name
        val corporation = schoolBean.corporation
        val corporation_way = schoolBean.corporation_way
        val address = schoolBean.address
        val level = schoolBean.level
        val supplier_id = schoolBean.supplier_id
        val reviewed = schoolBean.reviewed
        val school_nature = schoolBean.school_nature
        val stat = schoolBean.stat
        val corporation_telephone = schoolBean.corporation_mobile
        val is_branch_school = schoolBean.is_branch_school
        val social_credit_code = schoolBean.social_credit_code
        val school_nature_sub = schoolBean.school_nature_sub

        val school_area_id = schoolBean.school_area_id
        val remark = schoolBean.remark
        val license_main_type = schoolBean.license_main_type
        val license_main_child = schoolBean.license_main_child
        val department_head = schoolBean.department_head
        val department_mobilephone = schoolBean.department_mobilephone
        val department_telephone = schoolBean.department_telephone
        val department_fax = schoolBean.department_fax
        val department_email = schoolBean.department_email
        val food_safety_persion = schoolBean.food_safety_persion
        val food_safety_mobilephone = schoolBean.food_safety_mobilephone
        val food_safety_telephone = schoolBean.food_safety_telephone
        val level2 = schoolBean.level2
        val students_amount = schoolBean.students_amount
        val staff_amount = schoolBean.staff_amount
        val seat_province_id = schoolBean.seat_province_id
        val seat_province_name = schoolBean.seat_province_name
        val seat_city_id = schoolBean.seat_city_id
        val seat_city_name = schoolBean.seat_city_name
        //  将上海市各区的新的district_id映射 => 旧的area的映射
        val district_id = NewSchoolToOldSchool.committeeToOldArea(schoolBean.seat_district_id)

        val seat_district_name = schoolBean.seat_district_name
        val creator = schoolBean.creator
        val create_time = schoolBean.create_time
        val updater = schoolBean.updater
        val last_update_time = schoolBean.last_update_time
        val canteen_mode = "0"
        val ledger_type = "0"
        val is_customer = schoolBean.is_customer
        val customer_school_id = schoolBean.customer_school_id


        var department_id = "21"
        if ("e6ee4acf-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "1"
        } else if ("e6ee4e97-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "2"
        } else if ("e6ee4eec-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "3"
        } else if ("e6ee4f43-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "4"
        } else if ("e6ee4fa4-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "5"
        } else if ("e6ee4ffa-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "6"
        } else if ("e6ee5054-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "7"
        } else if ("e6ee50ac-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "8"
        } else if ("e6ee5101-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "9"
        } else if ("e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "10"
        } else if ("e6ee4c4f-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "11"
        } else if ("e6ee4cb2-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "12"
        } else if ("e6ee4d17-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "13"
        } else if ("e6ee4d78-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "14"
        } else if ("e6ee4dd1-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "15"
        } else if ("e6ee4e3f-2c5b-11e6-b1e8-005056a5ed30".equals(department_slave_id)) {
          department_id = "16"
        } else {
          department_id
        }


        (types, (org_merchant_id, org_parent_merchant_id, uuid, school_parent_id, parent_id, school_id, committee_org_merchant_id, committee_id, school_name, corporation),
          (corporation_way, address, level, supplier_id, reviewed, school_nature, stat, corporation_telephone, is_branch_school, social_credit_code),
          (school_nature_sub, department_master_id, department_slave_id, school_area_id, remark, license_main_type, license_main_child, department_head, department_mobilephone, department_telephone),
          (department_fax, department_email, food_safety_persion, food_safety_mobilephone, food_safety_telephone, level2, students_amount, staff_amount, seat_province_id, seat_province_name),
          (seat_city_id, seat_city_name, district_id, seat_district_name, creator, create_time, updater, last_update_time, canteen_mode, ledger_type, department_id),
          (is_customer, customer_school_id))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into t_edu_school (org_merchant_id,org_parent_merchant_id,id,school_parent_id,parent_id,school_id,committee_org_merchant_id,committee_id,school_name,corporation,corporation_way,address,level,supplier_id,reviewed,school_nature,stat,corporation_telephone,is_branch_school,social_credit_code,school_nature_sub,department_master_id,department_slave_id,school_area_id,remark,license_main_type,license_main_child,department_head,department_mobilephone,department_telephone,department_fax,department_email,food_safety_persion,food_safety_mobilephone,food_safety_telephone,level2,students_amount,staff_amount,seat_province_id,seat_province_name,seat_city_id,seat_city_name,area,seat_district_name,creator,create_time,updater,last_update_time,canteen_mode,ledger_type,department_id,is_customer,customer_school_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
          statement.setString(49, x._6._9)
          statement.setString(50, x._6._10)
          statement.setString(51, x._6._11)
          statement.setString(52, x._7._1)
          statement.setString(53, x._7._2)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
               |ledger_type =?,
               |is_customer =?,
               |customer_school_id = ?
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
          statement.setString(48, x._6._9)
          statement.setString(49, x._6._10)
          statement.setString(50, x._7._1)
          statement.setString(51, x._7._2)
          statement.execute()
          conn.close()
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_school where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })

  }


  /**
    *   将中台的学校与团餐公司的关联表信息，转到本地
    *  @param filterData RDD[SchoolBean] binlog日志数据
    */
  def SchoolSupplier(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("groupon_customer")).map({
      x =>
        val schoolSupplierBean = JSON.parseObject(x.data, classOf[SchoolSupplier])
        val types = x.`type` //插入，删除，更新操作类型
      val id = schoolSupplierBean.id
        val org_merchant_id = schoolSupplierBean.org_merchant_id
        val org_parent_merchant_id = schoolSupplierBean.org_parent_merchant_id
        var uuid = "null"
        if (StringUtils.isNoneEmpty(schoolSupplierBean.uuid) && !"null".equals(schoolSupplierBean.uuid)) {
          uuid = schoolSupplierBean.uuid
        } else {
          uuid
        }
        val school_id = schoolSupplierBean.school_id
        val supplier_id = schoolSupplierBean.supplier_id
        val stat = schoolSupplierBean.stat
        val company_id = schoolSupplierBean.company_id //11 表示b2b的   12 表示阳光午餐
      val industry_type = schoolSupplierBean.industry_type

        (types, id, org_merchant_id, org_parent_merchant_id, uuid, school_id, supplier_id, stat, company_id, industry_type)
    }).filter(x => ("12").equals(x._9)).filter(x => !"null".equals(x._5)).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into t_edu_school_supplier (groupon_customer_id,org_merchant_id,org_parent_merchant_id,id,school_id,supplier_id,stat,industry_type) values (?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._10)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into t_edu_school_supplier (groupon_customer_id,org_merchant_id,org_parent_merchant_id,id,school_id,supplier_id,stat,industry_type) values (?,?,?,?,?,?,?,?)")
          statement.setString(1, x._2)
          statement.setString(2, x._3)
          statement.setString(3, x._4)
          statement.setString(4, x._5)
          statement.setString(5, x._6)
          statement.setString(6, x._7)
          statement.setString(7, x._8)
          statement.setString(8, x._10)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_school_supplier where groupon_customer_id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  def grouponCustomer(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("groupon_customer")).map({
      x =>
        val grouponCustomerBean = JSON.parseObject(x.data, classOf[GrouponCustomer])
        val types = x.`type` //插入，删除，更新操作类型
        val id = grouponCustomerBean.id
        val org_merchant_id = grouponCustomerBean.org_merchant_id
        val uuid: String = grouponCustomerBean.uuid
        val customer_name: String = grouponCustomerBean.customer_name
        val customer_type: Int = grouponCustomerBean.customer_type
        val contact_mobile: String = grouponCustomerBean.contact_mobile
        val address: String = grouponCustomerBean.address
        val merchant_id: Long = grouponCustomerBean.merchant_id
        val is_available: Int = grouponCustomerBean.is_available
        val is_deleted: Int = grouponCustomerBean.is_deleted
        val version_no: Int = grouponCustomerBean.version_no
        val platform_id: Long = grouponCustomerBean.platform_id
        val company_id: Long = grouponCustomerBean.company_id
        val org_id: Long = grouponCustomerBean.org_id
        val org_type: Int = grouponCustomerBean.org_type
        val catering_new_type: Int = grouponCustomerBean.catering_new_type
        val process_type: Int = grouponCustomerBean.process_type
        val people_num: Int = grouponCustomerBean.people_num
        val identity_card_name: String = grouponCustomerBean.identity_card_name
        val identity_card_phone: String = grouponCustomerBean.identity_card_phone
        val status: Int = grouponCustomerBean.status
        val school_id: String = grouponCustomerBean.school_id
        val supplier_id: String = grouponCustomerBean.supplier_id
        val school_name: String = grouponCustomerBean.school_name
        val contact: String = grouponCustomerBean.contact
        val vote: Int = grouponCustomerBean.vote
        val pj_no: String = grouponCustomerBean.pj_no
        val manager_status: Int = grouponCustomerBean.manager_status
        val proj_type: Int = grouponCustomerBean.proj_type
        val level: String = grouponCustomerBean.level
        val meal_type: Int = grouponCustomerBean.meal_type
        val package_num: Int = grouponCustomerBean.package_num
        val delivery_way: Int = grouponCustomerBean.delivery_way
        val staff_count: Int = grouponCustomerBean.staff_count
        val worker_count: Int = grouponCustomerBean.worker_count
        val student_count: Int = grouponCustomerBean.student_count
        val stat: Int = grouponCustomerBean.stat
        val relation: Int = grouponCustomerBean.relation
        val prov: String = grouponCustomerBean.prov
        val prov_id: Long = grouponCustomerBean.prov_id
        val city: String = grouponCustomerBean.city
        val city_id: Long = grouponCustomerBean.city_id
        val district_name: String = grouponCustomerBean.district_name
        val district_id: Long = grouponCustomerBean.district_id
        val industry_type: Int = grouponCustomerBean.industry_type

        (types
          , (id,org_merchant_id,uuid,customer_name,customer_type,contact_mobile,address,merchant_id,is_available,is_deleted,
          version_no,platform_id,company_id,org_id,org_type,catering_new_type,process_type,people_num)
          ,(identity_card_name,identity_card_phone,status,school_id,supplier_id,school_name,contact,vote,pj_no,manager_status,proj_type)
          ,(level,meal_type,package_num,delivery_way,staff_count,worker_count,student_count,stat,relation,prov,prov_id,city,city_id,district_name,district_id,industry_type))
    })
      .
      foreach({

      x =>
        if ("insert".equals(x._1) || "update".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into groupon_customer (types,id,org_merchant_id,uuid,customer_name," +
            "customer_type,contact_mobile,address,merchant_id,is_available,is_deleted,version_no,platform_id,company_id," +
            "org_id,org_type,catering_new_type,process_type,people_num,identity_card_name,identity_card_phone,status," +
            "school_id,supplier_id,school_name,contact,vote,pj_no,manager_status,proj_type,level,meal_type,package_num," +
            "delivery_way,staff_count,worker_count,student_count,stat,relation,prov,prov_id,city,city_id,district_name," +
            "district_id,industry_type) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)")
          statement.setLong(1 , x._2._1 )
          statement.setLong(2 , x._2._2 )
          statement.setString(3 , x._2._3 )
          statement.setString(4 , x._2._4 )
          statement.setInt(5 , x._2._5 )
          statement.setString(6 , x._2._6 )
          statement.setString(7 , x._2._7 )
          statement.setLong(8 , x._2._8 )
          statement.setInt(9 , x._2._9 )
          statement.setInt(10, x._2._10)
          statement.setInt(11, x._2._11)
          statement.setLong(12, x._2._12)
          statement.setLong(13, x._2._13)
          statement.setLong(14, x._2._14)
          statement.setInt(15, x._2._15)
          statement.setInt(16, x._2._16)
          statement.setInt(17, x._2._17)
          statement.setInt(18, x._2._18)
          statement.setString(19, x._3._1)
          statement.setString(20, x._3._2)
          statement.setInt(21, x._3._3)
          statement.setString(22, x._3._4)
          statement.setString(23, x._3._5)
          statement.setString(24, x._3._6)
          statement.setString(25, x._3._7)
          statement.setInt(26, x._3._8)
          statement.setString(27, x._3._9)
          statement.setInt(28, x._3._10)
          statement.setInt(29, x._3._11)
          statement.setString(30, x._4._1)
          statement.setInt(31, x._4._2)
          statement.setInt(32, x._4._3)
          statement.setInt(33, x._4._4)
          statement.setInt(34, x._4._5)
          statement.setInt(35, x._4._6)
          statement.setInt(36, x._4._7)
          statement.setInt(37, x._4._8)
          statement.setInt(38, x._4._9)
          statement.setString(39, x._4._10)
          statement.setLong(40,  x._4._11)
          statement.setString(41,  x._4._12)
          statement.setLong(42, x._4._13)
          statement.setString(43,  x._4._14)
          statement.setLong(44,  x._4._15)
          statement.setInt(45, x._4._16)
          statement.execute()
          conn.close()
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from groupon_customer where id='${x._1}'")
          statement.execute()
          conn.close()
        }
    })
  }

  /**
    * 将中台的团餐公司信息表转到本地的t_pro_supplier表
    * @param filterData RDD[SchoolBean] binlog日志数据
    */
  def GroupSupplier(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("t_edu_group_catering_company")).map({
      x =>
        val groupSupplierBean = JSON.parseObject(x.data, classOf[GroupSupplier])
        val types = x.`type` //插入，删除，更新操作类型
      val org_merchant_id = groupSupplierBean.org_merchant_id
        val org_parent_merchant_id = groupSupplierBean.org_parent_merchant_id
        val parent_id = groupSupplierBean.parent_id
        val uuid = groupSupplierBean.uuid
        val supplier_name = groupSupplierBean.supplier_name
        val company_type = groupSupplierBean.company_type
        val supplier_classify = null
        val supplier_type = "1" //1团餐公司 2供应商
      val provinces = groupSupplierBean.provinces
        val city = groupSupplierBean.city

        val area = NewSchoolToOldSchool.committeeToOldArea(groupSupplierBean.area)
        val address = groupSupplierBean.address //详情地址
      val contacts = groupSupplierBean.contacts //联系人
      val contact_way = groupSupplierBean.contact_way //联系人联系方式
      val qa_person = groupSupplierBean.qa_person //质量负责人
      val qa_way = groupSupplierBean.qa_way //质量负责人联系电话
      val reg_address = groupSupplierBean.reg_address //注册地址
      val reg_capital = groupSupplierBean.reg_capital //注册资金
      val corporation = groupSupplierBean.corporation //法人
      val reviewed = groupSupplierBean.reviewed
        val stat = groupSupplierBean.stat
        val creator = groupSupplierBean.creator
        val create_time = groupSupplierBean.create_time
        val updater = groupSupplierBean.updater
        val last_update_time = groupSupplierBean.last_update_time
        val is_available = null
        val is_deleted = null
        val audit_state = null
        (types, (org_merchant_id, org_parent_merchant_id, parent_id, uuid, supplier_name, company_type, address, provinces, city, area), (supplier_classify, supplier_type, contacts, contact_way, reviewed, qa_person, qa_way, reg_capital, corporation, stat), (creator, create_time, updater, last_update_time, is_available, is_deleted, audit_state))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_pro_supplier where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })
  }


  /**
    *  中台的供应商信息转到本地的t_pro_supplier
    *  @param  filterData RDD[SchoolBean] binlog日志数据
    */

  def SupplierInfo(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("opms")
      && x.table.equals("supplier_info")).map({
      x =>
        val supplierInfoBean = JSON.parseObject(x.data, classOf[SupplierInfo])
        val types = x.`type`
        val id = supplierInfoBean.id
        val org_parent_merchant_id = null
        val parent_id = null
        var uuid = "-1"
        if ("12".equals(supplierInfoBean.company_id) && StringUtils.isNoneEmpty(supplierInfoBean.uuid) && !"null".equals(supplierInfoBean.uuid)) {
          uuid = supplierInfoBean.uuid
        } else {
          uuid = supplierInfoBean.id
        }

        val supplier_name = supplierInfoBean.supplier_name
        val company_type = null
        val address = supplierInfoBean.address
        val provinces = supplierInfoBean.province_id
        val city = supplierInfoBean.city_id
        val area = supplierInfoBean.region_id
        val supplier_classify = supplierInfoBean.supplier_classify
        val supplier_type = "2"
        val contacts = supplierInfoBean.supplier_contact_name
        val contact_way = supplierInfoBean.supplier_contact_mobilephone
        val reviewed = null
        val qa_person = null
        val qa_way = null
        val reg_capital = supplierInfoBean.registered_capital
        val corporation = supplierInfoBean.legal_representative
        val stat = null
        val creator = supplierInfoBean.create_username
        val create_time = supplierInfoBean.create_time
        val updater = supplierInfoBean.update_username
        val last_update_time = supplierInfoBean.update_time
        val is_available = supplierInfoBean.is_available
        val is_deleted = supplierInfoBean.is_deleted
        val audit_state = supplierInfoBean.audit_state

        (types, (id, org_parent_merchant_id, parent_id, uuid, supplier_name, company_type, address, provinces, city, area), (supplier_classify, supplier_type, contacts, contact_way, reviewed, qa_person, qa_way, reg_capital, corporation, stat), (creator, create_time, updater, last_update_time, is_available, is_deleted, audit_state))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_pro_supplier where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })

  }


  /**
    * 中台的地域信息转到本地的area
    * @param  filterData RDD[SchoolBean] binlog日志数据
    */

  def Area(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("area")).map({
      x =>
        val types = x.`type`
        val areaBean = JSON.parseObject(x.data, classOf[Area])
        val ID = areaBean.ID
        val code = areaBean.code
        val abbreviation = areaBean.abbreviation
        val name = areaBean.name
        val is_available = areaBean.is_available
        val isdeleted = areaBean.IS_DELETED
        val Create_UserName = areaBean.Create_UserName
        val create_time = areaBean.create_time
        val Update_UserName = areaBean.Update_UserName
        val update_time = areaBean.update_time
        (types, ID, code, abbreviation, name, is_available, isdeleted, Create_UserName, create_time, Update_UserName, update_time)
    }).foreach({
      x =>
        if (x._1.equals("insert")) {
          val conn = MysqlUtils.open
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
          statement.execute()
          conn.close()
        } else if (x._1.equals("update")) {
          val conn = MysqlUtils.open
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
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from area where ID='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }


  /**
    *  中台的t_edu_competent_department转到本地的t_edu_competent_department
    *  @param filterData RDD[SchoolBean] binlog日志数据
    */

  def EduCompetentDepartment(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("ssl-user")
      && x.table.equals("t_edu_competent_department")).map({
      x =>
        val eduCompetentDepartmentBean = JSON.parseObject(x.data, classOf[EduCompetentDepartment])
        val types = x.`type`
        val org_merchant_id = eduCompetentDepartmentBean.org_merchant_id
        val org_parent_merchant_id = eduCompetentDepartmentBean.org_parent_merchant_id
        val name = eduCompetentDepartmentBean.name
        val code = eduCompetentDepartmentBean.code
        val uuid = eduCompetentDepartmentBean.uuid
        val parent_id = eduCompetentDepartmentBean.parent_id
        val management_area_type = eduCompetentDepartmentBean.management_area_type
        val create_username = eduCompetentDepartmentBean.create_username
        val create_time = eduCompetentDepartmentBean.create_time
        val update_username = eduCompetentDepartmentBean.update_username
        val update_time = eduCompetentDepartmentBean.update_time
        val is_deleted = eduCompetentDepartmentBean.is_deleted
        (types, org_merchant_id, org_parent_merchant_id, name, code, uuid, parent_id, management_area_type, create_username, create_time, update_username, update_time, is_deleted)

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_competent_department where org_merchant_id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }


  /**
    * 阳光午餐的t_edu_calendar转到本地的t_edu_calendar
    *  @param filterData RDD[SchoolBean] binlog日志数据
    */

  def EduCalendar(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.table.equals("t_edu_calendar")).map({
      x =>
        val eduCalendarBean = JSON.parseObject(x.data, classOf[EduCalendar])
        val types = x.`type`
        val id = eduCalendarBean.id
        val the_day = eduCalendarBean.the_day
        val have_class = eduCalendarBean.have_class
        val create_id = eduCalendarBean.create_id
        val create_name = eduCalendarBean.create_name
        val create_time = eduCalendarBean.create_time
        val updater = eduCalendarBean.updater
        val last_update_time = eduCalendarBean.last_update_time
        val stat = eduCalendarBean.stat
        val school_id = eduCalendarBean.school_id
        val reason = eduCalendarBean.reason
        (types, id, the_day, have_class, create_id, create_name, create_time, updater, last_update_time, stat, school_id, reason)
    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_calendar where id='${x._2}'")
          statement.execute()
          conn.close()
        }

    })
  }

  /**
    *
    *阳光午餐的t_edu_schoolterm转到本地的t_edu_schoolterm
    *@param filterData binlog日志数据
    */
  def EduSchooltrem(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.table.equals("t_edu_schoolterm")).map({
      x =>
        val eduSchooltremBean = JSON.parseObject(x.data, classOf[EduSchooltrem])
        val types = x.`type`
        val id = eduSchooltremBean.id
        val term_year = eduSchooltremBean.term_year
        val school_id = eduSchooltremBean.school_id
        val school_name = eduSchooltremBean.school_name
        val first_start_date = eduSchooltremBean.first_start_date
        val first_end_date = eduSchooltremBean.first_end_date
        val second_start_date = eduSchooltremBean.second_start_date
        val second_end_date = eduSchooltremBean.second_end_date
        val creator = eduSchooltremBean.creator
        val create_time = eduSchooltremBean.create_time
        val updater = eduSchooltremBean.updater
        val last_update_time = eduSchooltremBean.last_update_time
        val stat = eduSchooltremBean.stat
        (types, id, term_year, school_id, school_name, first_start_date, first_end_date, second_start_date, second_end_date, creator, create_time, updater, last_update_time, stat)
    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_schoolterm where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }


  /**
    * *阳光午餐的t_edu_schoolterm_system转到本地的t_edu_schoolterm_system
    *  @param filterData[SchoolBean] binlog日志数据
    */

  def EduSchooltermSystem(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.table.equals("t_edu_schoolterm_system")).map({
      x =>
        val eduSchooltremBean = JSON.parseObject(x.data, classOf[EduSchooltrem])
        val types = x.`type`
        val id = eduSchooltremBean.id
        val term_year = eduSchooltremBean.term_year
        val first_start_date = eduSchooltremBean.first_start_date
        val first_end_date = eduSchooltremBean.first_end_date
        val second_start_date = eduSchooltremBean.second_start_date
        val second_end_date = eduSchooltremBean.second_end_date
        val creator = eduSchooltremBean.creator
        val create_time = eduSchooltremBean.create_time
        val updater = eduSchooltremBean.updater
        val last_update_time = eduSchooltremBean.last_update_time
        val stat = eduSchooltremBean.stat
        (types, id, term_year, first_start_date, first_end_date, second_start_date, second_end_date, creator, create_time, updater, last_update_time, stat)
    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_schoolterm_system where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  /**
    * 阳光午餐的t_edu_holiday转到本地的t_edu_holiday
    *@param filterData binlog日志数据
    */

  def EduHoliday(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.table.equals("t_edu_holiday")).map({
      x =>
        val eduHolidayBean = JSON.parseObject(x.data, classOf[EduHoliday])
        val types = x.`type`
        val id = eduHolidayBean.id
        val name = eduHolidayBean.name
        val holiday_day = eduHolidayBean.holiday_day
        val edutype = eduHolidayBean.`type`
        val create_id = eduHolidayBean.create_id
        val create_name = eduHolidayBean.create_name
        val create_time = eduHolidayBean.create_time
        val updater = eduHolidayBean.updater
        val last_update_time = eduHolidayBean.last_update_time
        val stat = eduHolidayBean.stat
        (types, id, name, holiday_day, edutype, create_id, create_name, create_time, updater, last_update_time, stat)
    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_edu_holiday where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  /**
    * 新b2b2的merchant转到本地的merchant
    *@param filterData[SchoolBean] binlog日志数据
    */

  def Merchant(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("tf-merchant")
      && x.table.equals("merchant"))
      .map({
        x =>
          val merchantBean = JSON.parseObject(x.data, classOf[Merchant])
          val id = merchantBean.id
          val user_id = merchantBean.user_id
          val code = merchantBean.code
          val channel_source = merchantBean.channel_source
          val auth_status = merchantBean.auth_status
          val merchant_type = merchantBean.merchant_type
          val merchant_business_type = merchantBean.merchant_business_type
          val del = merchantBean.del
          val create_id = merchantBean.create_id
          val create_time = merchantBean.create_time
          val last_update_id = merchantBean.last_update_id
          val last_update_time = merchantBean.last_update_time
          val ss_lunch_id = merchantBean.ss_lunch_id
          (x.`type`,id, user_id, code, channel_source, auth_status, merchant_type, merchant_business_type, del, create_id, create_time, last_update_id, last_update_time,ss_lunch_id)
      }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into merchant (id,user_id,code,channel_source,auth_status,merchant_type,merchant_business_type,del,create_id,create_time,last_update_id,last_update_time,ss_lunch_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into merchant (id,user_id,code,channel_source,auth_status,merchant_type,merchant_business_type,del,create_id,create_time,last_update_id,last_update_time,ss_lunch_id) values (?,?,?,?,?,?,?,?,?,?,?,?,?)")
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
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from merchant where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }

  /**
    * 新b2b2的merchant_buyer转到本地的merchant_buyer
    *  @param filterData[SchoolBean] binlog日志数据
    */
  def MerchantBuyer(filterData: RDD[SchoolBean])={
    filterData.filter(x => x != null
      && x.database.equals("tf-merchant")
      && x.table.equals("merchant_buyer"))
      .map({
        x =>
          val merchantBuyerBean = JSON.parseObject(x.data, classOf[MerchantBuyer])
          val id = merchantBuyerBean.id
          val merchant_id = merchantBuyerBean.merchant_id
          val credit_code = merchantBuyerBean.credit_code
          val user_id = merchantBuyerBean.user_id
          val company_name = merchantBuyerBean.company_name
          val company_type = merchantBuyerBean.company_type
          val del = merchantBuyerBean.del
          val create_id = merchantBuyerBean.create_id
          val create_time = merchantBuyerBean.create_time
          val last_update_id = merchantBuyerBean.last_update_id
          val last_update_time = merchantBuyerBean.last_update_time
          (x.`type`,id, merchant_id, credit_code, user_id, company_name, company_type, del, create_id, create_time, last_update_id, last_update_time)
      }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into merchant_buyer (id,merchant_id,credit_code,user_id,company_name,company_type,del,create_id,create_time,last_update_id,last_update_time) values (?,?,?,?,?,?,?,?,?,?,?)")
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
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into merchant_buyer (id,merchant_id,credit_code,user_id,company_name,company_type,del,create_id,create_time,last_update_id,last_update_time) values (?,?,?,?,?,?,?,?,?,?,?)")
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
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from merchant_buyer where id='${x._2}'")
          statement.execute()
          conn.close()
        }
    })
  }


  /**
    * 将新b2b的商户merchant 迁移到 t_pro_supplier 表
    * @param filterData[SchoolBean] binlog日志数据
    */
  def B2bSupplierInfo(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null
      && x.database.equals("tf-merchant")
      && x.table.equals("merchant"))
        .map(x =>(x.`type`,JSON.parseObject(x.data, classOf[Merchant])))
        .filter(x => "2".equals(x._2.merchant_business_type))
      .map({
        case (k,v) =>
        val merchantBean = v
        val types = k //插入，删除，更新操作类型
      val org_merchant_id = merchantBean.id
        val org_parent_merchant_id = null
        val parent_id = null
        val uuid = merchantBean.id
        val supplier_name = merchantBean.company_name
        val company_type = null
        val supplier_classify = null
        val supplier_type = null //b2b的商户只有卖家，可能是团餐公司也可能是供应商
      val provinces = merchantBean.office_province_id
        val city = merchantBean.office_city_id

        val area = NewSchoolToOldSchool.committeeToOldArea(merchantBean.office_region_id)
        val address = merchantBean.office_address //详情地址
      val contacts = merchantBean.contact_person //联系人
      val contact_way = merchantBean.contact_phone //联系人联系方式
      val qa_person = null //质量负责人
      val qa_way = null //质量负责人联系电话
      val reg_address = merchantBean.register_address //注册地址
      val reg_capital = merchantBean.registered_capital //注册资金
      val corporation = merchantBean.legal_representative //法人
        val is_deleted = merchantBean.del
      val reviewed = "1"
          var stat ="0"
        if("0".equals(is_deleted)){
          stat ="1"
        }else{
          stat
        }
        val creator = merchantBean.create_id
        val create_time = merchantBean.create_time
        val updater = merchantBean.last_update_id
        val last_update_time = merchantBean.last_update_time
        val is_available = null

        val audit_state = null
        (types, (org_merchant_id, org_parent_merchant_id, parent_id, uuid, supplier_name, company_type, address, provinces, city, area), (supplier_classify, supplier_type, contacts, contact_way, reviewed, qa_person, qa_way, reg_capital, corporation, stat), (creator, create_time, updater, last_update_time, is_available, is_deleted, audit_state))

    }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
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
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
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
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_pro_supplier where org_merchant_id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })
  }

  /**

    * 将新b2b的买家和卖家关联信息迁移到cooperation_apply表
    *  @param filterData[SchoolBean] binlog日志数据
    */

  def B2bCooperationApply(filterData: RDD[SchoolBean])={
    filterData.filter(x => x != null
      && x.database.equals("tf-merchant")
      && x.table.equals("cooperation_apply"))
      .map(x =>(x.`type`,JSON.parseObject(x.data, classOf[CooperationApply])))
        .map({
          case (k,v) =>
            val cooperationApplyBean = v
            val types = k //插入，删除，更新操作类型
          val id = cooperationApplyBean.id
            val buyer_merchant_id = cooperationApplyBean.buyer_merchant_id
            val seller_merchant_id = cooperationApplyBean.seller_merchant_id
            val status = cooperationApplyBean.status
            val del = cooperationApplyBean.del
            (types, (id, buyer_merchant_id, seller_merchant_id, status,del))

        }).foreach({
      x =>
        if ("insert".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into cooperation_apply (id,buyer_merchant_id,seller_merchant_id,status,del) values (?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.execute()
          conn.close()
        } else if ("update".equals(x._1)) {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement("replace into cooperation_apply (id,buyer_merchant_id,seller_merchant_id,status,del) values (?,?,?,?,?)")
          statement.setString(1, x._2._1)
          statement.setString(2, x._2._2)
          statement.setString(3, x._2._3)
          statement.setString(4, x._2._4)
          statement.setString(5, x._2._5)
          statement.executeUpdate()
          conn.close()
        } else {
          val conn = MysqlUtils.open
          val statement = conn.prepareStatement(s"delete from t_pro_supplier where id='${x._2._1}'")
          statement.execute()
          conn.close()
        }
    })
  }
}
