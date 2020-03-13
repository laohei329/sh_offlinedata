package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.MatSumInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.MatSumInfoDTO;

/**
 * 3.2.25.	用料汇总信息应用模型
 * @author fengyang_xie
 *
 */
public class MatSumInfoAppMod {
	private static final Logger logger = LogManager.getLogger(MatSumInfoAppMod.class.getName());
	
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
			"   \"matSumInfo\": \r\n" + 
			"{\r\n" + 
			"  \"dishSchNum\":500,\r\n" + 
			"\"conMatSchNum\":450,\r\n" + 
			"\"noConMatSchNum\":50,\r\n" + 
			"\"totalMatPlanNum\":3567,\r\n" + 
			"\"conMatPlanNum\":3000,\r\n" + 
			"\"noConMatPlanNum\":567,\r\n" + 
			"\"matConRate\":84.11\r\n" + 
			"},\r\n" + 
			"   \"msgId\":1\r\n" + 
			"}\r\n" + 
			"";
	
	/**
	 * 汇总数据
	 * @return
	 */
	private MatSumInfoDTO matSumInfoFunc(String distId,String[] dates, List<TEduDistrictDo> tedList,Db1Service db1Service
			, SaasService saasService ) {
		
		MatSumInfoDTO matSumInfoDTO = new MatSumInfoDTO();
		
		MatSumInfo matSumInfo = new MatSumInfo();
		matSumInfoDTO.setMatSumInfo(matSumInfo);
		
		JSONObject jsStr = JSONObject.parseObject(tempData); 
		matSumInfoDTO = (MatSumInfoDTO) JSONObject.toJavaObject(jsStr,MatSumInfoDTO.class);
		
		
		
		
		//时戳
		matSumInfoDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
		//消息ID
		matSumInfoDTO.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return matSumInfoDTO;
	}
	
	/**
	 * 汇总数据
	 * @return
	 */
	private MatSumInfoDTO matSumInfoFuncOne(String distId,String[] dates, List<TEduDistrictDo> tedList,Db1Service db1Service
			, SaasService saasService ) {
		
		MatSumInfoDTO matSumInfoDTO = new MatSumInfoDTO();
		
		MatSumInfo matSumInfo = new MatSumInfo();
		
		//已排菜学校
		Integer  distDishSchNum =getDishInfo(distId, dates, tedList,redisService);
		matSumInfo.setDishSchNum(distDishSchNum);
		
		//conMatSchNum	必选	INT	已确认用料学校
		//noConMatSchNum	必选	INT	未确认用料学校
		//totalMatPlanNum	必选	INT	用料计划总数
		//conMatPlanNum	必选	INT	已确认用料计划数
		//noConMatPlanNum	必选	INT	未确认用料计划数
		//matConRate	必选	FLOAT	确认率，保留小数点有效数字两位
		getAccDistrInfo(distId, dates, tedList, matSumInfo,redisService);

		
		matSumInfoDTO.setMatSumInfo(matSumInfo);
		
		//时戳
		matSumInfoDTO.setTime(BCDTimeUtil.convertNormalFrom(null));
		//消息ID
		matSumInfoDTO.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return matSumInfoDTO;
	}
	
	/**
	 * 获取配送信息：未验收配送单个数、验收率
	 * @param distId
	 * @param dates
	 * @param tedList
	 * @param acceptInfo
	 */
	public static void getAccDistrInfo(String distId, String[] dates, List<TEduDistrictDo> tedList,
			MatSumInfo matSumInfo,RedisService redisService) {
		String key = "";
		String keyVal = "";
		String field = "";
		// 当天排菜学校总数
		Map<String, String> schIdToPlatoonMap = new HashMap<>();
		int dateCount = dates.length;
		//用料计划总数
		int[]totalMatPlanNums = new int[dateCount];
		//已确认用料计划数
		int []conMatPlanNums = new int[dateCount];
		
		float matConRate = 0;
		//未确认用料计划数
		int []noConMatPlanNums = new int[dateCount];
		
		//未确认用料计划学校数
		int []conMatSchNums = new int[dateCount];
		//未确认用料计划学校数
		int []noConMatSchNums = new int[dateCount];
		// 当天各区配货计划总数量
		for(int k = 0; k < dates.length; k++) {
			totalMatPlanNums[k] = 0;
			noConMatPlanNums[k] = 0;
			conMatPlanNums[k] = 0;
			key = dates[k] + "_useMaterialPlanTotal";
			schIdToPlatoonMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if (schIdToPlatoonMap != null) {
				for (int i = 0; i < tedList.size(); i++) {
					TEduDistrictDo curTdd = tedList.get(i);
					String curDistId = curTdd.getId();
					//判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
					if(distId != null) {
						if(!curDistId.equalsIgnoreCase(distId)) {
							continue ;
						}
					}
					// 区域用料计划总数
					field = "area" + "_" + curDistId;
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						totalMatPlanNums[k] += Integer.parseInt(keyVal);
						if(totalMatPlanNums[k] < 0) {
							totalMatPlanNums[k] = 0;
						}
					}
					// 已确认用料计划数
					field = "area" + "_" + curDistId + "_" + "status" + "_2";
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						int conMatPlanNum = Integer.parseInt(keyVal);
						if(conMatPlanNum < 0) {
							conMatPlanNum = 0;
						}
						conMatPlanNums[k] += conMatPlanNum;
					}
					
					// 未确认用料计划数
					field = "area" + "_" + curDistId + "_" + "status" + "_1";
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						noConMatPlanNums[k] += Integer.parseInt(keyVal);
						if(noConMatPlanNums[k] < 0) {
							noConMatPlanNums[k] = 0;
						}
					}
					
					// 未确认用料计划数
					field = "area" + "_" + curDistId + "_" + "status" + "_0";
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						noConMatPlanNums[k] += Integer.parseInt(keyVal);
						if(noConMatPlanNums[k] < 0) {
							noConMatPlanNums[k] = 0;
						}
					}
					
