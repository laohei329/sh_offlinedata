package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduMenuGroupDo;

@Mapper
public interface TEduMenuGroupDoMapper {
	//获取所有菜单组ID和名称
	List<TEduMenuGroupDo> getAllMenuGroupIdName();
}