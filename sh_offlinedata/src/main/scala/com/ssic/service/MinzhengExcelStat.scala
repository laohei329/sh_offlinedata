package com.ssic.service

import java.sql.Timestamp

import com.ssic.utils.Rule
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.hive.HiveContext

class MinzhengExcelStat {

  /**
    * * 排菜统计数量
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/
  def platoonTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val platonnTotalData = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_total_d where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' and department_id != '-1' ")
      .rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val area = Rule.emptyToNull(row.getAs[String]("area"))
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val have_platoon = Rule.intToString(row.getAs[Integer]("have_platoon"))
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val department_master_id = Rule.emptyToNull(row.getAs[String]("department_master_id"))
        val department_slave_id_name = Rule.emptyToNull(row.getAs[String]("department_slave_id_name"))
        val platoon_deal_status = Rule.emptyToNull(row.getAs[String]("platoon_deal_status"))
        val total = row.getAs[Integer]("total")


        (department_name, area, have_class, have_platoon, level_name, school_nature_name, department_master_id, department_slave_id_name, platoon_deal_status, total)
    })

    //食堂数量
    val shitangTotal = platonnTotalData.filter(x => "null".equals(x._2) && "null".equals(x._3) && "null".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8) && "null".equals(x._9)).map(x => (x._1, x._10))

    //应排菜食堂
    val shouldPlatoonTotal = platonnTotalData.filter(x => "null".equals(x._2) && "1".equals(x._3) && "null".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8) && "null".equals(x._9)).map(x => (x._1, x._10))

    //已排菜食堂
    val havePlatoonTotal = platonnTotalData.filter(x => "null".equals(x._2) && "1".equals(x._3) && "1".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8) && "null".equals(x._9)).map(x => (x._1, x._10))

    //未排菜数量的统计数据
    val noPlatoonTotal = platonnTotalData.filter(x => "null".equals(x._2) && "1".equals(x._3) && "0".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8) && "null".equals(x._9)).map(x => (x._1, x._10))

    val content: Array[(String, Array[String])] = shitangTotal.leftOuterJoin(shouldPlatoonTotal).leftOuterJoin(havePlatoonTotal).leftOuterJoin(noPlatoonTotal).map({
      x =>
        val department = x._1
        val sTotal = x._2._1._1._1
        val shTotal = x._2._1._1._2.getOrElse(0)
        val hTotal = x._2._1._2.getOrElse(0)
        val nTotal = x._2._2.getOrElse(0)

        var lv = "0" + "%"
        if (shTotal == 0) {
          lv
        } else {
          lv = ((hTotal.toString.toDouble / shTotal.toString.toDouble) * 100).formatted("%.2f") + "%"
        }

        (department, Array[String]("1", department, sTotal.toString, shTotal.toString, hTotal.toString, nTotal.toString, lv))
    }).collect()

    return content

  }

  /**
    * * 排菜详情
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/

  def platoon(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val platoon: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val school_name = Rule.nullToEmpty(row.getAs[String]("school_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.license(license_main_type + "_" + license_main_child)
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val have_platoon = Rule.intToString(row.getAs[Integer]("have_platoon"))
        val platoon_create_time = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("platoon_create_time")), "yyyy-MM-dd HH:mm:ss")


        var haveClass = "否"
        if ("1".equals(have_class)) {
          haveClass = "是"
        } else {
          haveClass
        }

        var havePlatoon = "否"
        if ("1".equals(have_platoon)) {
          havePlatoon = "是"
        } else {
          havePlatoon
        }
        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, school_name, supplier_name, license, haveClass, havePlatoon, platoon_create_time))
    }).collect()

    return platoon
  }

  /**
    * * 菜品详情
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/

  def dishmenu(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val dishmenu: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_dish_menu where year ='${year}' and month ='${month}' and supply_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val school_name = Rule.nullToEmpty(row.getAs[String]("school_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.license(license_main_type + "_" + license_main_child)
        val cater_type_name = Rule.nullToEmpty(row.getAs[String]("cater_type_name"))
        val dishes_name = Rule.nullToEmpty(row.getAs[String]("dishes_name"))
        val dishes_number = Rule.toStringEmpty(row.getAs[Integer]("dishes_number"))
        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, school_name, license, supplier_name, cater_type_name, dishes_name, dishes_number))
    }).collect()
    return dishmenu
  }

  /**
    * * 验收统计
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/
  def ledegeTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {

    val schoolTotal = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_total_d where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' and area is null and  have_class is null and  level_name is null and school_nature_name is null and department_master_id is null and department_slave_id_name is null and platoon_deal_status is null")
      .rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val total = row.getAs[Integer]("total")
        (department_name, total)
    })

    val ledege = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_ledger_master_total_d where year ='${year}' and month ='${month}' and action_date ='${supplyDate}' and department_id != '-1' ")
      .rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val area = Rule.emptyToNull(row.getAs[String]("area"))
        val haul_status = Rule.intToString(row.getAs[Integer]("haul_status"))
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val department_master_id = Rule.emptyToNull(row.getAs[String]("department_master_id"))
        val department_slave_id_name = Rule.emptyToNull(row.getAs[String]("department_slave_id_name"))
        val dis_deal_status = Rule.emptyToNull(row.getAs[String]("dis_deal_status"))
        val school_total = row.getAs[Integer]("school_total")


        (department_name, area, haul_status, level_name, school_nature_name, department_master_id, department_slave_id_name, dis_deal_status, school_total)
    })

    val shouldLedege = ledege.filter(x => "null".equals(x._2) && "null".equals(x._3) && "null".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8)).map(x => (x._1, x._9))
    val haveLedege = ledege.filter(x => "null".equals(x._2) && "3".equals(x._3) && "null".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8)).map(x => (x._1, x._9))

    val ledegeTotal = schoolTotal.leftOuterJoin(shouldLedege).leftOuterJoin(haveLedege).map({
      x =>
        val department = x._1
        val school = x._2._1._1.toString
        val shLedege = x._2._1._2.getOrElse(0).toString
        val haLedege = x._2._2.getOrElse(0).toString
        val noLedege = (shLedege.toInt - haLedege.toString.toInt).toString
        var lv = "0" + "%"
        if (!"0".equals(shLedege)) {
          lv = ((haLedege.toDouble / shLedege.toDouble) * 100).formatted("%.2f") + "%"
        } else {
          lv
        }
        (department, Array[String]("1", department, school, shLedege, haLedege, noLedege, lv))
    }).collect()

    return ledegeTotal
  }

  /**
    * * 验收明细
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/

  def ledege(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val ledege: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val school_name = Rule.nullToEmpty(row.getAs[String]("school_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.license(license_main_type + "_" + license_main_child)
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val haul_status = Rule.intToString(row.getAs[Integer]("haul_status"))
        val delivery_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("delivery_date")), "yyyy-MM-dd HH:mm:ss")


        var haveClass = "否"
        if ("1".equals(have_class)) {
          haveClass = "是"
        } else {
          haveClass
        }

        var haulStatus = "否"
        if ("3".equals(haul_status)) {
          haulStatus = "是"
        } else {
          haulStatus
        }
        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, school_name, supplier_name, license, haveClass, haulStatus, delivery_date))
    }).collect()

    return ledege
  }

  /**
    * * 原料明细
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/
  def ledegeDetail(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val ledege: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_ledege_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val ware_batch_no = Rule.nullToEmpty(row.getAs[String]("ware_batch_no"))
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val school_name = Rule.nullToEmpty(row.getAs[String]("school_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.license(license_main_type + "_" + license_main_child)
        val supplier_material_name = Rule.nullToEmpty(row.getAs[String]("supplier_material_name"))
        val name = Rule.nullToEmpty(row.getAs[String]("name"))
        val wares_type_name = Rule.nullToEmpty(row.getAs[String]("wares_type_name"))
        val ledger_type = row.getAs[Integer]("ledger_type")
        var ledgerType = ""
        var huansuanQuantity = ""
        var deliveryNumber = ""
        if (ledger_type == 1) {
          ledgerType = "原料"
          huansuanQuantity = Rule.toStringEmpty(row.getAs[BigDecimal]("other_quantity")) + Rule.nullToEmpty(row.getAs[String]("supplier_material_units"))
          deliveryNumber = Rule.toStringEmpty(row.getAs[BigDecimal]("delivery_number")) + Rule.nullToEmpty(row.getAs[String]("supplier_material_units"))
        } else if (ledger_type == 2) {
          ledgerType = "成品菜"
          huansuanQuantity = Rule.toStringEmpty(row.getAs[BigDecimal]("actual_quantity")) + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
          deliveryNumber = Rule.toStringEmpty(row.getAs[BigDecimal]("actual_quantity")) + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
        } else {
          ledgerType
          huansuanQuantity
          deliveryNumber
        }
        val actual_quantity = Rule.toStringEmpty(row.getAs[BigDecimal]("actual_quantity")) + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
        val huansuan = Rule.toStringEmpty(row.getAs[BigDecimal]("first_num")) + "+" + Rule.nullToEmpty(row.getAs[String]("supplier_material_units")) + "=" + Rule.toStringEmpty(row.getAs[BigDecimal]("second_num")) + "+" + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
        val supply_name = Rule.nullToEmpty(row.getAs[String]("supply_name"))
        val haul_status = Rule.intToString(row.getAs[Integer]("haul_status"))
        val delivery_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("delivery_date")), "yyyy-MM-dd HH:mm:ss")
        val images = row.getAs[Seq[String]]("images")
        val stringBuffer = new StringBuffer()
        if (images == null) {
          stringBuffer.append("")
        } else {
          if (images.size != 0) {
            for (i <- 0 until images.size) {
              val index = images(i).split(":")(0)
              if ("1".equals(index)) {
                stringBuffer.append("http://uploadpic-cdn.sunshinelunch.com/" + images(i).split(":")(1) + " ")
              } else {
                stringBuffer.append("")
              }
            }
          } else {
            stringBuffer.append("")
          }
        }

        var haulStatus = "否"
        if ("3".equals(haul_status)) {
          haulStatus = "是"
        } else {
          haulStatus
        }

        ("1", Array[String]("1", supplyDate, ware_batch_no, area, department_name, level_name, school_nature_name, school_name, license, supplier_name, ledgerType, supplier_material_name, name, wares_type_name, actual_quantity, huansuan, huansuanQuantity, supply_name, haulStatus, deliveryNumber, stringBuffer.toString, delivery_date))
    }).collect()

    return ledege
  }

  /**
    * * 留样统计
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/

  def reserveTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val schoolTotal = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_platoon_total_d where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' and area is null and  have_class is null and  level_name is null and school_nature_name is null and department_master_id is null and department_slave_id_name is null and platoon_deal_status is null")
      .rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val total = row.getAs[Integer]("total")
        (department_name, total)
    })

    val reserve = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_reserve_total_d where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  and department_id !='-1' ")
      .rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val area = Rule.emptyToNull(row.getAs[String]("area"))
        val have_reserve = Rule.intToString(row.getAs[Integer]("have_reserve"))
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val department_master_id = Rule.emptyToNull(row.getAs[String]("department_master_id"))
        val department_slave_id_name = Rule.emptyToNull(row.getAs[String]("department_slave_id_name"))
        val reserve_deal_status = Rule.emptyToNull(row.getAs[String]("reserve_deal_status"))
        val school_total = row.getAs[Integer]("school_total")


        (department_name, area, have_reserve, level_name, school_nature_name, department_master_id, department_slave_id_name, reserve_deal_status, school_total)
    })
    val shouldReserve = reserve.filter(x => "null".equals(x._2) && "null".equals(x._3) && "null".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8)).map(x => (x._1, x._9))
    val haveReserve = reserve.filter(x => "null".equals(x._2) && "1".equals(x._3) && "null".equals(x._4) && "null".equals(x._5) && "null".equals(x._6) && "null".equals(x._7) && "null".equals(x._8)).map(x => (x._1, x._9))

    val reserveTotal = schoolTotal.leftOuterJoin(shouldReserve).leftOuterJoin(haveReserve).map({
      x =>
        val department = x._1
        val school = x._2._1._1.toString
        val shReserve = x._2._1._2.getOrElse(0).toString
        val haReserve = x._2._2.getOrElse(0).toString
        val noReserve = (shReserve.toInt - haReserve.toString.toInt).toString
        var lv = "0" + "%"
        if (!"0".equals(shReserve)) {
          lv = ((haReserve.toDouble / shReserve.toDouble) * 100).formatted("%.2f") + "%"
        } else {
          lv
        }
        (department, Array[String]("1", department, school, shReserve, haReserve, noReserve, lv))
    }).collect()

    return reserveTotal
  }

  /**
    * * 留样详情
    *
    * @param hiveContext        hive链接
    * @param year               年
    * @param month              月
    * @param supplyDate         时间
    * @param departmentidToName 管理部门id对应的名字
    *
    **/

