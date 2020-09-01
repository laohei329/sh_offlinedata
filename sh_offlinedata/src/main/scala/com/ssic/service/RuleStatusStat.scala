package com.ssic.service

import java.util.{Calendar, Date}

import com.ssic.impl.RuleStatusFuc
import org.apache.commons.lang3._
import org.apache.commons.lang3.time._

/**
  * 1 表示规范录入
  * 2 表示补录
  * 3 表示逾期补录
  * 4 表示无数据*/
class RuleStatusStat extends RuleStatusFuc {

  private val format = FastDateFormat.getInstance("yyyy-MM-dd")
  private val format1 = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss")

  override def platoonrulestatus(data: (String, Int, String)): Int = {
    val now_date = format.parse(data._1) //当前时间
    val week = data._2 //表示当前时间是周几
    val have_platoon = data._3.split("_")(1) //是否排菜
    if ("未排菜".equals(have_platoon)) {
      return 4
    } else {
      val createTime = data._3.split("_")(3)
      if("null".equals(createTime)){
        return 4
      }else{
        val platoon_create_time = format.parse(createTime) //排菜操作时间
        if (week == 1) {
          //如果当前时间是周一
          if (platoon_create_time before (now_date)) {
            //如果操作时间在当前时间前面，表示规范录入
            return 1
          } else {
            //如果操作时间不在当前时间前面，表示逾期补录
            return 3
          }
        } else {
          if (week != 0) {
            //当前时间不是周日
            val calendar = Calendar.getInstance()
            calendar.setTime(now_date)
            calendar.add(Calendar.DAY_OF_MONTH, -(week - 1))
            val now_week_one = calendar.getTime //当周的周一日期
            if (platoon_create_time before (now_week_one)) {
              //如果操作时间在周一前面，表示规范录入
              return 1
            } else {
              if (platoon_create_time before (now_date)) {
                //如果操作时间不在周一前面，在当前时间内，表示补录
                return 2
              } else {
                return 3
              }
            }

          } else {
            //当前时间是周日
            val calendar = Calendar.getInstance()
            calendar.setTime(now_date)
            calendar.add(Calendar.DAY_OF_MONTH, -6)
            val now_week_one = calendar.getTime //当周的周一日期
            if (platoon_create_time before (now_week_one)) {
              //如果操作时间在周一前面，表示规范录入
              return 1
            } else {
              if (platoon_create_time before (now_date)) {
                return 2
              } else {
                return 3
              }
            }
          }
        }
      }

    }

  }

  override def distributionstatus(data: (String, String)): Int = {
    if (StringUtils.isNoneEmpty(data._2) && !"null".equals(data._2)) {
      val deliveryDate = format1.parse(data._2) //验收时间
      val date = new Date
      val rule_date = format1.parse(data._1 + " 17:00:00") //规则时间
      val calendar = Calendar.getInstance()
      calendar.setTime(format.parse(data._1))
      calendar.add(Calendar.DAY_OF_MONTH, 1)
      val tomorrow_date = calendar.getTime //配送时间的后一天时间
      val tomorrow_date_rule = format1.parse(format.format(tomorrow_date) + " 17:00:00")

      if (deliveryDate.before(rule_date)) {
        //当验收时间 在 当天配送时间的17:00之前的时候 表示 规范录入
        return 1
      } else {
        //当验收时间 在 当天配送时间的17:00以及17:00之后的时候 表示 补录
        if (deliveryDate.before(tomorrow_date_rule)) {
          return 2
        } else {
          //当验收时间 在 配送时间第二天的17:00以及17:00之后的时候 表示 逾期补录
          return 3
        }

      }
    } else {
      return 4
    }


  }

  override def reservestatus(data: (String, String)):Int = {
    if (StringUtils.isNoneEmpty(data._2) && !"null".equals(data._2)) {
      val reserveCreateDate = format1.parse(data._2) //留样操作时间
      val rule_date = format1.parse(data._1 + " 23:59:59") //规则时间
      val calendar = Calendar.getInstance()
      calendar.setTime(format.parse(data._1))
      calendar.add(Calendar.DAY_OF_MONTH, 1)
      val tomorrow_date = calendar.getTime //排菜时间的后一天时间
      val tomorrow_date_rule = format1.parse(format.format(tomorrow_date) + " 23:59:59")
      if (reserveCreateDate.before(rule_date)) {
        //当留样操作时间 在当天排菜时间的23:59:59之前的时候 表示 规范录入
        return 1
      } else if (reserveCreateDate.equals(rule_date)) {
        //当留样操作时间 在当天排菜时间的23:59:59相等的时候 表示 规范录入
        return 1

      } else {
        if (reserveCreateDate.before(tomorrow_date_rule)) {
          //当留样操作时间 在第二天排菜时间的23:59:59之前的时候 表示 补录
          return 2
        } else if (reserveCreateDate.equals(tomorrow_date_rule)) {
          //当留样操作时间 在第二天排菜时间的23:59:59相等的时候 表示 补录
          return 2
        } else {
          //当留样操作时间 在第二天排菜时间的23:59:59之后的时候 表示 逾期补录
          return 3
        }
      }
    } else {
      return 4
    }
  }
}
