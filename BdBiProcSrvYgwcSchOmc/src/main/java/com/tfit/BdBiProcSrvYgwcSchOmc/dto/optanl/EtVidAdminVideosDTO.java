package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

public class EtVidAdminVideosDTO {
	String time;
	PageInfo pageInfo;
	List<EtVidAdminVideos> etVidAdminVideos;
	long msgId;
	
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public PageInfo getPageInfo() {
		return pageInfo;
	}
	public void setPageInfo(PageInfo pageInfo) {
		this.pageInfo = pageInfo;
	}
	public List<EtVidAdminVideos> getEtVidAdminVideos() {
		return etVidAdminVideos;
	}
	public void setEtVidAdminVideos(List<EtVidAdminVideos> etVidAdminVideos) {
		this.etVidAdminVideos = etVidAdminVideos;
	}
	public long getMsgId() {
		return msgId;
	}
	public void setMsgId(long msgId) {
		this.msgId = msgId;
	}
}
