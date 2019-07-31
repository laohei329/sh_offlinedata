package com.tfit.BdBiProcSrvYgwcSchOmc.service.info.manage.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.tfit.BdBiProcSrvYgwcSchOmc.config.AppModConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.config.SpringConfig;
import com.tfit.BdBiProcSrvYgwcSchOmc.constant.GlobalConstant;
import com.tfit.BdBiProcSrvYgwcSchOmc.constant.RedisKeyConstant;
import com.tfit.BdBiProcSrvYgwcSchOmc.constant.RegexExpressConstant;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSchoolDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.EduSchool;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.saas.EduSchoolSupplier;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.RecoveryWasteOilSummarySearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.RmcRecoveryWasteOilSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.SchoolRecoveryWasteOilSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.WasteOilTypeCodeSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.enums.OwnerType;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.enums.SchoolStructType;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.enums.WasteOilType;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.RmcRecoveryWasteOilDetailsExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.RmcRecoveryWasteOilSummaryExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.SchoolRecoveryWasteOilDetailsExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.SchoolRecoveryWasteOilSummaryExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.ro.GroupMealCompanyBasicRO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.ro.RecoveryWasteOilDetailRO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.ro.SchoolBasicRO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.uo.TableUO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.RecoveryWasteOilSummaryVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.RmcRecoveryWasteOilDetailVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.SchoolRecoveryWasteOilDetailVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.WasteOilTypeCodeVO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.PagedList;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.RedisService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.EduSchoolService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.info.manage.RecoveryWasteOilsService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.info.manage.SchoolBasicService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.saas.EduSchoolSupplierService;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.CommonUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.DictConvertUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.ExcelGenerateUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.RedisValueUtil;
import com.tfit.BdBiProcSrvYgwcSchOmc.util.UniqueIdGen;

import lombok.extern.slf4j.Slf4j;

/**
 * @Descritpion：回收废弃油脂服务实现类
 * @author: tianfang_infotech
 * @date: 2019/1/3 11:55
 */
@Slf4j
@Service
public class RecoveryWasteOilsServiceImpl implements RecoveryWasteOilsService {
	private static final Logger logger = LogManager.getLogger(RecoveryWasteOilsServiceImpl.class.getName());

    @Autowired
    private RedisService redisService;

    /**
     * 一天毫秒数(24 * 3600 * 1000);
     */
    private static final int DAY_TOTAL_MILLISECOND = 86400000;
    private static final String SPLIT_SYMBOL = "_";
    /**
     * 回收废弃油脂汇总后缀
     */
    private static final String RECOVERY_WASTE_OIL_SUMMARY_SUFFIX = "_total";
    /**
     * 循环迭代步长
     */
    private static final int ITERATE_STEP_SIZE = 2;
    /**
     * redis 回收废弃油脂详情格式长度
     */
    private static final int REDIS_RECOVERY_WASTE_OIL_DETAIL_RO_FORMAT_LENGTH = 14;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * 学校基础信息服务
     */
    @Autowired
    private SchoolBasicService schoolBasicService;

    /**
     * 教委系统学校信息服务
     */
    @Autowired
    private EduSchoolService eduSchoolService;

    /**
     * 教委学校供应商信息查询
     */
    @Autowired
    private EduSchoolSupplierService eduSchoolSupplierService;

    @Override
    public List<?> getWasteOilTypeCodes(WasteOilTypeCodeSearchDTO searchDTO) {
        return Arrays.asList(new WasteOilTypeCodeVO(WasteOilType.WASTE_OIL),
                new WasteOilTypeCodeVO(WasteOilType.OILY_WASTE_WATER));
    }

    @Override
    public PagedList<?> getSchoolRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        /**
         * 设置查询学校回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.SCHOOL.getCode());

        return new PagedList<>(getRecoveryWasteOilsVOProcess(searchDTO), searchDTO);
    }

    @Override
    public SchoolRecoveryWasteOilSummaryExport exportSchoolRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO) {
    	//如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        searchDTO.setPage(1);

        /**
         * 导出全部数据
         */
        searchDTO.setPageSize(Integer.MAX_VALUE);

        /**
         * 设置查询学校回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.SCHOOL.getCode());

        List<RecoveryWasteOilSummaryVO> result = getRecoveryWasteOilsVOProcess(searchDTO);
        
        TableUO tableUO = generateRecoveryWasterOilSummaryTableUO(result);

        String repFileName = "/expSchRecWasteOils/" + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];

        ExcelGenerateUtil.generateSimpleExcelFile(repFileName, tableUO);

        SchoolRecoveryWasteOilSummaryExport export = new SchoolRecoveryWasteOilSummaryExport();

        BeanUtils.copyProperties(searchDTO, export);

        export.setExpFileUrl(SpringConfig.repfile_srvdn + repFileName);

        logger.info("=> 导出文件URL：" + export.getExpFileUrl());

        return export;
    }

    private TableUO generateRecoveryWasterOilSummaryTableUO(List<RecoveryWasteOilSummaryVO> result) {
        int columnCount = 4;
        //增加合计行(result.size() + 1)
        int rowCount = result.size() + 1;

        TableUO tableUO = new TableUO(rowCount, columnCount);

        tableUO.setColumnNames(new String[]{"回收周期", "所在地", "回收次数", "回收数量(桶)"});

        Object[][] data = new Object[rowCount][columnCount];
        for (int i = 0; i < result.size(); i++) {
            data[i][0] = result.get(i).getRecDate();
            data[i][1] = result.get(i).getDistName();
            data[i][2] = result.get(i).getReFeq();
            data[i][3] = result.get(i).getRcNum();
        }

        int lastRowIndex = rowCount - 1;
        int reFeq = 0;
        int reNum = 0;
        RecoveryWasteOilSummaryVO oilsVO;
        Iterator<RecoveryWasteOilSummaryVO> iterator = result.iterator();
        while (iterator.hasNext()) {
            oilsVO = iterator.next();
            if (Objects.isNull(oilsVO)) {
                continue;
            }
            reFeq += oilsVO.getReFeq();
            reNum += oilsVO.getRcNum();
        }
        data[lastRowIndex][0] = "合计";
        data[lastRowIndex][1] = "---";
        data[lastRowIndex][2] = reFeq;
        data[lastRowIndex][3] = reNum;

        tableUO.setDataMatrix(data);

        return tableUO;
    }

    @Override
    public PagedList<?> getSchoolRecoveryWasteOilDetails(SchoolRecoveryWasteOilSearchDTO searchDTO,Db1Service db1Service) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }
 
        return new PagedList<>(getSchoolRecoveryWasteOilDetailsProcess(searchDTO,db1Service),searchDTO);
    }

    @Override
    public SchoolRecoveryWasteOilDetailsExport exportSchoolRecoveryWasteOilDetails(SchoolRecoveryWasteOilSearchDTO searchDTO,
    		Db1Service db1Service) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        searchDTO.setPage(1);

        /**
         * 导出全部数据
         */
        searchDTO.setPageSize(Integer.MAX_VALUE);

