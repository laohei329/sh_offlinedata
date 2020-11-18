package com.ssic.bean

case class PoiModel(
                     //内容
                     content: String,
                     //上一行同一位置内容
                     oldContent: String,
                     //行标
                     rowIndex: Int,
                     //列标
                     cellIndex: Int
                   )
