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
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnStaffLicDets;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw.WarnStaffLicDetsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//证照预警人员证件详情列表应用模型
public class WarnStaffLicDetsAppMod {
	private static final Logger logger = LogManager.getLogger(WarnStaffLicDetsAppMod.class.getName());
	
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
    String[] rmcName_Array = {"上海绿捷实业发展有限公司", "上海龙神餐饮有限公司"};
    String[] relSchName_Array = {"上海市徐汇中学", "上海市康健外国语实验中学"};
    String[] licName_Array = {"健康证", "健康证"};
    String[] fullName_Array = {"管雪芹", "魏士俊"};
    String[] licNo_Array = {"107217081610320285320844359", "461817071010522015341958079"};
    String[] validDate_Array = {"2018-12-23", "2018-06-03"};
    String[] licStatus_Array = {"剩余 1 天", "逾期"};
    int[] licAuditStatus_Array = {2, 2};
    String[] elimDate_Array = {"2018/09/03", "2018/09/03"};
	
	//模拟数据函数
	private WarnStaffLicDetsDTO SimuDataFunc() {
		WarnStaffLicDetsDTO wstldDto = new WarnStaffLicDetsDTO();
		//时戳
		wstldDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//证照预警人员证件详情列表模拟数据
		List<WarnStaffLicDets> warnStaffLicDets = new ArrayList<>();
		//赋值
		for(int i = 0; i < warnDate_Array.length; i++) {
			WarnStaffLicDets wstld = new WarnStaffLicDets();
			wstld.setWarnDate(warnDate_Array[i]);
			wstld.setDistName(distName_Array[i]);
			wstld.setRmcName(rmcName_Array[i]);
			wstld.setRelSchName(relSchName_Array[i]);
			wstld.setLicName(licName_Array[i]);
			wstld.setFullName(fullName_Array[i]);
			wstld.setLicNo(licNo_Array[i]);
			wstld.setValidDate(validDate_Array[i]);
			wstld.setLicStatus(licStatus_Array[i]);
			wstld.setLicAuditStatus(licAuditStatus_Array[i]);
			wstld.setElimDate(elimDate_Array[i]);
			warnStaffLicDets.add(wstld);
		}
		//设置数据
		wstldDto.setWarnStaffLicDets(warnStaffLicDets);
		//分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = distName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		wstldDto.setPageInfo(pageInfo);
		//消息ID
		wstldDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return wstldDto;
	}
	
