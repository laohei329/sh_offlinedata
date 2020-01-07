package com.tfit.BdBiProcSrvYgwcSchOmc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.appmod.export.ExportPdfMod;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.DownloadRecord;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.*;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.EduSchoolService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.ToolUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** 
 * @Description: 导出pdf
 * @Author: jianghy 
 * @Date: 2019/12/25
 * @Time: 16:39       
 */
@RestController
@RequestMapping(value = "/biOptAnl")
public class ExportController {
	private static final Logger logger = LogManager.getLogger(ExportController.class.getName());
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
    * hive数据库服务
    */
   @Autowired
   DbHiveDishService dbHiveDishService;
   
   /**
    * hive数据库服务
    */
   @Autowired
   DbHiveService dbHiveService;
   
   /**
    * 教委系统学校信息服务
    */
   @Autowired
   private EduSchoolService eduSchoolService;

	@Autowired
	ObjectMapper objectMapper;

	/** 
	 * @Description: 导出pdf 
	 * @Param: [] 
	 * @return: void 
	 * @Author: jianghy 
	 * @Date: 2020/1/1
	 * @Time: 20:42       
	 */
	@RequestMapping(value = "/v1/exportPdf", method = RequestMethod.POST)
	public void exportPdf(){
		logger.info("exportPdf 定时任务开始！"+ new ToolUtil().currentTime());
		new ExportPdfMod().appModFunc(db1Service, db2Service, saasService,dbHiveDishService,eduSchoolService,dbHiveService);
	}

	
	/** 
	 * @Description: 下载pdf
	 * @Param: [request] 
	 * @return: java.lang.String 
	 * @Author: jianghy 
	 * @Date: 2020/1/1
	 * @Time: 20:43
	 */
    @RequestMapping(value = "/v1/downloadPdf", method = RequestMethod.GET)
    public String exportPdf(HttpServletRequest request, HttpServletResponse response){
        //生成时间
        String createDate = request.getParameter("createDate");
        //教育局的名称
        String committeeName = request.getParameter("committeeName");
        //授权码
        String token = request.getHeader("Authorization");
        int status = new ExportPdfMod().downloadPdf(db1Service,createDate,committeeName,token,request,response);
        String msg ;
        if (DownloadRecord.STATUS_SUCCESS == status){
            msg = "下载成功";
        }else{
            msg = "下载失败";
        }
        return msg;
    }


    /** 
     * @Description: 获取数据报告列表
     * @Param: [request] 
     * @return: java.lang.String 
     * @Author: jianghy 
     * @Date: 2020/1/2
     * @Time: 16:11
     */
    @RequestMapping(value = "/v1/getWeekReportList", method = RequestMethod.GET)
    public String getWeekReportList(HttpServletRequest request){
        return new ExportPdfMod().getWeekReportList(request, db1Service, db2Service);
    }

}
