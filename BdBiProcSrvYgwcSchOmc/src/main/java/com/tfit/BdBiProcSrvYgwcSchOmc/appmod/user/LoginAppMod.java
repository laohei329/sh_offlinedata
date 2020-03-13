package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSuperviseUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTHttpRspVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTRspType;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//用户登录应用模型
public class LoginAppMod {
	private static final Logger logger = LogManager.getLogger(LoginAppMod.class.getName());
	
	//方法类型索引
	int methodIndex = 1;
	
	//登录方式（方案1）
	private IOTHttpRspVO loginMethod0(String userName, String password, Db1Service db1Service) {
		IOTHttpRspVO normResp = null;
  		//按参数形式处理
  		if(userName != null && password != null && db1Service != null) {
  			String token = null, checkStr = null;
  			TEduSuperviseUserDo tesuDo = db1Service.getUserNamePassByUserName(userName);
  			
  			if(tesuDo==null || tesuDo.getUserAccount() ==null || "".equals(tesuDo.getUserAccount())) {
  				Map<String,String> data = new HashMap<String,String>();
				data.put("token", null);      //返回数据对象相关值
				data.put("distId", null);//区域ID，为null表示拥有所有权限
				normResp = AppModConfig.getNormalResp(data,IOTRspType.USERACCOUNT_NOTEXIST_ERR.getCode(),IOTRspType.USERACCOUNT_NOTEXIST_ERR.getMsg());
				logger.info("账号：" + userName + "不存在！");
				return normResp;
  			}
  			
  			if(tesuDo != null) {
  				checkStr = "userName=" + tesuDo.getUserAccount() + "&password=" + tesuDo.getPassword() + "&time=" + BCDTimeUtil.convertNormalFrom(null);
  				if(tesuDo.getPassword().compareTo(password) == 0) {
  					token = DigestUtils.sha1Hex(checkStr);
  				}else {
  					Map<String,String> data = new HashMap<String,String>();
  					data.put("token", null);      //返回数据对象相关值
  					data.put("distId", null);//区域ID，为null表示拥有所有权限
  					normResp = AppModConfig.getNormalResp(data,IOTRspType.USERACCOUNTPASSWORD_ERR.getCode(),IOTRspType.USERACCOUNTPASSWORD_ERR.getMsg());
  					logger.info("账号或密码错误！");
  					return normResp;
  				}
  			}
  			if(token != null) {
  	    	    //更新生成的token到数据源ds1的数据表t_edu_supervise_user表中
  				boolean flag = db1Service.updateUserTokenToTEduSuperviseUser(userName, password, token);
  	    	    if(flag) {
  	  	    	    flag = AppModConfig.verifyAuthCode(token, db1Service);
  	  	    	    if(flag) {
  	  	    	    	Map<String,String> data = new HashMap<String,String>();
  	  	    	    	data.put("token", token);      //返回数据对象相关值
  	  	    	    	normResp = AppModConfig.getNormalResp(data);
  	  	    	    	logger.info("授权码：" + token + "，更新成功！");
  	  	    	    }
  	  	    	    else
  	  	    	    	logger.info("授权码：" + token + "，更新失败！");
  	    	    }
  	    	    else
  	    	    	logger.info("授权码：" + token + "，更新失败！");
  			}
  		}
  		else {
  			logger.info("访问接口参数非法！");
  		}
  		
  		return normResp;
	}
	
	//登录方式（方案2）
	private IOTHttpRspVO loginMethod1(String userName, String password, Db1Service db1Service, Db2Service db2Service, int[] codes) {
		IOTHttpRspVO normResp = null;
		//按参数形式处理
  		if(userName != null && password != null && db2Service != null) {
  			String token = null, checkStr = null;
  			TEduBdUserDo tebuDo = db2Service.getBdUserInfoByUserName(userName); 
  			if(tebuDo==null || tebuDo.getUserAccount() ==null || "".equals(tebuDo.getUserAccount())) {
  				Map<String,String> data = new HashMap<String,String>();
				data.put("token", null);      //返回数据对象相关值
				data.put("distId", null);//区域ID，为null表示拥有所有权限
				normResp = AppModConfig.getNormalResp(data,IOTRspType.USERACCOUNT_NOTEXIST_ERR.getCode(),IOTRspType.USERACCOUNT_NOTEXIST_ERR.getMsg());
				logger.info("账号：" + userName + "不存在！");
				return normResp;
  			}
  			if(tebuDo != null) {
  				checkStr = "userName=" + tebuDo.getUserAccount() + "&password=" + tebuDo.getPassword() + "&time=" + BCDTimeUtil.convertNormalFrom(null);
  				if(tebuDo.getPassword().equals(password)) {
  					token = DigestUtils.sha1Hex(checkStr);
  				}else {
  					Map<String,String> data = new HashMap<String,String>();
  					data.put("token", null);      //返回数据对象相关值
  					data.put("distId", null);//区域ID，为null表示拥有所有权限
  					normResp = AppModConfig.getNormalResp(data,IOTRspType.USERACCOUNTPASSWORD_ERR.getCode(),IOTRspType.USERACCOUNTPASSWORD_ERR.getMsg());
  					logger.info("账号或密码错误！");
  					return normResp;
  				}
  			}
  			if(token != null) {
  				if(tebuDo.getForbid() == 1) {    //账户启用状态
  					//更新记录到数据源ds1的数据表t_edu_bd_user中以输入字段
  					TEduBdUserDo inTebuDo = new TEduBdUserDo();
  					inTebuDo.setToken(token);
  					inTebuDo.setLastLoginTime(BCDTimeUtil.convertNormalFrom(null));
  					inTebuDo.setLastUpdateTime(BCDTimeUtil.convertNormalFrom(null));
  					boolean flag = db2Service.UpdateBdUserInfoByField(inTebuDo, "user_account", userName);
  					if(flag) {
  						flag = AppModConfig.verifyAuthCode2(token, db2Service, codes);
  						if(flag) {
  							Map<String,String> data = new HashMap<String,String>();
  							data.put("token", token);      //返回数据对象相关值
  							String distIdorSCName = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  //获取用户权限区域ID
  							data.put("token", token);      //返回数据对象相关值
  							data.put("distId", distIdorSCName);
  							
  							normResp = AppModConfig.getNormalResp(data);
  							logger.info("授权码：" + token + "，更新成功！");
  						}
  						else {
  							codes[0] = 2003;
  							logger.info("授权码：" + token + "，更新失败！");
  						}
  					}
  					else {
  						codes[0] = 2003;
  						logger.info("授权码：" + token + "，更新失败！");
  					}
  				}
  				else {    //账户禁用状态
  					codes[0] = 2035;
  					logger.info("账号：" + userName + "，被禁用！");
  				}
  			}
  		}
  		else {
  			codes[0] = 2017;
  			logger.info("访问接口参数非法！");
  		}
		
		return normResp;
	}
	
	//用户登录应用模型函数
  	public IOTHttpRspVO appModFunc(String userName, String password, Db1Service db1Service, Db2Service db2Service, int[] codes) {
  		IOTHttpRspVO normResp = null;
  		if(methodIndex == 0)
  			normResp = loginMethod0(userName, password, db1Service);
  		else if(methodIndex == 1)
  			normResp = loginMethod1(userName, password, db1Service, db2Service, codes);
  		
  		return normResp;
  	}
}
