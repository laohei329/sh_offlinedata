package com.ssic.beans

class EduCompetentDepartment (
                             var org_merchant_id:String,
                             var org_parent_merchant_id:String,
                             var name:String,
                             var code:String,
                             var uuid:String,
                             var parent_id:String,
                             var management_area_type:String,
                             var create_username:String,
                             var create_time:String,
                             var update_username:String,
                             var update_time:String,
                             var is_deleted:String
                             ){
  override def toString: String = super.toString
}
