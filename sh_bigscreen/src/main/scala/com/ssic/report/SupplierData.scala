package com.ssic.report

import java.util

import com.alibaba.fastjson.JSON
import com.ssic.beans.{SaasPackage, SchoolBean, SupplierInfo}
import com.ssic.report.DishPlan.format
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Created by 云 on 2018/8/22.
  * 供应商的总数，供应商供应学校数量
  */

object SupplierData {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")



  def SupplierBaseData(filterData: RDD[SchoolBean]) = {
    val supplietData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[SupplierInfo]),JSON.parseObject(x.old,classOf[SupplierInfo])))
    val supplietDelData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier") && x.`type`.equals("delete"))
      .map(x => JSON.parseObject(x.data,classOf[SupplierInfo]))
      .filter(x => !x.stat.equals("0"))
    val supplietUpdate = filterData.filter(x => x != null && x.table.equals("t_pro_supplier") && x.`type`.equals("update"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[SupplierInfo])))

    val supplData: RDD[(String, String, String, String, String)] = supplietData.distinct().filter(x => StringUtils.isNoneEmpty(x._2.supplier_type)).map({
      case (k,v,z) =>
        if ("insert".equals(k) && !"0".equals(v.stat)) {
          ("insert", v.supplier_type, v.stat, "null", "null")
        } else if ("delete".equals(k) && !"0".equals(v.stat)) {
          ("delete", v.supplier_type, v.stat, "null", "null")
        } else {
          ("update", v.supplier_type, v.stat, z.supplier_type, z.stat)
        }
    })

    supplData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1)) {
              jedis.hincrBy("supplier-data", "shanghai" + "_" + "supplier" + "_" + x._2, 1)
            } else if ("delete".equals(x._1)) {
              jedis.hincrBy("supplier-data", "shanghai" + "_" + "supplier" + "_" + x._2, -1)
            } else {
              if (StringUtils.isEmpty(x._5)) {
                if (StringUtils.isEmpty(x._4)) {
                  logger.info("供应商不需要更新的计算。。。。。。。。。。。。。。。。。。。。。。。。。")
                } else {
                  jedis.hincrBy("supplier-data", "shanghai" + "_" + "supplier" + "_" + x._4, -1)
                  jedis.hincrBy("supplier-data", "shanghai" + "_" + "supplier" + "_" + x._2, 1)
                }
              } else {
                if ("1".equals(x._5) && "0".equals(x._3)) {
                  jedis.hincrBy("supplier-data", "shanghai" + "_" + "supplier" + "_" + x._2, -1)
                } else if ("0".equals(x._5) && "1".equals(x._3)) {
                  jedis.hincrBy("supplier-data", "shanghai" + "_" + "supplier" + "_" + x._2, 1)
                } else {
                  logger.info("不符合的供应商数量计算---------------------")
                }
              }
            }
        })
    })

  }

  def SupplierToScholl(suData: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val supplier_school = suData._1.filter(x => x != null && x.table.equals("t_saas_package") )
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[SaasPackage]),JSON.parseObject(x.old,classOf[SaasPackage])))
    val supplier2schoolData = supplier_school.distinct().filter(x => StringUtils.isNoneEmpty(x._2.supply_date)).map({
      case (k,v,z) =>
        val supply_date = v.supply_date.split(" ")(0)
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val supplier_id = v.supplier_id
        if ("insert".equals(k) && "1".equals(v.stat)) {
          ("insert", date, suData._2.value.getOrElse(supplier_id, "null"), v.stat, "null")
        } else if ("delete".equals(k) && "1".equals(v.stat)) {
          ("delete", date, suData._2.value.getOrElse(supplier_id, "null"), v.stat, "null")
        } else if ("update".equals(k)) {
          ("update", date, suData._2.value.getOrElse(supplier_id, "null"), v.stat, z.stat)
        } else {
          ("null", "null", "null", "null", "null")
        }

    }).filter(x => !x._1.equals("null")).filter(x => !x._3.equals("null"))
    supplier2schoolData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1) && "1".equals(x._4)) {
              jedis.hincrBy(x._2 + "_" + "supplierToSchool", x._3, 1)
            } else if ("delete".equals(x._1) && "1".equals(x._4)) {
              jedis.hincrBy(x._2 + "_" + "supplierToSchool", x._3, -1)
            } else if ("update".equals(x._1)) {
              if ("0".equals(x._4) && "1".equals(x._5)) {
                jedis.hincrBy(x._2 + "_" + "supplierToSchool", x._3, -1)
              } else if ("1".equals(x._4) && "0".equals(x._5)) {}
              jedis.hincrBy(x._2 + "_" + "supplierToSchool", x._3, 1)
            }
        })
    })

    //    val supplier_school_data = supplier_school.filter(x => StringUtils.isNoneEmpty(x.data.school_id)).filter(x => StringUtils.isNoneEmpty(x.data.supplier_id)).map({
    //      x =>
    //        if ("insert".equals(x.`type`) && !"0".equals(x.data.stat)) {
    //          ("insert", x.data.id, suData._2.value.getOrElse(x.data.supplier_id, "null"), x.data.school_id, x.data.stat, "null")
    //        } else if ("delete".equals(x.`type`) && !"0".equals(x.data.stat)) {
    //          ("delete", x.data.id, suData._2.value.getOrElse(x.data.supplier_id, "null"), x.data.school_id, x.data.stat, "null")
    //        } else {
    //          (Supplier2schoolupdate(x)._1, Supplier2schoolupdate(x)._2, suData._2.value.getOrElse(Supplier2schoolupdate(x)._3, "null"), Supplier2schoolupdate(x)._4, Supplier2schoolupdate(x)._5, Supplier2schoolupdate(x)._6)
    //        }
    //    })
    //    supplier_school_data.foreachPartition({
    //      itr =>
    //        val jedis = JPools.getJedis
    //        itr.foreach({
    //          x =>
    //            if ("insert".equals(x._1)) {
    //              jedis.hincrBy("supplierToSchool", x._3, 1)
    //            } else if ("delete".equals(x._1)) {
    //              jedis.hincrBy("supplierToSchool", x._3, -1)
    //            } else {
    //              if (StringUtils.isEmpty(x._6)) {
    //                logger.info("供应商对应学校的数量不符合计算...................")
    //              } else {
    //                if ("0".equals(x._5) && "1".equals(x._4)) {
    //                  jedis.hincrBy("supplierToSchool", x._3, 1)
    //                } else if ("1".equals(x._5) && "0".equals(x._4)) {
    //                  jedis.hincrBy("supplierToSchool", x._3, -1)
    //                } else {
    //                  logger.info("供应商对应学校数量不符合数据----------------------" + x)
    //                }
    //              }
    //            }
    //        })
    //    })

  }

}
