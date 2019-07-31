package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProCategoryDo;

@Mapper
public interface TProCategoryDoMapper {
	//获取所有菜品类别
	List<TProCategoryDo> getAllDishTypes();
}
