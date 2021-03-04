package com.ssic.beans

class GroupSupplier(
                   var org_merchant_id:String,
                   var org_parent_merchant_id:String,
                   var parent_id:String,
                   var uuid:String,
                   var supplier_name:String,
                   var company_type:String,
                   var supplier_type:String,
                   var provinces:String,
                   var city:String,
                   var area:String,
                   var address:String,
                   var contacts:String,
                   var contact_way:String,
                   var qa_person:String,
                   var qa_way:String,
                   var reg_address:String,
                   var reg_capital:String,
                   var corporation:String,
                   var reviewed:String,
                   var stat:String,
                   var creator:String,
                   var create_time:String,
                   var updater:String,
                   var last_update_time:String
                   ) {
  override def toString: String = super.toString
}