        /**
         * 设置查询学校回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.SCHOOL.getCode());

        List<SchoolRecoveryWasteOilDetailVO> result = getSchoolRecoveryWasteOilDetailsProcess(searchDTO,db1Service);

        TableUO tableUO = generateSchoolRecoveryWasterOilDetailTableUO(result);

        String repFileName = "/expSchWasteOilDets/" + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];

        ExcelGenerateUtil.generateSimpleExcelFile(repFileName, tableUO);

        SchoolRecoveryWasteOilDetailsExport export = new SchoolRecoveryWasteOilDetailsExport();

        BeanUtils.copyProperties(searchDTO, export);

        export.setExpFileUrl(SpringConfig.repfile_srvdn + repFileName);

        logger.info("=> 导出文件URL：" + export.getExpFileUrl());

        return export;
    }

    private TableUO generateSchoolRecoveryWasterOilDetailTableUO(List<SchoolRecoveryWasteOilDetailVO> result) {
        int columnCount = 17;
        TableUO tableUO = new TableUO(result.size(), columnCount);

        tableUO.setColumnNames(new String[]{"回收日期", "项目点", "总校/分校", "分校数量", 
        		"关联总校", "所属", "主管部门", "所属区", "所在地", "学制", "办学性质",
        		"团餐公司", "种类", "数量（公斤）", "回收单位", "回收人", "回收单据"});

        Object[][] data = new Object[result.size()][columnCount];
        for (int i = 0; i < result.size(); i++) {
            //回收日期
            data[i][0] = dateFormat2.format(result.get(i).getRecDate());           
            //项目点
            data[i][1] = result.get(i).getPpName();
            //总校/分校
            data[i][2] = result.get(i).getSchGenBraFlag();
            //分校数量
            data[i][3] = result.get(i).getBraCampusNum();
            //关联总校
            data[i][4] = result.get(i).getRelGenSchName();
            //所属
            data[i][5] = result.get(i).getSubLevel();
            //主管部门
            data[i][6] = result.get(i).getCompDep();
            //所属区
            data[i][7] = result.get(i).getSubDistName();
            //所在地
            data[i][8] = result.get(i).getDistName();            
            //学制
            data[i][9] = result.get(i).getSchType();
            //办学性质
            data[i][10] = result.get(i).getSchProp();
            //团餐公司
            data[i][11] = result.get(i).getRmcName();
            //种类
            data[i][12] = result.get(i).getWoType();
            //数量（公斤）
            data[i][13] = result.get(i).getRecNum();
            //回收单位
            data[i][14] = result.get(i).getRecCompany();
            //回收人
            data[i][15] = result.get(i).getRecPerson();
            //回收单据
            data[i][16] = result.get(i).getRecBillNum();
        }
        tableUO.setDataMatrix(data);
        return tableUO;
    }

    @Override
    public PagedList<?> getRmcRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        /**
         * 设置查询团餐公司回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.RMC.getCode());

        return new PagedList<>(getRecoveryWasteOilsVOProcess(searchDTO), searchDTO);
    }

    @Override
    public RmcRecoveryWasteOilSummaryExport exportRmcRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        searchDTO.setPage(1);

        /**
         * 导出全部数据
         */
        searchDTO.setPageSize(Integer.MAX_VALUE);

        /**
         * 设置查询学校回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.RMC.getCode());

        List<RecoveryWasteOilSummaryVO> result = getRecoveryWasteOilsVOProcess(searchDTO);

        TableUO tableUO = generateRecoveryWasterOilSummaryTableUO(result);

        String repFileName = "/expRmcRecWasteOils/" + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];

        ExcelGenerateUtil.generateSimpleExcelFile(repFileName, tableUO);

        RmcRecoveryWasteOilSummaryExport export = new RmcRecoveryWasteOilSummaryExport();

        BeanUtils.copyProperties(searchDTO, export);

        export.setExpFileUrl(SpringConfig.repfile_srvdn + repFileName);

        logger.info("=> 导出文件URL：" + export.getExpFileUrl());

        return export;
    }

    @Override
    public PagedList<?> getRmcRecoveryWasteOilDetails(RmcRecoveryWasteOilSearchDTO searchDTO) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        /**
         * 设置查询团餐公司回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.RMC.getCode());

        return new PagedList<>(getRmcRecoveryWasteOilDetailsProcess(searchDTO), searchDTO);
    }

    @Override
    public RmcRecoveryWasteOilDetailsExport exportRmcRecoveryWasteOilDetails(RmcRecoveryWasteOilSearchDTO searchDTO) {
        //如无起止时间，取当天时间段
        if (Objects.isNull(searchDTO.getStartSubDate()) || Objects.isNull(searchDTO.getEndSubDate())) {
            Date date = new Date();
            searchDTO.setStartSubDate(date);
            searchDTO.setEndSubDate(date);
        }

        searchDTO.setPage(1);

        /**
         * 导出全部数据
         */
        searchDTO.setPageSize(Integer.MAX_VALUE);

        /**
         * 设置查询学校回收废弃油脂
         */
        searchDTO.setSearchType(OwnerType.RMC.getCode());

        List<RmcRecoveryWasteOilDetailVO> result = getRmcRecoveryWasteOilDetailsProcess(searchDTO);

        TableUO tableUO = generateRmcRecoveryWasterOilDetailTableUO(result);

        String repFileName = "/expRmcWasteOilDets/" + UniqueIdGen.uuid() + SpringConfig.repFileFormats[SpringConfig.curRepFileFrmtIdx];

