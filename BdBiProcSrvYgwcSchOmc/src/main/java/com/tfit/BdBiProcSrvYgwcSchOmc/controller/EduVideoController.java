package com.tfit.BdBiProcSrvYgwcSchOmc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl.EtVidAdminVideosAppMod;
import com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl.EtVidLibAppMod;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTHttpRspVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.EtVidAdminVideosDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.EtVidLibDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db2Service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * @Descritpion：运营管理-教育培训视频
 * @author: tianfang_infotech
 * @date: 2019/1/2 16:47
 */
@RestController
@RequestMapping(value = "/biOptAnl")
public class EduVideoController {
    private static final Logger logger = LogManager.getLogger(EduVideoController.class.getName());

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

    @Autowired
    ObjectMapper objectMapper;

    /**
     * 教育培训视频库应用模型
     */
    EtVidLibAppMod evlAppMod = new EtVidLibAppMod();

    /**
     * 教育培训视频管理视频列表应用模型
     */
    EtVidAdminVideosAppMod evavAppMod = new EtVidAdminVideosAppMod();


    /**
     * 3.10.1 - 教育培训视频库
     * @param request
     * @return
     */
    @RequestMapping(value = "/v1/etVidLib", method = RequestMethod.GET)
    public String v1_etVidLib(HttpServletRequest request) {
        //初始化响应数据
        String strResp = null;
        EtVidLibDTO evlDto = null;
        boolean isAuth = false;
		int code = 0;
		int[] codes = new int[1];
        //视频名称，模糊查询
        String vidName = request.getParameter("vidName");
        //视频分类，默认为0，0:全部，1:系统操作，2:食品安全，3:政策法规
        String strVidCategory = request.getParameter("vidCategory");
        int vidCategory = 0;
        if (strVidCategory != null) {
            vidCategory = Integer.parseInt(strVidCategory);
        }
        //排序类型，默认为0，0:按播放次数降序，1:按上传时间降序，2:按好评率降序
        String strSortType = request.getParameter("sortType");
        int sortType = 0;
        if (strSortType != null) {
            sortType = Integer.parseInt(strSortType);
        }
        //区域名称
        String distName = request.getParameter("distName");
        if (distName == null) {
            distName = request.getParameter("distname");
        }
        //授权码
        String token = AppModConfig.GetHeadJsonReq(request, "Authorization");
        //地级城市
        String prefCity = request.getParameter("prefCity");
        if (prefCity == null) {
            prefCity = request.getParameter("prefcity");
        }
        //省或直辖市
        String province = request.getParameter("province");
        //页号
        String page = request.getParameter("page");
        if (page == null) {
            page = "1";
        }
        //分页大小
        String pageSize = request.getParameter("pageSize");
        if (pageSize == null) {
            pageSize = "20";
        }
        logger.info("输入参数：" + "token = " + token + ", vidName = " + vidName + ", vidCategory = " + vidCategory + ", sortType = " + sortType + ", distName = " + distName + ", prefCity = " + prefCity + ", province = " + province + ", page = " + page + ", pageSize = " + pageSize);
  		//验证授权
  		isAuth = AppModConfig.verifyAuthCode2(token, db2Service, codes);
        //教育培训视频库应用模型函数
		if(isAuth)
			evlDto = evlAppMod.appModFunc(vidName, vidCategory, sortType, distName, prefCity, province, page, pageSize, db1Service);
		else
			logger.info("授权码：" + token + "，验证失败！");        
        //设置响应数据
        if (evlDto != null) {
            try {
                strResp = objectMapper.writeValueAsString(evlDto);
                logger.info(strResp);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            //获取异常响应
        	code = codes[0];
			logger.info("错误码：code = " + code);
			IOTHttpRspVO excepResp = AppModConfig.getExcepResp(code);
            try {
                strResp = objectMapper.writeValueAsString(excepResp);
                logger.info(strResp);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return strResp;
    }

    /**
     * 3.10.3 - 教育培训视频管理视频列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/v1/etVidAdminVideos", method = RequestMethod.GET)
    public String v1_etVidAdminVideos(HttpServletRequest request) {
        //初始化响应数据
        String strResp = null;
        EtVidAdminVideosDTO evavDto = null;
        boolean isAuth = false;
		int code = 0;
		int[] codes = new int[1];
        //日期类型，0:上传日期，1:审核日期
        String dateType = request.getParameter("dateType");
        //开始日期，格式：xxxx-xx-xx
        String startDate = request.getParameter("startSubDate");
        //结束日期，格式：xxxx-xx-xx
        String endDate = request.getParameter("endSubDate");
        //视频名称，模糊查询
        String vidName = request.getParameter("vidName");
        //视频分类，1:系统操作，2:食品安全，3:政策法规
        String vidCategory = request.getParameter("vidCategory");
        if (vidCategory == null){
            vidCategory = "1";}
        //视频状态，0:待审核，1:已审核，2:已下架，3:已驳回
        String vidStatus = request.getParameter("vidStatus");
        //区域名称
        String distName = request.getParameter("distName");
        if (distName == null) {
            distName = request.getParameter("distname");
        }
        //授权码
        String token = AppModConfig.GetHeadJsonReq(request, "Authorization");
        //地级城市
        String prefCity = request.getParameter("prefCity");
        if (prefCity == null) {
            prefCity = request.getParameter("prefcity");
        }
        //省或直辖市
        String province = request.getParameter("province");
        //页号
        String page = request.getParameter("page");
        if (page == null) {
            page = "1";
        }
        //分页大小
        String pageSize = request.getParameter("pageSize");
        if (pageSize == null) {
            pageSize = "20";
        }
        logger.info("输入参数：" + "token = " + token + ", dateType = " + dateType + ", startDate = " + startDate + ", endDate = " + endDate + ", vidName = " + vidName + ", vidCategory = " + vidCategory + ", vidStatus = " + vidStatus + ", distName = " + distName + ", prefCity = " + prefCity + ", province = " + province + ", page = " + page + ", pageSize = " + pageSize);
  		//验证授权
  		isAuth = AppModConfig.verifyAuthCode2(token, db2Service, codes);
        //教育培训视频管理视频列表应用模型函数
		if(isAuth)
			evavDto = evavAppMod.appModFunc(dateType, startDate, endDate, vidName, vidCategory, vidStatus, distName, prefCity, province, page, pageSize, db1Service);
		else
			logger.info("授权码：" + token + "，验证失败！");
        //设置响应数据
        if (evavDto != null) {
            try {
                strResp = objectMapper.writeValueAsString(evavDto);
                logger.info(strResp);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            //获取异常响应
        	code = codes[0];
			logger.info("错误码：code = " + code);
			IOTHttpRspVO excepResp = AppModConfig.getExcepResp(code);
            try {
                strResp = objectMapper.writeValueAsString(excepResp);
                logger.info(strResp);
            } catch (JsonProcessingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return strResp;
    }
}
