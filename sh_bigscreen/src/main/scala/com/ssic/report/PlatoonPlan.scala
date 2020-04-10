package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.beans.SchoolBean
import com.ssic.report.RetentionDish.format
import com.ssic.utils.NewTools
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


  /**
    *
    * * 分析，获取排菜数据
    *
    * * @param filterData binlog日志数据
    *
    * * @param  school2Area 学校对应的区数据
    *
    * * @param SparkSession
    *
    * * @return RDD[(String, String, String, String, String, String)]  时间，学校id，区号，表操作类型，创建时间,有效状态
    *
    */

  def PlatoonRealTimeData(filterData:RDD[SchoolBean], school2Area:Broadcast[Map[String, String]],session:SparkSession): RDD[(String, String, String, String, String, String)] = {
    val platoonData = filterData.filter(x => x != null && x.table.equals("t_saas_package") && !x.data.stat.equals("0") && x.data.is_publish != 0 && "1".equals(x.data.industry_type))
    val platoonDataFil = platoonData.distinct().filter(x => StringUtils.isNoneEmpty(x.data.supply_date)).filter(x => StringUtils.isNoneEmpty(x.data.school_id)).map({
      x =>
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val school_id = x.data.school_id
        val menu_id = x.data.menu_id //'菜谱ID(一个项目点一天的排菜)'
      val quhao = school2Area.value.getOrElse(school_id, "null")
        //val area = NewTools.schoolid(plaData._3, school_id)
        val create_time = x.data.create_time
        val stat = x.data.stat

        //时间，学校id，区号，表操作类型，创建时间
        val calendar = Calendar.getInstance()
        calendar.setTime(new Date())
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val time = calendar.getTime
        val now = format.format(time)
        if (format.parse(now).getTime <= format.parse(date).getTime) {
          (date, school_id, quhao, x.`type`, create_time, stat)
        } else {
          ("null", "null", "null", "null", "null", "null")
        }
    }).filter(x => !"null".equals(x._1))

    platoonDataFil
  }
}
