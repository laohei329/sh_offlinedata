package com.ssic.report

import java.util.Date

import com.ssic.beans.SchoolBean
import org.apache.commons.lang3._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

/**
  * Created by 云 on 2018/8/22.
  * 学校排菜功能实时使用展现指标
  */

object SchoolIdDisplay {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  /**

    * * 学校排菜功能实时使用展现指标

    * * @param RDD[SchoolBean]  mysql的业务binlgog日志

    * * @param  Broadcast[Map[String, String]] 学校对应的区数据

    * * @param SparkSession

    */

  def SchoolIdShow(filterData: (RDD[SchoolBean], Broadcast[Map[String, String]])): RDD[(String, String,String)] = {
    val DisplayData = filterData._1.filter(x => x != null && x.table.equals("t_saas_package") && !x.`type`.equals("delete") && !x.data.stat.equals("0"))
    val DisFiltData = DisplayData.filter(x => StringUtils.isNoneEmpty(x.data.create_time)).filter(x => StringUtils.isNoneEmpty(x.data.school_id))
      .map({
        x =>
          val school_id = x.data.school_id
          val date = format.format(new Date())
          val area = filterData._2.value.getOrElse(school_id, "null")

          (date, school_id, area)
      }).filter(x => "1".equals(x._3)
      || "2".equals(x._3)
      || "3".equals(x._3)
      || "4".equals(x._3)
      || "5".equals(x._3)
      || "6".equals(x._3)
      || "7".equals(x._3)
      || "8".equals(x._3)
      || "9".equals(x._3)
      || "10".equals(x._3)
      || "11".equals(x._3)
      || "12".equals(x._3)
      || "13".equals(x._3)
      || "14".equals(x._3)
      || "15".equals(x._3)
      || "16".equals(x._3))
    DisFiltData
  }

}
