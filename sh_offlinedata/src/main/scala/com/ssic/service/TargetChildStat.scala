package com.ssic.service

import com.ssic.impl.TargetChildFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.rdd.RDD

class TargetChildStat extends TargetChildFunc {

  /**
    *  用料计划的子页面,没有产生用料计划的的学校也要放入到子页面中
    *  @param platoonData 学校的供餐数据    例如: (9_cfee5c7f-7c7a-4443-b97c-d138754d0f1c,供餐_已排菜_create-time_2019-04-11 17:16:25_reason_null_plastatus_1)
    *  @param useValue 用料计划的子页面数据    [区_学校id ,List("area" + "_" + 区 + "_" + "type" + 类型 + "name" + "_" + schoolname + "_" + "projid" + 项目点id, 状态)]
    *  @param date 时间
    *  @param useMaterialChildData 已存在的用料计划子页面数据
    */

  override def usematerialchild(platoonData:RDD[(String, String)],useValue:RDD[(String, List[String])],date:String,useMaterialChildData:RDD[(String, String)]): Unit = {

    val allSchoolUse = platoonData.filter(x => !x._2.split("_")(0).equals("不供餐")).leftOuterJoin(useValue).map({
      x =>
        val usmaterialList = x._2._2.getOrElse(List("null"))
        if ("null".equals(usmaterialList(0))) {
          //与应供餐的学校关联不上的数据，表示该学校没有用料计划数据
          (x._1, "-1")
        } else {
          if ("0".equals(usmaterialList(1))) {
            (x._1, "1")
          } else {
            (x._1, usmaterialList(1))   // (区_学校id ,("area" + "_" + 区 + "_" + "type" + 类型 + "name" + "_" + schoolname + "_" + "projid" + 项目点id, 状态))
          }
        }
    })

    //每个项目点有多少条用料数据
    val allSchoolUseTotal = allSchoolUse.map(x => (x._1, 1)).reduceByKey(_ + _)
    val allSchoolUseStatusTotal = allSchoolUse.map((_, 1)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    allSchoolUseTotal.leftOuterJoin(allSchoolUseStatusTotal).map(x => (x._1, (x._2._1, x._2._2.getOrElse("null", -1)))).filter(x => x._2._2._2 != -1)
      .cogroup(useMaterialChildData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {

              jedis.hdel(date + "_useMaterialPlanTotal_child", k)

            } else {

              if ("-1".equals(v._1.head._2._1)) {
                //该学校没有用料计划
                jedis.hset(date + "_useMaterialPlanTotal_child", k, "total" + "_" + "0" + "_" + "usematerial" + "_" + "0" + "_" + "nousematerial" + "_" + "0" + "_" + "status" + "_" + "1")

              } else {
                //该学校有用料计划
                if ("1".equals(v._1.head._2._1)) {
                  if (v._1.head._2._2 != 0) {
                    jedis.hset(date + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "nousematerial" + "_" + v._1.head._2._2 + "_" + "status" + "_" + "1")

                  } else {
                    jedis.hset(date + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "nousematerial" + "_" + v._1.head._2._2 + "_" + "status" + "_" + "2")
                  }

                } else {
                  if ((v._1.head._1 - v._1.head._2._2) != 0) {
                    jedis.hset(date + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + v._1.head._2._2 + "_" + "nousematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "1")
                  } else {
                    jedis.hset(date + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + v._1.head._2._2 + "_" + "nousematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "2")
                  }

                }
              }
            }
        })
    })
  }
  /**
    * 配送计划的子页面，没有产生配送计划的学校也要放入到子页面中
    * @param platoonData 学校的供餐数据
    * @param disValue 配送计划的子页面数据
    * @param date 时间
    * @param distributionChildData 已存在的配送计划子页面数据
    */
  override def distributionchild(platoonData:RDD[(String, String)],disValue:RDD[(String, String)],date:String,distributionChildData:RDD[(String, String)]): Unit = {
    val allSchoolUse = platoonData.filter(x => !x._2.split("_")(0).equals("不供餐")).leftOuterJoin(disValue).map({
      x =>
        val distributionList = x._2._2.getOrElse("null")

        if ("null".equals(distributionList)) {
          //与应供餐的学校关联不上的数据，表示该学校没有配送计划数据
          (x._1, "-3", "4","null")
        } else {
          val status = distributionList.split("_")(0)
          val disstatus = distributionList.split("_")(1)
          val deliveryDate = distributionList.split("_")(2)
          if ("3".equals(status)) {
            (x._1, status, disstatus,deliveryDate)
          } else if ("2".equals(status)) {
            (x._1, "2", disstatus,deliveryDate)
          } else if ("1".equals(status)) {
            (x._1, "0", disstatus,deliveryDate)
          } else if ("0".equals(status)) {
            (x._1, "0", disstatus,deliveryDate)
          } else {
            (x._1, "-1", disstatus,deliveryDate)
          }
        }
    })


    val allSchoolUseTotal = allSchoolUse.map(x => (x._1, 1)).reduceByKey(_ + _) //每个学校的配送订单总数
    val allSchoolUseStatusTotal = allSchoolUse.map(x => ((x._1, x._2), 1)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2))) //每个学校的每个验收状态的订单总数
    val allSchoolUseDisStatusTotal = allSchoolUse.map(x => (x._1, x._3)).coalesce(1).sortBy(x => x._2).groupByKey().mapValues(x => x.toList.reverse(0)) //每个学校的每个配送操作状态的倒序，取第一个值
    val allSchooldeliveryDateTotal =allSchoolUse.map(x => (x._1, x._4)).coalesce(1).sortBy(x => x._2).groupByKey().mapValues(x => x.toList.reverse(0)) //每个学校的每个配送上报时间的倒序，取第一个值

    //RDD[(String, (((Int, Option[(String, Int)]), Option[String]), Option[String]))]
    allSchoolUseTotal.leftOuterJoin(allSchoolUseStatusTotal).leftOuterJoin(allSchoolUseDisStatusTotal).leftOuterJoin(allSchooldeliveryDateTotal)
      .map(x => (x._1,(x._2._1._1._1,x._2._1._1._2.getOrElse("null", -1),x._2._1._2.getOrElse("null"),x._2._2.getOrElse("-1")))).filter(x => x._2._2._2 != -1).filter(x => !"null".equals(x._2._3)).filter(x => !"-1".equals(x._2._4)).map(x => ("area" + "_" + x._1.split("_")(0) + "_" + "id" + "_" + x._1.split("_")(1), (x._2._1, x._2._2, x._2._3,x._2._4))).sortBy(x => x._2._2._1, false).foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              val value = jedis.hget(date + "_DistributionTotal_child", k)
              if (StringUtils.isNoneEmpty(value) && !value.equals("null")) {
                if ("3".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + v._2._2 + "_" + "assign" + "_" + v._2._2 + "_" + "shipp" + "_" + v._2._2 + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("2".equals(v._2._1)) {
                  //待验收
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + value.split("accept_")(1).split("_")(0) + "_" + "assign" + "_" + (value.split("accept_")(1).split("_")(0).toInt + v._2._2) + "_" + "shipp" + "_" + (value.split("accept_")(1).split("_")(0).toInt + v._2._2) + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                }
                else if ("0".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + value.split("accept_")(1).split("_")(0) + "_" + "assign" + "_" + (value.split("shipp_")(1).split("_")(0).toInt + v._2._2) + "_" + "shipp" + "_" + value.split("shipp_")(1).split("_")(0) + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("-1".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + value.split("accept_")(1).split("status")(0) + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("-3".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" + "0" + "_" + "assign" + "_" + "0" + "_" + "shipp" + "_" + "0" + "_" + "status" + "_" + "-1" + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                }
              } else {
                if ("3".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + v._2._2 + "_" + "assign" + "_" + v._2._2 + "_" + "shipp" + "_" + v._2._2 + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("2".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + "0" + "_" + "assign" + "_" + v._2._2 + "_" + "shipp" + "_" + v._2._2 + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("0".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + "0" + "_" + "assign" + "_" + v._2._2 + "_" + "shipp" + "_" + "0" + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("-1".equals(v._2._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + v._1 + "_" + "accept" + "_" + "0" + "_" + "assign" + "_" + "0" + "_" + "shipp" + "_" + "0" + "_" + "status" + "_" + v._2._1 + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                } else if ("-3".equals(v._1)) {
                  jedis.hset(date + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" + "0" + "_" + "assign" + "_" + "0" + "_" + "shipp" + "_" + "0" + "_" + "status" + "_" + "-1" + "_" + "disstatus" + "_" + v._3+"_"+"deliveryDate"+"_"+v._4)
                }
              }
          })
      })

    //    allSchoolUseTotal.leftOuterJoin(allSchoolUseStatusTotal).map(x => (x._1, (x._2._1,x._2._2.getOrElse(("null",-1)) ))).filter(x => x._2._2._2 != -1).map(x => ("area" + "_" + x._1.split("_")(0) + "_" + "id" + "_" + x._1.split("_")(1), x._2)).sortBy(x => x._2._2._1,false)
    //     .foreachPartition({
    //      itr =>
    //        val jedis = JPools.getJedis
    //        itr.foreach({
    //          case (k, v) =>
    //
    //            //表示左边没有，右边有
    ////            if (v._1.size == 0) {
    ////
    ////              jedis.hdel(data._3 + "_DistributionTotal_child", k)
    ////
    ////            }else{
    //              val value = jedis.hget(data._3 + "_DistributionTotal_child",k)
    //              if(StringUtils.isNoneEmpty(value) && !value.equals("null")){
    //
    //                if("3".equals(v._2._1)){
    //
    //                    jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+v._2._2 +"_"+"assign"+"_"+v._2._2 +"_"+"shipp"+"_"+v._2._2 +"_"+"status"+"_"+v._2._1)
    //
    //                }else if("2".equals(v._2._1)){
    //                  //待验收
    //                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+value.split("accept_")(1).split("_")(0)+"_"+"assign"+"_"+(value.split("accept_")(1).split("_")(0).toInt+v._2._2)+"_"+"shipp"+"_"+(value.split("accept_")(1).split("_")(0).toInt+v._2._2)+"_"+"status"+"_"+v._2._1)
    //                }
    //                else if("0".equals(v._2._1)){
    //                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+value.split("accept_")(1).split("_")(0)+"_"+"assign"+"_"+(value.split("shipp_")(1).split("_")(0).toInt+v._2._2)+"_"+"shipp"+"_"+value.split("shipp_")(1).split("_")(0)+"_"+"status"+"_"+v._2._1)
    //                }else if("-1".equals(v._2._1)) {
    //                  jedis.hset(data._3 + "_DistributionTotal_child",k, "total" + "_" + v._1 + "_" + "accept" + "_" + value.split("accept_")(1).split("status")(0)+"status"+"_"+v._2._1)
    //                }else if("-3".equals(v._2._1)) {
    //                  jedis.hset(data._3 + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" +"0"+"_"+ "assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+"-1")
    //                }
    //              }else{
    //                if("3".equals(v._2._1)){
    //                    jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+v._2._2+"_"+"assign"+"_"+v._2._2+"_"+"shipp"+"_"+v._2._2+"_"+"status"+"_"+v._2._1)
    //
    //                }else if("2".equals(v._2._1)){
    //                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+"0"+"_"+"assign"+"_"+v._2._2+"_"+"shipp"+"_"+v._2._2+"_"+"status"+"_"+v._2._1)
    //                }else if("0".equals(v._2._1)){
    //                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+"0"+"_"+"assign"+"_"+v._2._2+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+v._2._1)
    //                }else if("-1".equals(v._2._1)){
    //                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+"0"+"_"+"assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+v._2._1)
    //                }else if("-3".equals(v._1)) {
    //                  jedis.hset(data._3 + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" +"0"+"_"+ "assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+"-1")
    //                }
    //              }
    //   //         }
    //
    //        })
    //    })

  }
  /**
    *  留样计划的子页面，没有产生留样计划的学校也要放入到子页面中
    *  @param platoonData 学校的供餐数据
    *  @param reValue 留样计划处理后的数据
    *  @param date 时间
    *  @param retentionChildData 已存在的留样计划子页面数据
    */
  override def retentionchild(platoonData:RDD[(String, String)],reValue:RDD[(String, String,String,String,String,String,String)], date:String,retentionChildData:RDD[(String, String)]): Unit = {

    //(area, liuyang, schoolid,reservestatus)
    val retentionData = reValue.map(x => (x._1 + "_" + x._3, x._2 + "_" + x._4+ "_" + x._5))
    val allSchoolUse = platoonData.filter(x => !x._2.split("_")(0).equals("不供餐")).leftOuterJoin(retentionData).map({
      x =>
        val distributionList = x._2._2.getOrElse("null")
        if ("null".equals(distributionList)) {
          //与应供餐的学校关联不上的数据，表示该学校没有排菜计划数据
          (x._1, "-1", "4", "null")
        } else {
          if ("已留样".equals(distributionList.split("_")(0))) {
            (x._1, "1", distributionList.split("_")(1), distributionList.split("_")(2))
          } else {
            (x._1, "0", distributionList.split("_")(1), distributionList.split("_")(2))
          }
        }
    })
    val retenTotal = allSchoolUse.map(x => (x._1, 1)).reduceByKey(_ + _) //各学校菜品数量

    val retenstatdata = allSchoolUse.map(x => ((x._1, x._2), 1)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2))) //各学校各留样状态数量

    val retendealstatusdata = allSchoolUse.map(x => (x._1, x._3)).coalesce(1).sortBy(x => x._2).groupByKey().mapValues(x => x.toList.reverse(0)) //各学校的留样操作状态的倒序，取第一个值

    val retencreatetimedata = allSchoolUse.map(x => (x._1, x._4)).coalesce(1).sortBy(x => x._2).groupByKey().mapValues(x => x.toList.reverse(0)) //各学校的留样操作时间的倒序，取第一个值

    retenTotal.leftOuterJoin(retenstatdata).leftOuterJoin(retendealstatusdata).leftOuterJoin(retencreatetimedata).map(x => (x._1, (x._2._1._1._1, x._2._1._1._2.getOrElse(("null", -1)), x._2._1._2.getOrElse("null"), x._2._2.getOrElse("-1")))).filter(x => x._2._2._2 != -1).filter(x => !"null".equals(x._2._3)).filter(x => !"-1".equals(x._2._4))
      //.map(x => (x._1, (x._2._1._1, x._2._1._2.getOrElse(("null", -1)), x._2._2.getOrElse("null")))).filter(x => x._2._2._2 != -1).filter(x => !"null".equals(x._2._3))
      .map(x => ("area" + "_" + x._1.split("_")(0) + "_" + "id" + "_" + x._1.split("_")(1), x._2))
      .cogroup(retentionChildData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {

              jedis.hdel(date + "_gc-retentiondishtotal_child", k)

            } else {

              if ("-1".equals(v._1.head._2._1)) {
                //该学校没有留样计划
                jedis.hset(date + "_gc-retentiondishtotal_child", k, "total" + "_" + "0" + "_" + "reserve" + "_" + "0" + "_" + "noreserve" + "_" + "0" + "_" + "status" + "_" + "0" + "_" + "reservestatus" + "_" + "4"+ "_" + "createtime" + "_" + "null")
              } else {
                //该学校有留样计划
                if ("0".equals(v._1.head._2._1)) {
                  if (v._1.head._2._2 != 0) {
                    jedis.hset(date + "_gc-retentiondishtotal_child", k, "total" + "_" + v._1.head._1 + "_" + "reserve" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "noreserve" + "_" + v._1.head._2._2 + "_" + "status" + "_" + "0" + "_" + "reservestatus" + "_" + "4"+ "_" + "createtime" + "_" + "null")
                  } else {
                    jedis.hset(date + "_gc-retentiondishtotal_child", k, "total" + "_" + v._1.head._1 + "_" + "reserve" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "noreserve" + "_" + v._1.head._2._2 + "_" + "status" + "_" + "1" + "_" + "reservestatus" + "_" +v._1.head._3+ "_" + "createtime" + "_" + v._1.head._4)
                  }

                } else {
                  if ((v._1.head._1 - v._1.head._2._2) != 0) {
                    jedis.hset(date + "_gc-retentiondishtotal_child", k, "total" + "_" + v._1.head._1 + "_" + "reserve" + "_" + v._1.head._2._2 + "_" + "noreserve" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "0" + "_" + "reservestatus" + "_" + "4"+ "_" + "createtime" + "_" + "null")
                  } else {
                    jedis.hset(date + "_gc-retentiondishtotal_child", k, "total" + "_" + v._1.head._1 + "_" + "reserve" + "_" + v._1.head._2._2 + "_" + "noreserve" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "1" + "_" + "reservestatus" + "_" +v._1.head._3+ "_" + "createtime" + "_" + v._1.head._4)
                  }

                }
              }
            }

        })

    })
  }
}
