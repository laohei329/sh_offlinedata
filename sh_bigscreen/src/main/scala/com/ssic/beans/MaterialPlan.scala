package com.ssic.beans

class MaterialPlan (
                   var id:String,
                   var `type`:String,
                   var use_date:String,
                   var supplier_id:String,
                   var supplier_name:String,
                   var proj_id:String,
                   var status:String,
                   var stat:String,
                   var proj_name:String
                   ){
  override def toString: String = super.toString
}
