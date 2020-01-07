package com.tfit.BdBiProcSrvYgwcSchOmc.service.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.AppOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CheckOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.DishOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.DbHiveDishService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 排菜相关hive库的查询
 * @author Administrator
 *
 */
@Service
public class DbHiveDishServiceImpl implements DbHiveDishService {
	private static final Logger logger = LogManager.getLogger(DbHiveDishServiceImpl.class.getName());
	
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
  		if(initProcFlag){
			return ;
		}
  		initProcFlag = true;
  		logger.info("定时建立与 DataSource数据源dsHive对象表示的数据源的连接，时间：" + BCDTimeUtil.convertNormalFrom(null));
  		jdbcTemplateHive = new JdbcTemplate(dataSourceHive);
  		
  		jdbcTemplateHive2 = new JdbcTemplate(dataSourceHive2);
  	}
  	

    /** 
     * @Description: 排菜操作率
     * @Param: [startDate, endDate] 
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao> 
     * @Author: jianghy 
     * @Date: 2019/12/28
     * @Time: 18:20
     */
	@Override
	public List<DishOperateRateInfo> getDishOperateRateList(String startDate, String endDate) {
		JdbcTemplate jdbcTemplateTemp = DbHiveServiceImpl.getJdbcTemplateHive(jdbcTemplateHive, jdbcTemplateHive2, dataSourceHive);
		if(jdbcTemplateTemp == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(" select                                                                                                               ");
		//日期
		sb.append(" use_date as dishDate,                                           ");
		//区域
		sb.append(" area,                                                                                                                ");
		//学制
		sb.append(" case when ta1.level_name in ('0','1','2','3','4','5','6') then '幼托'                                                ");
		sb.append("      when ta1.level_name in ('7','8','9','10','11','12','13') then '中小学'                                          ");
		sb.append("      else '其他'                                                                                                     ");
		sb.append("      end as schType,                                                                                                 ");
		//应排菜数量
		sb.append(" case when ta1.have_class ='1' and (ta1.have_platoon is null or ta1.have_platoon ='null' and ta1.have_platoon='null') ");
		sb.append("      then total else 0 end as needDishSchoolNum,                                                                    ");
		//已排菜数量
		sb.append(" case when ta1.have_class ='1' and  ta1.have_platoon ='1'                                                             ");
		sb.append("      then total else 0 end as haveDishSchoolNum,                                                                      ");
		//未排菜数量
		sb.append(" case when ta1.have_class ='1' and  ta1.have_platoon='0'                                                              ");
		sb.append("      then total else 0 end as noDishSchoolNum,                                                                     ");
		//学校数量
		sb.append(" case when (ta1.have_class is null or ta1.have_class ='null' and ta1.have_class='null')                               ");
		sb.append("      then total else 0 end as schoolNum                                                                        ");
		sb.append(" from app_t_edu_platoon_total_d ta1                                                                                   ");
		sb.append(" where 1=1                                                                                                            ");
		sb.append(" and ta1.area is not null                                                                                             ");
		//上海市
		sb.append(" and ta1.department_id='-1'                                                                     ");
		//开始日期
		sb.append(" and ta1.use_date>='"+startDate+"'                                                                 ");
		//结束日期
		sb.append(" and ta1.use_date<='"+endDate+"'                                                                 ");
		//取各区分学制
		sb.append(" and (level_name is not null or ta1.level_name <>'null' and ta1.level_name<>'null')                                   ");
		logger.info("排菜操作率:"+ sb.toString());
		return (List<DishOperateRateInfo>)jdbcTemplateTemp.query(sb.toString(), new RowMapper<DishOperateRateInfo>() {
			@Override
			public DishOperateRateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				DishOperateRateInfo dishOperateRateInfo = new DishOperateRateInfo();
				dishOperateRateInfo.setDishDate(rs.getString("dishDate"));
				dishOperateRateInfo.setArea(rs.getString("area"));
				dishOperateRateInfo.setSchType(rs.getString("schType"));
				dishOperateRateInfo.setNeedDishSchoolNum(rs.getInt("needDishSchoolNum"));
				dishOperateRateInfo.setHaveDishSchoolNum(rs.getInt("haveDishSchoolNum"));
				dishOperateRateInfo.setNoDishSchoolNum(rs.getInt("noDishSchoolNum"));
				dishOperateRateInfo.setSchoolNum(rs.getInt("schoolNum"));
				return dishOperateRateInfo;
			}
		});
	}


	/** 
	 * @Description: 排菜准确率
	 * @Param: [startDate, endDate] 
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao> 
	 * @Author: jianghy 
	 * @Date: 2019/12/28
	 * @Time: 19:56       
	 */
	@Override
	public List<DishOperateRateInfo> getDishCorrectRateList(String startDate, String endDate) {
		JdbcTemplate jdbcTemplateTemp = DbHiveServiceImpl.getJdbcTemplateHive(jdbcTemplateHive, jdbcTemplateHive2, dataSourceHive);
		if(jdbcTemplateTemp == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(" select                                                                                                              ");
		sb.append(" from_unixtime(unix_timestamp(supply_date,'yyyy-MM-dd'),'yyyy-MM-dd') as dishDate,                                                                                            ");
		sb.append(" area,                                                                                                               ");
		sb.append(" case when t.level_name in ('0','1','2','3','4','5','6') then '幼托'                                                 ");
		sb.append("      when t.level_name in ('7','8','9','10','11','12','13') then '中小学'                                           ");
		sb.append("      else '其他'                                                                                                    ");
		sb.append("      end as schType,                                                                                                ");
		sb.append(" 1 as dishNum,                                                                                                       ");
		sb.append(" case when (dishes_number/meals_count>=0.8 and dishes_number/meals_count<=1.2) then 1 else 0 end as dishCorrectNum, ");
		sb.append(" case when (dishes_number/meals_count>=0.8 and dishes_number/meals_count<=1.2) then 0 else 1 end as dishUnCorrectNum");
		sb.append(" from app_t_edu_dish_menu t                                                                                          ");
		sb.append(" where 1=1                                                                                                           ");
		sb.append(" and (to_date(supply_date) >= '"+startDate+"' and to_date(supply_date) <= '"+endDate+"')                                 ");
		sb.append(" and area is not null                                                                                                ");
		sb.append(" and level_name is not null                                                                                          ");
		logger.info("排菜准确率:"+ sb.toString());
		return jdbcTemplateTemp.query(sb.toString(), new RowMapper<DishOperateRateInfo>() {
			@Override
			public DishOperateRateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				DishOperateRateInfo dishOperateRateInfo = new DishOperateRateInfo();
				dishOperateRateInfo.setDishDate(rs.getString("dishDate"));
				dishOperateRateInfo.setArea(rs.getString("area"));
				dishOperateRateInfo.setSchType(rs.getString("schType"));
				dishOperateRateInfo.setDishNum(rs.getInt("dishNum"));
				dishOperateRateInfo.setDishCorrectNum(rs.getInt("dishCorrectNum"));
				dishOperateRateInfo.setDishUnCorrectNum(rs.getInt("dishUnCorrectNum"));
				return dishOperateRateInfo;
			}
		});
	}


	/** 
	 * @Description: 验收操作率 
	 * @Param: [startDate, endDate] 
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao> 
	 * @Author: jianghy 
	 * @Date: 2019/12/29
	 * @Time: 14:25       
	 */
	@Override
	public List<CheckOperateRateInfo> getCheckOperateRateList(String startDate, String endDate) {
		JdbcTemplate jdbcTemplateTemp = DbHiveServiceImpl.getJdbcTemplateHive(jdbcTemplateHive, jdbcTemplateHive2, dataSourceHive);
		if(jdbcTemplateTemp == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(" select                                                                                   ");
		sb.append(" action_date as dishDate,                                                                 ");
		sb.append(" area,                                                                                    ");
		sb.append(" case when ta1.level_name in ('0','1','2','3','4','5','6') then '幼托'                    ");
		sb.append("      when ta1.level_name in ('7','8','9','10','11','12','13') then '中小学'              ");
		sb.append("      else '其他'                                                                         ");
		sb.append("      end as schType,                                                                     ");
		sb.append(" case when (ta1.haul_status is null or ta1.haul_status ='null' and ta1.haul_status='null')");
		sb.append("      then ta1.school_total else 0 end  as needCheckSchoolNum,                            ");
		sb.append(" case when ta1.haul_status= 3                                                             ");
		sb.append("      then ta1.school_total else 0 end  as haveCheckSchoolNum,                            ");
		sb.append(" case when (ta1.haul_status = -4 or ta1.haul_status = -2 or ta1.haul_status = -1          ");
		sb.append(" 	 or ta1.haul_status = 0 or ta1.haul_status = 1 or ta1.haul_status = 2)                  ");
		sb.append("      then ta1.school_total else 0 end  as noCheckSchoolNum                               ");
		sb.append(" from app_t_edu_ledger_master_total_d ta1                                                 ");
		sb.append(" where 1=1                                                                                ");
		sb.append(" and ta1.area is not null                                                                 ");
		sb.append(" and ta1.department_id='-1'                                                               ");
		sb.append(" and ta1.action_date>='"+startDate+"'                                                        ");
		sb.append(" and ta1.action_date<='"+endDate+"'                                                       ");
		sb.append(" and (level_name is not null or ta1.level_name <>'null' and ta1.level_name<>'null')       ");
		logger.info("验收操作率:"+ sb.toString());
		return jdbcTemplateTemp.query(sb.toString(), new RowMapper<CheckOperateRateInfo>() {
			@Override
			public CheckOperateRateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				CheckOperateRateInfo checkOperateRateInfo = new CheckOperateRateInfo();
				checkOperateRateInfo.setDishDate(rs.getString("dishDate"));
				checkOperateRateInfo.setArea(rs.getString("area"));
				checkOperateRateInfo.setSchType(rs.getString("schType"));
				checkOperateRateInfo.setNeedCheckSchoolNum(rs.getInt("needCheckSchoolNum"));
				checkOperateRateInfo.setHaveCheckSchoolNum(rs.getInt("haveCheckSchoolNum"));
				checkOperateRateInfo.setNoCheckSchoolNum(rs.getInt("noCheckSchoolNum"));
				return checkOperateRateInfo;
			}
		});
	}

	
	/** 
	 * @Description: 验收准确率
	 * @Param: [startDate, endDate] 
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao> 
	 * @Author: jianghy 
	 * @Date: 2019/12/29
	 * @Time: 14:25       
	 */
	@Override
	public List<CheckOperateRateInfo> getCheckCorrectRateList(String startDate, String endDate) {
		JdbcTemplate jdbcTemplateTemp = DbHiveServiceImpl.getJdbcTemplateHive(jdbcTemplateHive, jdbcTemplateHive2, dataSourceHive);
		if(jdbcTemplateTemp == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(" select                                                                          ");
		sb.append(" from_unixtime(unix_timestamp(delivery_date,'yyyy-MM-dd'),'yyyy-MM-dd') as dishDate,                                         ");
		sb.append(" ta1.area,                                                                       ");
		sb.append(" case when ta1.level_name in ('0','1','2','3','4','5','6') then '幼托'           ");
		sb.append("      when ta1.level_name in ('7','8','9','10','11','12','13') then '中小学'     ");
		sb.append("      else '其他'                                                                ");
		sb.append("      end as schType,                                                            ");
		sb.append(" coalesce(ta1.actual_quantity,0) as materialNum,                                 ");
		sb.append(" case when coalesce(ta1.delivery_number,0)/coalesce(ta1.actual_quantity,0) >=0.8 ");
		sb.append("      and coalesce(ta1.delivery_number,0)/coalesce(ta1.actual_quantity,0)<=1.2   ");
		sb.append("      then coalesce(ta1.actual_quantity,0) else 0 end as checkCorrectNum,         ");
		sb.append(" case when coalesce(ta1.delivery_number,0)/coalesce(ta1.actual_quantity,0) >=0.8 ");
		sb.append("      and coalesce(ta1.delivery_number,0)/coalesce(ta1.actual_quantity,0)<=1.2   ");
		sb.append("      then 0 else coalesce(ta1.actual_quantity,0) end as checkUnCorrectNum       ");
		sb.append(" from app_t_edu_ledege_detail ta1                                                ");
		sb.append(" where ta1.delivery_date is not null                                             ");
		sb.append(" and to_date(ta1.delivery_date) >='"+startDate+"'                                   ");
		sb.append(" and to_date(ta1.delivery_date) <='"+endDate+"'                                   ");
		logger.info("验收准确率:"+ sb.toString());
		return jdbcTemplateTemp.query(sb.toString(), new RowMapper<CheckOperateRateInfo>() {
			@Override
			public CheckOperateRateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				CheckOperateRateInfo checkOperateRateInfo = new CheckOperateRateInfo();
				checkOperateRateInfo.setDishDate(rs.getString("actionDate"));
				checkOperateRateInfo.setArea(rs.getString("area"));
				checkOperateRateInfo.setSchType(rs.getString("schType"));
				checkOperateRateInfo.setMaterialNum(rs.getInt("materialNum"));
				checkOperateRateInfo.setCheckCorrectNum(rs.getInt("checkCorrectNum"));
				checkOperateRateInfo.setCheckUnCorrectNum(rs.getInt("checkUnCorrectNum"));
				return checkOperateRateInfo;
			}
		});
	}


	/**
	 * @Description: app操作率
	 * @Param: [startDate, endDate]
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao>
	 * @Author: jianghy
	 * @Date: 2019/12/29
	 * @Time: 14:35
	 */
	@Override
	public List<AppOperateRateInfo> getAppOperateRateList(String startDate, String endDate) {
		JdbcTemplate jdbcTemplateTemp = DbHiveServiceImpl.getJdbcTemplateHive(jdbcTemplateHive, jdbcTemplateHive2, dataSourceHive);
		if(jdbcTemplateTemp == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(" select                                                                                   ");
		sb.append(" ta1.action_date dishDate,                                                                ");
		sb.append(" ta1.department_master_id area,                                                           ");
		sb.append(" case when ta1.level_name in ('0','1','2','3','4','5','6') then '幼托'                    ");
		sb.append(" 	 when ta1.level_name in ('7','8','9','10','11','12','13') then '中小学'                 ");
		sb.append(" 	 else '其他'                                                                            ");
		sb.append(" 	 end as schType,                                                                        ");
		sb.append(" 1 as needOrderNum,                                                                       ");
		sb.append(" case when (ta1.start_lat is not null  or ta1.start_lat<>'null' or ta1.start_lat<>'null' )");
		sb.append(" 		  or (ta1.start_lng is not null  or ta1.start_lng<>'null' or ta1.start_lng<>'null' )");
		sb.append(" 		  or (ta1.end_lat is not null  or ta1.end_lat<>'null' or ta1.end_lat<>'null' )      ");
		sb.append(" 		  or (ta1.end_lng is not null  or ta1.end_lng<>'null' or ta1.end_lng<>'null' )      ");
		sb.append(" 	 then 1                                                                                 ");
		sb.append("      else 0                                                                              ");
		sb.append(" 	 end as haveOrderNum,                                                                   ");
		sb.append(" case when (ta1.start_lat is not null  or ta1.start_lat<>'null' or ta1.start_lat<>'null' )");
		sb.append(" 		  or (ta1.start_lng is not null  or ta1.start_lng<>'null' or ta1.start_lng<>'null' )");
		sb.append(" 		  or (ta1.end_lat is not null  or ta1.end_lat<>'null' or ta1.end_lat<>'null' )      ");
		sb.append(" 		  or (ta1.end_lng is not null  or ta1.end_lng<>'null' or ta1.end_lng<>'null' )      ");
		sb.append(" 	 then 0                                                                                 ");
		sb.append("      else 1                                                                              ");
		sb.append(" 	 end as noOrderNum                                                                     ");
		sb.append(" from                                                                                     ");
		sb.append(" app_t_edu_ledger_master_detail ta1                                                       ");
		sb.append(" where ta1.action_date>'"+startDate+"'                                                     ");
		sb.append(" and ta1.action_date<='"+endDate+"'                                                         ");
		sb.append(" and (ta1.level_name is not null or ta1.level_name<>'null' or ta1.level_name<>'null')     ");
		logger.info("app操作率:"+ sb.toString());
		return jdbcTemplateTemp.query(sb.toString(), new RowMapper<AppOperateRateInfo>() {
			@Override
			public AppOperateRateInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				AppOperateRateInfo appOperateRateInfo = new AppOperateRateInfo();
				appOperateRateInfo.setDishDate(rs.getString("dishDate"));
				appOperateRateInfo.setArea(rs.getString("area"));
				appOperateRateInfo.setSchType(rs.getString("schType"));
				appOperateRateInfo.setNeedOrderNum(rs.getInt("needOrderNum"));
				appOperateRateInfo.setHaveOrderNum(rs.getInt("haveOrderNum"));
				appOperateRateInfo.setNoOrderNum(rs.getInt("noOrderNum"));
				return appOperateRateInfo;
			}
		});
	}
}
