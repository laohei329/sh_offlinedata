package com.ssic.report

import java.util
import java.util.{Calendar, Date}

import com.alibaba.fastjson.JSON
import com.ssic.beans.{B2bDelivery, _}
import com.ssic.utils.{JPools, MysqlUtils, Rule}
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Created by 云 on 2018/8/20.
  * 配送计划功能指标
  */
object Distribution {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")


  /**
    *
    * * 配送主表分析数据
    *
    * * @param filterData  mysql的业务binlgog日志
    *
    * * @param return  RDD[(String, List[String])] 主键id list[时间，配送类型，学校ID，团餐公司ID，发货批次，配送状态，历史发货状态，表操作类型，有效状态，历史有效状态，验收上报日期，进货日期，验收规则，验收日期]
    *
    */

  def DistributionPlan(filterData: RDD[SchoolBean]): RDD[(String, List[String])] = {
    val proLedgerMaster = filterData.filter(x => x != null && x.table.equals("t_pro_ledger_master"))
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[LedgerMaster]), JSON.parseObject(x.old, classOf[LedgerMaster])))
      .filter(x => "1".equals(x._2.industry_type))

    val ledgerMasterData: RDD[(String, List[String])] = proLedgerMaster.map({
      case (k, v, z) =>
        val id = v.id
        val action_date = v.action_date //配送时间
      val replaceAll = action_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val ledger_type = v.ledger_type
        val receiver_id = v.receiver_id //学校ID
      val source_id = v.source_id //团餐公司ID
      val ware_batch_no = v.ware_batch_no //发货批次
      val haul_status = v.haul_status //配送状态 -2 信息不完整 -1 未指派 0 已指派（未配送） 1配送中 2 待验收（已配送）3已验收 4已取消,',
      val stat = v.stat
        val delivery_date = Rule.emptyToNull(v.delivery_date) //验收上报日期
      val purchase_date = Rule.emptyToNull(v.purchase_date) //进货日期
      val compliance = Rule.emptyToNull(v.compliance) //验收规则
      val delivery_record_date = Rule.emptyToNull(v.delivery_record_date) //验收日期

        val calendar = Calendar.getInstance()
        calendar.setTime(new Date())
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val time = calendar.getTime
        val now = format.format(time)

        var oldHaulStatus = "null"
        var oldStat = "null"
        try {
          oldHaulStatus = z.haul_status
          oldStat = z.stat
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $z", e)
          }
        }

        if (format.parse(now).getTime <= format.parse(date).getTime) {
          if (k.equals("insert") && stat.equals("1")) {
            (id, List(date, ledger_type, receiver_id, source_id, ware_batch_no, haul_status, "null", "insert", stat, "null", delivery_date, purchase_date, compliance, delivery_record_date))
          } else if (k.equals("update")) {
            (id, List(date, ledger_type, receiver_id, source_id, ware_batch_no, haul_status, oldHaulStatus, "update", stat, oldStat, delivery_date, purchase_date, compliance, delivery_record_date))
          } else {
            (id, List(date, ledger_type, receiver_id, source_id, ware_batch_no, haul_status, "null", "delete", stat, "null", delivery_date, purchase_date, compliance, delivery_record_date))
          }
        } else {
          ("null", List("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"))
        }


    }).filter(x => !"null".equals(x._1))
    ledgerMasterData
  }

  def DistributionIndex(DistriData: (RDD[SchoolBean], Broadcast[Map[String, String]])): RDD[(String, String, String, String, String, String, String, String)] = {
    val ledger: RDD[(String, String, String, String, String, String, String, String)] = DistributionPlan(DistriData._1).distinct().filter(x => !x._1.equals("null")).map({
      x =>
        val date = x._2(0)
        val receiver_id = x._2(2) //学校ID
      val haul_status = x._2(5) //配送状态 -2 信息不完整 -1 未指派 0 已指派（未配送） 1配送中 2 待验收（已配送）3已验收 4已取消,',
      val old_haul_status = x._2(6)
        val typ = x._2(7)
        val stat = x._2(8)
        val quhao = DistriData._2.value.getOrElse(receiver_id, "null")
        if ("insert".equals(typ) && stat.equals("1")) {
          (date, "shanghai", quhao, haul_status, old_haul_status, typ, stat, x._2(9))
        } else if ("update".equals(typ)) {
          (date, "shanghai", quhao, haul_status, old_haul_status, typ, stat, x._2(9))
        } else {
          (date, "shanghai", quhao, haul_status, old_haul_status, typ, stat, x._2(9))
        }
    })
    ledger
  }

  /**
    *
    * * 配送子表分析数据
    *
    * * @param RDD[SchoolBean]  mysql的业务binlgog日志
    *
    */

  def ProLedgerPlan(filterData: RDD[SchoolBean]): RDD[(String, String)] = {
    val proLedger = filterData.filter(x => x != null && x.table.equals("t_pro_ledger") && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data, classOf[Ledger]))
      .filter(x => !"0".equals(x.stat))
    val proLedgerData: RDD[(String, String)] = proLedger.map({
      x =>
        val master_id = x.master_id
        val delivery_attr = x.delivery_attr.toString
        var delivery = ""
        if (delivery_attr.equals("0")) {
          delivery = "统配"
        } else if (delivery_attr.equals("1")) {
          delivery = "直配"
        } else if (delivery_attr.equals("2")) {
          delivery = "统配"
        } else {
          delivery = "直配"
        }
        (master_id, delivery)
    })
    proLedgerData
  }




  /**
    *
    * * b2b配送t_delivery更新和删除分析数据
    *
    * * @param RDD[SchoolBean]  mysql的业务binlgog日志
    *
    * * * @param school2Area  schoolid对于的区号
    *
    */


  def B2bUpDeLedege(filterData: RDD[SchoolBean], school2Area: Broadcast[Map[String, String]]) = {
    filterData.filter(x => x != null && x.table.equals("t_delivery") )
      .map(x => (x.`type`, JSON.parseObject(x.data, classOf[B2bDelivery])))
      .map({
        x =>
          val b2bDeliveryBean = x._2
          val id = b2bDeliveryBean.id
          val delivery_code = b2bDeliveryBean.delivery_code
          val types = MysqlUtils.merchantToType(b2bDeliveryBean.buyer_merchant_id)
          val eduId = MysqlUtils.merchantToEduid(b2bDeliveryBean.buyer_merchant_id)
          val supplierId = b2bDeliveryBean.seller_merchant_id
          val business_type = b2bDeliveryBean.business_type
          //val delivery_date = Rule.emptyToNull(b2bDeliveryBean.delivery_date)  配送时间
          //期望收货时间，相当于排菜时间
          val expected_receive_date = Rule.emptyToNull(b2bDeliveryBean.expected_receive_date)
          var date="2020-01-01"
          try {
             date = format.format(format.parse(expected_receive_date))
          }catch {
            case e: Exception => {
              logger.error(s"parse json error: $x", e)
            }
          }

          val calendar = Calendar.getInstance()
          calendar.setTime(new Date())
          calendar.add(Calendar.DAY_OF_MONTH, -1)
          val time = calendar.getTime
          val now = format.format(time)
          val del = Rule.emptyToNull(b2bDeliveryBean.del)
          var businessType = "null"
          if ("1".equals(business_type)) {
            businessType = "统配"
          } else {
            businessType = "直配"
          }

          val area = school2Area.value.getOrElse(eduId, "null")

          if (format.parse(now).getTime <= format.parse(date).getTime) {
            (id, types, x._1, del, (delivery_code, eduId, supplierId, businessType, date, area))
          } else {
            ("null", 0 , "null", "null", ("null", "null", "null", "null", "null", "null"))
          }

      }).filter(x => !"null".equals(x._1)).filter(x => x._2 == 2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              if("insert".equals(x._3)){
                jedis.hset(x._5._5 + "_" + "B2b-DistributionDetail", x._1,"id" + "_" + x._1 + "_" + "type" + "_" + "1" + "_" + "schoolid" + "_" + x._5._2 + "_" + "area" + "_" + x._5._6 + "_" + "sourceid" + "_" + x._5._3 + "_" + "batchno" + "_" + x._5._1 + "_" + "delivery" + "_" + x._5._4)
              }else if ("update".equals(x._3) ) {
                if("1".equals(x._4)){
                  jedis.hdel(x._5._5 + "_" + "B2b-DistributionDetail", x._1)
                }else{
                  jedis.hset(x._5._5 + "_" + "B2b-DistributionDetail", x._1,"id" + "_" + x._1 + "_" + "type" + "_" + "1" + "_" + "schoolid" + "_" + x._5._2 + "_" + "area" + "_" + x._5._6 + "_" + "sourceid" + "_" + x._5._3 + "_" + "batchno" + "_" + x._5._1 + "_" + "delivery" + "_" + x._5._4)
                }
              }else{
                jedis.hdel(x._5._5 + "_" + "B2b-DistributionDetail", x._1)
              }
          })
      })
  }

  /**
    *
    * * b2b配送t_delivery_extra更新和删除分析数据
    *
    * * @param RDD[SchoolBean]  mysql的业务binlgog日志
    *
    */

  def B2bUpDeLedegeExtra(filterData: RDD[SchoolBean]) = {
    filterData.filter(x => x != null && x.table.equals("t_delivery_extra") )
      .map(x => (x.`type`,JSON.parseObject(x.data, classOf[B2bDelivery])))
      .map({
        x =>
          val b2bDeliveryExtraBean = x._2
          val delivery_id = b2bDeliveryExtraBean.delivery_id
          val types = MysqlUtils.merchantToType(b2bDeliveryExtraBean.merchant_id)
          val delivery_date = Rule.emptyToNull(b2bDeliveryExtraBean.delivery_date)
          //验收上报时间
          val delivery_record_date = Rule.emptyToNull(b2bDeliveryExtraBean.delivery_record_date) //验收时间
        val haul_status = b2bDeliveryExtraBean.haul_status
          val compliance = b2bDeliveryExtraBean.compliance
          val purchase_date = Rule.emptyToNull(b2bDeliveryExtraBean.purchase_date) //进货时间

          (delivery_id, types, (haul_status, compliance, delivery_date, delivery_record_date, purchase_date),x._1)
      }).filter(x => x._2 == 2)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              if("insert".equals(x._4)){
                jedis.hset("b2bDistribution", x._1, x._3._1 + "_deliveryDate" + "_" + x._3._3 + "_" + "disstatus" + "_" + x._3._2 + "_" + "purchaseDate" + "_" + x._3._5 + "_" + "deliveryReDate" + "_" + x._3._4)
              }else if("update".equals(x._4)){
                jedis.hset("b2bDistribution", x._1, x._3._1 + "_deliveryDate" + "_" + x._3._3 + "_" + "disstatus" + "_" + x._3._2 + "_" + "purchaseDate" + "_" + x._3._5 + "_" + "deliveryReDate" + "_" + x._3._4)
              }else{
                jedis.hdel("b2bDistribution", x._1)
              }

          })
      })
  }


}
