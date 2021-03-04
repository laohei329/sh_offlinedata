package com.ssic.beans

class ReserveSampleDishes (
                          var id:String,
                          var sample_id:String,
                          var dishes:String,
                          var quantity:String,
                          var is_consistent:String,
                          var is_consistent_remark:String,
                          var stat:String
                          ){
  override def toString: String = super.toString
}
