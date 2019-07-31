package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.im;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSONObject;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSchoolDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageBean;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpGsPlanOpts;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.im.PpGsPlanOptsDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.EduSchoolService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.CommonUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.SortList;

//菜品留样列表应用模型
public class PpGsPlanOptsAppMod {
	private static final Logger logger = LogManager.getLogger(PpGsPlanOptsAppMod.class.getName());
	
	//Redis服务
	@Autowired
	RedisService redisService = new RedisService();
	
	/*//二级排序条件
	final String[] methods0 = {"getSubLevel", "getCompDep"};
	final String[] sorts0 = {"desc", "desc"};
	final String[] dataTypes0 = {"String", "String"};
	
	final String[] methods1 = {"getDistName", "getDishDate"};
	final String[] sorts1 = {"asc", "asc"};
	final String[] dataTypes1 = {"String", "String"};*/
	
	//是否为真实数据标识
	private static boolean isRealData = true;
	
	//三级排序条件
	final String[] methods = {"getDistName", "getSchType"};
	final String[] sorts = {"asc", "asc"};
	final String[] dataTypes = {"String", "String"};
	
	/**
	 * 方法类型索引
	 */
	int methodIndex = 1;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = 20;
	
	
	//模拟数据函数
	private PpGsPlanOptsDTO exampleDataFunc() {
		String data = "{\n" + 
				"   \"time\": \"2016-07-14 09:51:35\",\n" + 
				"   \"pageInfo\":\n" + 
				"   {\n" + 
				"     \"pageTotal\":2,\n" + 
				"     \"curPageNum\":1\n" + 
				"   },\n" + 
				"   \"ppGsPlanOpts\": [\n" + 
				"{\n" + 
				"  \"distrDate\":\"2018/09/04\",\n" + 
				"\"distName\":\"徐汇区\",\n" + 
				"\"schType\":\"小学\",\n" + 
				"\"ppName\":\"上海市徐汇区向阳小学\",\n" + 
				"\"detailAddr\":\"\",\n" + 
				"\"projContact\":\"\",\n" + 
				"\"pcMobilePhone\":\"\",\n" + 
				"\"distrPlanNum\":2,\n" + 
				"\"acceptStatus\":0,\n" + 
				"\"acceptPlanNum\":1,\n" + 
				"\"noAcceptPlanNum\":1,\n" + 
				"\"assignStatus\":1,\n" + 
				"\"assignPlanNum\":1,\n" + 
				"\"noAssignPlanNum\":1,\n" + 
				"\"dispStatus\":1,\n" + 
				"\"dispPlanNum\":2,\n" + 
				"\"noDispPlanNum\":0\n" + 
				"},\n" + 
				"{\n" + 
				"  \"distrDate\":\"2018/09/04\",\n" + 
				"\"distName\":\"徐汇区\",\n" + 
				"\"schType\":\"小学\",\n" + 
				"\"ppName\":\"上海市徐汇区世界小学\",\n" + 
				"\"detailAddr\":\"\",\n" + 
				"\"projContact\":\"\",\n" + 
				"\"pcMobilePhone\":\"\",\n" + 
				"\"distrPlanNum\":2,\n" + 
				"\"acceptStatus\":0,\n" + 
				"\"acceptPlanNum\":1,\n" + 
				"\"noAcceptPlanNum\":1,\n" + 
				"\"assignStatus\":1,\n" + 
				"\"assignPlanNum\":1,\n" + 
				"\"noAssignPlanNum\":1,\n" + 
				"\"dispStatus\":1,\n" + 
				"\"dispPlanNum\":2,\n" + 
				"\"noDispPlanNum\":0\n" + 
				" } ],\n" + 
				"   \"msgId\":1\n" + 
				"}\n" + 
				"";
		
		
		PpGsPlanOptsDTO drsDto = new PpGsPlanOptsDTO();
		JSONObject jsStr = JSONObject.parseObject(data); 
		drsDto = (PpGsPlanOptsDTO) JSONObject.toJavaObject(jsStr,PpGsPlanOptsDTO.class);
		
		
		//消息ID
		drsDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return drsDto;
	}
	
