package com.ssic.beans

class EduCalendar(
                   var id: String,
                   var the_day: String,
                   var have_class: String,
                   var create_id: String,
                   var create_name: String,
                   var create_time: String,
                   var updater: String,
                   var last_update_time: String,
                   var stat: String,
                   var school_id: String,
                   var reason: String
                 ) {
  override def toString: String = super.toString

}
