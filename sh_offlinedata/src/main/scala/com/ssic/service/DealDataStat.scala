package com.ssic.service

import com.ssic.impl.DealDataFunc
import org.apache.commons.lang3._
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class DealDataStat extends DealDataFunc {

  /**

    * * 对排菜的统计数据进行分类处理

    * * @param platoonTotal 已存在的排菜表的统计数据

    */
  override def platoontotal(platoonTotal: RDD[(String, String)]): RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))] = {
    val platoontotaldata: RDD[((String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String), (String, String))] = platoonTotal.map({
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
  /**

    * * 对用料计划的详情数据进行数据处理

    * * @param useMaterialPlanDetailData 已存在的排菜表的统计数据

    * * @param projid2schoolid 项目点id获取学校id

    * * @param projid2schoolname 项目点id获取学校名字

    * * @param gongcanSchool 供餐学校数据

    * * @param projid2Area 项目点id获取学校区号

    * * @param b2bPlatoonSchool b2b的排菜学校id

    * * @param schoolid2Projid 学校id对于项目点id

    * * @param schoolid2suppliername 学校id对于团餐公司名字

    * * @return  RDD[(String, String, String, String, String, String, String)] (区号，供餐，状态，key，学校名字，学校id,valuedata)

    */
  override def usematerialdealdata(useMaterialPlanDetailData:RDD[(String, String)],projid2schoolid:Broadcast[Map[String, String]],projid2schoolname:Broadcast[Map[String, String]],gongcanSchool:Broadcast[Map[String, String]],projid2Area:Broadcast[Map[String, String]] , b2bPlatoonSchool: RDD[(String, Int)],schoolid2Projid:Broadcast[Map[String, String]],schoolid2suppliername:Broadcast[Map[String, String]]): RDD[(String, String, String, String, String, String, String)] = {

    val useMaterialData = useMaterialPlanDetailData.map({
      x =>

        val projid = x._1.split("_projid_")(1).split("_")(0)
        val area = projid2Area.value.getOrElse(projid, "null")
        val schoolid = projid2schoolid.value.getOrElse(projid, "null")
        val schoolname = projid2schoolname.value.getOrElse(projid, "null")
        val status = x._2
        val key = "area" + "_" + area + "_" + "type" + x._1.split("name")(0).split("type")(1) + "name" + "_" + schoolname + "_" + "projid" + x._1.split("projid")(1)
        val gongcan=gongcanSchool.value.getOrElse(area + "_" + schoolid,"null")
        val valuedata="null"

        (area, gongcan, status, key, schoolname, schoolid,valuedata)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐")).filter(x => !x._5.equals("null")).filter(x => !x._6.equals("null"))

    val b2bUseMaterialData = b2bPlatoonSchool.map({
      x =>
        val schoolid = x._1
        val projid = schoolid2Projid.value.getOrElse(schoolid, "null")
        val area = projid2Area.value.getOrElse(projid, "null")
        val schoolname = projid2schoolname.value.getOrElse(projid, "null")
        val suppliername = schoolid2suppliername.value.getOrElse(schoolid, "null")
        val status = x._2.toString
        val key = "area" + "_" + area + "_" + "type" + "_" + "0" + "_" + "name" + "_" + schoolname + "_" + "projid" + "_" + projid + "_" + "suppliername" + "_" + suppliername
        val gongcan = gongcanSchool.value.getOrElse(area + "_" + schoolid, "null")
        val valuedata = "null"
        (area, gongcan, status, key, schoolname, schoolid, valuedata)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐")).filter(x => !x._5.equals("null")).filter(x => !x._6.equals("null"))

    val useMaterialDealData = useMaterialData.union(b2bUseMaterialData)

    return useMaterialDealData
  }
  /**

    * * 对用料的统计数据进行数据处理

    * * @param data 已存在的用料计划统计数据

    */

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

  /**

    * * 对配送计划的详情数据进行数据处理

    * * @param distributionDetailData 配送计划临时表数据

    * * @param gongcanSchool 供餐学校数据

    * * @param school2Area 学校id获取学校区号

    * * @param date 时间

    * * @param RDD[(String, String, String, String, String, String, String)]  （区号，供餐，处理后的value，学校id，key，状态，规范状态）


    */
  override def distributiondealdata(distributionDetailData:RDD[(String, String)],gongcanSchool:Broadcast[Map[String, String]],school2Area:Broadcast[Map[String, String]],date:String): RDD[(String, String, String, String, String, String, String)] = {
    val distribution = distributionDetailData.map({
      x =>

        val schoolid = x._1.split("_schoolid_")(1).split("_")(0)
        val area = school2Area.value.getOrElse(schoolid, "null")
        //x._1.split("_area_")(1).split("_")(0)
        val gongcan = gongcanSchool.value.getOrElse(area + "_" + schoolid, "null")

        var status = "null"
        var valuedata = "null"
        var disstatus = "null"
        if(x._2.split("_").length >= 4) {
          if (StringUtils.isNoneEmpty(x._2.split("_")(4)) && !"null".equals(x._2.split("_")(4))) {
            if ("4".equals(x._2.split("_")(0))) {
              status = "-1"
              valuedata = "-1" + "_" +"deliveryDate"+ x._2.split("deliveryDate")(1)
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
              disstatus = new RuleStatusStat().distributionstatus(date, deliveryDate).toString
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
            disstatus = new RuleStatusStat().distributionstatus(date, deliveryDate).toString
            valuedata = x._2 + "_" + "disstatus" + "_" + disstatus+ "_" + "purchaseDate" + "_" + "null" + "_" + "deliveryReDate" + "_" + "null"
            status = x._2.split("_")(0)
          }
        }

        val key = x._1.split("area")(0) + "area" + "_" + area + "_" + "sourceid" + x._1.split("sourceid")(1)

        (area, gongcan, valuedata, schoolid, key, status, disstatus)
    }).filter(x => !x._2.equals("null")).filter(x => !x._2.split("_")(0).equals("不供餐"))

    distribution
  }
  /**

    * * 对配送的统计数据进行数据处理

    * * @param data 已存在的配送计划统计数据

    */
  override def distributiondealtotaldata(data: RDD[(String, String)]):RDD[(String,(String, String))] = {

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
  /**

    * * 留样计划处理后的数据

    * * @param data 已存在的留样计划数据

    * * @return RDD[(String, String,String,String,String,String,String)] (区号，留样，学校id，留样规范状态，创建时间，key,valuedata)


    */
  override def retentiondealdata(data: RDD[(String, String)]): RDD[(String, String,String,String,String,String,String)] = {

    val retentoinData = data.map({
      x =>
        val area = x._2.split("_")(1)
        val liuyang = x._2.split("_")(14)
        val schoolid = x._2.split("_")(5)
        val reservestatus = x._2.split("_")(26)
        val createtime = x._2.split("_")(16)
        val key="null"
        val valuedata="null"
        (area, liuyang, schoolid,reservestatus,createtime,key,valuedata)
    })

    retentoinData
  }

  /**

    * * 处理好的留样计划统计数据

    * * @param data 已存在的留样计划统计数据


    */
  override def retentiondealtotaldata(data: RDD[(String, String)]):RDD[(String,(String, String))] = {

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

  override def shanghaitotal(data: RDD[(String, String)]):RDD[(String,(String, String))] = {
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

  override def warntotal(data: RDD[(String, String)]):RDD[(String,(String, String))] = {
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
