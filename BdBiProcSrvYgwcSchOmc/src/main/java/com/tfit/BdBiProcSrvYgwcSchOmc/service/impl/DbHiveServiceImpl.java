package com.tfit.BdBiProcSrvYgwcSchOmc.service.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.DataSourceConn;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaDishSupDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga.CaDishSupStats;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.DbHiveService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Service
public class DbHiveServiceImpl implements DbHiveService {
	private static final Logger logger = LogManager.getLogger(DbHiveServiceImpl.class.getName());

	//额外数据源Hive
	@Autowired
	@Qualifier("dsHive")
	DataSource dataSourceHive;

	@Autowired
	@Qualifier("dsHive2")
	DataSource dataSourceHive2;

	//额外数据源Hive连接模板
	JdbcTemplate jdbcTemplateHive = null;

	//额外数据源Hive连接模板
	JdbcTemplate jdbcTemplateHive2 = null;

	//初始化处理标识，true表示已处理，false表示未处理
	boolean initProcFlag = false;

	//是否使用mybatis中间件
	boolean mybatisUseFlag = true;

	//初始化处理
	@Scheduled(fixedRate = 60*60*1000)
	public void initProc() {
		if(initProcFlag)
			return ;
		initProcFlag = true;
		logger.info("定时建立与 DataSource数据源dsHive对象表示的数据源的连接，时间：" + BCDTimeUtil.convertNormalFrom(null));
		jdbcTemplateHive = new JdbcTemplate(dataSourceHive);
		jdbcTemplateHive2 = new JdbcTemplate(dataSourceHive2);
	}

