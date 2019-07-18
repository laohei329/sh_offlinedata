package com.ssic.service

import java.util.Calendar

import com.ssic.impl.HiveToDwSaasFunc
import com.ssic.utils.Tools
import org.apache.commons.lang3.time._
import org.apache.spark.SparkContext
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.hive.HiveContext


class HiveToDwSaasStat extends HiveToDwSaasFunc {
  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy")
  private val format2 = FastDateFormat.getInstance("M")

  override def insertdwschool(data: HiveContext): Unit = {
    data.sql(
      s"""
         |insert overwrite table saas_v1_dw.t_edu_school_new select a.id as id,
         |a.school_id as school_id,
         |a.committee_id as committee_id,
         |a.school_name as school_name,
         |a.school_thum as school_thum,
         |a.corporation as corporation,
         |a.corporation_way as corporation_way,
         |a.mobile_no as mobile_no,
         |a.contacts as contacts,
         |a.address as address,
         |a.longitude as longitude,
         |a.latitude as latitude,
         |a.supplier_id as supplier_id,
         |a.reviewed as reviewed,
         |a.online_payment as online_payment,
         |a.pj_no as pj_no,
         |a.canteen_mode as canteen_mode,
         |a.purchase_mode as purchase_mode,
         |a.ledger_type as ledger_type,
         |a.school_pic as school_pic,
         |a.school_logo as school_logo,
         |a.provinces as provinces,
         |a.city as city,
         |a.area as area,
         |a.school_num as school_num,
         |a.email as email,
         |a.property as property,
         |a.creator as creator,
         |a.create_time as create_time,
         |a.updater as updater,
         |a.last_update_time as last_update_time,
         |a.stat as stat,
         |a.corporation_telephone as corporation_telephone,
         |a.is_branch_school as is_branch_school,
         |a.social_credit_code as social_credit_code,
         |a.parent_id as parent_id,
         |a.department_master_id as department_master_id,
         |a.department_master as department_master,
         |case when b.district_id is null then a.department_slave_id else b.district_id end as department_slave_id_name,
         |a.department_slave as department_slave,
         |a.school_area_id as school_area_id,
         |a.school_area as school_area,
         |a.remark as remark,
         |a.department_head as department_head,
         |a.department_mobilephone as department_mobilephone,
         |a.department_telephone as department_telephone,
         |a.department_fax as department_fax,
         |a.department_email as department_email,
         |a.food_safety_persion as food_safety_persion,
         |a.food_safety_mobilephone as food_safety_mobilephone,
         |a.food_safety_telephone as food_safety_telephone,
         |a.students_amount as students_amount,
         |a.staff_amount as staff_amount,
         |case when a.level2 is null then
         |case when a.level is null then '-1'
         |when a.level ='0' then '3'
         |when a.level ='1' then '7'
         |when a.level ='2' then '8'
         |when a.level ='3' then '9'
         |when a.level ='6' then '13'
         |when a.level ='7' then '0'
         |when a.level ='9' then '17'
         |when a.level ='0,7' then '1'
         |when a.level ='7,0' then '1'
         |when a.level ='1,2' then '11'
         |when a.level ='2,3' then '10'
         |when a.level ='1,2,3' then '12'
         |when a.level ='1,2,3,6' then '12'
         |when a.level ='0,1' then '4'
         |when a.level ='3,6' then '14'
         |when a.level ='0,6' then '14'
         |when a.level ='0,1,2' then '5'
         |when a.level ='0,1,2,3' then '6'
         |when a.level ='0,1,2,3,6' then '6'
         |when a.level ='0,1,2,3,6,7,9' then '6'
         |when a.level ='1,9' then '17'
         |when a.level ='0,1,7' then '2'
         |when a.level ='0,1,2,3' then '6'
         |when a.level ='0,2' then '5'
         |else '-1' end else a.level2 end as level_name,
         |case when a.school_nature is null then '-1' when a.school_nature = '1' then '0' when a.school_nature = '0' then '0' when a.school_nature = '2' then '2' when a.school_nature = '3' then '3' when a.school_nature = '4' then '5' else '-1' end as school_nature_name,
         |a.school_nature_sub as school_nature_sub_name,
         |a.license_main_type,a.license_main_child from
         |(select * from saas_v1.t_edu_school where stat=1 and reviewed =1) as a
         |left join saas_v1.t_edu_committee as b
         |on a.department_slave_id=b.id
       """.stripMargin)
  }

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
