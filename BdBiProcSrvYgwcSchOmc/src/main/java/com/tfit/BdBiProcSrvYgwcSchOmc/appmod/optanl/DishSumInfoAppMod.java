package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.DishSumInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.DishSumInfoDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

/**
 * 3.2.9.	排菜汇总信息应用模型
 * @author fengyang_xie
 *
 */
public class DishSumInfoAppMod {
	private static final Logger logger = LogManager.getLogger(DishSumInfoAppMod.class.getName());
	
	/**
	 * Redis服务
	 */
	@Autowired
	RedisService redisService = new RedisService();
	
	/**
	 * 方法类型索引
	 */
	int methodIndex = 1;
	/**
	 * 是否为真实数据标识
	 */
	private static boolean isRealData = true;
	
	/**
	 * 数组数据初始化
	 */
	String tempData ="{\r\n" + 
			"   \"time\": \"2016-07-14 09:51:35\",\r\n" + 
			"   \"dishSumInfo\": \r\n" + 
			"{\r\n" + 
			"  \"totalSchNum\":3567,\r\n" + 
			"\"mealSchNum\":567,\r\n" + 
			"\"dishSchNum\":500,\r\n" + 
			"\"noDishSchNum\":67,\r\n" + 
			"\"dishRate\":84.11\r\n" + 
			" },\r\n" + 
			"   \"msgId\":1\r\n" + 
			"}\r\n" + 
			"";
	
	/**
	 * 汇总数据
	 * @return
	 */
	private DishSumInfoDTO dishSumInfoFunc(String distId,String[] dates, List<TEduDistrictDo> tedList,Db1Service db1Service
			, SaasService saasService ) {
		
		DishSumInfoDTO dishSumInfoDTO = new DishSumInfoDTO();
		
		DishSumInfo dishSumInfo = new DishSumInfo();
		dishSumInfoDTO.setDishSumInfo(dishSumInfo);
		
		JSONObject jsStr = JSONObject.parseObject(tempData); 
		dishSumInfoDTO = (DishSumInfoDTO) JSONObject.toJavaObject(jsStr,DishSumInfoDTO.class);
		
		
		
		
		//时戳
		dishSumInfoDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
		//消息ID
		dishSumInfoDTO.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return dishSumInfoDTO;
	}
	
	/**
	 * 汇总数据
	 * @return
	 */
	private DishSumInfoDTO dishSumInfoFuncOne(String distId,String[] dates, List<TEduDistrictDo> tedList,Db1Service db1Service
			, SaasService saasService ) {
		
		DishSumInfoDTO dishSumInfoDTO = new DishSumInfoDTO();
		DishSumInfo dishSumInfo = new DishSumInfo();
		
		/**
		 * 监管学校数量 totalSchNum	必选	INT	学校数量
		 */
		Integer supSchNum = getSupSchNum(distId);
		dishSumInfo.setTotalSchNum(supSchNum);
		/**
		 * 排菜汇总
		 * mealSchNum	必选	INT	供餐学校数量
		 * dishSchNum	必选	INT	已排菜学校数量
		 * noDishSchNum	必选	INT	未排菜学校数量
		 * dishRate	必选	FLOAT	排菜率，保留小数点有效数字两位
		 */
		getDishInfo(dishSumInfo,distId, dates, tedList);
		
		dishSumInfoDTO.setDishSumInfo(dishSumInfo);
		//时戳
		dishSumInfoDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
		//消息ID
		dishSumInfoDTO.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return dishSumInfoDTO;
	}
	
	/**
	 * 获取监管学校总数
	 * @param distId
	 * @return
	 */
	private Integer getSupSchNum(String distId) {
		Integer supSchNum = 0;
		String key = "schoolData";
		String keyVal = null;
		if(StringUtils.isEmpty(distId)) {
			//如果区域是空，数据则从从schoolData：shanghai中获取，否则从指定区域获取
			keyVal = redisService.getHashByKeyField(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key, "shanghai");
		}else {
			//如果区域不为空，则去除区域学校总数
			keyVal = redisService.getHashByKeyField(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key, "area_"+distId);
		}
		if(keyVal != null) {
			supSchNum = Integer.parseInt(keyVal);
		}
		return supSchNum;
	}
	
