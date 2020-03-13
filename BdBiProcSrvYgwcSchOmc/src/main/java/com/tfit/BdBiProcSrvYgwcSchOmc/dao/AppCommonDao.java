package com.tfit.BdBiProcSrvYgwcSchOmc.dao;

import java.util.LinkedHashMap;

public class AppCommonDao {
	private LinkedHashMap<String, Object> commonMap;
	
	public AppCommonDao() {
		
	}
	public AppCommonDao(LinkedHashMap<String, Object> commonMap) {
		this.commonMap=commonMap;
	}
	
	public LinkedHashMap<String, Object> getCommonMap() {
		return commonMap;
	}

	public void setCommonMap(LinkedHashMap<String, Object> commonMap) {
		this.commonMap = commonMap;
	}
	
}
