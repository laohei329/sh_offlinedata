package com.tfit.BdBiProcSrvYgwcSchOmc.dto.pn;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class AnnReadCodesDTO {
	String time;
	List<NameCode> annReadCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getAnnReadCodes() {
		return annReadCodes;
	}
	public void setAnnReadCodes(List<NameCode> annReadCodes) {
		this.annReadCodes = annReadCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
