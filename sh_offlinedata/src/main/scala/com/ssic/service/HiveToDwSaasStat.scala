package com.ssic.service

import com.ssic.impl.HiveToDwSaasFunc
import org.apache.commons.lang3.time._
import org.apache.spark.sql.hive.HiveContext


class HiveToDwSaasStat extends HiveToDwSaasFunc {

  override def insertdwimages(data: (HiveContext, String, String)): Unit = {

    data._1.sql(
      s"""
         |insert overwrite table saas_v1_dw.dw_t_pro_images partition(year,month) select c.relation_id as relation_id,
         |c.fs as image,
         |c.year as year,
         |c.month as month
         |from (select a.relation_id as relation_id,collect_set(a.ts)as fs,a.year as year,a.month as month
         |from(select relation_id,concat_ws(":",String(type),image)as ts,year,month
         |from saas_v1.t_pro_images where year='${data._2}' and month ='${data._3}')  as a
         |group by a.relation_id,a.year,a.month) as c
       """.stripMargin)

  }

  override def insertdwmaterialdetail(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
        |insert overwrite table saas_v1_dw.dw_t_edu_material_detail partition(year,month) select a.use_date as use_date,
        |a.type as type,
        |a.master_id as master_id,
        |a.proj_id_2 as proj_id,
        |a.supplier_id as supplier_id,
        |a.status as status,
        |b.school_id as school_id,
        |year(a.use_date) as year,
        |month(a.use_date) as month
        |from
        |(select use_date,type,master_id,proj_id_2,supplier_id,status from saas_v1.t_pro_material_plan_temp lateral view explode(split(proj_id,','))prodid as proj_id_2 where use_date >= '${data._2}')as a
        |left outer join
        |(select uuid,school_id from ods_b2b.erp_groupon_customer group by uuid,school_id) as b
        |on a.proj_id_2 = b.uuid
        |left outer join
        |(select id,school_name from app_saas_v1.t_edu_school_new )as c
        |on b.school_id = c.id
        |where c.school_name is not null
      """.stripMargin)
  }

  override def insertmaterialdish(data: (HiveContext, String,String,String)): Unit = {
    data._1.sql(
      s"""
        |insert overwrite table saas_v1_dw.dw_t_edu_material_dish partition(year,month) select a.id as id,
        |a.supply_date as supply_date,
        |a.school_id as school_id,
        |g.school_name as school_name,
        |g.area as area,
        |g.level_name as level_name,
        |g.address as address,
        |g.corporation as corporation,
        |g.food_safety_persion as food_safety_persion,
        |g.food_safety_mobilephone as food_safety_mobilephone,
        |c.material_id as material_id,
        |c.material_name as material_name,
        |case when a.ledger_type = 1 then e.supply_id else d.supply_id end as supply_id,
        |case when a.ledger_type = 1 then e.supply_name else d.supply_name end as supply_name,
        |b.dishes_name as dishes_name,
        |case when a.ledger_type = 1 then f.ware_batch_no else h.ware_batch_no end as ware_batch_no,
        |a.school_supplier_id as proj_id,
        |a.supplier_id as supplier_id,
        |m.supplier_name as supplier_name,
        |year(a.supply_date) as year,
        |month(a.supply_date) as month
        |from
        |(select id,school_id,supplier_id,supply_date,create_time,school_supplier_id,ledger_type from saas_v1.t_saas_package where industry_type=1 and stat=1 and (year='${data._3}' or year='${data._4}') and supply_date >= '${data._2}' ) as a
        |left outer join
        |(select id,package_id,dishes_id,dishes_name from saas_v1.t_saas_package_dishes where stat=1 and (year='${data._3}' or year='${data._4}')) as b
        |on a.id=b.package_id
        |left outer join
        |(select package_dishes_id,material_id,material_name from saas_v1.t_saas_package_dishes_ware where stat=1 and (year='${data._3}' or year='${data._4}')) as c
        |on b.id = c.package_dishes_id
        |left outer join
        |(select master_id,use_date,material_id,receiver_id,supply_id,supply_name from saas_v1.t_pro_ledger where stat =1 and (year='${data._3}' or year='${data._4}'))as e
        |on a.supply_date = e.use_date and a.school_id=e.receiver_id and c.material_id = e.material_id
        |left outer join
        |(select master_id,use_date,material_id,receiver_id,supply_id,supply_name from saas_v1.t_pro_ledger where stat =1 and (year='${data._3}' or year='${data._4}'))as d
        |on a.supply_date = e.use_date and a.school_id=e.receiver_id and b.dishes_id = d.material_id
        |left outer join
        |(select id,ware_batch_no from saas_v1.t_pro_ledger_master where stat =1 and (year='${data._3}' or year='${data._4}'))as f
        |on e.master_id=f.id
        |left outer join
        |(select id,ware_batch_no from saas_v1.t_pro_ledger_master where stat =1 and (year='${data._3}' or year='${data._4}'))as h
        |on d.master_id=h.id
        |left outer join
        |(select id,school_name,area,address,level_name,corporation,food_safety_persion,food_safety_mobilephone from app_saas_v1.t_edu_school_new ) as g
        |on a.school_id= g.id
        |left outer join
        |(select id ,supplier_name from app_saas_v1.t_pro_supplier where supplier_type =1 ) as m
        |on a.supplier_id=m.id
        |where g.school_name is not null
      """.stripMargin)
  }

  override def dweducalendar(data: (HiveContext, String, String, String, String, String, String)): Unit = {
    data._1.sql(
      s"""
        |insert overwrite table saas_v1_dw.dw_t_edu_calendar partition(year,month) select a.job_exec_date as use_date,
        |a.id as school_id,
        |case when b.have_class = 0 then 0
        |when b.have_class = 1 then 1
        |else
        |case when c.type= 1 then 0
        |when c.type =2 then 1
        |else
        |case when (d.first_start_date is null) then
        |case when ((e.first_start_date <= a.job_exec_date and a.job_exec_date<=e.first_end_date) or (e.second_start_date<=a.job_exec_date and a.job_exec_date<=e.second_end_date)) and (a.type =1 ora.type =2 or a.type=3 or a.type =4 or a.type =5 ) then 1
        |when ((e.first_start_date <= a.job_exec_date and a.job_exec_date<=e.first_end_date) or (e.second_start_date<=a.job_exec_date and a.job_exec_date<=e.second_end_date)) and (a.type =0 or a.type=6) then 0 else 0 end
        |else
        |case when ((d.first_start_date <= a.job_exec_date and a.job_exec_date<=d.first_end_date) or (d.second_start_date<=a.job_exec_date and a.job_exec_date<=d.second_end_date)) and (a.type =1 ora.type =2 or a.type=3 or a.type =4 or a.type =5 ) then 1
        |when ((d.first_start_date <= a.job_exec_date and a.job_exec_date<=d.first_end_date) or (d.second_start_date<=a.job_exec_date and a.job_exec_date<=d.second_end_date)) and (a.type =0 or a.type=6) then 0 else 0 end
        |end
        |end
        |end as have_class,
        |b.reason as reason,
        |year(a.job_exec_date) as year,
        |month(a.job_exec_date) as month
        |from
        |(select * ,pmod(datediff(date_sub(job_exec_date ,0), '1920-01-01') - 3, 7)as type,'${data._2}' as term_year from saas_v1.bi_edu_school where (year='${data._3}') and (month = '${data._3}' or month ='${data._4}' or month='${data._5}' or month='${data._6}') ) as a
        |left outer join
        |(select have_class,school_id,the_day,reason from saas_v1.t_edu_calendar where stat=1) as b
        |on a.id = b.school_id and a.job_exec_date = b.the_day
        |left outer join
        |(select type,holiday_day from saas_v1.t_edu_holiday where stat=1) as c
        |on a.job_exec_date = c.holiday_day
        |left outer join
        |(select id,first_start_date,first_end_date,second_start_date,second_end_date,school_id from saas_v1.t_edu_schoolterm where stat=1 and term_year='${data._2}') as d
        |on a.id = d.school_id
        |left outer join
        |(select * from saas_v1.t_edu_schoolterm_system where stat=1 and term_year='${data._2}') as e
        |on a.term_year = e.term_year
      """.stripMargin)
  }

  override def dwplatoondetail(data: (HiveContext, String, String)): Unit = {
    data._1.sql(
      s"""
        |insert overwrite table saas_v1_dw.dw_t_edu_platoon_detail partition(year,month) select a.supply_date as use_date,
        |a.school_id as school_id,
        |a.have_platoon as have_platoon,
        |a.create_time as platoon_create_time,
        |a.supplier_id as supplier_id,
        |b.status as material_status,
        |year(a.supply_date) as year,
        |month(a.supply_date) as month
        |from
        |(select * from (select school_id, from_unixtime(unix_timestamp(supply_date),'yyyy-MM-dd') as supply_date,school_name,supplier_id,create_time,1 as have_platoon,ROW_NUMBER() over(PARTITION BY school_id,supply_date ORDER BY create_time desc)as rn from saas_v1.t_saas_package where year='${data._3}'  and stat=1 and industry_type=1 and supply_date >= '${data._2}')as platoon_temp where platoon_temp.rn =1) as a
        |left outer join
        |(select * from(select use_date,proj_id,status,school_id,year,month,row_number() over (partition by proj_id,use_date order by status desc)as rn from saas_v1_dw.dw_t_edu_material_detail where year='${data._3}' and use_date>='${data._2}')as material_temp where material_temp.rn =1) as b
        |on a.school_id=b.school_id and a.supply_date=b.use_date
      """.stripMargin)
  }

  override def appeducalendar(data: (HiveContext, String)): Unit = {
    data._1.sql(
     s"""
        |insert overwrite table app_saas_v1.app_t_edu_calendar partition(year,month) select a.use_date as use_date,
        |a.school_id as school_id,
        |a.have_class as have_class,
        |a.reason as reason,
        |a.year as year,
        |a.month as month
        |from
        |(select * from saas_v1_dw.dw_t_edu_calendar where year='${data._2}') as a
      """.stripMargin)
  }

  override def insertschoollicensewwarntotal(data: (HiveContext, String)): Unit = {

    data._1.sql(
      s"""
         |insert overwrite table saas_v1_dw.dw_t_edu_warn_total partition(year,month) select a.warn_date as warn_date,
         |a.district_id as district_id,
         |a.target as target,
         |case when a.company_type=1 then 1 else 2 end as lic_type,
         |case when warn_stat =1 then total end as untreated_sum,
         |case when warn_stat =2 then total end as review_sum,
         |case when warn_stat =3 then total end as rejected_sum,
         |case when warn_stat =4 then total end as deal_sum,
         |0 as nodeal_sum,
         |a.year as year,
         |a.month as month
         |from
         |(select count(*) as total,warn_date, warn_stat,district_id,company_type,target,year,month
         |from app_saas_v1.app_t_edu_warn_detail
         |where year='${data._2}' and warn_type=1 and (warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by warn_stat,district_id,warn_date,company_type,target,year,month) as a
       """.stripMargin)

  }

  override def insertpeoplelicensedwwarntotal(data: (HiveContext, String)): Unit = {

    data._1.sql(
      s"""
         |insert into table saas_v1_dw.dw_t_edu_warn_total partition(year,month) select a.warn_date as warn_date,
         |a.district_id as district_id,
         |a.target as target,
         |3 as lic_type,
         |case when warn_stat =1 then total end as untreated_sum,
         |case when warn_stat =2 then total end as review_sum,
         |case when warn_stat =3 then total end as rejected_sum,
         |case when warn_stat =4 then total end as deal_sum,
         |0 as nodeal_sum,
         |a.year as year,
         |a.month as month
         |from
         |(select count(*) as total,warn_date, warn_stat,district_id,target,year,month from app_saas_v1.app_t_edu_warn_detail
         |where year='${data._2}' and warn_type=1 and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25)
         |group by warn_stat,district_id,warn_date,target,year,month) as a
       """.stripMargin)
  }

  override def insertalllicensedwwarntotal(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert into table saas_v1_dw.dw_t_edu_warn_total partition(year,month) select a.warn_date as warn_date,
         |a.district_id as district_id,
         |a.target as target,
         |4 as lic_type,
         |case when warn_stat =1 then total end as untreated_sum,
         |case when warn_stat =2 then total end as review_sum,
         |case when warn_stat =3 then total end as rejected_sum,
         |case when warn_stat =4 then total end as deal_sum,
         |0 as nodeal_sum,
         |a.year as year,
         |a.month as month
         |from
         |(select count(*) as total,warn_date, warn_stat,district_id,target,year,month
         |from app_saas_v1.app_t_edu_warn_detail
         |where year='${data._2}' and warn_type=1 and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by warn_stat,district_id,warn_date,target,year,month) as a
       """.stripMargin)
  }

  override def insertditinctdwwarntotal(data: (HiveContext, String)): Unit = {
    data._1.sql(
      s"""
         |insert into table dw_t_edu_warn_total partition(year,month) select a.warn_date as warn_date,
         |a.district_id as district_id,
         |a.target as target,
         |4 as lic_type,
         |0 as untreated_sum,
         |0 as review_sum,
         |0 as rejected_sum,
         |0 as deal_sum,
         |a.total as nodeal_sum,
         |a.year as year,
         |a.month as month
         |from
         |(select count(distinct(supplier_id)) as total,district_id,warn_date,target,year,month from app_saas_v1.app_t_edu_warn_detail
         |where year='${data._2}' and warn_stat != 4 and warn_type=1 and (warn_type_child =20 or warn_type_child =22 or warn_type_child=23 or warn_type_child=24 or warn_type_child=25 or warn_type_child =0 or warn_type_child =1 or warn_type_child=4)
         |group by district_id,warn_date,target,year,month) as a
       """.stripMargin)
  }

  override def insertdwcalendar(data:(HiveContext,String)): Unit = {

    data._1.sql(
      s"""
         |insert into table saas_v1_dw.dw_t_edu_calendar partition(year,month) select a.use_date as use_date,
         |a.id as school_id,
         |case when b.have_class = 0 then 0
         |when b.have_class = 1 then 1
         |else
         |case when c.type= 1 then 0
         |when c.type =2 then 1
         |else
         |case when ((d.first_start_date <= a.use_date and a.use_date<=d.first_end_date) or (d.second_start_date<=a.use_date and a.use_date<=d.second_end_date)) and (j.type =1 or j.type =2 or j.type=3 or j.type =4 or j.type =5 ) then 1
         |when ((d.first_start_date <= a.use_date and a.use_date<=d.first_end_date) or (d.second_start_date<=a.use_date and a.use_date<=d.second_end_date)) and (j.type =0 or j.type=6) then 0
         |else
         |case when ((e.first_start_date <= a.use_date and a.use_date<=e.first_end_date) or (e.second_start_date<=a.use_date and a.use_date<=e.second_end_date)) and (j.type =1 or j.type =2 or j.type=3 or j.type =4 or j.type =5 ) then 1
         |when ((d.first_start_date <= a.use_date and a.use_date<=d.first_end_date) or (d.second_start_date<=a.use_date and a.use_date<=d.second_end_date)) and (j.type =0 or j.type=6) then 0
         |else 0 end end end end as have_class,
         |year(a.use_date) as year,
         |month(a.use_date) as month
         |from
         |(select id,'${data._2}' as use_date,'2017' as term_year from saas_v1.t_edu_school where stat=1 and reviewed=1 and create_time <= '${data._2}') as a
         |left outer join
         |(select have_class,school_id from saas_v1.t_edu_calendar where stat=1 and the_day='${data._2}') as b
         |on a.id = b.school_id
         |left outer join
         |(select type,holiday_day from saas_v1.t_edu_holiday where stat=1) as c
         |on a.use_date = c.holiday_day
         |left outer join
         |(select id,first_start_date,first_end_date,second_start_date,second_end_date from saas_v1.t_edu_schoolterm where stat=1 and term_year='2017') as d
         |on a.id = d.id
         |left outer join
         |(select * from saas_v1.t_edu_schoolterm_system where stat=1 and term_year='2017') as e
         |on a.term_year = e.term_year
         |left outer join
         |(select pmod(datediff('${data._2}', '1920-01-01') - 3, 7)as type,'${data._2}' as use_date) as j
         |on a.use_date = j.use_date
       """.stripMargin)



  }


}
