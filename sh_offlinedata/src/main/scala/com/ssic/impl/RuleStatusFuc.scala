package com.ssic.impl

trait RuleStatusFuc {

  //排菜操作时间规则
  def platoonrulestatus(data:(String,Int,String)):Int

  //配送操作时间规则
  def distributionstatus(data:(String,String)):Int

  //留样操作时间规则
  def reservestatus(data:(String,String)):Int

}