	// 配货列表函数
	private PpGsPlanOptsDTO dishRetSamplesOne(String distIdorSCName, String[] dates, List<TEduDistrictDo> tedList,
			String ppName,Integer acceptStatus,Integer assignStatus,
			Integer dispStatus,Integer schType,String distNames,String schTypes,
			Db1Service db1Service) {
		PpGsPlanOptsDTO drsDto = new PpGsPlanOptsDTO();
		List<PpGsPlanOpts> dishRetSamples = new ArrayList<>();
		String key = "";
		
		List<Object> distNamesList=CommonUtil.changeStringToList(distNames);
		List<Object> schTypesList=CommonUtil.changeStringToList(schTypes);
		
		//获取学校
		List<TEduSchoolDo> schoolList = new ArrayList<TEduSchoolDo>();
		if(distIdorSCName!=null && !"".equals(distIdorSCName)) {
			schoolList = db1Service.getTEduSchoolDoListByDs1(distIdorSCName, 5);
		}else {
			schoolList = db1Service.getTEduSchoolDoListByDs1(distNamesList);
		}
		
		Map<String,TEduSchoolDo> schoolMap = schoolList.stream().collect(Collectors.toMap(TEduSchoolDo::getId,(b)->b));
		
		// 当天排菜学校总数
		Map<String, String>  platoonFeedTotalMap = null;
		int  k;
		// 当天各区菜品留样数量
		for (k = 0; k < dates.length; k++) {
			//供餐学校数量
			key = dates[k] + "_DistributionTotal_child";
			platoonFeedTotalMap = redisService.getHashByKey(SpringConfig.RedisConnPool.REDISCLUSTER1.value, SpringConfig.RedisDBIdx, key);
			if(platoonFeedTotalMap == null) {    //Redis没有该数据则从hdfs系统中获取
				//platoonFeedTotalMap = AppModConfig.getHdfsDataKey(dates[k], key);
			}
			if(platoonFeedTotalMap != null) {
				for(Map.Entry<String, String> entry : platoonFeedTotalMap.entrySet()) {
					String keys[] =entry.getKey().split("_");
					String values[] =entry.getValue().split("_");
					
					//过滤区
					if(distIdorSCName != null) {
						if(keys[1].compareTo(distIdorSCName) != 0) {
							continue ;
						}
					}else if(distNamesList!=null && distNamesList.size() >0) {
						if(StringUtils.isEmpty(keys[1]) || !StringUtils.isNumeric(keys[1])) {
							continue;
						}
						if(!distNamesList.contains(keys[1])) {
							continue ;
						}
					}
					
					//验收状态，0:待验收，1:已验收
					if(acceptStatus != null) {
						if(acceptStatus.equals(0) && (values[9].equals("3") || values[9].equals("4"))) {
							continue;
						}else if(acceptStatus.equals(1) && !values[9].equals("3")) {
							continue;
						}
					}
					//指派状态，0:未指派，1：已指派，2:已取消
					if(assignStatus != null) {
						if(assignStatus.equals(0) && !values[9].equals("-1") && !values[9].equals("-2")) {
							continue;
						}else if(assignStatus.equals(1) && !values[9].equals("0") && !values[9].equals("1") 
								&& !values[9].equals("2") && !values[9].equals("3")) {
							continue;
						}
						//已取消数据不统计（李左明确认）
						/*else if(assignStatus.equals(2) && !values[9].equals("4")) {
							continue;
						}*/
					}
					
					//过滤配送状态，0:未派送，1: 已配送 
					if(dispStatus != null) {
						if(dispStatus.equals(0) && !values[9].equals("-2") && !values[9].equals("-1") && 
								!values[9].equals("0")&& !values[9].equals("1")) {
							continue;
						}else if(dispStatus.equals(1) && !values[9].equals("2")&& !values[9].equals("3")) {
							continue;
						}
					}
					
					String schoolId = keys[3];
					TEduSchoolDo eduSchool = schoolMap.get(schoolId);
					if(eduSchool==null) {
						continue;
					}
					
					//过滤学校学制学校类型（学制），0:托儿所，1:托幼园，2:托幼小，3:幼儿园，4:幼小，5:幼小初，6:幼小初高，7:小学，8:初级中学，9:高级中学，10:完全中学，11:九年一贯制学校，
					//12:十二年一贯制学校，13:职业初中，14:中等职业学校，15:工读学校，16:特殊教育学校，17:其他
					Integer schTypeId = AppModConfig.getSchTypeId(eduSchool.getLevel(), eduSchool.getLevel2());
					if(schType != null && schType != -1) {
						if(schTypeId==null || 
								!schTypeId.equals(schType)) {
							continue ;
						}
					}else if(schTypesList!=null && schTypesList.size() >0) {
						if(schTypeId==null || !schTypesList.contains(schTypeId.toString())) {
							continue;
						}
					}
					
					//过滤项目点
					if(ppName != null) {
						if(!eduSchool.getSchoolName().contains(ppName)) {
							continue ;
						}
					}
					
					
					PpGsPlanOpts drs = new PpGsPlanOpts();
					//配货日期，格式：xxxx/xx/xx
					drs.setDistrDate(dates[k].replaceAll("-", "/"));
					//区域名称
					drs.setDistName(AppModConfig.distIdToNameMap.get(keys[1]));
					//学校类型（学制）
					drs.setSchType(AppModConfig.getSchType(eduSchool.getLevel(), eduSchool.getLevel2()));
					
					//项目点名称
					drs.setPpName(eduSchool.getSchoolName());
					//详细地址
					drs.setDetailAddr(eduSchool.getAddress()==null?"":eduSchool.getAddress());
					//项目联系人
					drs.setProjContact(eduSchool.getDepartmentHead()==null?"":eduSchool.getDepartmentHead());
					//手机
					drs.setPcMobilePhone(eduSchool.getDepartmentMobilephone()==null?"":eduSchool.getDepartmentMobilephone());
					
					//配货计划数量
					drs.setDistrPlanNum(0);
					if(StringUtils.isNumeric(values[1])) {
						drs.setDistrPlanNum(values[1]==null?0:Integer.parseInt(values[1]));
					}
					
					//验收状态，0:待验收，1:已验收
					drs.setAcceptStatus(values[9].equals("3")?1:0);
					//已验收数量
					if(StringUtils.isNumeric(values[3])) {
						drs.setAcceptPlanNum(values[3]==null?0:Integer.parseInt(values[3]));
					}
					
					//未验收数量(未验收数量 = 总数 - 已验收)
					int noAcceptPlanNum = drs.getDistrPlanNum() - drs.getAcceptPlanNum();
					if(noAcceptPlanNum < 0 ) {
						noAcceptPlanNum = 0;
					}
					drs.setNoAcceptPlanNum(noAcceptPlanNum);
					//指派状态，0:未指派，1：已指派
					int assignStatusTemp = 0;
					if(values[9].equals("0") || values[9].equals("1") || values[9].equals("2")
							|| values[9].equals("3")) {
						assignStatusTemp = 1;
					}else if (values[9].equals("-1") || values[9].equals("-2") ) {
						assignStatusTemp = 0;
					}
					/*else if (values[9].equals("4") ) {
						assignStatusTemp = 2;
					}*/
					drs.setAssignStatus(assignStatusTemp);
					//已指派数量 
					drs.setAssignPlanNum(0);
					if(StringUtils.isNumeric(values[5])) {
						drs.setAssignPlanNum(values[5]==null?0:Integer.parseInt(values[5]));
					}
					
					//未指派数量(未配送数量 = 总数 - 已配送)
					int noAssignPlanNum = drs.getDistrPlanNum() - drs.getAssignPlanNum();
					if(noAssignPlanNum < 0 ) {
						noAssignPlanNum = 0 ;
					}
					drs.setNoAssignPlanNum(noAssignPlanNum);
					//配送状态，0:未派送，1: 已配送
					int dispStatusTemp = 0;
					if(values[9].equals("0") || values[9].equals("-1") || values[9].equals("-2") || values[9].equals("1")) {
						dispStatusTemp = 0;
					}else if (values[9].equals("2") || values[9].equals("3") ) {
						dispStatusTemp = 1;
					}
					drs.setDispStatus(dispStatusTemp);
					//已配送数量
					drs.setDispPlanNum(0);
					if(StringUtils.isNumeric(values[7])) {
						drs.setDispPlanNum(values[7]==null?0:Integer.parseInt(values[7]));
					}
					
					//未配送数量(未指派数量 = 总数 -已指派)
					int noDispPlanNum = drs.getDistrPlanNum() -drs.getDispPlanNum();
					if(noDispPlanNum<0) {
						noDispPlanNum = 0;
					}
					drs.setNoDispPlanNum(noDispPlanNum);
					
					dishRetSamples.add(drs);
				}
			}
		}
		//排序
		/*String[] methods = {"getDistName","getSchType"};
		String[] sorts = {"asc","asc"};
		String[] dataTypes = {"String","String"};*/
		
		SortList<PpGsPlanOpts> sortList = new SortList<PpGsPlanOpts>();
		sortList.Sort(dishRetSamples, methods, sorts, dataTypes);
		// 设置返回数据
		drsDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		// 分页
		PageBean<PpGsPlanOpts> pageBean = new PageBean<PpGsPlanOpts>(dishRetSamples, curPageNum, pageSize);
		PageInfo pageInfo = new PageInfo();
		pageInfo.setPageTotal(pageBean.getTotalCount());
		pageInfo.setCurPageNum(curPageNum);
		drsDto.setPageInfo(pageInfo);
		// 设置数据
		drsDto.setPpGsPlanOpts(pageBean.getCurPageData());
		// 消息ID
		drsDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return drsDto;
	}
	
