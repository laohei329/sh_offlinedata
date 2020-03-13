package com.tfit.BdBiProcSrvYgwcSchOmc.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.EduSchoolService;

//业务数据统计BI应用服务
@RestController
@RequestMapping(value = "/biOptAnl")
public class BusiDataStatisticsController {
	private static final Logger logger = LogManager.getLogger(BusiDataStatisticsController.class.getName());

    /**
     * mysql主数据库服务
     */
    @Autowired
    SaasService saasService;

    /**
     * mysql数据库服务1
     */
    @Autowired
    Db1Service db1Service;

    /**
     * mysql数据库服务2
     */
    @Autowired
    Db2Service db2Service;
    
    /**
     * 教委系统学校信息服务
     */
    @Autowired
    private EduSchoolService eduSchoolService;

    @Autowired
    ObjectMapper objectMapper;

}