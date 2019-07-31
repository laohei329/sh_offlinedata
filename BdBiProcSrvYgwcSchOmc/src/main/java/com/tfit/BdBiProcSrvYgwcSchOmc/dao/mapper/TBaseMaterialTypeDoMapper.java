package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TBaseMaterialTypeDo;

@Mapper
public interface TBaseMaterialTypeDoMapper {
	// 获取所有物料分类
	List<TBaseMaterialTypeDo> getAllMatClassifyIdName();
}