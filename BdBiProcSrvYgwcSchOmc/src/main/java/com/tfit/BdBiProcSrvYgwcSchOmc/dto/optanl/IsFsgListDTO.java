package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class IsFsgListDTO {
	String time;
	PageInfo pageInfo;
	List<IsFsgList> isFsgList;
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
	public List<IsFsgList> getIsFsgList() {
		return isFsgList;
	}
	public void setIsFsgList(List<IsFsgList> isFsgList) {
		this.isFsgList = isFsgList;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
