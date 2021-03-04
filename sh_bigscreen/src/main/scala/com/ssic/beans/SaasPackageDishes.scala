package com.ssic.beans

class SaasPackageDishes(
                       var id:String,
                       var package_id:String,
                       var dishes_name:String,
                       var dishes_number:String,
                       var cater_type_name:String,
                       var category:String,
                       var create_time:String,
                       var stat:String
                       ) {
  override def toString: String = super.toString
}
