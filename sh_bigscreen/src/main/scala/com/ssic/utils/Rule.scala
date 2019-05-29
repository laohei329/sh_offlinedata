package com.ssic.utils

import org.apache.commons.lang3.StringUtils

object Rule {

  def emptyToNull(x: String): String = {
    var lastData = "null"
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x
    } else {
      lastData
    }
  }

  def int(x:String): String ={

    if(x.size <2){
      "0"+x
    }else{
      x
    }
  }

}
