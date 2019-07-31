package com.tfit.BdBiProcSrvYgwcSchOmc.service.saas.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.saas.EduSchoolSupplier;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.saas.EduSchoolSupplierMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.SupplierBasic;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.saas.EduSchoolSupplierService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Descritpion：教委学校供应商服务实现类
 * @author: tianfang_infotech
 * @date: 2019/1/10 13:36
 */
@Service
public class EduSchoolSupplierServiceImpl implements EduSchoolSupplierService {

    @Autowired
    private EduSchoolSupplierMapper schoolSupplierMapper;

    @Override
    public List<EduSchoolSupplier> getAllSchoolSuppliers() {
        return schoolSupplierMapper.findAllSuppliers();
    }

    @Override
    public List<EduSchoolSupplier> getSchoolSuppliers(String schoolId) {
        if (StringUtils.isBlank(schoolId)) {
            return null;
        }
        return schoolSupplierMapper.findSuppliersBySchoolId(schoolId);
    }

    @Override
    public SupplierBasic getSupplierBasicById(String supplierId) {
        if (StringUtils.isBlank(supplierId)) {
            return null;
        }
        return schoolSupplierMapper.findSupplierBasicById(supplierId);
    }

    @Override
    public List<SupplierBasic> getAllSupplierBasics() {
        return schoolSupplierMapper.findAllSupplierBasics();
    }
}
