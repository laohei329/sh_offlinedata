package com.tfit.BdBiProcSrvYgwcSchOmc.service.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdMenuDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdRoleDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdUserPermDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSuperviseUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdInterfaceColumnsDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdBriKitStoveDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdMailSrvDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdMsgNoticeDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdNoticeStatusDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.EduBdInterfaceColumnsMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.EduBdUserDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.TEduBdBriKitStoveDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.TEduBdMailSrvDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.TEduBdMsgNoticeDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.TEduBdNoticeStatusDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

@Service
public class Db2ServiceImpl implements Db2Service {
	private static final Logger logger = LogManager.getLogger(Db1ServiceImpl.class.getName());
	
	@Autowired
	EduBdUserDoMapper ebuDoMapper;
	
	@Autowired
	TEduBdMsgNoticeDoMapper tebmnDoMapper;
	
	@Autowired
	TEduBdNoticeStatusDoMapper tebnsDoMapper;
	
	@Autowired
	TEduBdMailSrvDoMapper tebmsDoMapper;
	
	@Autowired
	TEduBdBriKitStoveDoMapper tebbksDoMapper;
	
	@Autowired
	EduBdInterfaceColumnsMapper  columnsMapper;
	
	//额外数据源2
    @Autowired
    @Qualifier("ds2")
    DataSource dataSource2;
    //额外数据源2连接模板
    JdbcTemplate jdbcTemplate2= null;
    
    //初始化处理标识，true表示已处理，false表示未处理
    boolean initProcFlag = false;
    
    //初始化处理
  	@Scheduled(fixedRate = 60*60*1000)
  	public void initProc() {
  		if(initProcFlag)
  			return ;
  		initProcFlag = true;
  		logger.info("定时建立与 DataSource数据源ds2对象表示的数据源的连接，时间：" + BCDTimeUtil.convertNormalFrom(null));
  		jdbcTemplate2 = new JdbcTemplate(dataSource2);
  	}
  	
