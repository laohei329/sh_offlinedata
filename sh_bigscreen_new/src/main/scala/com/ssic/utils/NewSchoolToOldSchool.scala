package com.ssic.utils

object NewSchoolToOldSchool {

  //将中台的关于教属的映射转到以前老的教属的映射
  def committeeToOldMasterId(data: String): String = {
    var department_master_id = "null"
    if ("9".equals(data)) {
      "0" //中台其他 9  =》 老的其他 0
    } else if ("6".equals(data)) {
      "1" //中台部6  =》  老的部 1
    } else if ("0".equals(data)) {
      "2" //中台市 0 =》 老的市  2
    } else if ("2".equals(data)) {
      "3" //中台区 2 =》 老的区  3
    } else if ("5".equals(data)) {
      "5" //中台省 5 =》 老的没有这个概念，直接赋值
    } else if ("7".equals(data)) {
      "7" //中台国 7   =》 老的没有这个概念，直接赋值
    } else {
      department_master_id
    }
  }


  def committeeToOldSlaveId(data: (String, String)): String = {
    var department_slave_id = null
    if ("2".equals(data._1) && "黄浦区教育局".equals(data._2)) {
      "e6ee4acf-2c5b-11e6-b1e8-005056a5ed30" //老黄浦区教育局id
    } else if ("2".equals(data._1) && "嘉定区教育局".equals(data._2)) {
      "e6ee4e97-2c5b-11e6-b1e8-005056a5ed30" //老嘉定区教育局id
    } else if ("2".equals(data._1) && "宝山区教育局".equals(data._2)) {
      "e6ee4eec-2c5b-11e6-b1e8-005056a5ed30" //老宝山区教育局id
    } else if ("2".equals(data._1) && "浦东新区教育局".equals(data._2)) {
      "e6ee4f43-2c5b-11e6-b1e8-005056a5ed30" //老浦东新区教育局id
    } else if ("2".equals(data._1) && "松江区教育局".equals(data._2)) {
      "e6ee4fa4-2c5b-11e6-b1e8-005056a5ed30" //老松江区教育局id
    } else if ("2".equals(data._1) && "金山区教育局".equals(data._2)) {
      "e6ee4ffa-2c5b-11e6-b1e8-005056a5ed30" //老金山区教育局id
    } else if ("2".equals(data._1) && "青浦区教育局".equals(data._2)) {
      "e6ee5054-2c5b-11e6-b1e8-005056a5ed30" //老青浦区教育局id
    } else if ("2".equals(data._1) && "奉贤区教育局".equals(data._2)) {
      "e6ee50ac-2c5b-11e6-b1e8-005056a5ed30" //老奉贤区教育局id
    } else if ("2".equals(data._1) && "崇明区教育局".equals(data._2)) {
      "e6ee5101-2c5b-11e6-b1e8-005056a5ed30" //老崇明区教育局id
    } else if ("2".equals(data._1) && "静安区教育局".equals(data._2)) {
      "e6ee4bd5-2c5b-11e6-b1e8-005056a5ed30" //老静安区教育局id
    } else if ("2".equals(data._1) && "徐汇区教育局".equals(data._2)) {
      "e6ee4c4f-2c5b-11e6-b1e8-005056a5ed30" //老徐汇区教育局id
    } else if ("2".equals(data._1) && "长宁区教育局".equals(data._2)) {
      "e6ee4cb2-2c5b-11e6-b1e8-005056a5ed30" //老长宁区教育局id
    } else if ("2".equals(data._1) && "普陀区教育局".equals(data._2)) {
      "e6ee4d17-2c5b-11e6-b1e8-005056a5ed30" //老普陀区教育局id
    } else if ("2".equals(data._1) && "虹口区教育局".equals(data._2)) {
      "e6ee4d78-2c5b-11e6-b1e8-005056a5ed30" //老虹口区教育局id
    } else if ("2".equals(data._1) && "杨浦区教育局".equals(data._2)) {
      "e6ee4dd1-2c5b-11e6-b1e8-005056a5ed30" //老杨浦区教育局id
    } else if ("2".equals(data._1) && "闵行区教育局".equals(data._2)) {
      "e6ee4e3f-2c5b-11e6-b1e8-005056a5ed30" //老闵行区教育局id
    } else if ("9".equals(data._1)) {
      "0" //老当masterid 为0  其他  slave 对应 0 其他
    } else if ("6".equals(data._1) && "教育部".equals(data._2)) {
      "1" //老教育部id
    } else if ("6".equals(data._1) && "其他".equals(data._2)) {
      "0" //老部级的 其他id
    } else if ("0".equals(data._1) && "其他".equals(data._2)) {
      "0" //老市级的 其他id
    } else if ("0".equals(data._1) && "上海市水务局（海洋局）".equals(data._2)) {
      "1" //老市级的 市水务局（海洋局）id
    } else if ("0".equals(data._1) && "上海市农委".equals(data._2)) {
      "2" //老市级的 市农委id
    } else if ("0".equals(data._1) && "上海市交通委".equals(data._2)) {
      "3" //老市级的 市交通委id
    } else if ("0".equals(data._1) && "上海市科委".equals(data._2)) {
      "4" //老市级的 市科委id
    } else if ("0".equals(data._1) && "上海市商务委".equals(data._2)) {
      "5" //老市级的 市商务委i
    } else if ("0".equals(data._1) && "上海市经信委".equals(data._2)) {
      "6" //老市级的 市经信委id
    } else if ("0".equals(data._1) && "上海市教委".equals(data._2)) {
      "7" //老市级的 市教委id
    } else {
      department_slave_id
    }
  }

  def committeeToOldArea(data: String): String = {
    //将上海的区域映射成老的区域映射，其他地区不变
    if ("8627".equals(data)) {
      "1"
    } else if ("8637".equals(data)) {
      "2"
    } else if ("8636".equals(data)) {
      "3"
    } else if ("8638".equals(data)) {
      "4"
    } else if ("8640".equals(data)) {
      "5"
    } else if ("8639".equals(data)) {
      "6"
    } else if ("8641".equals(data)) {
      "7"
    } else if ("8642".equals(data)) {
      "8"
    } else if ("8643".equals(data)) {
      "9"
    } else if ("8630".equals(data)) {
      "10"
    } else if ("8628".equals(data)) {
      "11"
    } else if ("8629".equals(data)) {
      "12"
    } else if ("8631".equals(data)) {
      "13"
    } else if ("8633".equals(data)) {
      "14"
    } else if ("8634".equals(data)) {
      "15"
    } else if ("8635".equals(data)) {
      "16"
    } else {
      data
    }
  }

}
