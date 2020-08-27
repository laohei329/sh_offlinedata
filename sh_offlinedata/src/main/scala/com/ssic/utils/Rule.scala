package com.ssic.utils

import java.sql.Timestamp
import java.util.Calendar

import com.ssic.report.DayToExcelDepartment.format
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._


object Rule {

  /**
    *
    * * string转换为int
    *
    * * @param  String 数据
    *
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
    *
    * * null数据转换为""
    *
    * * @param x: String 数据
    *
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
    *
    * * 空数据转换为null
    *
    * * @param String 数据
    *
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
    *
    * * 空数据转换为null
    *
    * * @param String 数据
    *
    */

  def intToString(x: Integer): String = {
    var lastData = "null"
    if (x != null) {
      x.toString
    } else {
      lastData
    }
  }


  /**
    *
    * * 学制映射
    *
    * * @param String 数据
    *
    */
  def levelToName(x: String): String = {
    var level_name = ""
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
    } else if ("15".equals(x)) {
      "其他_工读学校"
    } else if ("16".equals(x)) {
      "其他_特殊教育学校"
    } else if ("17".equals(x)) {
      "其他_其他"
    } else {
      level_name
    }
  }

  /**
    *
    * * 区映射
    *
    * * @param String 数据
    *
    */
  def areaToName(area: String): String = {
    var areaName = ""
    if ("1".equals(area)) {
      areaName = "黄浦区"
    } else if ("2".equals(area)) {
      areaName = "嘉定区"
    } else if ("3".equals(area)) {
      areaName = "宝山区"
    } else if ("4".equals(area)) {
      areaName = "浦东新区"
    } else if ("5".equals(area)) {
      areaName = "松江区"
    } else if ("6".equals(area)) {
      areaName = "金山区"
    } else if ("7".equals(area)) {
      areaName = "青浦区"
    } else if ("8".equals(area)) {
      areaName = "奉贤区"
    } else if ("9".equals(area)) {
      areaName = "崇明区"
    } else if ("10".equals(area)) {
      areaName = "奉贤区"
    } else if ("2".equals(area)) {
      areaName = "静安区"
    } else if ("11".equals(area)) {
      areaName = "徐汇区"
    } else if ("12".equals(area)) {
      areaName = "长宁区"
    } else if ("13".equals(area)) {
      areaName = "普陀区"
    } else if ("14".equals(area)) {
      areaName = "虹口区"
    } else if ("15".equals(area)) {
      areaName = "杨浦区"
    } else if ("16".equals(area)) {
      areaName = "闵行区"
    } else {
      areaName
    }
    return areaName
  }

  /**
    *
    * * 食堂类型映射
    *
    * * @param String 数据
    *
    */
  def license(lincenseType: String): String = {
    var license = ""
    if ("0_0".equals(lincenseType)) {
      license = "自营-自行加工"
    } else if ("0_1".equals(lincenseType)) {
      license = "自营-食品加工"
    } else if ("1_0".equals(lincenseType)) {
      license = "外包-现场加工"
    } else if ("1_1".equals(lincenseType)) {
      license = "外包-快餐配送"
    } else {
      license
    }
    return license
  }

  /**
    *
    * * 民政食堂类型映射
    *
    * * @param String 数据
    *
    */
  def licenseNew(lincenseType: String, lincenseTypeChild: String): String = {
    var license = ""
    if ("0".equals(lincenseType)) {
      license = "自营"
    } else if ("1".equals(lincenseType)) {
      if ("0".equals(lincenseTypeChild)) {
        license = "外包-托管"
      } else if ("1".equals(lincenseTypeChild)) {
        license = "外包-外送"
      } else {
        license = "外包"
      }
    } else {
      license
    }
    return license
  }

  /**
    *
    * * 日期格式化
    *
    * * @param timeStampType 日期格式
    *
    * * @param day 与当前日期相差天数
    *
    */

  def timeToStamp(timeStampType: String, day: Int): String = {

    val format = FastDateFormat.getInstance(timeStampType)
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, day)
    val time = calendar.getTime
    val date = format.format(time)
    return date
  }

  /**
    *
    * * string日期格式化
    *
    * * @param string 日期
    *
    * * @param string 日期格式
    *
    */

  def stringToDate(date: String, timeStampType: String): String = {
    var time = ""
    if (StringUtils.isNoneEmpty(date)) {
      val format = FastDateFormat.getInstance(timeStampType)
      time = format.format(format.parse(date))
    } else {
      time
    }
    return time
  }


  /**
    *
    *
    * * @param data 数据
    *
    *
    */

  def toStringEmpty(data: Object): String = {
    if (data != null) {
      data.toString
    } else {
      ""
    }
  }

  /**
    *
    * * 民政主体映射
    *
    * * @param String 数据
    *
    */
  def mZLevelToName(x: String): String = {
    var level_name = ""
    if ("0".equals(x)) {
      level_name = "未知"
    } else if ("1".equals(x)) {
      level_name = "敬老院"
    } else if ("2".equals(x)) {
      level_name = "福利院"
    } else if ("3".equals(x)) {
      level_name = "养老院"
    } else if ("4".equals(x)) {
      level_name = "老年公寓"
    } else if ("5".equals(x)) {
      level_name = "护老院"
    } else if ("6".equals(x)) {
      level_name = "护养院"
    } else if ("7".equals(x)) {
      level_name = "护理院"
    } else if ("8".equals(x)) {
      level_name = "其它"
    } else {
      level_name
    }
    return level_name
  }

  /**
    *
    * * 民政主体映射
    *
    * * @param String 数据
    *
    */
  def mZSchoolNatureToName(x: String): String = {
    var schoolNatureName = ""
    if ("0".equals(x)) {
      "未知"
    } else if ("1".equals(x)) {
      "公办"
    } else if ("2".equals(x)) {
      "公建民营"
    } else if ("3".equals(x)) {
      "民办"
    } else {
      schoolNatureName
    }
  }

}
