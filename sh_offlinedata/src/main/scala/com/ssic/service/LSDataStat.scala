package com.ssic.service

import com.ssic.impl.LSDataFunc
import com.ssic.utils.JPools
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.hive.HiveContext

class LSDataStat extends LSDataFunc {

  /**

    * * 将原料排名数据放入到redis中

    * * @param hiveContext HiveContext

    * * @param date 时间

    * * @param year 年

    * * @param month 月

    * * @param materialData 已存在的原料排名表数据

    */
  override def materialtotaltoredis(hiveContext:HiveContext,date:String,year:String,month:String,materialData:RDD[(String, String)]): Unit = {
    hiveContext.sql(s"select area,name,sum(actual_quantity) as actual_quantity,wares_type_name from app_saas_v1.app_t_edu_ledege_total where year='${year}' and month='${month}' and use_date='${date}' group by area,name,wares_type_name").rdd.map({
      row =>
        val area = row.getAs[String]("area")
        val name = row.getAs[String]("name")
        val actual_quantity = row.getAs[java.math.BigDecimal]("actual_quantity")
        val wares_type_name = row.getAs[String]("wares_type_name")
        ("area" + "_" + area + "_" + "name" + "_" + name + "_" + "warestype" + "_" + wares_type_name, actual_quantity)
    }).cogroup(materialData)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date.split(" ")(0) + "_material_new", k, "0")
                jedis.expire(date.split(" ")(0) + "_material_new",345600)
              } else {
                jedis.hset(date.split(" ")(0) + "_material_new", k, v._1.head.toString)
                jedis.expire(date.split(" ")(0) + "_material_new",345600)
              }

          })
      })
  }

  /**

    * * 将菜品排名数据放入到redis中

    * * @param hiveContext HiveContext

    * * @param date 时间

    * * @param year 年

    * * @param month 月

    * * @param dishData 已存在的菜品排名数据

    */
  override def dishtotaltoredis(hiveContext:HiveContext,date:String,year:String,month:String,dishData:RDD[(String, String)]): Unit = {

    hiveContext.sql(s"select area,dishes_name,category,sum(dishes_number) as number from app_saas_v1.app_t_edu_dish_total where year='${year}' and month='${month}' and supply_date='${date}' group by area,dishes_name,category").rdd.map({
      row =>
        val area = row.getAs[String]("area")
        val name = row.getAs[String]("dishes_name")
        val category = row.getAs[String]("category")
        val number = row.getAs[Long]("number")

        ("area" + "_" + area + "_" + "name" + "_" + name + "_" + "category" + "_" + category, number)
    }).cogroup(dishData)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date.split(" ")(0) + "_dish_new", k, "0")
                jedis.expire(date.split(" ")(0) + "_dish_new",345600)
              } else {
                jedis.hset(date.split(" ")(0) + "_dish_new", k, v._1.head.toString)
                jedis.expire(date.split(" ")(0) + "_dish_new",345600)
              }

          })
      })
  }
  /**

    * * 将供应商供应学校排名的统计数据放入到redis中

    * * @param hiveContext HiveContext

    * * @param date 时间

    * * @param year 年

    * * @param month 月

    * * @param supplierToSchoolData 已存在的供应商供应学校排名的统计数据

    */
  override def supplynametotaltoredis(hiveContext:HiveContext,date:String,year:String,month:String,supplierToSchoolData:RDD[(String, String)]): Unit = {

    hiveContext.sql(s"select count(*) as total,a.supply_name,a.area from (select distinct(school_id),school_name,supply_name,area from app_saas_v1.app_t_edu_ledege_detail where year ='${year}' and month ='${month}' and use_date='${date}' and school_name is not null and school_id is not null)as a group by a.supply_name,a.area").rdd.map({
      row =>
        val area = row.getAs[String]("area")
        val name = row.getAs[String]("supply_name")
        val total = row.getAs[Long]("total")

        ("area" + "_" + area + "_" + name, total)

    }).cogroup(supplierToSchoolData)
      .foreachPartition({
        itr =>
          val jedis = JPools.getJedis
          itr.foreach({
            case (k, v) =>
              //表示左边没有，右边有
              if (v._1.size == 0) {
                jedis.hset(date.split(" ")(0) + "_supplierToSchool_new", k, "0")
                jedis.expire(date.split(" ")(0) + "_supplierToSchool_new",345600)
              } else {
                jedis.hset(date.split(" ")(0) + "_supplierToSchool_new", k, v._1.head.toString)
                jedis.expire(date.split(" ")(0) + "_supplierToSchool_new",345600)
              }

          })
      })
  }
}
