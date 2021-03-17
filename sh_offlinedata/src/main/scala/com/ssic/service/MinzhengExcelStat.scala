package com.ssic.service

import java.sql.{Date, Timestamp}

import com.ssic.utils.{Rule, Tools}
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.sql.hive.HiveContext

class MinzhengExcelStat {

  /**
    * * 排菜统计数量
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/
  def platoonTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val platonnTotalData = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' ")
      .rdd.map({
      row =>
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val area = Rule.areaToName(row.getAs[String]("area"))
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val have_platoon = Rule.intToString(row.getAs[Integer]("have_platoon"))

        (department_name, area, have_class, have_platoon)
    })

    //食堂数量
    val shitangTotal = platonnTotalData.map(x => (x._1, 1)).reduceByKey(_ + _)

    //应排菜食堂
    val shouldPlatoonTotal = platonnTotalData.filter(x => "1".equals(x._3)).map(x => (x._1, 1)).reduceByKey(_ + _)

    //已排菜食堂
    val havePlatoonTotal = platonnTotalData.filter(x => "1".equals(x._4)).map(x => (x._1, 1)).reduceByKey(_ + _)

    //未排菜数量的统计数据
    val noPlatoonTotal = platonnTotalData.filter(x => "0".equals(x._4)).map(x => (x._1, 1)).reduceByKey(_ + _)

    val content: Array[(String, Array[String])] = shitangTotal.leftOuterJoin(shouldPlatoonTotal).leftOuterJoin(havePlatoonTotal).leftOuterJoin(noPlatoonTotal).map({
      x =>
        val department = x._1
        val sTotal = x._2._1._1._1
        val shTotal = x._2._1._1._2.getOrElse(0)
        val hTotal = x._2._1._2.getOrElse(0)
        val nTotal = x._2._2.getOrElse(0)

        var lv = "0" + "%"
        if (shTotal == 0) lv else lv = ((hTotal.toString.toDouble / shTotal.toString.toDouble) * 100).formatted("%.2f") + "%"


        (department, Array[String]("1", department, sTotal.toString, shTotal.toString, hTotal.toString, nTotal.toString, lv))
    }).collect()

    return content

  }

  /**
    * * 排菜详情
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def platoon(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val platoon: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val have_platoon = Rule.intToString(row.getAs[Integer]("have_platoon"))
        val platoon_create_time = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("platoon_create_time")), "yyyy-MM-dd HH:mm:ss")

        var haveClass = "否"
        var havePlatoon = "否"
        if ("1".equals(have_class)) haveClass = "是" else haveClass
        if ("1".equals(have_platoon)) havePlatoon = "是" else havePlatoon

        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, license, haveClass, havePlatoon, platoon_create_time))
    }).collect()

    return platoon
  }

  /**
    * * 菜品详情
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def dishmenu(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val dishmenu: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_dish_menu where year ='${year}' and month ='${month}' and supply_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val cater_type_name = Rule.nullToEmpty(row.getAs[String]("cater_type_name"))
        val menu_group_name = Rule.nullToEmpty(row.getAs[String]("menu_group_name"))
        val dishes_name = Rule.nullToEmpty(row.getAs[String]("dishes_name"))
        val dishes_number = Rule.toStringEmpty(row.getAs[Integer]("dishes_number"))
        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, license, supplier_name, cater_type_name, menu_group_name, dishes_name, dishes_number))
    }).collect()
    return dishmenu
  }

  /**
    * * 验收统计
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/
  def ledegeTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {

    val ledege = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'")
      .rdd.map({
      row =>
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val haul_status = Rule.intToString(row.getAs[Integer]("haul_status"))

        (department_name, have_class, haul_status)
    })

    val schoolTotal = ledege.map(x => (x._1, 1)).reduceByKey(_ + _)
    val shouldLedege = ledege.filter(x => "1".equals(x._2)).map(x => (x._1, 1)).reduceByKey(_ + _)
    val haveLedege = ledege.filter(x => "3".equals(x._3)).map(x => (x._1, 1)).reduceByKey(_ + _)

    val ledegeTotal = schoolTotal.leftOuterJoin(shouldLedege).leftOuterJoin(haveLedege).map({
      x =>
        val department = x._1
        val school = x._2._1._1.toString
        val shLedege = x._2._1._2.getOrElse(0).toString
        val haLedege = x._2._2.getOrElse(0).toString
        val noLedege = (shLedege.toInt - haLedege.toString.toInt).toString
        var lv = "0" + "%"
        if (!"0".equals(shLedege)) lv = ((haLedege.toDouble / shLedege.toDouble) * 100).formatted("%.2f") + "%" else lv

        (department, Array[String]("1", department, school, shLedege, haLedege, noLedege, lv))
    }).collect()

    return ledegeTotal
  }

  /**
    * * 验收明细
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def ledege(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val ledege: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val haul_status = Rule.intToString(row.getAs[Integer]("haul_status"))
        val delivery_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("delivery_date")), "yyyy-MM-dd HH:mm:ss")


        var haveClass = "否"
        var haulStatus = "否"
        if ("1".equals(have_class)) haveClass = "是" else haveClass
        if ("3".equals(haul_status)) haulStatus = "是" else haulStatus

        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, license, haveClass, haulStatus, delivery_date))
    }).collect()

    return ledege
  }

  /**
    * * 原料明细
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/
  def ledegeDetail(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val ledege: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_ledege_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val ware_batch_no = Rule.nullToEmpty(row.getAs[String]("ware_batch_no"))
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val supplier_material_name = Rule.nullToEmpty(row.getAs[String]("supplier_material_name"))
        val name = Rule.nullToEmpty(row.getAs[String]("name"))
        val wares_type_name = Rule.nullToEmpty(row.getAs[String]("wares_type_name"))
        val haul_status = Rule.intToString(row.getAs[Integer]("haul_status"))
        val ledger_type = row.getAs[Integer]("ledger_type")
        var ledgerType = ""
        var huansuanQuantity = ""
        var deliveryNumber = ""
        if (ledger_type == 1) {
          ledgerType = "原料"
          huansuanQuantity = Rule.toStringEmpty(row.getAs[BigDecimal]("other_quantity")) + Rule.nullToEmpty(row.getAs[String]("supplier_material_units"))
          if (row.getAs[BigDecimal]("delivery_number") != null) {
            deliveryNumber = Rule.toStringEmpty(row.getAs[BigDecimal]("delivery_number")) + Rule.nullToEmpty(row.getAs[String]("supplier_material_units"))
          } else {
            deliveryNumber
          }

        } else if (ledger_type == 2) {
          ledgerType = "成品菜"
          huansuanQuantity = Rule.toStringEmpty(row.getAs[BigDecimal]("actual_quantity")) + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
          if (row.getAs[BigDecimal]("actual_quantity") != null) {
            deliveryNumber = Rule.toStringEmpty(row.getAs[BigDecimal]("actual_quantity")) + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
          } else {
            deliveryNumber
          }

        } else {
          ledgerType
          huansuanQuantity
          deliveryNumber
        }
        val actual_quantity = Rule.toStringEmpty(row.getAs[BigDecimal]("actual_quantity")) + Rule.nullToEmpty(row.getAs[String]("amount_unit"))
        val huansuan = ""
        val supply_name = Rule.nullToEmpty(row.getAs[String]("supply_name"))

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
                stringBuffer.append(images(i).split(":")(1) + " ")
              } else {
                stringBuffer.append("")
              }
            }
          } else {
            stringBuffer.append("")
          }
        }

        var haulStatus = "否"
        if ("3".equals(haul_status)) haulStatus = "是" else haulStatus

        ("1", Array[String]("1", supplyDate, ware_batch_no, area, department_name, level_name, school_nature_name, gerocomium_name, license, supplier_name, ledgerType, supplier_material_name, name, wares_type_name, actual_quantity, huansuan, huansuanQuantity, supply_name, haulStatus, deliveryNumber, stringBuffer.toString, delivery_date))
    }).collect()

    return ledege
  }

  /**
    * * 留样统计
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def reserveTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val reserve = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'")
      .rdd.map({
      row =>
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val have_class = Rule.intToString(row.getAs[Integer]("have_class"))
        val have_reserve = Rule.intToString(row.getAs[Integer]("have_reserve"))
        (department_name, have_class, have_reserve)
    })
    val schoolTotal = reserve.map(x => (x._1, 1)).reduceByKey(_ + _)
    val shouldReserve = reserve.filter(x => "1".equals(x._2)).map(x => (x._1, 1)).reduceByKey(_ + _)
    val haveReserve = reserve.filter(x => "1".equals(x._3)).map(x => (x._1, 1)).reduceByKey(_ + _)

    val reserveTotal = schoolTotal.leftOuterJoin(shouldReserve).leftOuterJoin(haveReserve).map({
      x =>
        val department = x._1
        val school = x._2._1._1.toString
        val shReserve = x._2._1._2.getOrElse(0).toString
        val haReserve = x._2._2.getOrElse(0).toString
        val noReserve = (shReserve.toInt - haReserve.toString.toInt).toString
        var lv = "0" + "%"
        if (!"0".equals(shReserve)) lv = ((haReserve.toDouble / shReserve.toDouble) * 100).formatted("%.2f") + "%" else lv
        (department, Array[String]("1", department, school, shReserve, haReserve, noReserve, lv))
    }).collect()

    return reserveTotal
  }

  /**
    * * 留样详情
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def reserve(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val reserve: Array[(String, Array[String])] = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_dish_menu where year ='${year}' and month ='${month}' and supply_date ='${supplyDate}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val cater_type_name = Rule.nullToEmpty(row.getAs[String]("cater_type_name"))
        val menu_group_name = Rule.nullToEmpty(row.getAs[String]("menu_group_name"))
        val dishes_name = Rule.nullToEmpty(row.getAs[String]("dishes_name"))
        val dishes_number = Rule.toStringEmpty(row.getAs[Integer]("dishes_number"))
        val have_reserve = Rule.intToString(row.getAs[Integer]("have_reserve"))
        var haveReserve = ""
        var reserve_create_time = ""
        if ("1".equals(have_reserve)) {
          haveReserve = "是"
          reserve_create_time = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("reserve_create_time")), "yyyy-MM-dd HH:mm:ss")
        } else if ("0".equals(have_reserve)) {
          haveReserve = "否"
        } else {
          haveReserve
          reserve_create_time
        }
        var quantitys = ""
        val quantity = row.getAs[Integer]("quantity")
        if (quantity != null) {
          quantitys = quantity.toString + "g"
        } else {
          quantitys
        }
        var reserve_time = ""
        val reserve_hour = row.getAs[Integer]("reserve_hour")
        if (reserve_hour != null) {
          reserve_time = supplyDate + " " + Rule.stringToDate(Rule.toStringEmpty(row.getAs[Integer]("reserve_hour")), "HH") + ":" + Rule.stringToDate(Rule.toStringEmpty(row.getAs[Integer]("reserve_minute")), "mm") + ":" + "00"
        } else {
          reserve_time
        }

        val remark = Rule.nullToEmpty(row.getAs[String]("remark"))
        val reserve_creator = Rule.nullToEmpty(row.getAs[String]("reserve_creator"))


        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, license, cater_type_name, menu_group_name, dishes_name, dishes_number, haveReserve, quantitys, reserve_time, remark, reserve_creator, reserve_create_time))
    }).collect()

    return reserve
  }

  /**
    * * 餐厨垃圾回收
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    * @param types       餐厨垃圾或油脂类型
    *
    **/

