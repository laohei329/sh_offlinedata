package com.ssic.report

import com.ssic.beans.SchoolBean
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._


object PeopleLicenseDetail {
  private val logger = LoggerFactory.getLogger(this.getClass)
  def licenseInsert(filterData: (RDD[SchoolBean],Broadcast[Map[String, String]])) = {
    val licenseData = filterData._1.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val employee = filterData._1.filter(x => x != null && x.table.equals("t_pro_employee") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val licenseDetailData = licenseData.distinct().map({
      x =>
        val written_name = x.data.written_name
        val lic_no = x.data.lic_no
        val lic_type = x.data.lic_type
        val give_lic_date = x.data.give_lic_date //证书发证时间
      val lic_end_date = x.data.lic_end_date //证书有效时间
      val supplier_id = x.data.supplier_id
        val relation_id = x.data.relation_id
        val lic_pic = x.data.lic_pic
        val stat = x.data.stat

        ( relation_id, List(written_name, lic_type, lic_no, supplier_id, give_lic_date, lic_end_date, stat,lic_pic))

    }).filter(x => !x._1.equals("null")).filter(x => x._2(1).equals("20") || x._2(1).equals("22") || x._2(1).equals("23") || x._2(1).equals("24") || x._2(1).equals("25"))

    val employeeData = employee.distinct().map({
      x =>
        val id = x.data.id
        val school_supplier_id = x.data.school_supplier_id
        var id_code_name="null"
        val id_code = x.data.id_code
        if(StringUtils.isNoneEmpty(id_code) && !"null".equals(id_code)){
          id_code_name=id_code
        }else{
          id_code_name
        }

        (id, List(school_supplier_id,id_code_name))
    })

    licenseDetailData.leftOuterJoin(employeeData).map(x => (x._1,x._2._1,x._2._2.getOrElse(List("null")))).filter(x => !x._3(0).equals("null"))
        .map(x =>(x._1,x._2,x._3,filterData._2.value.getOrElse(x._3(0),"null"))).filter(x => !x._4.equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              jedis.hset("people-license", x._2(1)+"_"+x._1,"name"+";"+x._2(0)+";"+"idcode"+";"+x._3(1)+";"+"supplierid"+";"+x._2(3)+";"+"schoolid"+";"+x._4+";"+"lictype"+";"+x._2(1)+";"+"licno"+";"+x._2(2)+";"+"licdate"+";"+x._2(4)+";"+"enddate"+";"+x._2(5)+";"+"pic"+";"+x._2(7))
          })
      })
  }


  def licenseDelete(filterData: (RDD[SchoolBean],Broadcast[Map[String, String]])) ={
    val employee = filterData._1.filter(x => x != null && x.table.equals("t_pro_employee") && !x.`type`.equals("insert"))
    employee.distinct().map({
      x =>
        val id = x.data.id
        val school_supplier_id = x.data.school_supplier_id
        var id_code_name="null"
        val id_code = x.data.id_code
        if(StringUtils.isNoneEmpty(id_code) && !"null".equals(id_code)){
          id_code_name=id_code
        }else{
          id_code_name
        }
        val stat = x.data.stat
        if("delete".equals(x.`type`) && "1".equals(stat)){
          ("delete",id,school_supplier_id,stat,"null",id_code_name)
        }else if("update".equals(x.`type`)){
          ("update",id,school_supplier_id,stat,x.old.stat,id_code_name)
        }else{
          ("null","null","null","null","null","null")
        }

    }).filter(x => !x._1.equals("null")).map(x=>(x._1,x._2,x._3,x._4,x._5,x._6,filterData._2.value.getOrElse(x._3,"null"))).filter(x => !x._7.equals("null"))
      .foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val v = jedis.hkeys("people-license")
            val strings = v.asScala
            for (i <- strings){
              val id = i.split("_")(1)
              if(id.equals(x._2)){
                if("delete".equals(x._1)){
                  jedis.hdel("people-license",i)
                }else if("update".equals(x._1)){
                  if(StringUtils.isNoneEmpty(x._5) && !x._5.equals("null")){
                    if("0".equals(x._4) && "1".equals(x._5)){
                      jedis.hdel("people-license",i)
                    }
                  }else{
                    val v = jedis.hget("people-license",i)
                    jedis.hset("people-license",i,v.split("idcode")(0)+"idcode"+";"+x._6+";"+"supplierid"+v.split("supplierid")(1).split("schoolid")(0)+"schoolid"+";"+x._7+";"+"lictype"+v.split("lictype")(1))
                  }
                }

              }
            }

        })
    })
  }

  def licenseUpdate(filterData:RDD[SchoolBean]) ={
    val license = filterData.filter(x => x !=null && x.table.equals("t_pro_license") && x.`type`.equals("update"))
    val licenseData = license.distinct().map({
      x =>
        val relation_id = x.data.relation_id
        val written_name = x.data.written_name
        val lic_no = x.data.lic_no
        val lic_type = x.data.lic_type
        val supplier_id = x.data.supplier_id
        val stat = x.data.stat
        val lic_pic = x.data.lic_pic
        val lic_end_date = x.data.lic_end_date
        val give_lic_date = x.data.give_lic_date
        (relation_id, lic_type, written_name, lic_no, supplier_id, stat,x.old.stat,lic_pic,lic_end_date,give_lic_date)
    }).filter(x =>  x._2.equals("20") || x._2.equals("22") || x._2.equals("23") || x._2.equals("24") || x._2.equals("25"))

    licenseData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if(StringUtils.isNoneEmpty(x._7) && !x._7.equals("null")){
              if("0".equals(x._6) && "1".equals(x._7)){
                jedis.hdel("people-license",x._2+"_"+x._1)
              }
            }else{
              val v = jedis.hget("people-license",x._2+"_"+x._1)
              if(StringUtils.isNoneEmpty(v) && !v.equals("null")){
                logger.info("人員證件"+v)
                jedis.hset("people-license",x._2+"_"+x._1,"name"+";"+x._3+";"+"idcode"+v.split("idcode")(1).split("supplierid")(0)+"supplierid"+";"+x._5+";"+"schoolid"+v.split("schoolid")(1).split("lictype")(0)+"lictype"+";"+x._2+";"+"licno"+";"+x._4+";"+"licdate"+";"+x._10+";"+"enddate"+";"+x._9+";"+"pic"+";"+x._8)
              }
            }
        })
    })
  }

}
