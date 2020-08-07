package com.ssic.beans

class RecyclerWaste(
                   var id:String,
                   var source_id:String,
                   var district_id:String,
                   var platform_type:Int,
                   var `type`:String,
                   var secont_type:Int,
                   var recycler_name:String,
                   var recycler_number:String,
                   var recycler_date:String,
                   var contact:String,
                   var stat:String,
                   var recycler_documents:String
                   ) {
  override def toString: String = super.toString
}
