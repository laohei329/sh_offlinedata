package com.ssic.report

import java.lang
import java.util.Date

import com.alibaba.fastjson.JSON
import com.ssic.beans.{SaasPackage, SaasPackageDishes, SaasPackageDishesWare, SchoolBean}
import com.ssic.utils.JPools
import org.apache.commons.lang3.StringUtils
import org.apache.commons.lang3.time.FastDateFormat
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory

/**
  * Created by 云 on 2018/8/7.
  * 原料计划指标
  */

object Material {
  private val logger = LoggerFactory.getLogger(this.getClass)
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")

  def MaterialSum(filterData: RDD[SchoolBean]) = {
    val materialData = filterData.filter(x => x != null && x.table.equals("t_saas_package_dishes_ware") && x.`type`.equals("insert") )
      .map(x => JSON.parseObject(x.data,classOf[SaasPackageDishesWare]))
      .filter(x => !"0".equals(x.stat))
    val packages = filterData.filter(x => x != null && x.table.equals("t_saas_package") && x.`type`.equals("insert"))
      .map(x => JSON.parseObject(x.data,classOf[SaasPackage]))
      .filter(x => !"0".equals(x.stat) && x.is_publish != 0)
    val dishData = filterData.filter(x => x != null && x.table.equals("t_saas_package_dishes") && x.`type`.equals("insert") )
      .map(x => JSON.parseObject(x.data,classOf[SaasPackageDishes]))
      .filter(x => !"0".equals(x.stat) )

    val package_Dish_Ware = materialData.distinct().map(x => (x.package_dishes_id,x.material_name))
    val packageData = packages.distinct().filter(x => StringUtils.isNoneEmpty(x.supply_date)).map({
      x =>
        val id = x.id
        val supply_date = x.supply_date
        val replaceAll = supply_date.replaceAll("\\D", "-")
        val date = format.format(format.parse(replaceAll))
        (id, date)
    })
    val package_Dish = dishData.distinct().map(x => (x.package_id,x.id))

    packageData.leftOuterJoin(package_Dish).map(x => (x._2._2.getOrElse("null"),x._2._1)).leftOuterJoin(package_Dish_Ware).map(x => (x._2._1,x._2._2.getOrElse("null"))).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          x =>
            jedis.hincrBy(x._1+"_"+"material",x._2,1)
            if(jedis.exists(x._1+"_"+"material").equals(true)){
              val total: lang.Long = jedis.hlen(x._1+"_"+"material")
              jedis.hset(x._1+"_"+"material-total","materialtotal",total.toString)
            }else{
              logger.info("不符合原料信息计算。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。")
            }
        })
    })
  }

}
