package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.EtVidLib;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.EtVidLibDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//教育培训视频库应用模型
public class EtVidLibAppMod {
	private static final Logger logger = LogManager.getLogger(EtVidLibAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	//页号、页大小和总页数
	int curPageNum = 1, pageTotal = 1, pageSize = AppModConfig.maxPageSize;
	
	//数组数据初始化
	String[] vidId_Array = {"294dd56a-95c1-44ea-bc6a-7f6f3041c29a", "a36a0eb6-ac8a-4f05-b2bb-1723875af0e0", "82eb9015-2932-4e14-b6ac-935725badf6d", "b386549f-a86d-453c-8e69-ccad3f581c45", "eeadb734-61b8-4818-8a6b-cc78a04673af", "29f9e757-330e-4cf3-b879-6e0da1a5c74c", "c497549f-a86d-453c-8e69-ccad3f581c45", "e58f7a33-2fed-49e7-ade4-450ff8906fd1", "b98cb275-cae0-4323-9a9c-f1c9563b67f0", "0f6ef492-52ca-4d40-9434-8e0d7b2d3c07"};
	String[] vidName_Array = {"平台前端操作演示视频01", "平台前端操作演示视频02", "平台前端操作演示视频03", "平台前端操作演示视频04", "平台前端操作演示视频05", "平台前端操作演示视频06", "平台前端操作演示视频07", "平台前端操作演示视频08", "平台前端操作演示视频09", "平台前端操作演示视频10"};
	int[] vidCategory_Array = {1, 1, 2, 2, 3, 3, 1, 1, 2, 2};
	int[] playCount_Array = {5885, 4885, 3001, 2889, 1993, 1893, 993, 893, 793, 693};
	String[] uploadTime_Array = {"2018-09-06 09:51:35", "2018-09-06 10:51:35", "2018-09-06 11:51:35", "2018-09-06 12:51:35", "2018-09-06 13:51:35", "2018-09-06 14:51:35", "2018-09-07 17:51:35", "2018-09-07 18:51:35", "2018-09-07 19:00:35", "2018-09-07 19:21:35"};
	int[] likeCount_Array = {1000, 800, 600, 400, 200, 100, 200, 100, 200, 100};
	String[] vidUrl_Array = {"/etVidLib/1.mp4", "/etVidLib/2.mp4", "/etVidLib/3.mp4", "/etVidLib/4.mp4", "/etVidLib/5.mp4", "/etVidLib/6.mp4", "/etVidLib/7.mp4", "/etVidLib/8.mp4", "/etVidLib/9.mp4", "/etVidLib/10.mp4"};

	//模拟数据函数
	private EtVidLibDTO SimuDataFunc() {
		EtVidLibDTO evlDto = new EtVidLibDTO();
		//时戳
		evlDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//教育培训视频库模拟数据
		List<EtVidLib> etVidLib = new ArrayList<>();
		//赋值
		for (int i = 0; i < vidId_Array.length; i++) {
			EtVidLib evl = new EtVidLib();
			evl.setVidId(vidId_Array[i]);
			evl.setVidName(vidName_Array[i]);
			evl.setVidCategory(vidCategory_Array[i]);
			evl.setPlayCount(playCount_Array[i]);
			evl.setUploadTime(uploadTime_Array[i]);
			evl.setLikeCount(likeCount_Array[i]);
			evl.setVidUrl(SpringConfig.video_srvdn + vidUrl_Array[i]);
			etVidLib.add(evl);
		}
		//设置数据
		evlDto.setEtVidLib(etVidLib);
		//设置分页
		PageInfo pageInfo = new PageInfo();
		pageTotal = vidName_Array.length;
		pageInfo.setPageTotal(pageTotal);
		pageInfo.setCurPageNum(curPageNum);
		evlDto.setPageInfo(pageInfo);
		//消息ID
		evlDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return evlDto;
	}
	
	//数据列表分页
	public List<EtVidLib> DataListPaging(int page, int pageSize, List<EtVidLib> inDataList) {
		List<EtVidLib> pagingDataList = new ArrayList<>();
		
		return pagingDataList;
	}
	
	// 教育培训视频库模型函数
	public EtVidLibDTO appModFunc(String vidName, int vidCategory, int sortType, String distName, String prefCity, String province, String page, String pageSize, Db1Service db1Service) {
		EtVidLibDTO evlDto = null;
		if(page != null)
			curPageNum = Integer.parseInt(page);
		if(pageSize != null)
			this.pageSize = Integer.parseInt(pageSize);
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			evlDto = SimuDataFunc();
		}		

		return evlDto;
	}
}
