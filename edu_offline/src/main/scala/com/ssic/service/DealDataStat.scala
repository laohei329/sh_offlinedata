package com.ssic.service

import com.ssic.impl.DealDataFunc
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DealDataStat extends DealDataFunc{

  override def platoontotal(data: RDD[(String, String)]):RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))] = {
    val platoontotaldata: RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))] = data.map({
      x =>
        val v = x._1.split("_")
        if (v.size == 3) {
          ((x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if ("level".equals(v(0)) && !"plastatus".equals(v(2))) {
          (("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if ("nature".equals((v(0))) && !"plastatus".equals(v(4))) {
          (("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if ("canteenmode".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("level".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if ("nature".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if ("canteenmode".equals(v(2))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if ("masterid".equals(v(0))  && !"plastatus".equals(v(4))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        } else if("department".equals(v(0))  && !"plastatus".equals(v(2))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),(x._1, x._2),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        }else if("area".equals(v(0)) && "plastatus".equals(v(2))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),(x._1, x._2),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        }else if("masterid".equals(v(0)) && "plastatus".equals(v(4))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),(x._1, x._2),("null", "null"),("null", "null"),("null", "null"))
        }else if("department".equals(v(0)) && "plastatus".equals(v(2))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),(x._1, x._2),("null", "null"),("null", "null"))
        }else if("level".equals(v(0)) && "plastatus".equals(v(2))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),(x._1, x._2),("null", "null"))
        }else if("nature".equals(v(0)) && "plastatus".equals(v(4))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),(x._1, x._2))
        }else {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"),("null", "null"))
        }
    })
    platoontotaldata

  }

  override def usematerialdealdata(data: (RDD[(String, String)], Broadcast[Map[String, String]], Broadcast[Map[String, String]],Broadcast[Map[String, String]],Broadcast[Map[String, String]])): RDD[(String, String,String,String,String,String)] = {

    val useMaterialData = data._1.map({
      x =>

        val projid = x._1.split("_projid_")(1).split("_")(0)
        val area = data._5.value.getOrElse(projid,"null")
        val schoolid = data._2.value.getOrElse(projid, "null")
        val schoolname = data._3.value.getOrElse(projid, "null")
        val status = x._2
        val key = "area"+"_"+area+"_"+"type"+x._1.split("name")(0).split("type")(1) + "name" + "_" + schoolname + "_" + "projid" + x._1.split("projid")(1)
        val gongcan = data._4.value.getOrElse(area + "_" + schoolid, "null")

        (area, gongcan, status, key, schoolname,schoolid)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐")).filter(x => !x._4.split("suppliername_")(1).equals("null")).filter(x => !x._5.equals("null")).filter(x => !x._6.equals("null"))

    useMaterialData
  }

  override def usematerialdealtotaldata(data: RDD[(String, String)]) = {
    val useNaterialDa = data.filter(x => x._1.size > 7).map({
      x =>
        val v = x._1.split("_")(0)
        if ("area".equals(v)) {
          ((x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("masterid".equals(v)) {
          (("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-area".equals(v)) {
          (("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-masterid".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nature".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-nature".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("level".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-level".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nat-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"),("null", "null"))
        } else if ("school-nat-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"))
        } else if ("lev-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"))
        } else if ("school-lev-area".equals(v)) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2))
        } else {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        }
    })
    useNaterialDa
  }

  override def distributiondealdata(data: (RDD[(String, String)],Broadcast[Map[String, String]],Broadcast[Map[String, String]])): RDD[(String, String,String,String,String,String)] = {
    val distribution = data._1.map({
      x =>

        val schoolid = x._1.split("_schoolid_")(1).split("_")(0)
        val area = data._3.value.getOrElse(schoolid,"null")//x._1.split("_area_")(1).split("_")(0)
        val value = data._2.value.getOrElse(area + "_" + schoolid, "null")

        var status ="null"
        var valuedata ="null"
        if(x._2.contains("_").equals(true)){
          if("4".equals(x._2.split("_")(0))){
            status = "-1"
            valuedata= "-1"+"_"+x._2.split("_")(1)
          }else{
            status = x._2.split("_")(0)
            valuedata=x._2
          }

        }else{
          if("4".equals(x._2)){
            status = "-1"
            valuedata = "-1"
          }else{
            status = x._2
            valuedata = x._2
          }
        }
        val key =x._1.split("area")(0)+"area"+"_"+area+"_"+"sourceid"+x._1.split("sourceid")(1)

        (area, value, valuedata, schoolid, key,status)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐"))

    distribution
  }

  override def distributiondealtotaldata(data: RDD[(String, String)]) = {

    val disTotal = data.filter(x => x._1.size > 7).map({
      x =>
        val v = x._1.split("_")
        if ("area".equals(v(0))) {
          ((x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("masterid".equals(v(0))) {
          (("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-area".equals(v(0))) {
          (("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("school-masterid".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nature".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        }else if("school-nature".equals(v(0))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        }else if("level".equals(v(0))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        }else if("school-level".equals(v(0))){
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else if ("nat-area".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"),("null", "null"))
        } else if ("school-nat-area".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"))
        } else if ("lev-area".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"))
        } else if ("school-lev-area".equals(v(0))) {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2))
        } else {
          (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
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
        (area, liuyang, schoolid)
    })

    retentoinData
  }

  override def retentiondealtotaldata(data: RDD[(String, String)]) = {

    val redishDa = data.map({
      x =>
        if (!x._1.contains("_")) {
          ((x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
        } else {
          val v = x._1.split("_")
          if ("masterid".equals(v(0))) {
            (("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("school-area".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("school-masterid".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("nature".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("school-nature".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("level".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("school-level".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("nat-area".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"))
          } else if ("school-nat-area".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"))
          } else if ("lev-area".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"))
          } else if ("school-lev-area".equals(v(0))) {
            (("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"),("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), (x._1, x._2))
          }
          else {
            (("null", "null"), ("null", "null"), (x._1, x._2), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"), ("null", "null"))
          }
        }
    })
    redishDa
  }
}
