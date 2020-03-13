package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TProReserveSampleDo;

@Mapper
public interface TProReserveSampleDoMapper {
	//获取留样单位
	List<TProReserveSampleDo> getAllRsUnits();
}
