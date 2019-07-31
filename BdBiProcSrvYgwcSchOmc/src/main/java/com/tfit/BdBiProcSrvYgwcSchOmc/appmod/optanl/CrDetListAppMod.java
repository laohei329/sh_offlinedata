package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrDetList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrDetListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//投诉举报详情列表应用模型
public class CrDetListAppMod {
	private static final Logger logger = LogManager.getLogger(CrDetListAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String [] crId_Array = {"12703e54-35cc-4db0-84a3-ca5e7c7d5ac7", "33803e54-35cc-4db0-84a3-ca5e7c7d5ac7", "44903e54-35cc-4db0-84a3-ca5e7c7d5ac7", "55a03e54-35cc-4db0-84a3-ca5e7c7d5ac7"};
	String [] subDate_Array = {"2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03"};
	String [] distName_Array = {"徐汇区", "徐汇区", "徐汇区", "徐汇区"};
	String [] title_Array = {"上海市**中学食堂卫生条件差", "上海市**小学食堂卫生条件差", "上海市**中学食堂卫生条件差", "上海市**小学食堂卫生条件差"};
	String [] schName_Array = {"上海市**中学", "上海市徐汇区**小学", "上海市**中学", "上海市徐汇区**小学"};
	String [] complainant_Array = {"匿名", "匿名", "匿名", "匿名"};
	String [] contractor_Array = {"", "", "徐汇区教育局", "徐汇区教育局"};
	int[] procStatus_Array = {0, 0, 1, 2};
	String [] handleDate_Array = {"", "", "", "2018-09-05"};
	
	//模拟数据函数
	private CrDetListDTO SimuDataFunc() {
		CrDetListDTO cdlDto = new CrDetListDTO();
		//时戳
		cdlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//投诉举报详情列表模拟数据
		List<CrDetList> crDetList = new ArrayList<>();
		//赋值
		for (int i = 0; i < crId_Array.length; i++) {
			CrDetList cdl = new CrDetList();
			cdl.setCrId(crId_Array[i]);
			cdl.setSubDate(subDate_Array[i]);
			cdl.setDistName(distName_Array[i]);
			cdl.setTitle(title_Array[i]);
			cdl.setSchName(schName_Array[i]);
			cdl.setComplainant(complainant_Array[i]);
			cdl.setContractor(contractor_Array[i]);
			cdl.setProcStatus(procStatus_Array[i]);
			cdl.setHandleDate(handleDate_Array[i]);
			crDetList.add(cdl);
		}
		//设置数据
		cdlDto.setCrDetList(crDetList);
		//分页信息
		PageInfo pageInfo = new PageInfo();
		pageTotal = crId_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		cdlDto.setPageInfo(pageInfo);
		//消息ID
		cdlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return cdlDto;
	}
	
	// 投诉举报详情列表模型函数
	public CrDetListDTO appModFunc(String subDate, String distName, String prefCity, String province, String schName, String contractor, int procStatus, Db1Service db1Service) {
		CrDetListDTO cdlDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			cdlDto = SimuDataFunc();
		}		

		return cdlDto;
	}
}