	//----------公共方法---------------------------------------------------------------------------
	public static JdbcTemplate getJdbcTemplateHive(JdbcTemplate jdbcTemplateHive,JdbcTemplate jdbcTemplateHive2,DataSource dataSourceHive) {

		JdbcTemplate jdbcTemplateHiveTemp = jdbcTemplateHive;
		logger.info("***********************************************************************");
		if(jdbcTemplateHiveTemp == null) {
			if(jdbcTemplateHive2!=null) {
				jdbcTemplateHiveTemp = jdbcTemplateHive2;
				logger.info("***********************************************************************01 jdbcTemplateHive2");
			}else {
				jdbcTemplateHiveTemp = null;
			}
		}
		boolean hive1IsBreak = false;
		if(jdbcTemplateHiveTemp !=null) {
			String sql = "select 1 from t_edu_school_new limit 0,0";
			try {
				jdbcTemplateHiveTemp.queryForMap(sql);
			}catch(Exception ex) {
				if(ex.getMessage().contains("java.net.SocketException: Broken pipe")) {
					logger.info("-------------------------------------------------------------------新线程启动hive");
					jdbcTemplateHiveTemp = jdbcTemplateHive2;
					logger.info("***********************************************************************catch jdbcTemplateHive2"+ex.getMessage());
				}
				hive1IsBreak= true;
			}
		}

		if(hive1IsBreak) {
			//如果hive1连接有问题，测试hive2
			if(jdbcTemplateHiveTemp !=null) {
				String sql = "select 1 from t_edu_school_new limit 0,0";
				try {
					jdbcTemplateHiveTemp.queryForMap(sql);
				}catch(Exception ex) {
					if(ex.getMessage().contains("java.net.SocketException: Broken pipe")) {

						logger.info("***********************************************************************catch jdbcTemplateHive2"+ex.getMessage());
						//重新加载hive1
						dataSourceHive = new DataSourceConn().getDataSource();
						jdbcTemplateHiveTemp = new JdbcTemplate(dataSourceHive);
					}
					hive1IsBreak= true;
				}
			}
		}
		logger.info("***********************************************************************"+jdbcTemplateHiveTemp.getDataSource().toString());

		return jdbcTemplateHiveTemp;
	}


	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询数据列表
	 */
	public List<CaDishSupDets> getCaDishSupDetsList(List<String> listYearMonth, String startDate,String endDateAddOne,
													String schName, String dishName, String rmcName,String distName,
													String caterType, int schType, int schProp, int optMode, String menuName,
													Integer startNum,Integer endNum,Map<Integer, String> schoolPropertyMap) {
		if(jdbcTemplateHive == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append("select from_unixtime(unix_timestamp(supply_date,'yyyyMMddHH'),'yyyy/MM/dd') repastDate,school_name schName,area distName,address detailAddr,");
		sb.append("level_name schType,school_nature_name schProp,license_main_type licenseMainType,license_main_child licenseMainChild,");
		sb.append("ledger_type dispType, cater_type_name caterType,");
		sb.append(" dishes_name dishName,category dishType,dishes_number supNum,menu_group_name menuName,supplier_name rmcName ");
		sb.append(" from app_t_edu_dish_menu" );
		sb.append(" where  1=1 ");
		getCaDishSupDetsListCondition(listYearMonth, startDate, endDateAddOne, schName, dishName, rmcName, distName,
				caterType, schType, schProp, optMode, menuName, sb);

		//sb.append(" order by distName asc,schType asc,menuName asc ");
		//sb.append(" order by distName asc ");
		sb.append(" limit "+startNum+","+endNum);
		logger.info("执行sql:"+sb.toString());
		String[] optModeNames= {""};
		logger.info("******************jdbcTemplateHive:"+jdbcTemplateHive);
		logger.info("******************jdbcTemplateHive:"+jdbcTemplateHive);
		return (List<CaDishSupDets>) jdbcTemplateHive.query(sb.toString(), new RowMapper<CaDishSupDets>() {

			@Override
			public CaDishSupDets mapRow(ResultSet rs, int rowNum) throws SQLException {
				if(rowNum > (endNum-(startNum==0?1:startNum))) {
					return null;
				}

				CaDishSupDets cdsd = new CaDishSupDets();
				cdsd.setRepastDate(rs.getString("repastDate"));
				if(rs.getString("schName")!=null){
					cdsd.setSchName(rs.getString("schName"));
				}

				cdsd.setDistName(rs.getString("distName"));
				cdsd.setDetailAddr(rs.getString("detailAddr"));

				if(rs.getString("schType")!=null && !"".equals(rs.getString("schType"))){
					cdsd.setSchType(AppModConfig.schTypeIdToNameMap.get(Integer.parseInt(rs.getString("schType"))));
				}

				if(rs.getString("schProp")!=null && !"".equals(rs.getString("schProp"))){
					cdsd.setSchProp(schoolPropertyMap.get(Integer.parseInt(rs.getString("schProp"))));
				}

				if(rs.getString("licenseMainType")!=null && rs.getString("licenseMainChild")!=null){
					optModeNames[0]="";
					optModeNames[0] = AppModConfig.getOptModeName(Short.parseShort("-1"), rs.getString("dispType"), rs.getString("licenseMainType"), rs.getShort("licenseMainChild"));
					cdsd.setOptMode(optModeNames[0]);
				}

				if(rs.getString("dispType")!=null){
					cdsd.setDispType(AppModConfig.dispTypeIdToNameForDsihMap.get((rs.getString("dispType")==null ||
							"".equals(rs.getString("dispType")))?-1:Integer.parseInt(rs.getString("dispType"))));
				}
				cdsd.setCaterType(rs.getString("caterType"));
				cdsd.setDishName(rs.getString("dishName"));
				cdsd.setDishType(rs.getString("dishType"));
				cdsd.setSupNum(rs.getInt("supNum"));
				cdsd.setMenuName(rs.getString("menuName"));
				cdsd.setRmcName(rs.getString("rmcName"));

				return cdsd;
			}
		});
	}

	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询数据条数
	 */
	public Integer getCaDishSupDetsCount(List<String> listYearMonth, String startDate,String endDateAddOne,
										 String schName, String dishName, String rmcName,String distName,
										 String caterType, int schType, int schProp, int optMode, String menuName) {
		if(jdbcTemplateHive == null)
			return null;
		final Integer[] dataCounts={0};

		StringBuffer sb = new StringBuffer();
		//sb.append("select count(*) dataCount from ( ");
		sb.append("select count(1) dataCount ");
		sb.append(" from app_t_edu_dish_menu" );
		sb.append(" where 1=1  ");

		getCaDishSupDetsListCondition(listYearMonth, startDate, endDateAddOne, schName, dishName, rmcName, distName,
				caterType, schType, schProp, optMode, menuName, sb);
		//sb.append(" ) totalTable ");
		logger.info("执行sql:"+sb.toString());
		jdbcTemplateHive.query(sb.toString(), new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				dataCounts[0] = rs.getInt("dataCount");
			}
		});
		return dataCounts[0];
	}

	/**
	 * 菜品供应明细查询条件（查询列表和查询总数共用）
	 * @param startYear
	 * @param startMonth
	 * @param startDate
	 * @param endDateAddOne
	 * @param schName
	 * @param dishName
	 * @param rmcName
	 * @param distName
	 * @param caterType
	 * @param schType
	 * @param schProp
	 * @param optMode
	 * @param menuName
	 * @param sb
	 */
	private void getCaDishSupDetsListCondition(List<String> listYearMonth, String startDate, String endDateAddOne,
											   String schName, String dishName, String rmcName, String distName, String caterType, int schType,
											   int schProp, int optMode, String menuName, StringBuffer sb) {
		String [] arrYearMonth ;

		if(listYearMonth.size() > 0) {
			sb.append("AND (");
			for(String strYearMonth : listYearMonth) {
				arrYearMonth = strYearMonth.split("_");
				sb.append(" (year =  \""+arrYearMonth[0]+"\"");
				sb.append(" and month >= \"" +arrYearMonth[1]+"\"");
				sb.append(" and month <= \"" +arrYearMonth[2]+"\") ");

				if(!strYearMonth.equals(listYearMonth.get(listYearMonth.size() - 1))) {
					sb.append(" or ");
				}
			}

			sb.append(") ");
		}

		sb.append(" and supply_date >= \""+startDate+" 00:00:00\"");
		sb.append(" and supply_date < \""+endDateAddOne +" 00:00:00\"");
		sb.append(" and  school_name is not null and school_name !=\"\" and school_name !=\"null\" ");

		//学校
		if(StringUtils.isNotEmpty(schName)) {
			sb.append(" and school_id = \"" + schName+"\"");
		}
		//团餐公司
		if(StringUtils.isNotEmpty(rmcName)) {
			sb.append(" and supplier_id = \"" + rmcName+"\"");
		}
		//区
		if(StringUtils.isNotEmpty(distName)) {
			sb.append(" and area = \"" + distName+"\"");
		}
		//菜品名称
		if(StringUtils.isNotEmpty(dishName)) {
			sb.append(" and dishes_name like  \"" +"%"+dishName+"%"+"\"");
		}

		//餐别
		if(StringUtils.isNotEmpty(caterType)) {
			sb.append(" and cater_type_name = \"" + caterType+"\"");
		}

		//菜单名称
		if(StringUtils.isNotEmpty(menuName)) {
			sb.append(" and menu_group_name = \"" + menuName+"\"");
		}

		//学校学段
		if(schType != -1) {
			sb.append(" and level_name = " + schType);
		}

		//学校学制
		if(schProp != -1) {
			sb.append(" and school_nature_name = \"" + schProp+"\"");
		}

		//经营模式：经营模式（供餐模式，0：学校-自行加工  1：学校-食品加工商 2：外包-现场加工 3：外包-快餐配送
		if(optMode != -1) {
			if(optMode==0 || optMode == 1) {
				sb.append(" and license_main_type = \"0\" ");
				if(optMode ==0) {
					sb.append(" and license_main_child = 0 ");
				}else if (optMode ==1) {
					sb.append(" and license_main_child = 1 ");
				}
			}else if (optMode==2 || optMode == 3) {
				sb.append(" and license_main_type = \"1\" ");
				if(optMode ==2) {
					sb.append(" and license_main_child = 0 ");
				}else if (optMode ==3) {
					sb.append(" and license_main_child = 1 ");
				}
			}

		}
	}

	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据列表
	 */
	public List<CaDishSupStats> getCaDishSupStatsList(List<String> listYearMonth, String startDate,String endDateAddOne,
													  String schName, String dishName,String distName,String dishType,String caterType,
													  Integer startNum,Integer endNum) {
		if(jdbcTemplateHive == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append("select dishes_name dishName,category dishType ,sum( dishes_number ) supNum ");
		sb.append(" from app_t_edu_dish_menu" );
		sb.append(" where  1=1 ");

		//拼接查询条件
		getCaDishSupStatsListCondition(listYearMonth, startDate, endDateAddOne, schName, dishName, distName, dishType, caterType, sb);

		sb.append(" group by dishes_name,category ");
		sb.append(" order by supNum desc ");
		sb.append(" limit "+startNum+","+endNum);
		logger.info("执行sql:"+sb.toString());

		return (List<CaDishSupStats>) jdbcTemplateHive.query(sb.toString(), new RowMapper<CaDishSupStats>(){
			@Override
			public CaDishSupStats mapRow(ResultSet rs, int rowNum) throws SQLException {
				if(rowNum > endNum-(startNum==0?1:startNum)) {
					return null;
				}else {
					CaDishSupStats cdss = new CaDishSupStats();
					cdss.setDishName(rs.getString("dishName"));
					cdss.setDishType(rs.getString("dishType"));
					cdss.setSupNum(rs.getInt("supNum"));
					return cdss;
				}
			}
		});
	}

	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据条数
	 */
	public Integer getCaDishSupStatsCount(List<String> listYearMonth, String startDate,String endDateAddOne,
										  String schName, String dishName,String distName,String dishType,String caterType) {
		if(jdbcTemplateHive == null)
			return null;
		final Integer[] dataCounts={0};

		StringBuffer sb = new StringBuffer();
		sb.append("select count(DISTINCT(dishes_name)) dataCount ");
		sb.append(" from app_t_edu_dish_menu" );
		sb.append(" where 1=1  ");
		getCaDishSupStatsListCondition(listYearMonth, startDate, endDateAddOne, schName, dishName, distName, dishType,
				caterType, sb);

		logger.info("执行sql:"+sb.toString());
		jdbcTemplateHive.query(sb.toString(), new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				dataCounts[0] = rs.getInt("dataCount");
			}
		});
		return dataCounts[0];
	}

	/**
	 * 菜品供应明细查询条件（查询列表和查询总数共用）
	 * @param startYear
	 * @param startMonth
	 * @param startDate
	 * @param endDateAddOne
	 * @param schName
	 * @param dishName
	 * @param rmcName
	 * @param distName
	 * @param caterType
	 * @param schType
	 * @param schProp
	 * @param optMode
	 * @param menuName
	 * @param sb
	 */
	private void getCaDishSupStatsListCondition(List<String> listYearMonth, String startDate, String endDateAddOne,
												String schName, String dishName,String distName,String dishType,String caterType,StringBuffer sb) {
		String [] arrYearMonth ;

		if(listYearMonth.size() > 0) {
			sb.append("AND (");
			for(String strYearMonth : listYearMonth) {
				arrYearMonth = strYearMonth.split("_");
				sb.append(" (year =  \""+arrYearMonth[0]+"\"");
				sb.append(" and month >= \"" +arrYearMonth[1]+"\"");
				sb.append(" and month <= \"" +arrYearMonth[2]+"\") ");

				if(!strYearMonth.equals(listYearMonth.get(listYearMonth.size() - 1))) {
					sb.append(" or ");
				}
			}

			sb.append(") ");
		}

		sb.append(" and supply_date >= \""+startDate+" 00:00:00\"");
		sb.append(" and supply_date < \""+endDateAddOne +" 00:00:00\"");
		sb.append(" and  school_name is not null and school_name !=\"\" and school_name !=\"null\" ");

		//学校
		if(StringUtils.isNotEmpty(schName)) {
			sb.append(" and school_id = \"" + schName+"\"");
		}
		//区
		if(StringUtils.isNotEmpty(distName)) {
			sb.append(" and area = \"" + distName+"\"");
		}
		//菜品名称
		if(StringUtils.isNotEmpty(dishName)) {
			sb.append(" and dishes_name like  \"" +"%"+dishName+"%"+"\"");
		}

		//餐别
		if(StringUtils.isNotEmpty(caterType)) {
			sb.append(" and cater_type_name = \"" + caterType+"\"");
		}

		//类别
		if(StringUtils.isNotEmpty(dishType)) {
			sb.append(" and category = \"" + dishType+"\"");
		}
	}

	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_total中根据条件查询菜品汇总数据列表
	 */
	public List<CaDishSupStats> getCaDishSupStatsListFromTotal(List<String> listYearMonth, String startDate,String endDateAddOne,
															   String dishName,String distName,String dishType,String caterType,
															   Integer startNum,Integer endNum) {
		if(jdbcTemplateHive == null)
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append("select dishes_name dishName,category dishType ,sum( dishes_number ) supNum ");
		sb.append(" from app_t_edu_dish_total" );
		sb.append(" where  1=1 ");

		//拼接查询条件
		getCaDishSupStatsListConditionFromTotal(listYearMonth, startDate, endDateAddOne, dishName, distName, dishType, caterType, sb);
		sb.append(" group by dishes_name,category ");
		sb.append(" order by supNum desc ");
		sb.append(" limit "+startNum+","+endNum);
		logger.info("执行sql:"+sb.toString());

		return (List<CaDishSupStats>) jdbcTemplateHive.query(sb.toString(), new RowMapper<CaDishSupStats>(){
			@Override
			public CaDishSupStats mapRow(ResultSet rs, int rowNum) throws SQLException {
				if(rowNum > endNum-(startNum==0?1:startNum)) {
					return null;
				}else {
					CaDishSupStats cdss = new CaDishSupStats();
					cdss.setDishName(rs.getString("dishName"));
					cdss.setDishType(rs.getString("dishType"));
					cdss.setSupNum(rs.getInt("supNum"));
					return cdss;
				}
			}
		});
	}

	/**
	 * 从数据库app_saas_v1的数据表app_t_edu_dish_menu中根据条件查询菜品汇总数据条数
	 */
	public Integer getCaDishSupStatsCountFromTotal(List<String> listYearMonth, String startDate,String endDateAddOne,
												   String dishName,String distName,String dishType,String caterType) {
		if(jdbcTemplateHive == null)
			return null;
		final Integer[] dataCounts={0};

		StringBuffer sb = new StringBuffer();
		sb.append("select count(dishes_name) dataCount ");
		sb.append(" from app_t_edu_dish_total" );
		sb.append(" where 1=1  ");
		getCaDishSupStatsListConditionFromTotal(listYearMonth, startDate, endDateAddOne, dishName, distName, dishType,
				caterType, sb);

		logger.info("执行sql:"+sb.toString());
		jdbcTemplateHive.query(sb.toString(), new RowCallbackHandler() {
			public void processRow(ResultSet rs) throws SQLException {
				dataCounts[0] = rs.getInt("dataCount");
			}
		});
		return dataCounts[0];
	}

	/**
	 * 菜品供应明细查询条件（查询列表和查询总数共用）
	 * @param startYear
	 * @param startMonth
	 * @param startDate
	 * @param endDateAddOne
	 * @param schName
	 * @param dishName
	 * @param rmcName
	 * @param distName
	 * @param caterType
	 * @param schType
	 * @param schProp
	 * @param optMode
	 * @param menuName
	 * @param sb
	 */
	private void getCaDishSupStatsListConditionFromTotal(List<String> listYearMonth, String startDate, String endDateAddOne,
														 String dishName,String distName,String dishType,String caterType,StringBuffer sb) {
		String [] arrYearMonth ;

		if(listYearMonth.size() > 0) {
			sb.append("AND (");
			for(String strYearMonth : listYearMonth) {
				arrYearMonth = strYearMonth.split("_");
				sb.append(" (year =  \""+arrYearMonth[0]+"\"");
				sb.append(" and month >= \"" +arrYearMonth[1]+"\"");
				sb.append(" and month <= \"" +arrYearMonth[2]+"\") ");

				if(!strYearMonth.equals(listYearMonth.get(listYearMonth.size() - 1))) {
					sb.append(" or ");
				}
			}

			sb.append(") ");
		}

		sb.append(" and supply_date >= \""+startDate+" 00:00:00\"");
		sb.append(" and supply_date < \""+endDateAddOne +" 00:00:00\"");
		//区
		if(StringUtils.isNotEmpty(distName)) {
			sb.append(" and area = \"" + distName+"\"");
		}
		//菜品名称
		if(StringUtils.isNotEmpty(dishName)) {
			sb.append(" and dishes_name like  \"" +"%"+dishName+"%"+"\"");
		}

		//餐别
		if(StringUtils.isNotEmpty(caterType)) {
			sb.append(" and cater_type_name = \"" + caterType+"\"");
		}

		//类别
		if(StringUtils.isNotEmpty(dishType)) {
			sb.append(" and category = \"" + dishType+"\"");
		}
	}
}
