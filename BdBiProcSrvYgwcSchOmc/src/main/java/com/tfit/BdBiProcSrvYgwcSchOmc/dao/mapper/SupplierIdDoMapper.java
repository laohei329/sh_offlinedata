package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SupplierIdDo;

@Mapper
public interface SupplierIdDoMapper {
	List<SupplierIdDo> getAllSupplierId();
}
