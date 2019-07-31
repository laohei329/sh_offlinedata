package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class SchPropCodesDTO {
	String time;
	List<NameCode> schPropCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getSchPropCodes() {
		return schPropCodes;
	}
	public void setSchPropCodes(List<NameCode> schPropCodes) {
		this.schPropCodes = schPropCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
