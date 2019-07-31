package com.tfit.BdBiProcSrvYgwcSchOmc.dto.ms;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class MailSrvPortCodesDTO {
	String time;
	List<NameCode> mailSrvPortCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getMailSrvPortCodes() {
		return mailSrvPortCodes;
	}
	public void setMailSrvPortCodes(List<NameCode> mailSrvPortCodes) {
		this.mailSrvPortCodes = mailSrvPortCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}