package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class PpRetSamplesDTO {
	String time;
	PageInfo pageInfo;
	List<PpRetSamples> ppRetSamples;
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
	
	public List<PpRetSamples> getPpRetSamples() {
		return ppRetSamples;
	}
	public void setPpRetSamples(List<PpRetSamples> ppRetSamples) {
		this.ppRetSamples = ppRetSamples;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
