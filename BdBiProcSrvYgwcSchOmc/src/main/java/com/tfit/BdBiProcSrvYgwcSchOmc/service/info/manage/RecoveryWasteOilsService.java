package com.tfit.BdBiProcSrvYgwcSchOmc.service.info.manage;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.RmcRecoveryWasteOilSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.SchoolRecoveryWasteOilSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.RecoveryWasteOilSummarySearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.WasteOilTypeCodeSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.RmcRecoveryWasteOilDetailsExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.RmcRecoveryWasteOilSummaryExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.SchoolRecoveryWasteOilDetailsExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.export.SchoolRecoveryWasteOilSummaryExport;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.PagedList;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;

import java.util.List;

/**
 * @Descritpion：回收废弃油脂服务
 * @author: tianfang_infotech
 * @date: 2019/1/3 11:53
 */
public interface RecoveryWasteOilsService {

    /**
     * 获取废弃油脂类型编码列表
     * @param searchDTO
     * @return
     */
    List<?> getWasteOilTypeCodes(WasteOilTypeCodeSearchDTO searchDTO);

    /**
     * 每个区所有学校回收废弃油脂汇总列表
     * @param searchDTO
     * @return
     */
    PagedList<?> getSchoolRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO);

    /**
     * 导出每个区所有学校回收废弃油脂汇总列表
     * @param searchDTO
     * @return
     */
    SchoolRecoveryWasteOilSummaryExport exportSchoolRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO);

    /**
     * 获取学校废弃油脂详情列表
     * @param searchDTO
     * @return
     */
    PagedList<?> getSchoolRecoveryWasteOilDetails(SchoolRecoveryWasteOilSearchDTO searchDTO,Db1Service db1Service);

    /**
     * 导出学校废弃油脂详情列表
     * @param searchDTO
     * @return
     */
    SchoolRecoveryWasteOilDetailsExport exportSchoolRecoveryWasteOilDetails(SchoolRecoveryWasteOilSearchDTO searchDTO,Db1Service db1Service);

    /**
     * 每个区所有团餐公司回收废弃油脂汇总列表
     * @param searchDTO
     * @return
     */
    PagedList<?> getRmcRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO);

    /**
     * 导出每个区所有团餐公司回收废弃油脂汇总列表
     * @param searchDTO
     * @return
     */
    RmcRecoveryWasteOilSummaryExport exportRmcRecoveryWasteOilSummary(RecoveryWasteOilSummarySearchDTO searchDTO);

    /**
     * 获取团餐公司废弃油脂详情列表
     * @param searchDTO
     * @return
     */
    PagedList<?> getRmcRecoveryWasteOilDetails(RmcRecoveryWasteOilSearchDTO searchDTO);

    /**
     * 导出团餐公司废弃油脂详情列表
     * @param searchDTO
     * @return
     */
    RmcRecoveryWasteOilDetailsExport exportRmcRecoveryWasteOilDetails(RmcRecoveryWasteOilSearchDTO searchDTO);
}
