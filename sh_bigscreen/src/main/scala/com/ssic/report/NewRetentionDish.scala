package com.ssic.report

import java.util.{Calendar, Date}

import com.alibaba.fastjson.JSON
import com.ssic.beans.{ReserveSample, ReserveSampleDishes, SchoolBean}
import com.ssic.report.MaterialConfirm.format
import com.ssic.report.RetentionDish.format
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

object NewRetentionDish {

  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  /**

    * * 当天的留样主表数据存入到redis的临时表中

    * * @param RDD[SchoolBean] binlog日志数据

    */

  def retentionSampleDetail(filterData: RDD[SchoolBean]) = {
    val retentionDish = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[ReserveSample])))
    retentionDish.distinct().map({
      case (k,v) =>
        val supply_date = v.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val package_id = v.package_id
        var creator = "null"
        if (StringUtils.isNoneEmpty(v.creator) && !v.creator.equals("null")) {
          creator = v.creator
        } else {
          creator
        }
        val id = v.id
        val create_time = v.create_time
        var remark = "null"
        if (StringUtils.isNoneEmpty(v.remark) && !v.remark.equals("null")) {
          remark = v.remark
        } else {
          remark
        }

        val cater_type_name = v.cater_type_name
        val menu_group_name = v.menu_group_name

        val reserve_hour = Rule.int(v.reserve_hour.toString)
        val reserve_minute = Rule.int(v.reserve_minute.toString)
        val reservedata = supply_date + " " + reserve_hour + ":" + reserve_minute + ":" + "00"

        var compliance_status = "null"
        if (StringUtils.isNoneEmpty(v.compliance_status) && !v.compliance_status.equals("null")) {
          compliance_status = v.compliance_status
        } else {
          compliance_status
        }

        val calendar = Calendar.getInstance()
        calendar.setTime(new Date())
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val time = calendar.getTime
        val now = format.format(time)
        if (format.parse(now).getTime <= format.parse(date).getTime) {
          if ("insert".equals(k) && "1".equals(v.stat)) {
            (id, package_id, creator, date, create_time, remark, cater_type_name, menu_group_name, reservedata, "null", "insert",compliance_status)
          } else if ("update".equals(k)) {
            (id, package_id, creator, date, create_time, remark, cater_type_name, menu_group_name, reservedata, v.stat, "update",compliance_status)
          } else {
            (id, package_id, creator, date, create_time, remark, cater_type_name, menu_group_name, reservedata, "null", "delete",compliance_status)
          }
        }else{
          ("null", package_id, creator, date, create_time, remark, cater_type_name, menu_group_name, reservedata, "null", "delete",compliance_status)
        }
    }).filter(x => !"null".equals(x._1)).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            //id,data,package_id,create_time,creator,quantity,remark
            if ("insert".equals(x._11)) {
              jedis.hset(x._4 + "_" + "retentionsample", x._1, "packageid" + "_" + x._2 + "_" + "createtime" + "_" + x._5 + "_" + "creator" + "_" + x._3 + "_" + "remark" + "_" + x._6 + "_" + "groupname" + "_" + x._8 + "_" + "catertypename" + "_" + x._7 + "_" + "reservedata" + "_" + x._9+ "_" + "cstatus" + "_" + x._12 )
              jedis.expire(x._4 + "_" + "retentionsample", 259200)
            } else if ("update".equals(x._11)) {
              if ("0".equals(x._10)) {
                jedis.hdel(x._4 + "_" + "retentionsample", x._1)
              } else {
                jedis.hset(x._4 + "_" + "retentionsample", x._1, "packageid" + "_" + x._2 + "_" + "createtime" + "_" + x._5 + "_" + "creator" + "_" + x._3 + "_" + "remark" + "_" + x._6 + "_" + "groupname" + "_" + x._8 + "_" + "catertypename" + "_" + x._7 + "_" + "reservedata"+"_" +x._9+ "_" + "cstatus" + "_" + x._12)
                jedis.expire(x._4 + "_" + "retentionsample", 259200)
              }
            } else {
              jedis.hdel(x._4 + "_" + "retentionsample", x._1)
            }

        })
    })

  }

  /**

    * * 当天的留样子表数据存入到redis的临时表中

    * * @param RDD[SchoolBean] binlog日志数据

    */

  def retentionDishDetail(filterData: RDD[SchoolBean]) = {

    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample_dishes") )
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[ReserveSampleDishes])))
      .filter(x =>  "1".equals(x._2.stat))
    retention.distinct().map({
      case (k,v) =>
        val sample_id = v.sample_id
        val quantity = v.quantity
        val dishes = v.dishes
        val dishes_name = dishes.replaceAll("_","")
        val id = v.id
        val is_consistent = Rule.emptyToNull(v.is_consistent)
        val is_consistent_remark = Rule.emptyToNull(v.is_consistent_remark)

        val now = format.format(new Date())

        if ("insert".equals(k) && "1".equals(v.stat)) {
          (now, sample_id, quantity, dishes_name, id, "null", "insert",is_consistent,is_consistent_remark)
        } else if ("update".equals(k)) {
          (now, sample_id, quantity, dishes_name, id, v.stat, "update",is_consistent,is_consistent_remark)
        } else {
          (now, sample_id, quantity, dishes_name, id, "null", "delete",is_consistent,is_consistent_remark)
        }

    }).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            //id,data,package_id,create_time,creator,quantity,remark
            if ("insert".equals(x._7)) {
              jedis.hset(x._1 + "_" + "retentiondish", x._5, "sampleid" + "_" + x._2 + "_" + "quantity" + "_" + x._3 + "_" + "dishes" + "_" + x._4+"_"+"consistent"+"_"+x._8+"_"+"cremark"+"_"+x._9)
              jedis.expire(x._1 + "_" + "retentiondish", 259200)
            } else if ("update".equals(x._7)) {
              if ("0".equals(x._6)) {
                jedis.hdel(x._1 + "_" + "retentiondish", x._5)
              } else {
                jedis.hset(x._1 + "_" + "retentiondish", x._5, "sampleid" + "_" + x._2 + "_" + "quantity" + "_" + x._3 + "_" + "dishes" + "_" + x._4+"_"+"consistent"+"_"+x._8+"_"+"cremark"+"_"+x._9)
                jedis.expire(x._1 + "_" + "retentiondish", 259200)
              }
            } else {

              jedis.hdel(x._1 + "_" + "retentiondish", x._5)
            }
        })
    })
  }


}
