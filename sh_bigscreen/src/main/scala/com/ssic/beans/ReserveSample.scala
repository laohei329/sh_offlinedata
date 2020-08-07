package com.ssic.beans

class ReserveSample(
                     var id: String,
                     var supply_date: String,
                     var package_id: String,
                     var remark: String,
                     var cater_type_name: String,
                     var menu_group_name: String,
                     var reserve_hour: String,
                     var reserve_minute: String,
                     var compliance_status:String,
                     var creator: String,
                     var create_time: String,
                     var stat: String
                   ) {
  override def toString: String = super.toString
}
