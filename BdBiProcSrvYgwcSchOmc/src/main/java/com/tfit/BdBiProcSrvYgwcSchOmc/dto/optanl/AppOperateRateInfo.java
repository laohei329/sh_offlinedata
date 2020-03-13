package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import lombok.Data;

import java.math.BigDecimal;

/** 
 * @Description: 排菜准确率和操作率
 * @Author: jianghy 
 * @Date: 2019/12/28
 * @Time: 17:18       
 */
@Data
public class AppOperateRateInfo {


	/**
	 * 区域
	 */
	private String area;
	/**
	 * 日期
	 */
	private String dishDate;
	/**
	 * 学制类型
	 */
	private String schType;
	/**
	 * 应使用订单
	 */
	private Integer needOrderNum;
	/**
	 * 已使用订单
	 */
	private Integer haveOrderNum;
	/**
	 * 未使用订单
	 */
	private Integer noOrderNum;
	/**
	 * app使用率
	 */
	private BigDecimal appOperateRate;
}
