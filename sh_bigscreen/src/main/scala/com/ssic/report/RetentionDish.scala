package com.ssic.report

import java.util.{Calendar, Date}

import com.alibaba.fastjson.JSON
import com.ssic.beans._
import com.ssic.report.PlatoonPlan.format
import com.ssic.report.StopFood.logger
import com.ssic.utils.{JPools, Rule}
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/*
* 留样菜品的详细信息
* 未留样的菜品通过排菜反算得到
* */
object RetentionDish {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")


  /**
    *
    * * 当天的排菜的菜品数据存入到redis的临时表中
    *
    * * @param RDD[SchoolBean] binlog日志数据
    *
    */
  def dishDetail(data: (RDD[SchoolBean])) = {
    val packages = data.filter(x => x != null && x.table.equals("t_saas_package") && x.`type`.equals("insert") )
      .map(x => JSON.parseObject(x.data,classOf[SaasPackage]))
      .filter(x => "1".equals(x.stat) && "1".equals(x.industry_type))
    val dishData = data.filter(x => x != null && x.table.equals("t_saas_package_dishes") && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data,classOf[SaasPackageDishes]))
      .filter(x =>  "1".equals(x.stat))

    val packageData = packages.distinct().map(x => {
      val id = x.id
      val supply_date = x.supply_date
      val replaceAll = supply_date.replaceAll("\\D", "-")
      val date = format.format(format.parse(replaceAll))
      val supplier_id = x.supplier_id //供应商Id
      val school_id = x.school_id //项目点Id
      val menu_group_name = x.menu_group_name //菜单分组名  教工餐
      val ledger_type = x.ledger_type //配送类型: 1 原料 2 成品菜
      val stat = x.stat
      val calendar = Calendar.getInstance()
      calendar.setTime(new Date())
      calendar.add(Calendar.DAY_OF_MONTH, -1)
      val time = calendar.getTime
      val yesterday = format.format(time)
      val now = format.format(new Date())
      if (format.parse(yesterday).getTime == format.parse(date).getTime) {
        (id, List(date, supplier_id, school_id, stat, Rule.emptyToNull(menu_group_name), Rule.emptyToNull(ledger_type)))
      } else if(format.parse(now).getTime == format.parse(date).getTime){
        (id, List(date, supplier_id, school_id, stat, Rule.emptyToNull(menu_group_name), Rule.emptyToNull(ledger_type)))
      }
      else {
        ("null", List("null", "null", "null", "null", "null", "null"))
      }

    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0))).filter(x => !x._1.equals("null"))
    val dishFlitData = dishData.distinct().map({
      x =>
        val date = format.format(format.parse(x.create_time))
        val now = format.format(new Date())
        val dishes_name = x.dishes_name
        val dishes_names = dishes_name.replaceAll("_", "")
        if (format.parse(now).getTime == format.parse(date).getTime) {
          (x.package_id, List(dishes_names, Rule.emptyToNull(x.dishes_number), x.id, Rule.emptyToNull(x.cater_type_name), x.category))
        } else {
          ("null", List("null", "null", "null", "null", "null", "null"))
        }

    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0))).filter(x => !"null".equals(x._1))
    packageData.leftOuterJoin(dishFlitData).map(x => (x._2._1, x._2._2.getOrElse(List("null")), x._1)).filter(x => !x._2(0).equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>

              //dish(id),supplydate,packageid,supplierid,schoolid,groupname,dishesname,dishesnumber,catertypename(早餐),ledgertype(配送类型),category（分类）
              jedis.hset(x._1(0) + "_" + "dish-menu", x._3 + "_" + x._2(2), "supplierid" + "_" + x._1(1) + "_" + "schoolid" + "_" + x._1(2) + "_" + "groupname" + "_" + x._1(4) + "_" + "dishesname" + "_" + x._2(0) + "_" + "dishesnumber" + "_" + x._2(1) + "_" + "catertypename" + "_" + x._2(3) + "_" + "ledgertype" + "_" + x._1(5) + "_" + "category" + "_" + x._2(4))
              jedis.expire(x._1(0) + "_" + "dish-menu", 259200)

          })
      })

  }

  /**
    *
    * * 当天的排菜的菜品更新和删除数据存入到redis的临时表中
    *
    * * @param RDD[SchoolBean] binlog日志数据
    *
    */

  def dishUpdateDelete(data: (RDD[SchoolBean])) = {

    val dishData = data.filter(x => x != null && x.table.equals("t_saas_package_dishes") && !x.`type`.equals("insert"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[SaasPackageDishes])))

    val dishFlitData = dishData.distinct().map({
      case (k,v) =>
        val date = format.format(format.parse(v.create_time))
        val now = format.format(new Date())
        val dishes_name = v.dishes_name
        val dishes_names = dishes_name.replaceAll("_", "")
        if (format.parse(now).getTime == format.parse(date).getTime) {
          (v.package_id, List(dishes_names, v.dishes_number, v.id, v.cater_type_name, v.category), v.stat, now, k)
        } else {
          ("null", List("null", "null", "null", "null", "null", "null"), "null", "null", "null")
        }

    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0))).filter(x => !"null".equals(x._1))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              logger.info(x._1 + "_" + "_" + x._2 + "_" + x._3)
              //dish(id),supplydate,packageid,supplierid,schoolid,groupname,dishesname,dishesnumber,catertypename(早餐)
              if ("delete".equals(x._5)) {
                jedis.hdel(x._4 + "_" + "dish-menu", x._1 + "_" + x._2(2))
              } else if ("update".equals(x._5)) {
                val v = jedis.hget(x._4 + "_" + "dish-menu", x._1 + "_" + x._2(2))
                if (StringUtils.isNoneEmpty(v) && !"null".equals(v)) {
                  if ("0".equals(x._3)) {
                    jedis.hdel(x._4 + "_" + "dish-menu", x._1 + "_" + x._2(2))
                  } else {
                    jedis.hset(x._4 + "_" + "dish-menu", x._1 + "_" + x._2(2), v.split("dishesname")(0) + "dishesname" + "_" + x._2(0) + "_" + "dishesnumber" + "_" + x._2(1) + "_" + "catertypename" + "_" + x._2(3) + "_" + "ledgertype" + v.split("ledgertype")(1))
                    jedis.expire(x._4 + "_" + "dish-menu", 259200)
                  }

                }
              }


          })
      })
  }

  def packageUpdateDelete(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val packageData = data._1.filter(x => x != null && x.table.equals("t_saas_package") && !x.`type`.equals("insert"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[SaasPackage]),JSON.parseObject(x.old,classOf[SaasPackage])))
    packageData.map({
      case (k,v,z) =>
        val id = v.id
        val supplier_id = v.supplier_id
        val school_id = v.school_id
        val menu_group_name = v.menu_group_name
        val stat = v.stat
        val supply_date = v.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val ledger_type = v.ledger_type //配送类型: 1 原料 2 成品菜
        var oldStat="null"
        try {
          oldStat = z.stat
        } catch {
          case e: Exception => {
            logger.error(s"parse json error: $z", e)
          }
        }
        if ("delete".equals(k)) {
          (id, "delete", supplier_id, school_id, menu_group_name, stat, date, ledger_type, "null")
        } else {
          (id, "update", supplier_id, school_id, menu_group_name, stat, date, ledger_type, oldStat)
        }

    }).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val strings = jedis.hkeys(x._7 + "_" + "dish-menu")
            for (i <- strings.asScala) {
              val packageid = i.split("_")(0)
              val v = jedis.hget(x._7 + "_" + "dish-menu", i)
              if (packageid.equals(x._1)) {
                if ("delete".equals(x._2)) {
                  jedis.hdel(x._7 + "_" + "dish-menu", i)

                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null"), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null") + "_" + "schoolid" + "_" + x._4, -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "catertypename" + "_" + v.split("_catertypename_")(1).split("_ledgertype")(0), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "catertypename" + "_" + v.split("_catertypename_")(1).split("_ledgertype")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null"), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "catertypename" + "_" + v.split("_catertypename_")(1).split("_ledgertype")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null") + "_" + "schoolid" + "_" + x._4, -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                } else if ("update".equals(x._2)) {
                  if (StringUtils.isNoneEmpty(x._9) && !x._9.equals("null")) {
                    if ("1".equals(x._9) && "0".equals(x._6)) {
                      jedis.hdel(x._7 + "_" + "dish-menu", i)

                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null"), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null") + "_" + "schoolid" + "_" + x._4, -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "catertypename" + "_" + v.split("_catertypename_")(1).split("_ledgertype")(0), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "catertypename" + "_" + v.split("_catertypename_")(1).split("_ledgertype")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null"), -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total", "dishesname" + "_" + v.split("_dishesname_")(1).split("_dishesnumber")(0) + "_" + "category" + "_" + v.split("_category_")(1).split("_area")(0) + "_" + "catertypename" + "_" + v.split("_catertypename_")(1).split("_ledgertype")(0) + "_" + "area" + "_" + data._2.value.getOrElse(x._4, "null") + "_" + "schoolid" + "_" + x._4, -(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                    } else if ("0".equals(x._9) && "1".equals(x._6)) {
                      jedis.hset(x._7 + "_" + "dish-menu", i, "supplierid" + "_" + x._3 + "_" + "schoolid" + "_" + x._4 + "_" + "groupname" + "_" + x._5 + "_dishesname" + v.split("_dishesname")(1).split("ledgertype")(0) + "ledgertype" + "_" + x._8 + "_" + "category" + v.split("category")(1).split("area")(0) + "area" + "_" + data._2.value.getOrElse(x._4, "null"))
                    } else {
                      logger.info("排菜信息中存在这个id但是不需要计算的1111111111--------------------")
                    }
                  } else {
                    logger.info("排菜信息中存在这个id但是不需要计算的444444444444444444--------------------")
                  }
                } else {
                  logger.info("排菜信息中存在这个id但是不需要计算的22222222222--------------------")
                }
              } else {
                logger.info("排菜信息中存在这个id但是不需要计算的333333333--------------------")
              }
            }
        })
    })
  }

  def retentionDishDetail(filterData: RDD[SchoolBean]) = {
    val retentionDish = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample") && x.`type`.equals("insert") )
      .map(x => JSON.parseObject(x.data,classOf[ReserveSample]))
      .filter(x =>  "1".equals(x.stat))
    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample_dishes") && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data,classOf[ReserveSampleDishes]))
      .filter(x =>  "1".equals(x.stat))
    val retentionDishData = retentionDish.distinct().map({
      x =>
        val supply_date = x.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val package_id = x.package_id
        var creator = "null"
        if (StringUtils.isNoneEmpty(x.creator) && !x.creator.equals("null")) {
          creator = x.creator
        } else {
          creator
        }
        val id = x.id
        val create_time = x.create_time
        var remark = "null"
        if (StringUtils.isNoneEmpty(x.remark) && !x.remark.equals("null")) {
          remark = x.remark
        } else {
          remark
        }

        val cater_type_name = x.cater_type_name
        val menu_group_name = x.menu_group_name

        val reserve_hour = Rule.int(x.reserve_hour.toString)
        val reserve_minute = Rule.int(x.reserve_minute.toString)
        val reservedata = supply_date + " " + reserve_hour + ":" + reserve_minute + ":" + "00"

        val now = format.format(new Date())
        //if (format.parse(now).getTime <= format.parse(date).getTime) {
        (id, List(package_id, creator, date, create_time, remark, cater_type_name, menu_group_name, reservedata))
      //        }else{
      //          ("null",List("null","null","null","null","null","null","null","null"))
      //        }
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => !x._1.equals("null"))

    val retentionData = retention.distinct().map({
      x =>
        val sample_id = x.sample_id
        val quantity = x.quantity
        val dishes = x.dishes
        val id = x.id
        (sample_id, List(quantity, dishes, id))
    })

    retentionDishData.leftOuterJoin(retentionData).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).filter(x => !("null").equals(x._3(0)))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              //id,data,package_id,create_time,creator,quantity,remark
              jedis.hset(x._2(2) + "_" + "retention-dish", x._1 + "_" + x._3(2), "packageid" + "_" + x._2(0) + "_" + "createtime" + "_" + x._2(3) + "_" + "creator" + "_" + x._2(1) + "_" + "quantity" + "_" + x._3(0) + "_" + "remark" + "_" + x._2(4) + "_" + "groupname" + "_" + x._2(6) + "_" + "catertypename" + "_" + x._2(5) + "_" + "dishesname" + "_" + x._3(1) + "_" + "reservedata" + "_" + x._2(7))
          })
      })

  }

  def retentionDishUpdateDelte(filterData: RDD[SchoolBean]) = {
    val retentionDish = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample") && x.`type`.equals("update"))
      .map(x => JSON.parseObject(x.data,classOf[ReserveSample]))
      .filter(x => "1".equals(x.stat))
    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample_dishes") && x.`type`.equals("update"))
      .map(x => JSON.parseObject(x.data,classOf[ReserveSampleDishes]))
      .filter(x =>  "1".equals(x.stat))

    val retentionDishData = retentionDish.distinct().map({
      x =>
        val supply_date = x.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val package_id = x.package_id
        var creator = "null"
        if (StringUtils.isNoneEmpty(x.creator) && !x.creator.equals("null")) {
          creator = x.creator
        } else {
          creator
        }
        val id = x.id
        val create_time = x.create_time
        var remark = "null"
        if (StringUtils.isNoneEmpty(x.remark) && !x.remark.equals("null")) {
          remark = x.remark
        } else {
          remark
        }

        val cater_type_name = x.cater_type_name
        val menu_group_name = x.menu_group_name

        val reserve_hour = Rule.int(x.reserve_hour.toString)
        val reserve_minute = Rule.int(x.reserve_minute.toString)
        val reservedata = supply_date + " " + reserve_hour + ":" + reserve_minute + ":" + "00"

        val now = format.format(new Date())
        //  if (format.parse(now).getTime <= format.parse(date).getTime) {
        (id, List(package_id, creator, date, create_time, remark, cater_type_name, menu_group_name, reservedata))
      //        }else{
      //          ("null",List("null","null","null","null","null","null","null","null"))
      //        }
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => !x._1.equals("null"))

    val retentionData = retention.distinct().map({
      x =>
        val sample_id = x.sample_id
        val quantity = x.quantity
        val dishes = x.dishes
        val id = x.id
        (sample_id, List(quantity, dishes, id))
    })

    retentionDishData.leftOuterJoin(retentionData).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).filter(x => !x._3(0).equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              //id,data,package_id,create_time,creator,quantity,remark
              jedis.hset(x._2(2) + "_" + "retention-dish", x._1 + "_" + x._3(2), "packageid" + "_" + x._2(0) + "_" + "createtime" + "_" + x._2(3) + "_" + "creator" + "_" + x._2(1) + "_" + "quantity" + "_" + x._3(0) + "_" + "remark" + "_" + x._2(4) + "_" + "groupname" + "_" + x._2(6) + "_" + "catertypename" + "_" + x._2(5) + "_" + "dishesname" + "_" + x._3(1) + "_" + "reservedata" + "_" + x._2(7))
          })
      })

  }

  def retentionUpdateDelte(filterData: RDD[SchoolBean]) = {
    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample") && !x.`type`.equals("insert"))
      .map(x => (x.`type`,JSON.parseObject(x.data,classOf[ReserveSample])))

    val retentionData = retention.distinct().map({
      case (k,v) =>
        val supply_date = v.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val id = v.id
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

        val now = format.format(new Date())
        // if (format.parse(now).getTime <= format.parse(date).getTime){
        (id, remark, k, date, v.stat, cater_type_name, menu_group_name, reservedata)
      //        }else{
      //          ("null", "null", "null","null","null","null","null","null")
      //        }


    }).filter(x => !x._1.equals("null"))

    retentionData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val strings = jedis.hkeys(x._4 + "_" + "retention-dish")
            for (i <- strings.asScala) {
              val id = i.split("_")(0)
              if (id.equals(x._1)) {
                if ("delete".equals(x._3)) {
                  jedis.hdel(x._4 + "_" + "retention-dish", i)
                } else if ("update".equals(x._3) && "1".equals(x._5)) {
                  val v = jedis.hget(x._4 + "_" + "retention-dish", i)
                  if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
                    logger.info(x._4 + ";" + i + ";_" + v + ";_" + x._2 + ";_" + x._7 + ";样子信息中存在这个id，并且是更新操作")
                    jedis.hset(x._4 + "_" + "retention-dish", i, v.split("remark")(0) + "remark_" + x._2 + "_" + "groupname" + "_" + x._7 + "_" + "catertypename" + "_" + x._6 ++ "_" + "dishesname" + v.split("dishesname")(1))
                  } else {
                    logger.info("留样子信息中存在这个id但是不需要计算的--------------------")
                  }
                } else if ("update".equals(x._2) && "0".equals(x._5)) {
                  jedis.hdel(x._4 + "_" + "retention-dish", i)
                }
              }

            }


        })
    })

  }

}
