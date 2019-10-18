package com.ssic.report

import java.util.Date

import com.ssic.beans.SchoolBean
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



  //项目点排菜的菜品详情
  def dishDetail(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val packages = data._1.filter(x => x != null && x.table.equals("t_saas_package") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val dishData = data._1.filter(x => x != null && x.table.equals("t_saas_package_dishes") && x.`type`.equals("insert") && x.data.stat.equals("1"))

    val packageData = packages.distinct().map(x => {
      val id = x.data.id
      val supply_date = x.data.supply_date
      val replaceAll = supply_date.replaceAll("\\D", "-")
      val date = format.format(format.parse(replaceAll))
      val supplier_id = x.data.supplier_id  //供应商Id
      val school_id = x.data.school_id      //项目点Id
      val menu_group_name = x.data.menu_group_name   //菜单分组名  教工餐
      val ledger_type = x.data.ledger_type      //配送类型: 1 原料 2 成品菜
      val stat = x.data.stat
      val now = format.format(new Date())
      if (format.parse(now).getTime <= format.parse(date).getTime) {
        (id, List(date, supplier_id, school_id, stat, menu_group_name,ledger_type))
      } else {
        ("null", List("null", "null", "null", "null", "null","null"))
      }

    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0))).filter(x => !x._1.equals("null"))
    val dishFlitData = dishData.distinct().map({
      x =>
        (x.data.package_id, List(x.data.dishes_name, x.data.dishes_number, x.data.id, x.data.cater_type_name,x.data.category))
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0)))
    packageData.leftOuterJoin(dishFlitData).map(x => (x._2._1, x._2._2.getOrElse(List("null")), x._1)).filter(x => !x._2(0).equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              //dish(id),supplydate,packageid,supplierid,schoolid,groupname,dishesname,dishesnumber,catertypename(早餐),ledgertype(配送类型),category（分类）
              jedis.hset(x._1(0) + "_" + "dish-menu", x._3+"_"+x._2(2), "supplierid" + "_" + x._1(1) + "_" + "schoolid" + "_" + x._1(2) + "_" + "groupname" + "_" + x._1(4) + "_" + "dishesname" + "_" + x._2(0) + "_" + "dishesnumber" + "_" + x._2(1) + "_" + "catertypename" + "_"+x._2(3)+"_"+"ledgertype"+"_"+x._1(5)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"))

              jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4),x._2(1).toFloat)
              jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"),x._2(1).toFloat)
              jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null")+ "_" + "schoolid" + "_" + x._1(2),x._2(1).toFloat)
              jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3),x._2(1).toFloat)
              jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"),x._2(1).toFloat)
              jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null")+ "_" + "schoolid" + "_" + x._1(2),x._2(1).toFloat)


          })
      })

  }

  def dishUpdateDelete(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val packages = data._1.filter(x => x != null && x.table.equals("t_saas_package") && x.`type`.equals("update") && x.data.stat.equals("1"))
    val dishData = data._1.filter(x => x != null && x.table.equals("t_saas_package_dishes") && x.`type`.equals("update") && x.data.stat.equals("1"))
    val packageData = packages.distinct().map(x => {
      val id = x.data.id
      val supply_date = x.data.supply_date
      val replaceAll = supply_date.replaceAll("\\D", "-")
      val date = format.format(format.parse(replaceAll))
      val supplier_id = x.data.supplier_id
      val school_id = x.data.school_id
      val menu_group_name = x.data.menu_group_name
      val ledger_type = x.data.ledger_type      //配送类型: 1 原料 2 成品菜
      val stat = x.data.stat
      val now = format.format(new Date())
      if (format.parse(now).getTime <= format.parse(date).getTime) {
        (id, List(date, supplier_id, school_id, stat, menu_group_name,ledger_type))
      } else {
        ("null", List("null", "null", "null", "null", "null","null"))
      }

    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0))).filter(x => !x._1.equals("null"))
    val dishFlitData = dishData.distinct().map({
      x =>
        (x.data.package_id, List(x.data.dishes_name, x.data.dishes_number, x.data.id, x.data.cater_type_name,x.data.category,x.old.dishes_name,x.old.dishes_number,x.data.stat))
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => StringUtils.isNoneEmpty(x._2(0)))
    packageData.leftOuterJoin(dishFlitData).map(x => (x._2._1, x._2._2.getOrElse(List("null")), x._1)).filter(x => !x._2(0).equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              logger.info(x._1(0)+"_"+x._1(1)+"_"+x._1(2)+"_"+x._1(3)+"_"+x._1(4)+"_"+x._1(5)+"_"+x._2(0)+"_"+x._2(1)+"_"+x._2(2)+"_"+x._2(3)+"_"+x._2(4)+"_"+x._2(5)+"_"+x._2(6)+"_"+x._2(7)+"_"+x._3)
              //dish(id),supplydate,packageid,supplierid,schoolid,groupname,dishesname,dishesnumber,catertypename(早餐)
              val strings = jedis.hkeys(x._1(0)+"_"+"dish-menu")
              for (i <- strings.asScala){
                val id = i.split("_")(1)
                if(id.equals(x._2(2))){
                  jedis.hset(x._1(0) + "_" + "dish-menu", x._3+"_"+x._2(2), "supplierid" + "_" + x._1(1) + "_" + "schoolid" + "_" + x._1(2) + "_" + "groupname" + "_" + x._1(4) + "_" + "dishesname" + "_" + x._2(0) + "_" + "dishesnumber" + "_" + x._2(1) + "_" + "catertypename" +"_"+ x._2(3)+"_"+"ledgertype"+"_"+x._1(5)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"))

                  if(StringUtils.isNoneEmpty(x._2(6)) && !x._2(6).equals("null")){
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4),x._2(1).toFloat)
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"),x._2(1).toFloat)
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null")+ "_" + "schoolid" + "_" + x._1(2),x._2(1).toFloat)
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3),x._2(1).toFloat)
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"),x._2(1).toFloat)
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null")+ "_" + "schoolid" + "_" + x._1(2),x._2(1).toFloat)

                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4),-(x._2(6).toFloat))
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"),-(x._2(6).toFloat))
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null")+ "_" + "schoolid" + "_" + x._1(2),-(x._2(6).toFloat))
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3),-(x._2(6).toFloat))
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null"),-(x._2(6).toFloat))
                    jedis.hincrByFloat(x._1(0) + "_" + "dish-menu-total","dishesname"+"_"+x._2(0)+"_"+"category"+"_"+x._2(4)+"_"+"catertypename"+"_"+x._2(3)+"_"+"area"+"_"+data._2.value.getOrElse(x._1(2),"null")+ "_" + "schoolid" + "_" + x._1(2),-(x._2(6).toFloat))
                  }else{
                    logger.info("不需要计算的菜品更新，其数量没有变化")
                  }

                }else{
                  logger.info("不需要计算的菜品更新，其不在之前的redis中")
                }
              }

          })
      })
  }

  def packageUpdateDelete(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val packageData = data._1.filter(x => x != null && x.table.equals("t_saas_package") && !x.`type`.equals("insert"))
    packageData.map({
      x =>
        val id = x.data.id
        val supplier_id = x.data.supplier_id
        val school_id = x.data.school_id
        val menu_group_name = x.data.menu_group_name
        val stat = x.data.stat
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val ledger_type = x.data.ledger_type      //配送类型: 1 原料 2 成品菜
        if("delete".equals(x.`type`)){
          (id, "delete", supplier_id, school_id, menu_group_name, stat,date,ledger_type,"null")
        }else{
          (id, "update", supplier_id, school_id, menu_group_name, stat,date,ledger_type,x.old.stat)
        }

    }).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val strings = jedis.hkeys(x._7+"_"+"dish-menu")
            for (i <- strings.asScala){
              val packageid = i.split("_")(0)
              val v = jedis.hget(x._7+"_"+"dish-menu", i)
              if(packageid.equals(x._1)){
                if("delete".equals(x._2)){
                  jedis.hdel(x._7+"_"+"dish-menu", i)

                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null"),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null")+ "_" + "schoolid" + "_" + x._4,-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"catertypename"+"_"+v.split("_catertypename_")(1).split("_ledgertype")(0),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"catertypename"+"_"+v.split("_catertypename_")(1).split("_ledgertype")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null"),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                  jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"catertypename"+"_"+v.split("_catertypename_")(1).split("_ledgertype")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null")+ "_" + "schoolid" + "_" + x._4,-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                }else if("update".equals(x._2)){
                  if(StringUtils.isNoneEmpty(x._9) && !x._9.equals("null")){
                    if("1".equals(x._9) && "0".equals(x._6)){
                      jedis.hdel(x._7+"_"+"dish-menu", i)

                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null"),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null")+ "_" + "schoolid" + "_" + x._4,-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"catertypename"+"_"+v.split("_catertypename_")(1).split("_ledgertype")(0),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"catertypename"+"_"+v.split("_catertypename_")(1).split("_ledgertype")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null"),-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                      jedis.hincrByFloat(x._7 + "_" + "dish-menu-total","dishesname"+"_"+v.split("_dishesname_")(1).split("_dishesnumber")(0)+"_"+"category"+"_"+v.split("_category_")(1).split("_area")(0)+"_"+"catertypename"+"_"+v.split("_catertypename_")(1).split("_ledgertype")(0)+"_"+"area"+"_"+data._2.value.getOrElse(x._4,"null")+ "_" + "schoolid" + "_" + x._4,-(v.split("_dishesnumber_")(1).split("_catertypename")(0).toFloat))
                    }else if("0".equals(x._9) && "1".equals(x._6)){
                      jedis.hset(x._7+"_"+"dish-menu", i, "supplierid" + "_" + x._3 + "_" + "schoolid" + "_" + x._4 + "_" + "groupname" + "_" + x._5 + "_dishesname" + v.split("_dishesname")(1).split("ledgertype")(0)+"ledgertype"+"_"+x._8+"_"+"category"+v.split("category")(1).split("area")(0)+"area"+"_"+data._2.value.getOrElse(x._4,"null"))
                    }else{
                      logger.info("排菜信息中存在这个id但是不需要计算的1111111111--------------------")
                    }
                  }else{
                    logger.info("排菜信息中存在这个id但是不需要计算的444444444444444444--------------------")
                  }
                }else{
                  logger.info("排菜信息中存在这个id但是不需要计算的22222222222--------------------")
                }
              }else{
                logger.info("排菜信息中存在这个id但是不需要计算的333333333--------------------")
              }
            }
        })
    })
  }

  def retentionDishDetail(filterData: RDD[SchoolBean]) = {
    val retentionDish = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample_dishes") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val retentionDishData = retentionDish.distinct().map({
      x =>
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val package_id = x.data.package_id
        var creator="null"
        if(StringUtils.isNoneEmpty(x.data.creator) && !x.data.creator.equals("null")){
          creator=x.data.creator
        }else{
          creator
        }
        val id = x.data.id
        val create_time = x.data.create_time
        var remark="null"
        if(StringUtils.isNoneEmpty(x.data.remark) && !x.data.remark.equals("null")){
          remark=x.data.remark
        }else{
          remark
        }

        val cater_type_name = x.data.cater_type_name
        val menu_group_name = x.data.menu_group_name

        val reserve_hour =Rule.int(x.data.reserve_hour.toString)
        val reserve_minute =Rule.int(x.data.reserve_minute.toString)
        val reservedata = supply_date+" "+reserve_hour+":"+reserve_minute+":"+"00"

        val now = format.format(new Date())
        //if (format.parse(now).getTime <= format.parse(date).getTime) {
          (id, List(package_id, creator, date, create_time, remark,cater_type_name,menu_group_name,reservedata))
//        }else{
//          ("null",List("null","null","null","null","null","null","null","null"))
//        }
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => !x._1.equals("null"))

    val retentionData = retention.distinct().map({
      x =>
        val sample_id = x.data.sample_id
        val quantity = x.data.quantity
        val dishes = x.data.dishes
        val id = x.data.id
        (sample_id, List(quantity,dishes,id))
    })

    retentionDishData.leftOuterJoin(retentionData).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).filter(x => !("null").equals(x._3(0)))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              //id,data,package_id,create_time,creator,quantity,remark
              jedis.hset( x._2(2)+"_"+"retention-dish", x._1+"_"+x._3(2) , "packageid"+"_" + x._2(0) +"_"+ "createtime" + "_" + x._2(3)+"_"+"creator" + "_" + x._2(1) + "_" + "quantity" + "_" + x._3(0)+ "_" + "remark" + "_" + x._2(4)+"_"+"groupname"+"_"+x._2(6)+"_"+"catertypename"+"_"+x._2(5)+"_"+"dishesname"+"_"+x._3(1)+"_"+"reservedata"+"_"+x._2(7))
          })
      })

  }

  def retentionDishUpdateDelte(filterData: RDD[SchoolBean]) = {
    val retentionDish = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample") && x.`type`.equals("update") && x.data.stat.equals("1"))
    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample_dishes") && x.`type`.equals("update") && x.data.stat.equals("1"))
    val retentionDishData = retentionDish.distinct().map({
      x =>
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val package_id = x.data.package_id
        var creator="null"
        if(StringUtils.isNoneEmpty(x.data.creator) && !x.data.creator.equals("null")){
          creator=x.data.creator
        }else{
          creator
        }
        val id = x.data.id
        val create_time = x.data.create_time
        var remark="null"
        if(StringUtils.isNoneEmpty(x.data.remark) && !x.data.remark.equals("null")){
          remark=x.data.remark
        }else{
          remark
        }

        val cater_type_name = x.data.cater_type_name
        val menu_group_name = x.data.menu_group_name

        val reserve_hour =Rule.int(x.data.reserve_hour.toString)
        val reserve_minute =Rule.int(x.data.reserve_minute.toString)
        val reservedata = supply_date+" "+reserve_hour+":"+reserve_minute+":"+"00"

        val now = format.format(new Date())
      //  if (format.parse(now).getTime <= format.parse(date).getTime) {
          (id, List(package_id, creator, date, create_time, remark,cater_type_name,menu_group_name,reservedata))
//        }else{
//          ("null",List("null","null","null","null","null","null","null","null"))
//        }
    }).filter(x => StringUtils.isNoneEmpty(x._1)).filter(x => !x._1.equals("null"))

    val retentionData = retention.distinct().map({
      x =>
        val sample_id = x.data.sample_id
        val quantity = x.data.quantity
        val dishes = x.data.dishes
        val id = x.data.id
        (sample_id, List(quantity,dishes,id))
    })

    retentionDishData.leftOuterJoin(retentionData).map(x => (x._1, x._2._1, x._2._2.getOrElse(List("null")))).filter(x => !x._3(0).equals("null"))
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            x =>
              //id,data,package_id,create_time,creator,quantity,remark
              jedis.hset( x._2(2)+"_"+"retention-dish", x._1+"_"+x._3(2) , "packageid"+"_" + x._2(0) +"_"+ "createtime" + "_" + x._2(3)+"_"+"creator" + "_" + x._2(1) + "_" + "quantity" + "_" + x._3(0)+ "_" + "remark" + "_" + x._2(4)+"_"+"groupname"+"_"+x._2(6)+"_"+"catertypename"+"_"+x._2(5)+"_"+"dishesname"+"_"+x._3(1)+"_"+"reservedata"+"_"+x._2(7))
          })
      })

  }

  def retentionUpdateDelte(filterData: RDD[SchoolBean]) = {
    val retention = filterData.filter(x => x != null && x.table.equals("t_pro_reserve_sample") && !x.`type`.equals("insert"))

    val retentionData = retention.distinct().map({
      x =>
        val supply_date = x.data.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val id = x.data.id
        var remark="null"
        if(StringUtils.isNoneEmpty(x.data.remark) && !x.data.remark.equals("null")){
          remark=x.data.remark
        }else{
          remark
        }
        val cater_type_name = x.data.cater_type_name
        val menu_group_name = x.data.menu_group_name

        val reserve_hour =Rule.int(x.data.reserve_hour.toString)
        val reserve_minute =Rule.int(x.data.reserve_minute.toString)
        val reservedata = supply_date+" "+reserve_hour+":"+reserve_minute+":"+"00"

        val now = format.format(new Date())
       // if (format.parse(now).getTime <= format.parse(date).getTime){
          (id, remark, x.`type`,date, x.data.stat,cater_type_name,menu_group_name,reservedata)
//        }else{
//          ("null", "null", "null","null","null","null","null","null")
//        }


    }).filter(x => !x._1.equals("null"))

    retentionData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val strings = jedis.hkeys(x._4+"_"+"retention-dish")
            for (i <- strings.asScala){
              val id = i.split("_")(0)
              if(id.equals(x._1)){
                if ("delete".equals(x._3)) {
                  jedis.hdel(x._4+"_"+"retention-dish", i)
                } else if ("update".equals(x._3) && "1".equals(x._5)) {
                  val v = jedis.hget(x._4+"_"+"retention-dish", i)
                  if(StringUtils.isNoneEmpty(v) && !v.equals("null")){
                    logger.info(x._4+";"+i+";_"+v+";_"+x._2+";_"+x._7+";样子信息中存在这个id，并且是更新操作")
                    jedis.hset(x._4+"_"+"retention-dish", i, v.split("remark")(0) + "remark_" + x._2+"_"+"groupname"+"_"+x._7+"_"+"catertypename"+"_"+x._6++"_"+"dishesname"+v.split("dishesname")(1))
                  }else{
                    logger.info("留样子信息中存在这个id但是不需要计算的--------------------")
                  }
                } else if ("update".equals(x._2) && "0".equals(x._5)) {
                  jedis.hdel(x._4+"_"+"retention-dish",i)
                }
              }

            }


        })
    })

  }

}
