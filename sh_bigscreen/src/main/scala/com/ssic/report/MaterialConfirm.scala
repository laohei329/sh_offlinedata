package com.ssic.report

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import com.alibaba.fastjson.JSON
import com.ssic.beans.{MaterialPlan, MaterialPlanMaster, SchoolBean}
import com.ssic.report.Distribution.format
import com.ssic.report.PlatoonPlan.format
import com.ssic.utils.{JPools, NewTools}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory


/**
  * Created by 云 on 2018/8/7.
  * 用料计划功能指标
  */
object MaterialConfirm {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def useMaterial(filterData: RDD[SchoolBean]): RDD[(String, String, String, String, String, String, String, String, String, String, String)] = {
    val materialPlanMaster = filterData.filter(x => x != null && x.table.equals("t_pro_material_plan_master") && !x.`type`.equals("delete"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[MaterialPlanMaster]), JSON.parseObject(x.old, classOf[MaterialPlanMaster])))
      .filter(x => !x._2.stat.equals("0"))

    val materialPlanData = materialPlanMaster.distinct().map({
      case (k, v, z) =>

        val mold = v.`type`.toString
        //'类型 0 原料 1 成品菜'
        val use_date = v.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val supplier_id = v.supplier_id
        val proj_id = v.proj_id
        val status = v.status
        val stat = v.stat
        val proj_name = v.proj_name
        val now = format.format(new Date())

        var oldStat = "null"
        var oldStatus = "null"
        try {
          oldStat = z.stat
          oldStatus= z.status
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $z", e)
          }

        }
        if (k.equals("insert") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "null", "old", stat, "null", "insert", proj_name)
        } else if (k.equals("update") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, oldStatus, "old", stat, oldStat, "update", proj_name)
        } else if (k.equals("insert") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "null", "new", stat, "null", "insert", proj_name)
        } else if (k.equals("update") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, oldStatus, "new", stat, oldStat, "update", proj_name)
        } else if (k.equals("delete") && format.parse(date).getTime >= format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "delete", "old", stat, "null", "delete", proj_name)
        } else if (k.equals("delete") && format.parse(date).getTime < format.parse(now).getTime) {
          (date, mold, supplier_id, proj_id, status, "delete", "new", stat, "null", "delete", proj_name)
        } else {
          ("无", "无", "无", "无", "无", "无", "无", "无", "无", "无", "无")
        }
    })

    materialPlanData
  }

  /**
    *
    * 用料计划存入redis的临时表中 useMaterialPlanDetail
    * @param filterData  mysql的业务binlgog日志
    * @param  projid2Area 项目点的id 对应的 区
    * @param supplierid2supplierName 团餐公司id对应团餐公司name
    *
    */

  def useMaterialdish(filterData: RDD[SchoolBean], projid2Area: Broadcast[Map[String, String]], supplierid2supplierName: Broadcast[Map[String, String]]) = {
    val materialPlanMaster = filterData.filter(x => x != null && x.table.equals("t_pro_material_plan"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[MaterialPlan])))
      .filter(x => !"0".equals(x._2.stat))
    val materialPlanData = materialPlanMaster.distinct().map({
      case (k, v) =>

        val mold = v.`type`.toString
        //'类型 0 原料 1 成品菜'
        val use_date = v.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val supplier_id = v.supplier_id
        val supplier_name = v.supplier_name
        val proj_id = v.proj_id
        val status = v.status
        val stat = v.stat
        val proj_name = v.proj_name
        val calendar = Calendar.getInstance()
        calendar.setTime(new Date())
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val time = calendar.getTime
        val now = format.format(time)

        //前天之前的历史数据不记录
        if (format.parse(now).getTime <= format.parse(date).getTime) {
          if (k.equals("insert")) {
            //时间，类型，团餐公司id，项目点id，用料计划操作状态，“null”，"old"，数据状态，"null"，表操作类型，项目点名字。团餐公司名字
            (date, mold, supplier_id, proj_id, status.toInt, "null", "old", stat, "null", "insert", proj_name, supplier_name)
          } else if (k.equals("update")) {
            (date, mold, supplier_id, proj_id, status.toInt, "null", "old", stat, "null", "update", proj_name, supplier_name)
          } else {
            (date, mold, supplier_id, proj_id, status.toInt, "delete", "old", stat, "null", "delete", proj_name, supplier_name)
          }
        } else {
          ("null", "null", "null", "null", 0, "null", "null", "null", "null", "null", "null", "null")
        }

    }).filter(x => !"null".equals(x._1)).distinct().sortBy(x => x._5, true)

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
                      jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12, x._5.toString)
                    }
                  } else {
                    jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12, x._5.toString)
                  }

                }
              } else if ("delete".equals(x._10)) {
                if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                  if (x._4.contains(",").equals(true)) {
                    val arr_proid = x._4.split(",")
                    for (pid <- arr_proid) {
                      jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12)
                    }
                  } else {
                    jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12)
                  }
                }
              } else if ("update".equals(x._10)) {
                if ("0".equals(x._8)) {
                  if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                    if (x._4.contains(",").equals(true)) {
                      val arr_proid = x._4.split(",")
                      for (pid <- arr_proid) {
                        jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12)
                      }
                    } else {
                      jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12)
                    }
                  }
                } else {
                  //实际数据中可能不存在更新时把有效数据更新为无效数据
                  if (StringUtils.isNoneEmpty(x._4) && !x._4.equals("null")) {
                    if (x._4.contains(",").equals(true)) {
                      val arr_proid = x._4.split(",")
                      for (pid <- arr_proid) {
                        jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(pid, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + pid + "_" + "suppliername" + "_" + x._12, x._5.toString)
                      }
                    } else {
                      val status = jedis.hget(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12)
                      if (!"2".equals(status)) {
                        jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + projid2Area.value.getOrElse(x._4, "null") + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._4 + "_" + "suppliername" + "_" + x._12, x._5.toString)
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
