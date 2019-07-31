package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrDet;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CrDetDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.BCDTimeUtil;

//投诉举报详情应用模型
public class CrDetAppMod {
	private static final Logger logger = LogManager.getLogger(CrDetAppMod.class.getName());
	
	//是否为真实数据标识
	private static boolean isRealData = false;
	
	//变量数据初始化
	String crId = "12703e54-35cc-4db0-84a3-ca5e7c7d5ac7";
	String ppName = "上海市**中学";
	String title = "上海市**中学食堂卫生条件差";
	String subDate = "2018-09-03";
	String crContent = "学校食堂的卫生让人无法忍受，新食堂的筷子总是洗不干净，有次拿起来上面还有菜叶，更多的时候感觉油腻，\r\n" + 
			"\r\n" + 
			"免费汤的碗一般都是没洗干净，有个小米粒什么的正常，有一次从筐里拿出来的碗居然有半碗剩下的粥，分汤的师傅\r\n" + 
			"\r\n" + 
			"居然就那么拿去旁边放下了，学五又一次吃出苍蝇，今天又吃完了，发现虫子。这是吃的多了，必然会经历的吗？上图！！";
	String complainant = "匿名";
	String telNumber = "13565456465";
	String contractor = "徐汇区教育局";
	int procStatus = 2;
	String procFeedBack = "经工作人员查实，该食堂确实存在投诉反映的情况，已经责任其一周内整改，一周后将实地复查，整改不达标将有进一步处罚措施。";
	String handleDate = "2018-09-05";
	
	//模拟数据函数
	private CrDetDTO SimuDataFunc() {
		CrDetDTO crdDto = new CrDetDTO();
		//时戳
		crdDto.setTime(BCDTimeUtil.convertNormalFrom(null));
		//投诉举报详情模拟数据
		CrDet crDet = new CrDet();
		//赋值
		crDet.setCrId(crId);
		crDet.setPpName(ppName);
		crDet.setTitle(title);
		crDet.setSubDate(subDate);
		crDet.setCrContent(crContent);
		crDet.setComplainant(complainant);
		crDet.setTelNumber(telNumber);
		crDet.setContractor(contractor);
		crDet.setProcStatus(procStatus);
		crDet.setProcFeedBack(procFeedBack);
		crDet.setHandleDate(handleDate);
		//设置数据
		crdDto.setCrDet(crDet);
		//消息ID
		crdDto.setMsgId(AppModConfig.msgId);
		AppModConfig.msgId++;
		// 消息id小于0判断
		AppModConfig.msgIdLessThan0Judge();
		
		return crdDto;
	}
	
	// 投诉举报详情模型函数
	public CrDetDTO appModFunc(String crId, String distName, String prefCity, String province, Db1Service db1Service) {
		CrDetDTO crdDto = null;
		if(isRealData) {       //真实数据
			
		}
		else {    //模拟数据
			//模拟数据函数
			crdDto = SimuDataFunc();
		}		

		return crdDto;
	}
}
