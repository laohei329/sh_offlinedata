package com.ssic.beans

class SupplierInfo(
                    var id: String,
                    var company_id: String,
                    var uuid: String,
                    var supplier_name: String,
                    var supplier_type:String,
                    var address: String,
                    var province_id: String,
                    var city_id: String,
                    var region_id: String,
                    var supplier_classify: String,
                    var supplier_contact_name: String,
                    var supplier_contact_mobilephone: String,
                    var registered_capital: String,
                    var legal_representative: String,
                    var business_license:String,
                    var create_username: String,
                    var create_time: String,
                    var update_username: String,
                    var update_time: String,
                    var is_available: String,
                    var is_deleted: String,
                    var audit_state: String,
                    var stat:String
                  ) {
  override def toString: String = super.toString
}
