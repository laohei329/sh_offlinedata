package com.ssic.beans

class SaasPackage(
                 var id:String,
                 var supply_date:String,
                 var school_id:String,
                 var menu_id:String,
                 var supplier_id:String,
                 var menu_group_name:String,
                 var ledger_type:String,
                 var create_time:String,
                 var stat:String,
                 var is_publish:Int,
                 var industry_type:String
                 ) {
  override def toString: String = super.toString
}
