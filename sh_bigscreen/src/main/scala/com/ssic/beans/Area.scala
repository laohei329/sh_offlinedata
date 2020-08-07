package com.ssic.beans

class Area(
            var ID: String,
            var code: String,
            var abbreviation: String,
            var name: String,
            var is_available: String,
            var IS_DELETED: String,
            var Create_UserName: String,
            var create_time: String,
            var Update_UserName: String,
            var update_time: String
          ) {
  override def toString: String = super.toString
}
