package com.tfit.BdBiProcSrvYgwcSchOmc.service;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SchIdNameDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SchOptModeDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SchOwnershipDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.SchTypeDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edu.TEduDistrictDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSchoolDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSuperviseUserDo;

public interface Db1Service {
	 //从数据库test_edu的数据表t_edu_district中查找id和区域名称
    List<TEduDistrictDo> getListByDs1IdName();
    
    //从数据库test_edu的数据表t_edu_school中查找level以id
    SchOptModeDo getSchOptModeByDs1Id(String id);
    
    //从数据库test_edu的数据表t_edu_school中查找property以id
    SchOwnershipDo getSchOwnByDs1Id(String id);
    
    //从数据库test_edu的数据表t_edu_school中查找level以id
    SchTypeDo getSchTypeByDs1Id(String id);
    
    //从数据库test_edu的数据表t_edu_school中查找所有id
    List<SchIdNameDo> getSchIdListByDs1(String distId);
    
    //从数据源ds1的数据表t_edu_school中查找所有id以区域ID（空时在查询所有）和输出字段方法
    List<TEduSchoolDo> getTEduSchoolDoListByDs1(String distId, int outMethod);
    
    //从数据源ds1的数据表t_edu_school中查找所有id以区域ID（空时在查询所有）和输出字段方法
    List<TEduSchoolDo> getTEduSchoolDoListByDs1(List<Object> distIdList);
    
    //从数据源ds1的数据表t_edu_school中查找所有总校id以区域ID（空时在查询所有）
    List<TEduSchoolDo> getGenSchIdNameListByDs1(String distId);
    
    //从数据源ds1的数据表t_edu_supervise_user中查找用户名和密码（sha1字符串）以用户名（账号）
    TEduSuperviseUserDo getUserNamePassByUserName(String userName);
    
    //更新生成的token到数据源ds1的数据表t_edu_supervise_user表中
    boolean updateUserTokenToTEduSuperviseUser(String userName, String password, String token);
    
    //从数据源ds1的数据表t_edu_supervise_user中查找授权码以当前授权码
    String getAuthCodeByCurAuthCode(String token);
}
