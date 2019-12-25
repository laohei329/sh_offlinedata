package com.ssic.beans

/**
  * Created by 云 on 2018/8/6.
  */

class DataBean(
                //t_pro_material_plan_master
                var id: String,
                var `type`: String,
                var use_date: String,
                var supplier_id: String,
                var proj_id: String,
                var proj_name: String,
                var status: String,
                var creator: String,
                var create_time: String,
                var updater: String,
                var last_update_time: String,
                var stat: String,

                //t_pro_ledger_master
                var action_date: String,
                var ledger_type: String,
                var cater_type_id: String,
                var cater_type_name: String,
                var receiver_id: String,
                var receiver_name: String,
                var user_id: String,
                var source_id: String,
                var ware_batch_no: String,
                var image: String,
                var haul_status: String,
                var delivery_date: String,

                //	t_pro_ledger
                var master_id: String,
                var wares_id: String,
                var menu_id: String,
                var delivery_attr: Int,
                var wares_type: Int,
                var wares_type_name: String,
                var name: String,
                var supply_id: String,
                var supply_name: String,
                var supplier_code: String,
                var supplier_name: String,
                var quantity: String,
                var amount_unit: String,
                var actual_quantity: String,
                var batch_no: String,
                var trace_code: String,
                var paste_flag: Int,
                var expiration_date: String,
                var warn_flag: Int,
                var delivery_number: String,
                var menu_group_name:String,

                //t_saas_package
                var cater_package_id: String,
                var package_name: String,
                var supply_date: String,
                var proj_group_id: String,
                var proj_group_name: String,
                var school_supplier_id: String,
                var school_id: String,
                var school_name: String,
                var menu_type: Int,
                var cost: Float,
                var is_publish: Int,
                var reserved: Int,
                var industry_type:String,

                //t_pro_supplier
                var company_type: String,
                var scope_id: String,
                var scope_name: String,
                var address: String,
                var provinces: String,
                var city: String,
                var area: String,
                var district_id: String,
                var supplier_classify: String,
                var supplier_type: String,
                var business_license: String,
                var organization_code: String,
                var food_service_code: String,
                var food_service_code_date: String,
                var food_business_code: String,
                var food_business_code_date: String,
                var food_circulation_code: String,
                var food_circulation_code_date: String,
                var food_produce_code: String,
                var food_produce_code_date: String,
                var corporation: String,
                var corporation_way: String,
                var contacts: String,
                var contact_way: String,
                var reg_time: String,
                var reg_address: String,
                var id_card: String,
                var id_type: String,
                var longitude: String,
                var latitude: String,
                var reviewed: String,
                var refuse_reason: String,
                var qa_person: String,
                var qa_way: String,
                var reg_capital: String,
                var annual_sales: String,
                var company_image: String,
                var note: String,
                var yidian_code: String,
                var is_new: Int,

                var region_id:String,
                var supplier_contact_name:String,
                var supplier_contact_mobilephone:String,
                var registered_capital:String,
                var legal_representative:String,
                var create_username:String,
                var update_username:String,
                var update_time:String,
                var is_available:String,
                var is_deleted:String,
                var audit_state:String,
                var business_license_number:String,
                var management_area_type:String,

                //t_edu_school_supplier
                var contact: String,
                var mobile_no: String,
                var project_name: String,
                var manager_id: String,
                var manager_name: String,
                var manager_no: String,
                var remark: String,
                var pj_no: String,
                var manager_status: Int,
                var proj_type: Int,
                var level: String,
                var package_num: Int,
                var delivery_way: Int,
                var staff_count: Int,
                var worker_count: Int,
                var student_count: Int,
                var relation: Int,
                var company_id:String,

                //t_saas_package_dishes
                var package_id: String,
                var category: String,
                var dishes_id: String,
                var dishes_name: String,
                var dishes_number: String,
                var food_quantity: Int,
                var dishes_image: String,

                //t_saas_package_dishes_ware
                var package_dishes_id: String,
                var material_name: String,
                var material_type: String,
                var ware_weight: Float,

                //t_edu_taboo
                var wares_dishes_id: String,
                var state: String,

                //t_edu_school
                var committee_id: String,
                var school_thum: String,
                var online_payment: String,
                var canteen_mode: String,
                var purchase_mode: String,
                var school_nature: String,
                var school_pic: String,
                var school_logo: String,
                var school_num: String,
                var email: String,
                var property: String,
                var corporation_telephone: String,
                var is_branch_school: String,
                var social_credit_code: String,
                var parent_id: String,
                var internal_num: String,
                var school_nature_sub: String,
                var department_master_id: String,
                var department_master: String,
                var department_slave_id: String,
                var department_slave: String,
                var school_area_id: String,
                var school_area: String,
                var license_main_type: String,
                var license_main_child: String,
                var department_head: String,
                var department_mobilephone: String,
                var department_telephone: String,
                var department_fax: String,
                var department_email: String,
                var food_safety_persion: String,
                var food_safety_mobilephone: String,
                var food_safety_telephone: String,
                var level2: String,
                var students_amount: String,
                var staff_amount: String,

                var org_merchant_id:String,
                var org_parent_merchant_id:String,
                var uuid:String,
                var school_parent_id:String,
                var committee_org_merchant_id:String,
                var seat_province_id :String,
                var seat_province_name:String,
                var seat_city_id:String,
                var seat_city_name:String,
                var seat_district_id:String,
                var seat_district_name:String,

                //t_pro_warning_license
                var lic_no: String,
                var license: String,
                var lic_pic: String,
                var written_name: String,

                //t_pro_reserve_sample
                var reserve_hour: Int,
                var reserve_minute: Int,
                var district_name: String,
                var compliance_status:String,

                //t_pro_reserve_sample_dishes
                var sample_id: String,
                var dishes: String,
                var is_consistent:String,
                var is_consistent_remark:String,


                //t_pro_recycler_waste
                var secont_type: Int,
                var recycler_id: String,
                var recycler_name: String,
                var recycler_number: String,
                var recycler_documents: String,
                var documents_pictures: String,
                var recycler_date: String,
                var platform_type: Int,

                //t_pro_images
                var relation_id: String,
                var logo: String,

                //t_pro_warning_view_relation
                var city_id: String,
                var schools_id: String,
                var province_id: String,
                var target: Int,
                var target_id: String,

                //t_pro_license
                var lic_name: String,
                var lic_type: String,
                var lic_start_date: String,
                var lic_end_date: String,
                var cer_source: String,
                var data_matching: String,
                var useing: String,
                var occupation_range: String,
                var healthexamine_organization: String,
                var job_organization: String,
                var inspect_institution: String,
                var certificate_type: String,
                var certificate_no: String,
                var operation: String,
                var give_lic_date: String,

              //area
                var ID:String,
                var code:String,
                var abbreviation:String,
                var IS_DELETED:String,
                var Create_UserName:String,
                var Update_UserName:String,

                var the_day:String,
                var have_class:String,
                var create_id:String,
                var create_name:String,
                var reason:String,
                var term_year:String,
                var first_start_date:String,
                var first_end_date:String,
                var second_start_date:String,
                var second_end_date:String,
                var holiday_day:String,


              //t_pro_employee
                var id_code:String
              ) {

}

class SchoolBean(
                  var database: String,
                  var table: String,
                  var `type`: String,
                  var ts: String,
                  var xid: String,
                  var data: DataBean,
                  var old: DataBean
                ) {
  override def toString: String = super.toString
}
