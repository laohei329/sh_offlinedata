package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrProcStatus;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrProcStatusDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//投诉举报处理状态列表应用模型
public class CrProcStatusAppMod {
	private static final Logger logger = LogManager.getLogger(CrProcStatusAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	
	//数组数据初始化
	String[] procName_Array = {"待处理", "已指派", "已办结"};
	int[] procStatus_Array = {0, 1, 2};	
	
	//模拟数据函数
	private CrProcStatusDTO SimuDataFunc() {
		CrProcStatusDTO cpsDto = new CrProcStatusDTO();
		//时戳
		cpsDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//投诉举报处理状态列表模拟数据
		List<CrProcStatus> contractors = new ArrayList<>();
		//赋值
		for (int i = 0; i < procName_Array.length; i++) {
			CrProcStatus cps = new CrProcStatus();
			cps.setProcName(procName_Array[i]);
			cps.setProcStatus(procStatus_Array[i]);
			contractors.add(cps);
		}
		//设置数据
		cpsDto.setCrProcStatus(contractors);
		//消息ID
		cpsDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return cpsDto;
	}
	
	// 投诉举报处理状态列表模型函数
	public CrProcStatusDTO appModFunc(String distName, String prefCity, String province, Db1Service db1Service) {
		CrProcStatusDTO cpsDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			cpsDto = SimuDataFunc();
		}		

		return cpsDto;
	}
}