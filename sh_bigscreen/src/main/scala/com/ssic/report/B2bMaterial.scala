package com.ssic.report

/**
  * 原料供应统计以及明细
  */

import java.util.Date

import com.ssic.beans.SchoolBean
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object B2bMaterial {

  private val logger = LoggerFactory.getLogger(this.getClass)

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def Material2SupplierUpdate(filterData: SchoolBean): (String, String, String, String, Float, String, String, String, String, String, Float) = {
    var stat_name = "null"
    try {
      stat_name = filterData.old.stat
    } catch {
      case e: Exception => {
        logger.error(s"parse json error: $stat_name", e)
        return ("null", "null", "null", "null", 0, "null", "null", "null", "null", "null", 0)
      }
    }
    ("update", filterData.data.name, filterData.data.supplier_material_name, filterData.data.wares_type_name, filterData.data.actual_quantity, filterData.data.amount_unit, filterData.data.supply_name, filterData.data.supplier_name, filterData.data.stat, filterData.old.stat, filterData.old.actual_quantity)
  }

  def MaterialData(filterData: RDD[SchoolBean]): RDD[(String, String, String, String, String, Float, String, String, String, String, String, String, String, Float, String)] = {

    val ledger = filterData.filter(x => x != null && x.table.equals("t_pro_ledger"))

    val ledgerData: RDD[(String, String, String, String, String, Float, String, String, String, String, String, String, String, Float, String)] = ledger.distinct().map({
      x =>
        val master_id = x.data.master_id
        val receiver_id = x.data.receiver_id
        val use_date = x.data.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val now = format.format(new Date())
        if (format.parse(date).getTime >= format.parse(now).getTime) {
          if ("insert".equals(x.`type`) && "1".equals(x.data.stat)) {
            //表状态，id,标准名，物料名称，分类，实际用量，单位,供应商名称，团餐公司名称,时间,stat,oldstata,学校id,oldactual_quantity
            ("insert", master_id, x.data.name, x.data.supplier_material_name, x.data.wares_type_name, x.data.actual_quantity, x.data.amount_unit, x.data.supply_name, x.data.supplier_name, date, x.data.stat, "null", receiver_id, 0.toFloat,x.data.id)
          } else if ("delete".equals(x.`type`)) {
            ("delete", master_id, x.data.name, x.data.supplier_material_name, x.data.wares_type_name, x.data.actual_quantity, x.data.amount_unit, x.data.supply_name, x.data.supplier_name, date, x.data.stat, "null", receiver_id, 0.toFloat,x.data.id)
          } else if ("update".equals(x.`type`)) {
            (Material2SupplierUpdate(x)._1, master_id, Material2SupplierUpdate(x)._2, Material2SupplierUpdate(x)._3, Material2SupplierUpdate(x)._4, Material2SupplierUpdate(x)._5, Material2SupplierUpdate(x)._6, Material2SupplierUpdate(x)._7, Material2SupplierUpdate(x)._8, date, Material2SupplierUpdate(x)._9, Material2SupplierUpdate(x)._10, receiver_id, Material2SupplierUpdate(x)._11,x.data.id)
          } else {
            ("null", "null", "null", "null", "null", 0, "null", "null", "null", "null", "null", "null", "null", 0.toFloat, "null")
          }
        } else {
          ("null", "null", "null", "null", "null", 0, "null", "null", "null", "null", "null", "null", "null", 0.toFloat, "null")
        }

    })
    ledgerData

  }

  def Material2Suppllier(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val materialData = MaterialData(data._1).filter(x => !x._1.equals("null"))
    materialData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if ("insert".equals(x._1) && x._11.equals("1")) {
              logger.info("综合分析中原料的插入数据" + x._1 + "_" + x._2 + "_" + x._3 + "_" + x._4 + "_" + x._5 + "_" + x._6 + "_" + x._7 + "_" + x._8 + "_" + x._9 + "_" + x._10 + "_" + x._11 + "_" + x._12 + "_" + x._13)
              if (x._5.equals("调味品") || x._5.equals("食品添加剂") || x._5.equals("食用油、油脂及其制品")) {
                jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, x._6)
                jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), x._6)
                jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, x._6)
              } else {
                jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, x._6)
                jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), x._6)
                jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, x._6)
              }
            } else if ("delete".equals(x._1)) {
              logger.info("综合分析中原料的删除数据" + x._1 + "_" + x._2 + "_" + x._3 + "_" + x._4 + "_" + x._5 + "_" + x._6 + "_" + x._7 + "_" + x._8 + "_" + x._9 + "_" + x._10 + "_" + x._11 + "_" + x._12 + "_" + x._13)
              val strings = jedis.hkeys(x._10+"_"+"Material2SupllierDetail")
              for (i <- strings.asScala){
                val id = i.split("_")(1)
                if(id.equals(x._15)){
                  if (x._5.equals("调味品") || x._5.equals("食品添加剂") || x._5.equals("食用油、油脂及其制品")) {
                    jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, -x._6)
                    jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), -x._6)
                    jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, -x._6)
                  } else {
                    jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, -x._6)
                    jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), -x._6)
                    jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, -x._6)
                  }
                }else{
                  logger.info("综合分析中原料的删除数据不在之前的redis中")
                }
              }
            } else if ("update".equals(x._1)) {
              logger.info("综合分析中原料的更新数据" + x._1 + "_" + x._2 + "_" + x._3 + "_" + x._4 + "_" + x._5 + "_" + x._6 + "_" + x._7 + "_" + x._8 + "_" + x._9 + "_" + x._10 + "_" + x._11 + "_" + x._12 + "_" + x._13)
              val strings = jedis.hkeys(x._10+"_"+"Material2SupllierDetail")
              for (i <- strings.asScala){
                val id = i.split("_")(1)
                if(id.equals(x._15)){
                  if (StringUtils.isNoneEmpty(x._12) && !x._12.equals("null")) {
                    if ("1".equals(x._12) && "0".equals(x._11)) {
                      if (x._5.equals("调味品") || x._5.equals("食品添加剂") || x._5.equals("食用油、油脂及其制品")) {
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, -x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), -x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, -x._6)

                      } else {
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, -x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), -x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, -x._6)
                      }
                    } else if ("0".equals(x._12) && "1".equals(x._11)) {
                      if (x._5.equals("调味品") || x._5.equals("食品添加剂") || x._5.equals("食用油、油脂及其制品")) {
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, x._6)
                      } else {
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, x._6)
                      }
                    } else {
                      logger.info("这个原料是不需要的不需要在综合信息中计算")
                    }
                  } else {
                    if (x._14 != 0) {
                      if (x._5.equals("调味品") || x._5.equals("食品添加剂") || x._5.equals("食用油、油脂及其制品")) {
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, -x._14)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), -x._14)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, -x._14)

                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "辅料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, x._6)
                      } else {
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, -x._14)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), -x._14)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, -x._14)

                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7, x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null"), x._6)
                        jedis.hincrByFloat(x._10 + "_" + "Material2supplier", "主料" + "_" + "name" + "_" + x._3 + "_" + "typename" + "_" + x._5 + "_" + "amount" + "_" + x._7+"_"+"area"+"_"+data._2.value.getOrElse(x._13,"null")+"_"+"schoolid"+"_"+x._13, x._6)
                      }
                    } else {
                      logger.info("这个原料是不需要的不需要在综合信息中更新进行计算，数量没有进行更新")
                    }
                  }
                }else{
                  logger.info("这个原料是不需要的不需要在综合信息中更新进行计算，之前的redis中没有")
                }
              }

            } else {
              logger.info("这个原料是不需要的2----------------------")
            }
        })
    })
  }

  def Material2SupplierDetailInsert(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) = {
    val ledegerMaster = data._1.filter(x => x != null && x.table.equals("t_pro_ledger_master") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val ledeger = data._1.filter(x => x != null && x.table.equals("t_pro_ledger") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val images = data._1.filter(x => x != null && x.table.equals("t_pro_images") && x.`type`.equals("insert") && x.data.stat.equals("1"))

    val now = format.format(new Date())
    val ledgerMasterData: RDD[(String, List[String])] = ledegerMaster.distinct().map({
      x =>
        //id,批次号，配送类型，配送状态，stat，旧的配送状态，oldstat
        val action_date = x.data.action_date
        val date = format.format(format.parse(action_date))

        var delivery_date ="null"
        if(StringUtils.isNoneEmpty(x.data.delivery_date) && !x.data.delivery_date.equals("null")){
          delivery_date=x.data.delivery_date
        }else{
          delivery_date
        }
        if(format.parse(date).getTime >= format.parse(now).getTime){
          (x.data.id, List(x.data.ware_batch_no, x.data.ledger_type, x.data.haul_status, x.data.stat, date, delivery_date))
        }else{
          ("null", List("null","null", "null", "null", "null","null"))
        }
    }).filter(x => !x._1.equals("null"))

    val imageData: RDD[(String, List[String])] = images.distinct().map({
      x =>
        val relation_id = x.data.relation_id
        val types = x.data.`type`.toString
        val image = x.data.image
        val logo = x.data.logo
        (relation_id, List(types, image, logo))
    })

    val ledgerData = ledeger.distinct().map({
      x =>
        val master_id = x.data.master_id
        val use_date = x.data.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val name = x.data.name //标准名
      val supplier_material_name = x.data.supplier_material_name //物料名称
      val wares_type_name = x.data.wares_type_name //分类
      val actual_quantity = x.data.actual_quantity.toString //实际用量
      val amount_unit = x.data.amount_unit //单位
      var huansuan = "-"
        if (StringUtils.isNoneEmpty(x.data.first_num) && StringUtils.isNoneEmpty(x.data.second_num)) {
          huansuan = x.data.first_num + x.data.supplier_material_units + "=" + x.data.second_num + x.data.amount_unit
        } else {
          huansuan
        }
        val other_quantity = x.data.other_quantity.toString //换算后数量
      val supplier_material_units = x.data.supplier_material_units //换算后单位
      val production_date = x.data.production_date //生产日期
      val shelf_life = x.data.shelf_life //保质期
      val supply_name = x.data.supply_name //供应商名称
      val delivery_number = x.data.delivery_number //验收数量
      val supplier_name = x.data.supplier_name //团餐公司名称
      val receiver_id = x.data.receiver_id //学校id
      var batch_no="null"
        if(StringUtils.isNoneEmpty(x.data.batch_no) && !x.data.batch_no.equals("null")){
         batch_no = x.data.batch_no //生产批号
        }else{
          batch_no
        }

      val id = x.data.id

        if (format.parse(date).getTime >= format.parse(now).getTime) {
          (master_id, List(date, name, supplier_material_name, wares_type_name, actual_quantity, amount_unit, huansuan, other_quantity, supplier_material_units, production_date, shelf_life, supply_name, delivery_number, supplier_name, receiver_id, id, batch_no))
        } else {
          ("null", List("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"))
        }

    }).filter(x => !x._1.equals("null"))

    //id,ledgerMasterData,ledgerData,imageData
    val value: RDD[(String, List[String], List[String], List[String])] = ledgerMasterData.leftOuterJoin(ledgerData).leftOuterJoin(imageData).map(x => (x._1, x._2._1._1, x._2._1._2.getOrElse(List("null")), x._2._2.getOrElse(List("null"))))

    value.filter(x => !x._3(0).equals("null")).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            if (x._4(0).equals("null")) {
              jedis.hset(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15), "batchno" + ";" + x._2(0) + ";" + "schoolid" + ";" + x._3(14) + ";" + "area" + ";" + data._2.value.getOrElse(x._3(14), "null") + ";" + "ledgertype" + ";" + x._2(1) + ";" + "suppliername" + ";" + x._3(13) + ";" + "suppliermaterialname" + ";" + x._3(2) + ";" + "name" + ";" + x._3(1) + ";" + "warestypename" + ";" + x._3(3) + ";" + "actualquantity" + ";" + x._3(4) + ";" + x._3(5) + ";" + "huansuan" + ";" + x._3(6) + ";" + "otherquantity" + ";" + x._3(7) + ";" + x._3(8)+";" + "nob" + ";" + x._3(16) + ";" + "productiondate" + ";" + x._3(9) + ";" + "shelflife" + ";" + x._3(10) + ";" + "supplyname" + ";" + x._3(11) + ";" + "status" + ";" + x._2(2) + ";" + "deliverynumber" + ";" + x._3(12) + ";" + "peiimage" + ";" + "null" + ";" + "peilogo" + ";" + "null" + ";" + "jianimage" + ";" + "null" + ";" + "jianlogo" + ";" + "null" + ";" + "deliverydate" + ";" + x._2(5))
            } else {
              val detail = jedis.hget(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15))
              if (StringUtils.isNoneEmpty(detail) && !detail.equals("null")) {
                if ("1".equals(x._4(0))) {
                  jedis.hset(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15), "batchno" + ";" + x._2(0) + ";" + "schoolid" + ";" + x._3(14) + ";" + "area" + ";" + data._2.value.getOrElse(x._3(14), "null") + ";" + "ledgertype" + ";" + x._2(1) + ";" + "suppliername" + ";" + x._3(13) + ";" + "suppliermaterialname" + ";" + x._3(2) + ";" + "name" + ";" + x._3(1) + ";" + "warestypename" + ";" + x._3(3) + ";" + "actualquantity" + ";" + x._3(4) + ";" + x._3(5) + ";" + "huansuan" + ";" + x._3(6) + ";" + "otherquantity" + ";" + x._3(7) + ";" + x._3(8)+";" + "nob" + ";" + x._3(16) + ";" + "productiondate" + ";" + x._3(9) + ";" + "shelflife" + ";" + x._3(10) + ";" + "supplyname" + ";" + x._3(11) + ";" + "status" + ";" + x._2(2) + ";" + "deliverynumber" + ";" + x._3(12) + ";" + "peiimage" + ";" + x._4(1) + ";" + "peilogo" + ";" +  x._4(2) + ";" + "jianimage" + detail.split("jianimage")(1).split("deliverydate")(0)+ "deliverydate" + ";" + x._2(5))
                }else if("2".equals(x._4(0))){
                  jedis.hset(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15), "batchno" + ";" + x._2(0) + ";" + "schoolid" + ";" + x._3(14) + ";" + "area" + ";" + data._2.value.getOrElse(x._3(14), "null") + ";" + "ledgertype" + ";" + x._2(1) + ";" + "suppliername" + ";" + x._3(13) + ";" + "suppliermaterialname" + ";" + x._3(2) + ";" + "name" + ";" + x._3(1) + ";" + "warestypename" + ";" + x._3(3) + ";" + "actualquantity" + ";" + x._3(4) + ";" + x._3(5) + ";" + "huansuan" + ";" + x._3(6) + ";" + "otherquantity" + ";" + x._3(7) + ";" + x._3(8)+";" + "nob" + ";" + x._3(16) + ";" + "productiondate" + ";" + x._3(9) + ";" + "shelflife" + ";" + x._3(10) + ";" + "supplyname" + ";" + x._3(11) + ";" + "status" + ";" + x._2(2) + ";" + "deliverynumber" + ";" + x._3(12) + ";" + "peiimage" +detail.split("peiimage")(1).split("jianimage")(0)+"jianimage" + ";" + x._4(1) + ";" + "jianlogo" + ";" + x._4(2) + ";" + "deliverydate" + ";" + x._2(5))
                }else{
                  logger.info("综合分析中原料详情中不符合计算的图片类型信息----------------------")
                }
              }else{
                if ("1".equals(x._4(0))) {
                  jedis.hset(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15), "batchno" + ";" + x._2(0) + ";" + "schoolid" + ";" + x._3(14) + ";" + "area" + ";" + data._2.value.getOrElse(x._3(14), "null") + ";" + "ledgertype" + ";" + x._2(1) + ";" + "suppliername" + ";" + x._3(13) + ";" + "suppliermaterialname" + ";" + x._3(2) + ";" + "name" + ";" + x._3(1) + ";" + "warestypename" + ";" + x._3(3) + ";" + "actualquantity" + ";" + x._3(4) + ";" + x._3(5) + ";" + "huansuan" + ";" + x._3(6) + ";" + "otherquantity" + ";" + x._3(7) + ";" + x._3(8)+";" + "nob" + ";" + x._3(16) + ";" + "productiondate" + ";" + x._3(9) + ";" + "shelflife" + ";" + x._3(10) + ";" + "supplyname" + ";" + x._3(11) + ";" + "status" + ";" + x._2(2) + ";" + "deliverynumber" + ";" + x._3(12) + ";" + "peiimage" + ";" + x._4(1) + ";" + "peilogo" + ";" + x._4(2) + ";" + "jianimage" + ";" + "null" + ";" + "jianlogo" + ";" + "null" + ";" + "deliverydate" + ";" + x._2(5))
                }else if("2".equals(x._4(0))){
                  jedis.hset(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15), "batchno" + ";" + x._2(0) + ";" + "schoolid" + ";" + x._3(14) + ";" + "area" + ";" + data._2.value.getOrElse(x._3(14), "null") + ";" + "ledgertype" + ";" + x._2(1) + ";" + "suppliername" + ";" + x._3(13) + ";" + "suppliermaterialname" + ";" + x._3(2) + ";" + "name" + ";" + x._3(1) + ";" + "warestypename" + ";" + x._3(3) + ";" + "actualquantity" + ";" + x._3(4) + ";" + x._3(5) + ";" + "huansuan" + ";" + x._3(6) + ";" + "otherquantity" + ";" + x._3(7) + ";" + x._3(8)+";" + "nob" + ";" + x._3(16) + ";" + "productiondate" + ";" + x._3(9) + ";" + "shelflife" + ";" + x._3(10) + ";" + "supplyname" + ";" + x._3(11) + ";" + "status" + ";" + x._2(2) + ";" + "deliverynumber" + ";" + x._3(12) + ";" + "peiimage" + ";" + "null" + ";" + "peilogo" + ";" + "null" + ";" + "jianimage" + ";" + x._4(1) + ";" + "jianlogo" + ";" + x._4(2) + ";" + "deliverydate" + ";" + x._2(5))
                }else{
                  logger.info("综合分析中原料详情中不符合计算的图片类型信息2222222222----------------------")
                }
              }
            }

        })
    })

  }

  def Material2SupplierDetailDelete(data: RDD[SchoolBean]) = {
    val ledegerMaster = data.filter(x => x != null && x.table.equals("t_pro_ledger_master") && x.`type`.equals("delete"))
    val ledeger = data.filter(x => x != null && x.table.equals("t_pro_ledger") && x.`type`.equals("delete"))
    val images = data.filter(x => x != null && x.table.equals("t_pro_images") && x.`type`.equals("delete"))

    val now = format.format(new Date())

    val ledgerMasterData: RDD[(String, List[String])] = ledegerMaster.distinct().map({
      x =>
        //id,批次号，配送类型，配送状态，stat，旧的配送状态，oldstat
        val action_date = x.data.action_date
        val date = format.format(format.parse(action_date))
        var delivery_date ="null"
        if(StringUtils.isNoneEmpty(x.data.delivery_date) && !x.data.delivery_date.equals("null")){
          delivery_date=x.data.delivery_date
        }else{
          delivery_date
        }

        if(format.parse(date).getTime >= format.parse(now).getTime){
          (x.data.id, List(x.data.ware_batch_no, x.data.ledger_type, x.data.haul_status, x.data.stat, date, delivery_date))
        }else{
          ("null", List("null","null", "null", "null", "null","null"))
        }

    }).filter(x => !x._1.equals("null"))

    val imageData: RDD[(String, List[String])] = images.distinct().map({
      x =>
        val relation_id = x.data.relation_id
        val types = x.data.`type`.toString
        val image = x.data.image
        val logo = x.data.logo
        (relation_id, List(types, image, logo))
    })

    val ledgerData = ledeger.distinct().map({
      x =>
        val master_id = x.data.master_id
        val use_date = x.data.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val name = x.data.name //标准名
      val supplier_material_name = x.data.supplier_material_name //物料名称
      val wares_type_name = x.data.wares_type_name //分类
      val actual_quantity = x.data.actual_quantity.toString //实际用量
      val amount_unit = x.data.amount_unit //单位
      var huansuan = "-"
        if (StringUtils.isNoneEmpty(x.data.first_num) && StringUtils.isNoneEmpty(x.data.second_num)) {
          huansuan = x.data.first_num + x.data.supplier_material_units + "=" + x.data.second_num + x.data.amount_unit
        } else {
          huansuan
        }
        val other_quantity = x.data.other_quantity.toString //换算后数量
      val supplier_material_units = x.data.supplier_material_units //换算后单位
      val production_date = x.data.production_date //生产日期
      val shelf_life = x.data.shelf_life //保质期
      val supply_name = x.data.supply_name //供应商名称
      val delivery_number = x.data.delivery_number //验收数量
      val supplier_name = x.data.supplier_name //团餐公司名称
      val receiver_id = x.data.receiver_id //学校id
      var batch_no="null"
        if(StringUtils.isNoneEmpty(x.data.batch_no) && !x.data.batch_no.equals("null")){
          batch_no = x.data.batch_no //生产批号
        }else{
          batch_no
        }
      val id = x.data.id
        if (format.parse(date).getTime >= format.parse(now).getTime) {
          (master_id, List(date, name, supplier_material_name, wares_type_name, actual_quantity, amount_unit, huansuan, other_quantity, supplier_material_units, production_date, shelf_life, supply_name, delivery_number, supplier_name, receiver_id, id, batch_no))
        } else {
          ("null", List("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null"))
        }

    }).filter(x => !x._1.equals("null"))

    //id,ledgerMasterData,ledgerData,imageData
    val value: RDD[(String, List[String], List[String], List[String])] = ledgerMasterData.leftOuterJoin(ledgerData).leftOuterJoin(imageData).map(x => (x._1, x._2._1._1, x._2._1._2.getOrElse(List("null")), x._2._2.getOrElse(List("null"))))

    value.filter(x => !x._3(0).equals("null")).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hdel(x._3(0) + "_" + "Material2SupllierDetail", x._1 + "_" + x._3(15))
        })
    })
  }


  def LedgerMasterUpdate(data: RDD[SchoolBean]) ={
    val ledgerMaster = data.filter(x => x != null && x.table.equals("t_pro_ledger_master") && x.`type`.equals("update"))
    val images = data.filter(x => x != null && x.table.equals("t_pro_images") && !x.`type`.equals("delete"))

    val now = format.format(new Date())

    val ledgerMasterData: RDD[(String, List[String])] = ledgerMaster.distinct().map({
      x =>
        //id,批次号，配送类型，配送状态，stat，旧的配送状态，oldstat
        val action_date = x.data.action_date
        val date = format.format(format.parse(action_date))
        var delivery_date ="null"
        if(StringUtils.isNoneEmpty(x.data.delivery_date) && !x.data.delivery_date.equals("null")){
          delivery_date=x.data.delivery_date
        }else{
          delivery_date
        }
        if(format.parse(date).getTime >= format.parse(now).getTime){
          (x.data.id, List(x.data.ware_batch_no, x.data.ledger_type, x.data.haul_status, x.data.stat, date, delivery_date,x.old.stat,x.old.haul_status))
        }else{
          ("null", List("null","null", "null", "null", "null","null","null","null"))
        }

    }).filter(x => !x._1.equals("null"))

    val imageData: RDD[(String, List[String])] = images.distinct().map({
      x =>
        val relation_id = x.data.relation_id
        val types = x.data.`type`.toString
        val image = x.data.image
        val logo = x.data.logo
        (relation_id, List(types, image, logo))
    })

    ledgerMasterData.leftOuterJoin(imageData).map(x =>(x._1,x._2._1,x._2._2.getOrElse(List("null")))).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val strings = jedis.hkeys(x._2(4) + "_" + "Material2SupllierDetail")
            for (i <- strings.asScala){
              val id = i.split("_")(0)
              if(id.equals(x._1)){
                val detail = jedis.hget(x._2(4) + "_" + "Material2SupllierDetail", i)
                if(x._3(0).equals("null")){
                  if(StringUtils.isNoneEmpty(x._2(6)) && !x._2(6).equals("null")){
                    if(x._2(3).equals("0") && x._2(6).equals("1")){
                      jedis.hdel(x._2(4) + "_" + "Material2SupllierDetail",i)
                    }else{
                      logger.info("配送主表更新是数据有效状态不为空的更新")
                    }
                  }else{
                    jedis.hset(x._2(4) + "_" + "Material2SupllierDetail", i,"batchno"+";"+x._2(0)+";"+"schoolid"+detail.split("schoolid")(1).split("ledgertype")(0)+"ledgertype"+";"+x._2(1)+";"+"suppliername"+detail.split("suppliername")(1).split("status")(0)+"status"+";"+x._2(2)+";"+"deliverynumber"+detail.split("deliverynumber")(1).split("deliverydate")(0)+"deliverydate"+";"+x._2(5))
                  }
                }else{
                  if ("1".equals(x._3(0))) {
                    jedis.hset(x._2(4) + "_" + "Material2SupllierDetail", i, "batchno"+";"+x._2(0)+";"+"schoolid"+detail.split("schoolid")(1).split("ledgertype")(0)+"ledgertype"+";"+x._2(1)+";"+"suppliername"+detail.split("suppliername")(1).split("status")(0)+"status"+";"+x._2(2)+";"+"deliverynumber"+detail.split("deliverynumber")(1).split("peiimage")(0)+"peiimage"+";"+x._3(1)+";"+"peilogo"+";"+x._3(2)+";"+"jianimage"+detail.split("jianimage")(1).split("deliverydate")(0)+"deliverydate"+";"+x._2(5))
                  }else if("2".equals(x._3(0))){
                    jedis.hset(x._2(4) + "_" + "Material2SupllierDetail", i, "batchno"+";"+x._2(0)+";"+"schoolid"+detail.split("schoolid")(1).split("ledgertype")(0)+"ledgertype"+";"+x._2(1)+";"+"suppliername"+detail.split("suppliername")(1).split("status")(0)+"status"+";"+x._2(2)+";"+"deliverynumber"+detail.split("deliverynumber")(1).split("jianimage")(0)+"jianimage"+";"+x._3(1)+";"+"jianlogo"+";"+x._3(2)+";"+"deliverydate"+";"+x._2(5))
                  }else{
                    logger.info("综合分析中原料详情中不符合计算的图片类型信息----------------------")
                  }
                }
              }else{
                logger.info("综合分析中配送详情主表的更新与redis中没有相同的key")
              }
            }

        })
    })

  }

  def LedgerUpdate(data: (RDD[SchoolBean], Broadcast[Map[String, String]])) ={

    val ledeger = data._1.filter(x => x != null && x.table.equals("t_pro_ledger") && x.`type`.equals("update"))

    val now = format.format(new Date())
    val ledgerData = ledeger.distinct().map({
      x =>
        val master_id = x.data.master_id
        val use_date = x.data.use_date
        val replaceAll = use_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        val name = x.data.name //标准名
      val supplier_material_name = x.data.supplier_material_name //物料名称
      val wares_type_name = x.data.wares_type_name //分类
      val actual_quantity = x.data.actual_quantity.toString //实际用量
      val amount_unit = x.data.amount_unit //单位
      var huansuan = "-"
        if (StringUtils.isNoneEmpty(x.data.first_num) && StringUtils.isNoneEmpty(x.data.second_num)) {
          huansuan = x.data.first_num + x.data.supplier_material_units + "=" + x.data.second_num + x.data.amount_unit
        } else {
          huansuan
        }
        val other_quantity = x.data.other_quantity.toString //换算后数量
      val supplier_material_units = x.data.supplier_material_units //换算后单位
      val production_date = x.data.production_date //生产日期
      val shelf_life = x.data.shelf_life //保质期
      val supply_name = x.data.supply_name //供应商名称
      val delivery_number = x.data.delivery_number //验收数量
      val supplier_name = x.data.supplier_name //团餐公司名称
      val receiver_id = x.data.receiver_id //学校id
      var batch_no="null"
        if(StringUtils.isNoneEmpty(x.data.batch_no) && !x.data.batch_no.equals("null")){
          batch_no = x.data.batch_no //生产批号
        }else{
          batch_no
        }
      val id = x.data.id

        if (format.parse(date).getTime >= format.parse(now).getTime) {
          (master_id, date, name, supplier_material_name, wares_type_name, actual_quantity, amount_unit, huansuan, other_quantity, supplier_material_units, production_date, shelf_life, supply_name, delivery_number, supplier_name, receiver_id, id, batch_no,x.old.stat)
        } else {
          ("null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null", "null")
        }

    }).filter(x => !x._1.equals("null"))

    ledgerData.foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            val strings = jedis.hkeys(x._2 + "_" + "Material2SupllierDetail")
            for (i <- strings.asScala){
              val id = i.split("_")(1)
              if(id.equals(x._17)){
                val detail = jedis.hget(x._2 + "_" + "Material2SupllierDetail", i)
                if(StringUtils.isNoneEmpty(x._19) && !x._19.equals("null")){
                  if(x._17.equals("0") && x._19.equals("1")){
                    jedis.hdel(x._2(4) + "_" + "Material2SupllierDetail",i)
                  }else{
                    logger.info("配送主表更新是数据有效状态不为空的更新")
                  }
                }else{
                  jedis.hset(x._2 + "_" + "Material2SupllierDetail", i,detail.split("schoolid")(0)+"schoolid"+";"+x._16+ ";" + "area" + ";" + data._2.value.getOrElse(x._16, "null") + ";" +"ledgertype"+ detail.split("ledgertype")(1).split("suppliername")(0)+ "suppliername" + ";" + x._15 + ";" + "suppliermaterialname" + ";" + x._4 + ";" + "name" + ";" + x._3 + ";" + "warestypename" + ";" + x._5 + ";" + "actualquantity" + ";" + x._6 + ";" + x._7 + ";" + "huansuan" + ";" + x._8 + ";" + "otherquantity" + ";" + x._9 + ";" + x._10 +";"+ "nob" + ";" + x._18 + ";" + "productiondate" + ";" + x._11 + ";" + "shelflife" + ";" + x._12 + ";" + "supplyname" + ";" + x._13 + ";"+"status" + detail.split("status")(1).split("deliverynumber")(0) + "deliverynumber" + ";" + x._14 + ";" + "peiimage" + detail.split("peiimage")(1))
                }
              }else{
                logger.info("综合分析的配送表的更新数据在之前的redis里没有")
              }
            }
        })
    })
  }


}
