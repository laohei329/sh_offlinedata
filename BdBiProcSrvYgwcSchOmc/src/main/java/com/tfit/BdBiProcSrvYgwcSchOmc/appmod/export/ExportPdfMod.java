package com.tfit.BdBiProcSrvYgwcSchOmc.appmod.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.ApplicationUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.AppCommonDao;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.AppCommonData;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.AppCommonExternalModulesDto;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.DownloadRecord;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTRspType;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.AppOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.CheckOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.optanl.DishOperateRateInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.*;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.EduSchoolService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.FtpUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.ToolUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: 导出pdf模板
 * @Author: jianghy
 * @Date: 2019/12/28
 * @Time: 16:29
 */
public class ExportPdfMod {
    private static final Logger logger = LogManager.getLogger(ExportPdfMod.class.getName());
    // 是否为真实数据标识
    private static boolean isRealData = true;

    // 页号、页大小和总页数
    int curPageNum = 1, pageTotal = 1, pageSize = 20;
    ObjectMapper objectMapper = new ObjectMapper();

    public void appModFunc(Db1Service db1Service, Db2Service db2Service,
                           SaasService saasService, DbHiveDishService dbHiveDishService, EduSchoolService eduSchoolService, DbHiveService dbHiveService) {
        // 固定Dto层
        List<AppCommonDao> sourceDao = null;
        AppCommonDao pageTotal = null;
//		List<LinkedHashMap<String, Object>> dataList = new ArrayList();
        LinkedHashMap<String, Object> data = new LinkedHashMap<String, Object>();
        // 业务操作
        try {
            Map<String, String> date = getDate();
            String startDate = date.get("startDate");
            String endDate = date.get("endDate");
            String nowDate = date.get("nowDate");
//            String startDate = "2017-10-09";//测试参数
//            String endDate = "2017-10-15";//测试参数
//            String nowDate = "2017-10-16";//测试参数
            // ==================================第一个报表==================================

            // -----------------排菜使用率部分-------------------
            // 查询排菜使用率(同一区，同一日期，同一学制有多条记录)
            List<DishOperateRateInfo> dishOperateRateList = dbHiveDishService.getDishOperateRateList(startDate, endDate);
            // 重组后的排菜使用率集合(同一区，同一日期，同一学制有一条记录)
            List<DishOperateRateInfo> resetDishOperateRateList = getResetDishOperateRateList(dishOperateRateList);
            // 重组后的排菜使用率小计集合(用一区，同一日期只包含小计)
            List<DishOperateRateInfo> resetTotalDishOperateRateList = getResetTotalDishOperateRateList(resetDishOperateRateList);
            // 重组后的排菜使用率按学制类型的集合(同一区，同一学制，日期为空的合计)
            List<DishOperateRateInfo> schTypeDishOperateRateList = getSchTypeDishOperateRateList(resetDishOperateRateList);
            // 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
            List<DishOperateRateInfo> totalDishOperateRateList = getTotalDishOperateRateList(resetTotalDishOperateRateList);
            // 重组后的排菜使用率（集合+小计）
            List<DishOperateRateInfo> resetListAndTotalDishOperateRateList = new ArrayList<>();
            resetListAndTotalDishOperateRateList.addAll(resetDishOperateRateList);
            resetListAndTotalDishOperateRateList.addAll(resetTotalDishOperateRateList);

            // -----------------排菜准确率部分-------------------
            // 查询排菜准确率(同一区，同一日期，同一学制有多条记录)
            List<DishOperateRateInfo> dishCorrectRateList = dbHiveDishService.getDishCorrectRateList(startDate, endDate);
            // 重组后的排菜准确率集合(同一区，同一日期，同一学制有一条记录)
            List<DishOperateRateInfo> resetDishCorrectRateList = getResetDishCorrectRateList(dishCorrectRateList);
            // 重组后的排菜准确率小计集合(用一区，同一日期只包含小计)
            List<DishOperateRateInfo> resetTotalDishCorrectRateList = getResetTotalDishCorrectRateList(resetDishCorrectRateList);
            // 重组后的排菜准确率按学制类型的集合(同一区，同一学制，日期为空的合计)
            List<DishOperateRateInfo> schTypeDishCorrectRateList = getSchTypeDishCorrectRateList(resetDishCorrectRateList);
            // 重组后的排菜准确率总的排菜率集合（其实每个区只有一条记录）
            List<DishOperateRateInfo> totalDishCorrectRateList = getTotalDishCorrectRateList(resetTotalDishCorrectRateList);
            // 重组后的排菜准确率（集合+小计）
            List<DishOperateRateInfo> resetListAndTotalDishCorrectRateList = new ArrayList<>();
            resetListAndTotalDishCorrectRateList.addAll(resetDishCorrectRateList);
            resetListAndTotalDishCorrectRateList.addAll(resetTotalDishCorrectRateList);

            // -----------------排菜率和准确率整合部分-------------------
            // 整合后的排菜使用率和正确率list
            List<DishOperateRateInfo> dishOperateRateInfos = getDishOperateRateInfoList(resetListAndTotalDishOperateRateList,resetListAndTotalDishCorrectRateList);


            // ==================================第二个报表==================================

            // -----------------验收使用率部分-------------------
            // 查询排菜使用率(同一区，同一日期，同一学制有多条记录)
            List<CheckOperateRateInfo> checkOperateRateList = dbHiveDishService.getCheckOperateRateList(startDate, endDate);
            // 重组后的排菜使用率集合(同一区，同一日期，同一学制有一条记录)
            List<CheckOperateRateInfo> resetCheckOperateRateList = getResetCheckOperateRateList(checkOperateRateList);
            // 重组后的排菜使用率小计集合(用一区，同一日期只包含小计)
            List<CheckOperateRateInfo> resetTotalCheckOperateRateList = getResetTotalCheckOperateRateList(resetCheckOperateRateList);
            // 重组后的排菜使用率按学制类型的集合(同一区，同一学制，日期为空的合计)
            List<CheckOperateRateInfo> schTypeCheckOperateRateList = getSchTypeCheckOperateRateList(resetCheckOperateRateList);
            // 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
            List<CheckOperateRateInfo> totalCheckOperateRateList = getTotalCheckOperateRateList(resetTotalCheckOperateRateList);
            // 重组后的排菜使用率（集合+小计）
            List<CheckOperateRateInfo> resetListAndTotalCheckOperateRateList = new ArrayList<>();
            resetListAndTotalCheckOperateRateList.addAll(resetCheckOperateRateList);
            resetListAndTotalCheckOperateRateList.addAll(resetTotalCheckOperateRateList);

            // -----------------验收准确率部分-------------------
            // 查询排菜准确率(同一区，同一日期，同一学制有多条记录)
            List<CheckOperateRateInfo> checkCorrectRateList = dbHiveDishService.getCheckCorrectRateList(startDate, endDate);
            // 重组后的排菜准确率集合(同一区，同一日期，同一学制有一条记录)
            List<CheckOperateRateInfo> resetCheckCorrectRateList = getResetCheckCorrectRateList(checkCorrectRateList);
            // 重组后的排菜准确率小计集合(用一区，同一日期只包含小计)
            List<CheckOperateRateInfo> resetTotalCheckCorrectRateList = getResetTotalCheckCorrectRateList(resetCheckCorrectRateList);
            // 重组后的排菜准确率按学制类型的集合(同一区，同一学制，日期为空的合计)
            List<CheckOperateRateInfo> schTypeCheckCorrectRateList = getSchTypeCheckCorrectRateList(resetCheckCorrectRateList);
            // 重组后的排菜准确率总的排菜率集合（其实每个区只有一条记录）
            List<CheckOperateRateInfo> totalCheckCorrectRateList = getTotalCheckCorrectRateList(resetTotalCheckCorrectRateList);
            // 重组后的排菜准确率（集合+小计）
            List<CheckOperateRateInfo> resetListAndTotalCheckCorrectRateList = new ArrayList<>();
            resetListAndTotalCheckCorrectRateList.addAll(resetCheckCorrectRateList);
            resetListAndTotalCheckCorrectRateList.addAll(resetTotalCheckCorrectRateList);

            // -----------------验收使用率和准确率整合部分-------------------
            // 整合后的验收使用率和正确率list
            List<CheckOperateRateInfo> checkOperateRateInfos = getCheckOperateRateInfoList(resetListAndTotalCheckOperateRateList, resetListAndTotalCheckCorrectRateList);


            // ==================================第三个报表==================================

            // -----------------app使用率率部分-------------------
            // 查询排菜使用率(同一区，同一日期，同一学制有多条记录)
            List<AppOperateRateInfo> appOperateRateList = dbHiveDishService.getAppOperateRateList(startDate, endDate);
            // 重组后的排菜使用率集合(同一区，同一日期，同一学制有一条记录)
            List<AppOperateRateInfo> resetAppOperateRateList = getResetAppOperateRateList(appOperateRateList);
            // 重组后的排菜使用率小计集合(用一区，同一日期只包含小计)
            List<AppOperateRateInfo> resetTotalAppOperateRateList = getResetTotalAppOperateRateList(resetAppOperateRateList);
            // 重组后的排菜使用率按学制类型的集合(同一区，同一学制，日期为空的合计)
            List<AppOperateRateInfo> schTypeAppOperateRateList = getSchTypeAppOperateRateList(resetAppOperateRateList);
            // 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
            List<AppOperateRateInfo> totalAppOperateRateList = getTotalAppOperateRateList(resetTotalAppOperateRateList);
            // 重组后的排菜使用率（集合+小计）
            List<AppOperateRateInfo> resetListAndTotalAppOperateRateList = new ArrayList<>();
            resetListAndTotalAppOperateRateList.addAll(resetAppOperateRateList);
            resetListAndTotalAppOperateRateList.addAll(resetTotalAppOperateRateList);

            // -----------------app使用率整合部分-------------------
            // 这里只有一个不用整合，直接接收
            List<AppOperateRateInfo> appOperateRateInfos = resetListAndTotalAppOperateRateList;

            //所有16个区的数据按区整合在一起
            List<Map<String, Object>> totalListByArea = getTotalListByArea(sourceDao, db1Service,
                    dishOperateRateInfos, checkOperateRateInfos, appOperateRateInfos,
                    schTypeDishOperateRateList,totalDishOperateRateList,
                    schTypeDishCorrectRateList,totalDishCorrectRateList,
                    schTypeCheckOperateRateList,totalCheckOperateRateList,
                    schTypeCheckCorrectRateList,totalCheckCorrectRateList,
                    schTypeAppOperateRateList,totalAppOperateRateList);
            for (Map<String, Object> strObjMap : totalListByArea) {
                //进行赋值
                Map<String, Object> o = new HashedMap();
                //排菜通用key
                String key1 = "pcczl_";
                //验收通用key
                String key2 = "ysczl_";
                //app通用key
                String key3 = "app_";


                //查询开始时间
                o.put(key1 + "start",startDate);
                o.put(key2 + "start",startDate);
                o.put(key3 + "start",startDate);
                //查询结束时间
                o.put(key1 + "end",endDate);
                o.put(key2 + "end",endDate);
                o.put(key3 + "end",endDate);
                //报告生成日期
                o.put("bgscrq",nowDate);
                o.put("bgzq_start",startDate);
                o.put("bgzq_end",endDate);

                String committeeName = String.valueOf(strObjMap.get("committeeName"));
                String committeeCode = String.valueOf(strObjMap.get("committeeCode"));
                //教育局
                o.put("jyj",committeeName);
                o.put("jyjcode",committeeCode);

                String[] schTypes = new String[]{"幼托","中小学","其他","合计"};

                //一个区的排菜数据(详细列表)
                List<DishOperateRateInfo> dishDetailList = (List<DishOperateRateInfo>)strObjMap.get("dishDetailList");
                //获取日期
                List<String> dateList = dishDetailList.stream().map(DishOperateRateInfo::getDishDate).collect(Collectors.toList());
                //去重
                List<String> distinctList = dateList.stream().distinct().collect(Collectors.toList());
                //升序排序
                List<String> sortedList = distinctList.stream().sorted().collect(Collectors.toList());

                for (int i = 0; i < sortedList.size(); i++) {
                    //左边日期赋值.
                    String key11 = key1 + (i + 1);
                    o.put(key11,sortedList.get(i));
                    for (String schType : schTypes) {
                        for (DishOperateRateInfo dishOperateRateInfo : dishDetailList) {
                            if (sortedList.get(i).equals(dishOperateRateInfo.getDishDate()) && schType.equals(dishOperateRateInfo.getSchType())){
                                String key111 = "";
                                if ("幼托".equals(dishOperateRateInfo.getSchType())){
                                    key111 = key11 + "_1";
                                }else if ("中小学".equals(dishOperateRateInfo.getSchType())){
                                    key111 = key11 + "_2";
                                }else if ("其他".equals(dishOperateRateInfo.getSchType())){
                                    key111 = key11 + "_3";
                                }else if ("合计".equals(dishOperateRateInfo.getSchType())){
                                    key111 = key11 + "_4";
                                }
                                o.put(key111 + "_1",dishOperateRateInfo.getNeedDishSchoolNum());
                                o.put(key111 + "_2",dishOperateRateInfo.getHaveDishSchoolNum());
                                o.put(key111 + "_3",dishOperateRateInfo.getNoDishSchoolNum());
                                o.put(key111 + "_4",percent(dishOperateRateInfo.getDishOperateRate()));
                                o.put(key111 + "_5",dishOperateRateInfo.getDishNum());
                                o.put(key111 + "_6",dishOperateRateInfo.getDishCorrectNum());
                                o.put(key111 + "_7",dishOperateRateInfo.getDishUnCorrectNum());
                                o.put(key111 + "_8",percent(dishOperateRateInfo.getDishCorrectRate()));
                                break;
                            }
                        }
                    }
                }


                //一个区的验收数据
                List<CheckOperateRateInfo> checkDetailList = (List<CheckOperateRateInfo>)strObjMap.get("checkDetailList");
                //获取日期
                List<String> dateList2 = checkDetailList.stream().map(CheckOperateRateInfo::getDishDate).collect(Collectors.toList());
                //去重
                List<String> distinctList2 = dateList2.stream().distinct().collect(Collectors.toList());
                //升序排序
                List<String> sortedList2 = distinctList2.stream().sorted().collect(Collectors.toList());

                for (int i = 0; i < sortedList2.size(); i++) {
                    //左边日期赋值.
                    String key22 = key2 + (i + 1);
                    o.put(key22, sortedList2.get(i));
                    for (String schType : schTypes) {
                        for (CheckOperateRateInfo dishOperateRateInfo : checkDetailList) {
                            if (sortedList2.get(i).equals(dishOperateRateInfo.getDishDate()) && schType.equals(dishOperateRateInfo.getSchType())) {
                                String key222 = "";
                                if ("幼托".equals(dishOperateRateInfo.getSchType())) {
                                    key222 = key22 + "_1";
                                } else if ("中小学".equals(dishOperateRateInfo.getSchType())) {
                                    key222 = key22 + "_2";
                                } else if ("其他".equals(dishOperateRateInfo.getSchType())) {
                                    key222 = key22 + "_3";
                                } else if ("合计".equals(dishOperateRateInfo.getSchType())) {
                                    key222 = key22 + "_4";
                                }
                                o.put(key222 + "_1", dishOperateRateInfo.getNeedCheckSchoolNum());
                                o.put(key222 + "_2", dishOperateRateInfo.getHaveCheckSchoolNum());
                                o.put(key222 + "_3", dishOperateRateInfo.getNoCheckSchoolNum());
                                o.put(key222 + "_4", percent(dishOperateRateInfo.getCheckOperateRate()));
                                o.put(key222 + "_5", dishOperateRateInfo.getMaterialNum());
                                o.put(key222 + "_6", dishOperateRateInfo.getCheckCorrectNum());
                                o.put(key222 + "_7", dishOperateRateInfo.getCheckUnCorrectNum());
                                o.put(key222 + "_8", percent(dishOperateRateInfo.getCheckCorrectRate()));
                                break;
                            }
                        }
                    }
                }

                //一个区的app数据
                List<AppOperateRateInfo> appDetailList = (List<AppOperateRateInfo>)strObjMap.get("appDetailList");
                //获取日期
                List<String> dateList3 = appDetailList.stream().map(AppOperateRateInfo::getDishDate).collect(Collectors.toList());
                //去重
                List<String> distinctList3 = dateList3.stream().distinct().collect(Collectors.toList());
                //升序排序
                List<String> sortedList3 = distinctList3.stream().sorted().collect(Collectors.toList());

                for (int i = 0; i < sortedList3.size(); i++) {
                    //左边日期赋值.
                    String key33 = key3 + (i + 1);
                    o.put(key33,sortedList3.get(i));
                    for (String schType : schTypes) {
                        for (AppOperateRateInfo dishOperateRateInfo : appDetailList) {
                            if (sortedList3.get(i).equals(dishOperateRateInfo.getDishDate()) && schType.equals(dishOperateRateInfo.getSchType())) {
                                String key333 = "";
                                if ("幼托".equals(dishOperateRateInfo.getSchType())) {
                                    key333 = key33 + "_1";
                                } else if ("中小学".equals(dishOperateRateInfo.getSchType())) {
                                    key333 = key33 + "_2";
                                } else if ("其他".equals(dishOperateRateInfo.getSchType())) {
                                    key333 = key33 + "_3";
                                } else if ("合计".equals(dishOperateRateInfo.getSchType())) {
                                    key333 = key33 + "_4";
                                }
                                o.put(key333 + "_1", dishOperateRateInfo.getNeedOrderNum());
                                o.put(key333 + "_2", dishOperateRateInfo.getHaveOrderNum());
                                o.put(key333 + "_3", dishOperateRateInfo.getNoOrderNum());
                                o.put(key333 + "_4", percent(dishOperateRateInfo.getAppOperateRate()));
                                break;
                            }
                        }
                    }
                }

                //学制通用key
                String schType_key = "";
                //学校数量通用key
                String schType_xxsl_key = "";

                //一个区的排菜使用率数据(按学制)
                List<DishOperateRateInfo> dishSchTypeOperateTotalList = (List<DishOperateRateInfo>) strObjMap.get("dishSchTypeOperateTotalList");
                for (DishOperateRateInfo dishOperateRateInfo : dishSchTypeOperateTotalList) {
                    if ("幼托".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="pcczl_yt_xxsl";
                        schType_key = "pcczl_yt_pcczl";
                    }else if ("中小学".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="pcczl_zxx_xxsl";
                        schType_key = "pcczl_zxx_pcczl";
                    }else if ("其他".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="pcczl_qt_xxsl";
                        schType_key = "pcczl_qt_pcczl";
                    }
                    o.put(schType_xxsl_key,dishOperateRateInfo.getNeedDishSchoolNum()/7);
                    o.put(schType_key,percent(dishOperateRateInfo.getDishOperateRate()));
                }

                //一个区的排菜准确率数据(按学制)
                List<DishOperateRateInfo> dishSchTypeCorrectTotalList = (List<DishOperateRateInfo>) strObjMap.get("dishSchTypeCorrectTotalList");
                for (DishOperateRateInfo dishOperateRateInfo : dishSchTypeCorrectTotalList) {
                    if ("幼托".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="pczql_yt_xxsl";
                        schType_key = "pczql_yt_pczql";
                    }else if ("中小学".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="pczql_zxx_xxsl";
                        schType_key = "pczql_zxx_pczql";
                    }else if ("其他".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="pczql_qt_xxsl";
                        schType_key = "pczql_qt_pczql";
                    }
//                    o.put(schType_xxsl_key,dishOperateRateInfo.getNeedDishSchoolNum()/7);
                    o.put(schType_key,percent(dishOperateRateInfo.getDishCorrectRate()));
                }

                //一个区的验收使用率数据(按学制)
                List<CheckOperateRateInfo> checkSchTypeOperateTotalList = (List<CheckOperateRateInfo>) strObjMap.get("checkSchTypeOperateTotalList");
                for (CheckOperateRateInfo dishOperateRateInfo : checkSchTypeOperateTotalList) {
                    if ("幼托".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="ysczl_yt_xxsl";
                        schType_key = "ysczl_yt_ysczl";
                    }else if ("中小学".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="ysczl_zxx_xxsl";
                        schType_key = "ysczl_zxx_ysczl";
                    }else if ("其他".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="ysczl_qt_xxsl";
                        schType_key = "ysczl_qt_ysczl";
                    }
                    o.put(schType_xxsl_key,dishOperateRateInfo.getNeedCheckSchoolNum()/7);
                    o.put(schType_key,percent(dishOperateRateInfo.getCheckOperateRate()));
                }

                //一个区的验收准确率数据(按学制)
                List<CheckOperateRateInfo> checkSchTypeCorrectTotalList = (List<CheckOperateRateInfo>) strObjMap.get("checkSchTypeCorrectTotalList");
                for (CheckOperateRateInfo dishOperateRateInfo : checkSchTypeCorrectTotalList) {
                    if ("幼托".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="yszql_yt_xxsl";
                        schType_key = "yszql_yt_yszql";
                    }else if ("中小学".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="yszql_zxx_xxsl";
                        schType_key = "yszql_zxx_yszql";
                    }else if ("其他".equals(dishOperateRateInfo.getSchType())){
                        schType_xxsl_key ="yszql_qt_xxsl";
                        schType_key = "yszql_qt_yszql";
                    }
//                    o.put(schType_xxsl_key,dishOperateRateInfo.getNeedCheckSchoolNum()/7);
                    o.put(schType_key,percent(dishOperateRateInfo.getCheckCorrectRate()));
                }

                //一个区的app使用率数据(按学制)
                List<AppOperateRateInfo> appSchTypeOperateTotalList = (List<AppOperateRateInfo>) strObjMap.get("appSchTypeOperateTotalList");
                for (AppOperateRateInfo dishOperateRateInfo : appSchTypeOperateTotalList) {
                    if ("幼托".equals(dishOperateRateInfo.getSchType())){
                        schType_key = "app_yt_syl";
                    }else if ("中小学".equals(dishOperateRateInfo.getSchType())){
                        schType_key = "app_zxx_syl";
                    }else if ("其他".equals(dishOperateRateInfo.getSchType())){
                        schType_key = "app_qt_syl";
                    }
                    o.put(schType_key,percent(dishOperateRateInfo.getAppOperateRate()));
                }

                //一个区的排菜使用率数据(汇总)
                List<DishOperateRateInfo> dishOperateTotalList = (List<DishOperateRateInfo>) strObjMap.get("dishOperateTotalList");
                if (CollectionUtils.isNotEmpty(dishOperateTotalList)){
                    DishOperateRateInfo dishOperateRateInfo = dishOperateTotalList.get(0);
                    o.put("pcczl_total",percent(dishOperateRateInfo.getDishOperateRate()));
                    o.put("pcczl_xxsl_total",dishOperateRateInfo.getNeedDishSchoolNum()/7);
                }

                //一个区的排菜准确率数据(汇总)
                List<DishOperateRateInfo> dishCorrectTotalList = (List<DishOperateRateInfo>) strObjMap.get("dishCorrectTotalList");
                if (CollectionUtils.isNotEmpty(dishCorrectTotalList)) {
                    DishOperateRateInfo dishOperateRateInfo1 = dishCorrectTotalList.get(0);
                    o.put("pczql_total", percent(dishOperateRateInfo1.getDishCorrectRate()));
                }

                //一个区的验收使用率数据(汇总)
                List<CheckOperateRateInfo> checkOperateTotalList = (List<CheckOperateRateInfo>) strObjMap.get("checkOperateTotalList");
                if (CollectionUtils.isNotEmpty(checkOperateTotalList)) {
                    CheckOperateRateInfo checkOperateRateInfo = checkOperateTotalList.get(0);
                    o.put("ysczl_total", percent(checkOperateRateInfo.getCheckOperateRate()));
                    o.put("ysczl_xxsl_total", checkOperateRateInfo.getNeedCheckSchoolNum() / 7);
                }

                //一个区的验收准确率数据(汇总)
                List<CheckOperateRateInfo> checkCorrectTotalList = (List<CheckOperateRateInfo>) strObjMap.get("checkCorrectTotalList");
                if (CollectionUtils.isNotEmpty(checkCorrectTotalList)) {
                    CheckOperateRateInfo checkOperateRateInfo1 = checkCorrectTotalList.get(0);
                    o.put("yszql_total", percent(checkOperateRateInfo1.getCheckCorrectRate()));
                }

                //一个区的app使用率数据(汇总)
                List<AppOperateRateInfo> appOperateTotalList = (List<AppOperateRateInfo>) strObjMap.get("appOperateTotalList");
                if (CollectionUtils.isNotEmpty(appOperateTotalList)) {
                    AppOperateRateInfo appOperateRateInfo = appOperateTotalList.get(0);
                    o.put("app_total", percent(appOperateRateInfo.getAppOperateRate()));
                    o.put("app_xxsl_total", appOperateRateInfo.getNeedOrderNum() / 7);
                }


                Map<String,Object> dataMap = new HashedMap();
                dataMap.put("dataMap",o);
                //导出pdf
                exportPdf(db1Service,dataMap,committeeCode,committeeName,nowDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @Description: 使用率和排菜率百分比
     * @Param: [haveNum, needNum] 
     * @return: java.lang.String
     * @Author: jianghy
     * @Date: 2020/1/3
     * @Time: 14:20       
     */
    public String percent(BigDecimal rate){
        String result = "0%";
        if(rate != null) {
            rate = rate.multiply(new BigDecimal(100));
            rate = rate.setScale(2, BigDecimal.ROUND_HALF_UP);
            result = rate.toString() + "%";
        }
        return result;
    }


    /**
     * @Description: 重组后的排菜使用率集合
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getResetDishOperateRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setNeedDishSchoolNum(dishOperateRateInfo.getNeedDishSchoolNum() + dishOperateRateRe.getNeedDishSchoolNum());
                        dishOperateRateRe.setHaveDishSchoolNum(dishOperateRateInfo.getHaveDishSchoolNum() + dishOperateRateRe.getHaveDishSchoolNum());
                        dishOperateRateRe.setNoDishSchoolNum(dishOperateRateInfo.getNoDishSchoolNum() + dishOperateRateRe.getNoDishSchoolNum());
                        if(dishOperateRateRe.getNeedDishSchoolNum() != 0){
                            dishOperateRateRe.setDishOperateRate(new BigDecimal(dishOperateRateRe.getHaveDishSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedDishSchoolNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getTotalDishOperateRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(总的合计)
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())) {
                        dishOperateRateRe.setNeedDishSchoolNum(dishOperateRateInfo.getNeedDishSchoolNum() + dishOperateRateRe.getNeedDishSchoolNum());
                        dishOperateRateRe.setHaveDishSchoolNum(dishOperateRateInfo.getHaveDishSchoolNum() + dishOperateRateRe.getHaveDishSchoolNum());
                        if (dishOperateRateRe.getNeedDishSchoolNum() != 0) {
                            dishOperateRateRe.setDishOperateRate(new BigDecimal(dishOperateRateRe.getHaveDishSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedDishSchoolNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }

    
    /** 
     * @Description: 重组后的排菜使用率(同一区，同一学制，日期为空的合计)
     * @Param: [dishOperateRateList] 
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo> 
     * @Author: jianghy 
     * @Date: 2019/12/31
     * @Time: 9:14       
     */
    public List<DishOperateRateInfo> getSchTypeDishOperateRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(按学制的合计)
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有学制合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setNeedDishSchoolNum(dishOperateRateInfo.getNeedDishSchoolNum() + dishOperateRateRe.getNeedDishSchoolNum());
                        dishOperateRateRe.setHaveDishSchoolNum(dishOperateRateInfo.getHaveDishSchoolNum() + dishOperateRateRe.getHaveDishSchoolNum());
                        if (dishOperateRateRe.getNeedDishSchoolNum() != 0) {
                            dishOperateRateRe.setDishOperateRate(new BigDecimal(dishOperateRateRe.getHaveDishSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedDishSchoolNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合(小合计的集合)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getResetTotalDishOperateRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(小合计的集合)
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                DishOperateRateInfo newDishOperateRateInfo = new DishOperateRateInfo();
                BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                newDishOperateRateInfo.setSchType("合计");
                dishOperateRateRes.add(newDishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals("合计")) {
                        dishOperateRateRe.setNeedDishSchoolNum(dishOperateRateInfo.getNeedDishSchoolNum() + dishOperateRateRe.getNeedDishSchoolNum());
                        dishOperateRateRe.setHaveDishSchoolNum(dishOperateRateInfo.getHaveDishSchoolNum() + dishOperateRateRe.getHaveDishSchoolNum());
                        dishOperateRateRe.setNoDishSchoolNum(dishOperateRateInfo.getNoDishSchoolNum() + dishOperateRateRe.getNoDishSchoolNum());
                        if (dishOperateRateRe.getNeedDishSchoolNum() != 0) {
                            dishOperateRateRe.setDishOperateRate(new BigDecimal(dishOperateRateRe.getHaveDishSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedDishSchoolNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    DishOperateRateInfo newDishOperateRateInfo = new DishOperateRateInfo();
                    BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                    newDishOperateRateInfo.setSchType("合计");
                    dishOperateRateRes.add(newDishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getResetDishCorrectRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setDishCorrectNum(dishOperateRateInfo.getDishCorrectNum() + dishOperateRateRe.getDishCorrectNum());
                        dishOperateRateRe.setDishUnCorrectNum(dishOperateRateInfo.getDishUnCorrectNum() + dishOperateRateRe.getDishUnCorrectNum());
                        dishOperateRateRe.setDishNum(dishOperateRateInfo.getDishNum() + dishOperateRateRe.getDishNum());
                        if(dishOperateRateRe.getDishNum() != 0){
                            dishOperateRateRe.setDishCorrectRate(new BigDecimal(dishOperateRateRe.getDishCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getDishNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getTotalDishCorrectRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(总的合计)
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())) {
                        dishOperateRateRe.setDishCorrectNum(dishOperateRateInfo.getDishCorrectNum() + dishOperateRateRe.getDishCorrectNum());
                        dishOperateRateRe.setDishUnCorrectNum(dishOperateRateInfo.getDishUnCorrectNum() + dishOperateRateRe.getDishUnCorrectNum());
                        dishOperateRateRe.setDishNum(dishOperateRateInfo.getDishNum() + dishOperateRateRe.getDishNum());
                        if(dishOperateRateRe.getDishNum() != 0){
                            dishOperateRateRe.setDishCorrectRate(new BigDecimal(dishOperateRateRe.getDishCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getDishNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率(同一区，同一学制，日期为空的合计)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getSchTypeDishCorrectRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(按学制的合计)
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有学制合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setDishCorrectNum(dishOperateRateInfo.getDishCorrectNum() + dishOperateRateRe.getDishCorrectNum());
                        dishOperateRateRe.setDishUnCorrectNum(dishOperateRateInfo.getDishUnCorrectNum() + dishOperateRateRe.getDishUnCorrectNum());
                        dishOperateRateRe.setDishNum(dishOperateRateInfo.getDishNum() + dishOperateRateRe.getDishNum());
                        if(dishOperateRateRe.getDishNum() != 0){
                            dishOperateRateRe.setDishCorrectRate(new BigDecimal(dishOperateRateRe.getDishCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getDishNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合(小合计的集合)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<DishOperateRateInfo> getResetTotalDishCorrectRateList(List<DishOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(小合计的集合)
        List<DishOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                DishOperateRateInfo newDishOperateRateInfo = new DishOperateRateInfo();
                BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                newDishOperateRateInfo.setSchType("合计");
                dishOperateRateRes.add(newDishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (DishOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals("合计")) {
                        dishOperateRateRe.setDishCorrectNum(dishOperateRateInfo.getDishCorrectNum() + dishOperateRateRe.getDishCorrectNum());
                        dishOperateRateRe.setDishUnCorrectNum(dishOperateRateInfo.getDishUnCorrectNum() + dishOperateRateRe.getDishUnCorrectNum());
                        dishOperateRateRe.setDishNum(dishOperateRateInfo.getDishNum() + dishOperateRateRe.getDishNum());
                        if(dishOperateRateRe.getDishNum() != 0){
                            dishOperateRateRe.setDishCorrectRate(new BigDecimal(dishOperateRateRe.getDishCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getDishNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    DishOperateRateInfo newDishOperateRateInfo = new DishOperateRateInfo();
                    BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                    newDishOperateRateInfo.setSchType("合计");
                    dishOperateRateRes.add(newDishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }



    /**
     * @Description: 重组后的排菜使用率集合
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getResetCheckOperateRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setNeedCheckSchoolNum(dishOperateRateInfo.getNeedCheckSchoolNum() + dishOperateRateRe.getNeedCheckSchoolNum());
                        dishOperateRateRe.setHaveCheckSchoolNum(dishOperateRateInfo.getHaveCheckSchoolNum() + dishOperateRateRe.getHaveCheckSchoolNum());
                        dishOperateRateRe.setNoCheckSchoolNum(dishOperateRateInfo.getNoCheckSchoolNum() + dishOperateRateRe.getNoCheckSchoolNum());
                        if(dishOperateRateRe.getNeedCheckSchoolNum() != 0){
                            dishOperateRateRe.setCheckOperateRate(new BigDecimal(dishOperateRateRe.getHaveCheckSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedCheckSchoolNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getTotalCheckOperateRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(总的合计)
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())) {
                        dishOperateRateRe.setNeedCheckSchoolNum(dishOperateRateInfo.getNeedCheckSchoolNum() + dishOperateRateRe.getNeedCheckSchoolNum());
                        dishOperateRateRe.setHaveCheckSchoolNum(dishOperateRateInfo.getHaveCheckSchoolNum() + dishOperateRateRe.getHaveCheckSchoolNum());
                        dishOperateRateRe.setNoCheckSchoolNum(dishOperateRateInfo.getNoCheckSchoolNum() + dishOperateRateRe.getNoCheckSchoolNum());
                        if(dishOperateRateRe.getNeedCheckSchoolNum() != 0){
                            dishOperateRateRe.setCheckOperateRate(new BigDecimal(dishOperateRateRe.getHaveCheckSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedCheckSchoolNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率(同一区，同一学制，日期为空的合计)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getSchTypeCheckOperateRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(按学制的合计)
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有学制合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setNeedCheckSchoolNum(dishOperateRateInfo.getNeedCheckSchoolNum() + dishOperateRateRe.getNeedCheckSchoolNum());
                        dishOperateRateRe.setHaveCheckSchoolNum(dishOperateRateInfo.getHaveCheckSchoolNum() + dishOperateRateRe.getHaveCheckSchoolNum());
                        dishOperateRateRe.setNoCheckSchoolNum(dishOperateRateInfo.getNoCheckSchoolNum() + dishOperateRateRe.getNoCheckSchoolNum());
                        if(dishOperateRateRe.getNeedCheckSchoolNum() != 0){
                            dishOperateRateRe.setCheckOperateRate(new BigDecimal(dishOperateRateRe.getHaveCheckSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedCheckSchoolNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合(小合计的集合)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getResetTotalCheckOperateRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(小合计的集合)
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                CheckOperateRateInfo newDishOperateRateInfo = new CheckOperateRateInfo();
                BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                newDishOperateRateInfo.setSchType("合计");
                dishOperateRateRes.add(newDishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals("合计")) {
                        dishOperateRateRe.setNeedCheckSchoolNum(dishOperateRateInfo.getNeedCheckSchoolNum() + dishOperateRateRe.getNeedCheckSchoolNum());
                        dishOperateRateRe.setHaveCheckSchoolNum(dishOperateRateInfo.getHaveCheckSchoolNum() + dishOperateRateRe.getHaveCheckSchoolNum());
                        dishOperateRateRe.setNoCheckSchoolNum(dishOperateRateInfo.getNoCheckSchoolNum() + dishOperateRateRe.getNoCheckSchoolNum());
                        if(dishOperateRateRe.getNeedCheckSchoolNum() != 0){
                            dishOperateRateRe.setCheckOperateRate(new BigDecimal(dishOperateRateRe.getHaveCheckSchoolNum()).divide(new BigDecimal(dishOperateRateRe.getNeedCheckSchoolNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    CheckOperateRateInfo newDishOperateRateInfo = new CheckOperateRateInfo();
                    BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                    newDishOperateRateInfo.setSchType("合计");
                    dishOperateRateRes.add(newDishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getResetCheckCorrectRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setCheckCorrectNum(dishOperateRateInfo.getCheckCorrectNum() + dishOperateRateRe.getCheckCorrectNum());
                        dishOperateRateRe.setCheckUnCorrectNum(dishOperateRateInfo.getCheckUnCorrectNum() + dishOperateRateRe.getCheckUnCorrectNum());
                        dishOperateRateRe.setMaterialNum(dishOperateRateInfo.getMaterialNum() + dishOperateRateRe.getMaterialNum());
                        if(dishOperateRateRe.getMaterialNum() != 0){
                            dishOperateRateRe.setCheckCorrectRate(new BigDecimal(dishOperateRateRe.getCheckCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getMaterialNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getTotalCheckCorrectRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(总的合计)
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())) {
                        dishOperateRateRe.setCheckCorrectNum(dishOperateRateInfo.getCheckCorrectNum() + dishOperateRateRe.getCheckCorrectNum());
                        dishOperateRateRe.setCheckUnCorrectNum(dishOperateRateInfo.getCheckUnCorrectNum() + dishOperateRateRe.getCheckUnCorrectNum());
                        dishOperateRateRe.setMaterialNum(dishOperateRateInfo.getMaterialNum() + dishOperateRateRe.getMaterialNum());
                        if(dishOperateRateRe.getMaterialNum() != 0){
                            dishOperateRateRe.setCheckCorrectRate(new BigDecimal(dishOperateRateRe.getCheckCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getMaterialNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率(同一区，同一学制，日期为空的合计)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getSchTypeCheckCorrectRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(按学制的合计)
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有学制合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setCheckCorrectNum(dishOperateRateInfo.getCheckCorrectNum() + dishOperateRateRe.getCheckCorrectNum());
                        dishOperateRateRe.setCheckUnCorrectNum(dishOperateRateInfo.getCheckUnCorrectNum() + dishOperateRateRe.getCheckUnCorrectNum());
                        dishOperateRateRe.setMaterialNum(dishOperateRateInfo.getMaterialNum() + dishOperateRateRe.getMaterialNum());
                        if(dishOperateRateRe.getMaterialNum() != 0){
                            dishOperateRateRe.setCheckCorrectRate(new BigDecimal(dishOperateRateRe.getCheckCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getMaterialNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合(小合计的集合)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<CheckOperateRateInfo> getResetTotalCheckCorrectRateList(List<CheckOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(小合计的集合)
        List<CheckOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (CheckOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                CheckOperateRateInfo newDishOperateRateInfo = new CheckOperateRateInfo();
                BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                newDishOperateRateInfo.setSchType("合计");
                dishOperateRateRes.add(newDishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (CheckOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals("合计")) {
                        dishOperateRateRe.setCheckCorrectNum(dishOperateRateInfo.getCheckCorrectNum() + dishOperateRateRe.getCheckCorrectNum());
                        dishOperateRateRe.setCheckUnCorrectNum(dishOperateRateInfo.getCheckUnCorrectNum() + dishOperateRateRe.getCheckUnCorrectNum());
                        dishOperateRateRe.setMaterialNum(dishOperateRateInfo.getMaterialNum() + dishOperateRateRe.getMaterialNum());
                        if(dishOperateRateRe.getMaterialNum() != 0){
                            dishOperateRateRe.setCheckCorrectRate(new BigDecimal(dishOperateRateRe.getCheckCorrectNum()).divide(new BigDecimal(dishOperateRateRe.getMaterialNum()),4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    CheckOperateRateInfo newDishOperateRateInfo = new CheckOperateRateInfo();
                    BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                    newDishOperateRateInfo.setSchType("合计");
                    dishOperateRateRes.add(newDishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }



    /**
     * @Description: 重组后的排菜使用率集合
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<AppOperateRateInfo> getResetAppOperateRateList(List<AppOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果
        List<AppOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (AppOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (AppOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setNeedOrderNum(dishOperateRateInfo.getNeedOrderNum() + dishOperateRateRe.getNeedOrderNum());
                        dishOperateRateRe.setHaveOrderNum(dishOperateRateInfo.getHaveOrderNum() + dishOperateRateRe.getHaveOrderNum());
                        dishOperateRateRe.setNoOrderNum(dishOperateRateInfo.getNoOrderNum() + dishOperateRateRe.getNoOrderNum());
                        if (dishOperateRateRe.getNeedOrderNum() != 0) {
                            dishOperateRateRe.setAppOperateRate(new BigDecimal(dishOperateRateRe.getHaveOrderNum()).divide(new BigDecimal(dishOperateRateRe.getNeedOrderNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率总的排菜率集合（其实每个区只有一条记录）
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<AppOperateRateInfo> getTotalAppOperateRateList(List<AppOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(总的合计)
        List<AppOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (AppOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (AppOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())) {
                        dishOperateRateRe.setNeedOrderNum(dishOperateRateInfo.getNeedOrderNum() + dishOperateRateRe.getNeedOrderNum());
                        dishOperateRateRe.setHaveOrderNum(dishOperateRateInfo.getHaveOrderNum() + dishOperateRateRe.getHaveOrderNum());
                        dishOperateRateRe.setNoOrderNum(dishOperateRateInfo.getNoOrderNum() + dishOperateRateRe.getNoOrderNum());
                        if (dishOperateRateRe.getNeedOrderNum() != 0) {
                            dishOperateRateRe.setAppOperateRate(new BigDecimal(dishOperateRateRe.getHaveOrderNum()).divide(new BigDecimal(dishOperateRateRe.getNeedOrderNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率(同一区，同一学制，日期为空的合计)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<AppOperateRateInfo> getSchTypeAppOperateRateList(List<AppOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(按学制的合计)
        List<AppOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (AppOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                dishOperateRateRes.add(dishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (AppOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有学制合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getSchType().equals(dishOperateRateInfo.getSchType())) {
                        dishOperateRateRe.setNeedOrderNum(dishOperateRateInfo.getNeedOrderNum() + dishOperateRateRe.getNeedOrderNum());
                        dishOperateRateRe.setHaveOrderNum(dishOperateRateInfo.getHaveOrderNum() + dishOperateRateRe.getHaveOrderNum());
                        dishOperateRateRe.setNoOrderNum(dishOperateRateInfo.getNoOrderNum() + dishOperateRateRe.getNoOrderNum());
                        if (dishOperateRateRe.getNeedOrderNum() != 0) {
                            dishOperateRateRe.setAppOperateRate(new BigDecimal(dishOperateRateRe.getHaveOrderNum()).divide(new BigDecimal(dishOperateRateRe.getNeedOrderNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    dishOperateRateRes.add(dishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }


    /**
     * @Description: 重组后的排菜使用率集合(小合计的集合)
     * @Param: [dishOperateRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/31
     * @Time: 9:14
     */
    public List<AppOperateRateInfo> getResetTotalAppOperateRateList(List<AppOperateRateInfo> dishOperateRateList){
        //定义排菜报表结果(小合计的集合)
        List<AppOperateRateInfo> dishOperateRateRes = new ArrayList<>();
        for (AppOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
            if(dishOperateRateRes.isEmpty()){
                AppOperateRateInfo newDishOperateRateInfo = new AppOperateRateInfo();
                BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                newDishOperateRateInfo.setSchType("合计");
                dishOperateRateRes.add(newDishOperateRateInfo);
            }else {
                boolean isExists = false;
                for (AppOperateRateInfo dishOperateRateRe : dishOperateRateRes) {
                    //查看重组表里是否有合计记录，如果有就加起来
                    if (dishOperateRateRe.getArea().equals(dishOperateRateInfo.getArea())
                            && dishOperateRateRe.getDishDate().equals(dishOperateRateInfo.getDishDate())
                            && dishOperateRateRe.getSchType().equals("合计")) {
                        dishOperateRateRe.setNeedOrderNum(dishOperateRateInfo.getNeedOrderNum() + dishOperateRateRe.getNeedOrderNum());
                        dishOperateRateRe.setHaveOrderNum(dishOperateRateInfo.getHaveOrderNum() + dishOperateRateRe.getHaveOrderNum());
                        dishOperateRateRe.setNoOrderNum(dishOperateRateInfo.getNoOrderNum() + dishOperateRateRe.getNoOrderNum());
                        if (dishOperateRateRe.getNeedOrderNum() != 0) {
                            dishOperateRateRe.setAppOperateRate(new BigDecimal(dishOperateRateRe.getHaveOrderNum()).divide(new BigDecimal(dishOperateRateRe.getNeedOrderNum()), 4, BigDecimal.ROUND_HALF_UP));
                        }
                        isExists = true;
                        break;
                    }
                }
                //如果重组表里没有找到该合计记录，就把该记录当成重组后的记录
                if(!isExists){
                    AppOperateRateInfo newDishOperateRateInfo = new AppOperateRateInfo();
                    BeanUtils.copyProperties(dishOperateRateInfo,newDishOperateRateInfo);
                    newDishOperateRateInfo.setSchType("合计");
                    dishOperateRateRes.add(newDishOperateRateInfo);
                }
            }
        }
        return dishOperateRateRes;
    }



    /** 
     * @Description: 所有16个区的数据按区整合在一起 
     * @Param: [sourceDao, db1Service, dishOperateRateInfos, checkOperateRateInfos, appOperateRateInfos]
     * @return: java.util.List<java.util.Map<java.lang.String,java.lang.Object>> 
     * @Author: jianghy 
     * @Date: 2019/12/30
     * @Time: 14:32       
     */
    public List<Map<String,Object>> getTotalListByArea(List<AppCommonDao> sourceDao,Db1Service db1Service,
                                                       List<DishOperateRateInfo> dishOperateRateInfos,List<CheckOperateRateInfo> checkOperateRateInfos,List<AppOperateRateInfo> appOperateRateInfos,
                                                       List<DishOperateRateInfo> schTypeDishOperateRateList,List<DishOperateRateInfo> totalDishOperateRateList,
                                                       List<DishOperateRateInfo> schTypeDishCorrectRateList,List<DishOperateRateInfo> totalDishCorrectRateList,
                                                       List<CheckOperateRateInfo> schTypeCheckOperateRateList,List<CheckOperateRateInfo> totalCheckOperateRateList,
                                                       List<CheckOperateRateInfo> schTypeCheckCorrectRateList,List<CheckOperateRateInfo> totalCheckCorrectRateList,
                                                       List<AppOperateRateInfo> schTypeAppOperateRateList,List<AppOperateRateInfo> totalAppOperateRateList){
        //获取行政区划
        List<Map<String, String>> committeeList = getCommitteeList(db1Service);
        List<Map<String,Object>> totalList = new ArrayList<>();
        for (Map<String, String> committeeMap : committeeList) {
            String code = committeeMap.get("code");
            String name = committeeMap.get("name");
            Map<String,Object> commitMap = new HashedMap();
            commitMap.put("committeeCode",code);
            commitMap.put("committeeName",name);
            //排菜(详细列表)
            List<DishOperateRateInfo> dishDetailList = new ArrayList<>();
            for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateInfos) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    dishDetailList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("dishDetailList",dishDetailList);

            //验收(详细列表)
            List<CheckOperateRateInfo> checkDetailList = new ArrayList<>();
            for (CheckOperateRateInfo checkOperateRateInfo : checkOperateRateInfos) {
                if (code.equals(checkOperateRateInfo.getArea())){
                    checkDetailList.add(checkOperateRateInfo);
                }
            }
            commitMap.put("checkDetailList",checkDetailList);

            //app(详细列表)
            List<AppOperateRateInfo> appDetailList = new ArrayList<>();
            for (AppOperateRateInfo appOperateRateInfo : appOperateRateInfos) {
                if (code.equals(appOperateRateInfo.getArea())){
                    appDetailList.add(appOperateRateInfo);
                }
            }
            commitMap.put("appDetailList",appDetailList);

            //排菜（使用率部分）按学制汇总
            List<DishOperateRateInfo> dishSchTypeOperateList = new ArrayList<>();
            for (DishOperateRateInfo dishOperateRateInfo : schTypeDishOperateRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    dishSchTypeOperateList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("dishSchTypeOperateTotalList",dishSchTypeOperateList);

            //排菜（准确率率部分）按学制汇总
            List<DishOperateRateInfo> dishSchTypeCorrectList = new ArrayList<>();
            for (DishOperateRateInfo dishOperateRateInfo : schTypeDishCorrectRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    dishSchTypeCorrectList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("dishSchTypeCorrectTotalList",dishSchTypeCorrectList);

            //验收（使用率部分）按学制汇总
            List<CheckOperateRateInfo> checkSchTypeOperateList = new ArrayList<>();
            for (CheckOperateRateInfo dishOperateRateInfo : schTypeCheckOperateRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    checkSchTypeOperateList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("checkSchTypeOperateTotalList",checkSchTypeOperateList);

            //验收（准确率部分）按学制汇总
            List<CheckOperateRateInfo> checkSchTypeCorrectList = new ArrayList<>();
            for (CheckOperateRateInfo dishOperateRateInfo : schTypeCheckCorrectRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    checkSchTypeCorrectList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("checkSchTypeCorrectTotalList",checkSchTypeCorrectList);

            //app（app使用率部分）按学制汇总
            List<AppOperateRateInfo> appSchTypeOperateList = new ArrayList<>();
            for (AppOperateRateInfo dishOperateRateInfo : schTypeAppOperateRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    appSchTypeOperateList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("appSchTypeOperateTotalList",appSchTypeOperateList);


            //排菜（使用率部分）汇总
            List<DishOperateRateInfo> dishOperateTotalList = new ArrayList<>();
            for (DishOperateRateInfo dishOperateRateInfo : totalDishOperateRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    dishOperateTotalList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("dishOperateTotalList",dishOperateTotalList);

            //排菜（准确率率部分）汇总
            List<DishOperateRateInfo> dishCorrectTotalList = new ArrayList<>();
            for (DishOperateRateInfo dishOperateRateInfo : totalDishCorrectRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    dishCorrectTotalList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("dishCorrectTotalList",dishCorrectTotalList);

            //验收（使用率部分）汇总
            List<CheckOperateRateInfo> checkOperateTotalList = new ArrayList<>();
            for (CheckOperateRateInfo dishOperateRateInfo : totalCheckOperateRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    checkOperateTotalList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("checkOperateTotalList",checkOperateTotalList);

            //验收（准确率部分）汇总
            List<CheckOperateRateInfo> checkCorrectTotalList = new ArrayList<>();
            for (CheckOperateRateInfo dishOperateRateInfo : totalCheckCorrectRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    checkCorrectTotalList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("checkCorrectTotalList",checkCorrectTotalList);

            //app（app使用率部分）汇总
            List<AppOperateRateInfo> appOperateTotalList = new ArrayList<>();
            for (AppOperateRateInfo dishOperateRateInfo : totalAppOperateRateList) {
                if (code.equals(dishOperateRateInfo.getArea())){
                    appOperateTotalList.add(dishOperateRateInfo);
                }
            }
            commitMap.put("appOperateTotalList",appOperateTotalList);

            totalList.add(commitMap);
        }
        return totalList;
    }


    /** 
     * @Description: 获取16个区的排菜操作率整合列表
     * @Param: [dishOperatRateList, dishCorrectRateList] 
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo> 
     * @Author: jianghy 
     * @Date: 2019/12/29
     * @Time: 20:39       
     */
    public List<DishOperateRateInfo> getDishOperateRateInfoList(List<DishOperateRateInfo> dishOperateRateList,List<DishOperateRateInfo> dishCorrectRateList){
        List<DishOperateRateInfo> dishOperateRateInfos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(dishOperateRateList) && CollectionUtils.isNotEmpty(dishCorrectRateList)){
            for (DishOperateRateInfo dishOperateRateInfo : dishOperateRateList) {
                for (DishOperateRateInfo operateRateInfo : dishCorrectRateList) {
                    if (dishOperateRateInfo.getArea().equals(operateRateInfo.getArea())
                        && dishOperateRateInfo.getSchType().equals(operateRateInfo.getSchType())
                        && dishOperateRateInfo.getDishDate().equals(operateRateInfo.getDishDate())){
                        dishOperateRateInfo.setDishNum(operateRateInfo.getDishNum());
                        dishOperateRateInfo.setDishCorrectNum(operateRateInfo.getDishCorrectNum());
                        dishOperateRateInfo.setDishUnCorrectNum(operateRateInfo.getDishUnCorrectNum());
                        dishOperateRateInfo.setDishCorrectRate(operateRateInfo.getDishCorrectRate());
                        dishOperateRateInfos.add(dishOperateRateInfo);
                        break;
                    }
                }
            }
        }
        return dishOperateRateInfos;
    }


    /**
     * @Description: 获取16个区的排菜操作率整合列表
     * @Param: [dishOperatRateList, dishCorrectRateList]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dto.optanl.DishOperateRateInfo>
     * @Author: jianghy
     * @Date: 2019/12/29
     * @Time: 20:39
     */
    public List<CheckOperateRateInfo> getCheckOperateRateInfoList(List<CheckOperateRateInfo> checkOperateRateList,List<CheckOperateRateInfo> checkCorrectRateList){
        List<CheckOperateRateInfo> dishOperateRateInfos = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(checkOperateRateList) && CollectionUtils.isNotEmpty(checkCorrectRateList)){
            for (CheckOperateRateInfo checkOperateRateInfo : checkOperateRateList) {
                for (CheckOperateRateInfo operateRateInfo : checkCorrectRateList) {
                    if (checkOperateRateInfo.getArea().equals(operateRateInfo.getArea())
                            && checkOperateRateInfo.getSchType().equals(operateRateInfo.getSchType())
                            && checkOperateRateInfo.getDishDate().equals(operateRateInfo.getDishDate())){
                        checkOperateRateInfo.setMaterialNum(operateRateInfo.getMaterialNum());
                        checkOperateRateInfo.setCheckCorrectNum(operateRateInfo.getCheckCorrectNum());
                        checkOperateRateInfo.setCheckUnCorrectNum(operateRateInfo.getCheckUnCorrectNum());
                        checkOperateRateInfo.setCheckCorrectRate(operateRateInfo.getCheckCorrectRate());
                        dishOperateRateInfos.add(checkOperateRateInfo);
                        break;
                    }
                }
            }
        }
        return dishOperateRateInfos;
    }


    /** 
     * @Description: 获取指定的日期
     * @Param: [] 
     * @return: java.util.Map<java.lang.String,java.lang.String> 
     * @Author: jianghy 
     * @Date: 2019/12/28
     * @Time: 19:51       
     */
    public Map<String,String> getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        Calendar calendar1 = Calendar.getInstance();
        Calendar calendar2 = Calendar.getInstance();
        int dayOfWeek = calendar1.get(Calendar.DAY_OF_WEEK) - 1;
        int offset1 = 1 - dayOfWeek;
        int offset2 = 7 - dayOfWeek;
        calendar1.add(Calendar.DATE, offset1 - 7);
        calendar2.add(Calendar.DATE, offset2 - 7);
        String nowDate = sdf.format(calendar.getTime());
        String startDate = sdf.format(calendar1.getTime());
        String endDate = sdf.format(calendar2.getTime());
        Map<String,String> map = new HashedMap();
        map.put("nowDate",nowDate);
        map.put("startDate",startDate);
        map.put("endDate",endDate);
        return map;
    }


    /** 
     * @Description: 获取教育局列表
     * @Param: [sourceDao, db1Service]
     * @return: java.util.List<com.tfit.BdBiProcSrvShEduOmc.dao.AppCommonDao> 
     * @Author: jianghy 
     * @Date: 2019/12/30
     * @Time: 10:57       
     */
    public List<Map<String,String>> getCommitteeList(Db1Service db1Service){
        List<AppCommonDao> committeeList = db1Service.getCommitteeList();
        List<Map<String,String>> list = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(committeeList)){
            for (AppCommonDao appCommonDao : committeeList) {
                Map<String,String> map = new HashedMap();
                LinkedHashMap<String, Object> commonMap = appCommonDao.getCommonMap();
                map.put("code",String.valueOf(commonMap.get("code")));
                map.put("name",String.valueOf(commonMap.get("name")));
                list.add(map);
            }
        }
        return list;
    }


    /** 
     * @Description: 导出pdf文件
     * @Param: [o, startDate, endDate, committeeName] 
     * @return: boolean 
     * @Author: jianghy 
     * @Date: 2020/1/1
     * @Time: 15:43       
     */
    public boolean exportPdf(Db1Service db1Service,Map<String, Object> o,String committeeCode,String committeeName,String nowDate){
        boolean flag = false;
        //获取环境参数
        Environment env = ApplicationUtil.getBean(Environment.class);
        // 模板路径
        String template_path = env.getProperty("report.pdf.template");
//        String template_path = "E:/word/pdf/ceshi2.pdf";//测试参数

        // 生成的新文件路径
        String newpdf_path = env.getProperty("report.pdf.newfile");
//        String fileName = committeeName+"("+nowDate+").pdf";
        String fileName = committeeCode+"-"+nowDate+".pdf";
        newpdf_path = newpdf_path + fileName;
//        String newpdf_path = "E:/word/pdf/"+committeeName+"("+nowDate+").pdf";//测试参数

        // 生成到ftp文件服务器上目录的路径
        String ftppath = env.getProperty("report.pdf.ftppath");

        PdfReader reader;
        FileOutputStream out;
        ByteArrayOutputStream bos;
        PdfStamper stamper;
        try {
             //这个是字体文件
//            BaseFont bf = BaseFont.createFont("C://Users//fu//Downloads//simsunttc//simsun.ttc,1", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
//            Font FontChinese = new Font(bf, 5, Font.NORMAL);
            reader = new PdfReader(template_path);// 读取pdf模板
            logger.info("读取到的reader:" + reader);
            out = new FileOutputStream(newpdf_path);// 输出流
            logger.info("读取到的out:" + out);
            bos = new ByteArrayOutputStream();
            stamper = new PdfStamper(reader, bos);
            AcroFields form = stamper.getAcroFields();
            Map<String, String> dataMap = (Map<String, String>) o.get("dataMap");
//            form.addSubstitutionFont(bf);
            for (String key : dataMap.keySet()) {
                String value = String.valueOf(dataMap.get(key));
                form.setField(key, value);
            }
            stamper.setFormFlattening(true);// 如果为false，生成的PDF文件可以编辑，如果为true，生成的PDF文件不可以编辑
            stamper.close();
            Document doc = new Document();
//            Font font = new Font(bf, 32);
            PdfCopy copy = new PdfCopy(doc, out);
            doc.open();
            PdfImportedPage importPage = null;
            ///循环是处理成品只显示一页的问题
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                importPage = copy.getImportedPage(new PdfReader(bos.toByteArray()), i);
                copy.addPage(importPage);
            }
            doc.close();
            logger.info("生成pdf文件完成~~~~~~~~~~");

            //把本地pdf文件上传到ftp
            uploadFtp(bos,newpdf_path,fileName,ftppath);
            logger.info("文件上传到ftp完成......");

            //存储记录
            String createDate = dataMap.get("bgscrq");
            String startDate = dataMap.get("bgzq_start");
            String endDate = dataMap.get("bgzq_end");
            boolean status = db1Service.doCreateReport(createDate,startDate,endDate,committeeCode,committeeName);
            if (status){
                logger.info(committeeName+"插入记录成功~~~~~~~~~~");
            }
            flag = true;
        } catch (IOException e) {
            logger.error("io异常：{}"+ e);
        } catch (DocumentException e1) {
            logger.error("文档异常：{}"+ e1);
        }
        return flag;
    }



    /**
     * @Description: 上传pdf到ftp
     * @Param: [os, newpdf_path, fileName]
     * @return: void
     * @Author: jianghy
     * @Date: 2020/1/6
     * @Time: 22:59
     */
    public void uploadFtp(ByteArrayOutputStream os,String newpdf_path,String fileName,String repFileResPath){
        //文件的全路径
        String repFileName = repFileResPath + fileName;
        String pathFileName = SpringConfig.base_dir + repFileName;
        //保存文件到本地
        FileOutputStream fileOutputStream = null;
        try {
//            fileOutputStream = new FileOutputStream(newpdf_path);
//            fileOutputStream.write(os.toByteArray());
            FtpUtil.ftpServer(pathFileName, os,repFileResPath);

//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** 
     * @Description: 下载pdf
     * @Param: [db1Service, createDate, committeeName, token, request, response]
     * @return: int 
     * @Author: jianghy 
     * @Date: 2020/1/1
     * @Time: 21:44
     */
    public int downloadPdf(Db1Service db1Service, String createDate, String committeeName, String token, HttpServletRequest request, HttpServletResponse response) {
        AppCommonExternalModulesDto appCommonExternalModulesDto = new AppCommonExternalModulesDto();
        DownloadRecord downloadRecord = null;
        // 业务操作
        try {
            //验证授权
//            boolean verTokenFlag = AppModConfig.verifyAuthCode2(token, db2Service,new int[2]);
            boolean verTokenFlag = true;
            if (verTokenFlag) {
                    //文件名
                    String fileName = committeeName+"("+createDate+").pdf";
                    String filePath = "E:/word/pdf/"+fileName;
                    //声明本次下载状态的记录对象
                    downloadRecord = new DownloadRecord(fileName, filePath, request);
                    //设置响应头和客户端保存文件名
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("multipart/form-data");
                    // 针对IE或者以IE为内核的浏览器：
                    String userAgent = request.getHeader("User-Agent");
                    if (userAgent.contains("MSIE") || userAgent.contains("Trident") || userAgent.contains("Edge")) {
                        fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
                    // google,火狐
                    } else if(userAgent.contains("Mozilla")){
                        fileName = new String(fileName.getBytes("UTF-8"), "ISO-8859-1");
                    // 其他浏览器
                    } else{
                        fileName = java.net.URLEncoder.encode(fileName, "UTF-8");
                    }
                    response.setHeader("Content-Disposition", "attachment;fileName= "+ fileName);
                    //用于记录以完成的下载的数据量，单位是byte
                    long downloadedLength = 0L;
                    try {
                        //打开本地文件流
                        InputStream inputStream = new FileInputStream(filePath);
                        //激活下载操作
                        OutputStream os = response.getOutputStream();

                        //循环写入输出流
                        byte[] b = new byte[2048];
                        int length;
                        while ((length = inputStream.read(b)) > 0) {
                            os.write(b, 0, length);
                            downloadedLength += b.length;
                        }

                        // 这里主要关闭。
                        os.close();
                        inputStream.close();
                    } catch (Exception e){
                        e.printStackTrace();
                        downloadRecord.setStatus(DownloadRecord.STATUS_ERROR);
                    }
                    downloadRecord.setStatus(DownloadRecord.STATUS_SUCCESS);
                    downloadRecord.setEndTime(new Timestamp(System.currentTimeMillis()));
                    downloadRecord.setLength(downloadedLength);
                }else {
                appCommonExternalModulesDto.setResCode(IOTRspType.AUTHCODE_CHKERR.getCode().toString());
                appCommonExternalModulesDto.setResMsg(IOTRspType.AUTHCODE_CHKERR.getMsg());
            }
        }catch (Exception e){
            e.printStackTrace();
            downloadRecord.setStatus(DownloadRecord.STATUS_ERROR);
        }
        return downloadRecord.getStatus();
    }


    /** 
     * @Description: 获取周汇总报告列表 
     * @Param: [request, db1Service, db1Service]
     * @return: java.lang.String 
     * @Author: jianghy 
     * @Date: 2020/1/2
     * @Time: 16:28       
     */
    public String getWeekReportList(HttpServletRequest request, Db1Service db1Service, Db2Service db2Service) {
        // 固定Dto层
        AppCommonExternalModulesDto appCommonExternalModulesDto = new AppCommonExternalModulesDto();
        AppCommonData appCommonData = new AppCommonData();
        List<AppCommonDao> sourceDao = null;
        AppCommonDao midDao = null;
        AppCommonDao pageTotal = null;
        List<LinkedHashMap<String, Object>> dataList = new ArrayList();
        LinkedHashMap<String, Object> data =new LinkedHashMap<String, Object>();
        // 业务操作
        try {
            //授权码
            String token =request.getHeader("Authorization");
            //验证授权
//            boolean verTokenFlag = AppModConfig.verifyAuthCode2(token, db2Service,new int[2]);
            boolean verTokenFlag = true;
            if (verTokenFlag) {
                // 以下业务逻辑层修改
                String year = request.getParameter("year");
                String month = request.getParameter("month");
                //教育局的编码
                String committeeCode = request.getParameter("committeeCode");
                //分页
                if (request.getParameter("page") != null && !request.getParameter("page").toString().isEmpty()) {
                    this.curPageNum = Integer.parseInt(request.getParameter("page").toString());
                }
                if (request.getParameter("pageSize") != null && !request.getParameter("pageSize").toString().isEmpty()) {
                    this.pageSize = Integer.parseInt(request.getParameter("pageSize").toString());
                }
                Integer startNum = (curPageNum - 1) * pageSize;
                sourceDao = db1Service.getWeekReportList(year,month,committeeCode);
                List<AppCommonDao> resultDao=sourceDao.subList(startNum, sourceDao.size() >(startNum + pageSize)?(startNum + pageSize):sourceDao.size());
                // 获取列表数据
                for (int i = 0; i < resultDao.size(); i++) {
                    LinkedHashMap<String, Object> commonMap = resultDao.get(i).getCommonMap();
                    dataList.add(commonMap);
                }

                data.put("curPageNum", curPageNum);
                data.put("pageTotal", sourceDao.size());

                appCommonData.setData(data);
                appCommonData.setDataList(dataList);
                appCommonExternalModulesDto.setData(appCommonData);
            }else {
                appCommonExternalModulesDto.setResCode(IOTRspType.System_ERR.getCode().toString());
                appCommonExternalModulesDto.setResMsg(IOTRspType.AUTHCODE_CHKERR.getMsg());
            }
        } catch (Exception e) {
            appCommonExternalModulesDto.setResCode(IOTRspType.System_ERR.getCode().toString());
            appCommonExternalModulesDto.setResMsg(e.getMessage());
        }
        String strResp = null;
            try {
            strResp = objectMapper.writeValueAsString(appCommonExternalModulesDto);
            strResp = new ToolUtil().rmExternalStructure(strResp,"dataList");
        } catch (Exception e) {
            strResp = new ToolUtil().getInitJson();
        }
		return strResp;
    }



    /** 
     * @Description: 获取行政区划列表
     * @Param: [request, db1Service, db2Service] 
     * @return: java.lang.String 
     * @Author: jianghy 
     * @Date: 2020/1/7
     * @Time: 10:19       
     */
    public String getCommitteeListMod(HttpServletRequest request, Db1Service db1Service,Db2Service db2Service) {
        // 固定Dto层
        AppCommonExternalModulesDto appCommonExternalModulesDto = new AppCommonExternalModulesDto();
        AppCommonData appCommonData = new AppCommonData();
        List<AppCommonDao> sourceDao = null;
        AppCommonDao pageTotal = null;
        List<Map<String,String>> dataList = new ArrayList();

        // 业务操作
        try {
            // 授权码
            String token = request.getHeader("Authorization");
            // 验证授权
//            boolean verTokenFlag = AppModConfig.verifyAuthCode2(token, db2Service, new int[2]);
            boolean verTokenFlag = true;
            if (verTokenFlag) {
                dataList = getCommitteeList(db1Service);
                appCommonExternalModulesDto.setData(dataList);
                // 以上业务逻辑层修改
                // 固定返回
            } else {
                appCommonExternalModulesDto.setResCode(IOTRspType.AUTHCODE_CHKERR.getCode().toString());
                appCommonExternalModulesDto.setResMsg(IOTRspType.AUTHCODE_CHKERR.getMsg().toString());
            }
        } catch (Exception e) {
            appCommonExternalModulesDto.setResCode(IOTRspType.System_ERR.getCode().toString());
            appCommonExternalModulesDto.setResMsg(IOTRspType.System_ERR.getMsg().toString());
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String strResp = null;
        try {
            strResp = objectMapper.writeValueAsString(appCommonExternalModulesDto);
            strResp = new ToolUtil().rmExternalStructure(strResp);
        } catch (Exception e) {
            strResp = new ToolUtil().getInitJson();
        }
        return strResp;
    }
}
