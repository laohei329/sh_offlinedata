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

  def emptyToNull(x: String): String = {
    var lastData = "null"
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x
    } else {
      lastData
    }
  }

  def levelToName(x: String): String = {
    var level_name = "null_null"
    if ("0".equals(x)) {
      "幼托_托儿所"
    } else if ("1".equals(x)) {
      "幼托_托幼园"
    } else if ("2".equals(x)) {
      "幼托_托幼小"
    } else if ("3".equals(x)) {
      "幼托_幼儿园"
    } else if ("4".equals(x)) {
      "幼托_幼小"
    } else if ("5".equals(x)) {
      "幼托_幼小初"
    } else if ("6".equals(x)) {
      "幼托_幼小初高"
    } else if ("7".equals(x)) {
      "中小学_小学"
    } else if ("8".equals(x)) {
      "中小学_初级中学"
    } else if ("9".equals(x)) {
      "中小学_高级中学"
    } else if ("10".equals(x)) {
      "中小学_完全中学"
    } else if ("11".equals(x)) {
      "中小学_九年一贯制学校"
    } else if ("12".equals(x)) {
      "中小学_十二年一贯制学校"
    } else if ("13".equals(x)) {
      "中小学_职业初中"
    } else if ("14".equals(x)) {
      "中小学_中等职业学校"
    }else if ("15".equals(x)) {
      "其他_工读学校"
    } else if ("16".equals(x)) {
      "其他_特殊教育学校"
    }   else if ("17".equals(x)) {
      "其他_其他"
    }  else {
      level_name
    }
  }

}
