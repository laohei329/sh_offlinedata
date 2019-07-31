package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import lombok.Data;

/**
 * 预警
 * @author fengyang_xie
 * @date 2019/01/25
 *
 */
@Data
public class SumDataWarnInfo {
	/**
	 * 未处理单位数
	 */
	private Integer noProcUnitNum;
	/**
	 * 未处理预警数
	 */
	private Integer noProcNum;
	/**
	 * 处理率
	 */
	private Float procRate;
}
