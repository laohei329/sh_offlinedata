package com.tfit.BdBiProcSrvYgwcSchOmc.service;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.AppOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CheckOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.DishOperateRateInfo;

import java.util.List;

/**
 * 排菜相关hive库的查询
 * @author Administrator
 *
 */
public interface DbHiveDishService {
	

     /**
      * @Description: 排菜操作率
      * @Param: [startDate, endDate]
      * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
      * @Author: jianghy
      * @Date: 2019/12/28
      * @Time: 17:30
      */
	 List<DishOperateRateInfo> getDishOperateRateList(String startDate, String endDate);

	/**
	 * @Description: 排菜准确率
	 * @Param: [startDate, endDate]
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
	 * @Author: jianghy
	 * @Date: 2019/12/28
	 * @Time: 17:30
	 */
	List<DishOperateRateInfo> getDishCorrectRateList(String startDate, String endDate);

	/**
	 * @Description: 排菜操作率
	 * @Param: [startDate, endDate]
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
	 * @Author: jianghy
	 * @Date: 2019/12/28
	 * @Time: 17:30
	 */
	List<CheckOperateRateInfo> getCheckOperateRateList(String startDate, String endDate);

	/**
	 * @Description: 排菜操作率
	 * @Param: [startDate, endDate]
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.CheckOperateRateInfo>
	 * @Author: jianghy
	 * @Date: 2019/12/28
	 * @Time: 17:30
	 */
	List<CheckOperateRateInfo> getCheckCorrectRateList(String startDate, String endDate);

	/**
	 * @Description: app使用率
	 * @Param: [startDate, endDate]
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
	 * @Author: jianghy
	 * @Date: 2019/12/28
	 * @Time: 17:30
	 */
	List<AppOperateRateInfo> getAppOperateRateList(String startDate, String endDate);
}
