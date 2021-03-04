package com.ssic.service

import com.ssic.impl.SchoolAllUseFunc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class RetentionSchoolTotalStat extends SchoolAllUseFunc{
  /**

    * * 各区留样计划各状态按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @return RDD[(String, String)]  (school-area_13_已留样 ,1)

    */
  override def areatotal(businessData: RDD[(String, String)], date: String): RDD[(String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("_")(7)
        var stat = "null"
        if ("0".equals(status)) {
          stat = "未留样"
        } else {
          stat = "已留样"
        }
        (area, stat)
    }).map(x => ("school-area" + "_" + x._1 + "_" + x._2, 1)).reduceByKey(_ + _).map(x => (x._1,x._2.toString))
  }

  /**

    * * 按照权限各区留样计划各状态按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,school-area_13_已留样 ,1)

    */
  override def departmentareatotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val area = x._1.split("_")(1)
        val status = x._2.split("_")(7)
        val school_id = x._1.split("_")(3)
        val department = schoolData.value.getOrElse(school_id, List("null"))(9)
        var stat = "null"
        if ("0".equals(status)) {
          stat = "未留样"
        } else {
          stat = "已留样"
        }
        ((department, area, stat), 1)
    }).reduceByKey(_ + _).map(x => (x._1._1, "school-area" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (school-level_1_已留样 ,1)

    */
  override def leveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), "未留样"), 1)
        } else {
          (("null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((v(0), "已留样"), 1)
        } else {
          (("null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2), x._2))
      .reduceByKey(_ + _).map(x => ("school-level" + "_" + x._1._1 + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按权限上海市各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,school-level_1_已留样,1)

    */
  override def departmentleveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), "未留样"), 1)
        } else {
          ((department, "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department, v(0), "已留样"), 1)
        } else {
          ((department, "null", "已留样"), 1)
        }
      }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-level" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (school-lev-area_1_level_1_已留样,1)

    */
  override def arealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)]  ={
    businessData.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val v = schoolData.value.getOrElse(schoolid, List("null"))
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), "未留样"), 1)
        } else {
          ((area, "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((area, v(0), "已留样"), 1)
        } else {
          ((area, "null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2))
      .reduceByKey(_ + _).map(x => ("school-lev-area" + "_" + x._1._1 + "_" + "level" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }


  /**

    * * 按权限各区各类型学校来留样计划各状态按照学校数据统计（level,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,school-lev-area_1_level_1_已留样,1)

    */
  override def departmentarealeveltotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department =v(9)
      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,area, v(0), "未留样"), 1)
        } else {
          ((department,area, "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(0)) && !v(0).equals("null")) {
          ((department,area, v(0), "已留样"), 1)
        } else {
          ((department,area, "null", "已留样"), 1)
        }
      }

    }).reduceByKey(_+_).map(x => (x._1._1,"school-lev-area" + "_" + x._1._2 + "_" + "level" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }

  /**

    * * 按上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (school-nature_0_nature-sub_1_已留样 ,1)

    */
  override def naturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)]  = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = schoolData.value.getOrElse(schoolid, List("null"))

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), "未留样"), 1)
        } else {
          (("null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((v(1), v(2), "已留样"), 1)
        } else {
          (("null", "null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2))
      .reduceByKey(_ + _).map(x => ("school-nature" + "_" + x._1._1 + "_" + "nature-sub" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按权限上海市办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）

    * * @param retentionchildData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,school-nature_0_nature-sub_1_已留样 ,1)

    */
  override def departmentnaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), "未留样"), 1)
        } else {
          ((department, "null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department, v(1), v(2), "已留样"), 1)
        } else {
          ((department, "null", "null", "已留样"), 1)
        }
      }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }

  /**

    * * 按各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (school-nat-area_1_nature_0_nature-sub_1_已留样 ,1)

    */
  override def areanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = schoolData.value.getOrElse(schoolid, List("null"))

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), "未留样"), 1)
        } else {
          ((area, "null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((area, v(1), v(2), "已留样"), 1)
        } else {
          ((area, "null", "null", "已留样"), 1)
        }
      }

    }).map(x => ((x._1._1, x._1._2, x._1._3, x._1._4), x._2))
      .reduceByKey(_ + _).map(x => ("school-nat-area" + "_" + x._1._1 + "_" + "nature" + "_" + x._1._2 + "_" + "nature-sub" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }

  /**

    * * 按权限各区办学性质来留样计划各状态按照学校数据统计（nature,对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,school-nat-area_1_nature_0_nature-sub_1_已留样,1)

    */
  override def departmentareanaturetotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {

      val area = x._1.split("_")(1)
      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)

      val v = schoolData.value.getOrElse(schoolid, List("null"))
      val department = v(9)

      if ("0".equals(status)) {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,area, v(1), v(2), "未留样"), 1)
        } else {
          ((department,area, "null", "null", "未留样"), 1)
        }
      } else {
        if (StringUtils.isNoneEmpty(v(1)) && !v(1).equals("null")) {
          ((department,area, v(1), v(2), "已留样"), 1)
        } else {
          ((department,area, "null", "null", "已留样"), 1)
        }
      }

    })
      .reduceByKey(_ + _).map(x => (x._1._1,"school-nat-area" + "_" + x._1._2 + "_" + "nature" + "_" + x._1._3 + "_" + "nature-sub" + "_" + x._1._4 + "_" + x._1._5, x._2.toString))
  }

  override def canteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentcanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  override def areacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = ???

  override def departmentareacanteentotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = ???

  /**

    * * 按所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeData 区属

    * * @return RDD[(String, String)]  (school-masterid_3_slave_奉贤区教育局_已留样 ,1)

    */
  override def masteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String)] = {
    businessData.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(7)

        val v = schoolData.value.getOrElse(schoolid, List("null"))
        if ("0".equals(status)) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), commiteeData.value.getOrElse(v(6), "null"), "未留样"), 1)
            } else {
              ((v(5), v(6), "未留样"), 1)
            }
          } else {
            (("null", "null", "未留样"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((v(5), commiteeData.value.getOrElse(v(6), "null"), "已留样"), 1)
            } else {
              ((v(5), v(6), "已留样"), 1)
            }
          } else {
            (("null", "null", "已留样"), 1)
          }
        }

    }).map(x => ((x._1._1, x._1._2, x._1._3), x._2)).reduceByKey(_ + _)
      .map(x => ("school-masterid" + "_" + x._1._1 + "_" + "slave" + "_" + x._1._2 + "_" + x._1._3, x._2.toString))
  }

  /**

    * * 按权限所属教育局留样计划各状态按照学校数据统计(对学校进行去重处理)

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @param commiteeData 区属

    * * @return RDD[(String, String, String)]  (管理部门id,school-masterid_3_slave_奉贤区教育局_已留样 ,1)

    */
  override def departmentmasteridtotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]], commiteeData: Broadcast[Map[String, String]]): RDD[(String, String, String)] = {
    businessData.map({
      x =>
        val schoolid = x._1.split("_")(3)
        val status = x._2.split("_")(7)

        val v = schoolData.value.getOrElse(schoolid, List("null"))
        val department = v(9)
        if ("0".equals(status)) {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), commiteeData.value.getOrElse(v(6), "null"), "未留样"), 1)
            } else {
              ((department, v(5), v(6), "未留样"), 1)
            }
          } else {
            ((department, "null", "null", "未留样"), 1)
          }
        } else {
          if (StringUtils.isNoneEmpty(v(5)) && !v(5).equals("null")) {
            if ("3".equals(v(5))) {
              ((department, v(5), commiteeData.value.getOrElse(v(6), "null"), "已留样"), 1)
            } else {
              ((department, v(5), v(6), "已留样"), 1)
            }
          } else {
            ((department, "null", "null", "已留样"), 1)
          }
        }

    }).reduceByKey(_ + _).map(x => (x._1._1, "school-masterid" + "_" + x._1._2 + "_" + "slave" + "_" + x._1._3 + "_" + x._1._4, x._2.toString))
  }

  /**

    * * 按管理部门留样计划各状态数据统计 (对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String)]  (school-department_1_已留样,1)

    */
  override def shanghaidepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String)] = {
    businessData.map(x => {

      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
      if ("0".equals(status)) {
        ((department, "未留样"), 1)
      } else {
        ((department, "已留样"), 1)
      }

    }).reduceByKey(_ + _).map(x => ( "school-department" + "_" + x._1._1 + "_" + x._1._2, x._2.toString))
  }

  /**

    * * 按权限管理部门留样计划各状态数据统计 (对学校进行去重处理）

    * * @param businessData 已存在的留样计划子页面数据

    * * @param date 时间

    * * @param schoolData 学校基础信息

    * * @return RDD[(String, String, String)]  (管理部门id,school-department_1_已留样 ,1)

    */
  override def departmentdepartmenttotal(businessData: RDD[(String, String)], date: String, schoolData: Broadcast[Map[String, List[String]]]): RDD[(String, String, String)] = {
    businessData.map(x => {

      val schoolid = x._1.split("_")(3)
      val status = x._2.split("_")(7)
      val department = schoolData.value.getOrElse(schoolid, List("null"))(9)
      if ("0".equals(status)) {
        ((department, "未留样"), 1)
      } else {
        ((department, "已留样"), 1)
      }

    }).reduceByKey(_ + _).map(x => ( x._1._1,"school-department" + "_" + x._1._1 + "_" + x._1._2, x._2.toString))
  }
}
