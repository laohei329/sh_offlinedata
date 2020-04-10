package com.ssic.service

import com.ssic.utils.JPools
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD

class EduAllDataStat  {
  /**

    * * 排菜，用料，配送，留样操作情况

    * * @param plaData 排菜数据

    * * @param useMaterialChildData 用料子页面数据

    * * @param distributionchildData 配送子页面数据

    * * @param retentionchildData   留样子页面数据

    * * @param schoolData 学校基础数据

    * * @param eduAllData 已存在的排菜，用料，配送，留样操作数据

    * * @param date 时间

    * * @param commiteeid2commiteeid 教属id信息

    * * @param schoolid2suppliername 团餐公司名字

    */
  def platoonmaterialdetailresert(plaData:RDD[(String, String)], useMaterialChildData:RDD[(String, String)],distributionchildData: RDD[(String, String)], retentionchildData:RDD[(String, String)], schoolData:Broadcast[Map[String, List[String]]],eduAllData:RDD[(String, String)],date:String,commiteeid2commiteeid:Broadcast[Map[String, String]],schoolid2suppliername:Broadcast[Map[String, String]]): Unit = {

    val platoon = plaData.map(x => ((x._1.split("_")(1), x._2)))
    val useMaterialData = useMaterialChildData.map(x => ((x._1.split("_")(1), x._2)))
    val distributionData = distributionchildData.map(x => ((x._1.split("id_")(1), x._2)))
    val retentionData = retentionchildData.map(x => ((x._1.split("id_")(1), x._2)))

    platoon.leftOuterJoin(useMaterialData).leftOuterJoin(distributionData).leftOuterJoin(retentionData).map({
      x =>
        //供餐_已排菜_create-time_2019-05-07 10:27:58+"_"+"reason"+"_"+reason+"_"+"plastatus"+"_"+"4"
        val id = x._1
        val platooData = x._2._1._1._1
        var gongcanStatus = "0"
        val reason = platooData.split("_")(5)
        if ("不供餐".equals(platooData.split("_")(0))) {
          gongcanStatus
        } else {
          gongcanStatus = "1"
        }

        var paicaiStatus = "0"
        if ("未排菜".equals(platooData.split("_")(1))) {
          paicaiStatus
        } else {
          paicaiStatus = "1"
        }

        val useMaterial = x._2._1._1._2.getOrElse("null")
        var useMaterialStatus = "1"
        if ("null".equals(useMaterial)) {
          useMaterialStatus
        } else {
          useMaterialStatus = useMaterial.split("status_")(1)
        }

        val distribution = x._2._1._2.getOrElse("null")
        var distributionStatus = "-1"
        if ("null".equals(distribution)) {
          distributionStatus
        } else {
          distributionStatus = distribution.split("_")(9)
        }

        val retention = x._2._2.getOrElse("null")
        var retentionStatus = "0"
        if ("null".equals(retention)) {
          retentionStatus
        } else {
          retentionStatus = retention.split("_")(7)
        }

        val schoolDeail = schoolData.value.getOrElse(id, List("null"))
        var area = "null"
        var department_master_id = "null"
        var department_slave_id = "null"
        var level="null"
        var school_nature_name ="null"
        var school_name ="null"
        var department_id="null"
        if (schoolDeail.size >= 2) {
          area = schoolDeail(7)
          department_master_id = schoolDeail(5)
          level = schoolDeail(0)
          school_nature_name=schoolDeail(1)
          school_name =schoolDeail(8)
          if("3".equals(schoolDeail(5))){
            department_slave_id = commiteeid2commiteeid.value.getOrElse(schoolDeail(6),"null")
          }else{
            department_slave_id = schoolDeail(6)
          }
          department_id = schoolDeail(9)

        } else {
          area
          department_master_id
          department_slave_id
          level
          school_nature_name
          school_name
          department_id
        }

        val supplier_name = schoolid2suppliername.value.getOrElse(id,"null")

        (id, "school_name"+":"+school_name+","+"area"+":"+area+","+"level_name"+":"+level+","+"school_nature_name"+":"+school_nature_name+","+"department_master_id"+":"+department_master_id+","+"department_slave_id_name"+":"+department_slave_id+","+"have_class"+":"+gongcanStatus+","+"have_platoon"+":"+paicaiStatus+","+"material_status"+":"+useMaterialStatus+","+"haul_status"+":"+distributionStatus+","+"have_reserve"+":"+retentionStatus+","+"supplier_name"+":"+supplier_name+","+"reason"+":"+reason+","+"department_id"+":"+department_id)

    }).cogroup(eduAllData).foreachPartition({
      itr =>
        val jedis = JPools.getJedis
        itr.foreach({
          case (k, v) =>{
            //表示左边没有，右边有
            if (v._1.size == 0) {
              jedis.hdel(date + "_allUseData", k)
            } else {
              jedis.hset(date + "_allUseData", k, v._1.head)
              jedis.expire(date + "_allUseData",259200)
            }
          }
        })
    })


  }
}
