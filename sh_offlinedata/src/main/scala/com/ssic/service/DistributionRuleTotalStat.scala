package com.ssic.service

import com.ssic.impl.TargetTotalFuc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DistributionRuleTotalStat extends TargetTotalFuc{
  /**

    * * 按照各区的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @return RDD[(String, String)]  (dis-area_1_disstatus_1,1)

    */
  override def areatotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String): RDD[(String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val disstatus = x._7
        ((area,disstatus),1)

    }).reduceByKey(_+_).map(x => ("dis-area"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
  /**

    * * 按照权限的各区的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-area_1_disstatus_1,1)

    */
  override def departmentareatotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):  RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val disstatus = x._7
        val school_id = x._4
        val department = schoolData.value.getOrElse(school_id, List("null"))(9)
        ((department,area,disstatus),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-area"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }

  override def areastatustotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String):  RDD[(String, String)] = ???

  override def departmentareastatustotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???
  /**

    * * 按照上海市教属的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeid2commiteename 区属

    * * @return RDD[(String, String)]  (dis-masterid_3_slave_嘉定区教育局_disstatus_3,1)

    */
  override def masteridtotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeid2commiteename: Broadcast[Map[String, String]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7

        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeid2commiteename.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((v(5), v(6), disstatus), 1)
          }
        } else {
          (("null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-masterid"+"_"+x._1._1+"_"+"slave"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }
  /**

    * * 按照权限的上海市教属的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeid2commiteename 区属

    * * @return RDD[(String, String, String)]  (管理部门id,dis-masterid_3_slave_嘉定区教育局_disstatus_3,1)

    */
  override def departmentmasteridtotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeid2commiteename: Broadcast[Map[String, String]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)

        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), commiteeid2commiteename.value.getOrElse(v(6), "null"), disstatus), 1)
          } else {
            ((department,v(5), v(6), disstatus), 1)
          }
        } else {
          ((department,"null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-masterid"+"_"+x._1._2+"_"+"slave"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2.toString))
  }
  /**

    * * 按照上海市办学性质的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (dis-nature_0_nature-sub_1_disstatus_3,1)

    */
  override def naturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), disstatus), 1)
        } else {
          (("null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => ("dis-nature"+"_"+x._1._1+"_"+"nature-sub"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }
  /**

    * * 按照权限的上海市办学性质的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-nature_0_nature-sub_1_disstatus_3,1)

    */
  override def departmentnaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val department=v(9)
        val disstatus = x._7

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,v(1), v(2), disstatus), 1)
        } else {
          ((department,"null", "null", disstatus), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"dis-nature"+"_"+x._1._2+"_"+"nature-sub"+"_"+x._1._3+"_"+"disstatus"+"_"+x._1._4,x._2.toString))
  }

  override def areanaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareanaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???
  /**

    * * 按照上海市办学学制的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (dis-level_1_disstatus_3,1)
    */
  override def levelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), disstatus), 1)
        } else {
          (("null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => ("dis-level"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
  /**

    * * 按照权限的上海市办学学制的验收操作规范的统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,dis-level_1_disstatus_3,1)

    */
  override def departmentlevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)

        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,v(0), disstatus), 1)
        } else {
          ((department,"null", disstatus), 1)
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"dis-level"+"_"+x._1._2+"_"+"disstatus"+"_"+x._1._3,x._2.toString))
  }

  override def arealevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentarealevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???
  /**

    * * 按照上海市管理部门的验收操作规范数量

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础数据

    * * @return RDD[(String, String)]  (dis-department_1_disstatus_1,1)

    */
  override def departmentstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x =>("dis-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
  /**

    * * 按照权限的上海市管理部门的验收操作规范数量

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础数据

    * * @return RDD[(String, String, String)]  (管理部门id,dis-department_1_disstatus_1,1)

    */
  override def departmentdepartmentstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val disstatus = x._7
        val department=v(9)
        ((department,disstatus),1)
    }).reduceByKey(_+_).map(x =>(x._1._1,"dis-department"+"_"+x._1._1+"_"+"disstatus"+"_"+x._1._2,x._2.toString))
  }
}
