package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import lombok.Data;

@Data
public class PpGsPlanOpts {
	//配货日期，格式：xxxx/xx/xx
	String distrDate;
	//区域名称
	String distName;
	//学校类型（学制）
	String schType;
	//项目点名称
	String ppName;
	//详细地址
	String detailAddr;
	//项目联系人
	String projContact;
	//手机
	String pcMobilePhone;
	//配货计划数量
	int distrPlanNum;
	//验收状态，0:待验收，1:已验收
	int acceptStatus;
	//已验收数量
	int acceptPlanNum;
	//未验收数量
	int noAcceptPlanNum;
	//指派状态，0:未指派，1：已指派，2:已取消
	int assignStatus;
	//已指派数量 
	int assignPlanNum;
	//未指派数量
	int noAssignPlanNum;
	//配送状态，0:未派送，1:配送中，2: 已配送
	int dispStatus;
	//已配送数量
	int dispPlanNum;
	//未配送数量
	int noDispPlanNum;
	
	
	
}
