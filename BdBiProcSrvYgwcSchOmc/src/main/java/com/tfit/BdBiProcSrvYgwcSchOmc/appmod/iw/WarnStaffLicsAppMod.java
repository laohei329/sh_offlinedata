package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.iw;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageBean;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnStaffLics;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnStaffLicsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//证照预警人员证件列表应用模型
public class WarnStaffLicsAppMod {
	private static final Logger logger = LogManager.getLogger(WarnStaffLicsAppMod.class.getName());
	
	//Redis服务
	@Autowired
	RedisService redisService = new RedisService();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String[] warnPeriod_Array = {"2018/09/03-2018/09/04", "2018/09/03-2018/09/04", "2018/09/03-2018/09/04"};
	String[] distName_Array = {"11", "1", "10"};
	int[] totalWarnNum_Array = {100, 100, 100};
	int[] noProcWarnNum_Array = {80, 80, 80};
	int[] rejectWarnNum_Array = {0, 0, 0};
	int[] auditWarnNum_Array = {10, 10, 10};
	int[] elimWarnNum_Array = {10, 10, 10};
	float[] warnProcRate_Array = {(float) 10.00, (float) 10.00, (float) 10.00};
	
	//模拟数据函数
	private WarnStaffLicsDTO SimuDataFunc() {
		WarnStaffLicsDTO wstlDto = new WarnStaffLicsDTO();
		//时戳
		wstlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//证照预警人员证件列表模拟数据
		List<WarnStaffLics> warnStaffLics = new ArrayList<>();
		//赋值
		for (int i = 0; i < warnPeriod_Array.length; i++) {
			WarnStaffLics wstl = new WarnStaffLics();
			wstl.setWarnPeriod(warnPeriod_Array[i]);
			wstl.setDistName(distName_Array[i]);
			wstl.setTotalWarnNum(totalWarnNum_Array[i]);
			wstl.setNoProcWarnNum(noProcWarnNum_Array[i]);
			wstl.setRejectWarnNum(rejectWarnNum_Array[i]);
			wstl.setAuditWarnNum(auditWarnNum_Array[i]);
			wstl.setElimWarnNum(elimWarnNum_Array[i]);
			wstl.setWarnProcRate(warnProcRate_Array[i]);
			warnStaffLics.add(wstl);
		}
		//设置数据
		wstlDto.setWarnStaffLics(warnStaffLics);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = distName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		wstlDto.setPageInfo(pageInfo);
		//消息ID
		wstlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return wstlDto;
	}
	
