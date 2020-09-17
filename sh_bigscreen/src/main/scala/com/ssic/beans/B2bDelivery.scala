package com.ssic.beans

class B2bDelivery(
                   var id: String,
                   var delivery_id:String,
                   var buyer_merchant_id: String,
                   var seller_merchant_id: String,
                   var expected_receive_date:String,
                   var merchant_id:String,
                   var delivery_code: String,
                   var start_time :String,
                   var business_type: String,
                   var delivery_date: String,
                   var purchase_date: String,
                   var delivery_record_date: String,
                   var compliance: String,
                   var haul_status: String,
                   var del: String
                 ) {
  override def toString: String = super.toString
}
