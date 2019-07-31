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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpDishList;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpDishListDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.user.UserDataPermInfoDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.CommonUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.SortList;

//项目点排菜列表应用模型
public class PpDishListAppMod {
	private static final Logger logger = LogManager.getLogger(PpDishListAppMod.class.getName());
	
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
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String[] dishDate_Array = {"2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03", "2018-09-03"};
	String[] subLevel_Array = {"区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属", "区属"};
	String[] compDep_Array = {"黄浦区教育局", "嘉定区教育局", "宝山区教育局", "浦东新区教育局", "松江区教育局", "金山区教育局", "青浦区教育局", "奉贤区教育局", "崇明区教育局", "静安区教育局", "徐汇区教育局", "长宁区教育局", "普陀区教育局", "虹口区教育局", "杨浦区教育局", "闵行区教育局"};
	String[] distName_Array = {"黄浦区", "嘉定区", "宝山区", "浦东新区", "松江区", "金山区", "青浦区", "奉贤区", "崇明区", "静安区 ", "徐汇区", "长宁区", "普陀区", "虹口区", "杨浦区", "闵行区"};
	int[] regSchNum_Array = {161, 222, 375, 974, 292, 150, 190, 210, 146, 253, 278, 150, 247, 162, 248, 475};
	int[] mealSchNum_Array = {151, 212, 365, 964, 282, 140, 180, 200, 136, 243, 268, 140, 237, 152, 238, 465};
	int[] dishSchNum_Array = {130, 166, 321, 615, 226, 119, 128, 159, 98, 217, 234, 134, 197, 139, 218, 358};
	int[] noDishSchNum_Array = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};   
	
	//模拟数据函数
	private PpDishListDTO SimuDataFunc() {
		PpDishListDTO pdlDto = new PpDishListDTO();
		// 设置返回数据
		pdlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//列表元素设置
		List<PpDishList> ppDishList = new ArrayList<>();
		//赋值
		for (int i = 0; i < distName_Array.length; i++) {
			PpDishList pdl = new PpDishList();
			pdl.setDishDate(dishDate_Array[i]);
			pdl.setSubLevel(subLevel_Array[i]);
			pdl.setCompDep(compDep_Array[i]);
			pdl.setDistName(distName_Array[i]);
			pdl.setRegSchNum(regSchNum_Array[i]);
			pdl.setMealSchNum(mealSchNum_Array[i]);
			pdl.setDishSchNum(dishSchNum_Array[i]);
			noDishSchNum_Array[i] = mealSchNum_Array[i]-dishSchNum_Array[i];
			pdl.setNoDishSchNum(noDishSchNum_Array[i]);
			float dishRate = 100*((float)(dishSchNum_Array[i])/(float)(mealSchNum_Array[i]));
			BigDecimal bd = new BigDecimal(dishRate); 
			dishRate = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
			if(dishRate > 100)
				dishRate = 100;
			pdl.setDishRate(dishRate);
			ppDishList.add(pdl);
		}
		//设置模拟数据
		pdlDto.setPpDishList(ppDishList);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = distName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		pdlDto.setPageInfo(pageInfo);
		//消息ID
		pdlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return pdlDto;
	}
	
	// 项目点排菜列表函数按主管部门
	private PpDishListDTO ppDishListByCompDep(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList, 
			int subLevel, int compDep, String subDistName,String subLevels,String compDeps) {
		PpDishListDTO pdlDto = new PpDishListDTO();
		List<PpDishList> ppDishList = new ArrayList<>();
		PpDishList pdl = null;
		String key = "", keyVal = "", field = "", fieldPrefix = "";
		// 当天排菜学校总数
		Map<String, String> platoonFeedTotalMap = null;
		int i, j, k, subLevelCount = 4, compDepCount = 0, 
				maxCompDepCount = AppModConfig.compDepIdToNameMap3.size(),
				dateCount = dates.length;
		int[][][] totalMealSchNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				distDishSchNums = new int[dateCount][subLevelCount][maxCompDepCount], 
				distNoDishSchNums = new int[dateCount][subLevelCount][maxCompDepCount];
		float[][] distDishRates = new float[subLevelCount][maxCompDepCount];
		
		List<Object> subLevelList=CommonUtil.changeStringToList(subLevels);
		List<Object> compDepList=CommonUtil.changeStringToList(compDeps);
		
		//各区学校数量
		key = "schoolData";
		Map<String, String> schoolDataMap = null;
		int[][] regSchNums = new int[subLevelCount][maxCompDepCount];
		schoolDataMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
		if(schoolDataMap != null) {
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
					field = fieldPrefix;
					if(schoolDataMap.containsKey(field)) {
						keyVal = schoolDataMap.get(field);
						regSchNums[i][j] = Integer.parseInt(keyVal);
					}
				}
			}
		}
		// 当天各区排菜学校数量
		for (k = 0; k < dates.length; k++) {
			//供餐学校数量
			key = dates[k] + "_platoonfeed-total";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(platoonFeedTotalMap == null) {    //Redis没有该数据则从hdfs系统中获取
				platoonFeedTotalMap = AppModConfig.getHdfsDataKey(dates[k], key);
			}
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
							if (curKey.indexOf(fieldPrefix) == 0) {
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
			// 该日期各区学校排菜率
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
					logger.info("日期：" + dates[k] + "，所属：" + AppModConfig.subLevelIdToNameMap.get(i) + "，主管部门：" + compDepName + "，排菜学校数量：" + distDishSchNums[k][i] 	+ "，供餐学校总数：" + totalMealSchNums[k][i] + "，排菜率：" + distDishRates[i] + "，field = " + field);				
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
					pdl = new PpDishList();
					pdl.setDishDate(dates[k].replaceAll("-", "/"));
					pdl.setSubLevel(String.valueOf(i) + "," + AppModConfig.subLevelIdToNameMap.get(i));
					pdl.setCompDep(compDepId + "," + compDepName);
					int totalDistSchNum = 0, distDishSchNum = 0, distNoDishSchNum = 0;
					totalDistSchNum = totalMealSchNums[k][i][j];
					distDishSchNum = distDishSchNums[k][i][j];
					distNoDishSchNum = distNoDishSchNums[k][i][j];		
					pdl.setRegSchNum(regSchNums[i][j]);
					pdl.setMealSchNum(totalDistSchNum);
					pdl.setDishSchNum(distDishSchNum);
					pdl.setNoDishSchNum(distNoDishSchNum);
					distDishRates[i][j] = 0;
					if(totalDistSchNum > 0) {
						distDishRates[i][j] = 100 * ((float) distDishSchNum / (float) totalDistSchNum);
						BigDecimal bd = new BigDecimal(distDishRates[i][j]);
						distDishRates[i][j] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
						if (distDishRates[i][j] > 100)
							distDishRates[i][j] = 100;
					}
					pdl.setDishRate(distDishRates[i][j]);
					ppDishList.add(pdl);
				}
			}
		}
		//排序
		SortList<PpDishList> sortList = new SortList<PpDishList>();
		sortList.Sort(ppDishList, methods0, sorts0, dataTypes0);
		//时戳
    	pdlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
    	// 分页
    	PageBean<PpDishList> pageBean = new PageBean<PpDishList>(ppDishList, curPageNum, pageSize);
    	PageInfo pageInfo = new PageInfo();
    	pageInfo.setPageTotal(pageBean.getTotalCount());
    	pageInfo.setCurPageNum(curPageNum);
    	pdlDto.setPageInfo(pageInfo);
    	// 设置数据
    	pdlDto.setPpDishList(pageBean.getCurPageData());
    	// 消息ID
    	pdlDto.setMsgId(AppModConfig.msgId);
    	AppModConfig.msgId++;
    	// 消息id小于0判断
    	AppModConfig.msgIdLessThan0Judge();
		
		return pdlDto;
	}
	
	// 项目点排菜列表函数按所在区
	private PpDishListDTO ppDishListByLocality(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList,
			String distNames) {
		PpDishListDTO pdlDto = new PpDishListDTO();
		List<PpDishList> ppDishList = new ArrayList<>();
		PpDishList pdl = null;
		String key = "", keyVal = "", field = "", fieldPrefix = "";
		// 当天排菜学校总数
		Map<String, String> platoonFeedTotalMap = null;
		int distCount = tedList.size(), dateCount = dates.length;
		int[][] totalMealSchNums = new int[dateCount][distCount], 
				distDishSchNums = new int[dateCount][distCount], 
				distNoDishSchNums = new int[dateCount][distCount];
		float[] distDishRates = new float[distCount];
		
		List<Object> distNamesList=CommonUtil.changeStringToList(distNames);
		
		//各区学校数量
		key = "schoolData";
		Map<String, String> schoolDataMap = null;
		int[] regSchNums = new int[distCount];
		schoolDataMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
		if(schoolDataMap != null) {
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
				field = "area_" + curDistId;
				if(schoolDataMap.containsKey(field)) {
					keyVal = schoolDataMap.get(field);
					regSchNums[i] = Integer.parseInt(keyVal);
				}
			}
		}
		// 当天各区排菜学校数量
		for (int k = 0; k < dates.length; k++) {
			//供餐学校数量
			key = dates[k] + "_platoonfeed-total";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(platoonFeedTotalMap == null) {    //Redis没有该数据则从hdfs系统中获取
				platoonFeedTotalMap = AppModConfig.getHdfsDataKey(dates[k], key);
			}
			if(platoonFeedTotalMap != null) {
				for(String curKey : platoonFeedTotalMap.keySet()) {
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
			// 该日期各区学校排菜率
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
						+ "，供餐学校总数：" + totalMealSchNums[k][i] + "，排菜率：" + distDishRates[i] + "，field = "
						+ field);
			}
		}
		for (int i = 0; i < distCount; i++) {
			for (int k = 0; k < dates.length; k++) {
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
				pdl = new PpDishList();
				pdl.setDishDate(dates[k].replaceAll("-", "/"));
				pdl.setDistName(curTdd.getId());
				int totalDistSchNum = 0, distDishSchNum = 0, distNoDishSchNum = 0;
				totalDistSchNum = totalMealSchNums[k][i];
				distDishSchNum = distDishSchNums[k][i];
				distNoDishSchNum = distNoDishSchNums[k][i];		
				pdl.setRegSchNum(regSchNums[i]);
				pdl.setMealSchNum(totalDistSchNum);
				pdl.setDishSchNum(distDishSchNum);
				pdl.setNoDishSchNum(distNoDishSchNum);
				distDishRates[i] = 0;
				if(totalDistSchNum > 0) {
					distDishRates[i] = 100 * ((float) distDishSchNum / (float) totalDistSchNum);
					BigDecimal bd = new BigDecimal(distDishRates[i]);
					distDishRates[i] = bd.setScale(2, BigDecimal.ROUND_HALF_UP).floatValue();
					if (distDishRates[i] > 100)
						distDishRates[i] = 100;
				}
				pdl.setDishRate(distDishRates[i]);
				ppDishList.add(pdl);
			}
		}
		//排序
    	SortList<PpDishList> sortList = new SortList<PpDishList>();  
    	sortList.Sort(ppDishList, methods1, sorts1, dataTypes1);
		//时戳
    	pdlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
    	// 分页
    	PageBean<PpDishList> pageBean = new PageBean<PpDishList>(ppDishList, curPageNum, pageSize);
    	PageInfo pageInfo = new PageInfo();
    	pageInfo.setPageTotal(pageBean.getTotalCount());
    	pageInfo.setCurPageNum(curPageNum);
    	pdlDto.setPageInfo(pageInfo);
    	// 设置数据
    	pdlDto.setPpDishList(pageBean.getCurPageData());
    	// 消息ID
    	pdlDto.setMsgId(AppModConfig.msgId);
    	AppModConfig.msgId++;
    	// 消息id小于0判断
    	AppModConfig.msgIdLessThan0Judge();

		return pdlDto;
	}
	
	// 项目点排菜列表函数
	private PpDishListDTO ppDishList(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList, 
			int schSelMode, int subLevel, int compDep, String subDistName,
			String subLevels,String compDeps,String distNames) {
		PpDishListDTO pdlDto = new PpDishListDTO();
		//筛选学校模式
		if(schSelMode == 0) {    //按主管部门
			pdlDto = ppDishListByCompDep(distIdorSCName, dates, tedList, subLevel, compDep, subDistName,subLevels,compDeps);
		}
		else if(schSelMode == 1) {  //按所在地
			pdlDto = ppDishListByLocality(distIdorSCName, dates, tedList,distNames);			
		}    	

		return pdlDto;
	}

	//项目点排菜列表模型函数
	public PpDishListDTO appModFunc(String token, String startDate, String endDate, String schSelMode, 
			String subLevel, String compDep, String subDistName, String distName, 
			String prefCity, String province, 
			String subLevels,String compDeps,String distNames,
			String page, String pageSize, 
			Db1Service db1Service, Db2Service db2Service) {
		PpDishListDTO pdlDto = null;
		this.curPageNum = Integer.parseInt(page);
		this.pageSize = Integer.parseInt(pageSize);
		if (isRealData) { // 真实数据
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
					// 项目点排菜列表函数
					pdlDto = ppDishList(distIdorSCName, dates, tedList, curSchSelMode, curSubLevel, 
							curCompDep, subDistName,subLevels,compDeps,distNames);
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
					// 项目点排菜列表函数
					pdlDto = ppDishList(distIdorSCName, dates, tedList, curSchSelMode, curSubLevel, curCompDep, 
							subDistName,subLevels,compDeps,distNames);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}
		} else { // 模拟数据
			//模拟数据函数
			pdlDto = SimuDataFunc();
		}

		return pdlDto;
	}
}
