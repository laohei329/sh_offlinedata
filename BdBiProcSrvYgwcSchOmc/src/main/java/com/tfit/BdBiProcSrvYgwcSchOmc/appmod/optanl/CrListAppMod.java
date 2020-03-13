package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//投诉举报列表应用模型
public class CrListAppMod {
	private static final Logger logger = LogManager.getLogger(CrListAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String[] subDate_Array = {"2018-08-15", "2018-08-14"};
	int[] crNum_Array = {20, 15};
	int[] noProcCrNum_Array = {15, 5};
    int[] assignCrNum_Array = {4, 8};
	int[] procCrNum_Array = {1, 2};
	float[] procCrRate_Array = {(float) 5.00, (float) 13.33};
	
	//模拟数据函数
	private CrListDTO SimuDataFunc() {
		CrListDTO crlDto = new CrListDTO();
		//时戳
		crlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//投诉举报列表模拟数据
		List<CrList> crList = new ArrayList<>();
		//赋值
		for (int i = 0; i < subDate_Array.length; i++) {
			CrList crl = new CrList();
			crl.setSubDate(subDate_Array[i]);
			crl.setCrNum(crNum_Array[i]);
			crl.setNoProcCrNum(noProcCrNum_Array[i]);
			crl.setAssignCrNum(assignCrNum_Array[i]);
			crl.setProcCrNum(procCrNum_Array[i]);
			crl.setProcCrRate(procCrRate_Array[i]);
			crList.add(crl);
		}
		//设置数据
		crlDto.setCrList(crList);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = subDate_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		crlDto.setPageInfo(pageInfo);
		//消息ID
		crlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return crlDto;
	}
	
	// 投诉举报列表模型函数
	public CrListDTO appModFunc(String distName, String prefCity, String province, String startSubDate, String endSubDate, Db1Service db1Service) {
		CrListDTO crlDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			crlDto = SimuDataFunc();
		}		

		return crlDto;
	}
}
