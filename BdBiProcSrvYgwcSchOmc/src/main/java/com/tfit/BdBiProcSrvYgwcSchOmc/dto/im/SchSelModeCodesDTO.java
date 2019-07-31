package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class SchSelModeCodesDTO {
	String time;
	List<NameCode> schSelModeCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getSchSelModeCodes() {
		return schSelModeCodes;
	}
	public void setSchSelModeCodes(List<NameCode> schSelModeCodes) {
		this.schSelModeCodes = schSelModeCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
