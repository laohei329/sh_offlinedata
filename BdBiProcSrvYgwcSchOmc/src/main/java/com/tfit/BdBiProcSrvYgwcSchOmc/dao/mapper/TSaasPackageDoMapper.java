package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TSaasPackageDo;

@Mapper
public interface TSaasPackageDoMapper {
	//获取所有菜单组名称
	List<TSaasPackageDo> getAllMenuGroupName();
}
