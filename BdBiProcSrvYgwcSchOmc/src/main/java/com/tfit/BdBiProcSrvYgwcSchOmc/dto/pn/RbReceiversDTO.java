package com.tfit.BdBiProcSrvYgwcSchOmc.dto.pn;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.util.Node;

public class RbReceiversDTO {
	String time;
	List<Node> rbReceivers;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<Node> getRbReceivers() {
		return rbReceivers;
	}
	public void setRbReceivers(List<Node> rbReceivers) {
		this.rbReceivers = rbReceivers;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
