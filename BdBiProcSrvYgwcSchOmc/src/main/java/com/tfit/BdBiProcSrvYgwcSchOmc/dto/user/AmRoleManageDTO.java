package com.tfit.BdBiProcSrvYgwcSchOmc.dto.user;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class AmRoleManageDTO {
	String time;
	PageInfo pageInfo;
	List<AmRoleManage> amRoleManage;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public PageInfo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}
	public List<AmRoleManage> getAmRoleManage() {
		return amRoleManage;
	}
	public void setAmRoleManage(List<AmRoleManage> amRoleManage) {
		this.amRoleManage = amRoleManage;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
