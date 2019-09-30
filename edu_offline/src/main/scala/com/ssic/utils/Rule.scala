package com.ssic.utils

import org.apache.commons.lang3._


object Rule {

  def emptyToInt(x: String): Int = {
    var lastData = 0
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x.toInt
    } else {
      lastData
    }
  }

  def nullToEmpty(x: String): String = {
    var lastData = ""
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x
    } else {
      lastData
    }
  }


}
