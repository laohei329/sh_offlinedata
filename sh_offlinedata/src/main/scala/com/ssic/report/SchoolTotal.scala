package com.ssic.report

import com.ssic.service.DealDataStat
import com.ssic.utils.Tools.{conn, edu_bd_department, edu_committee, edu_school, url}
import com.ssic.utils.{JPools, Tools}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import scala.collection.JavaConverters._

/**
  * 针对管理部门的管理权限统计学校数量
  */

object SchoolTotal {

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val bd_department = session.read.jdbc(url, edu_bd_department, conn)
    val school = session.read.jdbc(url, edu_school, conn)
    val committee = session.read.jdbc(url, edu_committee, conn)
    bd_department.createTempView("t_edu_bd_department")
    school.createTempView("t_edu_school")
    committee.createTempView("t_edu_committee")

    val departmentid = Tools.departmentid(session)
    for (i <- 0 until departmentid.size){
      val id = departmentid(i)
      val jedis = JPools.getJedis
      val keys = jedis.del("schoolData"+"_"+"department"+"_"+id)
    }



    //id,school_name,area,level_name,school_nature_name,school_nature_sub_name,license_main_type_name,license_main_child_name,department_master_id_name,department_slave_id_name,depart
    val schoolData = Tools.schoolRdd(session)

    val departTotal = schoolData.map(x => (x._11,1)).reduceByKey(_+_)    //管理部门学校统计数据
    val departAreaTotal = schoolData.map(x => ((x._11,x._3),1)).reduceByKey(_+_)  //管理部门各区学校统计数据
    val departLevelTotal = schoolData.map(x => ((x._11,x._4),1)).reduceByKey(_+_) //管理部门学校学制统计数据
    val departAreaLevelTotal = schoolData.map(x => ((x._11, x._3, x._4), 1)).reduceByKey(_ + _) //管理部门学校按照区号学制统计
    val departNatureTotal = schoolData.map(x => ((x._11,x._5,x._6),1)).reduceByKey(_+_)  //管理部门学校办学性质统计数据
    val departAreaNatureTotal = schoolData.map(x => ((x._11,x._3,x._5,x._6),1)).reduceByKey(_+_)  //管理部门学校各区办学性质统计数据
    val departLicenseTotal = schoolData.map(x => ((x._11,x._7,x._8),1)).reduceByKey(_+_)  //管理部门学校供餐模式统计数据
    val departAreaLicenseTotal = schoolData.map(x => ((x._11,x._3,x._7,x._8),1)).reduceByKey(_+_)   //管理部门各区学校供餐模式统计数据
    val departDepartmentTotal = schoolData.map(x => ((x._11,x._9,x._10),1)).reduceByKey(_+_)  //管理部门按照所属主管部门统计数据

    departTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1,"shanghai",x._2.toString)
        })
    })

    departAreaTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"area"+"_"+x._1._2,x._2.toString)
        })
    })

    departLevelTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"shanghai"+"-"+"level"+"_"+x._1._2,x._2.toString)
        })
    })

    departAreaLevelTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"area"+"_"+x._1._2+"_"+"level"+"_"+x._1._3,x._2.toString)
        })
    })

    departNatureTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"shanghai"+"-"+"nature"+"_"+x._1._2+"_"+"nature-sub"+"_"+x._1._3,x._2.toString)
        })
    })

    departAreaNatureTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"area"+"_"+x._1._2+"_"+"nature"+"_"+x._1._3+"_"+"nature-sub"+"_"+x._1._4,x._2.toString)
        })
    })

    departLicenseTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"shanghai"+"-"+"canteenmode"+"_"+x._1._2+"_"+"ledgertype"+"_"+x._1._3,x._2.toString)
        })
    })

    departAreaLicenseTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"area"+"_"+x._1._2+"_"+"canteenmode"+"_"+x._1._3+"_"+"ledgertype"+"_"+x._1._4,x._2.toString)
        })
    })

    departDepartmentTotal.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hset("schoolData"+"_"+"department"+"_"+x._1._1,"masterid"+"_"+x._1._2+"_"+"slave"+"_"+x._1._3,x._2.toString)
        })
    })


    //上海市学校总数

    val jedis = JPools.getJedis
    val schoolDataTotal = jedis.hgetAll("schoolData")
    val shanghaiSchoolDatalData = sc.parallelize(schoolDataTotal.asScala.toList) //已存在的用料计划数据

    schoolData.map(x => (1,1)).reduceByKey(_+_).map(x => ("shanghai",x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("shanghai")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => (x._3,1)).reduceByKey(_+_).map(x => ("area_"+x._1,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("area")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => (x._4,1)).reduceByKey(_+_).map(x => ("shanghai-level_"+x._1,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("shanghai-level")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => ((x._3,x._4),1)).reduceByKey(_+_).map(x => ("area_"+x._1._1+"_level_"+x._1._2,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("area-level")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => ((x._5,x._6),1)).reduceByKey(_+_).map(x => ("shanghai-nature_"+x._1._1+"_nature-sub_"+x._1._2,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("shanghai-nature")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => ((x._3,x._5,x._6),1)).reduceByKey(_+_).map(x => ("area_"+x._1._1+"_nature_"+x._1._2+"_nature-sub_"+x._1._3,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("area-nature")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => ((x._7,x._8),1)).reduceByKey(_+_).map(x => ("shanghai-canteenmode_"+x._1._1+"_ledgertype_"+x._1._2,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("shanghai-canteenmode")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => ((x._3,x._7,x._8),1)).reduceByKey(_+_).map(x => ("area_"+x._1._1+"_canteenmode_"+x._1._1+"_ledgertype_"+x._1._2,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("area-canteenmode")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })

    schoolData.map(x => ((x._9,x._10),1)).reduceByKey(_+_).map(x => ("masterid_"+x._1._1+"_slave_"+x._1._2,x._2)).cogroup(new DealDataStat().shanghaitotal(shanghaiSchoolDatalData).filter(x => x._1.equals("masterid")).map(_._2)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("schoolData", k, "0")
            } else {
              jedis.hset("schoolData", k, v._1.head.toString)
            }
        })
    })





    sc.stop()
  }
}