    //从数据源ds2的数据表t_edu_supervise_user中查找用户名和密码（sha1字符串）以用户名（账号）
    public TEduSuperviseUserDo getUserNamePassByUserName(String userName) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduSuperviseUserDo tesuDo = new TEduSuperviseUserDo();
    	String sql = "select user_account,password from t_edu_supervise_user where user_account = " + "'" + userName + "'" + " and stat = 1";
        jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tesuDo.setUserAccount(rs.getString("user_account"));
        		tesuDo.setPassword(rs.getString("password"));
        	}
        });
    	
    	return tesuDo;
    }
    
    //更新生成的token到数据源ds2的数据表t_edu_supervise_user表中
    public boolean updateUserTokenToTEduSuperviseUser(String userName, String password, String token) {
    	if(jdbcTemplate2 == null)
    		return false;
    	String sql = "update t_edu_supervise_user set token = " + "'" + token + "'" + " where user_account = " + "'" + userName + "'" + " and password = " + "'" + password + "'";
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //从数据源ds2的数据表t_edu_supervise_user中查找授权码以当前授权码
    public String getAuthCodeByCurAuthCode(String token) {
    	if(jdbcTemplate2 == null)
    		return null;
    	String retToken = null;
    	TEduSuperviseUserDo tesuDo = new TEduSuperviseUserDo();
    	String sql = "select token from t_edu_supervise_user where token = " + "'" + token + "'" + " and stat = 1";
        jdbcTemplate2.query(sql, new RowCallbackHandler() {   
        	public void processRow(ResultSet rs) throws SQLException {
        		tesuDo.setToken(token);
        	}   
        });
        if(tesuDo.getToken() != null) {
        	retToken = tesuDo.getToken();
        }
        
        return retToken;
    }
  	
    //从数据源ds2的数据表t_edu_bd_user中查找用户信息
    public TEduBdUserDo getBdUserInfoByUserName(String userName) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduBdUserDo tebuDo = new TEduBdUserDo();
    	String sql = "select id, user_account, password, email, fix_phone, mobile_phone, name, user_pic_url, is_admin, role_id, parent_id, last_login_time, creator, create_time, updater, last_update_time, forbid, token, stat, remarks, org_id, org_name, fax from t_edu_bd_user where user_account = " + "'" + userName + "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
    	jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tebuDo.setId(rs.getString("id"));
        		tebuDo.setUserAccount(rs.getString("user_account"));
        		tebuDo.setPassword(rs.getString("password"));
        		tebuDo.setEmail(rs.getString("email"));
        		tebuDo.setFixPhone(rs.getString("fix_phone"));
        		tebuDo.setMobilePhone(rs.getString("mobile_phone"));
        		tebuDo.setName(rs.getString("name"));
        		tebuDo.setUserPicUrl(rs.getString("user_pic_url"));
        		tebuDo.setIsAdmin(rs.getInt("is_admin"));
        		tebuDo.setRoleId(rs.getString("role_id"));
        		tebuDo.setParentId(rs.getString("parent_id"));
        		tebuDo.setLastLoginTime(rs.getString("last_login_time"));
        		tebuDo.setCreator(rs.getString("creator"));
        		tebuDo.setCreateTime(rs.getString("create_time"));
        		tebuDo.setUpdater(rs.getString("updater"));
        		tebuDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebuDo.setForbid(rs.getInt("forbid"));
        		tebuDo.setToken(rs.getString("token"));
        		tebuDo.setStat(rs.getInt("stat"));
        		tebuDo.setRemarks(rs.getString("remarks"));
        		tebuDo.setOrgId(rs.getString("org_id"));
        		tebuDo.setOrgName(rs.getString("org_name"));
        		tebuDo.setFax(rs.getString("fax"));
        	}
        });
    	
    	return tebuDo;
    }
    
    //从数据源ds2的数据表t_edu_bd_user中查找授权码以当前授权码
    public TEduBdUserDo getBdUserInfoByCurAuthCode(String token) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduBdUserDo tebuDo = new TEduBdUserDo();
    	String sql = "select id, user_account, password, email, fix_phone, mobile_phone, name, user_pic_url, is_admin, role_id, parent_id, last_login_time, creator, create_time, updater, last_update_time, forbid, token, stat, remarks, org_id, org_name, fax from t_edu_bd_user where token = " + "'" + token + "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
    	jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tebuDo.setId(rs.getString("id"));
        		tebuDo.setUserAccount(rs.getString("user_account"));
        		tebuDo.setPassword(rs.getString("password"));
        		tebuDo.setEmail(rs.getString("email"));
        		tebuDo.setFixPhone(rs.getString("fix_phone"));
        		tebuDo.setMobilePhone(rs.getString("mobile_phone"));
        		tebuDo.setName(rs.getString("name"));
        		tebuDo.setUserPicUrl(rs.getString("user_pic_url"));
        		tebuDo.setIsAdmin(rs.getInt("is_admin"));
        		tebuDo.setRoleId(rs.getString("role_id"));
        		tebuDo.setParentId(rs.getString("parent_id"));
        		tebuDo.setLastLoginTime(rs.getString("last_login_time"));
        		tebuDo.setCreator(rs.getString("creator"));
        		tebuDo.setCreateTime(rs.getString("create_time"));
        		tebuDo.setUpdater(rs.getString("updater"));
        		tebuDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebuDo.setForbid(rs.getInt("forbid"));
        		tebuDo.setToken(rs.getString("token"));
        		tebuDo.setStat(rs.getInt("stat"));
        		tebuDo.setRemarks(rs.getString("remarks"));
        		tebuDo.setOrgId(rs.getString("org_id"));
        		tebuDo.setOrgName(rs.getString("org_name"));
        		tebuDo.setFax(rs.getString("fax"));
        	}
        });
    	
    	return tebuDo;
    }
    
    //从数据源ds2的数据表t_edu_bd_user中查找授权码以当前授权码
    public String getAuthCodeByCurAuthCode2(String token) {
    	if(jdbcTemplate2 == null)
    		return null;
    	String retToken = null;
    	TEduSuperviseUserDo tesuDo = new TEduSuperviseUserDo();
    	String sql = "select token from t_edu_bd_user where token = " + "'" + token + "'" + " and stat = 1";
        jdbcTemplate2.query(sql, new RowCallbackHandler() {   
        	public void processRow(ResultSet rs) throws SQLException {
        		tesuDo.setToken(rs.getString("token"));
        	}   
        });
        if(tesuDo.getToken() != null) {
        	retToken = tesuDo.getToken();
        }
        
        return retToken;
    }
    
    //从数据源ds2的数据表t_edu_bd_user中查找所有用户信息
    public List<TEduBdUserDo> getAllBdUserInfo() {
    	if(jdbcTemplate2 == null)
    		return null;
    	String sql = "select id, user_account, password, email, fix_phone, mobile_phone, name, user_pic_url, is_admin, role_id, parent_id, last_login_time, creator, create_time, updater, last_update_time, forbid, token, stat, remarks, org_id, org_name, fax from t_edu_bd_user where" + " stat = 1";
    	logger.info("执行的MySql语句：" + sql);       
        return (List<TEduBdUserDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdUserDo>(){

            @Override
            public TEduBdUserDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdUserDo tebuDo = new TEduBdUserDo();
            	tebuDo.setId(rs.getString("id"));
        		tebuDo.setUserAccount(rs.getString("user_account"));
        		tebuDo.setPassword(rs.getString("password"));
        		tebuDo.setEmail(rs.getString("email"));
        		tebuDo.setFixPhone(rs.getString("fix_phone"));
        		tebuDo.setMobilePhone(rs.getString("mobile_phone"));
        		tebuDo.setName(rs.getString("name"));
        		tebuDo.setUserPicUrl(rs.getString("user_pic_url"));
        		tebuDo.setIsAdmin(rs.getInt("is_admin"));
        		tebuDo.setRoleId(rs.getString("role_id"));
        		tebuDo.setParentId(rs.getString("parent_id"));
        		tebuDo.setLastLoginTime(rs.getString("last_login_time"));
        		tebuDo.setCreator(rs.getString("creator"));
        		tebuDo.setCreateTime(rs.getString("create_time"));
        		tebuDo.setUpdater(rs.getString("updater"));
        		tebuDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebuDo.setForbid(rs.getInt("forbid"));
        		tebuDo.setToken(rs.getString("token"));
        		tebuDo.setStat(rs.getInt("stat"));
        		tebuDo.setRemarks(rs.getString("remarks"));
        		tebuDo.setOrgId(rs.getString("org_id"));
        		tebuDo.setOrgName(rs.getString("org_name"));
        		tebuDo.setFax(rs.getString("fax"));
                return tebuDo;
            }
        });
    }
    
    //从数据源ds2的数据表t_edu_bd_user中查找所有用户信息以父账户
    public List<TEduBdUserDo> getAllBdUserInfoByParentId(String parentId) {
    	if(jdbcTemplate2 == null)
    		return null;
    	String sql = "select id, user_account, password, email, fix_phone, mobile_phone, name, user_pic_url, is_admin, role_id, parent_id, last_login_time, creator, create_time, updater, last_update_time, forbid, token, stat, remarks, org_id, org_name, fax from t_edu_bd_user where parent_id = "+ "'" + parentId+ "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
        return (List<TEduBdUserDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdUserDo>(){

            @Override
            public TEduBdUserDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdUserDo tebuDo = new TEduBdUserDo();
            	tebuDo.setId(rs.getString("id"));
        		tebuDo.setUserAccount(rs.getString("user_account"));
        		tebuDo.setPassword(rs.getString("password"));
        		tebuDo.setEmail(rs.getString("email"));
        		tebuDo.setFixPhone(rs.getString("fix_phone"));
        		tebuDo.setMobilePhone(rs.getString("mobile_phone"));
        		tebuDo.setName(rs.getString("name"));
        		tebuDo.setUserPicUrl(rs.getString("user_pic_url"));
        		tebuDo.setIsAdmin(rs.getInt("is_admin"));
        		tebuDo.setRoleId(rs.getString("role_id"));
        		tebuDo.setParentId(rs.getString("parent_id"));
        		tebuDo.setLastLoginTime(rs.getString("last_login_time"));
        		tebuDo.setCreator(rs.getString("creator"));
        		tebuDo.setCreateTime(rs.getString("create_time"));
        		tebuDo.setUpdater(rs.getString("updater"));
        		tebuDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebuDo.setForbid(rs.getInt("forbid"));
        		tebuDo.setToken(rs.getString("token"));
        		tebuDo.setStat(rs.getInt("stat"));
        		tebuDo.setRemarks(rs.getString("remarks"));
        		tebuDo.setOrgId(rs.getString("org_id"));
        		tebuDo.setOrgName(rs.getString("org_name"));
        		tebuDo.setFax(rs.getString("fax"));
                return tebuDo;
            }
        });
    }
    
    //从数据源ds2的数据表t_edu_bd_user中查找用户信息以单位ID
    public List<TEduBdUserDo> getBdUserInfoByUserOrg(String orgId) {
    	if(jdbcTemplate2 == null || orgId == null)
    		return null;
    	String sql = "select id, user_account, password, email, fix_phone, mobile_phone, name, user_pic_url, is_admin, role_id, parent_id, last_login_time, creator, create_time, updater, last_update_time, forbid, token, stat, remarks, org_id, org_name, fax from t_edu_bd_user where org_id = "+ "'" + orgId+ "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
        return (List<TEduBdUserDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdUserDo>(){

            @Override
            public TEduBdUserDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdUserDo tebuDo = new TEduBdUserDo();
            	tebuDo.setId(rs.getString("id"));
        		tebuDo.setUserAccount(rs.getString("user_account"));
        		tebuDo.setPassword(rs.getString("password"));
        		tebuDo.setEmail(rs.getString("email"));
        		tebuDo.setFixPhone(rs.getString("fix_phone"));
        		tebuDo.setMobilePhone(rs.getString("mobile_phone"));
        		tebuDo.setName(rs.getString("name"));
        		tebuDo.setUserPicUrl(rs.getString("user_pic_url"));
        		tebuDo.setIsAdmin(rs.getInt("is_admin"));
        		tebuDo.setRoleId(rs.getString("role_id"));
        		tebuDo.setParentId(rs.getString("parent_id"));
        		tebuDo.setLastLoginTime(rs.getString("last_login_time"));
        		tebuDo.setCreator(rs.getString("creator"));
        		tebuDo.setCreateTime(rs.getString("create_time"));
        		tebuDo.setUpdater(rs.getString("updater"));
        		tebuDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebuDo.setForbid(rs.getInt("forbid"));
        		tebuDo.setToken(rs.getString("token"));
        		tebuDo.setStat(rs.getInt("stat"));
        		tebuDo.setRemarks(rs.getString("remarks"));
        		tebuDo.setOrgId(rs.getString("org_id"));
        		tebuDo.setOrgName(rs.getString("org_name"));
        		tebuDo.setFax(rs.getString("fax"));
                return tebuDo;
            }
        });
    }
    
    //插入记录到数据源ds2的数据表t_edu_bd_user中
    public boolean InsertBdUserInfo(TEduBdUserDo tebuDo) {
    	if(jdbcTemplate2 == null || tebuDo == null)
    		return false;    	
    	String sql = "", sql1 = "insert into t_edu_bd_user(", sql2 = " values(";
    	//主键id
    	if(tebuDo.getId() != null) {
    		sql1 += "id";
    		sql2 += "'" + tebuDo.getId() + "'";
    	}
    	//登录账户名
    	if(tebuDo.getUserAccount() != null) {
    		sql1 += ", user_account";
    		sql2 += ", '" + tebuDo.getUserAccount() + "'";
    	}
    	//密码
    	if(tebuDo.getPassword() != null) {
    		sql1 += ", password";
    		sql2 += ", '" + tebuDo.getPassword() + "'";
    	}               
    	//邮箱
    	if(tebuDo.getEmail() != null) {
    		sql1 += ", email";
    		sql2 += ", '" + tebuDo.getEmail() + "'";
    	}               
    	//固定电话
    	if(tebuDo.getFixPhone() != null) {
    		sql1 += ", fix_phone";
    		sql2 += ", '" + tebuDo.getFixPhone() + "'";
    	}             
    	//手机号码
    	if(tebuDo.getMobilePhone() != null) {
    		sql1 += ", mobile_phone";
    		sql2 += ", '" + tebuDo.getMobilePhone() + "'";
    	}         
    	//姓名
    	if(tebuDo.getName() != null) {
    		sql1 += ", name";
    		sql2 += ", '" + tebuDo.getName() + "'";
    	}              
    	//用户图片URL   
    	if(tebuDo.getUserPicUrl() != null) {
    		sql1 += ", user_pic_url";
    		sql2 += ", '" + tebuDo.getUserPicUrl() + "'";
    	}          
    	//0是false 1是true
    	if(tebuDo.getIsAdmin() != null) {
    		sql1 += ", is_admin";
    		sql2 += ", '" + tebuDo.getIsAdmin() + "'";
    	}         
    	//t_edu_bd_role  表id
    	if(tebuDo.getRoleId() != null) {
    		sql1 += ", role_id";
    		sql2 += ", '" + tebuDo.getRoleId() + "'";
    	}            
    	//账户父ID，空表示超级管理员账户，拥有最高权限
    	if(tebuDo.getParentId() != null) {
    		sql1 += ", parent_id";
    		sql2 += ", '" + tebuDo.getParentId() + "'";
    	}                     
    	//最后登录时间
    	if(tebuDo.getLastLoginTime() != null) {
    		sql1 += ", last_login_time";
    		sql2 += ", '" + tebuDo.getLastLoginTime() + "'";
    	}           
    	//创建者
    	if(tebuDo.getCreator() != null) {
    		sql1 += ", creator";
    		sql2 += ", '" + tebuDo.getCreator() + "'";
    	}       
    	//创建时间
    	if(tebuDo.getCreateTime() != null) {
    		sql1 += ", create_time";
    		sql2 += ", '" + tebuDo.getCreateTime() + "'";
    	}      
    	//更新人
    	if(tebuDo.getUpdater() != null) {
    		sql1 += ", updater";
    		sql2 += ", '" + tebuDo.getUpdater() + "'";
    	}          
    	//最近更新时间
    	if(tebuDo.getLastUpdateTime() != null) {
    		sql1 += ", last_update_time";
    		sql2 += ", '" + tebuDo.getLastUpdateTime() + "'";
    	}      
    	//是否禁用0禁用 1启用
    	if(tebuDo.getForbid() != null) {
    		sql1 += ", forbid";
    		sql2 += ", '" + tebuDo.getForbid() + "'";
    	}       
    	//用户授权码
    	if(tebuDo.getToken() != null) {
    		sql1 += ", token";
    		sql2 += ", '" + tebuDo.getToken() + "'";
    	}       
    	//是否有效 1 有效 0 无效
    	if(tebuDo.getStat() != null) {
    		sql1 += ", stat";
    		sql2 += ", '" + tebuDo.getStat() + "'";
    	}  
    	//备注
    	if(tebuDo.getRemarks() != null) {
    		sql1 += ", remarks";
    		sql2 += ", '" + tebuDo.getRemarks() + "'";
    	}
    	//组织ID
    	if(tebuDo.getOrgId() != null) {
    		sql1 += ", org_id";
    		sql2 += ", '" + tebuDo.getOrgId() + "'";
    	}
    	//组织名称
    	if(tebuDo.getOrgName() != null) {
    		sql1 += ", org_name";
    		sql2 += ", '" + tebuDo.getOrgName() + "'";
    	}
    	//传真
    	if(tebuDo.getFax() != null) {
    		sql1 += ", fax";
    		sql2 += ", '" + tebuDo.getFax() + "'";
    	}
    	sql1 += ")";
    	sql2 += ")";
    	sql = sql1 + sql2;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //更新记录到数据源ds2的数据表t_edu_bd_user中
    public boolean UpdateBdUserInfo(TEduBdUserDo tebuDo, String token) {
    	if(jdbcTemplate2 == null || tebuDo == null)
    		return false;    	
    	String sql = "", sql1 = "update t_edu_bd_user set", sql2 = " ", sql3 = " where token = " + "'" + token + "'";
        boolean headFlag = false;
    	//主键id
    	if(tebuDo.getId() != null) {
    		sql2 += "id=";
    		sql2 += "'" + tebuDo.getId() + "'";
    		headFlag = true;
    	}
    	//登录账户名
    	if(tebuDo.getUserAccount() != null) {
    		if(headFlag)
    			sql2 += ", user_account=";
    		else {
    			sql2 += "user_account=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getUserAccount() + "'";
    	}
    	//密码
    	if(tebuDo.getPassword() != null) {
    		if(headFlag)
    			sql2 += ", password=";
    		else {
    			sql2 += "password=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getPassword() + "'";
    	}     
    	//安全等级
    	if(tebuDo.getSafeGrade() != null) {
    		if(headFlag)
    			sql2 += ", safe_grade=";
    		else {
    			sql2 += "safe_grade=";
    			headFlag = true;
    		}
    		sql2 += tebuDo.getSafeGrade();
    	}
    	//邮箱
    	if(tebuDo.getEmail() != null) {
    		if(headFlag)
    			sql2 += ", email=";
    		else {
    			sql2 += "email=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getEmail() + "'";
    	}               
    	//固定电话
    	if(tebuDo.getFixPhone() != null) {
    		if(headFlag)
    			sql2 += ", fix_phone=";
    		else {
    			sql2 += "fix_phone=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getFixPhone() + "'";
    	}             
    	//手机号码
    	if(tebuDo.getMobilePhone() != null) {
    		if(headFlag)
    			sql2 += ", mobile_phone=";
    		else {
    			sql2 += "mobile_phone=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getMobilePhone() + "'";
    	}         
    	//姓名
    	if(tebuDo.getName() != null) {
    		if(headFlag)
    			sql2 += ", name=";
    		else {
    			sql2 += "name=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getName() + "'";
    	}              
    	//用户图片URL   
    	if(tebuDo.getUserPicUrl() != null) {
    		if(headFlag)
    			sql2 += ", user_pic_url=";
    		else {
    			sql2 += "user_pic_url=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getUserPicUrl() + "'";
    	}          
    	//0是false 1是true
    	if(tebuDo.getIsAdmin() != null) {
    		if(headFlag)
    			sql2 += ", is_admin=";
    		else {
    			sql2 += "is_admin=";
    			headFlag = true;
    		}
    		sql2 += tebuDo.getIsAdmin();
    	}         
    	//t_edu_bd_role  表id
    	if(tebuDo.getRoleId() != null) {
    		if(headFlag)
    			sql2 += ", role_id=";
    		else {
    			sql2 += "role_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getRoleId() + "'";
    	}            
    	//账户父ID，空表示超级管理员账户，拥有最高权限
    	if(tebuDo.getParentId() != null) {
    		if(headFlag)
    			sql2 += ", parent_id=";
    		else {
    			sql2 += "parent_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getParentId() + "'";
    	}                     
    	//最后登录时间
    	if(tebuDo.getLastLoginTime() != null) {
    		if(headFlag)
    			sql2 += ", last_login_time=";
    		else {
    			sql2 += "last_login_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getLastLoginTime() + "'";
    	}           
    	//创建者
    	if(tebuDo.getCreator() != null) {
    		if(headFlag)
    			sql2 += ", creator=";
    		else {
    			sql2 += "creator=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getCreator() + "'";
    	}       
    	//创建时间
    	if(tebuDo.getCreateTime() != null) {
    		if(headFlag)
    			sql2 += ", create_time=";
    		else {
    			sql2 += "create_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getCreateTime() + "'";
    	}      
    	//更新人
    	if(tebuDo.getUpdater() != null) {
    		if(headFlag)
    			sql2 += ", updater=";
    		else {
    			sql2 += "updater=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getUpdater() + "'";
    	}          
    	//最近更新时间
    	if(tebuDo.getLastUpdateTime() != null) {
    		if(headFlag)
    			sql2 += ", last_update_time=";
    		else {
    			sql2 += "last_update_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getLastUpdateTime() + "'";
    	}      
    	//是否禁用0禁用 1启用
    	if(tebuDo.getForbid() != null) {
    		if(headFlag)
    			sql2 += ", forbid=";
    		else {
    			sql2 += "forbid=";
    			headFlag = true;
    		}
    		sql2 += tebuDo.getForbid();
    	}       
    	//用户授权码
    	if(tebuDo.getToken() != null) {
    		if(headFlag)
    			sql2 += ", token=";
    		else {
    			sql2 += "token=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getToken() + "'";
    	}       
    	//是否有效 1 有效 0 无效
    	if(tebuDo.getStat() != null) {
    		if(headFlag)
    			sql2 += ", stat=";
    		else {
    			sql2 += "stat=";
    			headFlag = true;
    		}
    		sql2 += tebuDo.getStat();
    	}  
    	//备注
    	if(tebuDo.getRemarks() != null) {
    		if(headFlag)
    			sql2 += ", remarks=";
    		else {
    			sql2 += "remarks=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getRemarks() + "'";
    	}
    	//组织ID
    	if(tebuDo.getOrgId() != null) {
    		if(headFlag)
    			sql2 += ", org_id=";
    		else {
    			sql2 += "org_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getOrgId() + "'";
    	}
    	//组织名称
    	if(tebuDo.getOrgName() != null) {    		
    		if(headFlag)
    			sql2 += ", org_name=";
    		else {
    			sql2 += "org_name=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getOrgName() + "'";
    	}
    	//传真
    	if(tebuDo.getFax() != null) {    		
    		if(headFlag)
    			sql2 += ", fax=";
    		else {
    			sql2 += "fax=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getFax() + "'";
    	}
    	sql = sql1 + sql2 + sql3;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //更新记录到数据源ds2的数据表t_edu_bd_user中以输入字段
    public boolean UpdateBdUserInfoByField(TEduBdUserDo tebuDo, String fieldName, String fieldVal) {
    	if(jdbcTemplate2 == null || tebuDo == null)
    		return false;    	
    	String sql = "", sql1 = "update t_edu_bd_user set", sql2 = " ", sql3 = " where " + fieldName + " = " + "'" + fieldVal + "'" + " and stat = 1";
        boolean headFlag = false;
    	//主键id
    	if(tebuDo.getId() != null) {
    		sql2 += "id=";
    		sql2 += "'" + tebuDo.getId() + "'";
    		headFlag = true;
    	}
    	//登录账户名
    	if(tebuDo.getUserAccount() != null) {
    		if(headFlag)
    			sql2 += ", user_account=";
    		else {
    			sql2 += "user_account=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getUserAccount() + "'";
    	}
    	//密码
    	if(tebuDo.getPassword() != null) {
    		if(headFlag)
    			sql2 += ", password=";
    		else {
    			sql2 += "password=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getPassword() + "'";
    	}               
    	//邮箱
    	if(tebuDo.getEmail() != null) {
    		if(headFlag)
    			sql2 += ", email=";
    		else {
    			sql2 += "email=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getEmail() + "'";
    	}               
    	//固定电话
    	if(tebuDo.getFixPhone() != null) {
    		if(headFlag)
    			sql2 += ", fix_phone=";
    		else {
    			sql2 += "fix_phone=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getFixPhone() + "'";
    	}             
    	//手机号码
    	if(tebuDo.getMobilePhone() != null) {
    		if(headFlag)
    			sql2 += ", mobile_phone=";
    		else {
    			sql2 += "mobile_phone=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getMobilePhone() + "'";
    	}         
    	//姓名
    	if(tebuDo.getName() != null) {
    		if(headFlag)
    			sql2 += ", name=";
    		else {
    			sql2 += "name=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getName() + "'";
    	}              
    	//用户图片URL   
    	if(tebuDo.getUserPicUrl() != null) {
    		if(headFlag)
    			sql2 += ", user_pic_url=";
    		else {
    			sql2 += "user_pic_url=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getUserPicUrl() + "'";
    	}          
    	//0是false 1是true
    	if(tebuDo.getIsAdmin() != null) {
    		if(headFlag)
    			sql2 += ", is_admin=";
    		else {
    			sql2 += "is_admin=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getIsAdmin() + "'";
    	}         
    	//t_edu_bd_role  表id
    	if(tebuDo.getRoleId() != null) {
    		if(headFlag)
    			sql2 += ", role_id=";
    		else {
    			sql2 += "role_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getRoleId() + "'";
    	}            
    	//账户父ID，空表示超级管理员账户，拥有最高权限
    	if(tebuDo.getParentId() != null) {
    		if(headFlag)
    			sql2 += ", parent_id=";
    		else {
    			sql2 += "parent_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getParentId() + "'";
    	}                     
    	//最后登录时间
    	if(tebuDo.getLastLoginTime() != null) {
    		if(headFlag)
    			sql2 += ", last_login_time=";
    		else {
    			sql2 += "last_login_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getLastLoginTime() + "'";
    	}           
    	//创建者
    	if(tebuDo.getCreator() != null) {
    		if(headFlag)
    			sql2 += ", creator=";
    		else {
    			sql2 += "creator=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getCreator() + "'";
    	}       
    	//创建时间
    	if(tebuDo.getCreateTime() != null) {
    		if(headFlag)
    			sql2 += ", create_time=";
    		else {
    			sql2 += "create_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getCreateTime() + "'";
    	}      
    	//更新人
    	if(tebuDo.getUpdater() != null) {
    		if(headFlag)
    			sql2 += ", updater=";
    		else {
    			sql2 += "updater=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getUpdater() + "'";
    	}          
    	//最近更新时间
    	if(tebuDo.getLastUpdateTime() != null) {
    		if(headFlag)
    			sql2 += ", last_update_time=";
    		else {
    			sql2 += "last_update_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getLastUpdateTime() + "'";
    	}      
    	//是否禁用0禁用 1启用
    	if(tebuDo.getForbid() != null) {
    		if(headFlag)
    			sql2 += ", forbid=";
    		else {
    			sql2 += "forbid=";
    			headFlag = true;
    		}
    		sql2 += tebuDo.getForbid();
    	}       
    	//用户授权码
    	if(tebuDo.getToken() != null) {
    		if(headFlag)
    			sql2 += ", token=";
    		else {
    			sql2 += "token=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getToken() + "'";
    	}       
    	//是否有效 1 有效 0 无效
    	if(tebuDo.getStat() != null) {
    		if(headFlag)
    			sql2 += ", stat=";
    		else {
    			sql2 += "stat=";
    			headFlag = true;
    		}
    		sql2 += tebuDo.getStat();
    	}  
    	//备注
    	if(tebuDo.getRemarks() != null) {
    		if(headFlag)
    			sql2 += ", remarks=";
    		else {
    			sql2 += "remarks=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getRemarks() + "'";
    	}
    	//组织ID
    	if(tebuDo.getOrgId() != null) {
    		if(headFlag)
    			sql2 += ", org_id=";
    		else {
    			sql2 += "org_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getOrgId() + "'";
    	}
    	//组织名称
    	if(tebuDo.getOrgName() != null) {    		
    		if(headFlag)
    			sql2 += ", org_name=";
    		else {
    			sql2 += "org_name=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getOrgName() + "'";
    	}
    	//传真
    	if(tebuDo.getFax() != null) {    		
    		if(headFlag)
    			sql2 += ", fax=";
    		else {
    			sql2 += "fax=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebuDo.getFax() + "'";
    	}
    	sql = sql1 + sql2 + sql3;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //删除数据源ds2的数据表t_edu_bd_user中记录以用户名
    public boolean DeleteBdUserInfoByUserName(String userName) {
    	if(jdbcTemplate2 == null || userName == null)
    		return false;    	
    	String sql = "delete from t_edu_bd_user where user_account = " + "'" + userName + "'";
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //从数据源ds2的数据表t_edu_bd_user中查找用户信息以授权码token
    public TEduBdUserDo getBdUserInfoByToken(String token) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduBdUserDo tebuDo = new TEduBdUserDo();
    	String sql = "select id, user_account, password, safe_grade, email, fix_phone, mobile_phone, name, user_pic_url, is_admin, role_id, parent_id, last_login_time, creator, create_time, updater, last_update_time, forbid, token, stat, remarks, org_id, org_name, fax from t_edu_bd_user where token = " + "'" + token + "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
    	jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tebuDo.setId(rs.getString("id"));
        		tebuDo.setUserAccount(rs.getString("user_account"));
        		tebuDo.setPassword(rs.getString("password"));
        		tebuDo.setSafeGrade(rs.getInt("safe_grade"));
        		tebuDo.setEmail(rs.getString("email"));
        		tebuDo.setFixPhone(rs.getString("fix_phone"));
        		tebuDo.setMobilePhone(rs.getString("mobile_phone"));
        		tebuDo.setName(rs.getString("name"));
        		tebuDo.setUserPicUrl(rs.getString("user_pic_url"));
        		tebuDo.setIsAdmin(rs.getInt("is_admin"));
        		tebuDo.setRoleId(rs.getString("role_id"));
        		tebuDo.setParentId(rs.getString("parent_id"));
        		tebuDo.setLastLoginTime(rs.getString("last_login_time"));
        		tebuDo.setCreator(rs.getString("creator"));
        		tebuDo.setCreateTime(rs.getString("create_time"));
        		tebuDo.setUpdater(rs.getString("updater"));
        		tebuDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebuDo.setForbid(rs.getInt("forbid"));
        		tebuDo.setToken(rs.getString("token"));
        		tebuDo.setStat(rs.getInt("stat"));
        		tebuDo.setRemarks(rs.getString("remarks"));
        		tebuDo.setOrgId(rs.getString("org_id"));
        		tebuDo.setOrgName(rs.getString("org_name"));
        		tebuDo.setFax(rs.getString("fax"));
        	}
        });
    	
    	return tebuDo;
    }
    
    //插入记录到数据源ds2的数据表t_edu_bd_role中
    public boolean InsertBdRoleInfo(TEduBdRoleDo tebrDo) {
    	if(jdbcTemplate2 == null || tebrDo == null)
    		return false;    	
    	String sql = "", sql1 = "insert into t_edu_bd_role(", sql2 = " values(";
    	//主键id
    	if(tebrDo.getId() != null) {
    		sql1 += "id";
    		sql2 += "'" + tebrDo.getId() + "'";
    	}
    	//角色类型，1:监管部门，2:学校	
    	if(tebrDo.getRoleType() != null) {
    		sql1 += ", role_type";
    		sql2 += ", '" + tebrDo.getRoleType() + "'";
    	}
    	//角色名称
    	if(tebrDo.getRoleName() != null) {
    		sql1 += ", role_name";
    		sql2 += ", '" + tebrDo.getRoleName() + "'";
    	}
    	//创建时间
    	if(tebrDo.getCreateTime() != null) {
    		sql1 += ", create_time";
    		sql2 += ", '" + tebrDo.getCreateTime() + "'";
    	}             
    	//最后更新时间
    	if(tebrDo.getLastUpdateTime() != null) {
    		sql1 += ", last_update_time";
    		sql2 += ", '" + tebrDo.getLastUpdateTime() + "'";
    	}         
    	//创建人
    	if(tebrDo.getCreator() != null) {
    		sql1 += ", creator";
    		sql2 += ", '" + tebrDo.getCreator() + "'";
    	}    
    	//描述
    	if(tebrDo.getDiscrip() != null) {
    		sql1 += ", discrip";
    		sql2 += ", '" + tebrDo.getDiscrip() + "'";
    	}              	
    	//是否有效 1 有效 0 无效
    	if(tebrDo.getStat() != null) {
    		sql1 += ", stat";
    		sql2 += ", '" + tebrDo.getStat() + "'";
    	}    	
    	sql1 += ")";
    	sql2 += ")";
    	sql = sql1 + sql2;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //更新记录到数据源ds2的数据表t_edu_bd_role中以输入字段
    public boolean UpdateBdRoleInfoByField(TEduBdRoleDo tebrDo, String fieldName, String fieldVal) {
    	if(jdbcTemplate2 == null || tebrDo == null)
    		return false;    	
    	String sql = "", sql1 = "update t_edu_bd_role set", sql2 = " ", sql3 = " where " + fieldName + " = " + "'" + fieldVal + "'" + " and stat = 1";
        boolean headFlag = false;
    	//主键id
    	if(tebrDo.getId() != null) {
    		sql2 += "id=";
    		sql2 += "'" + tebrDo.getId() + "'";
    		headFlag = true;
    	}
    	//角色类型，1:监管部门，2:学校
    	if(tebrDo.getRoleType() != null) {
    		if(headFlag)
    			sql2 += ", role_type=";
    		else {
    			sql2 += "role_type=";
    			headFlag = true;
    		}
    		sql2 += tebrDo.getRoleType();
    	}    	
    	//角色名称
    	if(tebrDo.getRoleName() != null) {
    		if(headFlag)
    			sql2 += ", role_name=";
    		else {
    			sql2 += "role_name=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebrDo.getRoleName() + "'";
    	}               
    	//创建时间
    	if(tebrDo.getCreateTime() != null) {
    		if(headFlag)
    			sql2 += ", create_time=";
    		else {
    			sql2 += "create_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebrDo.getCreateTime() + "'";
    	}             
    	//最后更新时间
    	if(tebrDo.getLastUpdateTime() != null) {
    		if(headFlag)
    			sql2 += ", last_update_time=";
    		else {
    			sql2 += "last_update_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebrDo.getLastUpdateTime() + "'";
    	} 
    	//创建人
    	if(tebrDo.getCreator() != null) {
    		if(headFlag)
    			sql2 += ", creator=";
    		else {
    			sql2 += "creator=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebrDo.getCreator() + "'";
    	} 
    	//描述
    	if(tebrDo.getDiscrip() != null) {
    		if(headFlag)
    			sql2 += ", discrip=";
    		else {
    			sql2 += "discrip=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebrDo.getDiscrip() + "'";
    	}    	
    	//是否有效 1 有效 0 无效
    	if(tebrDo.getStat() != null) {
    		if(headFlag)
    			sql2 += ", stat=";
    		else {
    			sql2 += "stat=";
    			headFlag = true;
    		}
    		sql2 += tebrDo.getStat();
    	}  
    	sql = sql1 + sql2 + sql3;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以id
    public TEduBdRoleDo getBdRoleInfoByRoleId(String id) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduBdRoleDo tebrDo = new TEduBdRoleDo();
    	String sql = "select id, role_type, role_name, create_time, last_update_time, discrip, stat from t_edu_bd_role where id = " + "'" + id + "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
    	jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tebrDo.setId(rs.getString("id"));
        		tebrDo.setRoleType(rs.getInt("role_type"));
        		tebrDo.setRoleName(rs.getString("role_name"));
        		tebrDo.setCreateTime(rs.getString("create_time"));
        		tebrDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebrDo.setDiscrip(rs.getString("discrip"));
        		tebrDo.setStat(rs.getInt("stat"));
        	}
        });
    	
    	return tebrDo;
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    public TEduBdRoleDo getBdRoleInfoByRoleName(String roleName) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduBdRoleDo tebrDo = new TEduBdRoleDo();
    	String sql = "select id, role_type, role_name, create_time, last_update_time, discrip, stat from t_edu_bd_role where role_name = " + "'" + roleName + "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
    	jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tebrDo.setId(rs.getString("id"));
        		tebrDo.setRoleType(rs.getInt("role_type"));
        		tebrDo.setRoleName(rs.getString("role_name"));
        		tebrDo.setCreateTime(rs.getString("create_time"));
        		tebrDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebrDo.setDiscrip(rs.getString("discrip"));
        		tebrDo.setStat(rs.getInt("stat"));
        	}
        });
    	
    	return tebrDo;
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    public List<TEduBdRoleDo> getBdRoleInfoByRoleName2(String roleName) {
    	if(jdbcTemplate2 == null)
    		return null;
    	String sql = "select id, role_type, role_name, create_time, last_update_time, discrip, stat from t_edu_bd_role where role_name = " + "'" + roleName + "'" + " and stat = 1";
    	logger.info("执行的MySql语句：" + sql);
    	return (List<TEduBdRoleDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdRoleDo>(){

    		@Override
    		public TEduBdRoleDo mapRow(ResultSet rs, int rowNum) throws SQLException {
    			TEduBdRoleDo tebrDo = new TEduBdRoleDo();
    			tebrDo.setId(rs.getString("id"));
    			tebrDo.setRoleType(rs.getInt("role_type"));
    			tebrDo.setRoleName(rs.getString("role_name"));
        		tebrDo.setCreateTime(rs.getString("create_time"));
        		tebrDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebrDo.setDiscrip(rs.getString("discrip"));
        		tebrDo.setStat(rs.getInt("stat"));
        		return tebrDo;
        	}
    	});
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    public TEduBdRoleDo getBdRoleInfoByRoleName3(int roleType, String roleName) {
    	if(jdbcTemplate2 == null)
    		return null;
    	TEduBdRoleDo tebrDo = new TEduBdRoleDo();
    	String sql = "select id, role_type, role_name, create_time, last_update_time, discrip, stat from t_edu_bd_role where role_name = " + "'" + roleName + "'" + " and stat = 1" + " and role_type = " + roleType;
    	logger.info("执行的MySql语句：" + sql);
    	jdbcTemplate2.query(sql, new RowCallbackHandler(){   
        	public void processRow(ResultSet rs) throws SQLException{  
        		tebrDo.setId(rs.getString("id"));
        		tebrDo.setRoleType(rs.getInt("role_type"));
        		tebrDo.setRoleName(rs.getString("role_name"));
        		tebrDo.setCreateTime(rs.getString("create_time"));
        		tebrDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebrDo.setDiscrip(rs.getString("discrip"));
        		tebrDo.setStat(rs.getInt("stat"));
        	}
        });
    	
    	return tebrDo;
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    public List<TEduBdRoleDo> getBdRoleInfoByRoleName4(int roleType, String roleName) {
    	if(jdbcTemplate2 == null)
    		return null;
    	String sql = "select id, role_type, role_name, create_time, last_update_time, discrip, stat from t_edu_bd_role where role_name = " + "'" + roleName + "'" + " and stat = 1" + " and role_type = " + roleType;
    	logger.info("执行的MySql语句：" + sql);
    	return (List<TEduBdRoleDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdRoleDo>(){

    		@Override
    		public TEduBdRoleDo mapRow(ResultSet rs, int rowNum) throws SQLException {
    			TEduBdRoleDo tebrDo = new TEduBdRoleDo();
    			tebrDo.setId(rs.getString("id"));
    			tebrDo.setRoleType(rs.getInt("role_type"));
    			tebrDo.setRoleName(rs.getString("role_name"));
        		tebrDo.setCreateTime(rs.getString("create_time"));
        		tebrDo.setLastUpdateTime(rs.getString("last_update_time"));
        		tebrDo.setDiscrip(rs.getString("discrip"));
        		tebrDo.setStat(rs.getInt("stat"));
        		return tebrDo;
        	}
    	});
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找所有角色名称
    public List<TEduBdRoleDo> getBdRoleInfoAllRoleNames() {
    	if(jdbcTemplate2 == null)
    		return null;
        String sql = null;
        sql = "select distinct role_name from t_edu_bd_role" + " where stat = 1";        
        logger.info("sql语句：" + sql);
        return (List<TEduBdRoleDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdRoleDo>(){

            @Override
            public TEduBdRoleDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdRoleDo tebrDo = new TEduBdRoleDo();
            	tebrDo.setRoleName(rs.getString("role_name"));
                return tebrDo;
            }
        });
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色名称以角色类型
    public List<TEduBdRoleDo> getBdRoleInfoRoleNamesByRoleType(int roleType) {
    	if(jdbcTemplate2 == null)
    		return null;
        String sql = null;
        sql = "select distinct role_name from t_edu_bd_role" + " where stat = 1" + " and role_type = " + roleType;        
        logger.info("sql语句：" + sql);
        return (List<TEduBdRoleDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdRoleDo>(){

            @Override
            public TEduBdRoleDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdRoleDo tebrDo = new TEduBdRoleDo();
            	tebrDo.setRoleName(rs.getString("role_name"));
                return tebrDo;
            }
        });
    }
    
    //从数据源ds2的数据表t_edu_bd_role中查找所有角色信息
    public List<TEduBdRoleDo> getAllBdRoleInfo() {
    	if(jdbcTemplate2 == null)
    		return null;
    	String sql = "select id, role_type, role_name, create_time, last_update_time, creator, discrip, stat from t_edu_bd_role where" + " stat = 1";    	
    	logger.info("执行的MySql语句：" + sql);       
        return (List<TEduBdRoleDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdRoleDo>(){

            @Override
            public TEduBdRoleDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdRoleDo tebrDo = new TEduBdRoleDo();
            	tebrDo.setId(rs.getString("id"));
            	tebrDo.setRoleType(rs.getInt("role_type"));
            	tebrDo.setRoleName(rs.getString("role_name"));
            	tebrDo.setCreateTime(rs.getString("create_time"));
            	tebrDo.setLastUpdateTime(rs.getString("last_update_time"));
            	tebrDo.setCreator(rs.getString("creator"));
            	tebrDo.setDiscrip(rs.getString("discrip"));
            	tebrDo.setStat(rs.getInt("stat"));
                return tebrDo;
            }
        });
    }    
    
    //删除数据源ds2的数据表t_edu_bd_role中记录以角色名
    public boolean DeleteBdRoleInfoByRoleName(String roleName) {
    	if(jdbcTemplate2 == null || roleName == null)
    		return false;    	
    	String sql = "delete from t_edu_bd_role where role_name = " + "'" + roleName + "'";
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //插入记录到数据源ds2的数据表t_edu_bd_user_perm中
    public boolean InsertBdUserPermInfo(TEduBdUserPermDo tebupDo) {
    	if(jdbcTemplate2 == null || tebupDo == null)
    		return false;    	
    	String sql = "", sql1 = "insert into t_edu_bd_user_perm(", sql2 = " values(";    	
    	//角色id，表t_edu_bd_user的主键id
    	if(tebupDo.getUserId() != null) {
    		sql1 += "user_id";
    		sql2 += "'" + tebupDo.getUserId() + "'";
    	}
    	//权限id，数据权限则为表t_edu_bd_data_perm的主键id，菜单权限则为表t_edu_bd_menu的主键id
    	if(tebupDo.getPermId() != null) {
    		sql1 += ", perm_id";
    		sql2 += ", '" + tebupDo.getPermId() + "'";
    	}
    	//权限类型，1:数据权限，2:菜单权限
    	if(tebupDo.getPermType() != null) {
    		sql1 += ", perm_type";
    		sql2 += ", " + tebupDo.getPermType();
    	}               
    	//创建时间
    	if(tebupDo.getCreateTime() != null) {
    		sql1 += ", create_time";
    		sql2 += ", '" + tebupDo.getCreateTime() + "'";
    	}               
    	//更新时间
    	if(tebupDo.getUpdateTime() != null) {
    		sql1 += ", update_time";
    		sql2 += ", '" + tebupDo.getUpdateTime() + "'";
    	}
    	sql1 += ")";
    	sql2 += ")";
    	sql = sql1 + sql2;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //插入记录到数据源ds2的数据表t_edu_bd_user_perm中
    public boolean InsertBdUserPermInfo(List<TEduBdUserPermDo> tebupDoList) {
    	if(jdbcTemplate2 == null || tebupDoList == null)
    		return false;    	
    	if(tebupDoList.size() == 0)
    		return false;
    	String sql = "", sql1 = "insert into t_edu_bd_user_perm(", sql2 = " values(";
    	TEduBdUserPermDo tebupDo = tebupDoList.get(0);
    	//角色id，表t_edu_bd_user的主键id
    	if(tebupDo.getUserId() != null) {
    		sql1 += "user_id";
    		sql2 += "'" + tebupDo.getUserId() + "'";
    	}
    	//权限id，数据权限则为表t_edu_bd_data_perm的主键id，菜单权限则为表t_edu_bd_menu的主键id
    	if(tebupDo.getPermId() != null) {
    		sql1 += ", perm_id";
    		sql2 += ", '" + tebupDo.getPermId() + "'";
    	}
    	//权限类型，1:数据权限，2:菜单权限
    	if(tebupDo.getPermType() != null) {
    		sql1 += ", perm_type";
    		sql2 += ", " + tebupDo.getPermType();
    	}               
    	//创建时间
    	if(tebupDo.getCreateTime() != null) {
    		sql1 += ", create_time";
    		sql2 += ", '" + tebupDo.getCreateTime() + "'";
    	}               
    	//更新时间
    	if(tebupDo.getUpdateTime() != null) {
    		sql1 += ", update_time";
    		sql2 += ", '" + tebupDo.getUpdateTime() + "'";
    	}
    	sql1 += ")";
    	sql2 += ")";
    	for(int i = 1; i < tebupDoList.size(); i++) {
    		tebupDo = tebupDoList.get(i);
    		sql2 += ", (";
    		sql2 += "'" + tebupDo.getUserId() + "'";
    		sql2 += ", '" + tebupDo.getPermId() + "'";
    		sql2 += ", " + tebupDo.getPermType();
    		sql2 += ", '" + tebupDo.getCreateTime() + "'";
    		sql2 += ", '" + tebupDo.getUpdateTime() + "'";
    		sql2 += ")";
    	}
    	sql = sql1 + sql2;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //更新记录到数据源ds2的数据表t_edu_bd_user_perm中以输入字段
    public boolean UpdateBdRolePermInfoByField(TEduBdUserPermDo tebupDo, String fieldName, String fieldVal) {
    	if(jdbcTemplate2 == null || tebupDo == null)
    		return false;    	
    	String sql = "", sql1 = "update t_edu_bd_user_perm set", sql2 = " ", sql3 = " where " + fieldName + " = " + "'" + fieldVal + "'" + " and stat = 1";
        boolean headFlag = false;        
    	//主键id
    	if(tebupDo.getId() != null) {
    		sql2 += "id=";
    		sql2 += "'" + tebupDo.getId() + "'";
    		headFlag = true;
    	}
    	//角色id，表t_edu_bd_user的主键id
    	if(tebupDo.getUserId() != null) {
    		if(headFlag)
    			sql2 += ", user_id=";
    		else {
    			sql2 += "user_id=";
    			headFlag = true;
    		}
    		sql2 += tebupDo.getUserId();
    	}    	
    	//权限id，数据权限则为表t_edu_bd_data_perm的主键id，菜单权限则为表t_edu_bd_menu的主键id
    	if(tebupDo.getPermId() != null) {
    		if(headFlag)
    			sql2 += ", perm_id=";
    		else {
    			sql2 += "perm_id=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebupDo.getPermId() + "'";
    	}
    	//权限类型，1:数据权限，2:菜单权限
    	if(tebupDo.getPermType() != null) {
    		if(headFlag)
    			sql2 += ", perm_type=";
    		else {
    			sql2 += "perm_type=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebupDo.getPermType() + "'";
    	}               
    	//更新时间
    	if(tebupDo.getUpdateTime() != null) {
    		if(headFlag)
    			sql2 += ", update_time=";
    		else {
    			sql2 += "update_time=";
    			headFlag = true;
    		}
    		sql2 += "'" + tebupDo.getUpdateTime() + "'";
    	} 
    	//是否有效 1 有效 0 无效
    	if(tebupDo.getStat() != null) {
    		if(headFlag)
    			sql2 += ", stat=";
    		else {
    			sql2 += "stat=";
    			headFlag = true;
    		}
    		sql2 += tebupDo.getStat();
    	}
    	sql = sql1 + sql2 + sql3;
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //删除数据源ds2的数据表t_edu_bd_user_perm中记录以用户名
    public boolean DeleteBdUserPermInfoByUserId(String userId) {
    	if(jdbcTemplate2 == null || userId == null)
    		return false;    	
    	String sql = "delete from t_edu_bd_user_perm where user_id = " + "'" + userId + "'";
    	logger.info("执行的MySql语句：" + sql);
        jdbcTemplate2.execute(sql);
    	
    	return true;
    }
    
    //从数据源ds2的数据表t_edu_bd_user_perm中查找所有用户权限信息
    public List<TEduBdUserPermDo> getAllBdUserPermInfo(String userId, int permType) {
    	if(jdbcTemplate2 == null)
    		return null;
    	String sql = "select id, user_id, perm_id, perm_type from t_edu_bd_user_perm where" + " stat = 1" + " and user_id = " + "'" + userId + "'" + " and perm_type = " + permType;    	
    	logger.info("执行的MySql语句：" + sql);       
        return (List<TEduBdUserPermDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdUserPermDo>(){

            @Override
            public TEduBdUserPermDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdUserPermDo tebupDo = new TEduBdUserPermDo();
            	tebupDo.setId(rs.getLong("id"));
            	tebupDo.setUserId(rs.getString("user_id"));
            	tebupDo.setPermId(rs.getString("perm_id"));
            	tebupDo.setPermType(rs.getInt("perm_type"));
                return tebupDo;
            }
        });
    }    
    
    //从数据源ds2的数据表t_edu_bd_menu中查找菜单信息以菜单级别
    public List<TEduBdMenuDo> getBdMenuInfoByLevel(int level) {
    	if(jdbcTemplate2 == null)
    		return null;
        String sql = null;
        sql = "select distinct id, menu_name, level, parent_id, parent_name, menu_type, descript, stat from t_edu_bd_menu" + " where stat = 1" + " and level = " + level;
        logger.info("sql语句：" + sql);
        return (List<TEduBdMenuDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdMenuDo>(){

            @Override
            public TEduBdMenuDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdMenuDo tebmpDo = new TEduBdMenuDo();
            	tebmpDo.setId(rs.getString("id"));
            	tebmpDo.setMenuName(rs.getString("menu_name"));
            	tebmpDo.setLevel(rs.getInt("level"));
            	tebmpDo.setParentId(rs.getString("parent_id"));
            	tebmpDo.setParentName(rs.getString("parent_name"));
            	tebmpDo.setMenuType(rs.getInt("menu_type"));
            	tebmpDo.setDescript(rs.getString("descript"));
            	tebmpDo.setStat(rs.getInt("stat"));
                return tebmpDo;
            }
        });
    }
    
    //从数据源ds2的数据表t_edu_bd_menu中查找菜单信息以菜单级别和父菜单ID
    public List<TEduBdMenuDo> getBdMenuInfoByLevel(int level, String parentId) {
    	if(jdbcTemplate2 == null)
    		return null;
        String sql = null;
        sql = "select distinct id, menu_name, level, parent_id, parent_name, menu_type, descript, stat from t_edu_bd_menu" + " where stat = 1" + " and level = " + level + " and parent_id = " + "'" + parentId + "'";
        logger.info("sql语句：" + sql);
        return (List<TEduBdMenuDo>) jdbcTemplate2.query(sql, new RowMapper<TEduBdMenuDo>(){

            @Override
            public TEduBdMenuDo mapRow(ResultSet rs, int rowNum) throws SQLException {
            	TEduBdMenuDo tebmpDo = new TEduBdMenuDo();
            	tebmpDo.setId(rs.getString("id"));
            	tebmpDo.setMenuName(rs.getString("menu_name"));
            	tebmpDo.setLevel(rs.getInt("level"));
            	tebmpDo.setParentId(rs.getString("parent_id"));
            	tebmpDo.setParentName(rs.getString("parent_name"));
            	tebmpDo.setMenuType(rs.getInt("menu_type"));
            	tebmpDo.setDescript(rs.getString("descript"));
            	tebmpDo.setStat(rs.getInt("stat"));
                return tebmpDo;
            }
        });
    }
    
    //插入消息通知记录
    public int insertMsgNotice(TEduBdMsgNoticeDo tebmnDo) {
  		return tebmnDoMapper.insertMsgNotice(tebmnDo);
  	}
    
    //获取所有用户名、单位ID、单位名称记录信息
    public List<EduBdUserDo> getAllUserInfos() {
    	return ebuDoMapper.getAllUserInfos();
    }
    
    //插入消息通知状态记录
    public int insertMsgNoticeStatus(TEduBdNoticeStatusDo tebnsDo) {
  		return tebnsDoMapper.insertMsgNoticeStatus(tebnsDo);
  	}
    
    //查询消息通知状态记录列表以接收用户名
    public List<TEduBdNoticeStatusDo> getMsgNoticeStatusByRcvUserName(String rcvUserName) {
  		return tebnsDoMapper.getMsgNoticeStatusByRcvUserName(rcvUserName);
  	}
    
    //查询消息通知记录以通知id
    public TEduBdMsgNoticeDo getMsgNoticeById(String id) {
    	return tebmnDoMapper.getMsgNoticeById(id);
    }
    
    //查询消息通知状态记录列表以接收用户名
    public List<TEduBdNoticeStatusDo> getMsgNoticeStatusBySendUserName(String sendUserName) {
  		return tebnsDoMapper.getMsgNoticeStatusBySendUserName(sendUserName);
  	}
    
    //查询消息通知状态记录列表以通知ID和发布用户名
    public List<TEduBdNoticeStatusDo> getMsgNoticeStatusBybIdSendUser(String bulletinId, String sendUserName) {
  		return tebnsDoMapper.getMsgNoticeStatusBybIdSendUser(bulletinId, sendUserName);
  	}
    
    //查询消息通知状态记录列表以通知id和接收用户名
    public TEduBdNoticeStatusDo getMsgNoticeStatusBybIdRcvUserName(String bulletinId, String rcvUserName) {
  		return tebnsDoMapper.getMsgNoticeStatusBybIdRcvUserName(bulletinId, rcvUserName);
  	}
  	
  	//更新阅读次数
    public int updateReadCountInMsgNotice(String bulletinId, String rcvUserName, int readCount) {
  		return tebnsDoMapper.updateReadCountInMsgNotice(bulletinId, rcvUserName, readCount);
  	}

    //查询消息通知当前上一条记录以当前通知id
    public TEduBdMsgNoticeDo getPreMsgNoticeById(String id) {
  		return tebmnDoMapper.getPreMsgNoticeById(id);
  	}
    
    //查询消息通知当前下一条记录以当前通知id
    public TEduBdMsgNoticeDo getNextMsgNoticeById(String id) {
    	return tebmnDoMapper.getNextMsgNoticeById(id);
  	}
    
    //查询所有子用户记录信息以父用户id
    public List<EduBdUserDo> getAllSubUserInfosByParentId(String parentId) {
    	return ebuDoMapper.getAllSubUserInfosByParentId(parentId);
    }
    
    //查询用户记录信息以用户id
    public EduBdUserDo getUserInfoByUserId(String id) {
    	return ebuDoMapper.getUserInfoByUserId(id);
    }
    
    //查询消息通知当前上一条记录以当前通知id和接收用户名（接收用户名字串前后添加%）
  	public TEduBdMsgNoticeDo getPreMsgNoticeByIdRcvUserName(String id, String rcvUserName) {
  		return tebmnDoMapper.getPreMsgNoticeByIdRcvUserName(id, rcvUserName);
  	}
  	
  	//查询消息通知当前下一条记录以当前通知id和接收用户名（接收用户名字串前后添加%）
  	public TEduBdMsgNoticeDo getNextMsgNoticeByIdRcvUserName(String id, String rcvUserName) {
  		return tebmnDoMapper.getNextMsgNoticeByIdRcvUserName(id, rcvUserName);
  	}  	
  	
  	//查询消息通知当前上一条记录以当前通知id和接收用户名（发送用户名）
  	public TEduBdMsgNoticeDo getPreMsgNoticeByIdSendUserName(String id, String sendUserName) {
  		return tebmnDoMapper.getPreMsgNoticeByIdSendUserName(id, sendUserName);
  	}
  			
  	//查询消息通知当前下一条记录以当前通知id和接收用户名（发送用户名）
  	public TEduBdMsgNoticeDo getNextMsgNoticeByIdSendUserName(String id, String sendUserName) {
  		return tebmnDoMapper.getNextMsgNoticeByIdSendUserName(id, sendUserName);
  	}
  	
  	//查询邮件服务记录以用户名
  	public TEduBdMailSrvDo getMailSrvInfoByUserName(String userName) {
  		return tebmsDoMapper.getMailSrvInfoByUserName(userName);
  	}
  	
  	//插入邮件服务记录
  	public int insertMailSrv(TEduBdMailSrvDo tebmsDo) {
  		return tebmsDoMapper.insertMailSrv(tebmsDo);
  	}
  	
  	//更新邮件服务记录
  	public boolean updateMailSrv(TEduBdMailSrvDo tebmsDo) {
  		boolean retFlag = false;
  		if(tebmsDo.getUserName() != null) {
  			retFlag = true;
  			//更新邮件用户名
  			if(tebmsDo.getEmail() != null)
  				tebmsDoMapper.updateEmail(tebmsDo.getUserName(), tebmsDo.getEmail());
  			//更新密码以用户名
  			if(tebmsDo.getPassword() != null)
  				tebmsDoMapper.updatePassword(tebmsDo.getUserName(), tebmsDo.getPassword());  			
  			//更新接收服务器以用户名
  			if(tebmsDo.getRcvServer() != null)
  				tebmsDoMapper.updateRcvServer(tebmsDo.getUserName(), tebmsDo.getRcvServer());  			
  			//更新接收服务端口以用户名
  			if(tebmsDo.getRcvSrvPort() != null)
  				tebmsDoMapper.updateRcvSrvPort(tebmsDo.getUserName(), tebmsDo.getRcvSrvPort());  			
  			//更新接收服务端口号以用户名
  			if(tebmsDo.getRcvSrvPortNo() != null)
  				tebmsDoMapper.updateRcvSrvPortNo(tebmsDo.getUserName(), tebmsDo.getRcvSrvPortNo());  			
  			//更新发送服务器以用户名
  			if(tebmsDo.getSendServer() != null)
  				tebmsDoMapper.updateSendServer(tebmsDo.getUserName(), tebmsDo.getSendServer());  			
  			//更新发送服务端口以用户名
  			if(tebmsDo.getSendSrvPort() != null)
  				tebmsDoMapper.updateSendSrvPort(tebmsDo.getUserName(), tebmsDo.getSendSrvPort());  			
  			//更新发送服务端口号以用户名
  			if(tebmsDo.getSendSrvPortNo() != null)
  				tebmsDoMapper.updateSendSrvPortNo(tebmsDo.getUserName(), tebmsDo.getSendSrvPortNo());  			
  			//更新有效标识以用户名
  			if(tebmsDo.getStat() != null)
  				tebmsDoMapper.updateStat(tebmsDo.getUserName(), tebmsDo.getStat());
  		}
  		
  		return retFlag;
  	}
  	
  	//查询学校视频监控记录信息以学校id
    public List<TEduBdBriKitStoveDo> getSchVidSurvInfosBySchId(String schoolId) {
    	return tebbksDoMapper.getSchVidSurvInfosBySchId(schoolId);
    }
    
    //查询所有学校视频监控记录信息
    public List<TEduBdBriKitStoveDo> getAllSchVidSurvInfos() {
    	return tebbksDoMapper.getAllSchVidSurvInfos();
    }
    
    //查询学校视频监控记录信息以区域id
    public List<TEduBdBriKitStoveDo> getSchVidSurvInfosByDistId(String regionId) {
    	return tebbksDoMapper.getSchVidSurvInfosByDistId(regionId);
    }
    
    
    /**
     * 插入用户设置动态列
     * @param record
     * @return
     */
    public int addUserInterfaceColums(EduBdInterfaceColumnsDo record) {
    	return columnsMapper.insert(record);
    }

    /**
     * 根据主键修改用户设置的动态列
     * @param record
     * @return
     */
    public int updateUserInterfaceColumsByPrimaryKey(EduBdInterfaceColumnsDo record) {
    	return columnsMapper.updateByPrimaryKey(record);
    };
    
    
    /**
     * 根据接口名称查询对应的列设置
     * @param interfaceName
     * @return
     */
    public EduBdInterfaceColumnsDo getByInterfaceName(String userId,String interfaceName) {
    	return columnsMapper.selectByInterfaceName(userId,interfaceName);
    }
    
    /**
     * 根据接口名称查询对应的列设置
     * @param interfaceName
     * @return
     */
    public Integer getInterfaceColumnsMaxId() {
    	return columnsMapper.selectMaxId();
    }
}