	// 菜品留样列表模型函数
	public PpGsPlanOptsDTO appModFunc(String token,String startDate,String endDate,String ppName,
			Integer acceptStatus,Integer assignStatus, Integer dispStatus,Integer schType, String distName, 
			String prefCity, String province,String distNames,String schTypes,
			String page, String pageSize, Db1Service db1Service,
			EduSchoolService eduSchoolService, Db2Service db2Service) {
		PpGsPlanOptsDTO drsDto = null;
		
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
					if(methodIndex == 0) {
						drsDto = exampleDataFunc();
					}else if (methodIndex == 1) {
						// 菜品留样列表函数
						drsDto = dishRetSamplesOne(distIdorSCName, dates, tedList,ppName,acceptStatus,assignStatus,
								dispStatus,schType,distNames,schTypes,
								db1Service);
					}
					
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
					
					if(methodIndex == 0) {
						drsDto = exampleDataFunc();
					}else if (methodIndex == 1) {
						// 菜品留样列表函数
						drsDto = dishRetSamplesOne(distIdorSCName, dates, tedList,ppName,acceptStatus,assignStatus,
								dispStatus,schType,distNames,schTypes,
								db1Service);
					}
				}
			} else if (distName != null && prefCity != null && province != null) { // 按区域，地级市，省或直辖市处理

			} else if (distName == null && prefCity != null && province != null) { // 地级市，省或直辖市处理

			} else {
				logger.info("访问接口参数非法！");
			}			
		}
		else {    //模拟数据
			//模拟数据函数
			drsDto = exampleDataFunc();
		}		

		return drsDto;
	}
}