	/**
	 * 获取排菜数据
	 * @param distId
	 * @param dates
	 * @param tedList
	 * @return
	 */
	private void getDishInfo(DishSumInfo dishSumInfo,String distId, String[] dates, List<TEduDistrictDo> tedList) {
		String key = "";
		String keyVal = "";
		String fieldPrefix = "";
		// 当天排菜学校总数
		Map<String, String> platoonFeedTotalMap = null;
		int dateCount = dates.length;
		int[]totalMealSchNums = new int[dateCount];
		int[]distDishSchNums = new int[dateCount];
		int[]distNoDishSchNums = new int[dateCount];
		int[]distNoServeFoodSchNums = new int[dateCount];
		
		float distDishRate = 0;
		// 当天各区排菜学校数量
		for (int k = 0; k < dates.length; k++) {
			//供餐学校数量
			key = dates[k] + "_platoonfeed-total";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			//Redis没有该数据则从hdfs系统中获取
			if(platoonFeedTotalMap == null) {   
			}
			if(platoonFeedTotalMap != null) {
				for(String curKey : platoonFeedTotalMap.keySet()) {
					for (int i = 0; i < tedList.size(); i++) {
						TEduDistrictDo curTdd = tedList.get(i);
						String curDistId = curTdd.getId();
						//判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
						if(distId != null && curDistId.compareTo(distId) != 0) {
							continue ;
						}
						// 区域排菜学校供餐数
						fieldPrefix = curDistId + "_";
						int mealSchNum = 0;
						int dishSchNum = 0;
						int noDishSchNum = 0;
						int distNoServeFoodSchNum = 0;
						if (curKey.indexOf(fieldPrefix) == 0) {
							String[] curKeys = curKey.split("_");
							if(curKeys.length >= 3)
							{
								keyVal = platoonFeedTotalMap.get(curKey);
								if(keyVal == null || "".equals(keyVal) || !StringUtils.isNumeric(keyVal)) {
									continue;
								}
								if(curKeys[1].equalsIgnoreCase("供餐") && curKeys[2].equalsIgnoreCase("已排菜")) {
									if(keyVal != null) {
										mealSchNum = Integer.parseInt(keyVal);
										dishSchNum = mealSchNum;
									}
								}
								else if(curKeys[1].equalsIgnoreCase("供餐") && curKeys[2].equalsIgnoreCase("未排菜")) {
									if(keyVal != null) {
										mealSchNum = Integer.parseInt(keyVal);
										noDishSchNum = mealSchNum;
									}
								}else if (curKeys[1].equalsIgnoreCase("不供餐")) {
									distNoServeFoodSchNum =Integer.parseInt(keyVal);
								}
							}
						}
						totalMealSchNums[k] += mealSchNum;
						distDishSchNums[k] += dishSchNum;
						distNoDishSchNums[k] += noDishSchNum;
						distNoServeFoodSchNums[k] +=distNoServeFoodSchNum;
						
					}
				}
			}
		}
		
		// 判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
		int totalDistSchNum = 0;
		int distDishSchNum = 0;
		int distNoDishSchNum = 0;
		int distNoServeFoodSchNum = 0;
		for (int k = 0; k < dates.length; k++) {
			totalDistSchNum += totalMealSchNums[k];
			distDishSchNum += distDishSchNums[k];
			distNoDishSchNum += distNoDishSchNums[k];
			distNoServeFoodSchNum+=distNoServeFoodSchNums[k];
		}
		distDishRate = 0;
		if(totalDistSchNum > 0) {
			distDishRate = 100 * ((float) distDishSchNum / (float) totalDistSchNum);
			BigDecimal bd = new BigDecimal(distDishRate);
			distDishRate = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			if (distDishRate > 100) {
				distDishRate = 100;
			}
		}
			
		/**
		 * 供餐学校数量
		 */
		dishSumInfo.setMealSchNum(totalDistSchNum);
		
		/**
		 * 已排菜学校数量
		 */
		dishSumInfo.setDishSchNum(distDishSchNum);
		
		/**
		 * 未排菜学校数量
		 */
		dishSumInfo.setNoDishSchNum(distNoDishSchNum);
		
		/**
		 * 排菜率，保留小数点有效数字两位。
		 */
		dishSumInfo.setDishRate(distDishRate);
	}
	
