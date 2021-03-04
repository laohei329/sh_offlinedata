package com.ssic.beans

class License(
             var id:String,
             var lic_type:String,
             var lic_pic:String,
             var job_organization:String,
             var relation_id:String,
             var supplier_id:String,
             var lic_no:String,
             var operation:String,
             var give_lic_date:String,
             var lic_end_date:String,
             var occupation_range:String,
             var written_name:String,
             var stat:String
             ) {
  override def toString: String = super.toString
}
