package com.ssic.report

import com.alibaba.fastjson.JSON
import com.ssic.beans.{Employee, License, SchoolBean}
import com.ssic.report.StopFood.logger
import com.ssic.service.SupplierDetail.logger
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._


object PeopleLicenseDetail {
  private val logger = LoggerFactory.getLogger(this.getClass)
  /**

    * * 员工证件详情插入信息存入到redis的表中

    * * @param RDD[SchoolBean] binlog日志数据

    * * @param Broadcast[Map[String, String]]  项目点关联id 对应的 学校id

    */

  def licenseInsert(filterData: (RDD[SchoolBean],Broadcast[Map[String, String]])) = {
    val licenseData = filterData._1.filter(x => x != null && x.table.equals("t_pro_license") && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data,classOf[License]))
      .filter(x =>  "1".equals(x.stat))
    val employee = filterData._1.filter(x => x != null && x.table.equals("t_pro_employee") && x.`type`.equals("insert") )
      .map(x => JSON.parseObject(x.data,classOf[Employee]))
      .filter(x =>  "1".equals(x.stat))
    val licenseDetailData = licenseData.distinct().map({
      x =>
        val written_name = x.written_name
        val lic_no = x.lic_no
        val lic_type = x.lic_type
        val give_lic_date = x.give_lic_date //证书发证时间
      val lic_end_date = x.lic_end_date //证书有效时间
      val supplier_id = x.supplier_id
        val relation_id = x.relation_id
        val lic_pic = x.lic_pic
        val stat = x.stat

        ( relation_id, List(written_name, lic_type, lic_no, supplier_id, give_lic_date, lic_end_date, stat,lic_pic))

    }).filter(x => !x._1.equals("null")).filter(x => x._2(1).equals("20") || x._2(1).equals("22") || x._2(1).equals("23") || x._2(1).equals("24") || x._2(1).equals("25"))

    val employeeData = employee.distinct().map({
      x =>
        val id = x.id
        val school_supplier_id = x.school_supplier_id
        var id_code_name="null"
        val id_code = x.id_code
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

  /**

    * * 员工证件详情删除信息存入到redis的表中

    * * @param RDD[SchoolBean] binlog日志数据

    * * @param Broadcast[Map[String, String]]  项目点关联id 对应的 学校id

    */


  def licenseDelete(filterData: (RDD[SchoolBean],Broadcast[Map[String, String]])) ={
    val employee = filterData._1.filter(x => x != null && x.table.equals("t_pro_employee") && !x.`type`.equals("insert"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[Employee]),JSON.parseObject(x.old,classOf[Employee])))
    employee.distinct().map({
      case (k,v,z) =>
        val id = v.id
        val school_supplier_id = v.school_supplier_id
        var id_code_name="null"
        val id_code = v.id_code
        if(StringUtils.isNoneEmpty(id_code) && !"null".equals(id_code)){
          id_code_name=id_code
        }else{
          id_code_name
        }
        val stat = v.stat

        var oldStat ="null"

        try {
          oldStat = z.stat
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $z", e)
          }
        }
        if("delete".equals(k) && "1".equals(stat)){
          ("delete",id,school_supplier_id,stat,"null",id_code_name)
        }else if("update".equals(k)){
          ("update",id,school_supplier_id,stat,oldStat,id_code_name)
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

  /**

    * * 员工证件详情更新信息存入到redis的表中

    * * @param RDD[SchoolBean] binlog日志数据

    * * @param Broadcast[Map[String, String]]  项目点关联id 对应的 学校id

    */

  def licenseUpdate(filterData:RDD[SchoolBean]) ={
    val license = filterData.filter(x => x !=null && x.table.equals("t_pro_license") && x.`type`.equals("update"))
      .map(x => (JSON.parseObject(x.data,classOf[License]),JSON.parseObject(x.old,classOf[License])))
    val licenseData = license.distinct().map({
      case (k,v) =>
        val relation_id = k.relation_id
        val written_name = k.written_name
        val lic_no = k.lic_no
        val lic_type = k.lic_type
        val supplier_id = k.supplier_id
        val stat = k.stat
        val lic_pic = k.lic_pic
        val lic_end_date = k.lic_end_date
        val give_lic_date = k.give_lic_date

        var oldStat = "null"
        try {
          oldStat = v.stat
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $v", e)
          }
        }
        (relation_id, lic_type, written_name, lic_no, supplier_id, stat,oldStat,lic_pic,lic_end_date,give_lic_date)
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
