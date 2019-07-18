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

}
