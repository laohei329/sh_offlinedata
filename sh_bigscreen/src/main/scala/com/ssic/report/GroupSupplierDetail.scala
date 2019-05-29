package com.ssic.report

import com.ssic.beans.SchoolBean
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD


object GroupSupplierDetail {

  def groupSupplierInsert(filterData: RDD[SchoolBean]) = {
    val supplierData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier") && x.`type`.equals("insert") && x.data.stat.equals("1") && x.data.company_type != 1 && x.data.supplier_type.equals("1"))


    val suppilerDetailData = supplierData.distinct().map({
      x =>
        val id = x.data.id
        val supplier_type = x.data.supplier_type //1团餐公司 2供应商
      val supplier_name = x.data.supplier_name
        val area = x.data.district_id
        val address = x.data.address //详情地址
      val contact = x.data.contact //联系人
      val contact_way = x.data.contact_way //联系人联系方式
      val qa_person = x.data.qa_person //质量负责人
      val qa_way = x.data.qa_way //质量负责人联系电话
      val reg_address = x.data.reg_address //注册地址
      val reg_capital = x.data.reg_capital //注册资金
      val corporation = x.data.corporation //法人

        (id, List(supplier_type, supplier_name, area, address, contact, contact_way, qa_person, qa_way, reg_address, reg_capital, corporation))
    })

    suppilerDetailData//.leftOuterJoin(licenseDetailData).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null"))))
      .foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._1 + ";" + "suppliername" + ";" + x._2(1) + ";" + "area" + ";" + x._2(2) + ";" + "address" + ";" + x._2(3) + ";" + "contact" + ";" + x._2(4) + ";" + "contactway" + ";" + x._2(5) + ";" + "qaperson" + ";" + x._2(6) + ";" + "qaway" + ";" + x._2(7) + ";" + "regaddress" + ";" + x._2(8) + ";" + "regcapital" + ";" + x._2(9) + ";" + "corporation" + ";" + x._2(10) )

        })
    })

  }


  def groupSupplierUpDe(filterData: RDD[SchoolBean]) = {
    val supplierData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier") && !x.`type`.equals("insert") && x.data.company_type != 1 && x.data.supplier_type.equals("1"))
    val suppilerDetailData = supplierData.distinct().map({
      x =>
        val id = x.data.id
        val supplier_type = x.data.supplier_type //1团餐公司 2供应商
      val supplier_name = x.data.supplier_name
        val area = x.data.district_id
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
                  jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._2 + ";" + "suppliername" + ";" + x._4 + ";" + "area" + ";" + x._5 + ";" + "address" + ";" + x._6 + ";" + "contact" + ";" + x._7 + ";" + "contactway" + ";" + x._8 + ";" + "qaperson" + ";" + x._9 + ";" + "qaway" + ";" + x._10 + ";" + "regaddress" + ";" + x._11 + ";" + "regcapital" + ";" + x._12 + ";" + "corporation" + ";" + x._13 )
                }
              } else {
                val v = jedis.hget("group-supplier-detail", x._2)
                if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
                  jedis.hset("group-supplier-detail", x._1, "id" + ";" + x._2 + ";" + "suppliername" + ";" + x._4 + ";" + "area" + ";" + x._5 + ";" + "address" + ";" + x._6 + ";" + "contact" + ";" + x._7 + ";" + "contactway" + ";" + x._8 + ";" + "qaperson" + ";" + x._9 + ";" + "qaway" + ";" + x._10 + ";" + "regaddress" + ";" + x._11 + ";" + "regcapital" + ";" + x._12 + ";" + "corporation" + ";" + x._13 )
                }
              }
            }

        })
    })
  }


  def groupSupplierImage(filterData: RDD[SchoolBean]) ={

    val licenseData = filterData.filter(x => x != null && x.table.equals("t_pro_license"))
    val licenseDetailData = licenseData.distinct().map({
      x =>
        var supplier_id_name="null"
        val supplier_id = x.data.supplier_id
        if(StringUtils.isNoneEmpty(supplier_id) && !supplier_id.equals("null")){
          supplier_id_name=supplier_id
        }else{
          supplier_id_name
        }
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

        var operation_name="null"
        val operation = x.data.operation
        if(StringUtils.isNoneEmpty(operation) && !operation.equals("null")){
          operation_name=operation
        }else{
          operation_name
        }

        var lic_no_name="null"
        val lic_no = x.data.lic_no
        if(StringUtils.isNoneEmpty(lic_no) && !lic_no.equals("null")){
          lic_no_name=lic_no
        }else{
          lic_no_name
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

        var occupation_range_name="null"
        val occupation_range = x.data.occupation_range //从业范围
        if(StringUtils.isNoneEmpty(occupation_range) && !occupation_range.equals("null")){
          occupation_range_name=occupation_range
        }else{
          occupation_range_name
        }
        val stat = x.data.stat
        if ("delete".equals(x.`type`) && "1".equals(stat)) {
          ("delete", supplier_id_name, lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name,occupation_range_name, stat,"null")
        } else if ("update".equals(x.`type`)) {
          ("update", supplier_id_name, lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name,occupation_range_name, stat,x.old.stat)
        }else if("insert".equals(x.`type`) && "1".equals(x.data.stat)){
          ("insert", supplier_id_name, lic_type, lic_pic_name, job_organization_name, lic_no_name, operation_name, give_lic_date_name, lic_end_date_name,occupation_range_name, stat,"null")
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null","null")
        }
    }).filter(x => !x._1.equals("null")).filter(x => x._3.equals("1") || x._3.equals("13") || x._3.equals("14") || x._3.equals("8") || x._3.equals("0") || x._3.equals("4")).filter(x => !x._2.equals("null"))

    licenseDetailData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hget("group-supplier-detail", x._2)
            if(StringUtils.isNoneEmpty(v) && !v.equals("null")){
              if("0".equals(x._3)){
                if("delete".equals(x._1)){
                  jedis.hset("group-supplier-detail",x._2,v.split("0lictype")(0)+"0lictype"+";"+"null"+ ";"+"0licpic" + ";" + "null" + ";" + "0licjob" + ";" + "null" + ";" + "0licno" + ";" + "null" + ";" + "0licdate" + ";" + "null" + ";" + "0enddate" + ";" + "null" + ";" + "0operation" + ";" + "null" + ";" + "0range" + ";" + "null" + ";" + "1lictype"+v.split("1lictype")(1))
                }else if("insert".equals(x._1) && "1".equals(x._11)){
                  jedis.hset("group-supplier-detail",x._2,v.split("0lictype")(0)+"0lictype"+";"+x._3+ ";"+"0licpic" + ";" + x._4 + ";" + "0licjob" + ";" + x._5 + ";" + "0licno" + ";" + x._6 + ";" + "0licdate" + ";" + x._8 + ";" + "0enddate" + ";" + x._9 + ";" + "0operation" + ";" + x._7 + ";" + "0range" + ";" + x._10 + ";" + "1lictype"+v.split("1lictype")(1))
                } else if("update".equals(x._1)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")){
                    if ("1".equals(x._12) && "0".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("0lictype")(0)+"0lictype"+";"+"null"+ ";"+"0licpic" + ";" + "null" + ";" + "0licjob" + ";" + "null" + ";" + "0licno" + ";" + "null" + ";" + "0licdate" + ";" + "null" + ";" + "0enddate" + ";" + "null" + ";" + "0operation" + ";" + "null" + ";" + "0range" + ";" + "null" + ";" + "1lictype"+v.split("1lictype")(1))
                    }else if ("0".equals(x._12) && "1".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("0lictype")(0)+"0lictype"+";"+x._3+ ";"+"0licpic" + ";" + x._4 + ";" + "0licjob" + ";" + x._5 + ";" + "0licno" + ";" + x._6 + ";" + "0licdate" + ";" + x._8 + ";" + "0enddate" + ";" + x._9 + ";" + "0operation" + ";" + x._7 + ";" + "0range" + ";" + x._10 + ";" + "1lictype"+v.split("1lictype")(1))
                    }
                  }else{
                    jedis.hset("group-supplier-detail",x._2,v.split("0lictype")(0)+"0lictype"+";"+x._3+ ";"+"0licpic" + ";" + x._4 + ";" + "0licjob" + ";" + x._5 + ";" + "0licno" + ";" + x._6 + ";" + "0licdate" + ";" + x._8 + ";" + "0enddate" + ";" + x._9 + ";" + "0operation" + ";" + x._7 + ";" + "0range" + ";" + x._10 + ";" + "1lictype"+v.split("1lictype")(1))
                  }
                }
              } else if("1".equals(x._3)){
                if("delete".equals(x._1)){
                  jedis.hset("group-supplier-detail",x._2,v.split("1lictype")(0)+"1lictype"+";"+"null"+ ";"+"1licpic" + ";" + "null" + ";" + "1licjob" + ";" + "null" + ";" + "1licno" + ";" + "null" + ";" + "1licdate" + ";" + "null" + ";" + "1enddate" + ";" + "null" + ";" + "1operation" + ";" + "null" + ";" + "1range" + ";" + "null" + ";" + "4lictype"+v.split("4lictype")(1))
                }else if("insert".equals(x._1) && "1".equals(x._11)){
                  jedis.hset("group-supplier-detail",x._2,v.split("1lictype")(0)+"1lictype"+";"+x._3+ ";"+"1licpic" + ";" + x._4 + ";" + "1licjob" + ";" + x._5 + ";" + "1licno" + ";" + x._6 + ";" + "1licdate" + ";" + x._8 + ";" + "1enddate" + ";" + x._9 + ";" + "1operation" + ";" + x._7 + ";" + "1range" + ";" + x._10 + ";" + "4lictype"+v.split("4lictype")(1))
                } else if("update".equals(x._1)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")){
                    if ("1".equals(x._12) && "0".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("1lictype")(0)+"1lictype"+";"+"null"+ ";"+"1licpic" + ";" + "null" + ";" + "1licjob" + ";" + "null" + ";" + "1licno" + ";" + "null" + ";" + "1licdate" + ";" + "null" + ";" + "1enddate" + ";" + "null" + ";" + "1operation" + ";" + "null" + ";" + "1range" + ";" + "null" + ";" + "4lictype"+v.split("4lictype")(1))
                    }else if ("0".equals(x._12) && "1".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("1lictype")(0)+"1lictype"+";"+x._3+ ";"+"1licpic" + ";" + x._4 + ";" + "1licjob" + ";" + x._5 + ";" + "1licno" + ";" + x._6 + ";" + "1licdate" + ";" + x._8 + ";" + "1enddate" + ";" + x._9 + ";" + "1operation" + ";" + x._7 + ";" + "1range" + ";" + x._10 + ";" + "4lictype"+v.split("4lictype")(1))
                    }
                  }else{
                    jedis.hset("group-supplier-detail",x._2,v.split("1lictype")(0)+"1lictype"+";"+x._3+ ";"+"1licpic" + ";" + x._4 + ";" + "1licjob" + ";" + x._5 + ";" + "1licno" + ";" + x._6 + ";" + "1licdate" + ";" + x._8 + ";" + "1enddate" + ";" + x._9 + ";" + "1operation" + ";" + x._7 + ";" + "1range" + ";" + x._10 + ";" + "4lictype"+v.split("4lictype")(1))
                  }
                }
              } else if("4".equals(x._3)){
                if("delete".equals(x._1)){
                  jedis.hset("group-supplier-detail",x._2,v.split("4lictype")(0)+"4lictype"+";"+"null"+ ";"+"4licpic" + ";" + "null" + ";" + "4licjob" + ";" + "null" + ";" + "4licno" + ";" + "null" + ";" + "4licdate" + ";" + "null" + ";" + "4enddate" + ";" + "null" + ";" + "4operation" + ";" + "null" + ";" + "4range" + ";" + "null" + ";" + "13lictype"+v.split("13lictype")(1))
                }else if("insert".equals(x._1) && "1".equals(x._11)){
                  jedis.hset("group-supplier-detail",x._2,v.split("4lictype")(0)+"4lictype"+";"+x._3+ ";"+"4licpic" + ";" + x._4 + ";" + "4licjob" + ";" + x._5 + ";" + "4licno" + ";" + x._6 + ";" + "4licdate" + ";" + x._8 + ";" + "4enddate" + ";" + x._9 + ";" + "4operation" + ";" + x._7 + ";" + "4range" + ";" + x._10 + ";" + "13lictype"+v.split("13lictype")(1))
                } else if("update".equals(x._1)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")){
                    if ("1".equals(x._12) && "0".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("4lictype")(0)+"4lictype"+";"+"null"+ ";"+"4licpic" + ";" + "null" + ";" + "4licjob" + ";" + "null" + ";" + "4licno" + ";" + "null" + ";" + "4licdate" + ";" + "null" + ";" + "4enddate" + ";" + "null" + ";" + "4operation" + ";" + "null" + ";" + "4range" + ";" + "null" + ";" + "13lictype"+v.split("13lictype")(1))
                    }else if ("0".equals(x._12) && "1".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("4lictype")(0)+"4lictype"+";"+x._3+ ";"+"4licpic" + ";" + x._4 + ";" + "4licjob" + ";" + x._5 + ";" + "4licno" + ";" + x._6 + ";" + "4licdate" + ";" + x._8 + ";" + "4enddate" + ";" + x._9 + ";" + "4operation" + ";" + x._7 + ";" + "4range" + ";" + x._10 + ";" + "13lictype"+v.split("13lictype")(1))
                    }
                  }else{
                    jedis.hset("group-supplier-detail",x._2,v.split("4lictype")(0)+"4lictype"+";"+x._3+ ";"+"4licpic" + ";" + x._4 + ";" + "4licjob" + ";" + x._5 + ";" + "4licno" + ";" + x._6 + ";" + "4licdate" + ";" + x._8 + ";" + "4enddate" + ";" + x._9 + ";" + "4operation" + ";" + x._7 + ";" + "4range" + ";" + x._10 + ";" + "13lictype"+v.split("13lictype")(1))
                  }
                }
              } else if("13".equals(x._3)){
                if("delete".equals(x._1)){
                  jedis.hset("group-supplier-detail",x._2,v.split("13lictype")(0)+"13lictype"+";"+"null"+ ";"+"13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "13range" + ";" + "null" + ";" + "14lictype"+v.split("14lictype")(1))
                }else if("insert".equals(x._1) && "1".equals(x._11)){
                  jedis.hset("group-supplier-detail",x._2,v.split("13lictype")(0)+"13lictype"+";"+x._3+ ";"+"13licpic" + ";" + x._4 + ";" + "13licjob" + ";" + x._5 + ";" + "13licno" + ";" + x._6 + ";" + "13licdate" + ";" + x._8 + ";" + "13enddate" + ";" + x._9 + ";" + "13operation" + ";" + x._7 + ";" + "13range" + ";" + x._10 + ";" + "14lictype"+v.split("14lictype")(1))
                } else if("update".equals(x._1)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")){
                    if ("1".equals(x._12) && "0".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("13lictype")(0)+"13lictype"+";"+"null"+ ";"+"13licpic" + ";" + "null" + ";" + "13licjob" + ";" + "null" + ";" + "13licno" + ";" + "null" + ";" + "13licdate" + ";" + "null" + ";" + "13enddate" + ";" + "null" + ";" + "13operation" + ";" + "null" + ";" + "13range" + ";" + "null" + ";" + "14lictype"+v.split("14lictype")(1))
                    }else if ("0".equals(x._12) && "1".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("13lictype")(0)+"13lictype"+";"+x._3+ ";"+"13licpic" + ";" + x._4 + ";" + "13licjob" + ";" + x._5 + ";" + "13licno" + ";" + x._6 + ";" + "13licdate" + ";" + x._8 + ";" + "13enddate" + ";" + x._9 + ";" + "13operation" + ";" + x._7 + ";" + "13range" + ";" + x._10 + ";" + "14lictype"+v.split("14lictype")(1))
                    }
                  }else{
                    jedis.hset("group-supplier-detail",x._2,v.split("13lictype")(0)+"13lictype"+";"+x._3+ ";"+"13licpic" + ";" + x._4 + ";" + "13licjob" + ";" + x._5 + ";" + "13licno" + ";" + x._6 + ";" + "13licdate" + ";" + x._8 + ";" + "13enddate" + ";" + x._9 + ";" + "13operation" + ";" + x._7 + ";" + "13range" + ";" + x._10 + ";" + "14lictype"+v.split("14lictype")(1))
                  }
                }
              }else if("14".equals(x._3)){
                if("delete".equals(x._1)){
                  jedis.hset("group-supplier-detail",x._2,v.split("14lictype")(0)+"14lictype"+";"+"null"+ ";"+"14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "14range" + ";" + "null" + ";" + "8lictype"+v.split("8lictype")(1))
                }else if("insert".equals(x._1) && "1".equals(x._11)){
                  jedis.hset("group-supplier-detail",x._2,v.split("14lictype")(0)+"14lictype"+";"+x._3+ ";"+"14licpic" + ";" + x._4 + ";" + "14licjob" + ";" + x._5 + ";" + "14licno" + ";" + x._6 + ";" + "14licdate" + ";" + x._8 + ";" + "14enddate" + ";" + x._9 + ";" + "14operation" + ";" + x._7 + ";" + "14range" + ";" + x._10 + ";" + "8lictype"+v.split("8lictype")(1))
                } else if("update".equals(x._1)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")){
                    if ("1".equals(x._12) && "0".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("14lictype")(0)+"14lictype"+";"+"null"+ ";"+"14licpic" + ";" + "null" + ";" + "14licjob" + ";" + "null" + ";" + "14licno" + ";" + "null" + ";" + "14licdate" + ";" + "null" + ";" + "14enddate" + ";" + "null" + ";" + "14operation" + ";" + "null" + ";" + "14range" + ";" + "null" + ";" + "8lictype"+v.split("8lictype")(1))
                    }else if ("0".equals(x._12) && "1".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("14lictype")(0)+"14lictype"+";"+x._3+ ";"+"14licpic" + ";" + x._4 + ";" + "14licjob" + ";" + x._5 + ";" + "14licno" + ";" + x._6 + ";" + "14licdate" + ";" + x._8 + ";" + "14enddate" + ";" + x._9 + ";" + "14operation" + ";" + x._7 + ";" + "14range" + ";" + x._10 + ";" + "8lictype"+v.split("8lictype")(1))
                    }
                  }else{
                    jedis.hset("group-supplier-detail",x._2,v.split("14lictype")(0)+"14lictype"+";"+x._3+ ";"+"14licpic" + ";" + x._4 + ";" + "14licjob" + ";" + x._5 + ";" + "14licno" + ";" + x._6 + ";" + "14licdate" + ";" + x._8 + ";" + "14enddate" + ";" + x._9 + ";" + "14operation" + ";" + x._7 + ";" + "14range" + ";" + x._10 + ";" + "8lictype"+v.split("8lictype")(1))
                  }
                }
              }else if("8".equals(x._3)){
                if("delete".equals(x._1)){
                  jedis.hset("group-supplier-detail",x._2,v.split("8lictype")(0)+"8lictype"+";"+"null"+ ";"+"8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null" + ";" + "8range" + ";" + "null" )
                }else if("insert".equals(x._1) && "1".equals(x._11)){
                  jedis.hset("group-supplier-detail",x._2,v.split("8lictype")(0)+"8lictype"+";"+x._3+ ";"+"8licpic" + ";" + x._4 + ";" + "8licjob" + ";" + x._5 + ";" + "8licno" + ";" + x._6 + ";" + "8licdate" + ";" + x._8 + ";" + "8enddate" + ";" + x._9 + ";" + "8operation" + ";" + x._7 + ";" + "8range" + ";" + x._10 )
                } else if("update".equals(x._1)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")){
                    if ("1".equals(x._12) && "0".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("8lictype")(0)+"8lictype"+";"+"null"+ ";"+"8licpic" + ";" + "null" + ";" + "8licjob" + ";" + "null" + ";" + "8licno" + ";" + "null" + ";" + "8licdate" + ";" + "null" + ";" + "8enddate" + ";" + "null" + ";" + "8operation" + ";" + "null" + ";" + "8range" + ";" + "null" )
                    }else if ("0".equals(x._12) && "1".equals(x._11)){
                      jedis.hset("group-supplier-detail",x._2,v.split("8lictype")(0)+"8lictype"+";"+x._3+ ";"+"8licpic" + ";" + x._4 + ";" + "8licjob" + ";" + x._5 + ";" + "8licno" + ";" + x._6 + ";" + "8licdate" + ";" + x._8 + ";" + "8enddate" + ";" + x._9 + ";" + "8operation" + ";" + x._7 + ";" + "8range" + ";" + x._10 )
                    }
                  }else{
                    jedis.hset("group-supplier-detail",x._2,v.split("8lictype")(0)+"8lictype"+";"+x._3+ ";"+"8licpic" + ";" + x._4 + ";" + "8licjob" + ";" + x._5 + ";" + "8licno" + ";" + x._6 + ";" + "8licdate" + ";" + x._8 + ";" + "8enddate" + ";" + x._9 + ";" + "8operation" + ";" + x._7 + ";" + "8range" + ";" + x._10 )
                  }
                }
              }
            }

        })
    })
  }


}
