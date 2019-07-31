package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import java.util.List;

public class CrProcStatusDTO {
	String time;
	List<CrProcStatus> crProcStatus;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<CrProcStatus> getCrProcStatus() {
		return crProcStatus;
	}
	public void setCrProcStatus(List<CrProcStatus> crProcStatus) {
		this.crProcStatus = crProcStatus;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
