package com.ssic.service

import com.ssic.impl.WarnHiveDataFunc
import org.apache.spark.sql.hive.HiveContext

class WarnHiveDataStat extends WarnHiveDataFunc{

  override def areawarntotal(data: HiveContext): Unit = {

    data.sql(
      s"""
         |insert overwrite table app_saas_v1.app_t_edu_warn_total partition(year,month) select a.warn_date as warn_date,
         |a.district_id as district_id,
         |a.target as target,
         |a.lic_type as lic_type,
         |a.untreated_sum as untreated_sum,
         |a.review_sum as review_sum,
         |a.rejected_sum as rejected_sum,
         |a.deal_sum deal_sum,
         |a.nodeal_sum as nodeal_sum,
         |a.year as year,
         |a.month as month
         |from
         |(select sum(untreated_sum) as untreated_sum,sum(review_sum) as review_sum,sum(rejected_sum) as rejected_sum,sum(deal_sum) as deal_sum,sum(nodeal_sum) as nodeal_sum,warn_date,district_id,target,lic_type,year,month from saas_v1_dw.dw_t_edu_warn_total where year='2019' and month > (month(date_sub(current_date ,1)) -6)  group by district_id,warn_date,target,lic_type,year,month) as a
       """.stripMargin)
  }

  override def areanaturewarntotal(data: HiveContext): Unit = {
    data.sql(
      s"""
         |insert overwrite table app_saas_v1.app_t_edu_warn_nature_total partition(year,month) select b.school_nature_name as nature,
         |b.warn_date as use_date,
         |b.target as target,
         |b.district_id as district_id,
         |c.total as warn_dis_nodeal_sum,
         |a.total as warn_nodeal_sum,
         |b.total as warn_sum,
         |b.year as year,
         |b.month as month
         |from
         |(select count(*) as total,district_id,warn_date,target,year,month,school_nature_name
         |from
         |app_saas_v1.app_t_edu_warn_detail
         |where
         |year='2019' and month > (month(date_sub(current_date ,1)) -6) and warn_stat != 4 and warn_type=1
         |and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by
         |district_id,warn_date,target,year,month,school_nature_name) as a
         |right outer join
         |(select count(*) as total,district_id,warn_date,target,year,month,school_nature_name
         |from
         |app_saas_v1.app_t_edu_warn_detail
         |where
         |year='2019' and month > (month(date_sub(current_date ,1)) -6) and warn_type=1
         |and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by
         |district_id,warn_date,target,year,month,school_nature_name) as b
         |on b.warn_date=a.warn_date and b.target=a.target and b.school_nature_name=a.school_nature_name and b.district_id=a.district_id
         |left outer join
         |(select count(distinct(supplier_id)) as total,district_id,warn_date,target,year,month,school_nature_name
         |from
         |app_saas_v1.app_t_edu_warn_detail
         |where
         |year='2019' and month > (month(date_sub(current_date ,1)) -6) and warn_stat != 4 and warn_type=1
         |and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by
         |district_id,warn_date,target,year,month,school_nature_name) as c
         |on b.warn_date=c.warn_date and b.target=c.target and b.school_nature_name =c.school_nature_name and b.district_id=c.district_id
       """.stripMargin)
  }

  override def arealevelwarntotal(data: HiveContext): Unit = {

    data.sql(
      s"""
         |insert overwrite table app_saas_v1.app_t_edu_warn_level_total partition(year,month) select b.level_name as level,
         |b.warn_date as use_date,
         |b.target as target,
         |b.district_id as district_id,
         |c.total as warn_dis_nodeal_sum,
         |a.total as warn_nodeal_sum,
         |b.total as warn_sum,
         |b.year as year,
         |b.month as month
         |from
         |(select count(*) as total,district_id,warn_date,target,year,month,level_name
         |from
         |app_saas_v1.app_t_edu_warn_detail
         |where
         |year='2019' and month > (month(date_sub(current_date ,1)) -6) and warn_stat != 4 and warn_type=1
         |and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by
         |district_id,warn_date,target,year,month,level_name) as a
         |right outer join
         |(select count(*) as total,district_id,warn_date,target,year,month,level_name
         |from
         |app_saas_v1.app_t_edu_warn_detail
         |where
         |year='2019' and month > (month(date_sub(current_date ,1)) -6) and warn_type=1
         |and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by
         |district_id,warn_date,target,year,month,level_name) as b
         |on b.warn_date=a.warn_date and b.target=a.target and b.level_name=a.level_name and b.district_id=a.district_id
         |left outer join
         |(select count(distinct(supplier_id)) as total,district_id,warn_date,target,year,month,level_name
         |from
         |app_saas_v1.app_t_edu_warn_detail
         |where
         |year='2019' and month > (month(date_sub(current_date ,1)) -6) and warn_stat != 4 and warn_type=1
         |and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by
         |district_id,warn_date,target,year,month,level_name) as c
         |on b.warn_date=c.warn_date and b.target=c.target and b.level_name =c.level_name and b.district_id=c.district_id
       """.stripMargin)
  }
}
