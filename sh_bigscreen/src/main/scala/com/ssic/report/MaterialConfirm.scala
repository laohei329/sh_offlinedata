package com.ssic.report

import java.text.SimpleDateFormat
import java.util.Date

import com.ssic.beans.SchoolBean
import com.ssic.report.Distribution.format
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD

/**
  * Created by 云 on 2018/8/7.
  * 用料计划功能指标
  */
object MaterialConfirm {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def useMaterial(filterData: RDD[SchoolBean]): RDD[(String, String, String, String, String, String, String, String, String, String, String)] = {
    val materialPlanMaster = filterData.filter(x => x != null && x.table.equals("t_pro_material_plan_master") && !x.`type`.equals("delete") && !x.data.stat.equals("0"))
    val materialPlanData = materialPlanMaster.distinct().map({
      x =>
        val mold = x.data.`type`.toString
        //'类型 0 原料 1 成品菜'
        val use_date = x.data.use_date
      val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val supplier_id = x.data.supplier_id
        val proj_id = x.data.proj_id
        val status = x.data.status
        val stat = x.data.stat
        val proj_name = x.data.proj_name
        val now = format.format(new Date())
        if (x.`type`.equals("insert") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "null", "old",stat,"null","insert",proj_name)
        } else if (x.`type`.equals("update") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, x.old.status, "old",stat,x.old.stat,"update",proj_name)
        } else if (x.`type`.equals("insert") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "null", "new",stat,"null","insert",proj_name)
        } else if (x.`type`.equals("update") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, x.old.status, "new",stat,x.old.stat,"update",proj_name)
        } else if(x.`type`.equals("delete") && format.parse(date).getTime >= format.parse("2018-09-12").getTime){
          (date, mold, supplier_id, proj_id, status, "delete", "old",stat,"null","delete",proj_name)
        }else if(x.`type`.equals("delete") && format.parse(date).getTime < format.parse("2018-09-12").getTime){
          (date, mold, supplier_id, proj_id, status, "delete", "new",stat,"null","delete",proj_name)
        } else {
          ("无", "无", "无", "无", "无", "无", "无", "无", "无", "无", "无")
        }
    })
    materialPlanData
  }

}
