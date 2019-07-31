package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.CreatorDo;

@Mapper
public interface CreatorDoMapper {
	List<CreatorDo> getCreatorBySupplierId(String strSupplierId);
}
