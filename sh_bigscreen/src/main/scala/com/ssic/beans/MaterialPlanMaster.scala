package com.ssic.beans

class MaterialPlanMaster(
                        var id:String,
                        var `type`:String,
                        var use_date:String,
                        var supplier_id:String,
                        var proj_id:String,
                        var status:String,
                        var stat:String,
                        var proj_name:String
                        ) {
  override def toString: String = super.toString
}
