package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.IsFsgList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.IsFsgListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//信息共享食品安全等级列表应用模型
public class IsFsgListAppMod {
	private static final Logger logger = LogManager.getLogger(IsFsgListAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String[] distName_Array = {"徐汇区", "黄浦区", "静安区", "长宁区", "杨浦区", "普陀区", "虹口区", "浦东新区", "闵行区", "宝山区", "嘉定区", "青浦区", "松江区", "奉贤区"};
	int[] ppNum_Array = {268, 151, 243, 140, 282, 200, 238, 237, 465, 365, 212, 179, 282, 200};
	String[] ppGoodNum_Array = {"250（94%）", "141（93%）", "231（95%）", "135（92%）", "275（96%）", "191（95%）", "233（91%）", "201（88%）", "452（95%）", "355（96%）", "204（92%）", "170（92%）", "275（96%）", "191（95%）"};
	String[] ppGeneralNum_Array = {"15（5.7%）", "8（4.1%）", "13（4.3%）", "4（7.3%）", "5（2.9%）", "6（3.9%）", "4（7.9%）", "34（11%）", "10（3.9%）", "8（4%）", "6（3.9%）", "6（3.9%）", "5（2.9%）", "6（3.9%）"};
	String[] ppLessNum_Array = {"3（0.3%）", "2（2.9%）", "1（0.7%）", "1（0.7%）", "2（1.1%）", "3（1.1%）", "11（1.1%）", "3（1%）", "3（1.1%）", "2（1%）", "2（4.1%）", "3（4.1%）", "2（1.1%）", "3（1.1%）"};
	
	//模拟数据函数
	private IsFsgListDTO SimuDataFunc() {
		IsFsgListDTO iflDto = new IsFsgListDTO();
		//时戳
		iflDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//信息共享食品安全等级列表模拟数据
		List<IsFsgList> isFsgList = new ArrayList<>();
		//赋值
		for (int i = 0; i < distName_Array.length; i++) {
			IsFsgList ifl = new IsFsgList();
			ifl.setDistName(distName_Array[i]);
			ifl.setPpNum(ppNum_Array[i]);
			ifl.setPpGoodNum(ppGoodNum_Array[i]);
			ifl.setPpGeneralNum(ppGeneralNum_Array[i]);
			ifl.setPpLessNum(ppLessNum_Array[i]);
			isFsgList.add(ifl);
		}
		//设置数据
		iflDto.setIsFsgList(isFsgList);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = distName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		iflDto.setPageInfo(pageInfo);
		//消息ID
		iflDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return iflDto;
	}
	
	// 信息共享食品安全等级列表模型函数
	public IsFsgListDTO appModFunc(String distName, String prefCity, String province, Db1Service db1Service) {
		IsFsgListDTO iflDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			iflDto = SimuDataFunc();
		}		

		return iflDto;
	}
}
