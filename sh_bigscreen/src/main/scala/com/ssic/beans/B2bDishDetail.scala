package com.ssic.beans

class B2bDishDetail (
                    var id:String,
                    var arrange_dish_id:String,
                    var project_site_id:String,
                    var dinning_name:String,
                    var arrange_date:String,
                    var dish_name:String,
                    var dish_num:String,
                    var is_available:String,
                    var create_date:String,
                    var del_flag:String
                    ){
  override def toString: String = super.toString
}
