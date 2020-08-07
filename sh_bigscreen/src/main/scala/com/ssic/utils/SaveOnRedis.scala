package com.ssic.utils

import java.util

import org.apache.commons.lang3._
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Created by 云 on 2018/8/21.
  */
object SaveOnRedis {

  private val logger = LoggerFactory.getLogger(this.getClass)

  /**

    * * 将排菜分析数据存入到redis的临时表中platoon

    * * @param Iterator[(String, String, String, String, String, String)]  Iterator[(时间，区号，schoolid，表操作类型，创建时间, 是否有效)]

    */

  def PlatoonPlanReal(itr: Iterator[(String, String, String, String, String, String)]) = {
    val jedis = JPools.getJedis
    itr.foreach({
      x =>

        if ("delete".equals(x._4)) {
          jedis.hdel(x._1 + "_" + "platoon", x._2 + "_" + x._3)
          jedis.hdel(x._1 + "_" + "platoon", "null" + "_" + x._3)
        } else if ("update".equals(x._4)) {
          if ("0".equals(x._6)) {
            jedis.hdel(x._1 + "_" + "platoon", x._2 + "_" + x._3)
            jedis.hdel(x._1 + "_" + "platoon", "null" + "_" + x._3)
          } else {
            jedis.hset(x._1 + "_" + "platoon", x._2 + "_" + x._3, "1" + "_" + "create-time" + "_" + x._5)
          }
        } else {
          jedis.hset(x._1 + "_" + "platoon", x._2 + "_" + x._3, "1" + "_" + "create-time" + "_" + x._5)
        }

    })
  }

  /**

    * * 学校实时使用排菜系统功能的情况展现

    * * @param Iterator[(String, String)]  Iterator[(时间，学校id)]

    */
  def DisplayRealTime(itr: Iterator[(String, String)]) = {
    val jedis = JPools.getJedis
    itr.foreach({
      x =>
        jedis.hincrBy(x._1.split(" ")(0) + "_" + "schoolDisplay", x._1.split(" ")(1).substring(0, 5), 1)
        jedis.expire(x._1.split(" ")(0) + "_" + "schoolDisplay", 86400)
    })
  }

