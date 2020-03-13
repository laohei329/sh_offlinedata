package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import lombok.Data;

/**
 * 
 * @author Administrator
 *
 */
@Data
public class SchDishRsSitStat {
	
	public SchDishRsSitStat() {
		
		/**
		 * 已排菜学校数量
		 */
		this.dishSchNum = 0;
		
		/**
		 * 应留有学校数
		 */
		this.shouldRsSchNum = 0;
		
		/**
		 * 未留样学校
		 */
		this.noRsSchNum = 0;
		
		/**
		 * 已留样学校
		 */
		this.rsSchNum = 0;
		
		/**
		 * 菜品总数
		 */
		this.totalDishNum = 0;
		
		/**
		 * 已留样菜品数
		 */
		this.rsDishNum = 0;
		
		/**
		 * 未留样菜品数
		 */
		this.noRsDishNum = 0;
		
		/**
		 * 留样率，保留小数点有效数字两位
		 */
		this.rsRate = 0F;
		
		/**
		 * 学校留样率
		 */
		this.schRsRate = 0F;
	}
	
	/**
	 * 统计类别名称，按学校学制统计和按所属主管部门统计有效
	 */
	private String statClassName;
	
	/**
	 * 统计属性名称，具体名称依据统计模式值确定，如统计模式为0则为区域名称
	 */
	private String statPropName;
	
	/**
	 * 已排菜学校数量
	 */
	private Integer dishSchNum;
	
	/**
	 * 应留有学校数
	 */
	private Integer shouldRsSchNum;
	
	/**
	 * 未留样学校
	 */
	private Integer noRsSchNum;
	
	/**
	 * 已留样学校
	 */
	private Integer rsSchNum;
	
	/**
	 * 菜品总数
	 */
	private Integer totalDishNum;
	
	/**
	 * 已留样菜品数
	 */
	private Integer rsDishNum;
	
	/**
	 * 未留样菜品数
	 */
	private Integer noRsDishNum;
	
	/**
	 * 留样率，保留小数点有效数字两位
	 */
	private Float rsRate;
	
	/**
	 * 学校留样率
	 */
	private Float schRsRate;
	
	
}
