package com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class CaMatSupDetsDTO {
	String time;
	PageInfo pageInfo;
	List<CaMatSupDets> caMatSupDets;
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
	public List<CaMatSupDets> getCaMatSupDets() {
		return caMatSupDets;
	}
	public void setCaMatSupDets(List<CaMatSupDets> caMatSupDets) {
		this.caMatSupDets = caMatSupDets;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
