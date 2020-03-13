package com.tfit.BdBiProcSrvYgwcSchOmc.service.edubd;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.BasicBdUser;

/**
 * @Descritpion：用户信息
 * @author: tianfang_infotech
 * @date: 2019/1/23 11:31
 */
public interface EduBdUserService {

    /**
     * 查找大数据系统用户信息
     * @param token 授权码
     * @return
     */
    EduBdUserDo getEduBdUser(String token);

    /**
     * 查找大数据系统用户信息
     * @param token 授权码
     * @return
     */
    BasicBdUser getBasicBdUser(String token);
}