  def reserve(hiveContext: HiveContext, year: String, month: String, supplyDate: String, departmentidToName: Broadcast[Map[String, String]]): Array[(String, Array[String])] = {
    val reserve: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_edu_dish_menu where year ='${year}' and month ='${month}' and supply_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_id = row.getAs[String]("department_id")
        val department_name = departmentidToName.value.getOrElse(department_id, "null")
        val level_name = Rule.emptyToNull(row.getAs[String]("level_name"))
        val school_nature_name = Rule.emptyToNull(row.getAs[String]("school_nature_name"))
        val school_name = Rule.nullToEmpty(row.getAs[String]("school_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.license(license_main_type + "_" + license_main_child)
        val cater_type_name = Rule.nullToEmpty(row.getAs[String]("cater_type_name"))
        val menu_group_name = Rule.nullToEmpty(row.getAs[String]("menu_group_name"))
        val dishes_name = Rule.nullToEmpty(row.getAs[String]("dishes_name"))
        val dishes_number = Rule.toStringEmpty(row.getAs[Integer]("dishes_number"))
        val have_reserve = Rule.intToString(row.getAs[Integer]("have_reserve"))
        var haveReserve = ""
        if ("1".equals(have_reserve)) {
          haveReserve = "是"
        } else if ("0".equals(have_reserve)) {
          haveReserve = "否"
        } else {
          haveReserve
        }
        val quantity = Rule.toStringEmpty(row.getAs[Integer]("quantity")) + "g"
        val reserve_time = supplyDate + " " + Rule.stringToDate(Rule.toStringEmpty(row.getAs[Integer]("reserve_hour")), "HH") + ":" + Rule.stringToDate(Rule.toStringEmpty(row.getAs[Integer]("reserve_minute")), "mm") + ":" + "00"
        val remark = Rule.nullToEmpty(row.getAs[String]("remark"))
        val reserve_creator = Rule.nullToEmpty(row.getAs[String]("reserve_creator"))
        val reserve_create_time = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("reserve_create_time")), "yyyy-MM-dd HH:mm:ss")


        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, school_name, supplier_name, license, cater_type_name, menu_group_name, dishes_name, dishes_number, haveReserve, quantity,reserve_time,remark,reserve_creator, reserve_create_time))
    }).collect()

    return reserve
  }
}
