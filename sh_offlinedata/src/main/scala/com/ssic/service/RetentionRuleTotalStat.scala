package com.ssic.service

import com.ssic.impl.TargetTotalFuc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class RetentionRuleTotalStat extends TargetTotalFuc{
  /**

    * * 各区留样操作规则数据统计

    * * @param targetData 处理好的留样计划数据

    * * @param date 时间

    * * @return RDD[(String, String)]  (re-area_0_reservestatus_1,1)

    */
  override def areatotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String): RDD[(String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val school_id = x._3
        val reservestatus = x._4
        ((area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => ("re-area" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按照权限的各区留样操作规则数据统计

    * * @param targetData 处理好的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-area_0_reservestatus_1,1)

    */
  override def departmentareatotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val school_id = x._3
        val reservestatus = x._4
        val department = schoolData.value.getOrElse(school_id, List("null"))(9)
        ((department, area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-area" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  override def areastatustotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String): RDD[(String, String)] = ???

  override def departmentareastatustotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  /**

    * * 按所属教育部留样操作规则数据统计

    * * @param targetData 处理好的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeid2commiteename 区属

    * * @return RDD[(String, String)]  (re-masterid_3_slave_嘉定区教育局_reservestatus_1,1)

    */
  override def masteridtotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeid2commiteename: Broadcast[Map[String, String]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val school_id = x._3
        val v = schoolData.value.getOrElse(school_id, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeid2commiteename.value.getOrElse(v(6), "null"), x._4), 1)
          } else {
            ((v(5), v(6), x._4), 1)
          }
        } else {
          (("null", "null", x._4), 1)
        }
    }).reduceByKey(_ + _).map(x => ("re-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按权限所属教育部留样操作规则数据统计

    * * @param targetData 处理好的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeid2commiteename 区属

    * * @return RDD[(String, String, String)]  (管理部门id,re-masterid_3_slave_嘉定区教育局_reservestatus_1,1)

    */
  override def departmentmasteridtotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeid2commiteename: Broadcast[Map[String, String]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val school_id = x._3
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), commiteeid2commiteename.value.getOrElse(v(6), "null"), x._4), 1)
          } else {
            ((department, v(5), v(6), x._4), 1)
          }
        } else {
          ((department, "null", "null", x._4), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2.toString))
  }

  /**
    *  按上海市办学性质来留样操作规则各状态数据统计（nature）
    * @param targetData 处理好的留样计划数据
    *  @param date 时间
    *  @param schoolData 学校基础信息
    *  @return RDD[(String, String)]  (re-nature_0_nature-sub_1_reservestatus_1,1)
    */
  override def naturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map(x => {
      val v = schoolData.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((v(1), v(2), x._4), 1)
      } else {
        (("null", "null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  /**
    * 按权限上海市办学性质来留样操作规则各状态数据统计（nature）
    * @param targetData 处理好的留样计划数据
    * @param date 时间
    * @param schoolData 学校基础信息
    * @return RDD[(String, String, String)]  (管理部门id,re-nature_0_nature-sub_1_reservestatus_1,1)
    */
  override def departmentnaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map(x => {
      val v = schoolData.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((department, v(1), v(2), x._4), 1)
      } else {
        ((department, "null", "null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2.toString))
  }

  override def areanaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareanaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  /**
    * 按上海市各类型学校来留样操作规则数据统计（level）
    * @param targetData 已处理的留样计划数据
    * @param date 时间
    * @param schoolData 学校基础信息
    * @return RDD[(String, String)]  (re-level_1_reservestatus_1,1)
    */
  override def levelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map(x => {
      val v = schoolData.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((v(0), x._4), 1)
      } else {
        (("null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-level" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按权限上海市各类型学校来留样操作规则数据统计（level）

    * * @param targetData 已处理的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-level_1_reservestatus_1,1)

    */
  override def departmentlevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map(x => {
      val v = schoolData.value.getOrElse(x._3, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((department, v(0), x._4), 1)
      } else {
        ((department, "null", x._4), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-level" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  override def arealevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentarealevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]= ???

  /**

    * * 按管理部门留样计划操作规则统计

    * * @param targetData 已处理的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (re-department_1_reservestatus_1,1)

    */
  override def departmentstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map(x => {
      val schoolid = x._3
      val reservestatus = x._4
      val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
      ((department, reservestatus), 1)

    }).reduceByKey(_ + _).map(x => ("re-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按权限管理部门留样计划操作规则统计

    * * @param targetData 已处理的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-department_1_reservestatus_1,1)

    */
  override def departmentdepartmentstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map(x => {
      val schoolid = x._3
      val reservestatus = x._4
      val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
      ((department, reservestatus), 1)

    }).reduceByKey(_ + _).map(x => (x._1._1, "re-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }
}