	// 证照预警人员证件详情列表函数
	WarnStaffLicDetsDTO warnStaffLicDets(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList, Db1Service db1Service, SaasService saasService, String rmcName, int licType, int licStatus, int licAuditStatus, String fullName, String startElimDate, String endElimDate, String startValidDate, String endValidDate, String licNo, String schName) {
		WarnStaffLicDetsDTO wstldDto = new WarnStaffLicDetsDTO();
		List<WarnStaffLicDets> warnStaffLicDets = new ArrayList<>();
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
					// 证照预警人员证件详情列表
					String[] keyVals = keyVal.split("_");
					if(keyVals.length >= 16 && curKey.indexOf("people_") != -1) {
						WarnStaffLicDets wstld = new WarnStaffLicDets();			
						//预警日期
						wstld.setWarnDate(dates[k].replaceAll("-", "/"));
						//区
						i = AppModConfig.getVarValIndex(keyVals, "area");
						wstld.setDistName("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wstld.setDistName(keyVals[i]);
						}			
						//团餐公司名称
						i = AppModConfig.getVarValIndex(keyVals, "supplierid");
						wstld.setRmcName("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null")) {
								if(supIdToNameMap.containsKey(keyVals[i])) {
									wstld.setRmcName(supIdToNameMap.get(keyVals[i]));
								}
							}
						}
						//关联学校
						i = AppModConfig.getVarValIndex(keyVals, "schoolname");
						wstld.setRelSchName("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wstld.setRelSchName(keyVals[i]);
						}	
						//证件名称
						i = AppModConfig.getVarValIndex(keyVals, "warntypechild");
						wstld.setLicName("-");
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
									wstld.setLicName(AppModConfig.licTypeIdToNameMap.get(curLicType));
							}
						}
						//姓名
						i = AppModConfig.getVarValIndex(keyVals, "writtenname");
						wstld.setFullName("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wstld.setFullName(keyVals[i]);
						}
						//证件号码
						i = AppModConfig.getVarValIndex(keyVals, "licno");
						wstld.setLicNo("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wstld.setLicNo(keyVals[i]);
						}
						//有效日期
						i = AppModConfig.getVarValIndex(keyVals, "losetime");
						wstld.setValidDate("-");
						String validDate = null;
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null")) {
								int idx = keyVals[i].indexOf(" ");
								if(idx != -1) {
									validDate = keyVals[i].substring(0, idx);
									wstld.setValidDate(validDate);
								}
							}
						}
						//证件状况
						int curLicStatus = 0;
						i = AppModConfig.getVarValIndex(keyVals, "remaintime");
						wstld.setLicStatus("-");
						if(i != -1 && validDate != null) {
							String curDate = BCDTimeUtil.convertNormalDate(null);						
							DateTime startDt = BCDTimeUtil.convertDateStrToDate(validDate);
							DateTime endDt = BCDTimeUtil.convertDateStrToDate(curDate);
							if(curDate.compareTo(validDate) > 0) {
								wstld.setLicStatus("逾期");
								curLicStatus = 0;
							}
							else {
								int days = Math.abs(Days.daysBetween(startDt, endDt).getDays());
								wstld.setLicStatus("剩余 " + days + " 天");
								curLicStatus = 1;
							}
						}
						//状态
						i = AppModConfig.getVarValIndex(keyVals, "status");
						wstld.setLicAuditStatus(0);
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null")) {
								if(keyVals[i].equalsIgnoreCase("1"))
									wstld.setLicAuditStatus(0);
								else if(keyVals[i].equalsIgnoreCase("2"))
									wstld.setLicAuditStatus(1);
								else if(keyVals[i].equalsIgnoreCase("3"))
									wstld.setLicAuditStatus(3);
								else if(keyVals[i].equalsIgnoreCase("4"))
									wstld.setLicAuditStatus(2);
							}
						}
						//消除日期
						i = AppModConfig.getVarValIndex(keyVals, "dealtime");
						wstld.setElimDate("-");
						if(i != -1) {
							if(!keyVals[i].equalsIgnoreCase("null"))
								wstld.setElimDate(keyVals[i]);
						}
						//条件判断
						boolean isAdd = true;
						int[] flIdxs = new int[10];
						//判断区域（判断索引0）
						if(distIdorSCName != null) {
							String curDistName = wstld.getDistName();
							if(!curDistName.equalsIgnoreCase(distIdorSCName))
								flIdxs[0] = -1;
						}
						//判断团餐公司名称（判断索引1）
						if(rmcName != null) {
							if(supIdToNameMap.containsKey(rmcName)) {
								String curRmcName = supIdToNameMap.get(rmcName);
								if(!curRmcName.equalsIgnoreCase(wstld.getRmcName()))
									flIdxs[1] = -1;
							}
						}
						//判断证件名称（类型）（判断索引2）
						if(licType != -1) {
							if(AppModConfig.licTypeNameToIdMap.containsKey(wstld.getLicName())) {
								int curLicType = AppModConfig.licTypeNameToIdMap.get(wstld.getLicName());
								if(licType != curLicType)
									flIdxs[2] = -1;
							}
						}
						//判断预警状态（判断索引3）
						if(licAuditStatus != -1) {
							if(licAuditStatus != wstld.getLicAuditStatus())
								flIdxs[3] = -1;
						}
						//判断姓名（判断索引4）
						if(fullName != null) {
							if(wstld.getFullName().indexOf(fullName) == -1)
								flIdxs[4] = -1;
						}
						//判断消除日期（判断索引5）
						if(startElimDate != null && endElimDate != null) {
							if(!wstld.getElimDate().equalsIgnoreCase("-")) {
								if(wstld.getElimDate().compareTo(startElimDate) < 0 || wstld.getElimDate().compareTo(endElimDate) > 0)
									flIdxs[5] = -1;
							}
							else
								flIdxs[5] = -1;
						}
						//判断有效日期（判断索引6）
						if(startValidDate != null && endValidDate != null) {
							if(!wstld.getValidDate().equalsIgnoreCase("-")) {
								if(wstld.getValidDate().compareTo(startValidDate) < 0 || wstld.getValidDate().compareTo(endValidDate) > 0)
									flIdxs[6] = -1;
							}
							else
								flIdxs[6] = -1;
						}
						//判断证件号码（判断索引7）
						if(licNo != null) {
							if(wstld.getLicNo().indexOf(licNo) == -1)
								flIdxs[7] = -1;
						}
						//判断学校名称（判断索引8）
						if(schName != null) {
							i = AppModConfig.getVarValIndex(keyVals, "schoolid");
							if(i != -1) {
								if(!keyVals[i].equalsIgnoreCase(schName))
									flIdxs[8] = -1;
							}
						}
						//判断证件状况（判断索引9）
						if(licStatus != -1) {
							if(curLicStatus != licStatus)
								flIdxs[9] = -1;
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
							warnStaffLicDets.add(wstld);
					}
					else
						logger.info("证照预警人员证件详情："+ curKey + "，格式错误！");
				}
			}
		}
		//时戳
		wstldDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<WarnStaffLicDets> pageBean = new PageBean<WarnStaffLicDets>(warnStaffLicDets, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		wstldDto.setPageInfo(pageInfo);
		//设置数据
		wstldDto.setWarnStaffLicDets(pageBean.getCurPageData());
		//消息ID
		wstldDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return wstldDto;
	}	
	
	// 证照预警人员证件详情列表模型函数
	public WarnStaffLicDetsDTO appModFunc(String token, String startWarnDate, String endWarnDate, String distName, String prefCity, String province, String rmcName, String licType, String licStatus, String licAuditStatus, String fullName, String startElimDate, String endElimDate, String startValidDate, String endValidDate, String licNo, String schName, String page, String pageSize, Db1Service db1Service, Db2Service db2Service, SaasService saasService) {
		WarnStaffLicDetsDTO wstldDto = null;
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
					// 证照预警人员证件详情列表函数
					wstldDto = warnStaffLicDets(distIdorSCName, dates, tedList, db1Service, saasService, rmcName, curLicType, curLicStatus, curLicAuditStatus, fullName, startElimDate, endElimDate, startValidDate, endValidDate, licNo, schName);		
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
					// 证照预警人员证件详情列表函数
					wstldDto = warnStaffLicDets(distIdorSCName, dates, tedList, db1Service, saasService, rmcName, curLicType, curLicStatus, curLicAuditStatus, fullName, startElimDate, endElimDate, startValidDate, endValidDate, licNo, schName);
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}													
		}
		else {    //模拟数据
			//模拟数据函数
			wstldDto = SimuDataFunc();
		}		

		return wstldDto;
	}
}
