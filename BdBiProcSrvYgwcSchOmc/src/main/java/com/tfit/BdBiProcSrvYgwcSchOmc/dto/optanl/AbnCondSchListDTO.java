package com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;

import lombok.Data;

/**
 * 3.2.3.	异常情况学校列表模型
 * @author Administrator
 *
 */
@Data
public class AbnCondSchListDTO {
	String time;
	PageInfo pageInfo;
	List<AbnCondSchList> abnCondSchList;
	Long msgId;
}
