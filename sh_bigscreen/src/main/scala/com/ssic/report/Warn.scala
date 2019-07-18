package com.ssic.report

import java.util.Date

import com.ssic.beans.SchoolBean
import com.ssic.utils.JPools
import com.ssic.utils.SchoolRule.logger
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

/**
  * Created by 云 on 2018/8/22.
  * 预警计划功能指标
  * LicenceType0(0, "餐饮服务许可证"),
  * LicenceType1(1, "食品经营许可证"),
  * LicenceType2(2, "食品流通许可证"),
  * LicenceType3(3, "食品生产许可证"),
  * LicenceType4(4, "营业执照"),
  * LicenceType5(5, "组织机构代码"),
  * LicnceType6(6, "税务登记证"),
  * LicnceType7(7, "检验检疫合格证"),
  * LicnceType8(8, "ISO认证证书"),
  * certificateId(9, "身份证"),
  * LicnceType10(10, "港澳居民来往内地通行证"),
  * LicnceType11(11, "台湾居民往来内地通行证"),
  * LicenceType12(12, "其他资质证书"),
  * LicenceType13(13, "食品卫生许可证"),
  * LicenceType14(14, "运输许可证"),
  * LicenceType15(15, "其他证件类型A"),
  * LicenceType16(16, "其他证件类型B"),
  * officialCard(17, "军官证"),
  * LicenceType20(20, "健康证"),
  * passportId(21, "护照"),
  * LicenceType22(22, "A1"),
  * LicenceType23(23, "B"),
  * LicenceType24(24, "C"),
  * LicenceType25(25, "A2");
  */

object Warn {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def warnInsert(warnData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, Int]], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]])) = {
    val now = format.format(new Date())
    val warn = warnData._1.filter(x => x != null && x.table.equals("t_pro_warning_global_master") && x.`type`.equals("insert") && x.data.stat.equals("1") && x.data.warn_type.equals("1"))
    val license = warnData._1.filter(x => x != null && x.table.equals("t_pro_warning_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val warnDa = warn.distinct().map({
      x =>
        val id = x.data.id
        val supplier_id = x.data.supplier_id
        val warn_stat = x.data.warn_stat //预警状态 预警状态:1待处理,2处理中,3驳回,4已处理'
      val warn_type = x.data.warn_type //预警类型,1:证照预警,2:配送警示,3过保警示,4验收预警'
      val warn_type_child = x.data.warn_type_child //证件类别
      val create_time = x.data.create_time
        val replaceAll = create_time.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        if ("4".equals(warn_stat)) {
          (id, List(supplier_id, warn_type, warn_type_child, warn_stat, date, now))
        } else {
          (id, List(supplier_id, warn_type, warn_type_child, warn_stat, date, "null"))
        }

    }).filter(x => x._2(2).equals("0") || x._2(2).equals("1"))

    val licenseData = license.distinct().map({
      x =>
        val id = x.data.id
        val warn_master_id = x.data.warn_master_id
        val remain_time = x.data.remain_time //剩余多少天，0的时候是逾期
      val lose_time = x.data.lose_time //到期时间
      val lic_no = x.data.lic_no //证件号码
      val licen = x.data.license //证件名称
        (warn_master_id, List(id, remain_time, lose_time, lic_no, licen))
    })

