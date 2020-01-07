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
public class DishOperateRateInfo {


	/**
	 * 区域
	 */
	private String area;
	/**
	 * 学校数量
	 */
	private Integer schoolNum;
	/**
	 * 日期
	 */
	private String dishDate;
	/**
	 * 学制类型
	 */
	private String schType;
	/**
	 * 应排菜学校数量
	 */
	private Integer needDishSchoolNum;
	/**
	 * 已排菜学校数量
	 */
	private Integer haveDishSchoolNum;
	/**
	 * 未排菜学校数量
	 */
	private Integer noDishSchoolNum;
	/**
	 * 排菜操作率
	 */
	private BigDecimal dishOperateRate;
	/**
	 * 菜品个数
	 */
	private Integer dishNum;
	/**
	 * 排菜准确数据
	 */
	private Integer dishCorrectNum;
	/**
	 * 排菜不准确数据
	 */
	private Integer dishUnCorrectNum;
	/**
	 * 排菜准确率
	 */
	private BigDecimal dishCorrectRate;
}
