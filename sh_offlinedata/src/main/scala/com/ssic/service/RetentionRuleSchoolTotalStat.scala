package com.ssic.service

import com.ssic.impl.SchoolAllUseFunc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class RetentionRuleSchoolTotalStat extends SchoolAllUseFunc{

  /**

    * * 各区留样计划各状态按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @return RDD[(String, String)]  (re-school-area_0_reservestatus_1,1)

    */
  override def areatotal(businessData: RDD[(String, String)], date: String): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val reservestatus = x._2.split("_")(9)

        ((area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => ("re-school-area" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按照权限各区留样规则各状态按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-school-area_0_reservestatus_1,1)

    */
  override def departmentareatotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val reservestatus = x._2.split("_")(9)
        val school_id = x._1.split("_")(3)
        val department = schoolData.value.getOrElse(school_id, List("null"))(9)

        ((department, area, reservestatus), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-area" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按上海市各类型学校来留样操作规则数据统计（level）

    * * @param businessData 已处理的留样计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (re-school-level_1_reservestatus_1,1)

    */
  override def leveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((v(0), reservestatus), 1)
      } else {
        (("null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-school-level" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按权限上海市各类型学校来留样操作规则按照学校数据统计（level,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-school-level_1_reservestatus_1,1)

    */
  override def departmentleveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
        ((department, v(0), reservestatus), 1)
      } else {
        ((department, "null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-level" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  override def arealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)]  = ???

  override def departmentarealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  /**

    * * 按上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (re-school-nature_0_nature-sub_1_reservestatus_1,1)

    */
  override def naturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)]  = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((v(1), v(2), reservestatus), 1)
      } else {
        (("null", "null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => ("re-school-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按权限上海市办学性质来留样操作规则按照学校数据统计（nature,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-school-nature_0_nature-sub_1_reservestatus_1,1)

    */
  override def departmentnaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
        ((department, v(1), v(2), reservestatus), 1)
      } else {
        ((department, "null", "null", reservestatus), 1)
      }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2.toString))
  }


  override def areanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)]= ???

  override def canteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentcanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  override def areacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  /**

    * * 按所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeData 区属

    * * @return RDD[(String, String)]  (re-school-masterid_3_slave_嘉定区教育局_reservestatus_1,1)

    */
  override def masteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val reservestatus = x._2.split("_")(9)

        val v = schoolData.value.getOrElse(schoolid, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeData.value.getOrElse(v(6), "null"), reservestatus), 1)
          } else {
            ((v(5), v(6), reservestatus), 1)
          }
        } else {
          (("null", "null", reservestatus), 1)
        }


    }).reduceByKey(_ + _).map(x => ("re-school-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "reservestatus" + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按权限所属教育局留样计划各操作规则按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeData 区属

    * * @return RDD[(String, String, String)]  (管理部门id,re-school-masterid_3_slave_嘉定区教育局_reservestatus_1,1)

    */
  override def departmentmasteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val reservestatus = x._2.split("_")(9)

        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), commiteeData.value.getOrElse(v(6), "null"), reservestatus), 1)
          } else {
            ((department, v(5), v(6), reservestatus), 1)
          }
        } else {
          ((department, "null", "null", reservestatus), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "re-school-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "reservestatus" + "_" + x._1._4, x._2.toString))
  }

  /**

    * * 按管理部门留样操作规则统计数据统计 (对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (re-school-department_1_reservestatus_1,1)

    */
  override def shanghaidepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map(x => {

      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)
      val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
      ((department,reservestatus),1)

    }).reduceByKey(_+_).map(x => ("re-school-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按权限管理部门留样操作规则统计数据统计 (对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,re-school-department_1_reservestatus_1,1)

    */
  override def departmentdepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {

      val schoolid = x._1.split("_")(3)
      val reservestatus = x._2.split("_")(9)
      val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
      ((department,reservestatus),1)

    }).reduceByKey(_+_).map(x => (x._1._1,"re-school-department" + "_" + x._1._1 + "_" + "reservestatus" + "_" + x._1._2, x._2.toString))
  }
}
