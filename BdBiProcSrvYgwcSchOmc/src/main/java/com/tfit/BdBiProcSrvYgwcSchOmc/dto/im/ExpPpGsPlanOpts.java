package com.tfit.BdBiProcSrvYgwcSchOmc.dto.im;
import lombok.Data;

@Data
public class ExpPpGsPlanOpts {
	//开始日期，默认为当天日期，格式：xxxx-xx-xx
	String startDate;
	//结束日期，默认为当天日期，格式：xxxx-xx-xx
	String endDate;
	//项目点名称
	String ppName;
	//区域名称
	String distName;
	//地级城市
	String prefCity;
	//省或直辖市
	String province;
	//验收状态
	String acceptStatus;
	//指派状态
	String assignStatus;
	//配送状态
	String dispStatus;
	//学校类型（学制）
	String schType;
	//导出文件URL
	String expFileUrl;
}
