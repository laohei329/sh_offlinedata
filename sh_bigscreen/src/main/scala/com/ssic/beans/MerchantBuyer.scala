package com.ssic.beans

class MerchantBuyer(
                     var id: String,
                     var merchant_id: String,
                     var credit_code: String,
                     var user_id: String,
                     var company_name: String,
                     var company_type: String,
                     var del: String,
                     var create_id: String,
                     var create_time: String,
                     var last_update_id: String,
                     var last_update_time: String
                   ) {
  override def toString: String = super.toString
}
