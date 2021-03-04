package com.ssic.beans

import com.alibaba.fastjson.JSONObject

/**
  * Created by 云 on 2018/8/6.
  */


class SchoolBean(
                  var database: String,
                  var table: String,
                  var `type`: String,
                  var ts: String,
                  var xid: String,
                  var data: String,
                  var old: String
                ) {
  override def toString: String = super.toString
}
