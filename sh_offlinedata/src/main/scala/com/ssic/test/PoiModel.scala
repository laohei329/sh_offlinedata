package com.ssic.test


case class PoiModel(
                     //内容
                     val content: String,
                     //上一行同一位置内容
                     val oldContent: String,
                     //行标
                     val rowIndex: Int,
                     //列标
                     val cellIndex: Int
                   )
