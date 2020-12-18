package com.ssic.report

import java.util.{Calendar, Date}

import com.alibaba.fastjson.JSON
import com.ssic.beans.{B2bDishDetail, SaasPackage, SchoolBean}
import com.ssic.report.RetentionDish.format
import com.ssic.utils.{JPools, MysqlUtils, NewTools}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


/**
  * Created by 云 on 2018/8/22.
  * 排菜计划功能指标
  */
object PlatoonPlan {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyyMMdd")


  /**
    *
    * * 分析，获取排菜数据
    *
    * @param filterData binlog日志数据
    *
    *  @param  school2Area 学校对应的区数据
    *
    * @param session
    *
    * * @return RDD[(String, String, String, String, String, String)]  时间，学校id，区号，表操作类型，创建时间,有效状态
    *
    */

  def PlatoonRealTimeData(filterData: RDD[SchoolBean], school2Area: Broadcast[Map[String, String]], session: SparkSession): RDD[(String, String, String, String, String, String)] = {
    val platoonData = filterData.filter(x => x != null && x.table.equals("t_saas_package"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[SaasPackage])))
      .filter(x => !"0".equals(x._2.stat) && x._2.is_publish != 0 && "1".equals(x._2.industry_type))
    val platoonDataFil = platoonData.distinct().filter(x => StringUtils.isNoneEmpty(x._2.supply_date)).filter(x => StringUtils.isNoneEmpty(x._2.school_id)).map({
      case (k, v) =>
        val supply_date = v.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val school_id = v.school_id
        val menu_id = v.menu_id //'菜谱ID(一个项目点一天的排菜)'
      val quhao = school2Area.value.getOrElse(school_id, "null")
        //val area = NewTools.schoolid(plaData._3, school_id)
        val create_time = v.create_time
        val stat = v.stat


        val calendar = Calendar.getInstance()
        calendar.setTime(new Date())
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val time = calendar.getTime
        val now = format.format(time)
        if (format.parse(now).getTime <= format.parse(date).getTime) {
          //时间，学校id，区号，表操作类型，创建时间，数据是否有效状态
          (date, school_id, quhao, k, create_time, stat)
        } else {
          ("null", "null", "null", "null", "null", "null")
        }
    }).filter(x => !"null".equals(x._1))

    platoonDataFil
  }

  /**
    * 分析，获取排菜数据
    * @param filterData binlog日志数据
    */
  def b2bPlatoon(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null && x.table.equals("arrange_dish_detail_info"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[B2bDishDetail])))
      .map({
        x =>

          val b2bDishDetailBean = x._2  //arrange_dish_info的B2B排菜表的结构类
          val date = format.format(format1.parse(b2bDishDetailBean.arrange_date))
          val calendar = Calendar.getInstance()
          calendar.setTime(new Date())
          calendar.add(Calendar.DAY_OF_MONTH, -1)
          val time = calendar.getTime
          val now = format.format(time)

          val project_site_id = b2bDishDetailBean.project_site_id

          val types = MysqlUtils.merchantToType(project_site_id) //商家类型 1政府（三产） 2学校 3医院 4机构 5企业 6其他
        val eduId = MysqlUtils.merchantToEduid(project_site_id) //阳光午餐的学校id
          val create_date = b2bDishDetailBean.create_date
          val del_flag = b2bDishDetailBean.del_flag
          val id = b2bDishDetailBean.id
          val arrange_dish_id = b2bDishDetailBean.arrange_dish_id
          val is_available = b2bDishDetailBean.is_available

          if (format.parse(now).getTime <= format.parse(date).getTime) {
            (date, eduId, types, x._1, create_date, del_flag, id, arrange_dish_id, is_available)
          } else {
            ("null", "null", 0, "null", "null", "null", "null", "null", "null")
          }
      }).filter(x => !"null".equals(x._1)).filter(x => !"null".equals(x._2) && StringUtils.isNoneEmpty(x._2)).filter(x => x._3 == 2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              if ("insert".equals(x._4)) {
                if ("1".equals(x._9) && "0".equals(x._6))
                  jedis.hset(x._1 + "_" + "b2b-platoon", x._7 + "_" + x._2, "1" + "_" + "create-time" + "_" + x._5)
              } else if ("update".equals(x._4)) {
                if ("1".equals(x._6)) {
                  jedis.hdel(x._1 + "_" + "b2b-platoon", x._7 + "_" + x._2)
                } else {
                  jedis.hset(x._1 + "_" + "b2b-platoon", x._7 + "_" + x._2, "1" + "_" + "create-time" + "_" + x._5)
                }
              } else {
                jedis.hdel(x._1 + "_" + "b2b-platoon", x._7 + "_" + x._2)
              }
          })
      })
  }
}
