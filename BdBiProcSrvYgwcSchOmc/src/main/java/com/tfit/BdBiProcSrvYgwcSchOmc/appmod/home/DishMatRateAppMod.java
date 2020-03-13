package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.home;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.home.DishMatRate;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.home.DishMatRateDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//排菜率与用料确认率趋势应用模型
public class DishMatRateAppMod {
	private static final Logger logger = LogManager.getLogger(DishMatRateAppMod.class.getName());
	
	//Redis服务
	@Autowired
	RedisService redisService = new RedisService();
	
	//时间坐标个数
	final int timeCoordNum = 7;
	
	//最小供餐学校数量域值
	final int minMealSchNumThre = 20;
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//数组数据初始化
	String[] timeCoord_Array = {"9/21", "9/22", "9/23", "9/24", "9/25", "9/26", "9/27"};
	float[] dishRate_Array = {(float) 0.51, (float) 0.51, (float) 0.55, (float) 85.96, (float) 87.08, (float) 87.08, (float) 85.05};
	float[] matConRate_Array = {(float) 66.67, (float) 75.00, (float) 71.00, (float) 98.67, (float) 100.00, (float) 100.00, (float) 91.26};
	
	//模拟数据函数
	private DishMatRateDTO SimuDataFunc() {
		DishMatRateDTO dmrDto = new DishMatRateDTO();
		//时戳
		dmrDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//当前排菜率
		float curDishRate = (float) 81.09, curMatConRate = (float) 71.22;
		BigDecimal bd = new BigDecimal(curDishRate);
		curDishRate = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		dmrDto.setCurDishRate(curDishRate);
		//当前用料确认率
		bd = new BigDecimal(curMatConRate);
		curMatConRate = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
		dmrDto.setCurMatConRate(curMatConRate);
		//排菜率与用料确认率趋势模拟数据
		List<DishMatRate> dishMatRate = new ArrayList<>();
		DateTime dt = new DateTime();
		//赋值
		for (int i = 0; i < timeCoord_Array.length; i++) {
			DishMatRate dmr = new DishMatRate();
			DateTime curDt = dt.minusDays(timeCoord_Array.length-i-1);
			timeCoord_Array[i] = BCDTimeUtil.convertMonthDayForm(curDt);
			dmr.setTimeCoord(timeCoord_Array[i]);
			bd = new BigDecimal(dishRate_Array[i]);
			dishRate_Array[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			dmr.setDishRate(dishRate_Array[i]);
			bd = new BigDecimal(matConRate_Array[i]);
			matConRate_Array[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			dmr.setMatConRate(matConRate_Array[i]);
			dishMatRate.add(dmr);
		}
		//设置数据
		dmrDto.setDishMatRate(dishMatRate);
		//消息ID
		dmrDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return dmrDto;
	}
	
	// 排菜率与用料确认率趋势函数
	private DishMatRateDTO dishMatRate(String distIdorSCName, String[] dates, List<TEduDistrictDo> tddList) {
		DishMatRateDTO dmrDto = new DishMatRateDTO();
		List<DishMatRate> dishMatRate = new ArrayList<>();
		DishMatRate dmr = null;
		String key = "", keyVal = "", field = "", fieldPrefix = "";
		// 当天排菜学校总数
		Map<String, String> platoonFeedTotalMap = null, useMaterialPlanMap = null;
		int distCount = tddList.size(), dateCount = dates.length;
		int[][] totalMealSchNums = new int[dateCount][distCount], distDishSchNums = new int[dateCount][distCount];
		int[][] totalMatPlanNums = new int[dateCount][distCount], matPlanConfirmNums = new int[dateCount][distCount];
		float[] dateDishRates = new float[dateCount], dateMatRates = new float[dateCount];
		// 时间段内各区排菜学校数量
		for (int k = 0; k < dates.length; k++) {
			//供餐学校数量
			key = dates[k] + "_platoonfeed-total";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(platoonFeedTotalMap == null)
				continue;
			for(String curKey : platoonFeedTotalMap.keySet()) {
				for (int i = 0; i < tddList.size(); i++) {
					TEduDistrictDo curTdd = tddList.get(i);
					String curDistId = curTdd.getId();
					//判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
					if(distIdorSCName != null) {
						if(!curDistId.equals(distIdorSCName))
							continue ;
					}
					// 区域排菜学校供餐数
					fieldPrefix = curDistId + "_";
					int mealSchNum = 0, dishSchNum = 0;
					if (curKey.indexOf(fieldPrefix) == 0) {
						String[] curKeys = curKey.split("_");
						if(curKeys.length >= 3)
						{
							if(curKeys[1].equalsIgnoreCase("供餐") && curKeys[2].equalsIgnoreCase("已排菜")) {
								keyVal = platoonFeedTotalMap.get(curKey);
								if(keyVal != null) {
									mealSchNum = Integer.parseInt(keyVal);
									dishSchNum = mealSchNum;
								}
							}
							else if(curKeys[1].equalsIgnoreCase("供餐") && curKeys[2].equalsIgnoreCase("未排菜")) {
								keyVal = platoonFeedTotalMap.get(curKey);
								if(keyVal != null) {
									mealSchNum = Integer.parseInt(keyVal);
								}
							}
						}
					}
					totalMealSchNums[k][i] += mealSchNum;
					distDishSchNums[k][i] += dishSchNum;
				}
			}
		}
		//时间段内用料计划数量
		for (int k = 0; k < dates.length; k++) {
			//用料计划数量
			key = dates[k] + "_useMaterialPlanTotal";
			useMaterialPlanMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if (useMaterialPlanMap != null) {
				for(int i = 0; i < distCount; i++) {
					TEduDistrictDo curTdd = tddList.get(i);
					String curDistId = curTdd.getId();
					//判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
					if(distIdorSCName != null) {
						if(!curDistId.equalsIgnoreCase(distIdorSCName))
							continue ;
					}
					fieldPrefix = "area" + "_" + curDistId;
					// 用料计划总数
					int totalMatPlanNum = 0;
					for(int j = 0; j < 3; j++) {
						field = fieldPrefix + "_" + "status" + "_" + j;
						keyVal = useMaterialPlanMap.get(field);
						if (keyVal != null)
							totalMatPlanNum += Integer.parseInt(keyVal);
					}
					totalMatPlanNums[k][i] = totalMatPlanNum;
					// 用料计划确认数
					field = fieldPrefix + "_" + "status" + "_" + 2;
					keyVal = useMaterialPlanMap.get(field);
					if (keyVal != null)
						matPlanConfirmNums[k][i] = Integer.parseInt(keyVal);
				}
			}
		}
		//计算日期排菜率和用料计划确认率
		for (int k = 0; k < dates.length; k++) {
			int totalMealSchNum = 0, dateDishSchNum = 0, totalMatPlanNum = 0, matPlanConfirmNum = 0;
			dateDishRates[k] = 0;
			dateMatRates[k] = 0;
			for(int i = 0; i < distCount; i++) {
				totalMealSchNum += totalMealSchNums[k][i];
				dateDishSchNum += distDishSchNums[k][i];
				totalMatPlanNum += totalMatPlanNums[k][i];
				matPlanConfirmNum += matPlanConfirmNums[k][i];
			}
			if(totalMealSchNum > 0 && totalMealSchNum > minMealSchNumThre) {    //排菜率
				dateDishRates[k] = 100 * ((float) dateDishSchNum / (float) totalMealSchNum);
				BigDecimal bd = new BigDecimal(dateDishRates[k]);
				dateDishRates[k] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				if (dateDishRates[k] > 100) {
					dateDishRates[k] = 100;
				}
			}
			if(totalMatPlanNum > 0) {    //用料计划确认率
				dateMatRates[k] = 100 * ((float) matPlanConfirmNum / (float) totalMatPlanNum);
				BigDecimal bd = new BigDecimal(dateMatRates[k]);
				dateMatRates[k] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				if (dateMatRates[k] > 100) {
					dateMatRates[k] = 100;
				}
			}
		}
		//设置排菜和用料确认数据
		for(int k = 0; k < dates.length; k++) {
			dmr = new DishMatRate();
			DateTime curDt = BCDTimeUtil.convertDateStrToDate(dates[k]);
			String timeCoord = curDt.toString("M/d");
			dmr.setDishRate(dateDishRates[k]);
			dmr.setMatConRate(dateMatRates[k]);
			dmr.setTimeCoord(timeCoord);
			dishMatRate.add(dmr);
			//今日排菜率和确认率
			if(k == dateCount-1) {
				dmrDto.setCurDishRate(dateDishRates[k]);
				dmrDto.setCurMatConRate(dateMatRates[k]);
			}
			logger.info("日期：" + dates[k] + "，排菜率：" + dateDishRates[k]);
		}
		// 设置返回数据
		dmrDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//设置数据
		dmrDto.setDishMatRate(dishMatRate);
		//消息ID
		dmrDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return dmrDto;
	}
	
	// 排菜率与用料确认率趋势模型函数
	public DishMatRateDTO appModFunc(String token, String distName, String prefCity, String province, int timeType, String date, Db1Service db1Service, Db2Service db2Service) {
		DishMatRateDTO dmrDto = null;
		if(isRealData) {       //真实数据
			//日期
			String[] dates = null;
			if(timeType == 3) {  //按天取日期
				DateTime dt = null;
				dates = new String[timeCoordNum];
				if(date == null)
					dt = new DateTime();
				else
					dt = BCDTimeUtil.convertDateStrToDate(date);
				for(int i = timeCoordNum-1, j = 0; i >= 0; i--, j++)
					dates[i] = dt.minusDays(j).toString("yyyy-MM-dd");
			}
			else if(timeType == 4) {  //按周取日期
				
			}
			for (int i = 0; i < dates.length; i++) {
				logger.info("dates[" + i + "] = " + dates[i]);
			}
			// 省或直辖市
			if (province == null)
				province = "上海市";
			// 参数查找标识
			boolean bfind = false;
			String distIdorSCName = null;
			// 按不同参数形式处理
			if (distName != null && prefCity == null && province != null) { // 按区域，省或直辖市处理
				List<TEduDistrictDo> tddList = db1Service.getListByDs1IdName();
				// 查找是否存在该区域和省市
				for (int i = 0; i < tddList.size(); i++) {
					TEduDistrictDo curTdd = tddList.get(i);
					if (curTdd.getId().compareTo(distName) == 0) {
						bfind = true;
						distIdorSCName = curTdd.getId();
						break;
					}
				}
				// 存在则获取数据
				if (bfind) {
					if(distIdorSCName == null)
						distIdorSCName = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  //获取用户权限区域ID
					// 排菜率与用料确认率趋势函数
					dmrDto = dishMatRate(distIdorSCName, dates, tddList);
				}
			} else if (distName == null && prefCity == null && province != null) { // 按省或直辖市处理
				List<TEduDistrictDo> tddList = null;
				if (province.compareTo("上海市") == 0) {
					bfind = true;
					tddList = db1Service.getListByDs1IdName();
					distIdorSCName = null;
				}
				if (bfind) {
					if(distIdorSCName == null)
						distIdorSCName = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  //获取用户权限区域ID
					// 排菜率与用料确认率趋势函数
					dmrDto = dishMatRate(distIdorSCName, dates, tddList);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}
		}
		else {    //模拟数据
			//模拟数据函数
			dmrDto = SimuDataFunc();
		}		

		return dmrDto;
	}
}
