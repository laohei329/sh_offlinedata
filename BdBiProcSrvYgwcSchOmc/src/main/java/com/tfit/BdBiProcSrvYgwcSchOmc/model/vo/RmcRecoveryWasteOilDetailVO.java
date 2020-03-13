package com.tfit.BdBiProcSrvYgwcSchOmc.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @Descritpion：团餐公司回收废弃油脂详细列表
 * @author: tianfang_infotech
 * @date: 2019/1/7 16:11
 */
@Data
public class RmcRecoveryWasteOilDetailVO {

    /**
     * 回收日期
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd", timezone = "GMT+8")
    private Date recDate;

    /**
     * 区域名称
     */
    private String distName;

    /**
     * 团餐公司名称
     */
    private String rmcName;

    /**
     * 废弃油脂种类，0:废油，1:含油废水
     */
    private String woType;

    /**
     * 回收数量
     */
    private Integer recNum;

    /**
     * 回收单位
     */
    private String recCompany;

    /**
     * 回收人
     */
    private String recPerson;

    /**
     * 回收单据数
     */
    private Integer recBillNum;

	public Date getRecDate() {
		return recDate;
	}

	public void setRecDate(Date recDate) {
		this.recDate = recDate;
	}

	public String getDistName() {
		return distName;
	}

	public void setDistName(String distName) {
		this.distName = distName;
	}

	public String getRmcName() {
		return rmcName;
	}

	public void setRmcName(String rmcName) {
		this.rmcName = rmcName;
	}

	public String getWoType() {
		return woType;
	}

	public void setWoType(String woType) {
		this.woType = woType;
	}

	public Integer getRecNum() {
		return recNum;
	}

	public void setRecNum(Integer recNum) {
		this.recNum = recNum;
	}

	public String getRecCompany() {
		return recCompany;
	}

	public void setRecCompany(String recCompany) {
		this.recCompany = recCompany;
	}

	public String getRecPerson() {
		return recPerson;
	}

	public void setRecPerson(String recPerson) {
		this.recPerson = recPerson;
	}

	public Integer getRecBillNum() {
		return recBillNum;
	}

	public void setRecBillNum(Integer recBillNum) {
		this.recBillNum = recBillNum;
	}
}
