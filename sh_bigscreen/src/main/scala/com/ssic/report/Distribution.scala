package com.ssic.report

import java.util.Date

import com.ssic.beans.SchoolBean
import com.ssic.report.MaterialConfirm.format
import com.ssic.utils.Rule
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

/**
  * Created by 云 on 2018/8/20.
  * 配送计划功能指标
  */
object Distribution {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")


  /**

    * * 配送主表分析数据

    * * @param RDD[SchoolBean]  mysql的业务binlgog日志

    */

  def DistributionPlan(filterData: RDD[SchoolBean]): RDD[(String, List[String])] = {
    val proLedgerMaster = filterData.filter(x => x != null && x.table.equals("t_pro_ledger_master") && "1".equals(x.data.industry_type))
    val ledgerMasterData: RDD[(String, List[String])] = proLedgerMaster.map({
      x =>
        val id = x.data.id
        val action_date = x.data.action_date //配送时间
      val replaceAll = action_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val ledger_type = x.data.ledger_type
        val receiver_id = x.data.receiver_id //学校ID
      val source_id = x.data.source_id //团餐公司ID
      val ware_batch_no = x.data.ware_batch_no //发货批次
      val haul_status = x.data.haul_status //配送状态 -2 信息不完整 -1 未指派 0 已指派（未配送） 1配送中 2 待验收（已配送）3已验收 4已取消,',
      val stat = x.data.stat
        val delivery_date = Rule.emptyToNull(x.data.delivery_date) //验收上报日期
      val purchase_date = Rule.emptyToNull(x.data.purchase_date) //进货日期
      val compliance = Rule.emptyToNull(x.data.compliance) //验收规则
      val delivery_record_date = Rule.emptyToNull(x.data.delivery_record_date) //验收日期

        val now = format.format(new Date())

        if (format.parse(now).getTime <= format.parse(date).getTime) {
          if (x.`type`.equals("insert") && stat.equals("1")) {
            (id, List(date, ledger_type, receiver_id, source_id, ware_batch_no, haul_status, "null", "insert", stat, "null", delivery_date,purchase_date,compliance,delivery_record_date))
          } else if (x.`type`.equals("update")) {
            (id, List(date, ledger_type, receiver_id, source_id, ware_batch_no, haul_status, x.old.haul_status, "update", stat, x.old.stat, delivery_date,purchase_date,compliance,delivery_record_date))
          } else {
            (id, List(date, ledger_type, receiver_id, source_id, ware_batch_no, haul_status, "null", "delete", stat, "null", delivery_date,purchase_date,compliance,delivery_record_date))
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

    * * 配送子表分析数据

    * * @param RDD[SchoolBean]  mysql的业务binlgog日志

    */

  def ProLedgerPlan(filterData: RDD[SchoolBean]): RDD[(String, String)] = {
    val proLedger = filterData.filter(x => x != null && x.table.equals("t_pro_ledger") && x.`type`.equals("insert") && !x.data.stat.equals("0"))
    val proLedgerData: RDD[(String, String)] = proLedger.map({
      x =>
        val master_id = x.data.master_id
        val delivery_attr = x.data.delivery_attr.toString
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


}
