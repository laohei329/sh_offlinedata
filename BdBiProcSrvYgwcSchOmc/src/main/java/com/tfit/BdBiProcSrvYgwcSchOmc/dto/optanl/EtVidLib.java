package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

public class EtVidLib {
	String vidId;
	String vidName;
	int vidCategory;
	int playCount;
	String uploadTime;
    int likeCount;
    String vidUrl;
    
	public String getVidId() {
		return vidId;
	}
	public void setVidId(String vidId) {
		this.vidId = vidId;
	}
	public String getVidName() {
		return vidName;
	}
	public void setVidName(String vidName) {
		this.vidName = vidName;
	}
	public int getVidCategory() {
		return vidCategory;
	}
	public void setVidCategory(int vidCategory) {
		this.vidCategory = vidCategory;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}
	public String getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(String uploadTime) {
		this.uploadTime = uploadTime;
	}
	public int getLikeCount() {
		return likeCount;
	}
	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}
	public String getVidUrl() {
		return vidUrl;
	}
	public void setVidUrl(String vidUrl) {
		this.vidUrl = vidUrl;
	}
}
