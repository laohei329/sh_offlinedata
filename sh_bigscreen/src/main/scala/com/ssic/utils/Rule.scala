package com.ssic.utils

import org.apache.commons.lang3._

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

  def nullToZero(x:String):Int={
    var lastData = 0
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x.toInt
    } else {
      lastData
    }
  }

}
