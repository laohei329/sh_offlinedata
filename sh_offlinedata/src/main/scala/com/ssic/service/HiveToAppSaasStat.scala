package com.ssic.service

import com.ssic.impl.HiveToAppSaasFunc
import org.apache.spark.sql.hive.HiveContext

class HiveToAppSaasStat extends HiveToAppSaasFunc{
  override def appedudishmenu(data: (HiveContext, String, String)): Unit = {
    data._1.sql(
      s"""
        |insert overwrite table app_saas_v1.app_t_edu_dish_menu partition(year,month) select a.id as package_id,
        |a.supply_date as supply_date,
        |c.school_name as school_name,
        |c.area as area,
        |c.address as address,
        |c.level_name as level_name,
        |c.school_nature_name as school_nature_name,
        |c.school_nature_sub_name as school_nature_sub_name,
        |c.license_main_type as license_main_type,
        |c.license_main_child as license_main_child,
        |a.ledger_type as ledger_type,
        |b.cater_type_name as cater_type_name,
        |b.dishes_name as dishes_name,
        |b.category as category,
        |b.dishes_number as dishes_number,
        |a.menu_group_name as menu_group_name,
        |d.supplier_name as supplier_name,
        |a.school_id as school_id,
        |a.supplier_id as supplier_id,
        |a.create_time as package_create_time,
        |a.school_supplier_id as school_supplier_id,
        |e.reserve_hour as reserve_hour,
        |e.reserve_minute as reserve_minute,
        |e.remark as remark,
        |e.creator as reserve_creator,
        |f.quantity as quantity,
        |f.create_time as reserve_create_time,
        |c.is_branch_school as is_branch_school,
        |c.branch_total as branch_total,
        |c.parent_id as parent_id,
        |c.department_master_id as department_master_id,
        |c.department_slave_id_name as department_slave_id_name,
        |c.school_area_id as school_area_id,
        |g.have_class as have_class,
        |case when f.have_reserve = 1 then 1 else 0 end as have_reserve,
        |c.department_id as department_id,
        |case when f.create_time is null then '4' else
        |case when (f.create_time <= concat(date(a.supply_date)," 23:59:59")) then '1' else
        |case when (f.create_time <= concat(cast(date_add(date(a.supply_date),1) as string)," 23:59:59")) then '2' else '3'
        |end end end as reserve_deal_status,
        |year(a.supply_date) as year,
        |month(a.supply_date) as month
        |from
        |(select id,ledger_type,menu_group_name,school_id,supplier_id,supply_date,create_time,school_supplier_id from saas_v1.t_saas_package where stat=1 and industry_type=1 and (year='${data._3}')  and supply_date >= '${data._2}')as a
        |left outer join
        |(select package_id,dishes_name,cater_type_name,category,dishes_number from saas_v1.t_saas_package_dishes where stat=1 and ( year='${data._3}'))as b
        |on a.id=b.package_id
        |left outer join
        |(select * from (select package_id,id,supply_date,reserve_hour,reserve_minute,remark,school_id,supplier_id,creator,ROW_NUMBER() over(PARTITION BY package_id ORDER BY package_id desc)as rn from saas_v1.t_pro_reserve_sample_temp where supply_date >= '${data._2}') as reserve where reserve.rn=1) as e
        |on a.id = e.package_id
        |left outer join
        |(select sample_id,dishes,quantity,create_time,1 as have_reserve from saas_v1.t_pro_reserve_sample_dishes_temp ) as f
        |on e.id = f.sample_id and b.dishes_name = f.dishes
        |left outer join
        |(select use_date,have_class,school_id from saas_v1_dw.dw_t_edu_calendar where year='${data._3}' ) as g
        |on a.supply_date = g.use_date and a.school_id = g.school_id
        |left outer join
        |(select id,school_name,area,address,level_name,school_nature_name,school_nature_sub_name,license_main_type,license_main_child,is_branch_school,branch_total,parent_id,department_master_id,department_slave_id_name,school_area_id,department_id from app_saas_v1.t_edu_school_new) as c
        |on a.school_id=c.id
        |left outer join
        |(select id ,supplier_name from app_saas_v1.t_pro_supplier where supplier_type=1) as d
        |on a.supplier_id = d.id
        |where b.dishes_name is not null and c.school_name is not null
      """.stripMargin)
  }

  override def appeduledegedetail(data: (HiveContext, String, String)): Unit = {
    data._1.sql(
      s"""
        |insert overwrite table app_t_edu_ledege_detail partition(year,month) select a.id as ledger_master_id,b.use_date as use_date,
        |a.ware_batch_no as ware_batch_no,
        |c.school_name as school_name,
        |c.area as area,
        |c.address as address,
        |c.level_name as level_name,
        |c.school_nature_name as school_nature_name,
        |c.school_nature_sub_name as school_nature_sub_name,
        |c.license_main_type as license_main_type,
        |c.license_main_child as license_main_child,
        |a.ledger_type as ledger_type,
        |b.supplier_name as supplier_name,
        |b.supplier_material_name as supplier_material_name,
        |b.name as name,
        |b.wares_type_name as wares_type_name,
        |b.actual_quantity as actual_quantity,
        |b.amount_unit as amount_unit,
        |b.first_num as first_num,
        |b.supplier_material_units as supplier_material_units,
        |b.second_num as second_num,
        |b.other_quantity as other_quantity,
        |b.batch_no as batch_no,
        |b.production_date as production_date,
        |b.shelf_life as shelf_life,
        |b.supply_name as supply_name,
        |a.haul_status as haul_status,
        |b.delivery_number as delivery_number,
        |d.image as images,
        |a.delivery_date as delivery_date,
        |b.receiver_id as school_id,
        |b.supplier_id as supplier_id,
        |case when b.confirm_material_type= 1 then 1 when b.confirm_material_type =2 then 2 else 1 end as material_type,
        |b.supply_id as supply_id,
        |a.action_date as action_date,
        |b.spce as spce,
        |b.expiration_date as expiration_date,
        |c.department_master_id as department_master_id,
        |c.department_slave_id_name as department_slave_id_name,
        |b.quantity as quantity,
        |year(b.use_date) as year,
        |month(b.use_date) as month
        |from
        |(select id,ledger_type,ware_batch_no,haul_status,delivery_date,action_date from saas_v1.t_pro_ledger_master where stat=1  and year="${data._3}") as a
        |right outer join
        |(select master_id,use_date,name,supplier_material_name,wares_type_name,actual_quantity,amount_unit,first_num,second_num,supplier_material_units,other_quantity,production_date,shelf_life,supply_name,delivery_number,supplier_name,supplier_id,receiver_id,batch_no,supply_id,confirm_material_type,spce,expiration_date,quantity from saas_v1.t_pro_ledger where stat=1 and year="2019"and use_date >= "2019-09-01 00:00:00" ) as b
        |on a.id=b.master_id
        |left outer join
        |(select id,school_name,area,address,level_name,school_nature_name,school_nature_sub_name,license_main_type,license_main_child,department_master_id,department_slave_id_name from t_edu_school_new ) as c
        |on b.receiver_id=c.id
        |left outer join
        |(select relation_id,image from saas_v1_dw.dw_t_pro_images where year ="2019" and ( month='9' or month='10' or month='11')) as d
        |on a.id = d.relation_id
        |where c.school_name is not null
      """.stripMargin)
  }
}
