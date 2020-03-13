package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import lombok.Data;

import java.math.BigDecimal;

/** 
 * @Description: 验收准确率和操作率
 * @Author: jianghy 
 * @Date: 2019/12/28
 * @Time: 17:18       
 */
@Data
public class CheckOperateRateInfo {


	/**
	 * 区域
	 */
	private String area;
	/**
	 * 配送日期
	 */
	private String dishDate;
	/**
	 * 学制类型
	 */
	private String schType;
	/**
	 * 应验收学校数量
	 */
	private Integer needCheckSchoolNum;
	/**
	 * 已验收学校数量
	 */
	private Integer haveCheckSchoolNum;
	/**
	 * 未验收学校数量
	 */
	private Integer noCheckSchoolNum;
	/**
	 * 验收操作率
	 */
	private BigDecimal checkOperateRate;
	/**
	 * 物料数量
	 */
	private Integer materialNum;
	/**
	 * 排菜准确数据
	 */
	private Integer checkCorrectNum;
	/**
	 * 排菜不准确数据
	 */
	private Integer checkUnCorrectNum;
	/**
	 * 排菜准确率
	 */
	private BigDecimal checkCorrectRate;
}
