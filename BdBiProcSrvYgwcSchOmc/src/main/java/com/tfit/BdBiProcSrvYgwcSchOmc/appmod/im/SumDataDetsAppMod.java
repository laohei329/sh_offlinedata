package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.im;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.SumDataDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.SumDataDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.UserDataPermInfoDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.CommonUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.SortList;

//汇总数据详情列表应用模型
public class SumDataDetsAppMod {
	private static final Logger logger = LogManager.getLogger(SumDataDetsAppMod.class.getName());
	
	//Redis服务
	@Autowired
	RedisService redisService = new RedisService();
	
	//二级排序条件
	final String[] methods0 = {"getSubLevel", "getCompDep"};
	final String[] sorts0 = {"desc", "desc"};
	final String[] dataTypes0 = {"String", "String"};
		
	final String[] methods1 = {"getDistName", "getDishDate"};
	final String[] sorts1 = {"asc", "asc"};
	final String[] dataTypes1 = {"String", "String"};
	
	//时间坐标个数
	final int timeCoordNum = 7;
	
	//最小供餐学校数量域值
	final int minMealSchNumThre = 20;
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
		
	//数组数据初始化
	String[] dishDate_Array = {"2018/09/03-2018/09/04", "2018/09/03-2018/09/04", "2018/09/03-2018/09/04"};
	String[] subLevel_Array = {"区属", "区属", "区属"};
	String[] compDep_Array = {"黄浦区教育局", "徐汇区教育局", "闵行区教育局"};
	String[] distName_Array = {"黄浦区", "徐汇区", "闵行区"};
	int[] mealSchNum_Array = {150, 150, 150};
	int[] dishSchNum_Array = {60, 60, 60};
	int[] noDishSchNum_Array = {90, 90, 90};
	float[] dishRate_Array = {(float) 60.00, (float) 60.00, (float) 60.00};
	int[] totalGsPlanNum_Array = {150, 150, 150};
	int[] noAcceptGsPlanNum_Array = {60, 60, 60};
	int[] acceptGsPlanNum_Array = {90, 90, 90};
	float[] acceptRate_Array = {(float) 60.00, (float) 60.00, (float) 60.00};
	int[] totalDishNum_Array = {150, 150, 150};
	int[] noRsDishNum_Array = {60, 60, 60};
	int[] rsDishNum_Array = {90, 90, 90};
	float[] rsRate_Array = {(float) 60.00, (float) 60.00, (float) 60.00};
	int[] totalWarnNum_Array = {150, 150, 150};
	int[] noProcWarnNum_Array = {60, 60, 60};
	int[] elimWarnNum_Array = {90, 90, 90};
	float[] warnProcRate_Array = {(float) 60.00, (float) 60.00, (float) 60.00};	
	int[] totalKwRecNum_Array = {150, 150, 150};
	int[] kwSchRecNum_Array = {60, 60, 60};
	int[] kwRmcRecNum_Array = {90, 90, 90};
	int[] totalWoRecNum_Array = {150, 150, 150};
	int[] woSchRecNum_Array = {60, 60, 60};
	int[] woRmcRecNum_Array = {90, 90, 90};
	
	//模拟数据函数
	private SumDataDetsDTO SimuDataFunc() {
		SumDataDetsDTO sddDto = new SumDataDetsDTO();
		//时戳
		sddDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//汇总数据详情列表模拟数据
		List<SumDataDets> sumDataDets = new ArrayList<>();
		//赋值
		for (int i = 0; i < dishDate_Array.length; i++) {
			SumDataDets sdd = new SumDataDets();
			sdd.setDishDate(dishDate_Array[i]);
			sdd.setSubLevel(subLevel_Array[i]);
			sdd.setCompDep(compDep_Array[i]);
			sdd.setDistName(distName_Array[i]);
			sdd.setMealSchNum(mealSchNum_Array[i]);
			sdd.setDishSchNum(dishSchNum_Array[i]);
			sdd.setNoDishSchNum(noDishSchNum_Array[i]);
			sdd.setDishRate(dishRate_Array[i]);
			sdd.setTotalGsPlanNum(totalGsPlanNum_Array[i]);
			sdd.setNoAcceptGsPlanNum(noAcceptGsPlanNum_Array[i]);
			sdd.setAcceptGsPlanNum(acceptGsPlanNum_Array[i]);
			sdd.setAcceptRate(acceptRate_Array[i]);
			sdd.setTotalDishNum(totalDishNum_Array[i]);
			sdd.setNoRsDishNum(noRsDishNum_Array[i]);
			sdd.setRsDishNum(rsDishNum_Array[i]);
			sdd.setRsRate(rsRate_Array[i]);
			sdd.setTotalWarnNum(totalWarnNum_Array[i]);
			sdd.setNoProcWarnNum(noProcWarnNum_Array[i]);
			sdd.setElimWarnNum(elimWarnNum_Array[i]);
			sdd.setWarnProcRate(warnProcRate_Array[i]);
			sdd.setTotalKwRecNum(totalKwRecNum_Array[i]);
			sdd.setKwSchRecNum(kwSchRecNum_Array[i]);
			sdd.setKwRmcRecNum(kwRmcRecNum_Array[i]);
			sdd.setTotalWoRecNum(totalWoRecNum_Array[i]);
			sdd.setWoSchRecNum(woSchRecNum_Array[i]);
			sdd.setWoRmcRecNum(woRmcRecNum_Array[i]);
			sumDataDets.add(sdd);
		}
		//设置数据
		sddDto.setSumDataDets(sumDataDets);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = dishDate_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		sddDto.setPageInfo(pageInfo);
		//消息ID
		sddDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return sddDto;
	}
	
