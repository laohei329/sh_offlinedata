package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TBaseMaterialSupplierDo;

@Mapper
public interface TBaseMaterialSupplierDoMapper {
	//获取所有物料名称
	List<TBaseMaterialSupplierDo> getAllMatNames();
}