  //用料计划的上海市，各区，学校类型，餐饮类型的计算指标
  def MaterialConfirmRealTime(itr: Iterator[(String, String, String, String, String, String, String, String, String, String)]) = {
    val jedis = JPools.getJedis
    itr.foreach({
      x =>
        //时间，类型，团餐公司ID,区，用料状态，旧的用料状态，"new",stat,旧的stat,表的类型

        if ("insert".equals(x._10) && "1".equals(x._8)) {
          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai", 1)
          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4, 1)

          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai" + "_" + "status" + "_" + x._5, 1)
          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4 + "_" + "status" + "_" + x._5, 1)
        } else if ("delete".equals(x._10) && "1".equals(x._8)) {
          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai", -1)
          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4, -1)

          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai" + "_" + "status" + "_" + x._5, -1)
          jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4 + "_" + "status" + "_" + x._5, -1)
        } else if ("update".equals(x._10)) {
          if (StringUtils.isNoneEmpty(x._9) && !x._9.equals("null")) {
            if ("0".equals(x._8)) {
              jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai", -1)
              jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4, -1)

              jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai" + "_" + "status" + "_" + x._5, -1)
              jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4 + "_" + "status" + "_" + x._5, -1)
            } else {
              logger.info("用料计划指标计算是更新状态,数据从无效数据变成有效数据")
            }
          } else {
            if ("1".equals(x._8)) {
              if (StringUtils.isNoneEmpty(x._6) && !x._6.equals("null")) {
                jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai" + "_" + "status" + "_" + x._5, 1)
                jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "shanghai" + "_" + "status" + "_" + x._6, -1)
                jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4 + "_" + "status" + "_" + x._5, 1)
                jedis.hincrBy(x._1 + "_" + "useMaterialPlan", "area" + "_" + x._4 + "_" + "status" + "_" + x._6, -1)
              } else {
                logger.info("用料计划指标计算是更新状态，数据有效但是状态没有发生变化，不计算")
              }
            } else {
              logger.info("用料计划指标计算是更新状态，数据无效不计算")
            }
          }
        } else {
          logger.info("用料计划指标计算不需要计算的表的状态")
        }

    })
  }

  //用料计划详细的计算
  //时间，类型，团餐公司ID,区，用料状态，旧的用料状态，"new",stat,旧的stat,表的类型,项目点名称,项目点id
  def MaterialDetailRealTime(itr: Iterator[(String, String, String, String, String, String, String, String, String, String, String, String)]) = {
    val jedis = JPools.getJedis
    itr.foreach({
      x =>
        //用料类别，项目点名称，项目点id,团餐公司名称

        if ("delete".equals(x._10)) {
          jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + x._4 + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._12 + "_" + "suppliername" + "_" + x._3)
        } else if ("insert".equals(x._10) && "1".equals(x._8)) {
          jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + x._4 + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._12 + "_" + "suppliername" + "_" + x._3, x._5)
        } else if ("update".equals(x._10)) {
          if ("0".equals(x._8)) {
            jedis.hdel(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + x._4 + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._12 + "_" + "suppliername" + "_" + x._3)
          } else {
            //实际数据中可能不存在更新时把有效数据更新为无效数据
            jedis.hset(x._1 + "_" + "useMaterialPlanDetail", "area" + "_" + x._4 + "_" + "type" + "_" + x._2 + "_" + "name" + "_" + x._11 + "_" + "projid" + "_" + x._12 + "_" + "suppliername" + "_" + x._3, x._5)
          }
        } else {
          logger.info("不需要计算的用料计划详情------------------------------------")

        }

    })
  }

  //配送计划的上海市，各区计算指标
  //date, "shanghai", quhao, haul_status, old_haul_status, typ, stat,x._2(9))
  //时间，上海，区号，状态，旧状态，表类型，stat，旧的stat
  def DistributionRealTime(itr: Iterator[(String, String, String, String, String, String, String, String)]) = {
    val jedis = JPools.getJedis
    itr.foreach({
      x =>
        if ("insert".equals(x._6) && "1".equals(x._7)) {
          jedis.hincrBy(x._1 + "_" + "Distribution", x._2, 1)
          jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3, 1)

          jedis.hincrBy(x._1 + "_" + "Distribution", x._2 + "_" + "status" + "_" + x._4, 1)

          jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3 + "_" + "status" + "_" + x._4, 1)

        } else if ("delete".equals(x._6)) {
          jedis.hincrBy(x._1 + "_" + "Distribution", x._2, -1)
          jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3, -1)

          jedis.hincrBy(x._1 + "_" + "Distribution", x._2 + "_" + "status" + "_" + x._4, -1)
          jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3 + "_" + "status" + "_" + x._4, -1)
        } else if ("update".equals(x._6)) {
          //stat有更新
          if (StringUtils.isNoneEmpty(x._8) && x._8.equals("null")) {
            if ("0".equals(x._7)) {
              jedis.hincrBy(x._1 + "_" + "Distribution", x._2, -1)
              jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3, -1)

              jedis.hincrBy(x._1 + "_" + "Distribution", x._2 + "_" + "status" + "_" + x._4, -1)
              jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3 + "_" + "status" + "_" + x._4, -1)
            } else {
              logger.info("配送计划指标计算是更新状态,数据从无效数据变成有效数据")
            }
          } else {
            //stat没有更新
            //有效的数据
            if ("1".equals(x._7)) {
              //状态发生更新变化
              if (StringUtils.isNoneEmpty(x._5) && !x._5.equals("null")) {
                jedis.hincrBy(x._1 + "_" + "Distribution", x._2 + "_" + "status" + "_" + x._4, 1)
                jedis.hincrBy(x._1 + "_" + "Distribution", x._2 + "_" + "status" + "_" + x._5, -1)
                jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3 + "_" + "status" + "_" + x._4, 1)
                jedis.hincrBy(x._1 + "_" + "Distribution", "area" + "_" + x._3 + "_" + "status" + "_" + x._5, -1)
              } else {
                logger.info("配送计划指标的更新状态，数据是有效的，状态没有发生变化")
              }
            } else {
              logger.info("配送计划指标的更新状态，数据是无效的")
            }
          }
        } else {
          logger.info("配送计划不需要计算的--------------------------------------")
        }
    })
  }

  //配送计划的详细的计算

  /**

    * * 配送数据存入redis的临时表中

    * * @param Iterator[(String, String, String, String, String, String, Int, String, String, String, String, String, String, String, String)]
    * Iterator[(id,配送时间，配送类型，学校ID，团餐公司ID，发货批次，配送状态，统配,区号，表类型，stat,验收上报日期,进货日期,验收规则,验收日期)]

    */
  def DistributionDetailRealTime(itr: Iterator[(String, String, String, String, String, String, Int, String, String, String, String, String, String, String, String)]) = {
    val jedis = JPools.getJedis
    itr.foreach({
      x =>
        //id,配送时间，配送类型，学校ID，团餐公司ID，发货批次，配送状态，统配,区号，表类型，stat,验收上报日期,进货日期,验收规则,验收日期
        logger.info(x._1 + "_" + x._2 + "_" + x._3 + "_" + x._4 + "_" + x._5 + "_" + x._6 + "_" + x._7 + "_" + x._8 + "_" + x._9 + "_" + x._10 + "_" + x._11)
        if (jedis.exists(x._2 + "_" + "DistributionDetail").equals(true)) {
          val strings: util.Set[String] = jedis.hkeys(x._2 + "_" + "DistributionDetail")
            if ("insert".equals(x._10) && "1".equals(x._11)) {
              if (StringUtils.isNoneEmpty(x._8) && !x._8.equals("null")) {
                jedis.hset(x._2 + "_" + "DistributionDetail", "id" + "_" + x._1 + "_" + "type" + "_" + x._3 + "_" + "schoolid" + "_" + x._4 + "_" + "area" + "_" + x._9 + "_" + "sourceid" + "_" + x._5 + "_" + "batchno" + "_" + x._6 + "_" + "delivery" + "_" + x._8, x._7 + "_deliveryDate" + "_" + x._12+ "_" + "disstatus" + "_" + x._14 + "_" + "purchaseDate" + "_" + x._13 + "_" + "deliveryReDate" + "_" + x._15)
              } else {
                jedis.hset(x._2 + "_" + "DistributionDetail", "id" + "_" + x._1 + "_" + "type" + "_" + x._3 + "_" + "schoolid" + "_" + x._4 + "_" + "area" + "_" + x._9 + "_" + "sourceid" + "_" + x._5 + "_" + "batchno" + "_" + x._6 + "_" + "delivery" + "_" + "直配", x._7 + "_deliveryDate" + "_" + x._12+ "_" + "disstatus" + "_" + x._14 + "_" + "purchaseDate" + "_" + x._13 + "_" + "deliveryReDate" + "_" + x._15)
              }

            } else if ("update".equals(x._10)) {
              for (i <- strings.asScala) {
                val id = i.split("_type_")(0)
                if (id.equals("id_" + x._1)) {

                  if ("0".equals(x._11)) {
                    jedis.hdel(x._2 + "_" + "DistributionDetail", i)
                  } else if ("1".equals(x._11)) {
                    jedis.hset(x._2 + "_" + "DistributionDetail", i, x._7 + "_deliveryDate" + "_" + x._12+ "_" + "disstatus" + "_" + x._14 + "_" + "purchaseDate" + "_" + x._13 + "_" + "deliveryReDate" + "_" + x._15)
                  }
                }
              }

            } else if ("delete".equals(x._10)) {
              for (i <- strings.asScala) {
                val id = i.split("_type_")(0)
                if (id.equals("id_" + x._1)) {
                  jedis.hdel(x._2 + "_" + "DistributionDetail", i)
                }
              }
            } else {
              logger.info("配送计划详情不需要计算的---------------------------------------")
            }

          }
        else {
          jedis.hset(x._2 + "_" + "DistributionDetail", "id" + "_" + x._1 + "_" + "type" + "_" + x._3 + "_" + "schoolid" + "_" + x._4 + "_" + "area" + "_" + x._9 + "_" + "sourceid" + "_" + x._5 + "_" + "batchno" + "_" + x._6 + "_" + "delivery" + "_" + x._8, x._7 + "_deliveryDate" + "_" + x._12+ "_" + "disstatus" + "_" + x._14 + "_" + "purchaseDate" + "_" + x._13 + "_" + "deliveryReDate" + "_" + x._15)
        }

    })
  }

}
