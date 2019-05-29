package com.ssic.report

import java.sql.{DriverManager, Statement}

import com.ssic.beans.SchoolBean
import com.ssic.utils.Tools.{conn3, hiveurl}
import org.apache.commons.lang3.time._
import org.apache.spark.rdd.RDD

object WarnHive {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")

  def masterlicenseinsert(data: RDD[SchoolBean]): Unit = {

    val warn = data.filter(x => x != null && x.table.equals("t_pro_warning_global_master") && x.`type`.equals("insert") && x.data.stat.equals("1") && x.data.warn_type.equals("1"))
    val license = data.filter(x => x != null && x.table.equals("t_pro_warning_license") && x.`type`.equals("insert") && x.data.stat.equals("1"))
    val warnDa = warn.distinct().map({
      x =>
        val id = x.data.id
        val title = x.data.title
        val level_id = x.data.level_id
        val warn_stat = x.data.warn_stat
        //预警状态 预警状态:1待处理,2处理中,3驳回,4已处理'
        val warn_type = x.data.warn_type //预警类型,1:证照预警,2:配送警示,3过保警示,4验收预警'
      val warn_type_child = x.data.warn_type_child
        //证件类别
        val supplier_id = x.data.supplier_id
        val creator = x.data.creator
        val create_time = x.data.create_time
        val updater = x.data.updater
        val last_update_time = x.data.last_update_time
        val stat = x.data.stat
        val warn_rule_id = x.data.warn_rule_id

        (id, List(title, level_id, warn_stat, warn_type, warn_type_child, supplier_id, creator, create_time, updater, last_update_time, stat, warn_rule_id))


    }).filter(x => x._2(3).equals("1"))

    val licenseData = license.distinct().map({
      x =>
        val id = x.data.id
        val warn_master_id = x.data.warn_master_id
        val remain_time = x.data.remain_time //剩余多少天，0的时候是逾期
      val lose_time = x.data.lose_time //到期时间
      val lic_no = x.data.lic_no //证件号码
      val license = x.data.license //证件名称
      val written_name = x.data.written_name //证书上人的名字
      val stat = x.data.stat
        (warn_master_id, List(id, remain_time, lose_time, lic_no, license, written_name, stat))
    })

    warnDa.leftOuterJoin(licenseData).map(x => (x._1, (x._2._1, x._2._2.getOrElse(List("null"))))).filter(x => !x._2._2(0).equals("null"))
      .foreachPartition({
        itr =>
          val con = DriverManager.getConnection(hiveurl, conn3)
          val hiveStatement: Statement = con.createStatement()
          itr.foreach({
            x =>

              val year = format1.format(format.parse(x._2._1(7)))
              val month = format2.format(format.parse(x._2._1(7)))
              hiveStatement.execute(
                s"""
                   |insert into table saas_v1_dw.dw_warn_master_license partition(year='$year',month='$month') select(
                   |'${x._1}',
                   |'${x._2._1(0)}',
                   |'${x._2._1(1)}',
                   |'${x._2._1(2)}',
                   |'${x._2._1(3)}',
                   |'${x._2._1(4)}',
                   |'${x._2._1(5)}',
                   |'${x._2._1(6)}',
                   |'${x._2._1(7)}',
                   |'${x._2._1(8)}',
                   |'${x._2._1(9)}',
                   |'${x._2._1(10)}',
                   |'${x._2._1(11)}',
                   |'${x._2._2(0)}',
                   |'${x._2._2(1)}',
                   |'${x._2._2(2)}',
                   |'${x._2._2(3)}',
                   |'${x._2._2(4)}',
                   |'${x._2._2(5)}',
                   |'${x._2._2(6)}')
             """.stripMargin)
          })
          hiveStatement.close()
      })
  }

  def masterupdatedelete(data: RDD[SchoolBean]): Unit = {
    data.filter(x => x != null && x.table.equals("t_pro_warning_global_master") && !x.`type`.equals("insert") && x.data.warn_type.equals("1")).distinct().foreachPartition({
      itr =>
        val con = DriverManager.getConnection(hiveurl, conn3)
        val hiveStatement: Statement = con.createStatement()
        itr.foreach({
          x =>
            val types = x.`type`
            val id = x.data.id
            val title = x.data.title
            val level_id = x.data.level_id
            val warn_stat = x.data.warn_stat
            //预警状态 预警状态:1待处理,2处理中,3驳回,4已处理'
            val warn_type = x.data.warn_type //预警类型,1:证照预警,2:配送警示,3过保警示,4验收预警'
          val warn_type_child = x.data.warn_type_child
            //证件类别
            val supplier_id = x.data.supplier_id
            val creator = x.data.creator
            val create_time = x.data.create_time
            val updater = x.data.updater
            val last_update_time = x.data.last_update_time
            val stat = x.data.stat
            val warn_rule_id = x.data.warn_rule_id

            val year = format1.format(format.parse(create_time))
            val month = format2.format(format.parse(create_time))

            if ("update".equals(types)) {
              hiveStatement.execute(
                s"""
                    |update saas_v1_dw.dw_warn_master_license set warn_stat='${warn_stat}' where year='${year}' and month ='${month}' and id ='${id}'
             """.stripMargin)
            } else {
              hiveStatement.execute(s"delete from saas_v1_dw.dw_warn_master_license where year='${year}' and month ='${month}' and id ='${id}'")
            }
        })
        hiveStatement.close()
    })


  }

  def warnrelation(data: RDD[SchoolBean]): Unit = {
    data.filter(x => x != null && x.table.equals("t_pro_warning_global_master")).filter(x => x.data.target == 2 || x.data.target == 3).distinct()
      .foreachPartition({
        itr =>
          val con = DriverManager.getConnection(hiveurl, conn3)
          val hiveStatement: Statement = con.createStatement()
          itr.foreach({
            x =>
              val types = x.`type`
              val id = x.data.id
              val warn_master_id = x.data.warn_master_id
              val schools_id = x.data.schools_id
              val school_name = x.data.school_name
              val proj_id = x.data.proj_id
              val proj_name = x.data.proj_name
              val district_id = x.data.district_id
              val district_name = x.data.district_name
              val city_id = x.data.city_id
              val province_id = x.data.province_id
              val target = x.data.target
              val target_id = x.data.target_id
              val read_stat = x.data.read_stat
              val creator = x.data.creator
              val create_time = x.data.create_time
              val updater = x.data.updater
              val last_update_time = x.data.last_update_time
              val stat = x.data.stat

              val year = format1.format(format.parse(create_time))
              val month = format2.format(format.parse(create_time))

              if ("insert".equals(types)) {
                hiveStatement.execute(
                  s"""
                     |insert into table saas_v1.t_pro_warning_view_relation_tp partition(year='${year}',month='${month}') select(
                     |'${id}',
                     |'${warn_master_id}',
                     |'${schools_id}',
                     |'${school_name}',
                     |'${proj_id}',
                     |'${proj_name}',
                     |'${district_id},
                     |'${district_name}',
                     |'${city_id}',
                     |'${province_id}',
                     |'${target}',
                     |'${target_id}',
                     |'${read_stat}',
                     |'${creator}',
                     |'${create_time}',
                     |'${updater}',
                     |'${last_update_time}',
                     |'${stat}')
             """.stripMargin)
              } else if ("delete".equals(types)) {
                hiveStatement.execute(s"delete from saas_v1.t_pro_warning_view_relation_tp where year='${year}' and month ='${month}' and id ='${id}'")
              }
          })
          hiveStatement.close()
      })


  }

}
