package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class CaterTypeCodesDTO {
	String time;
	List<NameCode> caterTypeCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getCaterTypeCodes() {
		return caterTypeCodes;
	}
	public void setCaterTypeCodes(List<NameCode> caterTypeCodes) {
		this.caterTypeCodes = caterTypeCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
