package com.tfit.BdBiProcSrvYgwcSchOmc.dto.bd;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class BdSupListDTO {
	String time;
	PageInfo pageInfo;
	List<BdSupList> bdSupList;
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
	public List<BdSupList> getBdSupList() {
		return bdSupList;
	}
	public void setBdSupList(List<BdSupList> bdSupList) {
		this.bdSupList = bdSupList;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
