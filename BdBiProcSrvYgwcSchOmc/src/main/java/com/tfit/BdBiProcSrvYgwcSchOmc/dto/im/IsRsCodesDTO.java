package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class IsRsCodesDTO {
	String time;
	List<NameCode> isRsCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getIsRsCodes() {
		return isRsCodes;
	}
	public void setIsRsCodes(List<NameCode> isRsCodes) {
		this.isRsCodes = isRsCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
