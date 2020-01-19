package com.ssic.service

import com.ssic.impl.DealDataFunc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DealDataStat extends DealDataFunc {

  override def platoontotal(data: RDD[(String, String)]): RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))] = {
    val platoontotaldata: RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))] = data.map({
      x =>
        val v = x._1.split("_")
        if (v.size == 3) {
          ((x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("level".equals(v(0)) && !"plastatus".equals(v(2))) {
          (("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nature".equals((v(0))) && !"plastatus".equals(v(4))) {
          (("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("canteenmode".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("level".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nature".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("canteenmode".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("masterid".equals(v(0)) && !"plastatus".equals(v(4))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("department".equals(v(0)) && !"plastatus".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("area".equals(v(0)) && "plastatus".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("masterid".equals(v(0)) && "plastatus".equals(v(4))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("department".equals(v(0)) && "plastatus".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"))
        } else if ("level".equals(v(0)) && "plastatus".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"))
        } else if ("nature".equals(v(0)) && "plastatus".equals(v(4))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2))
        } else {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        }
    })
    platoontotaldata

  }

  override def usematerialdealdata(data: (RDD[(String, String)], Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, String]], Broadcast[Map[String, String]])): RDD[(String, String, String, String, String, String)] = {

    val useMaterialData = data._1.map({
      x =>

        val projid = x._1.split("_projid_")(1).split("_")(0)
        val area = data._5.value.getOrElse(projid, "null")
        val schoolid = data._2.value.getOrElse(projid, "null")
        val schoolname = data._3.value.getOrElse(projid, "null")
        val status = x._2
        val key = "area" + "_" + area + "_" + "type" + x._1.split("name")(0).split("type")(1) + "name" + "_" + schoolname + "_" + "projid" + x._1.split("projid")(1)
        val gongcan = data._4.value.getOrElse(area + "_" + schoolid, "null")

        (area, gongcan, status, key, schoolname, schoolid)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐")).filter(x => !x._5.equals("null")).filter(x => !x._6.equals("null"))

    useMaterialData
  }

  override def usematerialdealtotaldata(data: RDD[(String, String)]) = {
    val useNaterialDa = data.filter(x => x._1.size > 7).map({
      x =>
        val v = x._1.split("_")(0)
        if ("area".equals(v)) {
          ((x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("masterid".equals(v)) {
          (("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-area".equals(v)) {
          (("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-masterid".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nature".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-nature".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("level".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-level".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nat-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-nat-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("lev-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-lev-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"))
        } else if ("department".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"))
        } else if ("school-department".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2))
        }
        else {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        }
    })
    useNaterialDa
  }

  override def distributiondealdata(data: (RDD[(String, String)], Broadcast[Map[String, String]], Broadcast[Map[String, String]], String)): RDD[(String, String, String, String, String, String, String)] = {
    val distribution = data._1.map({
      x =>

        val schoolid = x._1.split("_schoolid_")(1).split("_")(0)
        val area = data._3.value.getOrElse(schoolid, "null")
        //x._1.split("_area_")(1).split("_")(0)
        val value = data._2.value.getOrElse(area + "_" + schoolid, "null")

        var status = "null"
        var valuedata = "null"
        var disstatus = "null"
        if(x._2.split("_").length >= 4) {
          if (StringUtils.isNoneEmpty(x._2.split("_")(4)) && !"null".equals(x._2.split("_")(4))) {
            if ("4".equals(x._2.split("_")(0))) {
              status = "-1"
              valuedata = "-1" + "_" + x._2.split("_")(1)
              disstatus = x._2.split("_")(4)
            } else {
              val deliveryDate = x._2.split("_")(2)
              disstatus = x._2.split("_")(4)
              valuedata = x._2
              status = x._2.split("_")(0)
            }
          } else {
            if ("4".equals(x._2.split("_")(0))) {
              status = "-1"
              valuedata = "-1" + "_deliveryDate_" + x._2.split("_")(2) + "_disstatus_" + "4" + "_purchaseDate" + x._2.split("_purchaseDate")(1)
              disstatus = "4"
            } else {
              val deliveryDate = x._2.split("_")(2)
              disstatus = new RuleStatusStat().distributionstatus(data._4, deliveryDate).toString
              valuedata = x._2.split("_disstatus_")(0) + "_disstatus_" + disstatus + "_purchaseDate" + x._2.split("_purchaseDate")(1)
              status = x._2.split("_")(0)
            }
          }
        }else{
          if ("4".equals(x._2.split("_")(0))) {
            status = "-1"
            valuedata = "-1" + "_deliveryDate_" + x._2.split("_")(2) + "_" + "disstatus" + "_" + "4" + "_" + "purchaseDate" + "_" + "null" + "_" + "deliveryReDate" + "_" + "null"
            disstatus = "4"
          } else {

            val deliveryDate = x._2.split("_")(2)
            disstatus = new RuleStatusStat().distributionstatus(data._4, deliveryDate).toString
            valuedata = x._2 + "_" + "disstatus" + "_" + disstatus+ "_" + "purchaseDate" + "_" + "null" + "_" + "deliveryReDate" + "_" + "null"
            status = x._2.split("_")(0)
          }
        }

        val key = x._1.split("area")(0) + "area" + "_" + area + "_" + "sourceid" + x._1.split("sourceid")(1)

        (area, value, valuedata, schoolid, key, status, disstatus)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐"))

    distribution
  }

  override def distributiondealtotaldata(data: RDD[(String, String)]) = {

    val disTotal = data.filter(x => x._1.size > 7).map({
      x =>
        val v = x._1.split("_")
        if ("area".equals(v(0))) {
          ("area",(x._1,x._2))
        } else if ("masterid".equals(v(0))) {
          ("masterid",(x._1,x._2))
        } else if ("school-area".equals(v(0))) {
          ("school-area",(x._1,x._2))
        } else if ("school-masterid".equals(v(0))) {
          ("school-masterid",(x._1,x._2))
        } else if ("nature".equals(v(0))) {
          ("nature",(x._1,x._2))
        }else if("school-nature".equals(v(0))){
          ("school-nature",(x._1,x._2))
        }else if("level".equals(v(0))){
          ("level",(x._1,x._2))
        }else if("school-level".equals(v(0))){
          ("school-level",(x._1,x._2))
        } else if ("nat-area".equals(v(0))) {
          ("nat-area",(x._1,x._2))
        } else if ("school-nat-area".equals(v(0))) {
          ("school-nat-area",(x._1,x._2))
        } else if ("lev-area".equals(v(0))) {
          ("lev-area",(x._1,x._2))
        } else if ("school-lev-area".equals(v(0))) {
          ("school-lev-area",(x._1,x._2))
        }else if("dis-area".equals(v(0))){
          ("dis-area",(x._1,x._2))
        }else if("dis-school-area".equals(v(0))){
          ("dis-school-area",(x._1,x._2))
        }else if("dis-nature".equals(v(0))){
          ("dis-nature",(x._1,x._2))
        }else if("dis-school-nature".equals(v(0))){
          ("dis-school-nature",(x._1,x._2))
        }else if("dis-level".equals(v(0))){
          ("dis-level",(x._1,x._2))
        }else if("dis-school-level".equals(v(0))){
          ("dis-school-level",(x._1,x._2))
        }else if("dis-masterid".equals(v(0))){
          ("dis-masterid",(x._1,x._2))
        }else if("dis-school-masterid".equals(v(0))){
          ("dis-school-masterid",(x._1,x._2))
        }else if("department".equals(v(0))){
          ("department",(x._1,x._2))
        }else if("school-department".equals(v(0))){
          ("school-department",(x._1,x._2))
        }else if("dis-department".equals(v(0))){
          ("dis-department",(x._1,x._2))
        }else if("dis-school-department".equals(v(0))){
          ("dis-school-department",(x._1,x._2))
        }else if ("school-canteenmode".equals(v(0))){
          ("school-canteenmode",(x._1,x._2))
        }
        else {
          ("null",("null","null"))
        }
    })
    disTotal
  }

  override def retentiondealdata(data: RDD[(String, String)]) = {

    val retentoinData = data.map({
      x =>
        val area = x._2.split("_")(1)
        val liuyang = x._2.split("_")(14)
        val schoolid = x._2.split("_")(5)
        val reservestatus = x._2.split("_")(26)
        val createtime = x._2.split("_")(16)
        (area, liuyang, schoolid,reservestatus,createtime)
    })

    retentoinData
  }

  override def retentiondealtotaldata(data: RDD[(String, String)]) = {

    val redishDa = data.map({
      x =>
        if (x._1.contains("_")) {
          val v = x._1.split("_")
          if (v.length == 2) {
            ("area", (x._1, x._2))
          } else if ("masterid".equals(v(0))) {
            ("masterid", (x._1, x._2))
          } else if ("school-area".equals(v(0))) {
            ("school-area", (x._1, x._2))
          } else if ("school-area".equals(v(0))) {
            ("school-area", (x._1, x._2))
          } else if ("school-masterid".equals(v(0))) {
            ("school-masterid", (x._1, x._2))
          } else if ("nature".equals(v(0))) {
            ("nature", (x._1, x._2))
          } else if ("school-nature".equals(v(0))) {
            ("school-nature", (x._1, x._2))
          } else if ("level".equals(v(0))) {
            ("level", (x._1, x._2))
          } else if ("school-level".equals(v(0))) {
            ("school-level", (x._1, x._2))
          } else if ("nat-area".equals(v(0))) {
            ("nat-area", (x._1, x._2))
          } else if ("school-nat-area".equals(v(0))) {
            ("school-nat-area", (x._1, x._2))
          } else if ("lev-area".equals(v(0))) {
            ("lev-area", (x._1, x._2))
          } else if ("school-lev-area".equals(v(0))) {
            ("school-lev-area", (x._1, x._2))
          } else if ("re-area".equals(v(0))) {
            ("re-area", (x._1, x._2))
          } else if ("re-school-area".equals(v(0))) {
            ("re-school-area", (x._1, x._2))
          } else if ("re-masterid".equals(v(0))) {
            ("re-masterid", (x._1, x._2))
          } else if ("re-school-masterid".equals(v(0))) {
            ("re-school-masterid", (x._1, x._2))
          } else if ("re-nature".equals(v(0))) {
            ("re-nature", (x._1, x._2))
          } else if ("re-school-nature".equals(v(0))) {
            ("re-school-nature", (x._1, x._2))
          } else if ("re-level".equals(v(0))) {
            ("re-level", (x._1, x._2))
          } else if ("re-school-level".equals(v(0))) {
            ("re-school-level", (x._1, x._2))
          } else if ("department".equals(v(0))) {
            ("department", (x._1, x._2))
          } else if ("school-department".equals(v(0))) {
            ("school-department", (x._1, x._2))
          } else if ("re-department".equals(v(0))) {
            ("re-department", (x._1, x._2))
          } else if ("re-school-department".equals(v(0))) {
            ("re-school-department", (x._1, x._2))
          } else {
            ("null", ("null", "null"))
          }
        } else {
          ("null", ("null", "null"))
        }
    })
    redishDa



  }

  override def shanghaitotal(data: RDD[(String, String)]) = {
    data.map({
      x =>
        if("shanghai".equals(x._1)){
          ("shanghai",(x._1,x._2))
        }else {
          if (x._1.contains("_")) {
            val v = x._1.split("_")
            if (v.size == 2) {
              ("area", (x._1, x._2))
            } else if ("shanghai-level".equals(v(0))) {
              ("shanghai-level", (x._1, x._2))
            } else if ("area".equals(v(0)) && "level".equals(v(2))) {
              ("area-level", (x._1, x._2))
            } else if ("shanghai-nature".equals(v(0))) {
              ("shanghai-nature", (x._1, x._2))
            } else if ("area".equals(v(0)) && "nature".equals(v(2))) {
              ("area-nature", (x._1, x._2))
            } else if ("shanghai-canteenmode".equals(v(0))) {
              ("shanghai-canteenmode", (x._1, x._2))
            } else if ("area".equals(v(0)) && "canteenmode".equals(v(2))) {
              ("area-canteenmode", (x._1, x._2))
            } else if ("masterid".equals(v(0))) {
              ("masterid", (x._1, x._2))
            } else {
              ("null", ("null", "null"))
            }
          } else
          {
            ("null", ("null", "null"))
          }
        }

    })
  }

  override def warntotal(data: RDD[(String, String)]) = {
    data.map({
      x =>
        if ("year".equals(x._1)) {
          ("year", (x._1, x._2))
        } else if ("month".equals(x._1)) {
          ("month", (x._1, x._2))
        } else {
          if (x._1.contains("_")) {
            val v = x._1.split("_")(0)
            if ("departmentid-nodeal".equals(v)) {
              ("departmentid-nodeal", (x._1, x._2))
            } else if ("departmentid-deal".equals(v)) {
              ("departmentid-deal", (x._1, x._2))
            } else if ("warntype-total".equals(v)) {
              ("warntype-total", (x._1, x._2))
            } else if ("warntypechilid-nodeal".equals(v)) {
              ("warntypechilid-nodeal", (x._1, x._2))
            } else if ("warntypechilid-total".equals(v)) {
              ("warntypechilid-total", (x._1, x._2))
            } else if ("warndate-nodeal".equals(v)) {
              ("warndate-nodeal", (x._1, x._2))
            } else if ("warndate-deal".equals(v)) {
              ("warndate-deal", (x._1, x._2))
            } else if ("warndate-total".equals(v)) {
              ("warndate-total", (x._1, x._2))
            } else {
              ("null", ("null", "null"))
            }
          } else {
            ("null", ("null", "null"))
          }
        }
    })
  }
}