	// 证照预警人员证件列表函数
	private WarnStaffLicsDTO warnStaffLics(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList, Db1Service db1Service) {
		WarnStaffLicsDTO wstlDto = new WarnStaffLicsDTO();
		List<WarnStaffLics> warnStaffLics = new ArrayList<>();
		WarnStaffLics wstl = null;
		String key = "", keyVal = "";
		int i, j, k;
		int distCount = tedList.size(), dateCount = dates.length, curWarnNum = 0;
		int[][] totalWarnNums = new int[dateCount][distCount+1], noProcWarnNums = new int[dateCount][distCount+1], rejectWarnNums = new int[dateCount][distCount+1], auditWarnNums = new int[dateCount][distCount+1], elimWarnNums = new int[dateCount][distCount+1];
		float[] warnProcRates = new float[distCount+1];
		//区域ID到索引映射
		Map<String, Integer> distIdToIdxMap = new HashMap<>();
		for(i = 0; i < distCount; i++) {
			distIdToIdxMap.put(tedList.get(i).getId(), i);
		}
		distIdToIdxMap.put("-", i);
		// 时间段预警总数
		Map<String, String> warnTotalMap = null;
		// 时间段内各区预警统计
		for (k = 0; k < dates.length; k++) {
			// 供应数量
			key = dates[k] + "_warn-total";
			warnTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(warnTotalMap == null) {    //Redis没有该数据则从hdfs系统中获取
				
			}
			if(warnTotalMap != null) {
				for(String curKey : warnTotalMap.keySet()) {
					String[] curKeys = curKey.split("_");
					if(curKeys.length == 5 && curKey.indexOf("people_") != -1) {
						i = AppModConfig.getVarValIndex(curKeys, "area");
						if(i != -1) {
							if(!curKeys[i].equalsIgnoreCase("null")) {
								if(distIdToIdxMap.containsKey(curKeys[i])) {
									int idx = distIdToIdxMap.get(curKeys[i]);
									keyVal = warnTotalMap.get(curKey);
									curWarnNum = 0;
									if(keyVal != null)
										curWarnNum = Integer.parseInt(keyVal);
									if(curWarnNum < 0)
										curWarnNum = 0;
									j = AppModConfig.getVarValIndex(curKeys, "status");
									if(j != -1) {
										if(curKeys[j].equalsIgnoreCase("1")) {         //未处理预警数
											noProcWarnNums[k][idx] += curWarnNum;
											totalWarnNums[k][idx] += curWarnNum;
										}
										else if(curKeys[j].equalsIgnoreCase("2")) {    //审核中预警数
											auditWarnNums[k][idx] += curWarnNum;
											totalWarnNums[k][idx] += curWarnNum;
										}
										else if(curKeys[j].equalsIgnoreCase("3")) {    //已驳回预警数
											rejectWarnNums[k][idx] += curWarnNum;
											totalWarnNums[k][idx] += curWarnNum;
										}
										else if(curKeys[j].equalsIgnoreCase("4")) {    //已消除预警数
											elimWarnNums[k][idx] += curWarnNum;
											totalWarnNums[k][idx] += curWarnNum;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		String startDate = dates[dates.length - 1].replaceAll("-", "/"), endDate = dates[0].replaceAll("-", "/");
		for (i = 0; i < distCount+1; i++) {
			String curDistId = "-";
			if(i < distCount) {
				TEduDistrictDo curTdd = tedList.get(i);
				curDistId = curTdd.getId();
			}
			// 判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
			if (distIdorSCName != null) {
				if (!curDistId.equals(distIdorSCName))
					continue;
			}
			wstl = new WarnStaffLics();
			wstl.setWarnPeriod(startDate + "-" + endDate);
			wstl.setDistName(curDistId);
			int totalWarnNum = 0, noProcWarnNum = 0, rejectWarnNum = 0, auditWarnNum = 0, elimWarnNum = 0;
			for (k = 0; k < dates.length; k++) {
				totalWarnNum += totalWarnNums[k][i];
				noProcWarnNum += noProcWarnNums[k][i];
				rejectWarnNum += rejectWarnNums[k][i];
				auditWarnNum += auditWarnNums[k][i];
				elimWarnNum += elimWarnNums[k][i];
			}			
			wstl.setTotalWarnNum(totalWarnNum);
			wstl.setNoProcWarnNum(noProcWarnNum);
			wstl.setRejectWarnNum(rejectWarnNum);
			wstl.setAuditWarnNum(auditWarnNum);
			wstl.setElimWarnNum(elimWarnNum);
			warnProcRates[i] = 0;
			if(totalWarnNum > 0) {
				warnProcRates[i] = 100 * ((float) elimWarnNum / (float) totalWarnNum);
				BigDecimal bd = new BigDecimal(warnProcRates[i]);
				warnProcRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				if (warnProcRates[i] > 100)
					warnProcRates[i] = 100;
			}
			wstl.setWarnProcRate(warnProcRates[i]);
			if(i == distCount && totalWarnNum == 0)   //无区号且预警数为0的不输出
				continue;
			warnStaffLics.add(wstl);
		}
		// 设置返回数据
		wstlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<WarnStaffLics> pageBean = new PageBean<WarnStaffLics>(warnStaffLics, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		wstlDto.setPageInfo(pageInfo);
		// 设置数据
		wstlDto.setWarnStaffLics(pageBean.getCurPageData());
		// 消息ID
		wstlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return wstlDto;
	}		
	
	// 证照预警人员证件列表模型函数
	public WarnStaffLicsDTO appModFunc(String token, String startWarnDate, String endWarnDate, String distName, String prefCity, String province, String page, String pageSize, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		WarnStaffLicsDTO wstlDto = null;
		if(page != null)
			curPageNum = Integer.parseInt(page);
		if(pageSize != null)
			this.pageSize = Integer.parseInt(pageSize);
		if(isRealData) {       //真实数据
			// 日期
			String[] dates = null;
			if (startWarnDate == null || endWarnDate == null) { // 按照当天日期获取数据
				dates = new String[1];
				dates[0] = BCDTimeUtil.convertNormalDate(null);
			} else { // 按照开始日期和结束日期获取数据
				DateTime startDt = BCDTimeUtil.convertDateStrToDate(startWarnDate);
				DateTime endDt = BCDTimeUtil.convertDateStrToDate(endWarnDate);
				int days = Days.daysBetween(startDt, endDt).getDays() + 1;
				dates = new String[days];
				for (int i = 0; i < days; i++) {
					dates[i] = endDt.minusDays(i).toString("yyyy-MM-dd");
				}
			}
			for (int i = 0; i < dates.length; i++) {
				logger.info("dates[" + i + "] = " + dates[i]);
			}
			// 省或直辖市
			if(province == null)
				province = "上海市";
			// 参数查找标识
			boolean bfind = false;
			String distIdorSCName = null;
			// 按不同参数形式处理
			if (distName != null && prefCity == null && province != null) { // 按区域，省或直辖市处理
				List<TEduDistrictDo> tedList = db1Service.getListByDs1IdName();
				if(tedList != null) {
					// 查找是否存在该区域和省市
					for (int i = 0; i < tedList.size(); i++) {
						TEduDistrictDo curTdd = tedList.get(i);
						if (curTdd.getId().compareTo(distName) == 0) {
							bfind = true;
							distIdorSCName = curTdd.getId();
							break;
						}
					}
				}
				// 存在则获取数据
				if (bfind) {
					if(distIdorSCName == null)
						distIdorSCName = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  //获取用户权限区域ID
					// 证照预警人员证件列表函数
					wstlDto = warnStaffLics(distIdorSCName, dates, tedList, db1Service);
				}
			} else if (distName == null && prefCity == null && province != null) { // 按省或直辖市处理
				List<TEduDistrictDo> tedList = null;
				if (province.compareTo("上海市") == 0) {
					tedList = db1Service.getListByDs1IdName();
					if(tedList != null)
						bfind = true;
					distIdorSCName = null;
				}
				if (bfind) {
					if(distIdorSCName == null)
						distIdorSCName = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  //获取用户权限区域ID
					// 证照预警人员证件列表函数
					wstlDto = warnStaffLics(distIdorSCName, dates, tedList, db1Service);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}													
		}
		else {    //模拟数据
			//模拟数据函数
			wstlDto = SimuDataFunc();
		}		

		return wstlDto;
	}
}
