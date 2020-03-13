package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.im;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.KwSchRecs;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.KwSchRecsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.CommonUtil;

//餐厨垃圾学校回收列表应用模型
public class KwSchRecsAppMod {
	private static final Logger logger = LogManager.getLogger(KwSchRecsAppMod.class.getName());
	
	//Redis服务
	@Autowired
	RedisService redisService = new RedisService();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化	
	String[] recDate_Array = {"2018/09/03-2018/09/04", "2018/09/03-2018/09/04", "2018/09/03-2018/09/04"};
	String[] distName_Array = {"11", "1", "16"};
	int[] schNum_Array = { 268, 151, 243};
	float[] rcNum_Array = {206, 130, 215};
	
	//模拟数据函数
	private KwSchRecsDTO SimuDataFunc() {
		KwSchRecsDTO ksrDto = new KwSchRecsDTO();
		//时戳
		ksrDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//餐厨垃圾学校回收列表模拟数据
		List<KwSchRecs> kwSchRecs = new ArrayList<>();
		//赋值
		for (int i = 0; i < recDate_Array.length; i++) {
			KwSchRecs ksr = new KwSchRecs();
			ksr.setRecDate(recDate_Array[i]);
			ksr.setDistName(distName_Array[i]);
			ksr.setSchNum(schNum_Array[i]);
			ksr.setRcNum(rcNum_Array[i]);
			kwSchRecs.add(ksr);
		}
		//设置数据
		ksrDto.setKwSchRecs(kwSchRecs);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = recDate_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		ksrDto.setPageInfo(pageInfo);
		//消息ID
		ksrDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return ksrDto;
	}
	
