package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.user;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.UserStatusCodesDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//账号状态编码列表应用模型
public class UserStatusCodesAppMod {
	private static final Logger logger = LogManager.getLogger(UserStatusCodesAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	
	//数组数据初始化
	String[] name_Array = {"禁用", "启用"};
	String[] code_Array = {"0", "1"};
	
	//模拟数据函数
	private UserStatusCodesDTO SimuDataFunc() {
		UserStatusCodesDTO uscDto = new UserStatusCodesDTO();
		//时戳
		uscDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//账号状态编码列表模拟数据
		List<NameCode> dishNameCodes = new ArrayList<>();
		//赋值
		for (int i = 0; i < name_Array.length; i++) {
			NameCode usc = new NameCode();
			usc.setName(name_Array[i]);
			usc.setCode(code_Array[i]);
			dishNameCodes.add(usc);
		}
		//设置数据
		uscDto.setUserStatusCodes(dishNameCodes);
		//消息ID
		uscDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return uscDto;
	}
	
	// 账号状态编码列表模型函数
	public UserStatusCodesDTO appModFunc(Db1Service db1Service, int[] codes) {
		UserStatusCodesDTO uscDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			uscDto = SimuDataFunc();
		}		

		return uscDto;
	}
}
