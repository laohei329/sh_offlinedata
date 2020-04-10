package com.ssic.service

import com.ssic.impl.SchoolAllUseFunc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class PlatoonTotalStat extends SchoolAllUseFunc{
  /**

    * * 计算按照市教委各区的排菜统计数据

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @return RDD[(String, String)]  (15_供餐_已排菜,1)

    */
  override def areatotal(businessData: RDD[(String, String)], date: String) :RDD[(String, String)]= {
    businessData.map({
      x =>
        //(16,供餐_已排菜)
        ((x._1.split("_")(0), x._2), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1 + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 计算按照权限管理部门的各区的排菜统计数据

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,15_供餐_已排菜,1)

    */
  override def departmentareatotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)]= {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
        if (x._2.split("_").size > 2) {
          ((area, department, x._2.split("_create-time")(0)), 1)
        } else {
          ((area, department, x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => (x._1._2, x._1._1 + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 计算上海市各类型学校的排菜情况 (level)

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (level_11_不供餐_未排菜,1)

    */
  override def leveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String, String)] = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), x._2), 1)
        } else {
          (("null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 计算权限管理部门上海市各类型学校的排菜统计 (level)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,level_11_不供餐_未排菜,1)

    */
  override def departmentleveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, v(0), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, v(0), x._2), 1)
          } else {
            ((department, "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "level" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 计算各区各类型学校的排菜情况 (level)

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (area_14_level_14_不供餐_未排菜,1)

    */
  override def arealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String, String)] = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val area = x._1.split("_")(0)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), x._2), 1)
        } else {
          ((area, "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }

  /**

    * *计算权限管理部门各区各类型学校的排菜统计 (level)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,area_14_level_14_不供餐_未排菜,1)

    */
  override def departmentarealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, area, v(0), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, area, "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department, area, v(0), x._2), 1)
          } else {
            ((department, area, "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }
  /**

    * * 计算上海市按照学校性质的排菜统计 (nature)

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (nature_0_nature-sub_null_不供餐_未排菜,1)

    */
  override def naturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]) :RDD[(String, String)]= {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), x._2), 1)
        } else {
          (("null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }
  /**

    * *计算权限管理部门上海市按照学校性质的排菜统计 (nature)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,nature_0_nature-sub_null_不供餐_未排菜,1)

    */
  override def departmentnaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, v(1), v(2), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, v(1), v(2), x._2), 1)
          } else {
            ((department, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }
  /**

    * * 计算上海市按照学校性质的排菜统计 (nature)

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (area_14_nature_0_nature-sub_null_不供餐_未排菜,1)

    */
  override def areanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String, String)] = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val area = x._1.split("_")(0)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), x._2), 1)
        } else {
          ((area, "null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }

  /**

    * *计算权限管理部门各区按照学校性质的排菜统计 (nature)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,area_14_nature_0_nature-sub_null_不供餐_未排菜,1)

    */

  override def departmentareanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, area, v(1), v(2), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, area, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department, area, v(1), v(2), x._2), 1)
          } else {
            ((department, area, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + x._1._5, x._2.toString))
  }
  /**

    * * 计算上海市按照学校食堂性质排菜统计 (canteenmode)

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (canteenmode_1_ledgertype_0_供餐_未排菜,1)

    */
  override def canteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String, String)] = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
          ((v(3), v(4), x._2), 1)
        } else {
          (("null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("canteenmode" + "_" + x._1._1 + "_" + "ledgertype" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }
  /**

    * *计算权限管理部门上海市按照学校食堂性质排菜统计 (canteenmode)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,canteenmode_1_ledgertype_0_供餐_未排菜,1)

    */
  override def departmentcanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, v(3), v(4), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, v(3), v(4), x._2), 1)
          } else {
            ((department, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "canteenmode" + "_" + x._1._2 + "_" + "ledgertype" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }
  /**

    * * 计算各区按照学校食堂性质排菜统计 (canteenmode)

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (area_14_canteenmode_1_ledgertype_0_供餐_未排菜,1)

    */
  override def areacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String, String)]  = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val area = x._1.split("_")(0)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
          ((area, v(3), v(4), x._2), 1)
        } else {
          ((area, "null", "null", x._2), 1)
        }
    }).reduceByKey(_ + _).map(x => ("area" + "_" + x._1._1 + "_" + "canteenmode" + "_" + x._1._2 + "_" + "ledgertype" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }
  /**

    * *计算权限管理部门各区按照学校食堂性质排菜统计 (canteenmode)

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,area_14_canteenmode_1_ledgertype_0_供餐_未排菜,1)

    */
  override def departmentareacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, area, v(3), v(4), x._2.split("_create-time")(0)), 1)
          } else {
            ((department, area, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(3)) && !v(3).equals("null")) {
            ((department, area, v(3), v(4), x._2), 1)
          } else {
            ((department, area, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "area" + "_" + x._1._2 + "_" + "canteenmode" + "_" + x._1._3 + "_" + "ledgertype" + "_" + x._1._4 + "_" + x._1._5, x._2.toString))
  }
  /**

    * * 计算按照区属的排菜统计

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @param commiteeData 教属数据

    * * @return RDD[(String, String)]  (masterid_3_slave_普陀区教育局_供餐_已排菜,1)


    */
  override def masteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]) :RDD[(String, String)]= {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeData.value.getOrElse(v(6), "null"), x._2), 1)
          } else {
            ((v(5), v(6), x._2), 1)
          }
        } else {
          (("null", "null", x._2), 0)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 计算权限管理部门按照区属的排菜统计

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @param commiteeData 教属数据

    * * @return RDD[(String,String, String)]  (管理部门id,masterid_3_slave_普陀区教育局_供餐_已排菜_未排菜,1)

    */
  override def departmentmasteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]) : RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58)
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), commiteeData.value.getOrElse(v(6), "null"), x._2.split("_create-time")(0)), 1)
            } else {
              ((department, v(5), v(6), x._2.split("_create-time")(0)), 1)
            }

          } else {
            ((department, "null", "null", x._2.split("_create-time")(0)), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), commiteeData.value.getOrElse(v(6), "null"), x._2), 1)
            } else {
              ((department, v(5), v(6), x._2), 1)
            }

          } else {
            ((department, "null", "null", x._2), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => (x._1._1, "masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }
  /**

    * * 计算上海市按照管理部门的维度的统计数据

    * * @param businessData 处理后的排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String, String)]  (department_1_供餐_未排菜,1)


    */
  override def shanghaidepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String, String)] = {
    businessData.map({
      x =>
        val id = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(id, List("null"))
        if (StringUtils.isNoneEmpty(v(9)) && !v(9).equals("null")) {
          ((v(9), x._2), 1)
        } else {
          ((v(9), x._2), 1)
        }
    }).filter(x => !x._1._1.equals("null")).reduceByKey(_ + _).map(x => ("department" + "_" + x._1._1 + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 计算权限管理部门按照管理部门的维度的统计数据

    * * @param businessData 排菜供餐表数据

    * * @param date 时间

    * * @param schoolData 处理后的学校基础数据

    * * @return RDD[(String,String, String)]  (管理部门id,department_1_供餐_未排菜,1)


    */
  override def departmentdepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]):RDD[(String,String, String)] = {
    businessData.map({
      x =>
        //(16_96b6083c-c65f-46c4-b327-fc54e766f19b,供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val area = x._1.split("_")(0)
        val schoolid = x._1.split("_")(1)
        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if (x._2.split("_").size > 2) {
          ((department, x._2.split("_create-time")(0)), 1)

        } else {
          ((department, x._2), 1)
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "department" + "_" + x._1._1 + "_" + x._1._2, x._2.toString))
  }
}
