package com.ssic.service

import com.ssic.impl.TargetTotalFuc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DistributionTotalStat extends TargetTotalFuc {
  /**
    *
    * * 各区配送计划总数据统计
    *
    * * @param targetData 处理好的配送计划数据
    *
    * * @param date 时间
    *
    * * @return RDD[(String, String)]  (area_2,1)
    *
    */
  override def areatotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String): RDD[(String, String)] = {
    targetData.map(x => (x._1, 1)).reduceByKey(_ + _).map(x => ("area_" + x._1, x._2.toString))
  }

  /**
    *
    * * 按照权限的各区配送计划总数据统计
    *
    * * @param targetData 处理好的配送计划数据
    *
    * * @param date 时间
    *
    * * @param schoolData 学校基础数据
    *
    * * @return RDD[(String, String, String)]  (管理部门id,area_2,1)
    *
    */
  override def departmentareatotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val school_id = x._4
        val department = schoolData.value.getOrElse(school_id, List("null"))(9)
        ((department, area), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "area_" + x._1._2, x._2.toString))
  }

  /**
    *
    * * 各区配送计划各状态数据统计
    *
    * * @param targetData 处理好的配送计划数据
    *
    * * @param date 时间
    *
    * * @return RDD[(String, String)]  (area_3_status_-1,1)
    *
    */
  override def areastatustotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String): RDD[(String, String)] = {
    targetData.map(x => ((x._1, x._6), 1)).reduceByKey(_ + _).map(x => ("area_" + x._1._1 + "_status_" + x._1._2, x._2.toString))
  }
  /**

    * * 按照权限的各区配送计划各状态数据统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础数据

    * * @return RDD[(String, String, String)]  (管理部门id,area_3_status_-1,1)

    */
  override def departmentareastatustotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val school_id = x._4
        val status = x._6
        val department = schoolData.value.getOrElse(school_id, List("null"))(9)
        ((department,area,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"area_" + x._1._2 + "_status_" + x._1._3, x._2.toString))
  }
  /**

    * * 按所属教育部配送计划各状态数据统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeid2commiteename 区属

    * * @return RDD[(String, String)]  (masterid_3_slave_嘉定区教育局_status_1,1)

    */
  override def masteridtotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeid2commiteename: Broadcast[Map[String, String]]): RDD[(String, String)] ={
    targetData.map({
      x =>

        val v = schoolData.value.getOrElse(x._4, List("null"))

        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((v(5), commiteeid2commiteename.value.getOrElse(v(6), "null"), x._6), 1)
          } else {
            ((v(5), v(6), x._6), 1)
          }
        } else {
          (("null", "null", x._6), 1)
        }
    }).reduceByKey(_ + _)
      .map(x => ("masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + "status_" + x._1._3, x._2.toString))
  }

  /**

    * * 按照权限的所属教育部配送计划各状态数据统计

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeid2commiteename 区属

    * * @return RDD[(String, String, String)]  (管理部门id,masterid_3_slave_嘉定区教育局_status_1,1)

    */
  override def departmentmasteridtotal(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeid2commiteename: Broadcast[Map[String, String]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val school_id = x._4
        val status = x._6
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
          if ("3".equals(v(5))) {
            ((department,v(5), commiteeid2commiteename.value.getOrElse(v(6), "null"), status), 1)
          } else {
            ((department,v(5), v(6), status), 1)
          }
        } else {
          ((department,"null", "null", status), 1)
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + "status_" + x._1._4, x._2.toString))
  }
  /**

    * * 按上海市办学性质来配送计划各状态数据统计（nature）

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (nature_0_nature-sub_1_status_3,1)

    */
  override def naturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val v = schoolData.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "3"), 1)
          } else {
            (("null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((v(1), v(2), "2"), 1)
          } else {
            (("null", "null", "2"), 1)
          }
        }
    })
      .reduceByKey(_ + _).map(x => ("nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 按照权限的上海市办学性质来配送计划各状态数据统计（nature）

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,nature_0_nature-sub_1_status_3,1)

    */
  override def departmentnaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val area = x._1
        val school_id = x._4
        val status = x._6
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val department = v(9)
        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department,v(1), v(2), "3"), 1)
          } else {
            ((department,"null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((department,v(1), v(2), "2"), 1)
          } else {
            ((department,"null", "null", "2"), 1)
          }
        }

    }).reduceByKey(_+_).map(x => (x._1._1,"nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2.toString))
  }
  /**

    * * 各区办学性质来配送计划各状态数据统计（nature）

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (nat-area_1_nature_0_nature-sub_1_status_3,1)

    */
  override def areanaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)]  = {

    targetData.map({
      x =>
        val v = schoolData.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((x._1, v(1), v(2), "3"), 1)
          } else {
            ((x._1, "null", "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
            ((x._1, v(1), v(2), "2"), 1)
          } else {
            ((x._1, "null", "null", "2"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + "status" + "_" + x._1._4, x._2.toString))
  }

  override def departmentareanaturestatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]) : RDD[(String, String, String)] = ???
  /**

    * * 按上海市各类型学校来配送计划各状态数据统计（level）

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (level_1_status_3,1)

    */
  override def levelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val v = schoolData.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "3"), 1)
          } else {
            (("null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((v(0), "2"), 1)
          } else {
            (("null", "2"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("level" + "_" + x._1._1 + "_" + "status" + "_" + x._1._2, x._2.toString))
  }
  /**

    * * 按照权限的上海市各类型学校来配送计划各状态数据统计（level）

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,level_1_status_3,1)

    */
  override def departmentlevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val v = schoolData.value.getOrElse(x._4, List("null"))
        val department =v(9)

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department,v(0), "3"), 1)
          } else {
            ((department,"null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((department,v(0), "2"), 1)
          } else {
            ((department,"null", "2"), 1)
          }
        }
    }).reduceByKey(_+_).map(x => (x._1._1,"level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2.toString))
  }
  /**

    * * 按各区各类型学校来配送计划各状态数据统计（level）

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (lev-area_1_level_1_status_3,1)

    */
  override def arealevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val v = schoolData.value.getOrElse(x._4, List("null"))

        if ("3".equals(x._6)) {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((x._1, v(0), "3"), 1)
          } else {
            ((x._1, "null", "3"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
            ((x._1, v(0), "2"), 1)
          } else {
            ((x._1, "null", "2"), 1)
          }
        }
    }).reduceByKey(_ + _).map(x => ("lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + "status" + "_" + x._1._3, x._2.toString))
  }

  override def departmentarealevelstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???
  /**

    * * 按照上海市管理部门的配送各状态数量

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础数据

    * * @return RDD[(String, String)]  (department_1_status_1,1)

    */
  override def departmentstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val status = x._6
        val department=v(9)
        ((department,status),1)

    }).reduceByKey(_+_).map(x => ("department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2.toString))
  }
  /**

    * * 按照权限的上海市管理部门的配送各状态数量

    * * @param targetData 处理好的配送计划数据

    * * @param date 时间

    * * @param schoolData 学校基础数据

    * * @return RDD[(String, String, String)]  (管理部门id,department_1_status_1,1)

    */
  override def departmentdepartmentstatus(targetData: RDD[(String, String, String, String, String, String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    targetData.map({
      x =>
        val school_id = x._4
        val v = schoolData.value.getOrElse(school_id, List("null"))
        val status = x._6
        val department=v(9)
        ((department,status),1)
    }).reduceByKey(_+_).map(x => (x._1._1,"department"+"_"+x._1._1+"_"+"status"+"_"+x._1._2,x._2.toString))
  }
}
