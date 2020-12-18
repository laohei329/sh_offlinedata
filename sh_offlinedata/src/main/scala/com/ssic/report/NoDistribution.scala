package com.ssic.report

import java.util
import java.util.Date

import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * 判断得到学校去重页面的学校验收状态
  */

object NoDistribution {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val logger = LoggerFactory.getLogger(this.getClass)
  Logger.getLogger("org").setLevel(Level.ERROR)

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("今日验收对学校去重子页面验收状态数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val date = format.format(new Date())

    val jedis = JPools.getJedis
    val dischild = jedis.hgetAll(date + "_DistributionTotal_child")
    val dischilddata = sc.parallelize(dischild.asScala.toList)  //配送子页面数据
    /**
     * Field:区号_学校id
     * Value: 供餐_已排菜_create-time_创建时间_reason_原因_plastatus_排菜操作状态
     * 排菜操作状态：1 表示规范录入  2  表示补录  3 表示逾期补录  4 表示无数据  5 表示不供餐
     */
    val platoon: util.Map[String, String] = jedis.hgetAll(date + "_platoon-feed")
    val platoonData = sc.parallelize(platoon.asScala.toList)    //学校的供餐数据
    /**
     * Feild:
     * (area_区号_id_学校id)
     * Value:
     * (total_配送计划总数量_accept_已验收数量_assign_已指派数量_shipp_已配送数量_status_配送计划状态_disstatus_验收操作状态_deliveryDate_验收上报时间)
     * 未验收数量 = 总数 - 已验收
     * 未配送数量 = 总数  - 已配送
     * 未指派数量 = 总数  - 已指派
     * 指派 -》 配送 -》 验收   例如状态为未配送，那么指派状态为 是，配送状态为否，验收状态为否
     */
    platoonData.filter(x => !x._2.split("_")(0).equals("不供餐")).map(x =>("area" + "_" + x._1.split("_")(0) + "_" + "id" + "_" + x._1.split("_")(1), x._2)).cogroup(dischilddata)
    .foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k,v) =>
            //表示左边没有，右边有
            if (v._1.size == 0){
              jedis.hdel(date+"_DistributionTotal_child",k)
            }else{
              val value = jedis.hget(date+"_DistributionTotal_child",k)
              if(StringUtils.isNoneEmpty(value) && !value.equals("null")){
                val shipp = Rule.emptyToInt(value.split("shipp_")(1).split("_")(0))  //已配送数量
                val assign = Rule.emptyToInt(value.split("assign_")(1).split("_")(0))   //已指派数量
                val accept = Rule.emptyToInt(value.split("accept_")(1).split("_")(0))   //已验收数量
                val disTotalNum = Rule.emptyToInt(value.split("total_")(1).split("_")(0))  //总数
                val disstatus = value.split("disstatus_")(1)  //验收操作状态
                val deliveryDate=value.split("deliveryDate_")(1) //验收上报时间

                if(disTotalNum != 0) {

                  if((disTotalNum  - assign) != 0){
                    //未指派不为0的时候，所有状态为否
                    jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"-1"+"_"+"disstatus"+"_"+"4"+"_"+"deliveryDate"+"_"+"null")
                  }else{
                    if(disTotalNum  - shipp != 0){
                      //未指派为0的时候,已指派为0的时候,未配送不为0的时候，指派状态为是，其他状态为否
                      jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"1"+"_"+"disstatus"+"_"+"4"+"_"+"deliveryDate"+"_"+"null")
                    }else{
                      if(disTotalNum  - accept != 0){
                        //未指派为0的时候,已指派为0的时候,未配送为0的时候，已配送不为0的时候，指派状态为是，配送状态为是，其他状态为否
                        jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"2"+"_"+"disstatus"+"_"+"4"+"_"+"deliveryDate"+"_"+"null")
                      }else{
                        jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"3"+"_"+"disstatus"+"_"+disstatus+"_"+"deliveryDate"+"_"+deliveryDate)
                      }
                    }
                  }
                }else{
                  jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"-1"+"_"+"disstatus"+"_"+"4"+"_"+"deliveryDate"+"_"+"null")
                }
              }else{
                jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" +"0"+"_"+ "assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+"-1"+"_"+"disstatus"+"_"+"4"+"_"+"deliveryDate"+"_"+"null")
              }

            }

        })
    })




    sc.stop()
  }

}
