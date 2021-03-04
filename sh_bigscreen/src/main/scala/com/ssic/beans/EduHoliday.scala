package com.ssic.beans

class EduHoliday (
                 var id:String,
                 var name:String,
                 var holiday_day:String,
                 var `type`:String,
                 var create_id:String,
                 var create_name:String,
                 var create_time:String,
                 var updater:String,
                 var last_update_time:String,
                 var stat:String
                 ){
  override def toString: String = super.toString
}
