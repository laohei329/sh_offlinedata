package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.bd;

import java.util.ArrayList;
import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.FblMbCodesDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//证件主体编码列表应用模型
public class FblMbCodesAppMod {
	//是否为真实数据标识
	private static boolean isRealData = true;
	
	//数组数据初始化
	String[] name_Array = {"学校", "外包"};
	String[] code_Array = {"0", "1"};
		
	//模拟数据函数
	private FblMbCodesDTO SimuDataFunc() {
		FblMbCodesDTO fmcDto = new FblMbCodesDTO();
		//时戳
		fmcDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//证件主体编码列表模拟数据
		List<NameCode> fblMbCodes = new ArrayList<>();
		//赋值
		for (int i = 0; i < name_Array.length; i++) {
			NameCode fmc = new NameCode();
			fmc.setName(name_Array[i]);
			fmc.setCode(code_Array[i]);			
			fblMbCodes.add(fmc);
		}
		//设置数据
		fmcDto.setFblMbCodes(fblMbCodes);
		//消息ID
		fmcDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return fmcDto;
	}
	
	// 证件主体编码列表模型函数
	public FblMbCodesDTO appModFunc(String distName, String prefCity, String province, Db1Service db1Service) {
		FblMbCodesDTO fmcDto = null;
		// 省或直辖市
		if(province == null)
			province = "上海市";
		if(isRealData) {       //真实数据
			fmcDto = new FblMbCodesDTO();
			//时戳
			fmcDto.setTime(BCDTimeUtil.convertNormalFrom(null));
			//证件主体编码列表模拟数据
			List<NameCode> fblMbCodes = new ArrayList<>();
			//赋值
			for (int i = 0; i < name_Array.length; i++) {
				NameCode fmc = new NameCode();
				fmc.setName(name_Array[i]);
				fmc.setCode(code_Array[i]);
				fblMbCodes.add(fmc);
			}
			//设置数据
			fmcDto.setFblMbCodes(fblMbCodes);
			//消息ID
			fmcDto.setMsgId(AppModConfig.msgId);
			AppModConfig.msgId++;
			// 消息id小于0判断
			AppModConfig.msgIdLessThan0Judge();
		}
		else {    //模拟数据
			//模拟数据函数
			fmcDto = SimuDataFunc();
		}		

		return fmcDto;
	}
}