    warnDa.leftOuterJoin(licenseData).map(x => (x._1, (x._2._1, x._2._2.getOrElse(List("null"))))).filter(x => !x._2._2(0).equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              //预警全部信息的总和
              logger.info("insert" + "_" + "warn" + x._1 + "_" + x._2._1(0) + "_" + x._2._1(2) + "_" + x._2._1(3) + "_" + x._2._1(4) + "_" + x._2._1(5) + "_" + x._2._2(0) + "_" + x._2._2(1) + "_" + x._2._2(2) + "_" + x._2._2(3) + "_" + x._2._2(4))
              jedis.hincrBy(x._2._1(4) + "_" + "warn-total", "area" + "_" + warnData._2.value.getOrElse(x._2._1(0), "null") + "_" + "status" + "_" + x._2._1(3), 1)

              if (warnData._3.value.getOrElse(x._2._1(0), -1) == 1) {
                //预警学校的信息总和
                jedis.hincrBy(x._2._1(4) + "_" + "warn-total", "school" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._2._1(0), "null") + "_" + "status" + "_" + x._2._1(3), 1)

                jedis.hset(x._2._1(4) + "_" + "warnDetail", "school" + "_" + x._1 + "_" + x._2._2(0), "area" + "_" + warnData._2.value.getOrElse(x._2._1(0), "null") + "_" + "supplierid" + "_" + x._2._1(0) + "_" + "warntypechild" + "_" + x._2._1(2) + "_" + "licno" + "_" + x._2._2(3) + "_" + "losetime" + "_" + x._2._2(2) + "_" + "remaintime" + "_" + x._2._1(1) + "_" + "status" + "_" + x._2._1(3) + "_" + "dealtime" + "_" + x._2._1(5))

                //教属的学校预警的信息情况
                val masterid = warnData._4.value.getOrElse(x._2._1(0), List("null"))
                if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                  if ("3".equals(masterid(1))) {
                    jedis.hincrBy(x._2._1(4) + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._2._1(3), 1)
                  } else {
                    jedis.hincrBy(x._2._1(4) + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._2._1(3), 1)
                  }
                }
              } else {
                //团餐公司总和
                jedis.hincrBy(x._2._1(4) + "_" + "warn-total", "supplier" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._2._1(0), "null") + "_" + "status" + "_" + x._2._1(3), 1)

                jedis.hset(x._2._1(4) + "_" + "warnDetail", "supplier" + "_" + x._1 + "_" + x._2._2(0), "area" + "_" + warnData._2.value.getOrElse(x._2._1(0), "null") + "_" + "supplierid" + "_" + x._2._1(0) + "_" + "warntypechild" + "_" + x._2._1(2) + "_" + "licno" + "_" + x._2._2(3) + "_" + "losetime" + "_" + x._2._2(2) + "_" + "remaintime" + "_" + x._2._1(1) + "_" + "status" + "_" + x._2._1(3) + "_" + "dealtime" + "_" + x._2._1(5))
              }


          })
      })
  }


  def warnInsertPeople(warnData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, Int]], Broadcast[Map[String, List[String]]],
    Broadcast[Map[String, String]]
    )) = {
    val now = format.format(new Date())
    val warn = warnData._1.filter(x => x != null && x.table.equals("t_pro_warning_global_master") && x.`type`.equals("insert") && x.data.stat.equals("1") && x.data.warn_type.equals("1"))
    val license = warnData._1.filter(x => x != null && x.table.equals("t_pro_warning_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val relation = warnData._1.filter(x => x != null && x.table.equals("t_pro_warning_view_relation") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val warnDa = warn.distinct().map({
      x =>
        val id = x.data.id
        val supplier_id = x.data.supplier_id
        val warn_stat = x.data.warn_stat //预警状态 预警状态:1待处理,2处理中,3驳回,4已处理'
      val warn_type = x.data.warn_type //预警类型,1:证照预警,2:配送警示,3过保警示,4验收预警'
      val warn_type_child = x.data.warn_type_child //证件类别
      val create_time = x.data.create_time
        val replaceAll = create_time.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        if ("4".equals(warn_stat)) {
          (id, List(supplier_id, warn_type, warn_type_child, warn_stat, date, now))
        } else {
          (id, List(supplier_id, warn_type, warn_type_child, warn_stat, date, "null"))
        }

    }).filter(x => x._2(2).equals("20") || x._2(2).equals("22") || x._2(2).equals("23") || x._2(2).equals("24") || x._2(2).equals("25"))

    val licenseData = license.distinct().map({
      x =>
        val id = x.data.id
        val warn_master_id = x.data.warn_master_id
        val remain_time = x.data.remain_time //剩余多少天，0的时候是逾期
      val lose_time = x.data.lose_time //到期时间
      val lic_no = x.data.lic_no //证件号码
      val licen = x.data.license //证件名称
      val written_name = x.data.written_name //证书上人的名字
        (warn_master_id, List(id, remain_time, lose_time, lic_no, licen, written_name))
    })

    val relationData = relation.distinct().map({
      x =>
        val id = x.data.id
        val warn_master_id = x.data.warn_master_id
        var school_id = "null"
        if (StringUtils.isNoneEmpty(x.data.schools_id) && !x.data.schools_id.equals("null")) {
          school_id = x.data.schools_id
        } else {
          school_id
        }

        var school_name = "null"
        if (StringUtils.isNoneEmpty(x.data.school_name) && !x.data.school_name.equals("null")) {
          school_name = x.data.school_name
        } else {
          school_name
        }

        var district_id = "null"
        if (StringUtils.isNoneEmpty(x.data.district_id) && !x.data.district_id.equals("null")) {
          district_id = x.data.district_id
        } else {
          district_id
        }

        (warn_master_id, List(school_id, school_name, district_id))
    }).distinct()


    warnDa.leftOuterJoin(licenseData).map(x => (x._1, (x._2._1, x._2._2.getOrElse(List("null"))))).filter(x => !x._2._2(0).equals("null")).leftOuterJoin(relationData).map(x => (x._1, x._2._1._1, x._2._1._2, x._2._2.getOrElse(List("null")))).filter(x => !x._4.equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              logger.info("insert" + "_" + "warn" + "_" + x._1 + "_" + x._2 + "_" + x._3 + "_" + x._4)
              //预警全部信息的总和
              jedis.hincrBy(x._2(4) + "_" + "warn-total", "area" + "_" + x._4(2) + "_" + "status" + "_" + x._2(3), 1)

              //人员信息预警总和
              jedis.hincrBy(x._2(4) + "_" + "warn-total", "people" + "_" + "area" + "_" + x._4(2) + "_" + "status" + "_" + x._2(3), 1)

              //人员预警信息详情
              jedis.hset(x._2(4) + "_" + "warnDetail", "people" + "_" + x._1 + "_" + x._3(0), "area" + "_" + x._4(2) + "_" + "supplierid" + "_" + x._2(0) + "_" + "warntypechild" + "_" + x._2(2) + "_" + "licno" + "_" + x._3(3) + "_" + "losetime" + "_" + x._3(2) + "_" + "remaintime" + "_" + x._2(1) + "_" + "status" + "_" + x._2(3) + "_" + "dealtime" + "_" + x._2(5) + "_" + "schoolid" + "_" + x._4(0) + "_" + "schoolname" + "_" + x._4(1) + "_" + "writtenname" + "_" + x._3(5))

              //人员预警的教属综合
              val masterid = warnData._4.value.getOrElse(x._4(0), List("null"))
              if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                if ("3".equals(masterid(1))) {
                  jedis.hincrBy(x._2(4) + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._2(3), 1)
                } else {
                  jedis.hincrBy(x._2(4) + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._2(3), 1)
                }
              }
          })

      })

  }

  def warnUpdateDelete(warnData: (RDD[SchoolBean], Broadcast[Map[String, String]], Broadcast[Map[String, Int]], Broadcast[Map[String, List[String]]], Broadcast[Map[String, String]], Broadcast[Map[String, List[String]]])) = {
    val now = format.format(new Date())
    val warn = warnData._1.filter(x => x != null && x.table.equals("t_pro_warning_global_master") && !x.`type`.equals("insert") && x.data.warn_type.equals("1"))
    val warnDa = warn.distinct().map({
      x =>
        val id = x.data.id
        val supplier_id = x.data.supplier_id
        val warn_stat = x.data.warn_stat //预警状态 预警状态:1待处理,2处理中,3驳回,4已处理'
      val warn_type = x.data.warn_type //预警类型,1:证照预警,2:配送警示,3过保警示,4验收预警'
      val warn_type_child = x.data.warn_type_child //证件类别
      val create_time = x.data.create_time
        val replaceAll = create_time.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        if ("4".equals(warn_stat)) {
          if ("update".equals(x.`type`)) {
            ("update", id, supplier_id, warn_type, warn_type_child, warn_stat, date, now, x.data.stat, x.old.stat, x.old.warn_stat)
          } else {
            ("delete", id, supplier_id, warn_type, warn_type_child, warn_stat, date, now, x.data.stat, "null", "null")
          }
        } else {
          if ("update".equals(x.`type`)) {
            ("update", id, supplier_id, warn_type, warn_type_child, warn_stat, date, "null", x.data.stat, x.old.stat, x.old.warn_stat)
          } else {
            ("delete", id, supplier_id, warn_type, warn_type_child, warn_stat, date, "null", x.data.stat, "null", "null")
          }
        }
    })

    warnDa.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            logger.info(x._1 + "_" + "warn" + "_" + x._2 + "_" + x._3 + "_" + x._4 + "_" + x._5 + "_" + x._6 + "_" + x._7 + "_" + x._8 + "_" + x._9 + "_" + x._10 + "_" + x._11)
            val strings = jedis.hkeys(x._7 + "_" + "warnDetail")
            for (i <- strings.asScala) {
              val id = i.split("_")(1)
              if (x._2.equals(id)) {
                if (x._5.equals("0") || x._5.equals("1")) {
                  if ("update".equals(x._1) && x._9.equals("0") && x._10.equals("1")) {
                    jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, -1)
                    if (warnData._3.value.getOrElse(x._3, -1) == 1) {
                      jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, -1)
                      jedis.hdel(x._7 + "_" + "warnDetail", i)
                      //教属的学校预警的信息情况
                      val masterid = warnData._4.value.getOrElse(x._3, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._6, -1)
                        } else {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._6, -1)
                        }
                      }
                    } else {
                      jedis.hincrBy(x._7 + "_" + "warn-total", "supplier" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, -1)
                      jedis.hdel(x._7 + "_" + "warnDetail", i)
                    }
                  } else if ("update".equals(x._1) && x._9.equals("1") && StringUtils.isNoneEmpty(x._11)) {
                    jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, 1)
                    jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._11, -1)
                    val v = jedis.hget(x._7 + "_" + "warnDetail", i)
                    if (warnData._3.value.getOrElse(x._3, -1) == 1) {

                      jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, 1)
                      jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._11, -1)
                      jedis.hset(x._7 + "_" + "warnDetail", i, v.split("status")(0) + "status" + "_" + x._6 + "_" + "dealtime" + "_" + x._8)

                      //教属的学校预警的信息情况
                      val masterid = warnData._4.value.getOrElse(x._3, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._6, 1)
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._11, -1)
                        } else {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._6, 1)
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._11, -1)
                        }
                      }
                    } else {
                      jedis.hincrBy(x._7 + "_" + "warn-total", "supplier" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, 1)
                      jedis.hincrBy(x._7 + "_" + "warn-total", "supplier" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._11, -1)
                      jedis.hset(x._7 + "_" + "warnDetail", i, v.split("status")(0) + "status" + "_" + x._6 + "_" + "dealtime" + "_" + x._8)
                    }
                  } else if ("delete".equals(x._1)) {
                    jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, -1)
                    if (warnData._3.value.getOrElse(x._3, -1) == 1) {
                      jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, -1)
                      jedis.hdel(x._7 + "_" + "warnDetail", i)
                      //教属的学校预警的信息情况
                      val masterid = warnData._4.value.getOrElse(x._3, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._6, -1)
                        } else {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "school" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._6, -1)
                        }
                      }
                    } else {
                      jedis.hincrBy(x._7 + "_" + "warn-total", "supplier" + "_" + "area" + "_" + warnData._2.value.getOrElse(x._3, "null") + "_" + "status" + "_" + x._6, -1)
                      jedis.hdel(x._7 + "_" + "warnDetail", i)
                    }
                  }

                } else if (x._5.equals("20") || x._5.equals("22") || x._5.equals("23") || x._5.equals("24") || x._5.equals("25")) {
                  val v = jedis.hget(x._7 + "_" + "warnDetail", i)
                  if (StringUtils.isNoneEmpty(v) && !v.equals("null")) {
                    val area = v.split("_")(1)
                    var schoolid = "null"

                    try {
                      schoolid = v.split("schoolid_")(1).split("_")(0)
                    } catch {
                      case e: Exception => {
                        logger.error(s"parse json error:", e)
                        schoolid
                      }
                    }

                    if ("update".equals(x._1) && x._9.equals("0") && x._10.equals("1")) {

                      jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + area + "_" + "status" + "_" + x._6, -1)

                      jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "area" + "_" + area + "_" + "status" + "_" + x._6, -1)

                      jedis.hdel(x._7 + "_" + "warnDetail", i)

                      //人员预警的教属综合
                      val masterid = warnData._6.value.getOrElse(schoolid, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._6, -1)
                        } else {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._6, -1)
                        }
                      }

                    } else if ("update".equals(x._1) && x._9.equals("1") && StringUtils.isNoneEmpty(x._11)) {

                      jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + area + "_" + "status" + "_" + x._6, 1)
                      jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + area + "_" + "status" + "_" + x._11, -1)

                      jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "area" + "_" + area + "_" + "status" + "_" + x._6, 1)
                      jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "area" + "_" + area + "_" + "status" + "_" + x._11, -1)

                      var status = "null"
                      try {
                        status = v.split("status")(0)
                      } catch {
                        case e: Exception => {
                          logger.error(s"parse json error:", e)
                          status
                        }
                      }

                      jedis.hset(x._7 + "_" + "warnDetail", i, status + "status" + "_" + x._6 + "_" + "dealtime" + "_" + x._8 + "_" + "schoolid" + schoolid)

                      //人员预警的教属综合
                      val masterid = warnData._6.value.getOrElse(schoolid, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._6, 1)
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._11, -1)
                        } else {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._6, 1)
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._11, -1)
                        }
                      }

                    } else if ("delete".equals(x._1)) {
                      jedis.hincrBy(x._7 + "_" + "warn-total", "area" + "_" + area + "_" + "status" + "_" + x._6, -1)

                      jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "area" + "_" + area + "_" + "status" + "_" + x._6, -1)

                      jedis.hdel(x._7 + "_" + "warnDetail", i)

                      //人员预警的教属综合
                      val masterid = warnData._6.value.getOrElse(schoolid, List("null"))
                      if (StringUtils.isNoneEmpty(masterid(0)) && !masterid(0).equals("null")) {
                        if ("3".equals(masterid(1))) {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + warnData._5.value.getOrElse(masterid(2), "null") + "_" + "status" + "_" + x._6, -1)
                        } else {
                          jedis.hincrBy(x._7 + "_" + "warn-total", "people" + "_" + "masterid" + "_" + masterid(1) + "_" + "slave" + "_" + masterid(2) + "_" + "status" + "_" + x._6, -1)
                        }
                      }
                    }

                  }
                }

              }
            }

        })
    })

  }


}
