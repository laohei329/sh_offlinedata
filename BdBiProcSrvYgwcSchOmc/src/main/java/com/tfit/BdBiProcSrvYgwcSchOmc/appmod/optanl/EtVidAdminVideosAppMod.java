package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.EtVidAdminVideos;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.EtVidAdminVideosDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//教育培训视频管理视频列表应用模型
public class EtVidAdminVideosAppMod {
	private static final Logger logger = LogManager.getLogger(EtVidAdminVideosAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	
	//数组数据初始化
	String[] vidId_Array = {"319d4ab7-5f60-4e99-9f32-495f024a0286", "449d4ab7-5f60-4e99-9f32-495f024a0286"};
	String[] uploadDate_Array = {"2018-08-20", "2018-08-20"};
	String[] vidName_Array = {"团餐公司操作演示视频04", "团餐公司操作演示视频03"};
	int[] vidCategory_Array = {1, 1};
	String[] pubPerson_Array = {"wang", "wang"};
	String[] auditPerson_Array = {"shsjw", "shsjw"};
	String[] auditDate_Array = {"2018-08-21", "2018-08-21"};
	int[] vidStatus_Array = {0, 1};
	
	//模拟数据函数
	private EtVidAdminVideosDTO SimuDataFunc(String vidStatus) {
		EtVidAdminVideosDTO evavDto = new EtVidAdminVideosDTO();
		//时戳
		evavDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//教育培训视频管理视频列表模拟数据
		List<EtVidAdminVideos> etVidLib = new ArrayList<>();
		//赋值
		for (int i = 0; i < vidId_Array.length; i++) {
			EtVidAdminVideos evav = new EtVidAdminVideos();
			evav.setVidId(vidId_Array[i]);
			evav.setUploadDate(uploadDate_Array[i]);
			evav.setVidName(vidName_Array[i]);
			evav.setVidCategory(vidCategory_Array[i]);
			evav.setPubPerson(pubPerson_Array[i]);
			evav.setAuditPerson(auditPerson_Array[i]);
			evav.setAuditDate(auditDate_Array[i]);
			evav.setVidStatus(vidStatus_Array[i]);
			if(vidStatus == null)
				etVidLib.add(evav);
			else {
				int curVidStatus = Integer.parseInt(vidStatus);
				if(curVidStatus == vidStatus_Array[i])
					etVidLib.add(evav);
			}
		}
		//设置数据
		evavDto.setEtVidAdminVideos(etVidLib);
		//设置分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = vidName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		evavDto.setPageInfo(pageInfo);
		//消息ID
		evavDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return evavDto;
	}
	
	//数据列表分页
	public List<EtVidAdminVideos> DataListPaging(int page, int pageSize, List<EtVidAdminVideos> inDataList) {
		List<EtVidAdminVideos> pagingDataList = new ArrayList<>();
		
		return pagingDataList;
	}
	
	// 教育培训视频管理视频列表模型函数
	public EtVidAdminVideosDTO appModFunc(String dateType, String startDate, String endDate, String vidName, String vidCategory, String vidStatus, String distName, String prefCity, String province, String page, String pageSize, Db1Service db1Service) {
		EtVidAdminVideosDTO evavDto = null;
		if(page != null)
			curPageNum = Integer.parseInt(page);
		if(pageSize != null)
			this.pageSize = Integer.parseInt(pageSize);
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			evavDto = SimuDataFunc(vidStatus);
		}		

		return evavDto;
	}
}
