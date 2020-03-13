package com.tfit.BdBiProcSrvYgwcSchOmc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edu.TEduDistrictV2DoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Custom1Service;

@Service
public class Custom1ServiceImpl implements Custom1Service {
	@Autowired
	TEduDistrictV2DoMapper tedMapper;
	
	//从数据源ds1的数据表t_edu_district中查找id和区域名称
    public List<TEduDistrictDo> getListByDs1IdName() {
    	return null;
    }
}