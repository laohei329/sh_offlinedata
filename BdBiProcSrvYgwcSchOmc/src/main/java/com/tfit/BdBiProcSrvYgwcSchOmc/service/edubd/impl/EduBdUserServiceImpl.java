package com.tfit.BdBiProcSrvYgwcSchOmc.service.edubd.impl;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd.EduBdUserDoMapper;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.BasicBdUser;
import com.tfit.BdBiProcSrvYgwcSchOmc.service.edubd.EduBdUserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Descritpion：大数据系统用户服务实现
 * @author: tianfang_infotech
 * @date: 2019/1/23 11:33
 */
@Service
public class EduBdUserServiceImpl implements EduBdUserService {

    @Autowired
    private EduBdUserDoMapper eduBdUserMapper;

    @Override
    public EduBdUserDo getEduBdUser(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return eduBdUserMapper.findBdUser(token);
    }

    @Override
    public BasicBdUser getBasicBdUser(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        return eduBdUserMapper.findBasicBdUser(token);
    }
}
