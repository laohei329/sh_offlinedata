package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProWarningDo;

@Mapper
public interface TProWarningDoMapper {
	//获取证照预警类型
	List<TProWarningDo> getLicWarnType();
}
