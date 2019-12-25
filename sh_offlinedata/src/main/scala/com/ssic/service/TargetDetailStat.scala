package com.ssic.service

import com.ssic.impl.TargetDetailFunc
import com.ssic.utils.JPools
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class TargetDetailStat extends TargetDetailFunc {

  override def usematerial(data: (RDD[(String, String, String, String, String, String)], RDD[(String, String)], String)): Unit = {

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

  override def distribution(data: (RDD[(String, String, String, String, String, String, String)], RDD[(String, String)], String)): Unit = {

    data._1.map(x => (((x._5.split("_")(11),x._5.split("_")(5)),(x._3.split("_")(0),x._5,x._3)))).groupByKey().mapValues(x => x.toList.sortBy(x => x._1 ).reverse(0)).map({
      x =>

        (x._2._2,x._2._3)
    })
    //data._1.map(x => (x._5, x._3))
      .cogroup(data._2).foreachPartition({
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

  override def retentiondish(data: (RDD[(String, String, String, String, String, String, String, String)], RDD[(String, String)], String, Broadcast[Map[String, String]], RDD[(String, String)])): Unit = {
    //对数仓存在的当天的菜品数据进行处理，过滤掉不供餐的数据
    val dishmenu = data._1.map({
      x =>
        val value = data._4.value.getOrElse(x._3 + "_" + x._2, "null")
        (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, value)
    }).filter(x => !x._9.equals("null")).filter(x => !x._5.equals("null")).filter(x => !x._9.split("_")(0).equals("不供餐")).map({
      x =>
        //package_id,school_id,area,menu_group_name,dishes_name,cater_type_name,dishes_number,supplier_id
        (x._1 + "_" + x._4 + "_catertypename_" + x._6 + "_dishesname_" + x._5, x._1 + "area" + "_" + x._3 + "_" + "supplierid" + "_" + x._8 + "_" + "schoolid" + "_" + x._2 + "_" + "groupname" + "_" + x._4 + "_" + "dishesname" + "_" + x._5 + "_" + "dishesnumber" + "_" + x._7 + "_" + "catertypename" + "_" + x._6)
    })

    //对留样临时表的留样数据进行处理，因为留样时间是新加上的
    val retentiondishDa = data._2.map({
      x =>
        val id = x._2.split("_")(1)
        if (x._2.contains("reservedata").equals(true)) {
          (id + "_" + x._2.split("groupname_")(1).split("_reservedata")(0), x._2)
        } else {
          (id + "_" + x._2.split("groupname_")(1), x._2)
        }
    })

    dishmenu.leftOuterJoin(retentiondishDa).map(x => (x._1, x._2._1, x._2._2.getOrElse("null"))).map({
      x =>
        if ("null".equals(x._3)) {

          (x._1, "area" + x._2.split("area")(1) + "_" + "未留样" + "_createtime_" + "null" + "_" + "creator" + "_" + "null" + "_" + "quantity" + "_" + "null" + "_" + "remark" + "_" + "null" + "_" + "reservedata" + "_" + "null"+"_"+"reservestatus"+"_"+"4")

        } else {
          val reserveStatus = new RuleStatusStat().reservestatus((data._3,x._3.split("createtime_")(1).split("_")(0)))
          if (x._3.contains("reservedata").equals(true)) {
            (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6) + "_" + "reservedata_" + x._3.split("reservedata_")(1)+"_"+"reservestatus"+"_"+reserveStatus)
            //            if(x._3.split("reservedata_")(1).size >=28){
            //              (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+"_"+"reservedata_"+x._3.split("reservedata_")(1).split(" ")(0)+" "+x._3.split("reservedata_")(1).split(" ")(2))
            //            }else{
            //              (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+"_"+"reservedata_"+x._3.split("reservedata_")(1))
            //            }

          } else {
            (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+ "_" + "reservedata" + "_" + "null"+"_"+"reservestatus"+"_"+reserveStatus)
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

  override def todayretentiondish(data: (RDD[(String, String, String, String, String, String, String, String)], RDD[(String, String)], String, Broadcast[Map[String, String]], RDD[(String, String)], RDD[(String, String)], Broadcast[Map[String, String]])): Unit = {
    //当天排菜的redis的排菜数据
    val todaydishmenu = data._6.map({
      x =>
        val package_id = x._1.split("_")(0)
        //supplierid_32ecf6e6-9177-44d7-bfa2-b8bb000797a6_schoolid_afbb82e0-a7ae-40fe-81fb-9559e61e9e5a_groupname_国内班_dishesname_达能夹心饼干_dishesnumber_396_catertypename_早点_ledgertype_1
        val supplierid = x._2.split("_")(1)
        val catertypename = x._2.split("_")(11)
        val dishesname = x._2.split("_")(7)
        val dishesnumber = x._2.split("_")(9)
        val groupname = x._2.split("_")(5)
        val schoolid = x._2.split("_")(3)
        val area = data._7.value.getOrElse(schoolid, "null")
        (package_id + "_" + groupname + "_catertypename_" + catertypename + "_dishesname_" + dishesname, package_id + "area" + "_" + area + "_" + "supplierid" + "_" + supplierid + "_" + "schoolid" + "_" + schoolid + "_" + "groupname" + "_" + groupname + "_" + "dishesname" + "_" + dishesname + "_" + "dishesnumber" + "_" + dishesnumber + "_" + "catertypename" + "_" + catertypename)
    })


    //对数仓存在的当天的菜品数据进行处理，过滤掉不供餐的数据
    val dishmenu = data._1.map({
      x =>
        val value = data._4.value.getOrElse(x._3 + "_" + x._2, "null")
        (x._1, x._2, x._3, x._4, x._5, x._6, x._7, x._8, value)
    }).filter(x => !x._9.equals("null")).filter(x => !x._5.equals("null")).filter(x => !x._9.split("_")(0).equals("不供餐")).map({
      x =>
        //package_id,school_id,area,menu_group_name,dishes_name,cater_type_name,dishes_number,supplier_id
        (x._1 + "_" + x._4 + "_catertypename_" + x._6 + "_dishesname_" + x._5, x._1 + "area" + "_" + x._3 + "_" + "supplierid" + "_" + x._8 + "_" + "schoolid" + "_" + x._2 + "_" + "groupname" + "_" + x._4 + "_" + "dishesname" + "_" + x._5 + "_" + "dishesnumber" + "_" + x._7 + "_" + "catertypename" + "_" + x._6)
    }).union(todaydishmenu)

    //对留样临时表的留样数据进行处理，因为留样时间是新加上的
    val retentiondishDa = data._2.map({
      x =>
        val id = x._2.split("_")(1)
        if (x._2.contains("reservedata").equals(true)) {
          (id + "_" + x._2.split("groupname_")(1).split("_reservedata")(0), x._2)
        } else {
          (id + "_" + x._2.split("groupname_")(1), x._2)
        }
    })

    dishmenu.leftOuterJoin(retentiondishDa).map(x => (x._1, x._2._1, x._2._2.getOrElse("null"))).map({
      x =>
        if ("null".equals(x._3)) {

          (x._1, "area" + x._2.split("area")(1) + "_" + "未留样" + "_createtime_" + "null" + "_" + "creator" + "_" + "null" + "_" + "quantity" + "_" + "null" + "_" + "remark" + "_" + "null" + "_" + "reservedata" + "_" + "null"+"_"+"reservestatus"+"_"+"4")

        } else {
          val reserveStatus = new RuleStatusStat().reservestatus((data._3,x._3.split("createtime_")(1).split("_")(0)))
          if (x._3.contains("reservedata").equals(true)) {
            (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6) + "_" + "reservedata_" + x._3.split("reservedata_")(1)+"_"+"reservestatus"+"_"+reserveStatus)
            //            if(x._3.split("reservedata_")(1).size >=28){
            //              (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+"_"+"reservedata_"+x._3.split("reservedata_")(1).split(" ")(0)+" "+x._3.split("reservedata_")(1).split(" ")(2))
            //            }else{
            //              (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+"_"+"reservedata_"+x._3.split("reservedata_")(1))
            //            }

          } else {
            (x._1, "area" + x._2.split("area")(1) + "_" + "已留样" + "_createtime_" + x._3.split("createtime_")(1).split("_")(0) + "_" + "creator" + "_" + x._3.split("createtime_")(1).split("_")(2) + "_" + "quantity" + "_" + x._3.split("createtime_")(1).split("_")(4) + "_" + "remark" + "_" + x._3.split("createtime_")(1).split("_")(6)+ "_" + "reservedata" + "_" + "null"+"_"+"reservestatus"+"_"+reserveStatus)
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
