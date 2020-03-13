package com.tfit.BdBiProcSrvYgwcSchOmc.config;

import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;

/*
 * 获得数据源DataSource,临时用,不需要启动容器
 */
public class DataSourceConn extends BasicDataSource {

	 public DataSource getDataSource() {
		  DataSource ds = null;
		  super.setDriverClassName("org.apache.hive.jdbc.HiveDriver");
		  //数据库是testdb1 
		  super.setUrl("jdbc:hive2://172.18.14.31:10000/app_saas_v1?useUnicode=true&amp;characterEncoding=UTF-8");
		  super.setUsername("");
		  super.setPassword("");
		  try {
			  ds = super.createDataSource();
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
		  return ds;
	 }
}
