package com.ssic.report

import com.ssic.beans.SchoolBean
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD


object GroupSupplierDetail {

  def groupSupplierInsert(filterData: RDD[SchoolBean]) = {
    val supplierData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val licenseData = filterData.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))

    val suppilerDetailData = supplierData.distinct().map({
      x =>
        val id = x.data.id
        val supplier_type = x.data.supplier_type //1团餐公司 2供应商
      val supplier_name = x.data.supplier_name
        val area = x.data.area
        val address = x.data.address //详情地址
      val contact = x.data.contact //联系人
      val contact_way = x.data.contact_way //联系人联系方式
      val qa_person = x.data.qa_person //质量负责人
      val qa_way = x.data.qa_way //质量负责人联系电话
      val reg_address = x.data.reg_address //注册地址
      val reg_capital = x.data.reg_capital //注册资金
      val corporation = x.data.corporation //法人

        (id, List(supplier_type, supplier_name, area, address, contact, contact_way, qa_person, qa_way, reg_address, reg_capital, corporation))
    }).filter(x => x._2(0).equals("1"))

    val licenseDetailData = licenseData.distinct().map({
      x =>
        val supplier_id = x.data.supplier_id
        val lic_type = x.data.lic_type
        val lic_pic = x.data.lic_pic //证书图片
      val job_organization = x.data.job_organization //经营单位
      val operation = x.data.operation //发证机关
      val lic_no = x.data.lic_no //证书号码
      val give_lic_date = x.data.give_lic_date //发证日期
      val lic_end_date = x.data.lic_end_date //证书有效日期截止时间
        (supplier_id, List(lic_type, lic_pic, job_organization, lic_no, give_lic_date, lic_end_date, operation))
    }).filter(x => x._2(0).equals("1") || x._2(0).equals("13") || x._2(0).equals("14") || x._2(0).equals("8"))

    suppilerDetailData.leftOuterJoin(licenseDetailData).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null"))))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              if (x._3(0).equals("null")) {
                jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + "null" + ";" + "1licpic" + ";" + "null" + ";" + "1licjob" + ";" + "null" + ";" + "1licno" + ";" + "null" + ";" + "1licdate" + ";" + "null" + ";" + "1enddate" + ";" + "null" + ";" + "1operation" + ";" + "null" + ";" + "13lictype" + ";" + "null" + ";" + "13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "14lictype" + ";" + "null" + ";" + "14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "8lictype" + ";" + "null" + ";" + "8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null")
              } else {
                if (jedis.hkeys("group-supplier-detail").contains(x._1).equals(true)) {
                  val v = jedis.hget("group-supplier-detail", x._1)
                  if ("1".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + x._3(0) + ";" + "1licpic" + ";" + x._3(1) + ";" + "1licjob" + ";" + x._3(2) + ";" + "1licno" + ";" + x._3(3) + ";" + "1licdate" + ";" + x._3(4) + ";" + "1enddate" + ";" + x._3(5) + ";" + "1operation" + ";" + x._3(6) + ";" + "13lictype" + v.split("13lictype")(1))
                  } else if ("13".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + v.split("1lictype")(1).split("13lictype")(0) + "13lictype" + ";" + x._3(0) + ";" + "13licpic" + ";" + x._3(1) + ";" + "13licjob" + ";" + x._3(2) + ";" + "13licno" + ";" + x._3(3) + ";" + "13licdate" + ";" + x._3(4) + ";" + "13enddate" + ";" + x._3(5) + ";" + "13operation" + ";" + x._3(6) + ";" + "14lictype" + v.split("14lictype")(1))
                  } else if ("14".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + v.split("1lictype")(1).split("14lictype")(0) + "14lictype" + ";" + x._3(0) + ";" + "14licpic" + ";" + x._3(1) + ";" + "14licjob" + ";" + x._3(2) + ";" + "14licno" + ";" + x._3(3) + ";" + "14licdate" + ";" + x._3(4) + ";" + "14enddate" + ";" + x._3(5) + ";" + "14operation" + ";" + x._3(6) + ";" + "8lictype" + v.split("8lictype")(1))
                  } else if ("8".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + v.split("1lictype")(1).split("8lictype")(0) + "8lictype" + ";" + x._3(0) + ";" + "8licpic" + ";" + x._3(1) + ";" + "8licjob" + ";" + x._3(2) + ";" + "8licno" + ";" + x._3(3) + ";" + "8licdate" + ";" + x._3(4) + ";" + "8enddate" + ";" + x._3(5) + ";" + "8operation" + ";" + x._3(6))
                  }
                } else {
                  if ("1".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + x._3(0) + ";" + "1licpic" + ";" + x._3(1) + ";" + "1licjob" + ";" + x._3(2) + ";" + "1licno" + ";" + x._3(3) + ";" + "1licdate" + ";" + x._3(4) + ";" + "1enddate" + ";" + x._3(5) + ";" + "1operation" + ";" + x._3(6) + ";" + "13lictype" + ";" + "null" + ";" + "13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "14lictype" + ";" + "null" + ";" + "14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "8lictype" + ";" + "null" + ";" + "8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null")
                  } else if ("13".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + "null" + ";" + "1licpic" + ";" + "null" + ";" + "1licjob" + ";" + "null" + ";" + "1licno" + ";" + "null" + ";" + "1licdate" + ";" + "null" + ";" + "1enddate" + ";" + "null" + ";" + "1operation" + ";" + "null" + ";" + "13lictype" + ";" + x._3(0) + ";" + "13licpic" + ";" + x._3(1) + ";" + "13licjob" + ";" + x._3(2) + ";" + "13licno" + ";" + x._3(3) + ";" + "13licdate" + ";" + x._3(4) + ";" + "13enddate" + ";" + x._3(5) + ";" + "13operation" + ";" + x._3(6) + ";" + "14lictype" + ";" + "null" + ";" + "14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "8lictype" + ";" + "null" + ";" + "8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null")
                  } else if ("14".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + "null" + ";" + "1licpic" + ";" + "null" + ";" + "1licjob" + ";" + "null" + ";" + "1licno" + ";" + "null" + ";" + "1licdate" + ";" + "null" + ";" + "1enddate" + ";" + "null" + ";" + "1operation" + ";" + "null" + ";" + "13lictype" + ";" + "null" + ";" + "13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "14lictype" + ";" + x._3(0) + ";" + "14licpic" + ";" + x._3(1) + ";" + "14licjob" + ";" + x._3(2) + ";" + "14licno" + ";" + x._3(3) + ";" + "14licdate" + ";" + x._3(4) + ";" + "14enddate" + ";" + x._3(5) + ";" + "14operation" + ";" + x._3(6) + ";" + "8lictype" + ";" + "null" + ";" + "8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null")
                  } else if ("8".equals(x._3(0))) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + x._3(0) + ";" + "1licpic" + ";" + x._3(1) + ";" + "1licjob" + ";" + x._3(2) + ";" + "1licno" + ";" + x._3(3) + ";" + "1licdate" + ";" + x._3(4) + ";" + "1enddate" + ";" + x._3(5) + ";" + "1operation" + ";" + x._3(6) + ";" + "13lictype" + ";" + "null" + ";" + "13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "14lictype" + ";" + "null" + ";" + "14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "8lictype" + ";" + x._3(0) + ";" + "8licpic" + ";" + x._3(1) + ";" + "8licjob" + ";" + x._3(2) + ";" + "8licno" + ";" + x._3(3) + ";" + "8licdate" + ";" + x._3(4) + ";" + "8enddate" + ";" + x._3(5) + ";" + "8operation" + ";" + x._3(6))
                  }
                }
              }

          })
      })

  }


  def groupSupplierUpDe(filterData: RDD[SchoolBean]) = {
    val supplierData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier") && !x.`type`.equals("insert"))
    val suppilerDetailData = supplierData.distinct().map({
      x =>
        val id = x.data.id
        val supplier_type = x.data.supplier_type //1团餐公司 2供应商
      val supplier_name = x.data.supplier_name
        val area = x.data.area
        val address = x.data.address //详情地址
      val contact = x.data.contact //联系人
      val contact_way = x.data.contact_way //联系人联系方式
      val qa_person = x.data.qa_person //质量负责人
      val qa_way = x.data.qa_way //质量负责人联系电话
      val reg_address = x.data.reg_address //注册地址
      val reg_capital = x.data.reg_capital //注册资金
      val corporation = x.data.corporation //法人
      val stat = x.data.stat


        if ("delete".equals(x.`type`)) {
          ("delete", id, supplier_type, supplier_name, area, address, contact, contact_way, qa_person, qa_way, reg_address, reg_capital, corporation, stat, "null")
        } else if ("update".equals(x.`type`)) {
          ("update", id, supplier_type, supplier_name, area, address, contact, contact_way, qa_person, qa_way, reg_address, reg_capital, corporation, stat, x.old.stat)
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
        }

    }).filter(x => x._3.equals("1")).filter(x => !x._1.equals("null"))

    suppilerDetailData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>

              if ("delete".equals(x._1)) {
                jedis.hdel("group-supplier-detail", x._2)
              } else if ("update".equals(x._1)) {
                if (StringUtils.isNoneEmpty(x._15) && !x._15.equals("null")) {
                  if ("1".equals(x._15) && "0".equals(x._14)) {
                    jedis.hdel("group-supplier-detail", x._2)
                  } else if ("0".equals(x._15) && "1".equals(x._14)) {
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) + ";" + "1lictype" + ";" + "null" + ";" + "1licpic" + ";" + "null" + ";" + "1licjob" + ";" + "null" + ";" + "1licno" + ";" + "null" + ";" + "1licdate" + ";" + "null" + ";" + "1enddate" + ";" + "null" + ";" + "1operation" + ";" + "null" + ";" + "13lictype" + ";" + "null" + ";" + "13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "14lictype" + ";" + "null" + ";" + "14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "8lictype" + ";" + "null" + ";" + "8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null")
                  }
                } else if (StringUtils.isEmpty(x._15) && x._15.equals("null")) {
                  val v = jedis.hget("group-supplier-detail", x._2)
                  if(StringUtils.isNoneEmpty(v) && !v.equals("null")){
                    jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._2 + ";" + "suppliername" + ";" + x._4 + ";" + "area" + ";" + x._5 + ";" + "address" + ";" + x._6 + ";" + "contact" + ";" + x._7 + ";" + "contactway" + ";" + x._8 + ";" + "qaperson" + ";" + x._9 + ";" + "qaway" + ";" + x._10 + ";" + "regaddress" + ";" + x._11 + ";" + "regcapital" + ";" + x._12 + ";" + "corporation" + ";" + x._13 + ";" + "1lictype" + v.split("1lictype")(1))
                  }
                }
              }

        })
    })
  }

}
