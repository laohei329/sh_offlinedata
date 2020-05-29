package com.ssic.yesreport

import java.util.{Calendar, Date}

import com.ssic.utils.JPools
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SQLContext
import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkConf, SparkContext}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._


/**
  * 将当天的留样的主表和留样子表进行关联
  * */

object YesRetentionDetail {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val hiveContext: HiveContext = new HiveContext(sc)
    val session = sqlContext.sparkSession


    val calendar = Calendar.getInstance()
    calendar.setTime(new Date())
    calendar.add(Calendar.DAY_OF_MONTH, -1)
    val time = calendar.getTime
    val yes = format.format(time)

    val now = format.format(new Date())

    val jedis = JPools.getJedis
    val retentiondish = jedis.hgetAll(yes + "_retention-dish")
    val retentiondishData: RDD[(String, String)] = sc.parallelize(retentiondish.asScala.toList)  //合并的留样计划临时表数据

    val retentionsample = jedis.hgetAll(yes + "_retentionsample")
    val retentionsampleData: RDD[(String, String)] = sc.parallelize(retentionsample.asScala.toList)  //留样主表临时表数据

    val yesretentiondishzi = jedis.hgetAll(yes + "_retentiondish")
    val yesretentiondishziData: RDD[(String, String)] = sc.parallelize(yesretentiondishzi.asScala.toList)  //昨日日留样子表临时表数据

    val retentiondishzi = jedis.hgetAll(now + "_retentiondish")
    val retentiondishziData: RDD[(String, String)] = sc.parallelize(retentiondishzi.asScala.toList)  //今日留样子表临时表数据

    val yesretentiondishziDealData = yesretentiondishziData.map({
      x =>
        val sampleid = x._2.split("_")(1)
        (sampleid, "id" + "_" + x._1 + "_" + x._2)
    })

    val nowretentiondishziDealData = retentiondishziData.map({
      x =>
        val sampleid = x._2.split("_")(1)
        (sampleid, "id" + "_" + x._1 + "_" + x._2)
    })

    val retentiondishziDealData: RDD[(String, String)] = yesretentiondishziDealData.union(nowretentiondishziDealData)


    retentionsampleData.leftOuterJoin(retentiondishziDealData).map(x => (x._1,x._2._1,x._2._2.getOrElse("null"))).filter(x => !"null".equals(x._3))
      .map({
        x =>
          //69fea856-532c-4c03-b57b-100ad4193367_a36490e5-765f-4500-8983-1ac375f385cd
        // packageid_614c4a87-b675-4ba8-829b-933e88b6f791_createtime_2019-11-22 08:08:12_creator_admin_quantity_250_remark_null_groupname_中大班_catertypename_午点_dishesname_清肺罗汉果茶_reservedata_2019-11-22 00:00:00 14:00:00_reservestatus_1_consistent_1_cremark_null

          val sampleid = x._1
          val dishid = x._3.split("_")(1)
          val packageid = x._2.split("_")(1)
          val createtime = x._2.split("_")(3)
          val creator = x._2.split("_")(5)
          val remark = x._2.split("_")(7)
          val groupname = x._2.split("_")(9)
          val catertypename = x._2.split("_")(11)
          val reservedata = x._2.split("_")(13)
          val quantity = x._3.split("_")(5)
          val dishes = x._3.split("_")(7)
          var reservestatus_name ="null"
          if(x._2.split("_").length >= 16){
            reservestatus_name = x._2.split("_")(15)
          }else{
            reservestatus_name
          }

          var consistent_name ="null"
          var cremark_name="null"
          if(x._3.split("_").length >= 10){
            consistent_name = x._3.split("_")(9)
            cremark_name = x._3.split("_")(11)
          }else{
            consistent_name
            cremark_name
          }

          val key = x._1 + "_" + dishid

          val value ="packageid"+"_"+packageid+"_"+"createtime"+"_"+createtime+"_"+"creator"+"_"+creator+"_"+"quantity"+"_"+quantity+"_"+"remark"+"_"+remark+"_"+"groupname"+"_"+groupname+"_"+"catertypename"+"_"+catertypename+"_"+"dishesname"+"_"+dishes+"_"+"reservedata"+"_"+reservedata+"_"+"reservestatus"+"_"+reservestatus_name+"_"+"consistent"+"_"+consistent_name+"_"+"cremark"+"_"+cremark_name

          (key,value)
      }).cogroup(retentiondishData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(yes + "_retention-dish", k)
            } else {
              jedis.hset(yes + "_retention-dish", k, v._1.head)
            }
        })
    })

    sc.stop()
  }
}
