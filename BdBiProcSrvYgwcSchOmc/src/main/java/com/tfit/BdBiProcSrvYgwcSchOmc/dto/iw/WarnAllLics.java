package com.tfit.BdBiProcSrvYgwcSchOmc.dto.iw;

public class WarnAllLics {
	String warnPeriod;
	String distName;
	int totalWarnNum;
	int noProcWarnNum;
	int rejectWarnNum;
	int auditWarnNum;
	int elimWarnNum;
	float warnProcRate;
	
	public String getWarnPeriod() {
		return warnPeriod;
	}
	public void setWarnPeriod(String warnPeriod) {
		this.warnPeriod = warnPeriod;
	}
	public String getDistName() {
		return distName;
	}
	public void setDistName(String distName) {
		this.distName = distName;
	}
	public int getTotalWarnNum() {
		return totalWarnNum;
	}
	public void setTotalWarnNum(int totalWarnNum) {
		this.totalWarnNum = totalWarnNum;
	}
	public int getNoProcWarnNum() {
		return noProcWarnNum;
	}
	public void setNoProcWarnNum(int noProcWarnNum) {
		this.noProcWarnNum = noProcWarnNum;
	}
	public int getRejectWarnNum() {
		return rejectWarnNum;
	}
	public void setRejectWarnNum(int rejectWarnNum) {
		this.rejectWarnNum = rejectWarnNum;
	}
	public int getAuditWarnNum() {
		return auditWarnNum;
	}
	public void setAuditWarnNum(int auditWarnNum) {
		this.auditWarnNum = auditWarnNum;
	}
	public int getElimWarnNum() {
		return elimWarnNum;
	}
	public void setElimWarnNum(int elimWarnNum) {
		this.elimWarnNum = elimWarnNum;
	}
	public float getWarnProcRate() {
		return warnProcRate;
	}
	public void setWarnProcRate(float warnProcRate) {
		this.warnProcRate = warnProcRate;
	}
}
