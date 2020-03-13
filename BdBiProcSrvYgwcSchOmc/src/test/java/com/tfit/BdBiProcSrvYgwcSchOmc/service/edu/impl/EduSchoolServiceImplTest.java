package com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.BaseTests;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.EduSchool;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edu.EduSchoolService;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

public class EduSchoolServiceImplTest extends BaseTests {

    @Autowired
    private EduSchoolService eduSchoolService;

    @Test
    public void getEduSchools() {

        List<EduSchool> list = eduSchoolService.getEduSchools();

        Assert.assertTrue(!CollectionUtils.isEmpty(list));
    }

}