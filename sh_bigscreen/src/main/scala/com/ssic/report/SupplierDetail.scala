package com.ssic.report

import com.ssic.beans.SchoolBean
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3.StringUtils
import org.apache.spark.rdd.RDD

object SupplierDetail {

  def supplierDetail(filterData: RDD[SchoolBean]) ={
    val supplietData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier"))
    val supplierDetail = supplietData.distinct().map({
      x =>
        val id = x.data.id
        val supplier_name = x.data.supplier_name
        val supplier_type = x.data.supplier_type // 1团餐公司 2供应商
      val supplier_classify = x.data.supplier_classify // '供应商分类  0 不区分 1生产类 2 经销类',
        val district_id = Rule.emptyToNull(x.data.district_id)
        val address = Rule.emptyToNull(x.data.address)
        val business_license = Rule.emptyToNull(x.data.business_license) //工商执照号
      val reg_capital =Rule.emptyToNull( x.data.reg_capital) //注册资金
      val food_business_code = Rule.emptyToNull(x.data.food_business_code) //食品经营许可证号
      val food_circulation_code = Rule.emptyToNull(x.data.food_circulation_code) //食品流通证号
      val food_produce_code = Rule.emptyToNull(x.data.food_produce_code) //食品生产证号
      val stat = x.data.stat

        if ("insert".equals(x.`type`) && "1".equals(stat)) {
          ("insert", id, supplier_name, supplier_type, supplier_classify, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat,"null")
        } else if ("delete".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("delete", id, supplier_name, supplier_type, supplier_classify, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat,"null")
        } else if ("update".equals(x.`type`)) {
          ("update", id, supplier_name, supplier_type, supplier_classify, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat,x.old.stat)
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null","null")
        }
    }).filter(x=> !x._1.equals("null")).filter(x => "2".equals(x._4))

    supplierDetail.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if("insert".equals(x._1) && "1".equals(x._13)){
              jedis.hset("supplier-detail",x._2,"id"+";"+x._2+";"+"suppliername"+";"+x._3+";"+"classify"+";"+x._5+";"+"area"+";"+x._6+";"+"address"+";"+x._7+";"+"businesslicense"+";"+x._8+";"+"regcapital"+";"+x._9+";"+"foodbusiness"+";"+x._10+";"+"foodcirculation"+";"+x._11+";"+"foodproduce"+";"+x._12)
            }else if("delete".equals(x._1)){
              jedis.hdel("supplier-detail",x._2)
            }else if("update".equals(x._1)){
              val v = jedis.hget("supplier-detail", x._2)
              if(StringUtils.isNoneEmpty(v) && !v.equals("null")){
                if(StringUtils.isNoneEmpty(x._14) && !x._14.equals("null")){
                  if("1".equals(x._14) && "0".equals(x._13)){
                    jedis.hdel("supplier-detail",x._2)
                  }else if("0".equals(x._14) && "1".equals(x._14)){
                    jedis.hset("supplier-detail",x._2,"id"+";"+x._2+";"+"suppliername"+";"+x._3+";"+"classify"+";"+x._5+";"+"area"+";"+x._6+";"+"address"+";"+x._7+";"+"businesslicense"+";"+x._8+";"+"regcapital"+";"+x._9+";"+"foodbusiness"+";"+x._10+";"+"foodcirculation"+";"+x._11+";"+"foodproduce"+";"+x._12)
                  }
                }else{
                  jedis.hset("supplier-detail",x._2,"id"+";"+x._2+";"+"suppliername"+";"+x._3+";"+"classify"+";"+x._5+";"+"area"+";"+x._6+";"+"address"+";"+x._7+";"+"businesslicense"+";"+x._8+";"+"regcapital"+";"+x._9+";"+"foodbusiness"+";"+x._10+";"+"foodcirculation"+";"+x._11+";"+"foodproduce"+";"+x._12)
                }
              }
            }
        })


    })
  }

}