					// 已确认用料计划学校数
					field = "school-area" + "_" + curDistId + "_" + "status" + "_2";
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						int conMatSchNum = Integer.parseInt(keyVal);
						if(conMatSchNum < 0) {
							conMatSchNum = 0;
						}
						conMatSchNums[k] += conMatSchNum;
					}
					
					// 未确认用料计划学校数
					field = "school-area" + "_" + curDistId + "_" + "status" + "_1";
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						noConMatSchNums[k] += Integer.parseInt(keyVal);
						if(noConMatSchNums[k] < 0) {
							noConMatSchNums[k] = 0;
						}
					}
					
					// 未确认用料计划学校数
					field = "school-area" + "_" + curDistId + "_" + "status" + "_0";
					keyVal = schIdToPlatoonMap.get(field);
					if(keyVal != null) {
						noConMatSchNums[k] += Integer.parseInt(keyVal);
						if(noConMatSchNums[k] < 0) {
							noConMatSchNums[k] = 0;
						}
					}
				}
			}
		}
		
		int totalMatPlanNum = 0;
		int conMatPlanNum = 0;
		int noConMatPlanNum = 0;
		int conMatSchNum = 0;
		int noConMatSchNum = 0;
		for(int k = 0; k < dates.length; k++) {
			totalMatPlanNum += totalMatPlanNums[k];
			conMatPlanNum += conMatPlanNums[k];
			noConMatPlanNum +=noConMatPlanNums[k];
			
			conMatSchNum += conMatSchNums[k];
			noConMatSchNum +=noConMatSchNums[k];
		}
		//验收数量及验收率
		matConRate = 0;
		if(totalMatPlanNum > 0) {
			matConRate = 100 * ((float) conMatPlanNum / (float) totalMatPlanNum);
			BigDecimal bd = new BigDecimal(matConRate);
			matConRate = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			if (matConRate > 100) {
				matConRate = 100;
			}
		}
		
		/**
		 * 已确认用料学校
		 */
		matSumInfo.setConMatSchNum(conMatSchNum);
		
		/**
		 * 未确认用料学校
		 */
		matSumInfo.setNoConMatSchNum(noConMatSchNum);
		
		/**
		 * 用料计划总数
		 */
		matSumInfo.setTotalMatPlanNum(totalMatPlanNum);
		
		/**
		 * 已确认用料计划数
		 */
		matSumInfo.setConMatPlanNum(conMatPlanNum);
		
		/**
		 * 未确认用料计划数
		 */
		matSumInfo.setNoConMatPlanNum(noConMatPlanNum);
		
		/**
		 * 确认率，保留小数点有效数字两位
		 */
		matSumInfo.setMatConRate(matConRate);
		
		/**
		 * 应确认用料学校（已确认用料学校+未确认用料学校）
		 */
		matSumInfo.setShouldAccSchNum(matSumInfo.getConMatSchNum() + matSumInfo.getNoConMatSchNum());
		
	}
	
	/**
	 * 获取排菜数据
	 * @param distId
	 * @param dates
	 * @param tedList
	 * @return
	 */
	public static Integer getDishInfo(String distId, String[] dates, List<TEduDistrictDo> tedList,RedisService redisService) {
		String key = "";
		String keyVal = "";
		String fieldPrefix = "";
		// 当天排菜学校总数
		Map<String, String> platoonFeedTotalMap = null;
		int dateCount = dates.length;
		int[]distDishSchNums = new int[dateCount];
		
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
							}
						}
						distDishSchNums[k] += dishSchNum;
					}
				}
			}
		}
		
		// 判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
		int distDishSchNum = 0;
		for (int k = 0; k < dates.length; k++) {
			distDishSchNum += distDishSchNums[k];
		}
		
		return distDishSchNum;
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
	public MatSumInfoDTO appModFunc(String token, String distName, String prefCity, String province,String startDate, String endDate, 
			Db1Service db1Service, Db2Service db2Service, SaasService saasService ) {
		
		MatSumInfoDTO matSumInfoDTO = null;
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
						// 餐厨垃圾学校回收列表函数
						matSumInfoDTO = matSumInfoFunc(distId, dates, tedList, db1Service, saasService);
					}else if (methodIndex==1) {
						// 餐厨垃圾学校回收列表函数
						matSumInfoDTO = matSumInfoFuncOne(distId, dates, tedList, db1Service, saasService);
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
						// 餐厨垃圾学校回收列表函数
						matSumInfoDTO = matSumInfoFunc(distId, dates, tedList, db1Service, saasService);
					}else if (methodIndex==1) {
						// 餐厨垃圾学校回收列表函数
						matSumInfoDTO = matSumInfoFuncOne(distId, dates, tedList, db1Service, saasService);
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
			matSumInfoDTO = new MatSumInfoDTO();
		}		

		
		return matSumInfoDTO;
	}
}