  def recyclerData(hiveContext: HiveContext, year: String, month: String, supplyDate: String, types: Int): Array[(String, Array[String])] = {
    val recycler = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_recycler_waste where year ='${year}' and month ='${month}' and recycler_date ='${supplyDate}' and type = '${types}'  ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val recycler_name = Rule.nullToEmpty(row.getAs[String]("recycler_name"))
        val contact = Rule.nullToEmpty(row.getAs[String]("contact"))
        val recycler_number = Rule.nullToEmpty(row.getAs[String]("recycler_number"))
        val recycler_documents = Rule.toStringEmpty(row.getAs[Integer]("recycler_documents"))
        var data = Array[String]()
        if (types == 1) {
          data = Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, recycler_name, contact, recycler_number, recycler_documents)
        } else {
          val secont_type = row.getAs[Integer]("secont_type")
          var secontType = ""
          if (secont_type == 1) {
            secontType = "废油"
          } else if (secont_type == 2) {
            secontType = "含油废水"
          } else {
            secontType
          }
          data = Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, secontType, recycler_name, contact, recycler_number, recycler_documents)
        }
        ("1", data)
    }).collect()
    return recycler
  }

  /**
    * * 证件预警
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def peopleLicense(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val format = FastDateFormat.getInstance("yyyy-MM-dd")

    val license = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_warn_detail where year ='${year}' and month ='${month}' and warn_date ='${supplyDate}' and warn_type = 1 and warn_type_child = 20 ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val license_name = "健康证"
        val written_name = Rule.nullToEmpty((row.getAs[String]("written_name")))
        val lic_no = Rule.nullToEmpty(row.getAs[String]("lic_no"))
        val lose_time = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("lose_time")), "yyyy-MM-dd")
        var status = ""
        if (StringUtils.isEmpty(lose_time) && "null".equals(lose_time)) {
          status
        } else {
          if (((format.parse(lose_time).getTime - format.parse(supplyDate).getTime) / (1000 * 3600 * 24)).toInt < 0) {
            status = "逾期"
          } else {
            status = "剩余" + ((format.parse(lose_time).getTime - format.parse(supplyDate).getTime) / (1000 * 3600 * 24)).toInt + "天"
          }
        }

        var dealStat = ""
        var deal_date = ""
        if (row.getAs[Integer]("warn_stat") == 1) {
          dealStat = "待处理"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 2) {
          dealStat = "处理中"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 3) {
          dealStat = "驳回"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 4) {
          dealStat = "消除"
          deal_date = row.getAs[String]("deal_date")
        } else {
          dealStat
          deal_date
        }
        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, license_name, written_name, lic_no, lose_time, status, dealStat, deal_date))
    }).collect()
    return license
  }

  /**
    * * 物料预警
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def materialWarn(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val material = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_warn_detail where year ='${year}' and month ='${month}' and warn_date ='${supplyDate}' and warn_type = 3")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val batch_no = Rule.nullToEmpty(row.getAs[String]("batch_no"))
        val material_name = Rule.nullToEmpty(row.getAs[String]("material_name"))
        val production_date = Rule.nullToEmpty(row.getAs[String]("production_date"))
        val expiration_date = Rule.nullToEmpty(row.getAs[String]("expiration_date"))
        val car_code = Rule.nullToEmpty(row.getAs[String]("car_code"))
        val driver_name = Rule.nullToEmpty(row.getAs[String]("driver_name"))
        val batch_date = Rule.nullToEmpty(row.getAs[String]("batch_date"))
        var dealStat = ""
        var deal_date = ""
        if (row.getAs[Integer]("warn_stat") == 1) {
          dealStat = "待处理"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 2) {
          dealStat = "处理中"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 3) {
          dealStat = "驳回"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 4) {
          dealStat = "消除"
          deal_date = row.getAs[String]("deal_date")
        } else {
          dealStat
          deal_date
        }

        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, batch_no, material_name, production_date, expiration_date, car_code, driver_name, batch_date, dealStat, deal_date))
    }).collect()
    return material
  }

  /**
    * * 物流配送预警
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/
  def ledegerWarn(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Array[String])] = {
    val ledeger = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_warn_detail where year ='${year}' and month ='${month}' and warn_date ='${supplyDate}' and warn_type = 2 ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        var childWarn = ""
        if (row.getAs[Integer]("warn_type_child") == 1) {
          childWarn = "位置偏移警示"
        } else if (row.getAs[Integer]("warn_type_child") == 2) {
          childWarn = "APP未开启警示"
        } else {
          childWarn
        }
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val batch_no = Rule.nullToEmpty(row.getAs[String]("batch_no"))
        val car_code = Rule.nullToEmpty(row.getAs[String]("car_code"))
        val driver_name = Rule.nullToEmpty(row.getAs[String]("driver_name"))
        val batch_date = Rule.nullToEmpty(row.getAs[String]("batch_date"))
        var dealStat = ""
        var deal_date = ""
        if (row.getAs[Integer]("warn_stat") == 1) {
          dealStat = "待处理"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 2) {
          dealStat = "处理中"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 3) {
          dealStat = "驳回"
          deal_date
        } else if (row.getAs[Integer]("warn_stat") == 4) {
          dealStat = "消除"
          deal_date = row.getAs[String]("deal_date")
        } else {
          dealStat
          deal_date
        }
        ("1", Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, childWarn, supplier_name, batch_no, car_code, driver_name, batch_date, dealStat, deal_date))
    }).collect()

    return ledeger

  }

  /**
    * * 证照预警数据统计
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def warnTotal(hiveContext: HiveContext, year: String, month: String, supplyDate: String): (Array[(String, Array[String])], String, String, String, String, String) = {
    val warnData = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_warn_total_d where year = '${year}' and month = '${month}' and warn_date = '${supplyDate}' and  target = 3 and warn_type =1 and lic_type = 4 and area = 'NULL' and level_name ='NULL' and school_nature_name ='NULL' and department_master_id ='NULL' and department_slave_id_name ='NULL' ")
      .rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = Tools.mzDepartmentidToName(department_id)
        var untreatedSum = 0
        var rejectedSum = 0
        var reviewSum = 0
        var dealSum = 0
        var lv = "0" + "%"
        if (row.getAs[Integer]("untreated_sum") != null) untreatedSum = row.getAs[Integer]("untreated_sum") else untreatedSum
        if (row.getAs[Integer]("rejected_sum") != null) rejectedSum = row.getAs[Integer]("rejected_sum") else rejectedSum
        if (row.getAs[Integer]("review_sum") != null) reviewSum = row.getAs[Integer]("review_sum") else reviewSum
        if (row.getAs[Integer]("deal_sum") != null) dealSum = row.getAs[Integer]("deal_sum") else dealSum
        val total = untreatedSum + rejectedSum + reviewSum + dealSum
        if (total == 0) lv else lv = ((dealSum.toString.toDouble / total.toString.toDouble) * 100).formatted("%.2f") + "%"
        (Array[String]("1", supplyDate, department_name, total.toString, untreatedSum.toString, rejectedSum.toString, reviewSum.toString, dealSum.toString), total, untreatedSum, rejectedSum, reviewSum, dealSum)
    })
    val total = warnData.map(_._2).sum().toString
    val untreatedSum = warnData.map(_._3).sum().toString
    val rejectedSum = warnData.map(_._4).sum().toString
    val reviewSum = warnData.map(_._5).sum().toString
    val dealSum = warnData.map(_._6).sum().toString
    return (warnData.map(x => ("1", x._1)).collect(), total, untreatedSum, rejectedSum, reviewSum, dealSum)

  }

  /**
    * * 业务操作数据汇总
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/

  def allUse(hiveContext: HiveContext, year: String, month: String, supplyDate: String): (Array[(String, Array[String])], String, String, String, String, String, String, String) = {
    val allUse = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_platoon_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' ")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        var haveClass = "×"
        var have_class = 0
        if (row.getAs[Integer]("have_class") == 1) {
          haveClass = "√"
          have_class = 1
        } else {
          haveClass
          have_class
        }
        var havePlatoon = "×"
        var have_platoon = 0
        if (row.getAs[Integer]("have_platoon") == 1) {
          havePlatoon = "√"
          have_platoon = 1
        } else {
          havePlatoon
          have_platoon
        }
        var materialStatus = "×"
        var material_status = 0
        if ("2".equals(row.getAs[String]("material_status"))) {
          materialStatus = "√"
          material_status = 1
        } else {
          materialStatus
          material_status
        }
        var zhipai = "×"
        var peisong = "×"
        var yanshou = "×"
        var zhi_pai = 0
        var pei_song = 0
        var yan_shou = 0
        if (row.getAs[Integer]("haul_status") == 0) {
          zhipai = "√"
          peisong
          yanshou
          zhi_pai = 1
          pei_song
          yan_shou
        } else if (row.getAs[Integer]("haul_status") == 1) {
          zhipai = "√"
          peisong
          yanshou
          zhi_pai = 1
          pei_song
          yan_shou
        } else if (row.getAs[Integer]("haul_status") == 2) {
          zhipai = "√"
          peisong = "√"
          yanshou
          zhi_pai = 1
          pei_song = 1
          yan_shou
        } else if (row.getAs[Integer]("haul_status") == 3) {
          zhipai = "√"
          peisong = "√"
          yanshou = "√"
          zhi_pai = 1
          pei_song = 1
          yan_shou = 1
        } else {
          zhipai
          peisong
          yanshou
          zhi_pai
          pei_song
          yan_shou
        }
        var haveReserve = "×"
        var have_reserve = 0
        if (row.getAs[Integer]("have_reserve") == 1) {
          haveReserve = "√"
          have_reserve = 1
        } else {
          haveReserve
          have_reserve
        }
        (Array[String]("1", supplyDate, area, department_name, level_name, school_nature_name, gerocomium_name, supplier_name, haveClass, havePlatoon, materialStatus, zhipai, peisong, yanshou, haveReserve), have_class, have_platoon, material_status, zhi_pai, pei_song, yan_shou, have_reserve)
    })

    val haveClassTotal = allUse.map(_._2).sum().toInt.toString
    val havePlatoonTotal = allUse.map(_._3).sum().toInt.toString
    val materialStatusTotal = allUse.map(_._4).sum().toInt.toString
    val zhipaiTotal = allUse.map(_._5).sum().toInt.toString
    val peisongTotal = allUse.map(_._6).sum().toInt.toString
    val yanshouTotal = allUse.map(_._7).sum().toInt.toString
    val haveReserveTotal = allUse.map(_._8).sum().toInt.toString
    return (allUse.map(x => ("1", x._1)).collect(), haveClassTotal, havePlatoonTotal, materialStatusTotal, zhipaiTotal, peisongTotal, yanshouTotal, haveReserveTotal)

  }

  /**
    * *五级预警：排菜/验收信息未上报汇总
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    * @param tableName   表名
    *
    **/
  def noWarnCollect(hiveContext: HiveContext, year: String, month: String, supplyDate: String, tableName: String): Array[(String, Array[String])] = {
    val noPlatonWarnCollectData = hiveContext.sql(s"select * from app_saas_v1.'${tableName}' where year ='${year}' and month ='${month}' and warn_date ='${supplyDate}'").rdd.map({
      row =>
        val department_id = row.getAs[String]("department_id")
        val department_name = Tools.mzDepartmentidToName(department_id)
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        var warnPrompt = "/"
        if (row.getAs[Integer]("warn_prompt") == 1) warnPrompt = "√" else warnPrompt
        var warnRemind = "/"
        if (row.getAs[Integer]("warn_remind") == 1) warnRemind = "√" else warnRemind
        var warnEarly = "/"
        if (row.getAs[Integer]("warn_early") == 1) warnEarly = "√" else warnEarly
        var warnSupervise = "/"
        if (row.getAs[Integer]("warn_supervise") == 1) warnSupervise = "√" else warnSupervise
        var warnAccountability = "/"
        if (row.getAs[Integer]("warn_accountability") == 1) warnAccountability = "√" else warnAccountability
        ("1", Array[String]("1", supplyDate, department_name, gerocomium_name, warnPrompt, warnRemind, warnEarly, warnSupervise, warnAccountability))
    }).collect()

    return noPlatonWarnCollectData
  }


  /**
    * *民政每天基础信息
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/
  def gerocomiumDetailAll(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, String, String, String, String, String, String, String, String, String, String, String, String)] = {
    hiveContext.sql(s"select * from saas_v1_dw.dw_civil_gerocomium_d where year ='${year}' and month ='${month}' and use_date ='${supplyDate}'")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val address = Rule.nullToEmpty(row.getAs[String]("address"))
        val corporation = Rule.nullToEmpty(row.getAs[String]("corporation"))
        val food_safety_persion = Rule.nullToEmpty(row.getAs[String]("food_safety_persion"))
        val food_safety_mobilephone = Rule.nullToEmpty(row.getAs[String]("food_safety_mobilephone"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val social_credit_code = Rule.nullToEmpty(row.getAs[String]("social_credit_code"))
        val yg_supplier_id = Rule.nullToEmpty(row.getAs[String]("yg_supplier_id"))
        val gerocomium_id = Rule.nullToEmpty(row.getAs[String]("gerocomium_id"))
        (area, level_name, gerocomium_name, address, corporation, food_safety_persion, food_safety_mobilephone, school_nature_name, license, social_credit_code, department_name, yg_supplier_id, gerocomium_id)
    }).collect()
  }

  /**
    * *民政每天基础信息
    *
    * @param hiveContext  hive链接
    * @param gerocomiumId 民政id
    *
    **/
  def gerocomiumDetail(hiveContext: HiveContext, gerocomiumId: String): Array[(String, String, String, String, String, String, String, String, String, String, String, String)] = {
    hiveContext.sql(s"select * from app_saas_v1.t_civil_gerocomium where  id ='${gerocomiumId}'")
      .rdd.map({
      row =>
        val area = Rule.areaToName(row.getAs[String]("area"))
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val level_name = Rule.mZLevelToName(row.getAs[String]("level_name"))
        val school_nature_name = Rule.mZSchoolNatureToName(row.getAs[String]("school_nature_name"))
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val address = Rule.nullToEmpty(row.getAs[String]("address"))
        val corporation = Rule.nullToEmpty(row.getAs[String]("corporation"))
        val food_safety_persion = Rule.nullToEmpty(row.getAs[String]("food_safety_persion"))
        val food_safety_mobilephone = Rule.nullToEmpty(row.getAs[String]("food_safety_mobilephone"))
        val license_main_type = Rule.emptyToNull(row.getAs[String]("license_main_type"))
        val license_main_child = Rule.toStringEmpty(row.getAs[Integer]("license_main_child"))
        val license = Rule.licenseNew(license_main_type, license_main_child)
        val social_credit_code = Rule.nullToEmpty(row.getAs[String]("social_credit_code"))
        val yg_supplier_id = Rule.nullToEmpty(row.getAs[String]("yg_supplier_id"))
        (area, level_name, gerocomium_name, address, corporation, food_safety_persion, food_safety_mobilephone, school_nature_name, license, social_credit_code, department_name, yg_supplier_id)
    }).collect()
  }


  /**
    * 民政对应的菜品信息
    * @param hiveContext  hive链接
    * @param year         年
    * @param month        月
    * @param supplyDate   时间
    * @param gerocomiumId 民政
    **/
  def gerocomiumDishDetail(hiveContext: HiveContext, year: String, month: String, supplyDate: String, gerocomiumId: String): (Array[(String, Array[String])], Array[String]) = {
    val data = hiveContext
      .sql(s"select * from app_saas_v1.app_t_gerocomium_dish_menu where year ='${year}' and month ='${month}' and supply_date ='${supplyDate}' and gerocomium_id = '${gerocomiumId}'")
      .rdd.map({
      row =>
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        val supplier_id = Rule.nullToEmpty(row.getAs[String]("supplier_id"))
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val menu_group_name = Rule.nullToEmpty(row.getAs[String]("menu_group_name"))
        val cater_type_name = Rule.nullToEmpty(row.getAs[String]("cater_type_name"))
        val dishes_name = Rule.emptyToNull(row.getAs[String]("dishes_name"))
        ((gerocomium_name, supplier_name, menu_group_name, cater_type_name, supplier_id), dishes_name + ";")
    }).reduceByKey(_ + _)
    (data.map(x => ("1", Array[String]("1", x._1._1, x._1._2, x._1._3, x._1._4, x._2))).collect(), data.map(x => x._1._5).collect())

  }

  /**
    * 阳光午餐团餐公司对应的证件信息
    * @param hiveContext hive链接
    * @param ygSupplieId 团餐公司id
    **/

  def supplierLicense(hiveContext: HiveContext, ygSupplieId: String): (Array[(String, Array[String])], Int, Int, Int, Int, Int) = {
    val data = hiveContext.sql(
      s"""
         |select a.written_name,a.lic_type,a.lic_no,a.give_lic_date,a.lic_end_date,a.supplier_id relation_id,b.supplier_name
         |from
         |(select written_name,lic_type,lic_no,give_lic_date,lic_end_date,relation_id from saas_v1.t_pro_license where reviewed = 1
         |and stat =1
         |and (lic_type =20 or lic_type =22 or lic_type =23 or lic_type =24 or lic_type = 25)
         |and supplier_id ='${ygSupplieId}') as a
         |left outer join
         |(select uuid,supplier_name from saas_v1.erp_edu_group_catering_company ) as b
         |on a.supplier_id = b.uuid
       """.stripMargin).rdd.map({
      row =>
        val written_name = Rule.nullToEmpty(row.getAs[String]("written_name"))
        val lic_type = row.getAs[Integer]("lic_type")
        var lic_name = ""
        if (lic_type == 20) {
          lic_name = "健康证"
        } else if (lic_type == 22) {
          lic_name = "A1证"
        } else if (lic_type == 23) {
          lic_name = "B证"
        } else if (lic_type == 24) {
          lic_name = "C证"
        } else {
          lic_name = " A2证"
        }
        val lic_no = Rule.nullToEmpty(row.getAs[String]("lic_no"))
        val give_lic_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Date]("give_lic_date")), "yyyy-MM-dd")
        val lic_end_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Date]("lic_end_date")), "yyyy-MM-dd")
        val stat = "有效"
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        (lic_name, Array[String]("1", written_name, lic_name, lic_no, give_lic_date, lic_end_date, stat, supplier_name))
    })

    val total1 = data.filter(_._1.equals("健康证")).count().toInt
    val total2 = data.filter(_._1.equals("A1证")).count().toInt
    val total3 = data.filter(_._1.equals("B证")).count().toInt
    val total4 = data.filter(_._1.equals("C证")).count().toInt
    val total5 = data.filter(_._1.equals("A2证")).count().toInt

    return (data.map(x => ("1", x._2)).collect(), total1, total2, total3, total4, total5)

  }

  /**
    * b2b团餐公司对应的证件信息
    * @param hiveContext  hive链接
    * @param b2bSupplieId 团餐公司id
    **/

  def b2bSupplierLicense(hiveContext: HiveContext, b2bSupplieId: String): Array[(String, Array[String])] = {
    hiveContext.sql(
      s"""
         |select a.company_name,c.name,a.office_address,a.credit_code,a.legal_representative,a.contact_person,b.start_effective_date,b.end_effective_date,
         |b.qualification_code,a.contact_phone
         |from
         |(select company_name,office_region_id,office_address,credit_code,legal_representative,contact_person,id,contact_phone from ods_b2b_new.merchant_apply
         |where del = 0
         |and auth_status =2
         |and merchant_id ='${b2bSupplieId}') as a
         |left outer join
         |(select qualification_code,start_effective_date,end_effective_date,merchant_apply_id from ods_b2b_new.merchant_apply_qualification where del =0 and auth_status = 3 and qualification_name ='食品经营许可证') as b
         |on a.id = b.merchant_apply_id
         |left outer join
         |(select code ,name from ods_b2b_new.t_area where is_available =1 and IS_DELETED =0) as c
         |on a.office_region_id = c.code
       """.stripMargin).rdd.map({
      row =>
        val company_name = Rule.nullToEmpty(row.getAs[String]("company_name"))
        val credit_code = Rule.nullToEmpty(row.getAs[String]("credit_code"))
        val qualification_code = Rule.nullToEmpty(row.getAs[String]("qualification_code"))
        val end_effective_date = Rule.toStringEmpty(row.getAs[Date]("end_effective_date"))
        val name = Rule.nullToEmpty(row.getAs[String]("name"))
        val office_address = Rule.nullToEmpty(row.getAs[String]("office_address"))
        val legal_representative = Rule.nullToEmpty(row.getAs[String]("legal_representative"))
        val contact_person = Rule.nullToEmpty(row.getAs[String]("contact_person"))
        val contact_phone = Rule.nullToEmpty(row.getAs[String]("contact_phone"))
        ("1", Array[String]("1", company_name, "", credit_code, qualification_code, end_effective_date, name, office_address, legal_representative, contact_person, contact_phone))
    }).collect()

  }

  /**
    * 民政对应的原料信息
    * @param hiveContext  hive链接
    * @param year         年
    * @param month        月
    * @param supplyDate   时间
    * @param gerocomiumId 民政
    **/
  def gerocomiumLedegeDetail(hiveContext: HiveContext, year: String, month: String, supplyDate: String, gerocomiumId: String): Array[((String, String), Int, Array[String])] = {
    val data = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_ledege_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' and gerocomium_id = '${gerocomiumId}'")
      .rdd.map({
      row =>
        val use_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("use_date")), "yyyy-MM-dd")
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val delivery_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("delivery_date")), "yyyy-MM-dd")
        val ware_batch_no = Rule.nullToEmpty(row.getAs[String]("ware_batch_no"))
        val supplier_material_name = Rule.nullToEmpty(row.getAs[String]("supplier_material_name"))
        val name = Rule.emptyToNull(row.getAs[String]("name"))
        val spce = Rule.emptyToNull(row.getAs[String]("spce"))
        val wares_type_name = Rule.emptyToNull(row.getAs[String]("wares_type_name"))
        var huansuan = ""
        if (row.getAs[Float]("other_quantity") != null) huansuan = row.getAs[Float]("other_quantity") + row.getAs[String]("supplier_material_units") else huansuan
        val batch_no = Rule.emptyToNull(row.getAs[String]("batch_no"))
        val production_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Date]("production_date")), "yyyy-MM-dd")
        val shelf_life = Rule.toStringEmpty(row.getAs[Integer]("shelf_life"))
        var hual_status = "否"
        if (3 == row.getAs[Integer]("haul_status")) hual_status = "是" else hual_status
        var yanshou = ""
        if (row.getAs[Float]("delivery_number") != null) yanshou = row.getAs[Float]("delivery_number") + row.getAs[String]("supplier_material_units") else yanshou

        val supply_id = Rule.nullToEmpty(row.getAs[String]("supply_id"))
        val supply_name = Rule.nullToEmpty(row.getAs[String]("supply_name"))
        (supply_id, supply_name, Array[String]("1", use_date, supplier_name, "", delivery_date, ware_batch_no, supplier_material_name, name, spce, wares_type_name, "", huansuan, batch_no, production_date, shelf_life, hual_status, yanshou))
    }).groupBy(x => (x._1, x._2))
    val tuples: Array[((String, String), Int, Array[String])] = data.map(x => (x._1, x._2.size, x._2.head._3)).collect()
    return tuples

  }

  /**
    * *民政一键排查的原料对应信息
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    *
    **/
  def gerocomiumMaterialDetail(hiveContext: HiveContext, year: String, month: String, supplyDate: String): Array[(String, Iterable[(String, String, String, String, String, String, String, String, Array[String])])] = {
    val data = hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_ledege_detail where year ='${year}' and month ='${month}' and use_date ='${supplyDate}' ")
      .rdd.map({
      row =>
        val use_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("use_date")), "yyyy-MM-dd")
        val supplier_name = Rule.nullToEmpty(row.getAs[String]("supplier_name"))
        val delivery_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Timestamp]("delivery_date")), "yyyy-MM-dd")
        val ware_batch_no = Rule.nullToEmpty(row.getAs[String]("ware_batch_no"))
        val supplier_material_name = Rule.nullToEmpty(row.getAs[String]("supplier_material_name"))
        val name = Rule.emptyToNull(row.getAs[String]("name"))
        val spce = Rule.emptyToNull(row.getAs[String]("spce"))
        val sku_code = Rule.emptyToNull(row.getAs[String]("sku_code"))
        val wares_type_name = Rule.emptyToNull(row.getAs[String]("wares_type_name"))
        var huansuan = ""
        if (row.getAs[Float]("other_quantity") != null) huansuan = row.getAs[Float]("other_quantity") + row.getAs[String]("supplier_material_units") else huansuan
        val batch_no = Rule.emptyToNull(row.getAs[String]("batch_no"))
        val production_date = Rule.stringToDate(Rule.toStringEmpty(row.getAs[Date]("production_date")), "yyyy-MM-dd")
        val shelf_life = Rule.toStringEmpty(row.getAs[Integer]("shelf_life"))
        var hual_status = "否"
        if (3 == row.getAs[Integer]("haul_status")) hual_status = "是" else hual_status
        var yanshou = ""
        if (row.getAs[Float]("delivery_number") != null) yanshou = row.getAs[Float]("delivery_number") + row.getAs[String]("supplier_material_units") else yanshou

        val supply_id = Rule.nullToEmpty(row.getAs[String]("supply_id"))
        val supply_name = Rule.nullToEmpty(row.getAs[String]("supply_name"))
        val area = Rule.areaToName(row.getAs[String]("area"))
        val gerocomium_id = Rule.nullToEmpty(row.getAs[String]("gerocomium_id"))
        val dish_name = Rule.nullToEmpty(row.getAs[String]("dish_name"))
        val supplier_id = Rule.nullToEmpty(row.getAs[String]("supplier_id"))
        (name, sku_code, supply_id, supply_name, area, gerocomium_id, dish_name, supplier_id, Array[String]("1", use_date, supplier_name, "", delivery_date, ware_batch_no, supplier_material_name, name, spce, wares_type_name, "", huansuan, batch_no, production_date, shelf_life, hual_status, yanshou))
    }).groupBy(x => (x._1))

    val tuples: Array[(String, Iterable[(String, String, String, String, String, String, String, String, Array[String])])] = data.collect()
    return tuples

  }

  /**
    * * 五级预警排菜/验收预警报表
    *
    * @param hiveContext hive链接
    * @param year        年
    * @param month       月
    * @param supplyDate  时间
    * @param warnType  预警类型
    * @param warnTypeChild 预警子类型
    *
    **/

  def noWarnDetail(hiveContext: HiveContext, year: String, month: String, supplyDate: String, warnType: Int, warnTypeChild: Int):Array[(String, Array[String])] = {
    hiveContext.sql(s"select * from app_saas_v1.app_t_gerocomium_level_warn_detail where year ='${year}' and month ='${month}' and warn_date = '${supplyDate}' and warnType ='{$warnType}' and  warnTypeChild ='{$warnTypeChild}'").rdd.map({
      row =>
        val department_slave_id_name = row.getAs[String]("department_slave_id_name")
        val department_name = Tools.mzDepartmentidToName(department_slave_id_name)
        val gerocomium_name = Rule.nullToEmpty(row.getAs[String]("gerocomium_name"))
        ("1",Array[String]("1",department_name,gerocomium_name,supplyDate,""))

    }).collect()
  }

}
