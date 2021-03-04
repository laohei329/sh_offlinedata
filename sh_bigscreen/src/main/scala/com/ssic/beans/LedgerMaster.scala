package com.ssic.beans

class LedgerMaster(
                  var id:String,
                  var action_date:String,
                  var ledger_type:String,
                  var receiver_id:String,
                  var source_id:String,
                  var ware_batch_no:String,
                  var haul_status:String,
                  var stat:String,
                  var delivery_date:String,
                  var purchase_date:String,
                  var compliance:String,
                  var delivery_record_date:String,
                  var industry_type:String
                  ) {
  override def toString: String = super.toString
}
