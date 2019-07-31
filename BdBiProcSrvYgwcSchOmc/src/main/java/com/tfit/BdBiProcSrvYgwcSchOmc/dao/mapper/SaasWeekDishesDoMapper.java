package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SaasWeekDishesDoMapper {
	List<String> getAllSupplierId();
}
