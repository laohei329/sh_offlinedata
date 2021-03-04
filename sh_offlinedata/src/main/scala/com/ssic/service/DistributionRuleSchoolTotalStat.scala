package com.ssic.service

import com.ssic.impl.SchoolAllUseFunc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DistributionRuleSchoolTotalStat extends SchoolAllUseFunc {
  /**
    *
    * * 按照各区的验收操作规范的统计(对学校去重)
    *
    * * @param businessData 已存在的配送计划子页面数据
    *
    * * @param date 时间
    *
    * * @return RDD[(String, String)]  (dis-school-area_1_disstatus_1,1)
    *
    */
  override def areatotal(businessData: RDD[(String, String)], date: String): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        ((area, disstatus), 1)
    }).reduceByKey(_ + _).map(x => ("dis-school-area" + "_" + x._1._1 + "_" + "disstatus" + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 按照权限的各区的验收操作规范的统计(对学校去重)

    * * @param businessData 已存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-school-area_1_disstatus_1,1)

    */
  override def departmentareatotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
        ((department,area,disstatus),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-area"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }
  /**

    * * 按照上海市办学学制的验收操作规范的统计(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (dis-school-level_1_disstatus_3,1)

    */
  override def leveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), disstatus), 1)
        } else {
          (("null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-school-level"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
  /**

    * * 按照权限的上海市办学学制的验收操作规范的统计(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-school-level_1_disstatus_3,1)

    */
  override def departmentleveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department=v(9)

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,v(0), disstatus), 1)
        } else {
          ((department,"null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-level"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }

  override def arealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentarealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???
  /**

    * * 按照上海市办学性质的验收操作规范的统计(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (dis-school-nature_0_nature-sub_1_disstatus_3,1)

    */
  override def naturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), disstatus), 1)
        } else {
          (("null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-school-nature"+"_"+x._1._1+"_"+"nature-sub"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }
  /**

    * * 按照权限的上海市办学性质的验收操作规范的统计(对学校去重

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-school-nature_0_nature-sub_1_disstatus_3,1)

    */
  override def departmentnaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,v(1), v(2), disstatus), 1)
        } else {
          ((department,"null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-nature"+"_"+x._1._2+"_"+"nature-sub"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2.toString))
  }

  override def areanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  override def canteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentcanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  override def areacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???
  /**

    * * 按照上海市教属的验收操作规范的统计(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeData 区属

    * * @return RDD[(String, String)]  (dis-school-masterid_3_slave_奉贤区教育局_disstatus_3,1)

    */
  override def masteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeData.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((v(5), v(6), disstatus), 1)
          }
        } else {
          (("null", "null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => ("dis-school-masterid"+"_"+x._1._1+"_"+"slave"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }
  /**

    * * 按照权限的上海市教属的验收操作规范的统计(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeData 区属

    * * @return RDD[(String, String, String)]  (管理部门id,dis-school-masterid_3_slave_奉贤区教育局_disstatus_3,1)

    */
  override def departmentmasteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String, String)]  = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), commiteeData.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((department,v(5), v(6), disstatus), 1)
          }
        } else {
          ((department,"null", "null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-masterid"+"_"+x._1._2+"_"+"slave"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2.toString))
  }
  /**

    * * 按照上海市管理部门的验收操作规范数量(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (dis-school-department_1_disstatus_1,1)

    */
  override def shanghaidepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x => ("dis-school-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
  /**

    * * 按照权限的上海市管理部门的验收操作规范数量(对学校去重)

    * * @param businessData 存在的配送计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-school-department_1_disstatus_1,1)

    */
  override def departmentdepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val schoolid = x._1.split("_")(3)
        val disstatus = x._2.split("_")(11)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-school-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
}
