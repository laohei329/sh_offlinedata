package com.ssic.service

import com.ssic.impl.TargetChildFunc
import com.ssic.utils.JPools
import org.apache.commons.lang3._
import org.apache.spark.rdd.RDD

class TargetChildStat extends TargetChildFunc {

  override def usematerialchild(data: (RDD[(String, String)], RDD[(String, List[String])], String, RDD[(String, String)])): Unit = {

    val allSchoolUse = data._1.filter(x => !x._2.split("_")(0).equals("不供餐")).leftOuterJoin(data._2).map({
      x =>
        val usmaterialList = x._2._2.getOrElse(List("null"))
        if ("null".equals(usmaterialList(0))) {
          //与应供餐的学校关联不上的数据，表示该学校没有用料计划数据
          (x._1, "-1")
        } else {
          if ("0".equals(usmaterialList(1))) {
            (x._1, "1")
          } else {
            (x._1, usmaterialList(1))
          }
        }
    })

    //每个项目点有多少条用料数据
    val allSchoolUseTotal = allSchoolUse.map(x => (x._1, 1)).reduceByKey(_ + _)
    val allSchoolUseStatusTotal = allSchoolUse.map((_, 1)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    allSchoolUseTotal.leftOuterJoin(allSchoolUseStatusTotal).map(x => (x._1, (x._2._1, x._2._2.getOrElse("null", -1)))).filter(x => x._2._2._2 != -1)
      .cogroup(data._4).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {

              jedis.hdel(data._3 + "_useMaterialPlanTotal_child", k)

            } else {

              if ("-1".equals(v._1.head._2._1)) {
                //该学校没有用料计划
                jedis.hset(data._3 + "_useMaterialPlanTotal_child", k, "total" + "_" + "0" + "_" + "usematerial" + "_" + "0" + "_" + "nousematerial" + "_" + "0" + "_" + "status" + "_" + "1")
              } else {
                //该学校有用料计划
                if ("1".equals(v._1.head._2._1)) {
                  if (v._1.head._2._2 != 0) {
                    jedis.hset(data._3 + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "nousematerial" + "_" + v._1.head._2._2 + "_" + "status" + "_" + "1")
                  } else {
                    jedis.hset(data._3 + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "nousematerial" + "_" + v._1.head._2._2 + "_" + "status" + "_" + "2")
                  }

                } else {
                  if ((v._1.head._1 - v._1.head._2._2) != 0) {
                    jedis.hset(data._3 + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + v._1.head._2._2 + "_" + "nousematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "1")
                  } else {
                    jedis.hset(data._3 + "_useMaterialPlanTotal_child", k, "total" + "_" + v._1.head._1 + "_" + "usematerial" + "_" + v._1.head._2._2 + "_" + "nousematerial" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "2")
                  }

                }
              }
            }
        })
    })
  }

  override def distributionchild(data: (RDD[(String, String)], RDD[(String, String)], String,RDD[(String, String)])): Unit = {
    val allSchoolUse = data._1.filter(x => !x._2.split("_")(0).equals("不供餐")).leftOuterJoin(data._2).map({
      x =>
        val distributionList = x._2._2.getOrElse("null")
        if ("null".equals(distributionList)) {
          //与应供餐的学校关联不上的数据，表示该学校没有配送计划数据
          (x._1, "-3")
        } else {
          if ("3".equals(distributionList)) {
            (x._1, "3")
          } else if ("2".equals(distributionList)) {
            (x._1, "2")
          } else if ("1".equals(distributionList)) {
            (x._1, "0")
          } else if ("0".equals(distributionList)) {
            (x._1, "0")
          } else {
            (x._1, "-1")
          }
        }
    })

    val allSchoolUseTotal = allSchoolUse.map(x => (x._1, 1)).reduceByKey(_ + _)
    val allSchoolUseStatusTotal = allSchoolUse.map((_, 1)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    allSchoolUseTotal.leftOuterJoin(allSchoolUseStatusTotal).map(x => (x._1, (x._2._1,x._2._2.getOrElse(("null",-1)) ))).filter(x => x._2._2._2 != -1).map(x => ("area" + "_" + x._1.split("_")(0) + "_" + "id" + "_" + x._1.split("_")(1), x._2)).sortBy(x => x._2._2._1,false)
     .foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>

            //表示左边没有，右边有
//            if (v._1.size == 0) {
//
//              jedis.hdel(data._3 + "_DistributionTotal_child", k)
//
//            }else{
              val value = jedis.hget(data._3 + "_DistributionTotal_child",k)
              if(StringUtils.isNoneEmpty(value) && !value.equals("null")){

                if("3".equals(v._2._1)){

                    jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+v._2._2 +"_"+"assign"+"_"+v._2._2 +"_"+"shipp"+"_"+v._2._2 +"_"+"status"+"_"+v._2._1)

                }else if("2".equals(v._2._1)){
                  //待验收
                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+value.split("accept_")(1).split("_")(0)+"_"+"assign"+"_"+(value.split("accept_")(1).split("_")(0).toInt+v._2._2)+"_"+"shipp"+"_"+(value.split("accept_")(1).split("_")(0).toInt+v._2._2)+"_"+"status"+"_"+v._2._1)
                }
                else if("0".equals(v._2._1)){
                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+value.split("accept_")(1).split("_")(0)+"_"+"assign"+"_"+(value.split("shipp_")(1).split("_")(0).toInt+v._2._2)+"_"+"shipp"+"_"+value.split("shipp_")(1).split("_")(0)+"_"+"status"+"_"+v._2._1)
                }else if("-1".equals(v._2._1)) {
                  jedis.hset(data._3 + "_DistributionTotal_child",k, "total" + "_" + v._1 + "_" + "accept" + "_" + value.split("accept_")(1).split("status")(0)+"status"+"_"+v._2._1)
                }else if("-3".equals(v._2._1)) {
                  jedis.hset(data._3 + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" +"0"+"_"+ "assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+"-1")
                }
              }else{
                if("3".equals(v._2._1)){
                    jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+v._2._2+"_"+"assign"+"_"+v._2._2+"_"+"shipp"+"_"+v._2._2+"_"+"status"+"_"+v._2._1)

                }else if("2".equals(v._2._1)){
                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+"0"+"_"+"assign"+"_"+v._2._2+"_"+"shipp"+"_"+v._2._2+"_"+"status"+"_"+v._2._1)
                }else if("0".equals(v._2._1)){
                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+"0"+"_"+"assign"+"_"+v._2._2+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+v._2._1)
                }else if("-1".equals(v._2._1)){
                  jedis.hset(data._3 + "_DistributionTotal_child",k,"total"+"_"+v._1+"_"+"accept"+"_"+"0"+"_"+"assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+v._2._1)
                }else if("-3".equals(v._1)) {
                  jedis.hset(data._3 + "_DistributionTotal_child", k, "total" + "_" + "0" + "_" + "accept" + "_" +"0"+"_"+ "assign"+"_"+"0"+"_"+"shipp"+"_"+"0"+"_"+"status"+"_"+"-1")
                }
              }
   //         }

        })
    }

    )
  }

  override def retentionchild(data: (RDD[(String, String)],RDD[(String, String, String)], String,RDD[(String, String)])): Unit = {

    //(area, liuyang, schoolid)
    val retentionData = data._2.map(x =>(x._1+"_"+x._3,x._2))
    val allSchoolUse = data._1.filter(x => !x._2.split("_")(0).equals("不供餐")).leftOuterJoin(retentionData).map({
      x =>
        val distributionList = x._2._2.getOrElse("null")
        if ("null".equals(distributionList)) {
          //与应供餐的学校关联不上的数据，表示该学校没有配送计划数据
          (x._1, "-1")
        } else {
          if ("已留样".equals(distributionList)) {
            (x._1, "1")
          } else {
            (x._1, "0")
          }
        }
    })
    val retenTotal = allSchoolUse.map(x => (x._1,1)).reduceByKey(_ + _)

    val retenstatdata = allSchoolUse.map((_, 1)).reduceByKey(_ + _).map(x => (x._1._1, (x._1._2, x._2)))

    retenTotal.leftOuterJoin(retenstatdata).map(x => (x._1, (x._2._1,x._2._2.getOrElse(("null",-1)) ))).filter(x => x._2._2._2 != -1).map(x => ("area" + "_" + x._1.split("_")(0) + "_" + "id" + "_" + x._1.split("_")(1), x._2))
      .cogroup(data._4).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>
            //表示左边没有，右边有
            if (v._1.size == 0) {

              jedis.hdel(data._3 + "_gc-retentiondishtotal_child", k)

            } else {

              if ("-1".equals(v._1.head._2._1)) {
                //该学校没有留样计划
                jedis.hset(data._3 + "_gc-retentiondishtotal_child", k, "total"+"_"+"0"+"_"+"reserve"+"_"+"0"+"_"+"noreserve"+"_"+"0"+"_"+"status"+"_"+"0" )
              } else {
                //该学校有用料计划
                if ("0".equals(v._1.head._2._1)) {
                  if (v._1.head._2._2 != 0) {
                    jedis.hset(data._3 + "_gc-retentiondishtotal_child", k, "total"+"_"+v._1.head._1+"_"+"reserve"+"_"+(v._1.head._1 - v._1.head._2._2)+"_"+"noreserve"+"_"+v._1.head._2._2+"_"+"status"+"_"+"0")
                  } else {
                    jedis.hset(data._3 + "_gc-retentiondishtotal_child", k,"total"+"_"+v._1.head._1+"_"+"reserve"+"_"+(v._1.head._1 - v._1.head._2._2)+"_"+"noreserve"+"_"+v._1.head._2._2+"_"+"status"+"_"+"1")
                  }

                } else {
                  if ((v._1.head._1 - v._1.head._2._2) != 0) {
                    jedis.hset(data._3 + "_gc-retentiondishtotal_child", k, "total" + "_" + v._1.head._1 + "_" + "reserve" + "_" + v._1.head._2._2 + "_" + "noreserve" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "0")
                  } else {
                    jedis.hset(data._3 + "_gc-retentiondishtotal_child", k, "total" + "_" + v._1.head._1 + "_" + "reserve" + "_" + v._1.head._2._2 + "_" + "noreserve" + "_" + (v._1.head._1 - v._1.head._2._2) + "_" + "status" + "_" + "1")
                  }

                }
              }
            }

        })

    })
  }
}
