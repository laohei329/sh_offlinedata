package com.ssic.beans

class CooperationApply(
                        var id: String,
                        var buyer_merchant_id: String,
                        var seller_merchant_id: String,
                        var status:String,
                        var del: String
                      ) {
  override def toString: String = super.toString
}
