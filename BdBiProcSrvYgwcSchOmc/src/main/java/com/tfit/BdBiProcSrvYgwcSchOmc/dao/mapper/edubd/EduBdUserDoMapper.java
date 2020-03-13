package com.tfit.BdBiProcSrvYgwcSchOmc.dao.mapper.edubd;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.BasicBdUser;

import java.util.List;

import org.apache.ibatis.annotations.Param;

/**
 * @Descritpion：大数据用户信息映射类
 * @author: tianfang_infotech
 * @date: 2019/1/23 11:12
 */
public interface EduBdUserDoMapper {

    /**
     * 查询 token 对应用户信息
     *
     * @return
     */
    EduBdUserDo findBdUser(@Param("token") String token);

    /**
     * 查询 token 对应基本用户信息
     *
     * @return
     */
    BasicBdUser findBasicBdUser(@Param("token") String token);
    
    //获取所有用户名、单位ID、单位名称记录信息
    List<EduBdUserDo> getAllUserInfos();
    
    //查询所有子用户记录信息以父用户id
    List<EduBdUserDo> getAllSubUserInfosByParentId(@Param("parentId") String parentId);
    
    //查询用户记录信息以用户id
    EduBdUserDo getUserInfoByUserId(@Param("id") String id);
}
