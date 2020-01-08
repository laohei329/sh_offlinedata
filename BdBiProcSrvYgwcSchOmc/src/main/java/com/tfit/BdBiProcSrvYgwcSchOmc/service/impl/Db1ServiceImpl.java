package com.tfit.BdBiProcSrvYgwcSchOmc.service.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.ApplicationUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.AppCommonDao;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.*;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edu.TEduDistrictV2DoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edu.TEduSchoolDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Service
public class Db1ServiceImpl implements Db1Service {
	private static final Logger logger = LogManager.getLogger(Db1ServiceImpl.class.getName());
	
	@Autowired
	TEduDistrictV2DoMapper tedv2DoMapper;
	
	@Autowired
	TEduSchoolDoMapper tesDoMapper;
	
	//额外数据源1
	@Autowired
	@Qualifier("ds1")
	DataSource dataSource1;
	//额外数据源1连接模板
	JdbcTemplate jdbcTemplate1 = null;
	
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
  		logger.info("定时建立与 DataSource数据源ds1对象表示的数据源的连接，时间：" + BCDTimeUtil.convertNormalFrom(null));
  		jdbcTemplate1 = new JdbcTemplate(dataSource1);
  	}
  	
  	//从数据源ds1的数据表t_edu_district中查找id和区域名称
    public List<TEduDistrictDo> getListByDs1IdName() {
    	if(mybatisUseFlag)
    		return tedv2DoMapper.getDistIdNameListByDs1();
    	else {
    		if(jdbcTemplate1 == null)
    			return null;
    		String sql = "select distinct id,name from t_edu_district";
    		return (List<TEduDistrictDo>) jdbcTemplate1.query(sql, new RowMapper<TEduDistrictDo>() {

    			@Override
    			public TEduDistrictDo mapRow(ResultSet rs, int rowNum) throws SQLException {
    				TEduDistrictDo tdd = new TEduDistrictDo();
    				tdd.setId(rs.getString("id"));
    				tdd.setName(rs.getString("name"));
                
    				return tdd;
    			}
    		});
    	}        
    }
    
    //从数据源ds1的数据表t_edu_school中查找canteen_mode以id
    public SchOptModeDo getSchOptModeByDs1Id(String id){
    	if(jdbcTemplate1 == null)
    		return null;
    	SchOptModeDo somDo = new SchOptModeDo(); 
        String sql = "select canteen_mode from t_edu_school where id = " + "'" + id + "'" + " and stat = 1 and reviewed = 1";
        jdbcTemplate1.query(sql, new RowCallbackHandler(){
        	public void processRow(ResultSet rs) throws SQLException{
        		somDo.setCanteenMode(rs.getInt("canteen_mode"));
        	}
        });
        int curOptMode = somDo.getCanteenMode();
        if(curOptMode == 0)
        	somDo.setStrCanteenMode("Self_support");
        else if(curOptMode == 1)
        	somDo.setStrCanteenMode("outsource");
        else if(curOptMode == 2)
        	somDo.setStrCanteenMode("blend");
        else
        	somDo.setStrCanteenMode("");
        
        return somDo;
    }
    
    //从数据源ds1的数据表t_edu_school中查找school_nature以id
    public SchOwnershipDo getSchOwnByDs1Id(String id){
    	if(jdbcTemplate1 == null)
    		return null;
    	SchOwnershipDo soDo = new SchOwnershipDo(); 
    	String[] schOwnTypes = { "Domestic_public_office", "Nation_public_office", "Domestic_civilian_office", "Nation_civilian_office" };
        String sql = "select school_nature from t_edu_school where id = " + "'" + id + "'" + " and stat = 1 and reviewed = 1";
        jdbcTemplate1.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		soDo.setSchoolNature(rs.getString("school_nature"));
        	}   
        });
        String curStrOwn = soDo.getSchoolNature();
        int curOwn = 0;
        if(curStrOwn != null) {
        	if(!curStrOwn.isEmpty()) {
        		String[] curStrOwns = curStrOwn.split(",");
        		curOwn = Integer.valueOf(curStrOwns[0]);
        	}
        }
        if(curOwn == 0)
        	soDo.setStrOwnership(schOwnTypes[0]);
        else if(curOwn == 1)
        	soDo.setStrOwnership(schOwnTypes[2]);
        else if(curOwn == 2)
        	soDo.setStrOwnership(schOwnTypes[1]);
        else if(curOwn == 3)
        	soDo.setStrOwnership(schOwnTypes[3]);
        
        return soDo;
    }
    
    //从数据源ds1的数据表t_edu_school中查找level以id
    public SchTypeDo getSchTypeByDs1Id(String id){
    	if(jdbcTemplate1 == null)
    		return null;
    	SchTypeDo stDo = new SchTypeDo(); 
    	String[] schTypes = { "Nursery", "Kindergarten", "Primary_school", "middle_school", "high_school", "Vocational_school", "Other" };
        String sql = "select level from t_edu_school where id = " + "'" + id + "'" + " and stat = 1 and reviewed = 1";
        jdbcTemplate1.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		stDo.setLevel(rs.getString("level"));
        	}   
        });
        String curSchType = stDo.getLevel();
        String [] curSchTypes = curSchType.split(",");
        for(int i = 0; i < curSchTypes.length; i++) {
        	if(curSchTypes[i].compareTo("0") == 0)
        		stDo.setSchType(schTypes[1]);
        	else if(curSchTypes[i].compareTo("1") == 0)
        		stDo.setSchType(schTypes[2]);
        	else if(curSchTypes[i].compareTo("2") == 0)
        		stDo.setSchType(schTypes[3]);
        	else if(curSchTypes[i].compareTo("3") == 0)
        		stDo.setSchType(schTypes[4]);
        	else if(curSchTypes[i].compareTo("6") == 0)
        		stDo.setSchType(schTypes[5]);
        	else if(curSchTypes[i].compareTo("7") == 0)
        		stDo.setSchType(schTypes[0]);
        	else if(curSchTypes[i].compareTo("9") == 0)
        		stDo.setSchType(schTypes[6]);
        }
        
        return stDo;
    }
    
    //从数据源ds1的数据表t_edu_school中查找所有id以区域ID（空时在查询所有）
    public List<SchIdNameDo> getSchIdListByDs1(String distId) {
    	if(mybatisUseFlag) {
    		if(distId == null)
    			return tesDoMapper.getSchIdListByDs1();
    		else
    			return tesDoMapper.getSchIdListByDs1DistId(distId);
    	}
    	else {
    		if(jdbcTemplate1 == null)
    			return null;
    		String sql = null;
    		if(distId == null)
    			sql = "select distinct id, school_name from t_edu_school" + " where stat = 1 and reviewed = 1";
    		else
    			sql = "select distinct id, school_name from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
    		logger.info("sql语句：" + sql);
    		return (List<SchIdNameDo>) jdbcTemplate1.query(sql, new RowMapper<SchIdNameDo>(){

    			@Override
    			public SchIdNameDo mapRow(ResultSet rs, int rowNum) throws SQLException {
    				SchIdNameDo siDo = new SchIdNameDo();
    				siDo.setId(rs.getString("id"));
    				siDo.setName(rs.getString("school_name"));
    				return siDo;
    			}
    		});
    	}
    }
    
    //从数据源ds1的数据表t_edu_school中查找所有id以区域ID（空时在查询所有）和输出字段方法
    public List<TEduSchoolDo> getTEduSchoolDoListByDs1(String distId, int outMethod) {
    	if(jdbcTemplate1 == null)
    		return null;
        String sql = null;
        if(distId == null) {
        	if(outMethod == 0)        //输出学校ID、学校名称（项目点名称）
        		sql = "select distinct id, school_name, area from t_edu_school" + " where stat = 1 and reviewed = 1";
        	else if(outMethod == 1)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、学校性质、食堂经营模式、证件主体、供餐模式
        		sql = "select distinct id, school_name, area, level, school_nature, canteen_mode, ledger_type, level2, license_main_type, license_main_child from t_edu_school" + " where stat = 1 and reviewed = 1";
        	else if(outMethod == 2)   //输出输出学校ID、学校名称（项目点名称）、详细地址、区域ID、学校学制、学校性质、食堂经营模式、证件主体、供餐模式
        		sql = "select distinct id, school_name, address, area, level, school_nature, canteen_mode, ledger_type, level2, license_main_type, license_main_child from t_edu_school" + " where stat = 1 and reviewed = 1";
        	else if(outMethod == 3)   //输出输出学校ID、学校名称（项目点名称）、详细地址、区域ID、学校学制、学校性质、食堂经营模式、总校ID、食品许可证件主体的类型(1学校|2外包)、供餐模式:20181024新增:1自行加工,2食品加工商3快餐配送,4现场加工
        		sql = "select distinct id, school_name, address, area, level, school_nature, canteen_mode, ledger_type, level2, parent_id, license_main_type, license_main_child, school_area_id from t_edu_school" + " where stat = 1 and reviewed = 1";
        	else if(outMethod == 4)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、所属区、所属级别、主管部门、证件主体、供餐模式
        		sql = "select distinct id, school_name, area, level, level2, school_area_id, department_master_id, department_slave_id, license_main_type, license_main_child from t_edu_school" + " where stat = 1 and reviewed = 1";
        	else if(outMethod == 5)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、学校性质、所属区ID、所属级别、主管部门、是否分校、关联的总校、证件主体、供餐模式
        		sql = "select distinct id, school_name, area, level, level2, school_nature, school_area_id, department_master_id, department_slave_id,"
        				+ " is_branch_school, parent_id, license_main_type, license_main_child,department_head as departmentHead ,department_mobilephone as departmentMobilephone,address "
        				+ "from t_edu_school" + " where stat = 1 and reviewed = 1";
        	else if(outMethod == 6)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、所属区、所属级别、主管部门、部门负责人、部门邮件
        		sql = "select distinct id, school_name, area, level, level2, school_area_id, department_master_id, department_slave_id, department_head, department_mobilephone, department_email from t_edu_school" + " where stat = 1 and reviewed = 1";
        }
        else {
        	if(outMethod == 0)        //输出学校ID、学校名称（项目点名称）
        		sql = "select distinct id, school_name, area from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        	else if(outMethod == 1)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、学校性质、食堂经营模式、证件主体、供餐模式
        		sql = "select distinct id, school_name, area, level, school_nature, canteen_mode, ledger_type, level2, license_main_type, license_main_child from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        	else if(outMethod == 2)   //输出输出学校ID、学校名称（项目点名称）、详细地址、区域ID、学校学制、学校性质、食堂经营模式、证件主体、供餐模式
        		sql = "select distinct id, school_name, address, area, level, school_nature, canteen_mode, ledger_type, level2, license_main_type, license_main_child from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        	else if(outMethod == 3)   //输出输出学校ID、学校名称（项目点名称）、详细地址、区域ID、学校学制、学校性质、食堂经营模式、总校ID、食品许可证件主体的类型(1学校|2外包)、供餐模式:20181024新增:1自行加工,2食品加工商3快餐配送,4现场加工
        		sql = "select distinct id, school_name, address, area, level, school_nature, canteen_mode, ledger_type, level2, parent_id, license_main_type, license_main_child, school_area_id from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        	else if(outMethod == 4)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、所属区、所属级别、主管部门、证件主体、供餐模式
        		sql = "select distinct id, school_name, area, level, level2, school_area_id, department_master_id, department_slave_id, license_main_type, license_main_child from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        	else if(outMethod == 5)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、学校性质、所属区ID、所属级别、主管部门、是否分校、关联的总校、证件主体、供餐模式
        		sql = "select distinct id, school_name, area, level, level2, school_nature, school_area_id, department_master_id, department_slave_id, "
        				+ "is_branch_school, parent_id, license_main_type, license_main_child ,department_head as departmentHead ,department_mobilephone as departmentMobilephone,address "
        				+ "from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        	else if(outMethod == 6)   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、所属区、所属级别、主管部门、部门负责人、部门邮件
        		sql = "select distinct id, school_name, area, level, level2, school_area_id, department_master_id, department_slave_id, department_head, department_mobilephone, department_email from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1 and reviewed = 1";
        }
        logger.info("sql语句：" + sql);
        return (List<TEduSchoolDo>) jdbcTemplate1.query(sql, new RowMapper<TEduSchoolDo>(){

            @Override
            public TEduSchoolDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduSchoolDo tesDo = new TEduSchoolDo();
            	if(outMethod == 0) {      //输出学校ID、学校名称（项目点名称）
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            	}
            	else if(outMethod == 1) {   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、学校性质、食堂经营模式
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            		tesDo.setLevel(rs.getString("level"));
            		tesDo.setSchoolNature(rs.getString("school_nature"));
            		tesDo.setCanteenMode(rs.getShort("canteen_mode"));
            		tesDo.setLedgerType(rs.getString("ledger_type"));
            		if(rs.getObject("level2") != null)
            			tesDo.setLevel2(rs.getInt("level2"));
            		tesDo.setLicenseMainType(rs.getString("license_main_type"));
            		if(rs.getObject("license_main_child") != null)
            			tesDo.setLicenseMainChild(rs.getShort("license_main_child"));
            	}
            	else if(outMethod == 2) {   //输出输出学校ID、学校名称（项目点名称）、详细地址、区域ID、学校学制、学校性质、食堂经营模式
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            		tesDo.setAddress(rs.getString("address"));
            		tesDo.setLevel(rs.getString("level"));
            		tesDo.setSchoolNature(rs.getString("school_nature"));
            		tesDo.setCanteenMode(rs.getShort("canteen_mode"));
            		tesDo.setLedgerType(rs.getString("ledger_type"));
            		if(rs.getObject("level2") != null)
            			tesDo.setLevel2(rs.getInt("level2"));
            		tesDo.setLicenseMainType(rs.getString("license_main_type"));
            		if(rs.getObject("license_main_child") != null)
            			tesDo.setLicenseMainChild(rs.getShort("license_main_child"));
            	}
            	else if(outMethod == 3) {   //输出输出学校ID、学校名称（项目点名称）、详细地址、区域ID、学校学制、学校性质、食堂经营模式、总校ID
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            		tesDo.setAddress(rs.getString("address"));
            		tesDo.setLevel(rs.getString("level"));
            		tesDo.setSchoolNature(rs.getString("school_nature"));
            		tesDo.setCanteenMode(rs.getShort("canteen_mode"));
            		tesDo.setLedgerType(rs.getString("ledger_type"));
            		if(rs.getObject("level2") != null)
            			tesDo.setLevel2(rs.getInt("level2"));
            		tesDo.setParentId(rs.getString("parent_id"));
            		tesDo.setLicenseMainType(rs.getString("license_main_type"));
            		if(rs.getObject("license_main_child") != null)
            			tesDo.setLicenseMainChild(rs.getShort("license_main_child"));
            		tesDo.setSchoolAreaId(rs.getString("school_area_id"));
            	}
            	else if(outMethod == 4) {   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、所属区、所属级别、主管部门
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            		tesDo.setLevel(rs.getString("level"));
            		if(rs.getObject("level2") != null)
            			tesDo.setLevel2(rs.getInt("level2"));
            		tesDo.setSchoolAreaId(rs.getString("school_area_id"));            		
            		tesDo.setDepartmentMasterId(rs.getString("department_master_id"));
            		tesDo.setDepartmentSlaveId(rs.getString("department_slave_id"));
            		tesDo.setLicenseMainType(rs.getString("license_main_type"));
            		if(rs.getObject("license_main_child") != null)
            			tesDo.setLicenseMainChild(rs.getShort("license_main_child"));
            	}
            	else if(outMethod == 5) {   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、学校性质、所属区ID、所属级别、主管部门、是否分校、关联的总校、证件主体
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            		tesDo.setLevel(rs.getString("level"));
            		if(rs.getObject("level2") != null)
            			tesDo.setLevel2(rs.getInt("level2"));
            		tesDo.setSchoolNature(rs.getString("school_nature"));
            		tesDo.setSchoolAreaId(rs.getString("school_area_id"));            		
            		tesDo.setDepartmentMasterId(rs.getString("department_master_id"));
            		tesDo.setDepartmentSlaveId(rs.getString("department_slave_id"));
            		if(rs.getObject("is_branch_school") != null)
            			tesDo.setIsBranchSchool(rs.getInt("is_branch_school"));
            		tesDo.setParentId(rs.getString("parent_id"));
            		tesDo.setLicenseMainType(rs.getString("license_main_type"));
            		if(rs.getObject("license_main_child") != null)
            			tesDo.setLicenseMainChild(rs.getShort("license_main_child"));            		
            		tesDo.setDepartmentHead(rs.getString("departmentHead"));
            		tesDo.setDepartmentMobilephone(rs.getString("departmentMobilephone"));
            		tesDo.setAddress(rs.getString("address"));
            	}
            	else if(outMethod == 6) {   //输出输出学校ID、学校名称（项目点名称）、区域ID、学校学制、所属区、所属级别、主管部门、部门负责人、部门邮件
            		tesDo.setId(rs.getString("id"));
            		tesDo.setSchoolName(rs.getString("school_name"));
            		tesDo.setArea(rs.getString("area"));
            		tesDo.setLevel(rs.getString("level"));
            		if(rs.getObject("level2") != null)
            			tesDo.setLevel2(rs.getInt("level2"));
            		tesDo.setSchoolAreaId(rs.getString("school_area_id"));            		
            		tesDo.setDepartmentMasterId(rs.getString("department_master_id"));
            		tesDo.setDepartmentSlaveId(rs.getString("department_slave_id"));
            		tesDo.setDepartmentHead(rs.getString("department_head"));
            		tesDo.setDepartmentMobilephone(rs.getString("department_mobilephone"));
            		tesDo.setDepartmentEmail(rs.getString("department_email"));
            	}
                
                return tesDo;
            }
        });
    }
    
    //从数据源ds1的数据表t_edu_school中查找所有id以区域ID（空时在查询所有）和输出字段方法
    public List<TEduSchoolDo> getTEduSchoolDoListByDs1(List<Object> distIdList) {
    	if(jdbcTemplate1 == null)
    		return null;
        String sql = null;
		sql = "select distinct id, school_name, area, level, level2, school_nature, school_area_id, department_master_id, department_slave_id, "
				+ " is_branch_school, parent_id, license_main_type, license_main_child ,department_head as departmentHead ,"
				+ " department_mobilephone as departmentMobilephone,address, "
				+"  canteen_mode, ledger_type "
				+ "from t_edu_school" 
				+ " where "+ "  stat = 1 and reviewed = 1";
		 if(distIdList!=null && distIdList.size()>0) {
			 sql += " and area in(";
			 for(int i =0;i<distIdList.size();i++) {
				 Object distId = distIdList.get(i);
				 if(i<(distIdList.size()-1)) {
					 sql +=  "'" + distId + "',";
				 }else {
					 sql +=  "'" + distId + "'";
				 }
			 }
			 
			 sql += ")";
		 }
        logger.info("sql语句：" + sql);
        return (List<TEduSchoolDo>) jdbcTemplate1.query(sql, new RowMapper<TEduSchoolDo>(){

            @Override
            public TEduSchoolDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduSchoolDo tesDo = new TEduSchoolDo();
        		tesDo.setId(rs.getString("id"));
        		tesDo.setSchoolName(rs.getString("school_name"));
        		tesDo.setArea(rs.getString("area"));
        		tesDo.setLevel(rs.getString("level"));
        		if(rs.getObject("level2") != null)
        			tesDo.setLevel2(rs.getInt("level2"));
        		tesDo.setSchoolNature(rs.getString("school_nature"));
        		tesDo.setSchoolAreaId(rs.getString("school_area_id"));            		
        		tesDo.setDepartmentMasterId(rs.getString("department_master_id"));
        		tesDo.setDepartmentSlaveId(rs.getString("department_slave_id"));
        		if(rs.getObject("is_branch_school") != null)
        			tesDo.setIsBranchSchool(rs.getInt("is_branch_school"));
        		tesDo.setParentId(rs.getString("parent_id"));
        		tesDo.setLicenseMainType(rs.getString("license_main_type"));
        		if(rs.getObject("license_main_child") != null)
        			tesDo.setLicenseMainChild(rs.getShort("license_main_child"));            		
        		tesDo.setDepartmentHead(rs.getString("departmentHead"));
        		tesDo.setDepartmentMobilephone(rs.getString("departmentMobilephone"));
        		tesDo.setAddress(rs.getString("address"));
        		tesDo.setCanteenMode(rs.getShort("canteen_mode"));
        		tesDo.setLedgerType(rs.getString("ledger_type"));
                return tesDo;
            }
        });
    }
    
    //从数据源ds1的数据表t_edu_school中查找所有总校id以区域ID（空时在查询所有）
    public List<TEduSchoolDo> getGenSchIdNameListByDs1(String distId) {
    	if(jdbcTemplate1 == null)
    		return null;
        String sql = null;
        if(distId == null)
        	sql = "select distinct id, school_name from t_edu_school" + " where stat = 1" + " and is_branch_school = 0" + " and reviewed = 1";
        else
        	sql = "select distinct id, school_name from t_edu_school" + " where area = '" + distId + "'" + " and stat = 1" + " and is_branch_school = 0" + " and reviewed = 1";
        logger.info("sql语句：" + sql);
        return (List<TEduSchoolDo>) jdbcTemplate1.query(sql, new RowMapper<TEduSchoolDo>(){

            @Override
            public TEduSchoolDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduSchoolDo tesDo = new TEduSchoolDo();
            	tesDo.setId(rs.getString("id"));
            	tesDo.setSchoolName(rs.getString("school_name"));
                return tesDo;
            }
        });
    }
    
    //从数据源ds1的数据表t_edu_supervise_user中查找用户名和密码（sha1字符串）以用户名（账号）
    public TEduSuperviseUserDo getUserNamePassByUserName(String userName) {
    	if(jdbcTemplate1 == null)
    		return null;
    	TEduSuperviseUserDo tesuDo = new TEduSuperviseUserDo();
    	String sql = "select user_account,password from t_edu_supervise_user where user_account = " + "'" + userName + "'" + " and stat = 1";
        jdbcTemplate1.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tesuDo.setUserAccount(rs.getString("user_account"));
        		tesuDo.setPassword(rs.getString("password"));
        	}
        });
    	
    	return tesuDo;
    }
    
    //更新生成的token到数据源ds1的数据表t_edu_supervise_user表中
    public boolean updateUserTokenToTEduSuperviseUser(String userName, String password, String token) {
    	if(jdbcTemplate1 == null)
    		return false;
    	String sql = "update t_edu_supervise_user set token = " + "'" + token + "'" + " where user_account = " + "'" + userName + "'" + " and password = " + "'" + password + "'" + " and stat = 1";
        jdbcTemplate1.execute(sql);
    	
    	return true;
    }
    
    //从数据源ds1的数据表t_edu_supervise_user中查找授权码以当前授权码
    public String getAuthCodeByCurAuthCode(String token) {
    	if(jdbcTemplate1 == null)
    		return null;
    	String retToken = null;
    	TEduSuperviseUserDo tesuDo = new TEduSuperviseUserDo();
    	String sql = "select token from t_edu_supervise_user where token = " + "'" + token + "'" + " and stat = 1";
        jdbcTemplate1.query(sql, new RowCallbackHandler() {   
        	public void processRow(ResultSet rs) throws SQLException {
        		tesuDo.setToken(token);
        	}   
        });
        if(tesuDo.getToken() != null) {
        	retToken = tesuDo.getToken();
        }
        
        return retToken;
    }




	/**
	 * @Description: 获取上海各区的教育局信息
	 * @Param: []
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao>
	 * @Author: jianghy
	 * @Date: 2019/12/30
	 * @Time: 13:44
	 */
	@Override
	public List<AppCommonDao> getCommitteeList(){
		if (jdbcTemplate1 == null){
			return null;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" select distinct name,district_id as code ");
		sql.append(" from t_edu_committee ");
		sql.append(" where 1=1 ");
		logger.info("sql语句：" + sql.toString());
		return (List<AppCommonDao>) jdbcTemplate1.query(sql.toString(), new RowMapper<AppCommonDao>() {
			@Override
			public AppCommonDao mapRow(ResultSet rs, int rowNum) throws SQLException {
				LinkedHashMap<String, Object> commonMap = new LinkedHashMap<String, Object>();
				commonMap.put("code", rs.getString("code"));
				commonMap.put("name", rs.getString("name"));
				return new AppCommonDao(commonMap);
			}
		});
	}


	/**
	 * @Description: 获取报告列表
	 * @Param: [year, month, committeeCode]
	 * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao>
	 * @Author: jianghy
	 * @Date: 2020/1/2
	 * @Time: 17:54
	 */
	@Override
	public List<AppCommonDao> getWeekReportList(String year,String month,String committeeCode) {
		if (jdbcTemplate1 == null){
			return null;
		}
		String filterStr = "";
		if (StringUtils.isNotBlank(year)){
			filterStr += " and year_date = "+ year;
		}
		if (StringUtils.isNotBlank(month)){
			filterStr += " and month_date = "+ month;
		}
		if (StringUtils.isNotBlank(committeeCode)){
			filterStr += " and committee_code = "+ committeeCode;
		}
		StringBuffer sql = new StringBuffer();
		sql.append(" select * ");
		sql.append(" from t_week_data_report ");
		sql.append(" where 1=1 ");
		sql.append(filterStr);
		sql.append(" order by create_date desc ");
		logger.info("sql语句：" + sql.toString());
		return (List<AppCommonDao>) jdbcTemplate1.query(sql.toString(), new RowMapper<AppCommonDao>() {
			@Override
			public AppCommonDao mapRow(ResultSet rs, int rowNum) throws SQLException {
				LinkedHashMap<String, Object> commonMap = new LinkedHashMap<String, Object>();
				commonMap.put("id", rs.getString("id"));
				commonMap.put("yearDate", rs.getString("year_date") + "年");
				commonMap.put("monthDate", rs.getString("month_date") + "月份");
				commonMap.put("committeeCode", rs.getString("committee_code"));
				if ("0".equals(rs.getString("committee_type"))){
					commonMap.put("committeeType", "市报告");
				}else{
					commonMap.put("committeeType", "区报告");
				}
				commonMap.put("committeeName", rs.getString("committee_name"));
				commonMap.put("reportName", rs.getString("report_name"));
				commonMap.put("createDate", rs.getString("create_date") + " 0:00");
				commonMap.put("fileUrl", rs.getString("file_url"));
				return new AppCommonDao(commonMap);
			}
		});
	}


	/**
	 * @Description: 插入周报告记录
	 * @Param: [createDate, startDate, endDate, committeeCode, committeeName]
	 * @return: boolean
	 * @Author: jianghy
	 * @Date: 2020/1/2
	 * @Time: 20:04
	 */
	@Override
	public boolean doCreateReport(String createDate, String startDate, String endDate, String committeeCode, String committeeName) {
		if (jdbcTemplate1 == null || StringUtils.isBlank(createDate) || StringUtils.isBlank(startDate) || StringUtils.isBlank(endDate) || StringUtils.isBlank(committeeCode) || StringUtils.isBlank(committeeName)){
			return false;
		}
		Environment env = ApplicationUtil.getBean(Environment.class);
		String URL = env.getProperty("report.pdf.url");
		String ftpPath = env.getProperty("report.pdf.ftppath");
		String id = UUID.randomUUID().toString();
		String year = createDate.substring(0,4);
		String month = createDate.substring(5,7);
		String type;
		if (committeeName.equals("上海市教委")){
			type = "0";//0:市报告
		}else{
			type = "1";//1:区报告
		}
		String reportName = startDate + "至 " + endDate + "周数据报告("+committeeName+")" ;
//		String file_url = URL + ftpPath +"/biOptAnl/v1/downloadPdf?createDate="+createDate+"&committeeName="+committeeName;
//		String file_url = URL + ftpPath +committeeCode+"-"+createDate+".pdf";
		String file_url = URL + "/biOptAnl/v1/downloadPdf?createDate="+createDate+"&committeeCode="+committeeCode;
		String sql = "insert into t_week_data_report(id,year_date,month_date,committee_type,committee_code,committee_name,report_name,create_date,file_url) values('"+id+"','"+year+"','"+month+"','"+type+"','"+committeeCode+"','"+committeeName+"','"+reportName+"','"+createDate+"','"+file_url+"')";
		logger.info("sql语句：" + sql);
		jdbcTemplate1.execute(sql);
		return true;
	}
}
