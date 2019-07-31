package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduMenuTemplateDishesDo;

@Mapper
public interface TEduMenuTemplateDishesDoMapper {
	//获取所有菜品类别
	List<TEduMenuTemplateDishesDo> getAllDishTypes();
}