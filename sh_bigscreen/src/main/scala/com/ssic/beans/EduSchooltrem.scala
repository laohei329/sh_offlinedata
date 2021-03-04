package com.ssic.beans

class EduSchooltrem(
                     var id: String,
                     var term_year: String,
                     var school_id: String,
                     var school_name: String,
                     var first_start_date: String,
                     var first_end_date: String,
                     var second_start_date: String,
                     var second_end_date: String,
                     var creator: String,
                     var create_time: String,
                     var updater: String,
                     var last_update_time: String,
                     var stat: String
                   ) {
  override def toString: String = super.toString
}
