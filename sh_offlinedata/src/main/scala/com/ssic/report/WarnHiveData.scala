package com.ssic.report

import java.util.{Calendar, Date}

import com.ssic.report.WarnHiveData.{format, format1, format2}
import com.ssic.service.{DealDataStat, WarnHiveDataStat}
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}

import scala.collection.JavaConverters._

/**
  * 预警的hive数据汇总处理到redis，针对大屏的
  */

object WarnHiveData {

  private val format = FastDateFormat.getInstance("yyyy")
  private val format1 = FastDateFormat.getInstance("M")
  private val format2 = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("预警的hive数据汇总处理到redis")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)

    val hiveContext: HiveContext = new HiveContext(sc)


    val year = format.format(new Date())
    val month = format1.format(new Date())

    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH, -4)
    val time = format2.format(calendar.getTime)
    val date = format2.format(new Date())

    val jedis = JPools.getJedis
    val total = jedis.hgetAll("warndata")
    val warnTotal = sc.parallelize(total.asScala.toList) //已存在的预警统计表的数据
    val warnDeatlTotal = new DealDataStat().warntotal(warnTotal)

    val supplier = jedis.hgetAll("supplier-warndata")  //团餐公司证照预警未处理
    val supplierwarndata = sc.parallelize(supplier.asScala.toList) //已存在的预警统计表的数据

    val warnData = hiveContext.sql(s"select warn_date,warn_type,warn_type_child,warn_stat,company_type,department_id,year,month,supplier_name from app_saas_v1.app_t_edu_warn_detail where year='${year}' " +
      s"and target= 3 ").rdd
      .map({
        row =>
          val warn_date = row.getAs[String]("warn_date")
          val warn_type = row.getAs[Int]("warn_type")
          val warn_type_child = row.getAs[Int]("warn_type_child")
          val warn_stat = row.getAs[Int]("warn_stat")
          val company_type = row.getAs[Int]("company_type")
          val department_id = row.getAs[String]("department_id")
          val years = row.getAs[String]("year")
          val months = row.getAs[String]("month")
          val supplier_name = Rule.emptyToNull(row.getAs[String]("supplier_name"))
          (department_id, warn_type, warn_type_child, company_type, warn_stat, department_id,warn_date,years,months,supplier_name)
      })

    //未处理证照预警数
    val warntypechilidNodeal = warnDeatlTotal.filter(x => "warntypechilid-nodeal".equals(x._1))
    warnData.filter(x => x._2 == 1 && x._5 != 4).map(x => (x._3,1)).reduceByKey(_+_).map(x => ("warntypechilid-nodeal"+"_"+x._1,x._2)).cogroup(warntypechilidNodeal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //证照预警数统计数据
    val warntypechilidTotal = warnDeatlTotal.filter(x => "warntypechilid-total".equals(x._1))
    warnData.filter(x => x._2 == 1 ).map(x => (x._3,1)).reduceByKey(_+_).map(x => ("warntypechilid-total"+"_"+x._1,x._2)).cogroup(warntypechilidTotal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //各管理部门预计统计数未处理数
    val departmentidNodeal = warnDeatlTotal.filter(x => "departmentid-nodeal".equals(x._1))
    warnData.filter(x => x._5 != 4).map(x => (x._6,1)).reduceByKey(_+_).map(x => ("departmentid-nodeal"+"_"+x._1,x._2)).cogroup(departmentidNodeal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //各管理部门预计统计数已处理数
    val departmentidDeal = warnDeatlTotal.filter(x => "departmentid-deal".equals(x._1))
    warnData.filter(x => x._5 == 4).map(x => (x._6,1)).reduceByKey(_+_).map(x => ("departmentid-deal"+"_"+x._1,x._2)).cogroup(departmentidDeal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //预计类型统计数据
    val warntypeTotal = warnDeatlTotal.filter(x => "warntype-total".equals(x._1))
    warnData.map(x => (x._2,1)).reduceByKey(_+_).map(x => ("warntype-total"+"_"+x._1,x._2)).cogroup(warntypeTotal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //年预警总数
    val yearTotal = warnDeatlTotal.filter(x => "year".equals(x._1))
    warnData.map(x => (x._8,1)).reduceByKey(_+_).map(x => ("year",x._2)).cogroup(yearTotal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //月预警总数
    val monthTotal = warnDeatlTotal.filter(x => "month".equals(x._1))
    warnData.filter(x => month.equals(x._9)).map(x => (x._9,1)).reduceByKey(_+_).map(x => ("month",x._2)).cogroup(monthTotal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })


    //团餐公司预警未处理
    warnData.filter(x => x._2 == 1 && x._5 != 4 && x._4 == 0).filter(x => x._3 == 0 || x._3 == 1).filter(x=> !"null".equals(x._10)).map(x => (x._10,1)).reduceByKey(_+_).cogroup(supplierwarndata).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel("supplier-warndata", k)
            } else {
              jedis.hset("supplier-warndata", k, v._1.head.toString)
            }
        })
    })

    //预警趋势图
    val dayWarnData = hiveContext.sql(s"select warn_date,warn_type,warn_type_child,warn_stat,company_type,department_id,year,month,supplier_name from app_saas_v1.app_t_edu_warn_detail where warn_date >= '${time}' and warn_date <= '${date}' ").rdd
      .map({
        row =>
          val warn_date = row.getAs[String]("warn_date")
          val warn_type = row.getAs[Int]("warn_type")
          val warn_type_child = row.getAs[Int]("warn_type_child")
          val warn_stat = row.getAs[Int]("warn_stat")
          val company_type = row.getAs[Int]("company_type")
          val department_id = row.getAs[String]("department_id")
          val years = row.getAs[String]("year")
          val months = row.getAs[String]("month")
          val supplier_name = row.getAs[String]("supplier_name")
          (department_id, warn_type, warn_type_child, company_type, warn_stat, department_id, warn_date, years, months, supplier_name)
      })

    //未处理
    val warndateNodeal = warnDeatlTotal.filter(x => "warndate-nodeal".equals(x._1))
    dayWarnData.filter(x => x._5 != 4).map(x => (x._7,1)).reduceByKey(_+_).map(x => ("warndate-nodeal"+"_"+x._1,x._2)).cogroup(warndateNodeal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //已处理
    val warndateDeal = warnDeatlTotal.filter(x => "warndate-deal".equals(x._1))
    dayWarnData.filter(x => x._5 == 4).map(x => (x._7,1)).map(x => ("warndate-deal"+"_"+x._1,x._2)).reduceByKey(_+_).cogroup(warndateDeal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })

    //总数
    val warndateTotal = warnDeatlTotal.filter(x => "warndate-total".equals(x._1))
    dayWarnData.map(x => (x._7,1)).reduceByKey(_+_).map(x => ("warndate-deal"+"_"+x._1,x._2)).cogroup(warndateTotal).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hset("warndata", k, "0")
            } else {
              jedis.hset("warndata", k, v._1.head.toString)
            }
        })
    })


//    //证件预警各区学校证件，团餐公司证件，人员证件，全部的统计数据
//    new WarnHiveDataStat().areawarntotal(hiveContext)
//
//    //各区按照学校性质的维度统计，现只包含了预警的未处理单位，预警总数，未处理预警数量
//    new WarnHiveDataStat().areanaturewarntotal(hiveContext)
//
//    //按照学制分类进行的统计，现只包含预警的未预警单位，预警总数，预警未处理数量
//    new WarnHiveDataStat().arealevelwarntotal(hiveContext)



    sc.stop()
  }

}
