package com.ssic.beans

class StopFoodBean(
                var id: String,
                var name: String,
                var `type`: String,
                var note: String,
                var stat: String,
                var create_time: String
              ) {
  override def toString: String = super.toString
}
