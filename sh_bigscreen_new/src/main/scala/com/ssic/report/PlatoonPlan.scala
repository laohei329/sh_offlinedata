package com.ssic.report

import com.ssic.beans.SchoolBean
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

  def PlatoonRealTimeData(plaData: (RDD[SchoolBean], Broadcast[Map[String, String]],SparkSession)): RDD[(String, String, String, String, String)] = {
    val platoonData = plaData._1.filter(x => x != null && x.table.equals("t_saas_package")  && !x.data.stat.equals("0") && x.data.is_publish != 0 && "1".equals(x.data.industry_type))
    val platoonDataFil = platoonData.distinct().filter(x => StringUtils.isNoneEmpty(x.data.supply_date)).filter(x => StringUtils.isNoneEmpty(x.data.school_id)).map({
      x =>
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val school_id = x.data.school_id
        val menu_id = x.data.menu_id //'菜谱ID(一个项目点一天的排菜)'
      val quhao = plaData._2.value.getOrElse(school_id, "null")
        //val area = NewTools.schoolid(plaData._3, school_id)
        val create_time = x.data.create_time

        //时间，学校id，区号，表操作类型，创建时间
        (date, school_id, quhao,x.`type`,create_time)
    })
//      .filter(x => "1".equals(x._3)
//                 ||"2".equals(x._3)
//                 ||"3".equals(x._3)
//                 ||"4".equals(x._3)
//                 ||"5".equals(x._3)
//                 ||"6".equals(x._3)
//                 ||"7".equals(x._3)
//                 ||"8".equals(x._3)
//                 ||"9".equals(x._3)
//                 ||"10".equals(x._3)
//                 ||"11".equals(x._3)
//                 ||"12".equals(x._3)
//                 ||"13".equals(x._3)
//                 ||"14".equals(x._3)
//                 ||"15".equals(x._3)
//                 ||"16".equals(x._3))
    platoonDataFil
  }
}
