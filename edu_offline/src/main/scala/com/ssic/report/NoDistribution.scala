package com.ssic.report

import java.util
import java.util.Date

import com.ssic.report.TargetChild.format
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SQLContext

import scala.collection.JavaConverters._


object NoDistribution {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setMaster("spark://172.16.10.17:7077").setAppName("大数据运营管理后台离线数据")
      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .set("spark.debug.maxToStringFields", "200")

    val sc = new SparkContext(sparkConf)
    val sqlContext = new SQLContext(sc)
    val session = sqlContext.sparkSession

    val date = format.format(new Date())

    val jedis = JPools.getJedis
    val dischild = jedis.hgetAll(date + "_DistributionTotal_child_test")
    val dischilddata = sc.parallelize(dischild.asScala.toList)  //配送子页面数据

    val platoon: util.Map[String, String] = jedis.hgetAll(date + "_platoon-feed")
    val platoonData = sc.parallelize(platoon.asScala.toList)    //学校的供餐数据

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
              val shipp = Rule.emptyToInt(value.split("shipp_")(1).split("_")(0))  //已配送数量
              val assign = Rule.emptyToInt(value.split("assign_")(1).split("_")(0))   //已指派数量
              val accept = Rule.emptyToInt(value.split("accept_")(1).split("_")(0))   //已验收数量
              val disTotalNum = Rule.emptyToInt(value.split("total_")(1).split("_")(0))  //总数

              if(disTotalNum != 0) {

                if((disTotalNum  - assign) != 0){
                  //未指派不为0的时候，所有状态为否
                  jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"-1")
                }else{
                  if(disTotalNum  - shipp != 0){
                    //未指派为0的时候,已指派为0的时候,未配送不为0的时候，指派状态为是，其他状态为否
                    jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"1")
                  }else{
                    if(disTotalNum  - accept != 0){
                      //未指派为0的时候,已指派为0的时候,未配送为0的时候，已配送不为0的时候，指派状态为是，配送状态为是，其他状态为否
                      jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"2")
                    }else{
                      jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"3")
                    }
                  }
                }
              }else{
                jedis.hset(date + "_DistributionTotal_child",k,value.split("status")(0)+"status"+"_"+"-1")
              }
            }

        })
    })




    sc.stop()
  }

}
