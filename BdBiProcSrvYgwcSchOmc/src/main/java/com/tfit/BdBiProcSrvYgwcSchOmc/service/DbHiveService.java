package com.tfit.BdBiProcSrvYgwcSchOmc.service;

import java.util.List;
import java.util.Map;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaDishSupDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaDishSupStats;

public interface DbHiveService {
	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询数据列表
	 * @return
	 */
    List<CaDishSupDets> getCaDishSupDetsList(List<String> listYearMonth, String startDate,String endDateAddOne,
    		String schName, String dishName, String rmcName,String distName,
    		String caterType, int schType, int schProp, int optMode, String menuName,
    		Integer startNum,Integer endNum,Map<Integer, String> schoolPropertyMap);
    /**
     * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询数据条数
     * @return
     */
    Integer getCaDishSupDetsCount(List<String> listYearMonth, String startDate,String endDateAddOne,
    		String schName, String dishName, String rmcName,String distName,
    		String caterType, int schType, int schProp, int optMode, String menuName);
    
	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据列表
	 * @return
	 */
    List<CaDishSupStats> getCaDishSupStatsList(List<String> listYearMonth, String startDate,String endDateAddOne,
    		String schName, String dishName,String distName,String dishType,String caterType,
    		Integer startNum,Integer endNum);
    /**
     * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据条数
     * @return
     */
    Integer getCaDishSupStatsCount(List<String> listYearMonth, String startDate,String endDateAddOne,
    		String schName, String dishName,String distName,String dishType,String caterType);
    
    /**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据列表
	 * @return
	 */
    List<CaDishSupStats> getCaDishSupStatsListFromTotal(List<String> listYearMonth, String startDate,String endDateAddOne,
    		String dishName,String distName,String dishType,String caterType,
    		Integer startNum,Integer endNum);
    /**
     * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据条数
     * @return
     */
    Integer getCaDishSupStatsCountFromTotal(List<String> listYearMonth, String startDate,String endDateAddOne,
    		String dishName,String distName,String dishType,String caterType);
}
