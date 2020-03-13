package com.tfit.BdBiProcSrvYgwcSchOmc.service.info.manage.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.BaseTests;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.SchoolRecoveryWasteOilSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.RecoveryWasteOilSummarySearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.info.manage.WasteOilTypeCodeSearchDTO;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.PagedList;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.Db1Service;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.info.manage.RecoveryWasteOilsService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * @Descritpion：
 * @author: yanzhao_xu
 * @date: 2019/1/10 20:19
 */
public class RecoveryWasteOilsServiceImplTest extends BaseTests {

    @Autowired
    private RecoveryWasteOilsService recoveryWasteOilsService;
    
    @Autowired
    private Db1Service db1Service;

    //@Test
    public void getSchoolRecoveryWasteOilTotalsForDistName() {

        RecoveryWasteOilSummarySearchDTO searchDTO = new RecoveryWasteOilSummarySearchDTO();
        searchDTO.setDistName("12");

        PagedList list = recoveryWasteOilsService.getSchoolRecoveryWasteOilSummary(searchDTO);

        assertNotNull(list);
    }

    @Test
    public void getWasteOilTypeCodes() {

        WasteOilTypeCodeSearchDTO searchDTO = new WasteOilTypeCodeSearchDTO();

        List list = recoveryWasteOilsService.getWasteOilTypeCodes(searchDTO);

        assertNotNull(list);
    }

    //@Test
    public void getSchoolRecoveryWasteOilDetails() {

        SchoolRecoveryWasteOilSearchDTO searchDTO = new SchoolRecoveryWasteOilSearchDTO();

        PagedList list = recoveryWasteOilsService.getSchoolRecoveryWasteOilDetails(searchDTO,db1Service);

        assertNotNull(list);
    }
}