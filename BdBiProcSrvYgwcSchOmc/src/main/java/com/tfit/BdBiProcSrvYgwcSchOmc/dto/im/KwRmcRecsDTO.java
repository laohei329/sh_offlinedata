package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class KwRmcRecsDTO {
	String time;
	PageInfo pageInfo;
	List<KwRmcRecs> kwRmcRecs;
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
	public List<KwRmcRecs> getKwRmcRecs() {
		return kwRmcRecs;
	}
	public void setKwRmcRecs(List<KwRmcRecs> kwRmcRecs) {
		this.kwRmcRecs = kwRmcRecs;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
