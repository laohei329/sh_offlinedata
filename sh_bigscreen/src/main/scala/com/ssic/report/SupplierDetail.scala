package com.ssic.report

import com.ssic.beans.SchoolBean
import org.apache.spark.rdd.RDD

object SupplierDetail {

  def supplierDetailInsert(filterData: RDD[SchoolBean]) ={
    val supplietData = filterData.filter(x => x != null && x.table.equals("t_pro_supplier"))
    val supplierDetail = supplietData.distinct().map({
      x =>
        val id = x.data.id
        val supplier_name = x.data.supplier_name
        val supplier_type = x.data.supplier_type // 1团餐公司 2供应商
      val supplier_classify = x.data.supplier_classify // '供应商分类  0 不区分 1生产类 2 经销类',
      val city = x.data.city
        val district_id = x.data.district_id
        val address = x.data.address
        val business_license = x.data.business_license //工商执照号
      val reg_capital = x.data.reg_capital //注册资金
      val food_business_code = x.data.food_business_code //食品经营许可证号
      val food_circulation_code = x.data.food_circulation_code //食品流通证号
      val food_produce_code = x.data.food_produce_code //食品生产证号
      val stat = x.data.stat

        if ("insert".equals(x.`type`) && "1".equals(stat)) {
          ("insert", id, supplier_name, supplier_type, supplier_classify, city, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat)
        } else if ("delete".equals(x.`type`) && "1".equals(x.data.stat)) {
          ("delete", id, supplier_name, supplier_type, supplier_classify, city, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat)
        } else if ("update".equals(x.`type`)) {
          ("update", id, supplier_name, supplier_type, supplier_classify, city, district_id, address, business_license, reg_capital, food_business_code, food_circulation_code, food_produce_code, stat)
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
        }
    }).filter(x=> !x._1.equals("null")).filter(x => "2".equals(x._4))

    supplierDetail.foreachPartition({
      itr =>




    })
  }

}
