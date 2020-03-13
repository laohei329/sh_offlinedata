package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.bd;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.BdSupList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd.BdSupListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//基础数据供应商列表应用模型
public class BdSupListAppMod {
	private static final Logger logger = LogManager.getLogger(BdSupListAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
		
	//数组数据初始化
	String[] supplierName_Array = {"上海清美食品有限公司", "上海清美食品有限公司"};
	String[] supplierProp_Array = {"民营", "民营"};
	String[] province_Array = {"上海市", "上海市"};
	String[] distCounty_Array = {"浦东新区", "浦东新区"};
	String[] detAddress_Array = {"上海市浦东新区宣桥镇三灶工业园区宣春路201号", "上海市浦东新区宣桥镇三灶工业园区宣春路201号"};
	String[] blNo_Array = {"310225000140649", "310225000140649"};
	String[] fblNo_Array = {"JY1561655500555", "JY1561655500555"};
	String[] fcpNo_Array = {"LT1561655500555", "LT1561655500555"};
	String[] fplNo_Array = {"SCKX1561655500555", "SCKX1561655500555"};
	int[] supplierType_Array = {0, 0};
	int[] regCapital_Array = {8649, 8649};
	int[] relRmcNum_Array = {2, 2};
	int[] supSchNum_Array = {20, 20};
	
	//模拟数据函数
	private BdSupListDTO SimuDataFunc() {
		BdSupListDTO bslDto = new BdSupListDTO();
		//时戳
		bslDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//基础数据供应商列表模拟数据
		List<BdSupList> bdSupList = new ArrayList<>();
		//赋值
		for (int i = 0; i < supplierName_Array.length; i++) {
			BdSupList bsl = new BdSupList();
			bsl.setSupplierName(supplierName_Array[i]);
			bsl.setSupplierType(supplierType_Array[i]);
			bsl.setSupplierProp(supplierProp_Array[i]);
			bsl.setProvince(province_Array[i]);
			bsl.setDistCounty(distCounty_Array[i]);
			bsl.setDetAddress(detAddress_Array[i]);
			bsl.setBlNo(blNo_Array[i]);
			bsl.setRegCapital(regCapital_Array[i]);
			bsl.setFblNo(fblNo_Array[i]);
			bsl.setFcpNo(fcpNo_Array[i]);
			bsl.setFplNo(fplNo_Array[i]);
			bsl.setRelRmcNum(relRmcNum_Array[i]);
			bsl.setSupSchNum(supSchNum_Array[i]);
			bdSupList.add(bsl);
		}
		//设置数据
		bslDto.setBdSupList(bdSupList);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = supplierName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		bslDto.setPageInfo(pageInfo);
		//消息ID
		bslDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return bslDto;
	}
	
	// 基础数据供应商列表模型函数
	public BdSupListDTO appModFunc(String supplierName, int supplierType, String distName, String prefCity, String province, String blNo, int regCapital, String fblNo, String fcpNo, Db1Service db1Service) {
		BdSupListDTO bslDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			bslDto = SimuDataFunc();
		}		

		return bslDto;
	}
}