        ExcelGenerateUtil.generateSimpleExcelFile(repFileName, tableUO);

        RmcRecoveryWasteOilDetailsExport export = new RmcRecoveryWasteOilDetailsExport();

        BeanUtils.copyProperties(searchDTO, export);

        export.setExpFileUrl(SpringConfig.repfile_srvdn + repFileName);

        logger.info("=> 导出文件URL：" + export.getExpFileUrl());

        return export;
    }

    private TableUO generateRmcRecoveryWasterOilDetailTableUO(List<RmcRecoveryWasteOilDetailVO> result) {
        int columnCount = 8;
        TableUO tableUO = new TableUO(result.size(), columnCount);

        tableUO.setColumnNames(new String[]{"回收日期", "区", "种类", "团餐公司", "数量(桶)", "回收单位", "回收人", "回收单据"});

        Object[][] data = new Object[result.size()][columnCount];
        for (int i = 0; i < result.size(); i++) {
            //回收日期
            data[i][0] = dateFormat2.format(result.get(i).getRecDate());
            //区
            data[i][1] = result.get(i).getDistName();
            //种类
            data[i][2] = result.get(i).getWoType();
            //团餐公司
            data[i][3] = result.get(i).getRmcName();
            //数量(桶)
            data[i][4] = result.get(i).getRecNum();
            //回收单位
            data[i][5] = result.get(i).getRecCompany();
            //回收人
            data[i][6] = result.get(i).getRecPerson();
            //回收单据
            data[i][7] = result.get(i).getRecBillNum();
        }
        tableUO.setDataMatrix(data);

        return tableUO;
    }

    private List<RecoveryWasteOilSummaryVO> getRecoveryWasteOilsVOProcess(RecoveryWasteOilSummarySearchDTO searchDTO) {
        List<RecoveryWasteOilSummaryVO> aggregationResult = new ArrayList<>();

        List<RecoveryWasteOilSummaryVO> result = getDailyRecoveryWasteOilTotalList(searchDTO);

        Map<String, List<RecoveryWasteOilSummaryVO>> groupResult = result.stream().collect(Collectors.groupingBy(RecoveryWasteOilSummaryVO::getDistName));
        for (String key : groupResult.keySet()) {
            RecoveryWasteOilSummaryVO oilsVO = new RecoveryWasteOilSummaryVO();

            Integer reFeq = 0;
            Integer rcNum = 0;
            Iterator<RecoveryWasteOilSummaryVO> iterator = groupResult.get(key).iterator();
            while (iterator.hasNext()) {
                RecoveryWasteOilSummaryVO item = iterator.next();
                reFeq += item.getReFeq();
                rcNum += item.getRcNum();
            }
            oilsVO.setReFeq(reFeq);
            oilsVO.setRcNum(rcNum);

            oilsVO.setDistName(key);
            oilsVO.setRecDate(String.format("%s-%s", dateFormat2.format(searchDTO.getStartSubDate()), dateFormat2.format(searchDTO.getEndSubDate())));

            aggregationResult.add(oilsVO);
        }

        Collections.sort(aggregationResult, (o1, o2) -> {
            //回收频次降序
            int i = o2.getReFeq().compareTo(o1.getReFeq());
            if (i == 0) {
                //回收数量降序
                return o2.getRcNum().compareTo(o1.getRcNum());
            }
            return i;
        });
        return aggregationResult;
    }

    /**
     * 获取学校回收废弃油脂详细列表信息
     *
     * @param searchDTO
     * @return
     */
    private List<SchoolRecoveryWasteOilDetailVO> getSchoolRecoveryWasteOilDetailsProcess(SchoolRecoveryWasteOilSearchDTO searchDTO,
    		Db1Service db1Service) {

        List<SchoolRecoveryWasteOilDetailVO> result = new ArrayList<>();

        //所有学校
        List<EduSchool> eduSchools = eduSchoolService.getEduSchools();
        //所有学校团餐公司(供应商)
        List<EduSchoolSupplier> eduSchoolSuppliers = eduSchoolSupplierService.getAllSchoolSuppliers();

        String typeRedisKey = resolveRedisKeyForWasteOilDetail(searchDTO.getSearchType());

        Map<String, Date> keyMap = genRedisKey(typeRedisKey, searchDTO.getStartSubDate(), searchDTO.getEndSubDate());

        List<RecoveryWasteOilDetailRO> detailROList = resolveSchoolRecoveryWasteOilDetailROList(keyMap);

        //过滤出学校基础信息查询条件的其他条件的过滤
        List<RecoveryWasteOilDetailRO> filteredDetailROList = filterSchoolRecoveryWasteOilDetailROList(detailROList, eduSchoolSuppliers, searchDTO);

        List<Object> distNamesList=CommonUtil.changeStringToList(searchDTO.getDistNames());
        Map<String, Integer> schIdMap = new HashMap<>();
        List<TEduSchoolDo> tesDoList = new ArrayList<TEduSchoolDo>();
		if(searchDTO.getDistName()!=null) {
			tesDoList = db1Service.getTEduSchoolDoListByDs1(searchDTO.getDistName().toString(), 5);
		}else{
			tesDoList = db1Service.getTEduSchoolDoListByDs1(distNamesList);
		}
		for(int i = 0; i < tesDoList.size(); i++) {
			schIdMap.put(tesDoList.get(i).getId(), i+1);
		}
		
        boolean okFlag;
        for (RecoveryWasteOilDetailRO detailRO : filteredDetailROList) {

            SchoolBasicRO schoolBasicRO = schoolBasicService.getSchoolBasicFromRedis(detailRO.getSchoolId());
            if (Objects.isNull(schoolBasicRO)) {
                logger.info("School=> id={} 不存在学校基础信息，匹配失败。", detailRO.getSchoolId());
                continue;
            }

            //过滤学校相关查询条件
            okFlag = filterSchoolBasicRO(schoolBasicRO, searchDTO);
            
            //不满足过滤条件
            if (!okFlag) {
                continue;
            }

            SchoolRecoveryWasteOilDetailVO detailVO = new SchoolRecoveryWasteOilDetailVO();
            //回收日期
            detailVO.setRecDate(detailRO.getRecDate());
            //回收单位
            detailVO.setRecCompany(detailRO.getReceiverName());
            //回收人
            detailVO.setRecPerson(detailRO.getContact());
            //区域信息
            detailVO.setDistName(DictConvertUtil.mapToDistName(String.valueOf(detailRO.getArea())));
            //回收单据数
            detailVO.setRecBillNum(detailRO.getDocuments());
            //回收数量
            detailVO.setRecNum(detailRO.getNumber());
            //废弃油脂类型
            detailVO.setWoType(DictConvertUtil.mapToWasteOilTypeName(detailRO.getSecondType()));

            //项目点
            detailVO.setPpName(schoolBasicRO.getSchoolName());
            //总校/分校信息
            if (Integer.valueOf(SchoolStructType.BRANCH_SCHOOL.getCode()).equals(schoolBasicRO.getIsBranchSchool())) {
                //分校数量
                long subSchoolCount = eduSchools.stream().filter(s -> null != s.getParentId() && s.getParentId().equals(detailRO.getSchoolId())).count();
                detailVO.setBraCampusNum((int) subSchoolCount);
                detailVO.setSchGenBraFlag(SchoolStructType.BRANCH_SCHOOL.getName());
            } else {
                detailVO.setBraCampusNum(0);
                detailVO.setSchGenBraFlag(SchoolStructType.GENERAL_SCHOOL.getName());
            }

            //关联总校
            detailVO.setRelGenSchName(GlobalConstant.STRING_EMPTY_DISPLAY_DEFAULT);
            if (!StringUtils.isEmpty(schoolBasicRO.getParentId())) {
                //关联总校RO对象
                SchoolBasicRO subSchoolBasicRO = schoolBasicService.getSchoolBasicFromRedis(schoolBasicRO.getParentId());
                if (!Objects.isNull(subSchoolBasicRO) && !StringUtils.isEmpty(subSchoolBasicRO.getSchoolName())) {
                    detailVO.setRelGenSchName(subSchoolBasicRO.getSchoolName());
                }
            }

            //所属，0:其他，1:部属，2:市属，3: 区属
            detailVO.setSubLevel(DictConvertUtil.mapToSubLevelName(schoolBasicRO.getDepartmentMasterId()));
            //主管部门
            detailVO.setCompDep(GlobalConstant.STRING_EMPTY_DISPLAY_DEFAULT);
            String compDep = getCompDep(schoolBasicRO);
            if (!StringUtils.isEmpty(compDep)) {
                detailVO.setCompDep(compDep);
            }
            
            //所属区域名称
            
            TEduSchoolDo tesDo = null;
			if(schIdMap.containsKey(detailRO.getSchoolId())) {
				int j = schIdMap.get(detailRO.getSchoolId());
				tesDo = tesDoList.get(j-1);
			}
			if(tesDo == null)
				continue;
			
            detailVO.setSubDistName("-");
			if(tesDo.getSchoolAreaId() != null)
				detailVO.setSubDistName(AppModConfig.distIdToNameMap.get(tesDo.getSchoolAreaId()));

            //学制
            detailVO.setSchType(DictConvertUtil.mapToSchTypeName(schoolBasicRO.getLevel()));
            //办学性质
            detailVO.setSchProp(AppModConfig.getSchProp(schoolBasicRO.getSchoolNature()));
            //团餐公司
            detailVO.setRmcName(GlobalConstant.STRING_EMPTY_DISPLAY_DEFAULT);
            List<String> schoolSupplierNames = eduSchoolSuppliers.stream().filter(s -> s.getSchoolId().equalsIgnoreCase(detailRO.getSchoolId())).map(s -> s.getSupplierName()).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(schoolSupplierNames)) {
                detailVO.setRmcName(schoolSupplierNames.get(0));
            }

            result.add(detailVO);
        }

        Collections.sort(result, (o1, o2) -> {
            //所在区域升序
            int i = o1.getDistName().compareTo(o2.getDistName());
            if (i == 0) {
                //学校类型升序
                return o1.getSchType().compareTo(o2.getSchType());
            }
            return i;
        });

        return result;
    }

    /**
     * 获取学校回收废弃油脂详细列表信息
     *
     * @param searchDTO
     * @return
     */
    private List<RmcRecoveryWasteOilDetailVO> getRmcRecoveryWasteOilDetailsProcess(RmcRecoveryWasteOilSearchDTO searchDTO) {

        List<RmcRecoveryWasteOilDetailVO> result = new ArrayList<>();

        String typeRedisKey = resolveRedisKeyForWasteOilDetail(searchDTO.getSearchType());

        Map<String, Date> keyMap = genRedisKey(typeRedisKey, searchDTO.getStartSubDate(), searchDTO.getEndSubDate());

        List<RecoveryWasteOilDetailRO> detailROList = resolveSchoolRecoveryWasteOilDetailROList(keyMap);

        List<RecoveryWasteOilDetailRO> filteredDetailROList = filterRmcRecoveryWasteOilDetailROList(detailROList, searchDTO);

        //加载所有供应商信息
        Map<String, String> supplierIdNameMap = eduSchoolSupplierService.getAllSupplierBasics()
                .stream().collect(Collectors.toMap(s -> s.getSupplierId(), e -> e.getSupplierName()));

        for (RecoveryWasteOilDetailRO detailRO : filteredDetailROList) {
            String supplierName = supplierIdNameMap.get(detailRO.getSchoolId());
            if (Objects.isNull(supplierName)) {
                logger.info("GroupMealCompany=> id={} 不存在团餐公司基础信息，匹配失败。", detailRO.getSchoolId());
                continue;
            }

            RmcRecoveryWasteOilDetailVO detailVO = new RmcRecoveryWasteOilDetailVO();
            //回收日期
            detailVO.setRecDate(detailRO.getRecDate());
            //回收单位
            detailVO.setRecCompany(detailRO.getReceiverName());
            //回收人
            detailVO.setRecPerson(detailRO.getContact());
            //区域信息
            detailVO.setDistName(DictConvertUtil.mapToDistName(String.valueOf(detailRO.getArea())));
            //回收单据数
            detailVO.setRecBillNum(detailRO.getDocuments());
            //回收数量
            detailVO.setRecNum(detailRO.getNumber());
            //废弃油脂类型
            detailVO.setWoType(DictConvertUtil.mapToWasteOilTypeName(detailRO.getSecondType()));
            //团餐公司
            detailVO.setRmcName(supplierName);

            result.add(detailVO);
        }

        Collections.sort(result, (o1, o2) -> {
            //回收日期降序
            int i = o2.getRecDate().compareTo(o1.getRecDate());
            if (i == 0) {
                //团餐公司名称升序
                return o1.getRmcName().compareTo(o2.getRmcName());
            }
            return i;
        });

        return result;
    }

    /**
     * 检索过滤 Redis 学校回收油脂详情
     *
     * @param detailROList
     * @param searchDTO
     * @return
     */
    private List<RecoveryWasteOilDetailRO> filterSchoolRecoveryWasteOilDetailROList(List<RecoveryWasteOilDetailRO> detailROList,
                                                                                    List<EduSchoolSupplier> eduSchoolSuppliers, SchoolRecoveryWasteOilSearchDTO searchDTO) {

        List<RecoveryWasteOilDetailRO> detailList = new ArrayList<>();

        if (CollectionUtils.isEmpty(detailROList)) {
            return detailList;
        }

        boolean okFlag;
        for (RecoveryWasteOilDetailRO detailRO : detailROList) {
            okFlag = filterSchoolRecoveryWasteOilDetailRO(detailRO, eduSchoolSuppliers, searchDTO);
            if (!okFlag) {
                continue;
            }
            detailList.add(detailRO);
        }

        return detailList;
    }

    /**
     * 检索过滤 Redis 学校回收油脂详情
     *
     * @param detailROList
     * @param searchDTO
     * @return
     */
    private List<RecoveryWasteOilDetailRO> filterRmcRecoveryWasteOilDetailROList(List<RecoveryWasteOilDetailRO> detailROList, RmcRecoveryWasteOilSearchDTO searchDTO) {

        List<RecoveryWasteOilDetailRO> detailList = new ArrayList<>();

        if (CollectionUtils.isEmpty(detailROList)) {
            return detailList;
        }

        boolean okFlag;
        for (RecoveryWasteOilDetailRO detailRO : detailROList) {
            okFlag = filterRmcRecoveryWasteOilDetailRO(detailRO, searchDTO);
            if (!okFlag) {
                continue;
            }
            detailList.add(detailRO);
        }

        return detailList;
    }

    /**
     * Redis学校基础信息过滤器
     *
     * @param schoolBasicRO
     * @param searchDTO
     * @return
     */
    private boolean filterSchoolBasicRO(SchoolBasicRO schoolBasicRO, SchoolRecoveryWasteOilSearchDTO searchDTO) {

        //过滤油脂类型Code
        if (!Objects.isNull(searchDTO.getWoType())) {
            //此模型无需过滤
        }

        //过滤项目点（学校Id）
        if (StringUtils.isNotBlank(searchDTO.getPpName())) {
            if (!searchDTO.getPpName().equalsIgnoreCase(schoolBasicRO.getId())) {
                return false;
            }
        }

        //过滤区Id
        if (!Objects.isNull(searchDTO.getDistName())) {
            if (!searchDTO.getDistName().equals(schoolBasicRO.getArea())) {
                return false;
            }
        }
        
        //过滤学制Id
        List<Object> schTypeList=CommonUtil.changeStringToList(searchDTO.getSchTypes());
        if (!Objects.isNull(searchDTO.getSchType())) {
            if (!searchDTO.getSchType().equals(schoolBasicRO.getLevel())) {
                return false;
            }
        }else if (schTypeList!=null && schTypeList.size() >0) {
        	if(schoolBasicRO.getLevel()==null) {
        		return false;
        	}
            if (!schTypeList.contains(schoolBasicRO.getLevel().toString())) {
                return false;
            }
        }

        //过滤团餐公司Id
        if (StringUtils.isNotBlank(searchDTO.getRmcName())) {
            //此模型无需过滤
        }

        //回收公司Id
        if (StringUtils.isNotBlank(searchDTO.getRecCompany())) {
            //此模型无需过滤
        }

        //过滤回收人Id
        if (StringUtils.isNotBlank(searchDTO.getRecPerson())) {
            //此模型无需过滤
        }
        
        List<Object> schPropsList=CommonUtil.changeStringToList(searchDTO.getSchProps());
        //学校性质
        if (searchDTO.getSchProp() !=null && searchDTO.getSchProp() != -1) {
        	if(StringUtils.isEmpty(schoolBasicRO.getSchoolNature())) {
        		return false;
        	}
            if (!schoolBasicRO.getSchoolNature().equals(searchDTO.getSchProp().toString())) {
                return false;
            }
        }else if (schPropsList!=null && schPropsList.size() >0) {
        	if(StringUtils.isEmpty(schoolBasicRO.getSchoolNature())) {
        		return false;
        	}
            if (!schPropsList.contains(schoolBasicRO.getSchoolNature())) {
                return false;
            }
        }

    	List<Object> subLevelsList=CommonUtil.changeStringToList(searchDTO.getSubLevels());
    	List<Object> compDepsList=CommonUtil.changeStringToList(searchDTO.getCompDeps());
    	
        //判断所属
		if(StringUtils.isNotEmpty(searchDTO.getSubLevel()) && !"-1".equals(searchDTO.getSubLevel())) {
			if(schoolBasicRO.getDepartmentMasterId() != Integer.parseInt(searchDTO.getSubLevel()) )
				return false;
		}else if(subLevelsList!=null && subLevelsList.size() >0) {
			if(!subLevelsList.contains(String.valueOf(schoolBasicRO.getDepartmentMasterId()))) {
				return false;
			}
		}
		
        //判断所属部门
		if(StringUtils.isNotEmpty(searchDTO.getCompDep()) && !"-1".equals(searchDTO.getCompDep())) {
			String currDepartmentMasterId = schoolBasicRO.getDepartmentSlaveId();
			//如果是区属查询，需要根据编码（32位字母+数字）转换为对应的数字
			if(schoolBasicRO.getDepartmentMasterId() == 3) {
				String orgName = AppModConfig.compDepIdToNameMap3bd.get(schoolBasicRO.getDepartmentSlaveId());
				if(orgName != null) {
					currDepartmentMasterId = AppModConfig.compDepNameToIdMap3.get(orgName);
				}
			}
			if(!searchDTO.getCompDep().equals(schoolBasicRO.getDepartmentMasterId()+"_"+currDepartmentMasterId))
				return false;
		}else if(compDepsList!=null && compDepsList.size() >0) {
			
			String currDepartmentMasterId = schoolBasicRO.getDepartmentSlaveId();
			//如果是区属查询，需要根据编码（32位字母+数字）转换为对应的数字
			if(schoolBasicRO.getDepartmentMasterId() == 3) {
				String orgName = AppModConfig.compDepIdToNameMap3bd.get(schoolBasicRO.getDepartmentSlaveId());
				if(orgName != null) {
					currDepartmentMasterId = AppModConfig.compDepNameToIdMap3.get(orgName);
				}
			}
			
			if(!compDepsList.contains(schoolBasicRO.getDepartmentMasterId()+"_"+currDepartmentMasterId)) {
				return false;
			}
		}
		
        
        //通过过滤
        return true;
    }

    /**
     * Redis团餐公司基础信息过滤器
     *
     * @param mealCompanyBasicRO
     * @param searchDTO
     * @return
     */
    private boolean filterGroupMealCompanyBasicRO(GroupMealCompanyBasicRO mealCompanyBasicRO, RmcRecoveryWasteOilSearchDTO searchDTO) {

        //过滤油脂类型Code
        if (!Objects.isNull(searchDTO.getWoType())) {
            //此模型无需过滤
        }

        //团餐公司编号
        if (StringUtils.isNotBlank(searchDTO.getRmcName())) {
            if (!searchDTO.getRmcName().equalsIgnoreCase(mealCompanyBasicRO.getId())) {
                return false;
            }
        }

        //过滤区Id
        if (!Objects.isNull(searchDTO.getDistName())) {
            if (!searchDTO.getDistName().equals(mealCompanyBasicRO.getArea())) {
                return false;
            }
        }

        //过滤团餐公司Id
        if (StringUtils.isNotBlank(searchDTO.getRmcName())) {
            //此模型无需过滤
        }

        //回收公司Id
        if (StringUtils.isNotBlank(searchDTO.getRecCompany())) {
            //此模型无需过滤
        }

        //过滤回收人Id
        if (StringUtils.isNotBlank(searchDTO.getRecPerson())) {
            //此模型无需过滤
        }

        //通过过滤
        return true;
    }

    /**
     * 过滤Redis回收废弃油脂详情
     *
     * @param detailVO
     * @param searchDTO
     * @param eduSchoolSuppliers
     * @return
     */
    private boolean filterSchoolRecoveryWasteOilDetailRO(RecoveryWasteOilDetailRO detailVO, List<EduSchoolSupplier> eduSchoolSuppliers,
                                                         SchoolRecoveryWasteOilSearchDTO searchDTO) {

        //过滤油脂类型Code
        if (!Objects.isNull(searchDTO.getWoType())) {
            if (!searchDTO.getWoType().equals(detailVO.getSecondType())) {
                return false;
            }
        }

        //过滤项目点（学校Id/团餐公司Id）
        if (StringUtils.isNotBlank(searchDTO.getPpName())) {
            if (!searchDTO.getPpName().equalsIgnoreCase(detailVO.getSchoolId())) {
                return false;
            }
        }

        //过滤区Id
        if (!Objects.isNull(searchDTO.getDistName())) {
            if (!searchDTO.getDistName().equals(detailVO.getArea())) {
                return false;
            }
        }else if (StringUtils.isNotEmpty(searchDTO.getDistNames())) {
            //区域集合过滤
            List<Object> distNamesList=CommonUtil.changeStringToList(searchDTO.getDistNames());
            if(distNamesList!=null && distNamesList.size() >0) {
				if(!distNamesList.contains(String.valueOf(detailVO.getArea()))) {
					return false;
				}
			}
        }

        //过滤学制Id
        if (!Objects.isNull(searchDTO.getSchType())) {
            //此模型无需过滤
        }

        //过滤团餐公司Id
        if (StringUtils.isNotBlank(searchDTO.getRmcName())) {
            long count = eduSchoolSuppliers.stream().filter(s -> s.getSchoolId().equalsIgnoreCase(detailVO.getSchoolId())
                    && searchDTO.getRmcName().equalsIgnoreCase(s.getSupplierId())).count();
            if (count == 0) {
                return false;
            }
        }

        //回收公司Id
        if (!filterRecoveryCompanyAndPerson(detailVO, searchDTO.getRecCompany(), searchDTO.getRecPerson())) {
            return false;
        }

        
        //所属
        //主管部门
        //办学性质
        
        return true;
    }

    /**
     * 过滤Redis回收废弃油脂详情
     *
     * @param detailVO
     * @param searchDTO
     * @return
     */
    private boolean filterRmcRecoveryWasteOilDetailRO(RecoveryWasteOilDetailRO detailVO, RmcRecoveryWasteOilSearchDTO searchDTO) {

        //过滤油脂类型Code
        if (!Objects.isNull(searchDTO.getWoType())) {
            if (!searchDTO.getWoType().equals(detailVO.getSecondType())) {
                return false;
            }
        }

        //团餐公司编号
        if (StringUtils.isNotBlank(searchDTO.getRmcName())) {
            if (!searchDTO.getRmcName().equalsIgnoreCase(detailVO.getSchoolId())) {
                return false;
            }
        }

        //过滤区Id
        if (!Objects.isNull(searchDTO.getDistName())) {
            if (!searchDTO.getDistName().equals(detailVO.getArea())) {
                return false;
            }
        }else if (StringUtils.isNotEmpty(searchDTO.getDistNames())) {
            //区域集合过滤
            List<Object> distNamesList=CommonUtil.changeStringToList(searchDTO.getDistNames());
            if(distNamesList!=null && distNamesList.size() >0) {
				if(!distNamesList.contains(String.valueOf(detailVO.getArea()))) {
					return false;
				}
			}
        }

        if (!filterRecoveryCompanyAndPerson(detailVO, searchDTO.getRecCompany(), searchDTO.getRecPerson())) {
            return false;
        }

        return true;
    }

    /**
     * @param detailVO
     * @param recCompany 待搜索的回收公司
     * @param recPerson  待检索的回收人
     * @return
     */
    private boolean filterRecoveryCompanyAndPerson(RecoveryWasteOilDetailRO detailVO, String recCompany, String recPerson) {
        //回收公司Id
        if (StringUtils.isNotBlank(recCompany)) {
            //数据为空
            if (GlobalConstant.REDIS_NULL_VALUE_STRING_DEFAULT.equalsIgnoreCase(detailVO.getReceiverName())
                    || StringUtils.isBlank(detailVO.getReceiverName())) {
                return false;
            }

            //支持回收公司模糊查询
            if (!Objects.isNull(detailVO.getReceiverName()) && detailVO.getReceiverName().indexOf(recCompany) == -1) {
                return false;
            }
        }

        //过滤回收人Id
        if (StringUtils.isNotBlank(recPerson)) {
            //数据为空
            if (GlobalConstant.REDIS_NULL_VALUE_STRING_DEFAULT.equalsIgnoreCase(detailVO.getContact())
                    || StringUtils.isBlank(detailVO.getContact())) {
                return false;
            }

            //支持回收人模糊查询
            if (!Objects.isNull(detailVO.getContact()) && detailVO.getContact().indexOf(recPerson) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * 获取主管部门
     *
     * @param schoolBasicRO
     * @return
     */
    private static String getCompDep(SchoolBasicRO schoolBasicRO) {
        String departmentSlaveId = schoolBasicRO.getDepartmentSlaveId();
        if (GlobalConstant.REDIS_NULL_VALUE_STRING_DEFAULT.equalsIgnoreCase(departmentSlaveId)) {
            return null;
        }

        if (Objects.isNull(schoolBasicRO.getDepartmentMasterId())) {
            return "其他";
        }

        //-----
        Integer curCompDep = 0;
        if(schoolBasicRO.getDepartmentMasterId() == 0) {      //其他
			if(StringUtils.isNotEmpty(departmentSlaveId)) {
				curCompDep = Integer.parseInt(departmentSlaveId);
			}
			return  AppModConfig.compDepIdToNameMap0.get(String.valueOf(curCompDep));
		}
		else if(schoolBasicRO.getDepartmentMasterId() ==1) {      //部级   
			if(StringUtils.isNotEmpty(departmentSlaveId)) {
				curCompDep = Integer.parseInt(departmentSlaveId);
			}
			return  AppModConfig.compDepIdToNameMap1.get(String.valueOf(curCompDep));
		}
		else if(schoolBasicRO.getDepartmentMasterId() == 2) {      //市级
			if(StringUtils.isNotEmpty(departmentSlaveId)) {
				curCompDep = Integer.parseInt(departmentSlaveId);
			}
			return AppModConfig.compDepIdToNameMap2.get(String.valueOf(curCompDep));
		}
		else if(schoolBasicRO.getDepartmentMasterId() == 3) {      //区级
			if(StringUtils.isNotEmpty(departmentSlaveId)) {
				String orgName = AppModConfig.compDepIdToNameMap3bd.get(departmentSlaveId);
				if(orgName != null) {
					curCompDep = Integer.parseInt(AppModConfig.compDepNameToIdMap3.get(orgName));
				}
			}
			return AppModConfig.compDepIdToNameMap3.get(String.valueOf(curCompDep));
		}  

        return "其他";
    }

    /**
     * 解析redis废弃油脂详情列表信息
     *
     * @param keyMap
     * @return
     */
    private List<RecoveryWasteOilDetailRO> resolveSchoolRecoveryWasteOilDetailROList(Map<String, Date> keyMap) {
        Map<String, String> fieldMap;
        List<RecoveryWasteOilDetailRO> detailROList = new ArrayList<>();

        for (String key : keyMap.keySet()) {
            fieldMap = readRedis(key);
            if (Objects.isNull(fieldMap)) {
                continue;
            }

            for (String value : fieldMap.values()) {
                RecoveryWasteOilDetailRO detailRO = resolveSchoolRecoveryWasteOilDetailRO(value);
                if (Objects.isNull(detailRO)) {
                    continue;
                }

                detailRO.setRecDate(keyMap.get(key));

                detailROList.add(detailRO);
            }
        }

        return detailROList;
    }

    /**
     * 解析 redis中 回收废弃油脂详情模型
     *
     * @param value
     * @return
     */
    private RecoveryWasteOilDetailRO resolveSchoolRecoveryWasteOilDetailRO(String value) {

        if (value.indexOf(SPLIT_SYMBOL) == -1) {
            logger.info("redis：key={},value={} 值格式错误, 不包含分隔符“{}”, 解析失败。", RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_DETAIL, value, SPLIT_SYMBOL);
            return null;
        }

        int index = -1;
        RecoveryWasteOilDetailRO detailRO = new RecoveryWasteOilDetailRO();

        String[] splitArray = value.split(SPLIT_SYMBOL);

        /**
         * 格式(area_区号_schoolid_学校id_number_回收数量_receivername_回收单位_contact_回收人_documents_回收单据数量_seconttype_油脂类型)
         */
        if (splitArray.length < REDIS_RECOVERY_WASTE_OIL_DETAIL_RO_FORMAT_LENGTH) {
            logger.info("redis：key={},value={} 值格式错误, 数据项少于{}, 解析失败。", RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_DETAIL, value, REDIS_RECOVERY_WASTE_OIL_DETAIL_RO_FORMAT_LENGTH);
            return null;
        }

        index += ITERATE_STEP_SIZE;
        detailRO.setArea(RedisValueUtil.toInteger(splitArray[index]));

        index += ITERATE_STEP_SIZE;
        detailRO.setSchoolId(RedisValueUtil.filterString(splitArray[index]));

        index += ITERATE_STEP_SIZE;
        detailRO.setNumber(RedisValueUtil.toInteger(splitArray[index]));

        index += ITERATE_STEP_SIZE;
        detailRO.setReceiverName(RedisValueUtil.filterString(splitArray[index]));

        index += ITERATE_STEP_SIZE;
        detailRO.setContact(RedisValueUtil.filterString(splitArray[index]));

        index += ITERATE_STEP_SIZE;
        detailRO.setDocuments(RedisValueUtil.toInteger(splitArray[index]));

        index += ITERATE_STEP_SIZE;
        detailRO.setSecondType(RedisValueUtil.toInteger(splitArray[index]));

        return detailRO;
    }

    /**
     * 解析废弃油脂汇总 redis Key
     *
     * @param searchType
     * @return
     */
    private String resolveRedisKeyForWasteOilTotal(Integer searchType) {

        //如为空,默认查询学校油脂汇总
        if (Objects.isNull(searchType)) {
            return RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_TOTAL;
        }

        if (OwnerType.SCHOOL.getCode() == searchType.intValue()) {
            return RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_TOTAL;
        }

        if (OwnerType.RMC.getCode() == searchType.intValue()) {
            return RedisKeyConstant.RMC_RECOVERY_WASTE_OIL_TOTAL;
        }

        return RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_TOTAL;
    }

    /**
     * 解析废弃油脂详情 redis Key
     *
     * @param searchType
     * @return
     */
    private String resolveRedisKeyForWasteOilDetail(Integer searchType) {

        //如为空,默认查询学校油脂详情
        if (Objects.isNull(searchType)) {
            return RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_DETAIL;
        }

        if (OwnerType.SCHOOL.getCode() == searchType.intValue()) {
            return RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_DETAIL;
        }

        if (OwnerType.RMC.getCode() == searchType.intValue()) {
            return RedisKeyConstant.RMC_RECOVERY_WASTE_OIL_DETAIL;
        }

        return RedisKeyConstant.SCHOOL_RECOVERY_WASTE_OIL_DETAIL;
    }

    /**
     * 解析出 Redis 每日废弃油脂存储 Key。
     *
     * @param type
     * @param beginDate
     * @param endDate
     * @return
     */
    private Map<String, Date> genRedisKey(String type, Date beginDate, Date endDate) {

        Map<String, Date> keyMap = new HashMap<>();

        for (long i = beginDate.getTime(); i <= endDate.getTime(); i += DAY_TOTAL_MILLISECOND) {
            Date date = new Date(i);
            keyMap.put(String.format("%s_%s", dateFormat.format(date), type), date);
        }

        return keyMap;
    }

    /**
     * 读取redis
     *
     * @param key
     * @return
     */
    private Map<String, String> readRedis(String key) {
        return redisService.getHashByKey(SpringConfig.RedisRunEnvIdx, SpringConfig.RedisDBIdx, key);
    }

    /**
     * 解析每日回收废弃油脂汇总信息
     *
     * @param searchDTO 检索模型
     * @return
     */
    private List<RecoveryWasteOilSummaryVO> getDailyRecoveryWasteOilTotalList(RecoveryWasteOilSummarySearchDTO searchDTO) {
        //获取区/县编码
        String distNameCode = resolveDistNameCode(searchDTO.getDistName());
        boolean isFilterCode = !StringUtils.isEmpty(distNameCode);
        
        //区域集合过滤
        List<Object> distNamesList=CommonUtil.changeStringToList(searchDTO.getDistNames());
        if(distNamesList!=null && distNamesList.size() >0) {
        	isFilterCode = true;
        }
        String typeKey = resolveRedisKeyForWasteOilTotal(searchDTO.getSearchType());

        		
        Map<String, String> fieldMap;
        List<RecoveryWasteOilSummaryVO> result = new ArrayList<>();
        Map<String, Date> keyMap = genRedisKey(typeKey, searchDTO.getStartSubDate(), searchDTO.getEndSubDate());

        List<String> distCodeList = new ArrayList<>();
        
        for(String key : AppModConfig.distIdToNameMap.keySet()) {
        	 distCodeList.add(key);
        }

        for (String key : keyMap.keySet()) {
            fieldMap = readRedis(key);

            for (String distCode : distCodeList) {
            	if (isFilterCode) {
                    //过滤区/县信息
                    if (distNameCode!=null && !distCode.equalsIgnoreCase(distNameCode)) {
                        continue;
                    }else if(distNamesList!=null && distNamesList.size() >0) {
						if(!CommonUtil.isInteger(distCode)) {
							continue;
						}
						if(!distNamesList.contains(distCode)) {
							continue ;
						}
					}
                }
                RecoveryWasteOilSummaryVO oilsVO = generateRecoveryWasteOilSummaryVO(keyMap.get(key), fieldMap, distCode);
                if (oilsVO == null) {
                    continue;
                }
                result.add(oilsVO);
            }
        }       
        distCodeList.clear();

        return result;
    }

    /**
     * 生成废弃油脂汇总信息
     *
     * @param date
     * @param fieldMap
     * @param distCode
     * @return
     */
    private RecoveryWasteOilSummaryVO generateRecoveryWasteOilSummaryVO(Date date, Map<String, String> fieldMap, String distCode) {

        RecoveryWasteOilSummaryVO oilsVO = new RecoveryWasteOilSummaryVO();

        String distName = DictConvertUtil.mapToDistName(RedisValueUtil.filterString(distCode));
        if (StringUtils.isEmpty(distName)) {
            //忽略解析不到区信息的数据。
            return null;
        } else {
            oilsVO.setDistName(distName);
        }
        if(fieldMap.get(distCode) != null)
        	oilsVO.setRcNum(RedisValueUtil.toInteger(fieldMap.get(distCode)));
        else
        	oilsVO.setRcNum(0);
        if(fieldMap.get(distCode + RECOVERY_WASTE_OIL_SUMMARY_SUFFIX) != null)
        	oilsVO.setReFeq(RedisValueUtil.toInteger(fieldMap.get(distCode + RECOVERY_WASTE_OIL_SUMMARY_SUFFIX)));
        else
        	oilsVO.setReFeq(0);
        oilsVO.setRecDate(dateFormat2.format(date));

        return oilsVO;
    }

    /**
     * 解析出区/县Code信息
     *
     * @param distName 区/县编号
     * @return
     */
    private String resolveDistNameCode(String distName) {

        if (StringUtils.isEmpty(distName)) {
            return null;
        }

        if (Pattern.matches(RegexExpressConstant.REGEX_ALL_NUMBER_FORMAT, distName)) {
            return distName;
        }

        //如“黄浦区”
        return DictConvertUtil.mapToDistId(distName);
    }

}
