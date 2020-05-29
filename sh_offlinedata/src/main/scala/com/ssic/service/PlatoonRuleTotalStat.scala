package com.ssic.service

import com.ssic.impl.SchoolAllUseFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class PlatoonRuleTotalStat extends SchoolAllUseFunc{
  /**

    * * 计算各区的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @return RDD[(String, String)]  (area_1_plastatus_1,1)

    */
  override def areatotal(businessData: RDD[(String, String)], date: String): RDD[(String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val plastatus = x._2.split("_")(7)
        ((area, plastatus), 1)
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 计算权限管理部门各区的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String,String)]  (管理部门id,area_1_plastatus_1,1)


    */
  override def departmentareatotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)]  = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
        val plastatus = x._2.split("_")(7)
        ((department, area, plastatus), 1)

    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 计算上海市各类型学校的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (level_1_plastatus_1,1)

    */
  override def leveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val plastatus = x._2.split("_")(7)
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), plastatus), 1)
        } else {
          (("null", plastatus), 1)
        }
    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 计算权限管理部门上海市各类型学校的排菜操作规则数量统计(level)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String,String)]  (管理部门id,level_1_plastatus_1,1)

    */
  override def departmentleveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val plastatus = x._2.split("_")(7)
        val v =schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), plastatus), 1)
        } else {
          ((department, "null", plastatus), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "level" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2.toString))
  }

  override def arealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentarealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)] = ???
  /**

    * * 计算上海市按照学校性质的排菜操作规则数量统计 (nature)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (nature_0_nature-sub_null_plastatus_1,1)

    */

  override def naturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val id = x._1.split("_")(1)
        val plastatus = x._2.split("_")(7)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), plastatus), 1)
        } else {
          (("null", "null", plastatus), 1)
        }
    }).reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 计算上海市按照学校性质的排菜操作规则数量统计 (nature)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String,String)]  (管理部门id,nature_0_nature-sub_null_plastatus_1,1)

    */
  override def departmentnaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)]  = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        val plastatus = x._2.split("_")(7)

        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), plastatus), 1)
        } else {
          ((department, "null", "null", plastatus), 1)
        }


    }).reduceByKey(_ + _).map(x => (x._1._1, "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "plastatus" + "_" + x._1._4, x._2.toString))
  }

  override def areanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)] = ???

  override def canteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentcanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)] = ???

  override def areacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String,String)] = ???
  /**

    * * 计算按照区属的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @param commiteeData 区属

    * * @return RDD[(String, String)]  (masterid_1_slave_普陀区教育局_plastatus_1,1)

    */
  override def masteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String)]  = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        val plastatus = x._2.split("_")(7)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeData.value.getOrElse(v(6), "null"), plastatus), 1)
          } else {
            ((v(5), v(6), plastatus), 1)
          }
        } else {
          (("null", "null", plastatus), 0)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "plastatus" + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 计算权限管理部门按照区属的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @param commiteeData 区属

    * * @return RDD[(String, String, String)]  (管理部门id,masterid_1_slave_普陀区教育局_plastatus_1,1)

    */
  override def departmentmasteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]):  RDD[(String, String, String)]  = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        val plastatus = x._2.split("_")(7)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department, v(5), commiteeData.value.getOrElse(v(6), "null"), plastatus), 1)
          } else {
            ((department, v(5), v(6), plastatus), 1)
          }

        } else {
          ((department, "null", "null", plastatus), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "plastatus" + "_" + x._1._4, x._2.toString))
  }
  /**

    * * 计算上海市按照管理部门的维度的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (department_1_plastatus_1,1)

    */
  override def shanghaidepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        val plastatus = x._2.split("_")(7)
        if (StringUtils.isNoneEmpty(v(9)) && !v(9).equals("null")) {
          ((v(9), plastatus), 1)
        } else {
          ((v(9), plastatus), 1)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("department" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 计算权限管理部门按照管理部门的排菜操作规则数量统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String, String)]  (管理部门id,department_1_plastatus_1,1)

    */
  override def departmentdepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        val plastatus = x._2.split("_")(7)

        ((department, plastatus), 1)


    }).reduceByKey(_ + _).map(x => (x._1._1, "department" + "_" + x._1._1 + "_" + "plastatus" + "_" + x._1._2, x._2.toString))
  }
}
