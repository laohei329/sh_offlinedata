package com.ssic.service

import com.alibaba.fastjson.JSON
import com.ssic.beans.{SchoolBean, SupplierInfo}
import com.ssic.report.Receyler.logger
import com.ssic.utils.{JPools, NewSchoolToOldSchool, Rule}
import org.apache.commons.lang3._
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

object SupplierDetail {
  private val logger = LoggerFactory.getLogger(this.getClass)

  /**
    *
    * * 供应商数据存入到redis的表中
    *
    * * @param RDD[SchoolBean] binlog日志数据
    *
    */

  def supplierDetail(filterData: RDD[SchoolBean]) = {
    val supplietData = filterData.filter(x => x != null
      && x.database.equals("opms")
      && x.table.equals("supplier_info"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[SupplierInfo]), JSON.parseObject(x.old, classOf[SupplierInfo])))
    val supplierDetail = supplietData.distinct().map({
      case (k, v, z) =>
        var id = "null"
        if ("12".equals(v.company_id) && StringUtils.isNoneEmpty(v.uuid) && !"null".equals(v.uuid)) {
          id = v.uuid
        } else {
          id = v.id
        }
        val supplier_name = v.supplier_name
        val supplier_type = "2" // 1团餐公司 2供应商
        val supplier_classify = v.supplier_classify // '供应商分类  0 不区分 1生产类 2 经销类',
        val region_id = Rule.emptyToNull(v.region_id)
        //  将上海市各区的新的district_id映射 => 旧的area的映射
        val district_id = NewSchoolToOldSchool.committeeToOldArea(region_id)
        val address = Rule.emptyToNull(v.address)
        val business_license = Rule.emptyToNull(v.social_credit_code) //营业执照号码
        val reg_capital = Rule.emptyToNull(v.registered_capital) //注册资金
        val food_business_code = "null" //食品经营许可证号
        val food_circulation_code = "null" //食品流通证号
        val food_produce_code = "null" //食品生产证号
        //新的中台数据没有stat这个字段
        var stat = "null"
        if ("1".equals(v.is_available) && "0".equals(v.is_deleted)) {
          stat = "1"
        } else {
          stat = "0"
        }

        var oldStat = "null"

        try {
          oldStat = z.stat
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $z", e)
          }
        }

        if ("insert".equals(k) && "1".equals(stat)) {
          ("insert", id, supplier_name, supplier_type, supplier_classify, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat, "null")
        } else if ("delete".equals(k) && "1".equals(v.stat)) {
          ("delete", id, supplier_name, supplier_type, supplier_classify, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat, "null")
        } else if ("update".equals(k)) {
          ("update", id, supplier_name, supplier_type, supplier_classify, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat, oldStat)
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
        }
    }).filter(x => !x._1.equals("null")).filter(x => "2".equals(x._4))

    supplierDetail.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1) && "1".equals(x._13)) {
              jedis.hset("supplier-detail", x._2, "id" + ";" + x._2 + ";" + "suppliername" + ";" + x._3 + ";" + "classify" + ";" + x._5 + ";" + "area" + ";" + x._6 + ";" + "address" + ";" + x._7 + ";" + "businesslicense" + ";" + x._8 + ";" + "regcapital" + ";" + x._9 + ";" + "foodbusiness" + ";" + x._10 + ";" + "foodcirculation" + ";" + x._11 + ";" + "foodproduce" + ";" + x._12)
            } else if ("delete".equals(x._1)) {
              jedis.hdel("supplier-detail", x._2)
            } else if ("update".equals(x._1)) {
              val v = jedis.hget("supplier-detail", x._2)
              if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
                if (StringUtils.isNoneEmpty(x._14) && !x._14.equals("null")) {
                  if ("1".equals(x._14) && "0".equals(x._13)) {
                    jedis.hdel("supplier-detail", x._2)
                  } else if ("0".equals(x._14) && "1".equals(x._14)) {
                    jedis.hset("supplier-detail", x._2, "id" + ";" + x._2 + ";" + "suppliername" + ";" + x._3 + ";" + "classify" + ";" + x._5 + ";" + "area" + ";" + x._6 + ";" + "address" + ";" + x._7 + ";" + "businesslicense" + ";" + x._8 + ";" + "regcapital" + ";" + x._9 + ";" + "foodbusiness" + ";" + x._10 + ";" + "foodcirculation" + ";" + x._11 + ";" + "foodproduce" + ";" + x._12)
                  }
                } else {
                  jedis.hset("supplier-detail", x._2, "id" + ";" + x._2 + ";" + "suppliername" + ";" + x._3 + ";" + "classify" + ";" + x._5 + ";" + "area" + ";" + x._6 + ";" + "address" + ";" + x._7 + ";" + "businesslicense" + ";" + x._8 + ";" + "regcapital" + ";" + x._9 + ";" + "foodbusiness" + ";" + x._10 + ";" + "foodcirculation" + ";" + x._11 + ";" + "foodproduce" + ";" + x._12)
                }
              }
            }
        })


    })
  }

}
