package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.IsFsgDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.IsFsgDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//信息共享食品安全等级详情列表应用模型
public class IsFsgDetsAppMod {
	private static final Logger logger = LogManager.getLogger(IsFsgDetsAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String[] ppName_Array = {"上海市徐汇区向阳小学", "上海市徐汇区世界小学"};
	String[] schType_Array = {"小学", "小学"};
	int[] dinnerMod_Array = {2, 0};
	String[] compName_Array = {"上海绿捷实业发展有限公司", "上海龙神餐饮有限公司"};
	String[] licNo_Array = {"JY31026558588885", "JY31026558588857"};
	String[] compAddress_Array = {"上海市徐汇区襄阳南路388弄15号", "上海市徐汇区武康路280弄2号"};
	String[] lastInspDate_Array = {"2018-09-06", "2018-09-06"};
	int[] grade_Array = {0, 1};
	
	//模拟数据函数
	private IsFsgDetsDTO SimuDataFunc() {
		IsFsgDetsDTO ifdDto = new IsFsgDetsDTO();
		//时戳
		ifdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//信息共享食品安全等级详情列表模拟数据
		List<IsFsgDets> isFsgList = new ArrayList<>();
		//赋值
		for (int i = 0; i < ppName_Array.length; i++) {
			IsFsgDets ifd = new IsFsgDets();
			ifd.setPpName(ppName_Array[i]);
			ifd.setSchType(schType_Array[i]);
			ifd.setDinnerMod(dinnerMod_Array[i]);
			ifd.setCompName(compName_Array[i]);
			ifd.setLicNo(licNo_Array[i]);
			ifd.setCompAddress(compAddress_Array[i]);
			ifd.setLastInspDate(lastInspDate_Array[i]);
			ifd.setGrade(grade_Array[i]);
			isFsgList.add(ifd);
		}
		//设置数据
		ifdDto.setIsFsgDets(isFsgList);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = ppName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		ifdDto.setPageInfo(pageInfo);
		//消息ID
		ifdDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return ifdDto;
	}
	
	// 信息共享食品安全等级详情列表模型函数
	public IsFsgDetsDTO appModFunc(String ppName, String schType, String dinnerMod, String grade, String distName, String prefCity, String province, String page, String pageSize, Db1Service db1Service) {
		IsFsgDetsDTO ifdDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			ifdDto = SimuDataFunc();
		}		

		return ifdDto;
	}
}