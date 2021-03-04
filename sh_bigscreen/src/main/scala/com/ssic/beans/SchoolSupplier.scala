package com.ssic.beans

class SchoolSupplier(
                      var id: String,
                      var org_merchant_id: String,
                      var org_parent_merchant_id: String,
                      var uuid: String,
                      var school_id: String,
                      var supplier_id: String,
                      var stat: String,
                      var company_id: String,
                      var industry_type: String
                    ) {
  override def toString: String = super.toString
}
