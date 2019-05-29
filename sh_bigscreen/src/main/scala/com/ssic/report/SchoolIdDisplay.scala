package com.ssic.report

import java.util.Date

import com.ssic.beans.SchoolBean
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD

/**
  * Created by 云 on 2018/8/22.
  * 学校排菜功能实时使用展现指标
  */

object SchoolIdDisplay {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")
  def SchoolIdShow(filterData: RDD[SchoolBean]): RDD[(String, String)] = {
    val DisplayData = filterData.filter(x => x != null && x.table.equals("t_saas_package") && !x.`type`.equals("delete") && !x.data.stat.equals("0"))
    val DisFiltData = DisplayData.filter(x => StringUtils.isNoneEmpty(x.data.create_time)).filter(x => StringUtils.isNoneEmpty(x.data.school_id))
      .map({
        x =>
          val school_id = x.data.school_id
          val date = format.format(new Date())

          (date, school_id)
      })
    DisFiltData
  }

}
