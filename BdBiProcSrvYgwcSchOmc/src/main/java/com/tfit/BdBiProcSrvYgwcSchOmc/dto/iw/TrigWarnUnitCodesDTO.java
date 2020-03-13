package com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class TrigWarnUnitCodesDTO {
	String time;
	PageInfo pageInfo;
	List<NameCode> trigWarnUnitCodes;
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
	public List<NameCode> getTrigWarnUnitCodes() {
		return trigWarnUnitCodes;
	}
	public void setTrigWarnUnitCodes(List<NameCode> trigWarnUnitCodes) {
		this.trigWarnUnitCodes = trigWarnUnitCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
