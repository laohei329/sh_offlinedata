package com.tfit.BdBiProcSrvYgwcSchOmc.service.edubd;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdRoleDo;

public interface EduBdService {
	//查询角色信息以角色类型和角色名称
    TEduBdRoleDo getBdRoleInfoByRoleName(int roleType, String roleName);
}
