package com.tfit.BdBiProcSrvYgwcSchOmc.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.appmod.optanl.*;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTHttpRspVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.*;
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
 * @Descritpion：业务数据-信息共享
 * @author: tianfang_infotech
 * @date: 2019/1/2 16:47
 */
@RestController
@RequestMapping(value = "/biOptAnl")
public class InfoShareController {
    private static final Logger logger = LogManager.getLogger(HomeController.class.getName());

    @Autowired
    ObjectMapper objectMapper;

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
     * 信息共享食品安全等级列表应用模型
     */
    IsFsgListAppMod iflAppMod = new IsFsgListAppMod();

    /**
     * 信息共享食品安全等级详情列表应用模型
     */
    IsFsgDetsAppMod ifdAppMod = new IsFsgDetsAppMod();

    /**
     * 3.6.1 - 信息共享食品安全等级列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/v1/isFsgList", method = RequestMethod.GET)
    public String v1_isFsgList(HttpServletRequest request) {
        //初始化响应数据
        String strResp = null;
        IsFsgListDTO iflDto = null;
        boolean isAuth = false;
		int code = 0;
		int[] codes = new int[1];
        //授权码
        String token = AppModConfig.GetHeadJsonReq(request, "Authorization");
        //区域名称
        String distName = request.getParameter("distName");
        if (distName == null) {
            distName = request.getParameter("distname");
        }
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
        logger.info("输入参数：" + "token = " + token + ", distName = " + distName + ", prefCity = " + prefCity + ", province = " + province);
        //验证授权
  		isAuth = AppModConfig.verifyAuthCode2(token, db2Service, codes);        
        //信息共享食品安全等级列表应用模型函数
		if(isAuth)
			iflDto = iflAppMod.appModFunc(distName, prefCity, province, db1Service);
		else
			logger.info("授权码：" + token + "，验证失败！");          
        //设置响应数据
        if (iflDto != null) {
            try {
                strResp = objectMapper.writeValueAsString(iflDto);
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
     * 3.6.2 - 信息共享食品安全等级详情列表
     * @param request
     * @return
     */
    @RequestMapping(value = "/v1/isFsgDets", method = RequestMethod.GET)
    public String v1_isFsgDets(HttpServletRequest request) {
        //初始化响应数据
        String strResp = null;
        IsFsgDetsDTO ifdDto = null;
        boolean isAuth = false;
		int code = 0;
		int[] codes = new int[1];
        //授权码
        String token = AppModConfig.GetHeadJsonReq(request, "Authorization");
        //项目点名称
        String ppName = request.getParameter("ppName");
        //学校类型
        String schType = request.getParameter("schType");
        //供餐模式，0:自营，1:外包-快餐配送，2:外包-现场加工，3:其他
        String dinnerMod = request.getParameter("dinnerMod");
        //等级，0:良好，1:一般，2:较差
        String grade = request.getParameter("grade");
        //区域名称
        String distName = request.getParameter("distName");
        if (distName == null) {
            distName = request.getParameter("distname");
        }
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
        logger.info("输入参数：" + "token = " + token + ", ppName = " + ppName + ", schType = " + schType + ", dinnerMod = " + dinnerMod + ", grade = " + grade + ", distName = " + distName + ", prefCity = " + prefCity + ", province = " + province + ", page = " + page + ", pageSize = " + pageSize);
        //验证授权
  		isAuth = AppModConfig.verifyAuthCode2(token, db2Service, codes);        
        //信息共享食品安全等级详情列表应用模型函数
		if(isAuth)
			ifdDto = ifdAppMod.appModFunc(ppName, schType, dinnerMod, grade, distName, prefCity, province, page, pageSize, db1Service);
		else
			logger.info("授权码：" + token + "，验证失败！");        
        //设置响应数据
        if (ifdDto != null) {
            try {
                strResp = objectMapper.writeValueAsString(ifdDto);
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
