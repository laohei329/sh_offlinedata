package com.tfit.BdBiProcSrvYgwcSchOmc.dto.user;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.NameCode;

public class RoleTypeCodesDTO {
	String time;
	List<NameCode> roleTypeCodes;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public List<NameCode> getRoleTypeCodes() {
		return roleTypeCodes;
	}
	public void setRoleTypeCodes(List<NameCode> roleTypeCodes) {
		this.roleTypeCodes = roleTypeCodes;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
