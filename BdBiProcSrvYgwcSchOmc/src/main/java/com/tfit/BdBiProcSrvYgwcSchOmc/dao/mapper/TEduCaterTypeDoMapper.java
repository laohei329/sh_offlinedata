package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduCaterTypeDo;

@Mapper
public interface TEduCaterTypeDoMapper {
	//获取所有餐别类型名称
	List<TEduCaterTypeDo> getAllCaterTypeNames();
}
