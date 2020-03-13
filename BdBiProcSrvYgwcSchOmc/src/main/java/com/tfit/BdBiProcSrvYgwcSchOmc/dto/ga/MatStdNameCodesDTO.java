package com.tfit.BdBiProcSrvYgwcSchOmc.dto.ga;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class MatStdNameCodesDTO {
	String time;
	PageInfo pageInfo;
	List<NameCode> matStdNameCodes;
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
	public List<NameCode> getMatStdNameCodes() {
		return matStdNameCodes;
	}
	public void setMatStdNameCodes(List<NameCode> matStdNameCodes) {
		this.matStdNameCodes = matStdNameCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