	//汇总数据详情列表函数按主管部门
	private SumDataDetsDTO sumDataDetsByCompDep(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList, 
			int subLevel, int compDep, String subDistName,
			String subLevels,String compDeps) {
		SumDataDetsDTO sddDto = new SumDataDetsDTO();
		List<SumDataDets> sumDataDets = new ArrayList<>();
		SumDataDets sdd = null;
		String key = "", keyVal = "", field = "", fieldPrefix = "";
		int i, j, k, subLevelCount = 4, compDepCount = 0, maxCompDepCount = AppModConfig.compDepIdToNameMap3.size();
		//排菜数据
		Map<String, String> platoonFeedTotalMap = null;
		int distCount = tedList.size(), dateCount = dates.length;
		int[][][] totalMealSchNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				distDishSchNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				distNoDishSchNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] distDishRates = new float[subLevelCount][maxCompDepCount];
		//验收数据
		Map<String, String> distributionTotalMap = null;
		int[][][] totalGsPlanNums = new int[dateCount][subLevelCount][maxCompDepCount];
		int[][][] noAcceptGsPlanNums = new int[dateCount][subLevelCount][maxCompDepCount]; 
		int[][][] acceptGsPlanNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] acceptRates = new float[subLevelCount][maxCompDepCount];
		//学校验收信息
		int[][][] shouldAcceptSchNums = new int[dateCount][subLevelCount][maxCompDepCount];
		int[][][] acceptSchNums = new int[dateCount][subLevelCount][maxCompDepCount]; 
		int[][][] noAcceptSchNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] schAcceptRates = new float[subLevelCount][maxCompDepCount];
		//菜品留样
		Map<String, String> gcRetentiondishtotalMap = null;
		int[][][] totalDishNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				dishRsDishNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				dishNoRsDishNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] dishRsRates = new float[subLevelCount][maxCompDepCount];
		
		//学校留样
		int[][][] rsSchNums = new int[dateCount][subLevelCount][maxCompDepCount];
		int[][][] noRsSchNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] schRsRates = new float[subLevelCount][maxCompDepCount];
		
		//证照逾期处理
		Map<String, String> warnTotalMap = null;
		int curWarnNum = 0;
		int[][][] totalWarnNums = new int[dateCount][subLevelCount][maxCompDepCount],
				noProcWarnNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				elimWarnNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] warnProcRates = new float[subLevelCount][maxCompDepCount];			
		//区域ID到索引映射
		Map<String, Integer> distIdToIdxMap = new HashMap<>();
		for(i = 0; i < distCount; i++) {
			distIdToIdxMap.put(tedList.get(i).getId(), i);
		}
		distIdToIdxMap.put("-", i);
		//餐厨垃圾回收
		Map<String, String> schoolwastetotalMap = null;
		float[][][] kwSchRcNums = new float[dateCount][subLevelCount][maxCompDepCount];			
		//废弃油脂回收
		Map<String, String> schooloiltotalMap = null;
		float[][][] woSchRcNums = new float[dateCount][subLevelCount][maxCompDepCount];			
		
		List<Object> subLevelList=CommonUtil.changeStringToList(subLevels);
		List<Object> compDepList=CommonUtil.changeStringToList(compDeps);
		
		// 各天各区排菜学校数量
		for (k = 0; k < dates.length; k++) {
			//排菜学校
			key = dates[k] + "_platoonfeed-total";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(platoonFeedTotalMap != null) {
				for(String curKey : platoonFeedTotalMap.keySet()) {
					for (i = 0; i < subLevelCount; i++) {
						if(i == 0)
							compDepCount = 1;
						else if(i == 1)
							compDepCount = 2;
						else if(i == 2)
							compDepCount = 8;
						else if(i == 3)
							compDepCount = 17;
						else
							compDepCount = 0;
						for(j = 0; j < compDepCount; j++) {
							//判断是否按主管部门获取数据
							if(subLevel != -1) {
								if(i != subLevel)
									continue ;
								else {
									if(compDep != -1) {
										if(compDep != j)
											continue ;
									}
								}
							}else if(subLevelList!=null && subLevelList.size()>0) {
								if(!subLevelList.contains(String.valueOf(i))) {
									continue;
								}else {
									if(compDepList!=null && compDepList.size()>0) {
										if(!compDepList.contains(i+"_"+(j))) {
											continue ;
										}
									}
								}
							}
							// 设置前置域名
							if(i < 3)
								fieldPrefix = "masterid_" + i + "_slave_" + j;
							else if(i == 3) {
								String compDepId = String.valueOf(j);								
								fieldPrefix = "masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
							}
							// 区域排菜学校供餐数
							int mealSchNum = 0, dishSchNum = 0, noDishSchNum = 0;
							if (curKey.indexOf(fieldPrefix) != -1) {
								String[] curKeys = curKey.split("_");
								if(curKeys.length >= 6)
								{
									if(curKeys[4].equalsIgnoreCase("供餐") && curKeys[5].equalsIgnoreCase("已排菜")) {
										keyVal = platoonFeedTotalMap.get(curKey);
										if(keyVal != null) {
											mealSchNum = Integer.parseInt(keyVal);
											dishSchNum = mealSchNum;
										}
									}
									else if(curKeys[4].equalsIgnoreCase("供餐") && curKeys[5].equalsIgnoreCase("未排菜")) {
										keyVal = platoonFeedTotalMap.get(curKey);
										if(keyVal != null) {
											mealSchNum = Integer.parseInt(keyVal);
											noDishSchNum = mealSchNum;
										}
									}
								}
							}
							totalMealSchNums[k][i][j] += mealSchNum;
							distDishSchNums[k][i][j] += dishSchNum;
							distNoDishSchNums[k][i][j] += noDishSchNum;
						}
					}
				}
			}
			//验收数据
			key = dates[k] + "_DistributionTotal";
			distributionTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if (distributionTotalMap != null) {
				for (i = 0; i < subLevelCount; i++) {
					if(i == 0)
						compDepCount = 1;
					else if(i == 1)
						compDepCount = 2;
					else if(i == 2)
						compDepCount = 8;
					else if(i == 3)
						compDepCount = 17;
					else
						compDepCount = 0;
					for(j = 0; j < compDepCount; j++) {
						//判断是否按主管部门获取数据
						if(subLevel != -1) {
							if(i != subLevel)
								continue ;
							else {
								if(compDep != -1) {
									if(compDep != j)
										continue ;
								}
							}
						}else if(subLevelList!=null && subLevelList.size()>0) {
							if(!subLevelList.contains(String.valueOf(i))) {
								continue;
							}else {
								if(compDepList!=null && compDepList.size()>0) {
									if(!compDepList.contains(i+"_"+(j))) {
										continue ;
									}
								}
							}
						}
						// 设置前置域名
						if(i < 3)
							fieldPrefix = "masterid_" + i + "_slave_" + j;
						else if(i == 3) {
							String compDepId = String.valueOf(j);								
							fieldPrefix = "masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
						}
						
						/**
						 * 配货计划验收信息：应验收配货计划、已验收配货计划、未验收配货计划、配货计划验收率
						 */
						// 区域配货计划总数
						for(int l = -2; l < 4; l++) {
							field = fieldPrefix + "_" + "status" + "_" + l;
							if(l == 3) { // 已验收数
								acceptGsPlanNums[k][i][j] = 0;
								keyVal = distributionTotalMap.get(field);
								if(keyVal != null) {
									acceptGsPlanNums[k][i][j] = Integer.parseInt(keyVal);
									if(acceptGsPlanNums[k][i][j] < 0)
										acceptGsPlanNums[k][i][j] = 0;
								}
								totalGsPlanNums[k][i][j] += acceptGsPlanNums[k][i][j];
							}
							else {   //未验收数
								keyVal = distributionTotalMap.get(field);
								int curGsPlanNum = 0;
								if(keyVal != null) {						
									curGsPlanNum = Integer.parseInt(keyVal);
									if(curGsPlanNum < 0)
										curGsPlanNum = 0;
								}
								// 未验收数
								noAcceptGsPlanNums[k][i][j] += curGsPlanNum;
								totalGsPlanNums[k][i][j] += curGsPlanNum;
							}
						}
						
						/**
						 * 学校验收信息：应验收学校、已验收学校、未验收学校、学校验收率
						 */
						// 设置前置域名 
						if(i < 3)
							fieldPrefix = "school-masterid_" + i + "_slave_" + j;
						else if(i == 3) {
							String compDepId = String.valueOf(j);								
							fieldPrefix = "school-masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
						}
						// 区域配货计划总数
						for(int l = -2; l < 4; l++) {
							field = fieldPrefix + "_" + "status" + "_" + l;
							if(l == 3) { // 已验收数
								acceptSchNums[k][i][j] = 0;
								keyVal = distributionTotalMap.get(field);
								if(keyVal != null && !"".equals(keyVal) && !"null".equals(keyVal)) {
									acceptSchNums[k][i][j] = Integer.parseInt(keyVal);
									if(acceptSchNums[k][i][j] < 0)
										acceptSchNums[k][i][j] = 0;
								}
								shouldAcceptSchNums[k][i][j] += acceptSchNums[k][i][j];
							}
							else {   //未验收数
								keyVal = distributionTotalMap.get(field);
								int curGsPlanNum = 0;
								if(keyVal != null && !"".equals(keyVal) && !"null".equals(keyVal)) {						
									curGsPlanNum = Integer.parseInt(keyVal);
									if(curGsPlanNum < 0)
										curGsPlanNum = 0;
								}
								// 未验收数
								noAcceptSchNums[k][i][j] += curGsPlanNum;
								shouldAcceptSchNums[k][i][j] += curGsPlanNum;
							}
						}
					}
				}
			}
			//菜品留样
			key = dates[k] + "_gc-retentiondishtotal";
			gcRetentiondishtotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(gcRetentiondishtotalMap != null) {
				for(String curKey : gcRetentiondishtotalMap.keySet()) {
					for (i = 0; i < subLevelCount; i++) {
						if(i == 0)
							compDepCount = 1;
						else if(i == 1)
							compDepCount = 2;
						else if(i == 2)
							compDepCount = 8;
						else if(i == 3)
							compDepCount = 17;
						else
							compDepCount = 0;
						for(j = 0; j < compDepCount; j++) {
							//判断是否按主管部门获取数据
							if(subLevel != -1) {
								if(i != subLevel)
									continue ;
								else {
									if(compDep != -1) {
										if(compDep != j)
											continue ;
									}
								}
							}else if(subLevelList!=null && subLevelList.size()>0) {
								if(!subLevelList.contains(String.valueOf(i))) {
									continue;
								}else {
									if(compDepList!=null && compDepList.size()>0) {
										if(!compDepList.contains(i+"_"+(j))) {
											continue ;
										}
									}
								}
							}
							/**
							 * 菜品留样相关
							 */
							// 设置前置域名
							if(i < 3)
								fieldPrefix = "masterid_" + i + "_slave_" + j;
							else if(i == 3) {
								String compDepId = String.valueOf(j);								
								fieldPrefix = "masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
							}
							// 区域菜品留样和未留样数
							if (curKey.indexOf(fieldPrefix) != -1) {
								String[] curKeys = curKey.split("_");
								if(curKeys.length >= 5)	{
									if(curKeys[4].equalsIgnoreCase("已留样")) {     //区域留样菜品总数
										keyVal = gcRetentiondishtotalMap.get(curKey);
										if(keyVal != null) {
											dishRsDishNums[k][i][j] = Integer.parseInt(keyVal);
											totalDishNums[k][i][j] += dishRsDishNums[k][i][j];
										}
									}
									else if(curKeys[4].equalsIgnoreCase("未留样")) {     //区域未留样菜品总数
										keyVal = gcRetentiondishtotalMap.get(curKey);
										if(keyVal != null) {
											dishNoRsDishNums[k][i][j] = Integer.parseInt(keyVal);
											totalDishNums[k][i][j] += dishNoRsDishNums[k][i][j];
										}
									}
								}
							}
							
							/**
							 * 学校留样相关
							 */
							// 设置前置域名
							if(i < 3)
								fieldPrefix = "school-masterid_" + i + "_slave_" + j;
							else if(i == 3) {
								String compDepId = String.valueOf(j);								
								fieldPrefix = "school-masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
							}
							// 区域菜品留样和未留样数
							if (curKey.indexOf(fieldPrefix) != -1) {
								String[] curKeys = curKey.split("_");
								if(curKeys.length >= 5)	{
									if(curKeys[4].equalsIgnoreCase("已留样")) {     //区域留样菜品总数
										keyVal = gcRetentiondishtotalMap.get(curKey);
										if(keyVal != null) {
											rsSchNums[k][i][j] = Integer.parseInt(keyVal);
										}
									}
									else if(curKeys[4].equalsIgnoreCase("未留样")) {     //区域未留样菜品总数
										keyVal = gcRetentiondishtotalMap.get(curKey);
										if(keyVal != null) {
											noRsSchNums[k][i][j] = Integer.parseInt(keyVal);
										}
									}
								}
							}
						}
					}
				}
			}
			//证照逾期处理
			key = dates[k] + "_warn-total";
			warnTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(warnTotalMap != null) {
				for(String curKey : warnTotalMap.keySet()) {
					String[] curKeys = curKey.split("_");
					for (i = 0; i < subLevelCount; i++) {
						if(i == 0)
							compDepCount = 1;
						else if(i == 1)
							compDepCount = 2;
						else if(i == 2)
							compDepCount = 8;
						else if(i == 3)
							compDepCount = 16;
						else
							compDepCount = 0;
						for(j = 0; j < compDepCount; j++) {
							//判断是否按主管部门获取数据
							if(subLevel != -1) {
								if(i != subLevel)
									continue ;
								else {
									if(compDep != -1) {
										if(compDep != j)
											continue ;
									}
								}
							}else if(subLevelList!=null && subLevelList.size()>0) {
								if(!subLevelList.contains(String.valueOf(i))) {
									continue;
								}else {
									if(compDepList!=null && compDepList.size()>0) {
										if(!compDepList.contains(i+"_"+(j))) {
											continue ;
										}
									}
								}
							}
							// 设置前置域名
							if(i < 3)
								fieldPrefix = "masterid_" + i + "_slave_" + j;
							else if(i == 3) {
								String compDepId = String.valueOf(j);								
								fieldPrefix = "masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
							}
							if (curKey.indexOf(fieldPrefix) != -1) {
								keyVal = warnTotalMap.get(curKey);
								curWarnNum = 0;
								if(keyVal != null)
									curWarnNum = Integer.parseInt(keyVal);
								if(curWarnNum < 0)
									curWarnNum = 0;
								int l = AppModConfig.getVarValIndex(curKeys, "status");
								if(l != -1) {
									if(curKeys[l].equalsIgnoreCase("1")) {         //未处理预警数
										noProcWarnNums[k][i][j] += curWarnNum;
										totalWarnNums[k][i][j] += curWarnNum;
									}
									else if(curKeys[l].equalsIgnoreCase("2")) {    //审核中预警数
										totalWarnNums[k][i][j] += curWarnNum;
									}
									else if(curKeys[l].equalsIgnoreCase("3")) {    //已驳回预警数
										totalWarnNums[k][i][j] += curWarnNum;
									}
									else if(curKeys[l].equalsIgnoreCase("4")) {    //已消除预警数
										elimWarnNums[k][i][j] += curWarnNum;
										totalWarnNums[k][i][j] += curWarnNum;
									}
								}
							}
						}
					}
				}
			}				
			//餐厨垃圾回收
			//学校回收桶数
			key = dates[k] + "_schoolwastetotal";
			schoolwastetotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(schoolwastetotalMap != null) {
				for(String curKey : schoolwastetotalMap.keySet()) {
					for (i = 0; i < subLevelCount; i++) {
						if(i == 0)
							compDepCount = 1;
						else if(i == 1)
							compDepCount = 2;
						else if(i == 2)
							compDepCount = 8;
						else if(i == 3)
							compDepCount = 16;
						else
							compDepCount = 0;
						for(j = 0; j < compDepCount; j++) {
							//判断是否按主管部门获取数据
							if(subLevel != -1) {
								if(i != subLevel)
									continue ;
								else {
									if(compDep != -1) {
										if(compDep != j)
											continue ;
									}
								}
							}else if(subLevelList!=null && subLevelList.size()>0) {
								if(!subLevelList.contains(String.valueOf(i))) {
									continue;
								}else {
									if(compDepList!=null && compDepList.size()>0) {
										if(!compDepList.contains(i+"_"+(j))) {
											continue ;
										}
									}
								}
							}
							// 设置前置域名
							if(i < 3)
								fieldPrefix = "masterid_" + i + "_slave_" + j;
							else if(i == 3) {
								String compDepId = String.valueOf(j);								
								fieldPrefix = "masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
							}
							// 学校回收垃圾桶数
							fieldPrefix += "_total";
							if (curKey.equalsIgnoreCase(fieldPrefix)) {
								keyVal = schoolwastetotalMap.get(curKey);
								if(keyVal != null) {
									kwSchRcNums[k][i][j] = Float.parseFloat(keyVal);
								}
							}
						}
					}
				}
			}
			//废弃油脂回收
			//学校回收桶数
			key = dates[k] + "_schooloiltotal";
			schooloiltotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(schooloiltotalMap != null) {
				for(String curKey : schooloiltotalMap.keySet()) {
					for (i = 0; i < subLevelCount; i++) {
						if(i == 0)
							compDepCount = 1;
						else if(i == 1)
							compDepCount = 2;
						else if(i == 2)
							compDepCount = 8;
						else if(i == 3)
							compDepCount = 17;
						else
							compDepCount = 0;
						for(j = 0; j < compDepCount; j++) {
							//判断是否按主管部门获取数据
							if(subLevel != -1) {
								if(i != subLevel)
									continue ;
								else {
									if(compDep != -1) {
										if(compDep != j)
											continue ;
									}
								}
							}else if(subLevelList!=null && subLevelList.size()>0) {
								if(!subLevelList.contains(String.valueOf(i))) {
									continue;
								}else {
									if(compDepList!=null && compDepList.size()>0) {
										if(!compDepList.contains(i+"_"+(j))) {
											continue ;
										}
									}
								}
							}
							// 设置前置域名
							if(i < 3)
								fieldPrefix = "masterid_" + i + "_slave_" + j;
							else if(i == 3) {
								String compDepId = String.valueOf(j);								
								fieldPrefix = "masterid_" + i + "_slave_" + AppModConfig.compDepIdToNameMap3.get(compDepId);
							}
							//区域回收垃圾桶数
							fieldPrefix += "_total";
							if (curKey.equalsIgnoreCase(fieldPrefix)) {
								keyVal = schooloiltotalMap.get(curKey);
								if(keyVal != null) {
									woSchRcNums[k][i][j] = Float.parseFloat(keyVal);
								}
							}
						}
					}
				}
			}
			
			// 该日期段各区排菜数据、验收数据、菜品留样、证照逾期处理、餐厨垃圾回收和废弃油脂回收
			for (i = 0; i < subLevelCount; i++) {
				if(i == 0)
					compDepCount = 1;
				else if(i == 1)
					compDepCount = 2;
				else if(i == 2)
					compDepCount = 8;
				else if(i == 3)
					compDepCount = 16;
				else
					compDepCount = 0;
				for(j = 0; j < compDepCount; j++) {
					//判断是否按主管部门获取数据
					if(subLevel != -1) {
						if(i != subLevel)
							continue ;
						else {
							if(compDep != -1) {
								if(compDep != j)
									continue ;
							}
						}
					}else if(subLevelList!=null && subLevelList.size()>0) {
						if(!subLevelList.contains(String.valueOf(i))) {
							continue;
						}else {
							if(compDepList!=null && compDepList.size()>0) {
								if(!compDepList.contains(i+"_"+(j))) {
									continue ;
								}
							}
						}
					}
					// 设置前置域名
					String compDepId = "", compDepName = "其他";
					if(i < 3) {   //其他、部属、市属
						compDepId = String.valueOf(j);
						fieldPrefix = "masterid_" + i + "_slave_" + j;
						if(i == 1) {
							compDepName = AppModConfig.compDepIdToNameMap1.get(compDepId);
						}
						else if(i == 2) {
							compDepName = AppModConfig.compDepIdToNameMap2.get(compDepId);
						}
					}
					else if(i == 3) {  //区属
						compDepId = String.valueOf(j);								
						compDepName = AppModConfig.compDepIdToNameMap3.get(compDepId);
						fieldPrefix = "masterid_" + i + "_slave_" + compDepName;
					}
					// 区域学校排菜率
					if (totalMealSchNums[k][i][j] != 0) {
						distDishRates[i][j] = 100 * ((float) distDishSchNums[k][i][j] / (float) totalMealSchNums[k][i][j]);
						BigDecimal bd = new BigDecimal(distDishRates[i][j]);
						distDishRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (distDishRates[i][j] > 100) {
							distDishRates[i][j] = 100;
							distDishSchNums[k][i][j] = totalMealSchNums[k][i][j];
						}
					}
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) + "，主管部门：" + compDepName 
							+ "，排菜学校数量：" + distDishSchNums[k][i] 	+ "，供餐学校总数：" + totalMealSchNums[k][i] 
									+ "，排菜率：" + distDishRates[i] + "，field = " + field);				
					//配货单验收率
					if (totalGsPlanNums[k][i][j] != 0) {
						//验收率
						acceptRates[i][j] = 100 * ((float) acceptGsPlanNums[k][i][j] / (float) totalGsPlanNums[k][i][j]);
						BigDecimal bd = new BigDecimal(acceptRates[i][j]);
						acceptRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (acceptRates[i][j] > 100) {
							acceptRates[i][j] = 100;
						}
					}
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) + "，主管部门：" + compDepName 
							+ "，配货计划总数：" + totalGsPlanNums[k][i] + "，验收数：" + acceptGsPlanNums[k][i] + "，验收率：" + acceptRates[i]);
					// 区域留样率
					if (totalDishNums[k][i][j] != 0) {
						dishRsRates[i][j] = 100 * ((float) dishRsDishNums[k][i][j] / (float) totalDishNums[k][i][j]);
						BigDecimal bd = new BigDecimal(dishRsRates[i][j]);
						dishRsRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (dishRsRates[i][j] > 100) {
							dishRsRates[i][j] = 100;
							dishRsDishNums[k][i][j] = totalDishNums[k][i][j];
						}
					}
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) + "，主管部门：" + compDepName + "，菜品数量：" + totalDishNums[k][i]
							+ "，已留样菜品数：" + dishRsDishNums[k][i] + "，未留样菜品数：" + dishNoRsDishNums[k][i] + "，留样率：" + dishRsRates[i] + "，field = "
							+ field);
					//证照逾期处理
					int totalWarnNum = totalWarnNums[k][i][j], elimWarnNum = elimWarnNums[k][i][j];
					warnProcRates[i][j] = 0;
					if(totalWarnNum > 0) {
						warnProcRates[i][j] = 100 * ((float) elimWarnNum / (float) totalWarnNum);
						BigDecimal bd = new BigDecimal(warnProcRates[i][j]);
						warnProcRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (warnProcRates[i][j] > 100)
							warnProcRates[i][j] = 100;
					}
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) 
					+ "，主管部门：" + compDepName + "，预警数量：" + totalWarnNums[k][i]
							+ "，已处理预警数：" + elimWarnNums[k][i] + "，未处理预警数：" + noProcWarnNums[k][i] 
									+ "，处理率：" + warnProcRates[i] + "，field = "
							+ field);					
					//餐厨垃圾回收
					//餐厨垃圾学校回收
					BigDecimal bd = new BigDecimal(kwSchRcNums[k][i][j]);
					kwSchRcNums[k][i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) + "，主管部门：" + compDepName 
							+ "，餐厨垃圾学校回收数量：" + kwSchRcNums[k][i][j] + " 桶" + "，field = " + field);				
					//废弃油脂回收
					//废弃油脂学校回收
					bd = new BigDecimal(woSchRcNums[k][i][j]);
					woSchRcNums[k][i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) + "，主管部门：" + compDepName 
							+ "，废弃油脂学校回收数量：" + woSchRcNums[k][i] + " 桶" + "，field = " + field);
				}
			}
		}		
		for (i = 0; i < subLevelCount; i++) {
			if(i == 0)
				compDepCount = 1;
			else if(i == 1)
				compDepCount = 2;
			else if(i == 2)
				compDepCount = 8;
			else if(i == 3)
				compDepCount = 17;
			else
				compDepCount = 0;
			for(j = 0; j < compDepCount; j++) {
				//判断是否按主管部门获取数据
				if(subLevel != -1) {
					if(i != subLevel)
						continue ;
					else {
						if(compDep != -1) {
							if(compDep != j)
								continue ;
						}
					}
				}else if(subLevelList!=null && subLevelList.size()>0) {
					if(!subLevelList.contains(String.valueOf(i))) {
						continue;
					}else {
						if(compDepList!=null && compDepList.size()>0) {
							if(!compDepList.contains(i+"_"+(j))) {
								continue ;
							}
						}
					}
				}
				// 设置前置域名
				String compDepId = "", compDepName = "其他";
				if(i < 3) {   //其他、部属、市属
					compDepId = String.valueOf(j);
					fieldPrefix = "masterid_" + i + "_slave_" + j;
					if(i == 1) {
						compDepName = AppModConfig.compDepIdToNameMap1.get(compDepId);
					}
					else if(i == 2) {
						compDepName = AppModConfig.compDepIdToNameMap2.get(compDepId);
					}
				}
				else if(i == 3) {  //区属
					compDepId = String.valueOf(j);								
					compDepName = AppModConfig.compDepIdToNameMap3.get(compDepId);
					fieldPrefix = "masterid_" + i + "_slave_" + compDepName;
				}
				for (k = 0; k < dates.length; k++) {
					sdd = new SumDataDets();
					//日期
					sdd.setDishDate(dates[k].replaceAll("-", "/"));
					//所属
					sdd.setSubLevel(AppModConfig.subLevelIdToNameMap.get(i));
					//主管部门
					sdd.setCompDep(compDepName);
					//应排菜数数、已排菜数、未排菜数、排菜率
					int totalDistSchNum = 0, distDishSchNum = 0, distNoDishSchNum = 0;					
					totalDistSchNum = totalMealSchNums[k][i][j];
					distDishSchNum = distDishSchNums[k][i][j];
					distNoDishSchNum = distNoDishSchNums[k][i][j];
					sdd.setMealSchNum(totalDistSchNum);
					sdd.setDishSchNum(distDishSchNum);
					sdd.setNoDishSchNum(distNoDishSchNum);
					distDishRates[i][j] = 0;
					if(totalDistSchNum > 0) {
						distDishRates[i][j] = 100 * ((float) distDishSchNum / (float) totalDistSchNum);
						BigDecimal bd = new BigDecimal(distDishRates[i][j]);
						distDishRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (distDishRates[i][j] > 100)
							distDishRates[i][j] = 100;
					}
					sdd.setDishRate(distDishRates[i][j]);	
					//验收数据：配货计划总数、待验收数、	已验收数、验收率
					int totalGsPlanNum = 0, noAcceptGsPlanNum = 0, acceptGsPlanNum = 0;
					totalGsPlanNum = totalGsPlanNums[k][i][j];
					noAcceptGsPlanNum = noAcceptGsPlanNums[k][i][j];
					acceptGsPlanNum = acceptGsPlanNums[k][i][j];
					//验收数量及验收率
					acceptRates[i][j] = 0;
					if(totalGsPlanNum > 0) {
						acceptRates[i][j] = 100 * ((float) acceptGsPlanNum / (float) totalGsPlanNum);
						BigDecimal bd = new BigDecimal(acceptRates[i][j]);
						acceptRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (acceptRates[i][j] > 100)
							acceptRates[i][j] = 100;
					}
					sdd.setTotalGsPlanNum(totalGsPlanNum);
					sdd.setNoAcceptGsPlanNum(noAcceptGsPlanNum);
					sdd.setAcceptGsPlanNum(acceptGsPlanNum);
					sdd.setAcceptRate(acceptRates[i][j]);
					//学校验收信息以及验收率
					int shouldAcceptSchNum = 0;
					int acceptSchNum = 0;
					int noAcceptSchNum = 0;
					shouldAcceptSchNum = shouldAcceptSchNums[k][i][j];
					acceptSchNum = acceptSchNums[k][i][j];
					noAcceptSchNum = noAcceptSchNums[k][i][j];
					sdd.setShouldAcceptSchNum(shouldAcceptSchNum);
					sdd.setAcceptSchNum(acceptSchNum);
					sdd.setNoAcceptSchNum(noAcceptSchNum);
					
					schAcceptRates[i][j] = 0;
					if(shouldAcceptSchNum > 0) {
						schAcceptRates[i][j] = 100 * ((float) acceptSchNum / (float) shouldAcceptSchNum);
						BigDecimal bd = new BigDecimal(schAcceptRates[i][j]);
						schAcceptRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (schAcceptRates[i][j] > 100)
							schAcceptRates[i][j] = 100;
					}
					sdd.setSchAcceptRate(schAcceptRates[i][j]);
					
					//菜品总数、未留样数、已留样数、留样率
					int totalDishNum = 0, dishRsDishNum = 0, dishNoRsDishNum = 0;					
					totalDishNum = totalDishNums[k][i][j];
					dishRsDishNum = dishRsDishNums[k][i][j];
					dishNoRsDishNum = dishNoRsDishNums[k][i][j];
					sdd.setTotalDishNum(totalDishNum);
					sdd.setRsDishNum(dishRsDishNum);
					sdd.setNoRsDishNum(dishNoRsDishNum);
					dishRsRates[i][j] = 0;
					if(totalDishNum > 0) {
						dishRsRates[i][j] = 100 * ((float) dishRsDishNum / (float) totalDishNum);
						BigDecimal bd = new BigDecimal(dishRsRates[i][j]);
						dishRsRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (dishRsRates[i][j] > 100)
							dishRsRates[i][j] = 100;
					}
					sdd.setRsRate(dishRsRates[i][j]);
					
					//学校留样相关信息
					int shouldRsSchNum = 0;
					int rsSchNum = 0;
					int noRsSchNum = 0;
					
					rsSchNum = rsSchNums[k][i][j];
					noRsSchNum = noRsSchNums[k][i][j];
					shouldRsSchNum = rsSchNum + noRsSchNum;
					sdd.setShouldRsSchNum(shouldRsSchNum);
					sdd.setRsSchNum(rsSchNum);
					sdd.setNoRsSchNum(noRsSchNum);
					schRsRates[i][j] = 0;
					if(shouldRsSchNum > 0) {
						schRsRates[i][j] = 100 * ((float) rsSchNum / (float) shouldRsSchNum);
						BigDecimal bd = new BigDecimal(schRsRates[i][j]);
						schRsRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (schRsRates[i][j] > 100)
							schRsRates[i][j] = 100;
					}
					sdd.setSchRsRate(schRsRates[i][j]);
					
					
					//预警总数、待处理预警数、已消除预警数、预警处理率
					int totalWarnNum = 0, noProcWarnNum = 0, elimWarnNum = 0;					
					totalWarnNum = totalWarnNums[k][i][j];
					noProcWarnNum = noProcWarnNums[k][i][j];
					elimWarnNum = elimWarnNums[k][i][j];
					sdd.setTotalWarnNum(totalWarnNum);
					sdd.setNoProcWarnNum(noProcWarnNum);
					sdd.setElimWarnNum(elimWarnNum);
					warnProcRates[i][j] = 0;
					if(totalWarnNum > 0) {
						warnProcRates[i][j] = 100 * ((float) elimWarnNum / (float) totalWarnNum);
						BigDecimal bd = new BigDecimal(warnProcRates[i][j]);
						warnProcRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (warnProcRates[i][j] > 100)
							warnProcRates[i][j] = 100;
					}
					sdd.setWarnProcRate(warnProcRates[i][j]);
					//餐厨垃圾学校回收数量
					float kwSchRcNum = 0, kwRmcRcNum = 0, kwTotalRcNum = 0;
					kwSchRcNum = kwSchRcNums[k][i][j];
					sdd.setTotalKwRecNum((int)kwTotalRcNum);
					sdd.setKwSchRecNum((int)kwSchRcNum);
					sdd.setKwRmcRecNum((int)kwRmcRcNum);
					//废弃油脂学校回收数量
					float woSchRcNum = 0, woRmcRcNum = 0, woTotalRcNum = 0;					
					woSchRcNum += woSchRcNums[k][i][j];
					sdd.setTotalWoRecNum((int)woTotalRcNum);
					sdd.setWoSchRecNum((int)woSchRcNum);
					sdd.setWoRmcRecNum((int)woRmcRcNum);
					sumDataDets.add(sdd);
				}
			}
		}
		//排序
    	SortList<SumDataDets> sortList = new SortList<SumDataDets>();  
    	sortList.Sort(sumDataDets, methods0, sorts0, dataTypes0);
		//时戳
		sddDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<SumDataDets> pageBean = new PageBean<SumDataDets>(sumDataDets, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		sddDto.setPageInfo(pageInfo);
		// 设置数据
		sddDto.setSumDataDets(pageBean.getCurPageData());
		// 消息ID
		sddDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return sddDto;
	}
	
	//汇总数据详情列表函数按所在区
	private SumDataDetsDTO sumDataDetsByLocality(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList,
			String distNames) {
		SumDataDetsDTO sddDto = new SumDataDetsDTO();
		List<SumDataDets> sumDataDets = new ArrayList<>();
		SumDataDets sdd = null;
		String key = "", keyVal = "", field = "", fieldPrefix = "";
		int i, j, k;
		//排菜数据
		Map<String, String> platoonFeedTotalMap = null;
		int distCount = tedList.size(), dateCount = dates.length;
		int[][] totalMealSchNums = new int[dateCount][distCount], 
				distDishSchNums = new int[dateCount][distCount], 
				distNoDishSchNums = new int[dateCount][distCount];
		float[] distDishRates = new float[distCount];
		//验收数据
		Map<String, String> distributionTotalMap = null;
		int[][] totalGsPlanNums = new int[dateCount][distCount], 
				noAcceptGsPlanNums = new int[dateCount][distCount],
				acceptGsPlanNums = new int[dateCount][distCount];
		float[] acceptRates = new float[distCount];
		
		//学校验收信息
		int[][] shouldAcceptSchNums = new int[dateCount][distCount];
		int[][] acceptSchNums = new int[dateCount][distCount]; 
		int[][] noAcceptSchNums = new int[dateCount][distCount];
		float[] schAcceptRates = new float[distCount];
		
		//菜品留样
		Map<String, String> gcRetentiondishtotalMap = null;
		int[][] totalDishNums = new int[dateCount][distCount];
		int[][] dishRsDishNums = new int[dateCount][distCount];
		int[][] dishNoRsDishNums = new int[dateCount][distCount];
		float[] dishRsRates = new float[distCount];
		
		//学校留样
		int[][] rsSchNums = new int[dateCount][distCount];
		int[][] noRsSchNums = new int[dateCount][distCount];
		float[] schRsRates = new float[distCount];
		
		//证照逾期处理
		Map<String, String> warnTotalMap = null;
		int curWarnNum = 0;
		int[][] totalWarnNums = new int[dateCount][distCount], 
				noProcWarnNums = new int[dateCount][distCount], 
				elimWarnNums = new int[dateCount][distCount];
		float[] warnProcRates = new float[distCount];			
		//区域ID到索引映射
		Map<String, Integer> distIdToIdxMap = new HashMap<>();
		for(i = 0; i < distCount; i++) {
			distIdToIdxMap.put(tedList.get(i).getId(), i);
		}
		distIdToIdxMap.put("-", i);
		//餐厨垃圾回收
		Map<String, String> schoolwastetotalMap = null, 
				supplierwastetotalMap = null;
		int[][] kwSchRcFreqs = new int[dateCount][distCount], 
				kwRmcRcFreqs = new int[dateCount][distCount];
		float[][] kwSchRcNums = new float[dateCount][distCount], 
				kwRmcRcNums = new float[dateCount][distCount], 
				kwTotalRcNums = new float[dateCount][distCount];			
		//废弃油脂回收
		Map<String, String> schooloiltotalMap = null, 
				supplieroiltotalMap = null;
		int[][] woSchRcFreqs = new int[dateCount][distCount], 
				woRmcRcFreqs = new int[dateCount][distCount];
		float[][] woSchRcNums = new float[dateCount][distCount], 
				woRmcRcNums = new float[dateCount][distCount], 
				woTotalRcNums = new float[dateCount][distCount];	
		List<Object> distNamesList=CommonUtil.changeStringToList(distNames);
		
		// 各天各区排菜学校数量
		for (k = 0; k < dates.length; k++) {
			//排菜学校
			key = dates[k] + "_platoonfeed-total";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(platoonFeedTotalMap != null) {
				for(String curKey : platoonFeedTotalMap.keySet()) {
					for (i = 0; i < tedList.size(); i++) {
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
						// 区域排菜学校供餐数
						fieldPrefix = curDistId + "_";
						int mealSchNum = 0, dishSchNum = 0, noDishSchNum = 0;
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
										noDishSchNum = mealSchNum;
									}
								}
							}
						}
						totalMealSchNums[k][i] += mealSchNum;
						distDishSchNums[k][i] += dishSchNum;
						distNoDishSchNums[k][i] += noDishSchNum;
					}
				}
			}
			//验收数据
			key = dates[k] + "_DistributionTotal";
			distributionTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if (distributionTotalMap != null) {
				for (i = 0; i < tedList.size(); i++) {
					TEduDistrictDo curTdd = tedList.get(i);
					String curDistId = curTdd.getId();
					//判断是否按区域获取排菜数据（distIdorSCName为空表示按省或直辖市级别获取数据）
					if(distIdorSCName != null) {
						if(!curDistId.equalsIgnoreCase(distIdorSCName))
							continue ;
					}else if(distNamesList!=null && distNamesList.size() >0) {
						if(StringUtils.isEmpty(curDistId) || !StringUtils.isNumeric(curDistId)) {
							continue;
						}
						if(!distNamesList.contains(curDistId)) {
							continue ;
						}
					}
					// 区域配货计划总数
					field = "area" + "_" + curDistId;
					totalGsPlanNums[k][i] = 0;
					keyVal = distributionTotalMap.get(field);
					if(keyVal != null) {
						totalGsPlanNums[k][i] = Integer.parseInt(keyVal);
						if(totalGsPlanNums[k][i] < 0)
							totalGsPlanNums[k][i] = 0;
					}
					// 已验收数
					field = "area" + "_" + curDistId + "_" + "status" + "_3";
					acceptGsPlanNums[k][i] = 0;
					keyVal = distributionTotalMap.get(field);
					if(keyVal != null) {
						acceptGsPlanNums[k][i] = Integer.parseInt(keyVal);
						if(acceptGsPlanNums[k][i] < 0)
							acceptGsPlanNums[k][i] = 0;
					}
					// 未验收数
					noAcceptGsPlanNums[k][i] = totalGsPlanNums[k][i] - acceptGsPlanNums[k][i];
					
					/**
					 * 学校验收信息
					 */
					// 已验收数
					acceptSchNums[k][i]=0;
					field = "school-area" + "_" + curDistId + "_" + "status" + "_3";
					acceptSchNums[k][i] = 0;
					keyVal = distributionTotalMap.get(field);
					if(keyVal != null) {
						acceptSchNums[k][i] = Integer.parseInt(keyVal);
						if(acceptSchNums[k][i] < 0)
							acceptSchNums[k][i] = 0;
					}
					// 未验收学校
					noAcceptSchNums[k][i] = 0;
					for(int m = -2; m <= 2; m++) {
						field = "school-area" + "_" + curDistId + "_" + "status" + "_" + m;
						keyVal = distributionTotalMap.get(field);
						if(keyVal != null) {
							int noAcceptSchNum = Integer.parseInt(keyVal);
							if(noAcceptSchNum < 0) {
								noAcceptSchNum = 0;
							}
							noAcceptSchNums[k][i] += noAcceptSchNum;
						}
					}
					
				}
			}
			//菜品留样
			key = dates[k] + "_gc-retentiondishtotal";
			gcRetentiondishtotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(gcRetentiondishtotalMap != null) {
				for(String curKey : gcRetentiondishtotalMap.keySet()) {
					for (i = 0; i < tedList.size(); i++) {
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
						// 区域菜品留样和未留样数
						fieldPrefix = curDistId + "_";
						if (curKey.indexOf(fieldPrefix) == 0) {
							String[] curKeys = curKey.split("_");
							if(curKeys.length >= 2)
							{
								if(curKeys[1].equalsIgnoreCase("已留样")) {     //区域留样菜品总数
									keyVal = gcRetentiondishtotalMap.get(curKey);
									if(keyVal != null) {
										dishRsDishNums[k][i] = Integer.parseInt(keyVal);
									}
								}
								else if(curKeys[1].equalsIgnoreCase("未留样")) {     //区域未留样菜品总数
									keyVal = gcRetentiondishtotalMap.get(curKey);
									if(keyVal != null) {
										dishNoRsDishNums[k][i] = Integer.parseInt(keyVal);
									}
								}
							}
						}
						if(curKey.equalsIgnoreCase(curDistId)) {      //区域菜品总数
							keyVal = gcRetentiondishtotalMap.get(curKey);
							if(keyVal != null) {
								totalDishNums[k][i] = Integer.parseInt(keyVal);
							}
						}
						
						// 区域学校留样和未留样数
						fieldPrefix = "school-area" + "_" + curDistId + "_";
						if (curKey.indexOf(fieldPrefix) == 0) {
							String[] curKeys = curKey.split("_");
							if(curKeys.length >= 3)
							{
								if(curKeys[2].equalsIgnoreCase("已留样")) {     //区域留样菜品总数
									keyVal = gcRetentiondishtotalMap.get(curKey);
									if(keyVal != null) {
										rsSchNums[k][i] = Integer.parseInt(keyVal);
									}
								}
								else if(curKeys[2].equalsIgnoreCase("未留样")) {     //区域未留样菜品总数
									keyVal = gcRetentiondishtotalMap.get(curKey);
									if(keyVal != null) {
										noRsSchNums[k][i] = Integer.parseInt(keyVal);
									}
								}
							}
						}
					}
				}
			}				
			//证照逾期处理
			key = dates[k] + "_warn-total";
			warnTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(warnTotalMap != null) {
				for(String curKey : warnTotalMap.keySet()) {
					String[] curKeys = curKey.split("_");
					if(curKeys.length == 4) {
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
											totalWarnNums[k][idx] += curWarnNum;
										}
										else if(curKeys[j].equalsIgnoreCase("3")) {    //已驳回预警数
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
			//餐厨垃圾回收
			//学校回收桶数
			key = dates[k] + "_schoolwastetotal";
			schoolwastetotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(schoolwastetotalMap != null) {
				for(String curKey : schoolwastetotalMap.keySet()) {
					for (i = 0; i < tedList.size(); i++) {
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
								kwSchRcFreqs[k][i] = Integer.parseInt(keyVal);
							}
						}
						// 区域回收垃圾桶数
						fieldPrefix = curDistId;
						if (curKey.equalsIgnoreCase(fieldPrefix)) {
							keyVal = schoolwastetotalMap.get(curKey);
							if(keyVal != null) {
								kwSchRcNums[k][i] = Float.parseFloat(keyVal);
							}
						}
					}
				}
			}
			//团餐公司回收桶数
			key = dates[k] + "_supplierwastetotal";
			supplierwastetotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(supplierwastetotalMap != null) {
				for(String curKey : supplierwastetotalMap.keySet()) {
					for (i = 0; i < tedList.size(); i++) {
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
							keyVal = supplierwastetotalMap.get(curKey);
							if(keyVal != null) {
								kwRmcRcFreqs[k][i] = Integer.parseInt(keyVal);
							}
						}
						// 区域回收垃圾桶数
						fieldPrefix = curDistId;
						if (curKey.equalsIgnoreCase(fieldPrefix)) {
							keyVal = supplierwastetotalMap.get(curKey);
							if(keyVal != null) {
								kwRmcRcNums[k][i] = Float.parseFloat(keyVal);
							}
						}
						
					}
				}
			}				
			//废弃油脂回收
			//学校回收桶数
			key = dates[k] + "_schooloiltotal";
			schooloiltotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(schooloiltotalMap != null) {
				for(String curKey : schooloiltotalMap.keySet()) {
					for (i = 0; i < tedList.size(); i++) {
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
							keyVal = schooloiltotalMap.get(curKey);
							if(keyVal != null) {
								woSchRcFreqs[k][i] = Integer.parseInt(keyVal);
							}
						}
						//区域回收垃圾桶数
						fieldPrefix = curDistId;
						if (curKey.equalsIgnoreCase(fieldPrefix)) {
							keyVal = schooloiltotalMap.get(curKey);
							if(keyVal != null) {
								woSchRcNums[k][i] = Float.parseFloat(keyVal);
							}
						}
					}
				}
			}
			//团餐公司回收桶数
			key = dates[k] + "_supplieroiltotal";
			supplieroiltotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(supplieroiltotalMap != null) {
				for(String curKey : supplieroiltotalMap.keySet()) {
					for (i = 0; i < tedList.size(); i++) {
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
							keyVal = supplieroiltotalMap.get(curKey);
							if(keyVal != null) {
								woRmcRcFreqs[k][i] = Integer.parseInt(keyVal);
							}
						}
						//区域回收垃圾桶数
						fieldPrefix = curDistId;
						if (curKey.equalsIgnoreCase(fieldPrefix)) {
							keyVal = supplieroiltotalMap.get(curKey);
							if(keyVal != null) {
								woRmcRcNums[k][i] = Float.parseFloat(keyVal);
							}
						}
					}
				}
			}
			
			// 该日期段各区排菜数据、验收数据、菜品留样、证照逾期处理、餐厨垃圾回收和废弃油脂回收
			for (i = 0; i < distCount; i++) {
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
				// 区域学校排菜率
				if (totalMealSchNums[k][i] != 0) {
					distDishRates[i] = 100 * ((float) distDishSchNums[k][i] / (float) totalMealSchNums[k][i]);
					BigDecimal bd = new BigDecimal(distDishRates[i]);
					distDishRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (distDishRates[i] > 100) {
						distDishRates[i] = 100;
						distDishSchNums[k][i] = totalMealSchNums[k][i];
					}
				}
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，排菜学校数量：" + distDishSchNums[k][i] 	
						+ "，供餐学校总数：" + totalMealSchNums[k][i] + "，排菜率：" + distDishRates[i] + "，field = " + field);				
				//验收数据
				if (totalGsPlanNums[k][i] != 0) {
					//验收率
					acceptRates[i] = 100 * ((float) acceptGsPlanNums[k][i] / (float) totalGsPlanNums[k][i]);
					BigDecimal bd = new BigDecimal(acceptRates[i]);
					acceptRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (acceptRates[i] > 100) {
						acceptRates[i] = 100;
					}
				}
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，配货计划总数：" + totalGsPlanNums[k][i] 
						+ "，验收数：" + acceptGsPlanNums[k][i] + "，验收率：" + acceptRates[i]);
				// 区域留样率
				if (totalDishNums[k][i] != 0) {
					dishRsRates[i] = 100 * ((float) dishRsDishNums[k][i] / (float) totalDishNums[k][i]);
					BigDecimal bd = new BigDecimal(dishRsRates[i]);
					dishRsRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (dishRsRates[i] > 100) {
						dishRsRates[i] = 100;
						dishRsDishNums[k][i] = totalDishNums[k][i];
					}
				}
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，菜品数量：" + totalDishNums[k][i]
						+ "，已留样菜品数：" + dishRsDishNums[k][i] + "，未留样菜品数：" + dishNoRsDishNums[k][i] + "，留样率：" + dishRsRates[i] + "，field = "
						+ field);
				//证照逾期处理
				int totalWarnNum = totalWarnNums[k][i], elimWarnNum = elimWarnNums[k][i];
				warnProcRates[i] = 0;
				if(totalWarnNum > 0) {
					warnProcRates[i] = 100 * ((float) elimWarnNum / (float) totalWarnNum);
					BigDecimal bd = new BigDecimal(warnProcRates[i]);
					warnProcRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (warnProcRates[i] > 100)
						warnProcRates[i] = 100;
				}
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，预警数量：" + totalWarnNums[k][i]
						+ "，已处理预警数：" + elimWarnNums[k][i] + "，未处理预警数：" + noProcWarnNums[k][i] + "，处理率：" + warnProcRates[i] + "，field = "
						+ field);					
				//餐厨垃圾回收
				//餐厨垃圾学校回收
				BigDecimal bd = new BigDecimal(kwSchRcNums[k][i]);
				kwSchRcNums[k][i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，餐厨垃圾学校回收次数：" + kwSchRcFreqs[k][i]
						+ "，餐厨垃圾学校回收数量：" + kwSchRcNums[k][i] + " 桶" + "，field = " + field);
				//餐厨垃圾团餐公司回收
				bd = new BigDecimal(kwRmcRcNums[k][i]);
				kwRmcRcNums[k][i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，餐厨垃圾团餐公司回收次数：" + kwRmcRcFreqs[k][i]
						+ "，餐厨垃圾团餐公司回收数量：" + kwRmcRcNums[k][i] + " 桶" + "，field = " + field);
				kwTotalRcNums[k][i] = kwSchRcNums[k][i] + kwRmcRcNums[k][i];					
				//废弃油脂回收
				//废弃油脂学校回收
				bd = new BigDecimal(woSchRcNums[k][i]);
				woSchRcNums[k][i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，废弃油脂学校回收次数：" + woSchRcFreqs[k][i]
						+ "，废弃油脂学校回收数量：" + woSchRcNums[k][i] + " 桶" + "，field = " + field);
				//废弃油脂团餐公司回收
				bd = new BigDecimal(woRmcRcNums[k][i]);
				woRmcRcNums[k][i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
				logger.info("日期：" + dates[k] + "，辖区名称：" + curTdd.getName() + "，/废弃油脂团餐公司回收次数：" + woRmcRcFreqs[k][i]
						+ "，/废弃油脂团餐公司回收数量：" + woRmcRcNums[k][i] + " 桶" + "，field = " + field);
				woTotalRcNums[k][i] = woSchRcNums[k][i] + woRmcRcNums[k][i];
			}
		}
		for (i = 0; i < distCount; i++) {
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
			for (k = 0; k < dates.length; k++) {
				sdd = new SumDataDets();
				//日期
				sdd.setDishDate(dates[k].replaceAll("-", "/"));
				//所在区域
				sdd.setDistName(curTdd.getName());
				//应排菜数数、已排菜数、未排菜数、排菜率
				int totalDistSchNum = 0, distDishSchNum = 0, distNoDishSchNum = 0;			
				totalDistSchNum = totalMealSchNums[k][i];
				distDishSchNum = distDishSchNums[k][i];
				distNoDishSchNum = distNoDishSchNums[k][i];
				sdd.setMealSchNum(totalDistSchNum);
				sdd.setDishSchNum(distDishSchNum);
				sdd.setNoDishSchNum(distNoDishSchNum);
				distDishRates[i] = 0;
				if(totalDistSchNum > 0) {
					distDishRates[i] = 100 * ((float) distDishSchNum / (float) totalDistSchNum);
					BigDecimal bd = new BigDecimal(distDishRates[i]);
					distDishRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (distDishRates[i] > 100)
						distDishRates[i] = 100;
				}
				sdd.setDishRate(distDishRates[i]);
				//验收数据：配货计划总数、待验收数、	已验收数、验收率
				int totalGsPlanNum = 0, noAcceptGsPlanNum = 0, acceptGsPlanNum = 0;				
				totalGsPlanNum = totalGsPlanNums[k][i];
				noAcceptGsPlanNum = noAcceptGsPlanNums[k][i];
				acceptGsPlanNum = acceptGsPlanNums[k][i];
				//验收数量及验收率
				acceptRates[i] = 0;
				if(totalGsPlanNum > 0) {
					acceptRates[i] = 100 * ((float) acceptGsPlanNum / (float) totalGsPlanNum);
					BigDecimal bd = new BigDecimal(acceptRates[i]);
					acceptRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (acceptRates[i] > 100)
						acceptRates[i] = 100;
				}
				sdd.setTotalGsPlanNum(totalGsPlanNum);
				sdd.setNoAcceptGsPlanNum(noAcceptGsPlanNum);
				sdd.setAcceptGsPlanNum(acceptGsPlanNum);
				sdd.setAcceptRate(acceptRates[i]);
				
				//学校验收信息以及验收率
				int shouldAcceptSchNum = 0;
				int acceptSchNum = 0;
				int noAcceptSchNum = 0;
				acceptSchNum = acceptSchNums[k][i];
				noAcceptSchNum = noAcceptSchNums[k][i];
				
				shouldAcceptSchNum = acceptSchNum + noAcceptSchNum;
				
				sdd.setShouldAcceptSchNum(shouldAcceptSchNum);
				sdd.setAcceptSchNum(acceptSchNum);
				sdd.setNoAcceptSchNum(noAcceptSchNum);
				
				schAcceptRates[i] = 0;
				if(shouldAcceptSchNum > 0) {
					schAcceptRates[i] = 100 * ((float) acceptSchNum / (float) shouldAcceptSchNum);
					BigDecimal bd = new BigDecimal(schAcceptRates[i]);
					schAcceptRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (schAcceptRates[i] > 100)
						schAcceptRates[i] = 100;
				}
				sdd.setSchAcceptRate(schAcceptRates[i]);
				
				//菜品总数、未留样数、已留样数、留样率
				int totalDishNum = 0, dishRsDishNum = 0, dishNoRsDishNum = 0;
				totalDishNum = totalDishNums[k][i];
				dishRsDishNum = dishRsDishNums[k][i];
				dishNoRsDishNum = dishNoRsDishNums[k][i];
				sdd.setTotalDishNum(totalDishNum);
				sdd.setRsDishNum(dishRsDishNum);
				sdd.setNoRsDishNum(dishNoRsDishNum);
				dishRsRates[i] = 0;
				if(totalDishNum > 0) {
					dishRsRates[i] = 100 * ((float) dishRsDishNum / (float) totalDishNum);
					BigDecimal bd = new BigDecimal(dishRsRates[i]);
					dishRsRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (dishRsRates[i] > 100)
						dishRsRates[i] = 100;
				}
				sdd.setRsRate(dishRsRates[i]);
				
				//学校留样相关信息
				int shouldRsSchNum = 0;
				int rsSchNum = 0;
				int noRsSchNum = 0;
				
				rsSchNum = rsSchNums[k][i];
				noRsSchNum = noRsSchNums[k][i];
				shouldRsSchNum = rsSchNum + noRsSchNum;
				sdd.setShouldRsSchNum(shouldRsSchNum);
				sdd.setRsSchNum(rsSchNum);
				sdd.setNoRsSchNum(noRsSchNum);
				schRsRates[i] = 0;
				if(shouldRsSchNum > 0) {
					schRsRates[i] = 100 * ((float) rsSchNum / (float) shouldRsSchNum);
					BigDecimal bd = new BigDecimal(schRsRates[i]);
					schRsRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (schRsRates[i] > 100)
						schRsRates[i] = 100;
				}
				sdd.setSchRsRate(schRsRates[i]);
				
				//预警总数、待处理预警数、已消除预警数、预警处理率
				int totalWarnNum = 0, noProcWarnNum = 0, elimWarnNum = 0;				
				totalWarnNum = totalWarnNums[k][i];
				noProcWarnNum = noProcWarnNums[k][i];
				elimWarnNum = elimWarnNums[k][i];
				sdd.setTotalWarnNum(totalWarnNum);
				sdd.setNoProcWarnNum(noProcWarnNum);
				sdd.setElimWarnNum(elimWarnNum);
				warnProcRates[i] = 0;
				if(totalWarnNum > 0) {
					warnProcRates[i] = 100 * ((float) elimWarnNum / (float) totalWarnNum);
					BigDecimal bd = new BigDecimal(warnProcRates[i]);
					warnProcRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (warnProcRates[i] > 100)
						warnProcRates[i] = 100;
				}
				sdd.setWarnProcRate(warnProcRates[i]);	
				//餐厨垃圾回收合计，按所在地有效、餐厨垃圾学校回收数量、餐厨垃圾团餐公司回收数量，按所在地有效
				float kwSchRcNum = 0, kwRmcRcNum = 0, kwTotalRcNum = 0;				
				kwSchRcNum = kwSchRcNums[k][i];
				kwRmcRcNum = kwRmcRcNums[k][i];
				kwTotalRcNum = kwTotalRcNums[k][i];
				sdd.setTotalKwRecNum((int)kwTotalRcNum);
				sdd.setKwSchRecNum((int)kwSchRcNum);
				sdd.setKwRmcRecNum((int)kwRmcRcNum);	
				//废弃油脂回收合计，按所在地有效、废弃油脂学校回收数量、废弃油脂团餐公司回收数量，按所在地有效
				float woSchRcNum = 0, woRmcRcNum = 0, woTotalRcNum = 0;
				woSchRcNum = woSchRcNums[k][i];
				woRmcRcNum = woRmcRcNums[k][i];
				woTotalRcNum = woTotalRcNums[k][i];
				sdd.setTotalWoRecNum((int)woTotalRcNum);
				sdd.setWoSchRecNum((int)woSchRcNum);
				sdd.setWoRmcRecNum((int)woRmcRcNum);
				sumDataDets.add(sdd);
			}
		}
		//排序
    	SortList<SumDataDets> sortList = new SortList<SumDataDets>();  
    	sortList.Sort(sumDataDets, methods1, sorts1, dataTypes1);
		//时戳
		sddDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<SumDataDets> pageBean = new PageBean<SumDataDets>(sumDataDets, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		sddDto.setPageInfo(pageInfo);
		// 设置数据
		sddDto.setSumDataDets(pageBean.getCurPageData());
		// 消息ID
		sddDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return sddDto;
	}
	
	// 汇总数据详情列表函数
	private SumDataDetsDTO sumDataDets(String distIdorSCName, String[] dates,
			List<TEduDistrictDo> tedList, 
			int schSelMode, int subLevel, int compDep, String subDistName,
			String subLevels,String compDeps,String distNames) {
		SumDataDetsDTO sddDto = new SumDataDetsDTO();
		//筛选学校模式
		if(schSelMode == 0) {    //按主管部门
			sddDto = sumDataDetsByCompDep(distIdorSCName, dates, tedList, subLevel, compDep, subDistName,
					subLevels,compDeps);
		}
		else if(schSelMode == 1) {  //按所在地
			sddDto = sumDataDetsByLocality(distIdorSCName, dates, tedList,distNames);
		}

		return sddDto;
	}
	
	// 汇总数据详情列表模型函数
	public SumDataDetsDTO appModFunc(String token, String startDate, String endDate, String schSelMode, 
			String subLevel, String compDep, String subDistName, String distName, String prefCity, 
			String province, 
			String subLevels,String compDeps,String distNames,
			String page, String pageSize, 
			Db1Service db1Service, Db2Service db2Service) {
		SumDataDetsDTO sddDto = null;
		this.curPageNum = Integer.parseInt(page);
		this.pageSize = Integer.parseInt(pageSize);
		if(isRealData) {       //真实数据
			// 日期
			String[] dates = null;
			if (startDate == null || endDate == null) { // 按照当天日期获取数据
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
			if(province == null)
				province = "上海市";
			//学校筛选方式，0:按主管部门，1:按所在地
			int curSchSelMode = 1;
			if(schSelMode != null)
				curSchSelMode = Integer.parseInt(schSelMode);
			//所属，0:其他，1:部属，2:市属，3: 区属，按主管部门有效
			int curSubLevel = -1;
			if(subLevel != null)
				curSubLevel = Integer.parseInt(subLevel);
			//主管部门，按主管部门有效
			int curCompDep = -1;
			if(compDep != null)
				curCompDep = Integer.parseInt(compDep);			
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
					//获取用户数据权限信息
				  	UserDataPermInfoDTO udpiDto = AppModConfig.getUserDataPermInfo(token, db1Service, db2Service);
				  	if(curSubLevel == -1)
				  		curSubLevel = udpiDto.getSubLevelId();
				  	if(curCompDep == -1)
				  		curCompDep = udpiDto.getCompDepId();
					// 汇总数据详情列表函数
					sddDto = sumDataDets(distIdorSCName, dates, tedList, curSchSelMode, curSubLevel,
							curCompDep, subDistName,
							subLevels,compDeps,distNames);
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
					//获取用户数据权限信息
				  	UserDataPermInfoDTO udpiDto = AppModConfig.getUserDataPermInfo(token, db1Service, db2Service);
				  	if(curSubLevel == -1)
				  		curSubLevel = udpiDto.getSubLevelId();
				  	if(curCompDep == -1)
				  		curCompDep = udpiDto.getCompDepId();
					// 汇总数据详情列表函数
					sddDto = sumDataDets(distIdorSCName, dates, tedList, curSchSelMode, curSubLevel, 
							curCompDep, subDistName,
							subLevels,compDeps,distNames);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}
		}
		else {    //模拟数据
			//模拟数据函数
			sddDto = SimuDataFunc();
		}		

		return sddDto;
	}
}