	/**
	 * 投诉举报详情模型函数
	 * @param crId
	 * @param distName
	 * @param prefCity
	 * @param province
	 * @param startDate
	 * @param endDate
	 * @param db1Service
	 * @return
	 */
	public DishSumInfoDTO appModFunc(String token, String distName, String prefCity, String province,String startDate, String endDate, 
			Db1Service db1Service, Db2Service db2Service, SaasService saasService ) {
		
		DishSumInfoDTO dishSumInfoDTO = null;
		//真实数据
		if(isRealData) {       
			// 日期
			String[] dates = null;
			// 按照当天日期获取数据
			if (startDate == null || endDate == null) { 
				dates = new String[1];
				dates[0] = BCDTimeUtil.convertNormalDate(null);
			} else { // 按照开始日期和结束日期获取数据
				DateTime startDt = BCDTimeUtil.convertDateStrToDate(startDate);
				DateTime endDt = BCDTimeUtil.convertDateStrToDate(endDate);
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
			if(province == null) {
				province = "上海市";
			}
			// 参数查找标识
			boolean bfind = false;
			String distId = null;
			// 按不同参数形式处理
			if (distName != null && prefCity == null && province != null) { 
				// 按区域，省或直辖市处理
				List<TEduDistrictDo> tedList = db1Service.getListByDs1IdName();
				if(tedList != null) {
					// 查找是否存在该区域和省市
					for (int i = 0; i < tedList.size(); i++) {
						TEduDistrictDo curTdd = tedList.get(i);
						if (curTdd.getId().compareTo(distName) == 0) {
							bfind = true;
							distId = curTdd.getId();
							break;
						}
					}
				}
				// 存在则获取数据
				if (bfind) {
					if(distId == null) {
						//获取用户权限区域ID
						distId = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  
					}
					// 餐厨垃圾学校回收列表函数
					if(methodIndex==0) {
						dishSumInfoDTO = dishSumInfoFunc(distId, dates, tedList, db1Service, saasService);
					}else if (methodIndex==1) {
						dishSumInfoDTO = dishSumInfoFuncOne(distId, dates, tedList, db1Service, saasService);
					}
				}
			} else if (distName == null && prefCity == null && province != null) { 
				// 按省或直辖市处理
				List<TEduDistrictDo> tedList = null;
				if (province.compareTo("上海市") == 0) {
					tedList = db1Service.getListByDs1IdName();
					if(tedList != null) {
						bfind = true;
					}
					distId = null;
				}
				if (bfind) {
					if(distId == null) {
						//获取用户权限区域ID
						distId = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  
					}
					// 餐厨垃圾学校回收列表函数
					if(methodIndex==0) {
						dishSumInfoDTO = dishSumInfoFunc(distId, dates, tedList, db1Service, saasService);
					}else if (methodIndex==1) {
						dishSumInfoDTO = dishSumInfoFuncOne(distId, dates, tedList, db1Service, saasService);
					}
				}
			} else if (distName != null && prefCity != null && province != null) { 
				// 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { 
				// 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}			
		}
		else {    //模拟数据
			//模拟数据函数
			dishSumInfoDTO = new DishSumInfoDTO();
		}		

		
		return dishSumInfoDTO;
	}
}
