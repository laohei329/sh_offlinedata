package com.ssic.utils

import java.sql.{Connection, DriverManager}

import com.ssic.utils.Tools.{pwd, url, user}
import com.typesafe.config.ConfigFactory

object MysqlUtils {

  private val config = ConfigFactory.load()

  val url = config.getString("db.default.url")
  val user = config.getString("db.default.user")
  val pwd = config.getString("db.default.password")


  /**
    * 链接mysql
    */

  def open: Connection = {
    val conn = DriverManager.getConnection(url, user, pwd)
    return conn
  }

  /**
    * b2b的买家id查询其类型
    *
    * @param  id 买家的merchant_id
    */

  def merchantToType(id: String): Int = {
    val conn: Connection = open
    val pstmt = conn.prepareStatement(s"select company_type from merchant_buyer where del=0 and merchant_id=?")
    pstmt.setString(1, id)
    val resultSet = pstmt.executeQuery()
    var company_type = 0
    while (resultSet.next()) {
      company_type = resultSet.getInt("company_type")
    }
    conn.close()
    return company_type
  }

  /**
    * b2b的买家id查询其阳光午餐的id
    *
    * @param  id 买家的merchant_id
    */

  def merchantToEduid(id: String): String = {
    val conn: Connection = open
    val pstmt = conn.prepareStatement(s"select ss_lunch_id from merchant where del=0 and id=?")
    pstmt.setString(1, id)
    val resultSet = pstmt.executeQuery()
    var ss_lunch_id = "null"
    while (resultSet.next()) {
      ss_lunch_id = resultSet.getString("ss_lunch_id")
    }
    conn.close()
    return ss_lunch_id
  }

  /**
    * b2b的买家id查询其买家id
    *
    * @param  id 买家的merchant_id
    */

  def merchantToMaiId(id: String): String = {
    val conn: Connection = open
    val pstmt = conn.prepareStatement(s"select seller_merchant_id from cooperation_apply where del=0 and status = 6 and buyer_merchant_id=?")
    pstmt.setString(1, id)
    val resultSet = pstmt.executeQuery()
    var seller_merchant_id = "null"
    while (resultSet.next()) {
      seller_merchant_id = resultSet.getString("seller_merchant_id")
    }
    conn.close()
    return seller_merchant_id
  }


}