	// 餐厨垃圾学校回收列表函数
	private KwSchRecsDTO kwSchRecs(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList,
			String distNames) {
		KwSchRecsDTO ksrDto = new KwSchRecsDTO();
		List<KwSchRecs> kwSchRecs = new ArrayList<>();
		KwSchRecs ksr = null;
		String key = "", keyVal = "", field = "", fieldPrefix = "";
		// 时间段内各区餐厨垃圾学校回收总数
		Map<String, String> schoolwastetotalMap = null;
		int distCount = tedList.size(), dateCount = dates.length;
		int[][] totalRcFreqs = new int[dateCount][distCount];
		float[][] totalRcNums = new float[dateCount][distCount];
		List<Object> distNamesList=CommonUtil.changeStringToList(distNames);
		// 时间段内各区餐厨垃圾学校回收数量
		for (int k = 0; k < dates.length; k++) {
			// 回收桶数
			key = dates[k] + "_schoolwastetotal";
			schoolwastetotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(schoolwastetotalMap == null) {    //Redis没有该数据则从hdfs系统中获取
				
			}
			if(schoolwastetotalMap != null) {
				for(String curKey : schoolwastetotalMap.keySet()) {
					for (int i = 0; i < tedList.size(); i++) {
						TEduDistrictDo curTdd = tedList.get(i);
						String curDistId = curTdd.getId();
						//判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
						if(distIdorSCName != null) {
							if(!curDistId.equals(distIdorSCName))
								continue ;
						}else if(distNamesList!=null && distNamesList.size() >0) {
							if(StringUtils.isEmpty(curDistId) || !StringUtils.isNumeric(curDistId)) {
								continue;
							}
							if(!distNamesList.contains(curDistId)) {
								continue ;
							}
						}
						//区域回收次数
						fieldPrefix = curDistId + "_total";
						if (curKey.equalsIgnoreCase(fieldPrefix)) {
							keyVal = schoolwastetotalMap.get(curKey);
							if(keyVal != null) {
								totalRcFreqs[k][i] = Integer.parseInt(keyVal);
							}
						}
						// 区域回收垃圾桶数
						fieldPrefix = curDistId;
						if (curKey.equalsIgnoreCase(fieldPrefix)) {
							keyVal = schoolwastetotalMap.get(curKey);
							if(keyVal != null) {
								totalRcNums[k][i] = Float.parseFloat(keyVal);
							}
						}
					}
				}
			}
			// 该日期各区餐厨垃圾回收列表
			for (int i = 0; i < distCount; i++) {
				TEduDistrictDo curTdd = tedList.get(i);
				String curDistId = curTdd.getId();
				field = "area" + "_" + curDistId;
				// 判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
				if (distIdorSCName != null) {
					if (!curDistId.equals(distIdorSCName))
						continue;
				}else if(distNamesList!=null && distNamesList.size() >0) {
					if(StringUtils.isEmpty(curDistId) || !StringUtils.isNumeric(curDistId)) {
						continue;
					}
					if(!distNamesList.contains(curDistId)) {
						continue ;
					}
				}
				BigDecimal bd = new BigDecimal(totalRcNums[k][i]);
				totalRcNums[k][i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，回收次数：" + totalRcFreqs[k][i]
						+ "，回收数量：" + totalRcNums[k][i] + " 桶" + "，field = " + field);
			}
		}
		String startDate = dates[dates.length - 1].replaceAll("-", "/"), endDate = dates[0].replaceAll("-", "/");
		for (int i = 0; i < distCount; i++) {
			TEduDistrictDo curTdd = tedList.get(i);
			String curDistId = curTdd.getId();
			// 判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
			if (distIdorSCName != null) {
				if (!curDistId.equals(distIdorSCName))
					continue;
			}else if(distNamesList!=null && distNamesList.size() >0) {
				if(StringUtils.isEmpty(curDistId) || !StringUtils.isNumeric(curDistId)) {
					continue;
				}
				if(!distNamesList.contains(curDistId)) {
					continue ;
				}
			}
			ksr = new KwSchRecs();
			ksr.setRecDate(startDate + "-" + endDate);
			ksr.setDistName(curTdd.getId());
			int totalRcFreq = 0;
			float totalRcNum = 0;
			for (int k = 0; k < dates.length; k++) {
				totalRcFreq += totalRcFreqs[k][i];
				totalRcNum += totalRcNums[k][i];
			}
			ksr.setSchNum(totalRcFreq);
			ksr.setRcNum(totalRcNum);
			kwSchRecs.add(ksr);
		}
		// 设置返回数据
		ksrDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<KwSchRecs> pageBean = new PageBean<KwSchRecs>(kwSchRecs, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		ksrDto.setPageInfo(pageInfo);
		// 设置数据
		ksrDto.setKwSchRecs(pageBean.getCurPageData());
		// 消息ID
		ksrDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();

		return ksrDto;
	}	
	
	// 餐厨垃圾学校回收列表模型函数
	public KwSchRecsDTO appModFunc(String token, String recStartDate, String recEndDate, 
			String distName, String prefCity, String province, String distNames,
			String page, String pageSize, Db1Service db1Service, Db2Service db2Service) {
		KwSchRecsDTO ksrDto = null;
		this.curPageNum = Integer.parseInt(page);
		this.pageSize = Integer.parseInt(pageSize);
		if(isRealData) {       //真实数据
			// 日期
			String[] dates = null;
			if (recStartDate == null || recEndDate == null) { // 按照当天日期获取数据
				dates = new String[1];
				dates[0] = BCDTimeUtil.convertNormalDate(null);
			} else { // 按照开始日期和结束日期获取数据
				DateTime startDt = BCDTimeUtil.convertDateStrToDate(recStartDate);
				DateTime endDt = BCDTimeUtil.convertDateStrToDate(recEndDate);
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
					// 餐厨垃圾学校回收列表函数
					ksrDto = kwSchRecs(distIdorSCName, dates, tedList,distNames);
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
					// 餐厨垃圾学校回收列表函数
					ksrDto = kwSchRecs(distIdorSCName, dates, tedList,distNames);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}			
		}
		else {    //模拟数据
			//模拟数据函数
			ksrDto = SimuDataFunc();
		}		

		return ksrDto;
	}
}
