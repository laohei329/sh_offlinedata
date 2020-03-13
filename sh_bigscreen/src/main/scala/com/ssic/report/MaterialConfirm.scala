package com.ssic.report

import java.text.SimpleDateFormat
import java.util.Date

import com.ssic.beans.SchoolBean
import com.ssic.report.Distribution.format
import com.ssic.report.PlatoonPlan.format
import com.ssic.utils.{JPools, NewTools}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


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
          (date, mold, supplier_id, proj_id, status, "null", "old", stat, "null", "insert", proj_name)
        } else if (x.`type`.equals("update") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, x.old.status, "old", stat, x.old.stat, "update", proj_name)
        } else if (x.`type`.equals("insert") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "null", "new", stat, "null", "insert", proj_name)
        } else if (x.`type`.equals("update") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, x.old.status, "new", stat, x.old.stat, "update", proj_name)
        } else if (x.`type`.equals("delete") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "delete", "old", stat, "null", "delete", proj_name)
        } else if (x.`type`.equals("delete") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "delete", "new", stat, "null", "delete", proj_name)
        } else {
          ("无", "无", "无", "无", "无", "无", "无", "无", "无", "无", "无")
        }
    })

    materialPlanData
  }

  /**

    * * 用料计划存入redis的临时表中 useMaterialPlanDetail

    * * @param RDD[SchoolBean]  mysql的业务binlgog日志

    * * @param  Broadcast[Map[String, String]] 团餐公司id对应的名字

    * * @param SparkSession

    */

  def useMaterialdish(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, String]])) = {
    val materialPlanMaster = filterData._1.filter(x => x != null && x.table.equals("t_pro_material_plan") && !x.data.stat.equals("0"))
    val materialPlanData = materialPlanMaster.distinct().map({
      x =>

        val mold = x.data.`type`.toString
        //'类型 0 原料 1 成品菜'
        val use_date = x.data.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val supplier_id = x.data.supplier_id
        val supplier_name = x.data.supplier_name
        val proj_id = x.data.proj_id
        val status = x.data.status
        val stat = x.data.stat
        val proj_name = x.data.proj_name
        val now = format.format(new Date())
        if (format.parse(now).getTime <= format.parse(date).getTime) {
          if (x.`type`.equals("insert")) {
            (date, mold, supplier_id, proj_id, status.toInt, "null", "old", stat, "null", "insert", proj_name, supplier_name)
          } else if (x.`type`.equals("update")) {
            (date, mold, supplier_id, proj_id, status.toInt, x.old.status, "old", stat, x.old.stat, "update", proj_name, supplier_name)
          } else {
            (date, mold, supplier_id, proj_id, status.toInt, "delete", "old", stat, "null", "delete", proj_name, supplier_name)
          }
        }else{
          ("null", "null", "null", "null", 0, "null", "null", "null", "null", "null", "null", "null")
        }

    }).filter(x => !"null".equals(x._1)).distinct().sortBy(x => x._5,true)

    materialPlanData.filter(x => x._7.equals("old")).filter(x => !x._1.equals("无"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              if ("insert".equals(x._10)) {
                if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                  if (x._4.contains(",").equals(true)) {
                    val arr_proid = x._4.split(",")
                    for (pid <- arr_proid) {
                      jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12, x._5.toString)
                    }
                  } else {
                    jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12, x._5.toString)
                  }

                }
              } else if ("delete".equals(x._10)) {
                if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                  if (x._4.contains(",").equals(true)) {
                    val arr_proid = x._4.split(",")
                    for (pid <- arr_proid) {
                      jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12)
                    }
                  } else {
                    jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12)
                  }
                }
              } else if ("update".equals(x._10)) {
                if ("0".equals(x._8)) {
                  if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                    if (x._4.contains(",").equals(true)) {
                      val arr_proid = x._4.split(",")
                      for (pid <- arr_proid) {
                        jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12)
                      }
                    } else {
                      jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12)
                    }
                  }
                } else {
                  //实际数据中可能不存在更新时把有效数据更新为无效数据
                  if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                    if (x._4.contains(",").equals(true)) {
                      val arr_proid = x._4.split(",")
                      for (pid <- arr_proid) {
                        jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12, x._5.toString)
                      }
                    } else {
                      val status = jedis.hget(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12)
                    if (!"2".equals(status)) {
                        jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + filterData._2.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12, x._5.toString)
                     }

                    }

                  }
                }
              }
          })

      })
  }

  //  def useMaterialdish(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
  //    val materialPlanMaster = filterData._1.filter(x => x != null && x.table.equals("t_pro_material_plan_master") && !x.data.stat.equals("0") && x.data.`type`.toString.equals("1")).map({
  //      x =>
  //        val id = x.data.id
  //        val use_date = x.data.use_date
  //        val replaceAll = use_date.replaceAll("\\D", "-")
  //        val date = format.format(format.parse(replaceAll))
  //        val status = x.data.status
  //        val supplier_id = x.data.supplier_id
  //        (id, List(date, status, x.`type`, supplier_id))
  //    })
  //
  //    val materialPlan = filterData._1.filter(x => x != null && x.table.equals("t_pro_material_plan") && !x.`type`.equals("delete") && !x.data.stat.equals("0") && x.data.`type`.toString.equals("1")).map(x => {
  //      val proj_id = x.data.proj_id
  //      (x.data.master_id, x.data.proj_id)
  //    }
  //    ).distinct()
  //
  //    materialPlan.leftOuterJoin(materialPlanMaster).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).filter(x => !x._3(0).equals("null")).map({
  //      x =>
  //        val now = format.format(new Date())
  //       // val area = filterData._2.value.getOrElse(x._2, "null")
  //        if (x._3(2).equals("insert") && format.parse(x._3(0)).getTime >= format.parse(now).getTime) {
  //          (x._1, x._3(0), "1", x._3(3), x._2, x._3(1), "insert")
  //        } else if (x._3(2).equals("update") && format.parse(x._3(0)).getTime >= format.parse(now).getTime) {
  //          (x._1, x._3(0), "1", x._3(3), x._2, x._3(1), "update")
  //        } else if (x._3(2).equals("delete") && format.parse(x._3(0)).getTime >= format.parse(now).getTime) {
  //          (x._1, x._3(0), "1", x._3(3), x._2, x._3(1), "delete")
  //        } else {
  //          ("null", "null", "null", "null", "null", "null", "null")
  //        }
  //
  //    }).filter(x => !x._1.equals("null")).foreachPartition({
  //      itr =>
  //        val jedis = JPools.getJedis
  //        itr.foreach({
  //          x =>
  //            if ("insert".equals(x._7)) {
  //              if(StringUtils.isNoneEmpty(x._5) && !x._5.equals("null")) {
  //                if (x._5.contains(",").equals(true)){
  //                  val arr_proid = x._5.split(",")
  //                  for ( pid <- arr_proid){
  //                    jedis.hset(x._2 + "_useMaterialPlanDish", x._1 + "_" + pid, "area_" + filterData._2.value.getOrElse(pid,"null") + "_type_1_" + "projid" + "_" + pid + "_supplierid_" + x._4 + "_status_" + x._6)
  //                    jedis.hset(x._2 + "_useMaterialPlanDishtemp", x._1, x._6)
  //                  }
  //                }else{
  //                  jedis.hset(x._2 + "_useMaterialPlanDish", x._1 + "_" + x._5, "area_" + filterData._2.value.getOrElse(x._5,"null") + "_type_1_" + "projid" + "_" + x._5 + "_supplierid_" + x._4 + "_status_" + x._6)
  //                  jedis.hset(x._2 + "_useMaterialPlanDishtemp", x._1, x._6)
  //                }
  //
  //              }
  //
  //            } else if ("update".equals(x._7)) {
  //              jedis.hset(x._2 + "_useMaterialPlanDishtemp", x._1,x._6)
  //            } else if ("delete".equals(x._7)) {
  //              jedis.hdel(x._2 + "_useMaterialPlanDishtemp", x._1)
  //            }
  //        })
  //    })
  //
  //
  //  }

}
