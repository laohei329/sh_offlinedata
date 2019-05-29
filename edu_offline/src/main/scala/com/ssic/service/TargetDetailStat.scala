package com.ssic.service

import com.ssic.impl.TargetDetailFunc
import com.ssic.utils.JPools
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class TargetDetailStat extends TargetDetailFunc{

  override def usematerial(data: (RDD[(String, String,String,String,String,String)], RDD[(String, String)], String)): Unit = {

    data._1.map(x => (x._4, x._3)).cogroup(data._2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(data._3 + "_useMaterialPlan-Detail", k)
            } else {
              jedis.hset(data._3 + "_useMaterialPlan-Detail", k, v._1.head)
            }
        })
    })
  }

  override def distribution(data: (RDD[(String, String,String,String,String,String)], RDD[(String, String)], String)): Unit = {

    data._1.map(x => (x._5, x._3)).cogroup(data._2).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(data._3 + "_Distribution-Detail", k)
            } else {
              jedis.hset(data._3 + "_Distribution-Detail", k, v._1.head)
            }
        })
    })

  }

  override def retentiondish(data:(RDD[(String, String, String, String, String, String, String, String)],RDD[(String, String)],String,Broadcast[Map[String, String]],RDD[(String, String)])): Unit = {
    //对数仓存在的当天的菜品数据进行处理，过滤掉不供餐的数据
    val dishmenu =data._1.map({
      x =>
        val value = data._4.value.getOrElse(x._3 + "_" + x._2, "null")
        (x._1,x._2,x._3,x._4,x._5,x._6,x._7,x._8,value)
    }).filter(x => !x._9.equals("null")).filter(x => !x._5.equals("null")).filter(x => !x._9.split("_")(0).equals("不供餐")).map({
      x =>
        //package_id,school_id,area,menu_group_name,dishes_name,cater_type_name,dishes_number,supplier_id
        (x._1 + "_" + x._4 + "_catertypename_" + x._6 + "_dishesname_" + x._5, x._1 + "area" + "_" + x._3+"_"+"supplierid"+"_"+x._8+"_"+"schoolid"+"_"+x._2+"_"+"groupname"+"_"+x._4+"_"+"dishesname"+"_"+x._5+"_"+"dishesnumber"+"_"+x._7+"_"+"catertypename"+"_"+x._6)
    })

    //对留样临时表的留样数据进行处理，因为留样时间是新加上的
    val retentiondishDa = data._2.map({
      x =>
        val id = x._2.split("_")(1)
        if(x._2.contains("reservedata").equals(true)){
          (id + "_" + x._2.split("groupname_")(1).split("_reservedata")(0), x._2)
        }else{
          (id + "_" + x._2.split("groupname_")(1), x._2)
        }
    })

    dishmenu.leftOuterJoin(retentiondishDa).map(x => (x._1, x._2._1, x._2._2.getOrElse("null"))).map({
      x =>
        if ("null".equals(x._3)) {

          (x._1, "area" + x._2.split("area")(1) + "_" + "未留样" + "_createtime_" + "null" + "_" + "creator" + "_" + "null" + "_" + "quantity" + "_" + "null" + "_" + "remark" + "_" + "null")

        } else {
          if(x._3.contains("reservedata").equals(true)){
            if(x._3.split("reservedata_")(1).size >=28){
              (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+"_"+"reservedata_"+x._3.split("reservedata_")(1).split(" ")(0)+" "+x._3.split("reservedata_")(1).split(" ")(2))
            }else{
              (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+"_"+"reservedata_"+x._3.split("reservedata_")(1))
            }

          }else{
            (x._1, "area" + x._2.split("area")(1)+ "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6))
          }
        }

    }).cogroup(data._5).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(data._3 + "_gc-retentiondish", k)
            } else {
              jedis.hset(data._3 + "_gc-retentiondish", k, v._1.head)
            }
        })
    })
  }
}
