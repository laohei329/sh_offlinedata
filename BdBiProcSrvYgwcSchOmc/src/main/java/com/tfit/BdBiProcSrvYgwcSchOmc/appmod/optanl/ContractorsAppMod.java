package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.Contractors;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.ContractorsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//承办人列表应用模型
public class ContractorsAppMod {
	private static final Logger logger = LogManager.getLogger(ContractorsAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	
	//数组数据初始化
	String[] contractor_Array = {"徐汇区教育局", "黄浦区教育局", "静安区教育局", "长宁区教育局", "普陀区教育局", "虹口区教育局", "杨浦区教育局", "闵行区教育局", "嘉定区教育局", "宝山区教育局", "浦东新区教育局", "松江区教育局", "金山区教育局", "青浦区教育局", "奉贤区教育局", "崇明区教育局"};
	String[] contractorId_Array = {"11", "1", "10", "12", "13", "14", "15", "16", "2", "3", "4", "5", "6", "7", "8", "9"};		
	
	//模拟数据函数
	private ContractorsDTO SimuDataFunc() {
		ContractorsDTO ctsDto = new ContractorsDTO();
		//时戳
		ctsDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//承办人列表模拟数据
		List<Contractors> contractors = new ArrayList<>();
		//赋值
		for (int i = 0; i < contractor_Array.length; i++) {
			Contractors cts = new Contractors();
			cts.setContractor(contractor_Array[i]);
			cts.setContractorId(contractorId_Array[i]);
			contractors.add(cts);
		}
		//设置数据
		ctsDto.setContractors(contractors);
		//消息ID
		ctsDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return ctsDto;
	}
	
	// 承办人列表模型函数
	public ContractorsDTO appModFunc(String distName, String prefCity, String province, Db1Service db1Service) {
		ContractorsDTO ctsDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			ctsDto = SimuDataFunc();
		}		

		return ctsDto;
	}
}
