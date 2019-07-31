package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.iw;

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
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProSupplierDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageBean;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnAllLicDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnAllLicDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//证照预警全部证件详情列表应用模型
public class WarnAllLicDetsAppMod {
	private static final Logger logger = LogManager.getLogger(WarnAllLicDetsAppMod.class.getName());
	
	//Redis服务
	@Autowired
	RedisService redisService = new RedisService();
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	//数组数据初始化
	String[] warnDate_Array = {"2018/09/03", "2018/09/03"};
	String[] distName_Array = {"11", "11"};
	String[] schName_Array = {"上海市徐汇区徐浦小学", "上海市徐汇区东兰幼儿园"};
	String[] trigWarnUnit_Array = {"上海市徐汇区徐浦小学", "上海市徐汇区东兰幼儿园"};
	String[] licName_Array = {"食品经营许可证", "食品经营许可证"};
	String[] licNo_Array = {"JY23101140041987", "JY13101050042467"};
	String[] validDate_Array = {"2018-12-23", "2018-06-03"};
	String[] licStatus_Array = {"剩余33天", "剩余33天"};
	int[] licAuditStatus_Array = {2, 2};
	String[] elimDate_Array = {"2018/09/03", "2018/09/03"};
	
	//模拟数据函数
	private WarnAllLicDetsDTO SimuDataFunc() {
		WarnAllLicDetsDTO waldDto = new WarnAllLicDetsDTO();
		//时戳
		waldDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//证照预警全部证件详情列表模拟数据
		List<WarnAllLicDets> warnRmcLicDets = new ArrayList<>();
		//赋值
		for (int i = 0; i < warnDate_Array.length; i++) {
			WarnAllLicDets wald = new WarnAllLicDets();
			wald.setWarnDate(warnDate_Array[i]);
			wald.setDistName(distName_Array[i]);
			wald.setSchName(schName_Array[i]);
			wald.setTrigWarnUnit(trigWarnUnit_Array[i]);
			wald.setLicName(licName_Array[i]);
			wald.setLicNo(licNo_Array[i]);
			wald.setValidDate(validDate_Array[i]);
			wald.setLicStatus(licStatus_Array[i]);
			wald.setLicAuditStatus(licAuditStatus_Array[i]);
			wald.setElimDate(elimDate_Array[i]);
			warnRmcLicDets.add(wald);
		}
		//设置数据
		waldDto.setWarnAllLicDets(warnRmcLicDets);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = distName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		waldDto.setPageInfo(pageInfo);
		//消息ID
		waldDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return waldDto;
	}
	
