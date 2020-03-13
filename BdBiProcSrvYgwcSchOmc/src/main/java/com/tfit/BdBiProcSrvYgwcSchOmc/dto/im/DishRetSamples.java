package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;

public class DishRetSamples {
	String repastDate;
	String subLevel;
	String compDep;
	String distName;
	int dishSchNum;
	//应留样学校
	int shouldRsSchNum;
	int noRsSchNum;
	int rsSchNum;
	//学校留样率
	float schRsRate;
	int menuNum;
	int rsMenuNum;
	int noRsMenuNum;
	float rsRate;
	
	public String getRepastDate() {
		return repastDate;
	}
	public void setRepastDate(String repastDate) {
		this.repastDate = repastDate;
	}
	public String getSubLevel() {
		return subLevel;
	}
	public void setSubLevel(String subLevel) {
		this.subLevel = subLevel;
	}
	public String getCompDep() {
		return compDep;
	}
	public void setCompDep(String compDep) {
		this.compDep = compDep;
	}
	public String getDistName() {
		return distName;
	}
	public void setDistName(String distName) {
		this.distName = distName;
	}
	public int getDishSchNum() {
		return dishSchNum;
	}
	public void setDishSchNum(int dishSchNum) {
		this.dishSchNum = dishSchNum;
	}
	public int getNoRsSchNum() {
		return noRsSchNum;
	}
	public void setNoRsSchNum(int noRsSchNum) {
		this.noRsSchNum = noRsSchNum;
	}
	public int getRsSchNum() {
		return rsSchNum;
	}
	public void setRsSchNum(int rsSchNum) {
		this.rsSchNum = rsSchNum;
	}
	public int getMenuNum() {
		return menuNum;
	}
	public void setMenuNum(int menuNum) {
		this.menuNum = menuNum;
	}
	public int getRsMenuNum() {
		return rsMenuNum;
	}
	public void setRsMenuNum(int rsMenuNum) {
		this.rsMenuNum = rsMenuNum;
	}
	public int getNoRsMenuNum() {
		return noRsMenuNum;
	}
	public void setNoRsMenuNum(int noRsMenuNum) {
		this.noRsMenuNum = noRsMenuNum;
	}
	public float getRsRate() {
		return rsRate;
	}
	public void setRsRate(float rsRate) {
		this.rsRate = rsRate;
	}
	public int getShouldRsSchNum() {
		return shouldRsSchNum;
	}
	public void setShouldRsSchNum(int shouldRsSchNum) {
		this.shouldRsSchNum = shouldRsSchNum;
	}
	public float getSchRsRate() {
		return schRsRate;
	}
	public void setSchRsRate(float schRsRate) {
		this.schRsRate = schRsRate;
	}
	
}
