package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class SendStatusCodesDTO {
	String time;
	List<NameCode> sendStatusCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getSendStatusCodes() {
		return sendStatusCodes;
	}
	public void setSendStatusCodes(List<NameCode> sendStatusCodes) {
		this.sendStatusCodes = sendStatusCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
