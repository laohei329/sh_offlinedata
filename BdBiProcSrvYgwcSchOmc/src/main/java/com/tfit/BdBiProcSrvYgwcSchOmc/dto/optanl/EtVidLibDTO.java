package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class EtVidLibDTO {
	String time;
	PageInfo pageInfo;
	List<EtVidLib> etVidLib;
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
	public List<EtVidLib> getEtVidLib() {
		return etVidLib;
	}
	public void setEtVidLib(List<EtVidLib> etVidLib) {
		this.etVidLib = etVidLib;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
