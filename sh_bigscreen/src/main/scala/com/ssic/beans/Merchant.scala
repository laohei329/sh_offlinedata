package com.ssic.beans

class Merchant(
                var id: String,
                var user_id: String,
                var code: String,
                var company_name:String,
                var channel_source: String,
                var auth_status: String,
                var merchant_type: String,
                var merchant_business_type: String,
                var office_province_id:String,
                var office_city_id:String,
                var office_region_id:String,
                var office_address:String,
                var contact_person:String,
                var contact_phone:String,
                var register_address:String,
                var registered_capital:String,
                var legal_representative:String,
                var del: String,
                var create_id: String,
                var create_time: String,
                var last_update_id: String,
                var last_update_time: String,
                var ss_lunch_id: String
              ) {

  override def toString: String = super.toString

}
