package com.tfit.BdBiProcSrvYgwcSchOmc.service.saas.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.BaseTests;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SupplierIdDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.SaasService;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.impl.SaasServiceImpl;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.saas.GroupMealCompanyService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Descritpion：
 * @author: yanzhao_xu
 * @date: 2019/1/9 18:49
 */
public class GroupMealCompanyServiceImplTest extends BaseTests {

    @Autowired
    private GroupMealCompanyService groupMealCompanyService;

    @Autowired
    private SaasService saasService;

    @Test
    public void getAllSupplierIds() {

        List<SupplierIdDo> list = groupMealCompanyService.getAllSupplierIds();

        Assert.assertTrue(!CollectionUtils.isEmpty(list));
    }

    @Test
    public void getAllSupplierIds1() {

        List<SupplierIdDo> list = saasService.getAllSupplierId();

        Assert.assertTrue(!CollectionUtils.isEmpty(list));
    }
}