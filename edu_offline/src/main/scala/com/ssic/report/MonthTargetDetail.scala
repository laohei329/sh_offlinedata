package com.ssic.report

import java.sql.Timestamp
import java.util.{Calendar, Date}

import com.ssic.report.MonthPlatoon.format
import com.ssic.service.{DealDataStat, TargetDetailStat}
import com.ssic.utils.{JPools, Rule, Tools}
import com.ssic.utils.Tools.{conn, edu_school, edu_school_supplier, url}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext

import scala.collection.JavaConverters._

object MonthTargetDetail {

  /**
    * 一个月的用料计划，配送计划，留样计划的详情数据
    **/

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")
  private val format3 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val session = sqlContext.sparkSession

    val school = session.read.jdbc(url, edu_school, conn)
    val school_supplier = session.read.jdbc(url, edu_school_supplier, conn)

    school.createTempView("t_edu_school")
    school_supplier.createTempView("t_edu_school_supplier")

    for (i <- -30 to -1) {
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, i)
      val time = calendar.getTime
      val date = format.format(time)
      val datetime = date + " 00:00:00"
      val year = format1.format(time)
      val month = format2.format(time)


      val projid2schoolid: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolid(session)) //项目点id获取学校id
      val projid2schoolname: Broadcast[Map[String, String]] = sc.broadcast(Tools.projid2schoolname(session)) //项目点id获取学校名字
      val gongcanSchool: Broadcast[Map[String, String]] = sc.broadcast(Tools.gongcanSchool(date)) //供餐学校数据
      val projid2Area = sc.broadcast(Tools.projid2Area(session)) //项目点id获取学校区号
      val school2Area = sc.broadcast(Tools.school2Area(session)) //学校id获取学校区号

      val jedis = JPools.getJedis

      val useMaterialPlanDetail = jedis.hgetAll(date + "_useMaterialPlanDetail")
      val useMaterialPlanDetailData = sc.parallelize(useMaterialPlanDetail.asScala.toList) //用料计划临时表数据

      val useMaterials = jedis.hgetAll(date + "_useMaterialPlan-Detail")
      val useMaterialData = sc.parallelize(useMaterials.asScala.toList) //用料计划已经存在的数据

      val distributionDetail = jedis.hgetAll(date + "_DistributionDetail")
      val distributionDetailData = sc.parallelize(distributionDetail.asScala.toList) //配送计划临时表数据

      val distributions = jedis.hgetAll(date + "_Distribution-Detail")
      val distributionData = sc.parallelize(distributions.asScala.toList) //配送计划已经存在的数据

      //val dishmenu: RDD[(String, String, String, String, String, String, String, String)] = Tools.hivedishmenu(hiveContext, datetime, year, month) //hive数据库的菜品数据

      val retentiondish = jedis.hgetAll(date + "_retention-dish")
      val retentiondishData: RDD[(String, String)] = sc.parallelize(retentiondish.asScala.toList) //留样计划临时表数据

      val gcretentiondish = jedis.hgetAll(date + "_gc-retentiondish")
      val gcretentiondishData: RDD[(String, String)] = sc.parallelize(gcretentiondish.asScala.toList) //留样计划已经存在的数据

      hiveContext.sql(s"select * from app_saas_v1.app_t_edu_dish_menu where year='${year}' and month ='${month}' and supply_date ='${datetime}' and have_class='1' ")
        .rdd.map({
        row =>
          val package_id = Rule.emptyToNull(row.getAs[String]("package_id"))
          val area = Rule.emptyToNull(row.getAs[String]("area"))
          val supplier_id = Rule.emptyToNull(row.getAs[String]("supplier_id"))
          val school_id = Rule.emptyToNull(row.getAs[String]("school_id"))
          val menu_group_name = Rule.emptyToNull(row.getAs[String]("menu_group_name"))
          val dishes_name = Rule.emptyToNull(row.getAs[String]("dishes_name"))
          val dishes_number = row.getAs[Integer]("dishes_number")
          val cater_type_name = Rule.emptyToNull(row.getAs[String]("cater_type_name"))
          val reserve_create_time = row.getAs[Timestamp]("reserve_create_time")
          val quantity = row.getAs[Integer]("quantity")
          val remark = Rule.emptyToNull(row.getAs[String]("remark"))
          val supply_date = row.getAs[Timestamp]("supply_date")
          val reserve_hour = row.getAs[Integer]("reserve_hour")
          val reserve_minute = row.getAs[Integer]("reserve_minute")
          val reserve_deal_status = row.getAs[String]("reserve_deal_status")
          val have_reserve = Rule.emptyToNull(row.getAs[Integer]("have_reserve").toString)
          val reserve_creator = Rule.emptyToNull(row.getAs[String]("reserve_creator"))



          if ("1".equals(have_reserve)) {

            val value = "area" + "_" + area + "_" + "supplierid" + "_" + supplier_id + "_" + "schoolid" + "_" + school_id + "_" + "groupname" + "_" + menu_group_name + "_" + "dishesname" + "_" + dishes_name + "_" + "dishesnumber" + "_" + dishes_number + "_" + "catertypename" + "_" + cater_type_name + "_" + "已留样" + "_" + "createtime" + "_" + format3.format(reserve_create_time) + "_" + "creator" + "_" + reserve_creator + "_" + "quantity" + "_" + quantity + "_" + "remark" + "_" + remark + "_" + "reservedata" + "_" + datetime + " " + reserve_hour + ":" + reserve_minute + ":" + "00" + "_" + "reservestatus" + "_" + reserve_deal_status

            (package_id+"_"+menu_group_name+"_"+"catertypename"+"_"+cater_type_name+"_"+"dishesname"+"_"+dishes_name,value)

          } else {

            val value = "area" + "_" + area + "_" + "supplierid" + "_" + supplier_id + "_" + "schoolid" + "_" + school_id + "_" + "groupname" + "_" + menu_group_name + "_" + "dishesname" + "_" + dishes_name + "_" + "dishesnumber" + "_" + "null" + "_" + "catertypename" + "_" + cater_type_name + "_" + "未留样" + "_" + "createtime" + "_" + "null" + "_" + "creator" + "_" + "null" + "_" + "quantity" + "_" + "null" + "_" + "remark" + "_" + remark + "_" + "reservedata" + "_" +"null" + "_" + "reservestatus" + "_" + reserve_deal_status
            (package_id+"_"+menu_group_name+"_"+"catertypename"+"_"+cater_type_name+"_"+"dishesname"+"_"+dishes_name,value)
          }

      }).cogroup(gcretentiondishData).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hdel(date + "_gc-retentiondish", k)
              } else {
                jedis.hset(date + "_gc-retentiondish", k, v._1.head)
              }
          })
      })

      //用料计划的详情数据
      //处理好的用料计划数据
      //      val usematerialDealData = new DealDataStat().usematerialdealdata(useMaterialPlanDetailData, projid2schoolid, projid2schoolname, gongcanSchool,projid2Area)
      //      new TargetDetailStat().usematerial(usematerialDealData, useMaterialData, date)
      //
      //      //配送计划的详情数据
      //      // 处理好的配送计划数据
      //      val distributiondealdata = new DealDataStat().distributiondealdata(distributionDetailData, gongcanSchool,school2Area,date)
      //      new TargetDetailStat().distribution(distributiondealdata, distributionData, date)

      //菜品留样的详情数据
     // new TargetDetailStat().retentiondish(dishmenu, retentiondishData, date, gongcanSchool, gcretentiondishData)

    }

    sc.stop()
  }
}
