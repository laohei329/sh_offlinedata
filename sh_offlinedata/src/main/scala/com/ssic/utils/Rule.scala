package com.ssic.utils

import java.util.Calendar

import com.ssic.report.DayToExcelDepartment.format
import org.apache.commons.lang3._
import org.apache.commons.lang3.time.FastDateFormat


object Rule {

  /**

    * * string转换为int

    * * @param  String 数据

    */
  def emptyToInt(x: String): Int = {
    var lastData = 0
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x.toInt
    } else {
      lastData
    }
  }

  /**

    * * null数据转换为""

    * * @param x: String 数据

    */
  def nullToEmpty(x: String): String = {
    var lastData = ""
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x
    } else {
      lastData
    }
  }
  /**

    * * 空数据转换为null

    * * @param String 数据

    */

  def emptyToNull(x: String): String = {
    var lastData = "null"
    if (StringUtils.isNoneEmpty(x) && !x.equals("null")) {
      x
    } else {
      lastData
    }
  }

  /**

    * * 学制映射

    * * @param String 数据

    */
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

  /**

    * * 日期格式化

    * * @param timeStampType 日期格式

    * * @param day 与当前日期相差天数

    */

  def timeToStamp(timeStampType:String,day:Int):String={

    val format = FastDateFormat.getInstance(timeStampType)
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, day)
    val time = calendar.getTime
    val date = format.format(time)
    return date
  }

}