	// 证照预警全部证件详情列表函数
	WarnAllLicDetsDTO warnAllLicDets(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList, Db1Service db1Service, SaasService saasService, String schName, String trigWarnUnit, int licType, int licStatus, int licAuditStatus, String startElimDate, String endElimDate, String startValidDate, String endValidDate, String licNo) {
		WarnAllLicDetsDTO waldDto = new WarnAllLicDetsDTO();
		List<WarnAllLicDets> warnAllLicDets = new ArrayList<>();
		Map<String, String> warnDetailMap = new HashMap<>();
		int i, k, dateCount = dates.length;
		String key = null, keyVal = null;
		//供应商id和名称
		Map<String, String> supIdToNameMap = new HashMap<>();
    	List<TProSupplierDo> tpsDoList = saasService.getIdSupplierIdName();
    	if(tpsDoList != null) {
    		for(i = 0; i < tpsDoList.size(); i++) {
    			supIdToNameMap.put(tpsDoList.get(i).getId(), tpsDoList.get(i).getSupplierName());
    		}
    	}
    	// 时间段内各区学校餐厨垃圾详情
		for(k = 0; k < dateCount; k++) {
			key = dates[k] + "_warnDetail";
			warnDetailMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if (warnDetailMap != null) {
				for (String curKey : warnDetailMap.keySet()) {
					keyVal = warnDetailMap.get(curKey);
					// 证照预警全部证件详情列表
					String[] keyVals = keyVal.split("_");
					if(keyVals.length >= 16) {
						WarnAllLicDets wald = new WarnAllLicDets();
						//预警日期
						wald.setWarnDate(dates[k].replaceAll("-", "/"));
						//区
						i = AppModConfig.getVarValIndex(keyVals, "area");
						wald.setDistName("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wald.setDistName(keyVals[i]);
						}							
						//学校名称
						i = AppModConfig.getVarValIndex(keyVals, "schoolname");
						wald.setSchName("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wald.setSchName(keyVals[i]);
						}
						//触发预警单位
						i = AppModConfig.getVarValIndex(keyVals, "supplierid");
						wald.setTrigWarnUnit("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null")) {
								if(supIdToNameMap.containsKey(keyVals[i])) {
									wald.setTrigWarnUnit(supIdToNameMap.get(keyVals[i]));
								}
							}
						}
						//证件名称
						i = AppModConfig.getVarValIndex(keyVals, "warntypechild");
						wald.setLicName("-");
						if(i != -1) {
							int curLicType = -1;
							if(!keyVals[i].equalsIgnoreCase("null")) {
								if(keyVals[i].equalsIgnoreCase("0"))        //餐饮服务许可证
									curLicType = 3;
								else if(keyVals[i].equalsIgnoreCase("1"))   //食品经营许可证
									curLicType = 0;
								else if(keyVals[i].equalsIgnoreCase("20"))  //健康证
									curLicType = 2;
								else if(keyVals[i].equalsIgnoreCase("22"))  //A1
									curLicType = 4;
								else if(keyVals[i].equalsIgnoreCase("23"))  //B
									curLicType = 6;
								else if(keyVals[i].equalsIgnoreCase("24"))  //C
									curLicType = 7;
								else if(keyVals[i].equalsIgnoreCase("25"))  //A2
									curLicType = 5;
								if(curLicType != -1)
									wald.setLicName(AppModConfig.licTypeIdToNameMap.get(curLicType));
							}
						}
						//证件号码
						i = AppModConfig.getVarValIndex(keyVals, "licno");
						wald.setLicNo("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wald.setLicNo(keyVals[i]);
						}
						//有效日期
						i = AppModConfig.getVarValIndex(keyVals, "losetime");
						wald.setValidDate("-");
						String validDate = null;
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null")) {
								int idx = keyVals[i].indexOf(" ");
								if(idx != -1) {
									validDate = keyVals[i].substring(0, idx);
									wald.setValidDate(validDate);
								}
							}
						}
						//证件状况
						int curLicStatus = 0;
						i = AppModConfig.getVarValIndex(keyVals, "remaintime");
						wald.setLicStatus("-");
						if(i != -1 && validDate != null) {
							String curDate = BCDTimeUtil.convertNormalDate(null);						
							DateTime startDt = BCDTimeUtil.convertDateStrToDate(validDate);
							DateTime endDt = BCDTimeUtil.convertDateStrToDate(curDate);
							if(curDate.compareTo(validDate) > 0) {
								wald.setLicStatus("逾期");
								curLicStatus = 0;
							}
							else {
								int days = Math.abs(Days.daysBetween(startDt, endDt).getDays());
								wald.setLicStatus("剩余 " + days + " 天");
								curLicStatus = 1;
							}
						}
						//状态
						i = AppModConfig.getVarValIndex(keyVals, "status");
						wald.setLicAuditStatus(0);
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null")) {
								if(keyVals[i].equalsIgnoreCase("1"))
									wald.setLicAuditStatus(0);
								else if(keyVals[i].equalsIgnoreCase("2"))
									wald.setLicAuditStatus(1);
								else if(keyVals[i].equalsIgnoreCase("3"))
									wald.setLicAuditStatus(3);
								else if(keyVals[i].equalsIgnoreCase("4"))
									wald.setLicAuditStatus(2);
							}
						}
						//消除日期
						i = AppModConfig.getVarValIndex(keyVals, "dealtime");
						wald.setElimDate("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wald.setElimDate(keyVals[i]);
						}
						//条件判断
						boolean isAdd = true;
						int[] flIdxs = new int[9];
						//判断区域（判断索引0）
						if(distIdorSCName != null) {
							String curDistName = wald.getDistName();
							if(!curDistName.equalsIgnoreCase(distIdorSCName))
								flIdxs[0] = -1;
						}
						//判断学校名称（判断索引1）
						if(schName != null) {
							i = AppModConfig.getVarValIndex(keyVals, "schoolid");
							if(i != -1) {
								if(!keyVals[i].equalsIgnoreCase(schName))
									flIdxs[1] = -1;
							}
						}
						//判断触发预警单位（判断索引2）
						if(trigWarnUnit != null) {
							if(supIdToNameMap.containsKey(trigWarnUnit)) {
								if(!wald.getTrigWarnUnit().equalsIgnoreCase(supIdToNameMap.get(trigWarnUnit)))
									flIdxs[2] = -1;
							}
						}
						//判断证件名称（名称）（判断索引3）
						if(licType != -1) {
							if(AppModConfig.licTypeNameToIdMap.containsKey(wald.getLicName())) {
								int curLicType = AppModConfig.licTypeNameToIdMap.get(wald.getLicName());
								if(licType != curLicType)
									flIdxs[3] = -1;
							}
						}
						//判断预警状态（判断索引4）
						if(licAuditStatus != -1) {
							if(licAuditStatus != wald.getLicAuditStatus())
								flIdxs[4] = -1;
						}
						//判断消除日期（判断索引5）
						if(startElimDate != null && endElimDate != null) {
							if(!wald.getElimDate().equalsIgnoreCase("-")) {
								if(wald.getElimDate().compareTo(startElimDate) < 0 || wald.getElimDate().compareTo(endElimDate) > 0)
									flIdxs[5] = -1;
							}
							else
								flIdxs[5] = -1;
						}
						//判断有效日期（判断索引6）
						if(startValidDate != null && endValidDate != null) {
							if(!wald.getValidDate().equalsIgnoreCase("-")) {
								if(wald.getValidDate().compareTo(startValidDate) < 0 || wald.getValidDate().compareTo(endValidDate) > 0)
									flIdxs[6] = -1;
							}
							else
								flIdxs[6] = -1;
						}
						//判断证件号码（判断索引7）
						if(licNo != null) {
							if(wald.getLicNo().indexOf(licNo) == -1)
								flIdxs[7] = -1;
						}
						//判断证件状况（判断索引8）
						if(licStatus != -1) {
							if(curLicStatus != licStatus)
								flIdxs[8] = -1;
						}
						//总体条件判断
						for(i = 0; i < flIdxs.length; i++) {
							if(flIdxs[i] == -1) {
								isAdd = false;
								break;
							}
						}
						//是否满足条件
						if(isAdd)
							warnAllLicDets.add(wald);
					}
					else
						logger.info("菜品供应明细："+ curKey + "，格式错误！");
				}
			}
		}
		//时戳
		waldDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<WarnAllLicDets> pageBean = new PageBean<WarnAllLicDets>(warnAllLicDets, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		waldDto.setPageInfo(pageInfo);
		//设置数据
		waldDto.setWarnAllLicDets(pageBean.getCurPageData());
		//消息ID
		waldDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return waldDto;
	}	
	
	// 证照预警全部证件详情列表模型函数
	public WarnAllLicDetsDTO appModFunc(String token, String startWarnDate, String endWarnDate, String distName, String prefCity, String province, String schName, String trigWarnUnit, String licType, String licStatus, String licAuditStatus, String startElimDate, String endElimDate, String startValidDate, String endValidDate, String licNo, String page, String pageSize, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		WarnAllLicDetsDTO waldDto = null;
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
					logger.info("dates[" + i + "] = " + dates[i]);
				}
			}
			// 省或直辖市
			if(province == null)
				province = "上海市";  		
			//证件类型，0:食品经营许可证，1:营业执照，2:健康证，3:餐饮服务许可证，4:A1证，5:A2证，6:B证，7:C证
			int curLicType = -1;
			if(licType != null)
				curLicType = Integer.parseInt(licType);
			//证件状况，0:逾期，1:到期
			int curLicStatus = -1;
			if(licStatus != null)
				curLicStatus = Integer.parseInt(licStatus);
			//审核状态，0:未处理，1:审核中，2:已消除，3:已驳回
			int curLicAuditStatus = -1;
			if(licAuditStatus != null)
				curLicAuditStatus = Integer.parseInt(licAuditStatus);			
			// 参数查找标识
			boolean bfind = false;
			String distIdorSCName = null;
			// 按不同参数形式处理
			if (distName != null && prefCity == null && province != null) {    // 按区域，省或直辖市处理
				List<TEduDistrictDo> tedList = db1Service.getListByDs1IdName();
				// 查找是否存在该区域和省市
				for (int i = 0; i < tedList.size(); i++) {
					TEduDistrictDo curTdd = tedList.get(i);
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
					// 证照预警全部证件详情列表函数
					waldDto = warnAllLicDets(distIdorSCName, dates, tedList, db1Service, saasService, schName, trigWarnUnit, curLicType, curLicStatus, curLicAuditStatus, startElimDate, endElimDate, startValidDate, endValidDate, licNo);		
				}
			} else if (distName == null && prefCity == null && province != null) { // 按省或直辖市处理
				List<TEduDistrictDo> tedList = null;
				if (province.compareTo("上海市") == 0) {
					bfind = true;
					tedList = db1Service.getListByDs1IdName();
					distIdorSCName = null;
				}
				if (bfind) {
					if(distIdorSCName == null)
						distIdorSCName = AppModConfig.getUserDataPermDistId(token, db1Service, db2Service);  //获取用户权限区域ID
					// 证照预警全部证件详情列表函数
					waldDto = warnAllLicDets(distIdorSCName, dates, tedList, db1Service, saasService, schName, trigWarnUnit, curLicType, curLicStatus, curLicAuditStatus, startElimDate, endElimDate, startValidDate, endValidDate, licNo);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}															
		}
		else {    //模拟数据
			//模拟数据函数
			waldDto = SimuDataFunc();
		}		

		return waldDto;
	}
}
