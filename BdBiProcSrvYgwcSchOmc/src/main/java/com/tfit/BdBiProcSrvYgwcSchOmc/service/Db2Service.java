package com.tfit.BdBiProcSrvYgwcSchOmc.service;

import java.util.List;

import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdMenuDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdRoleDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduBdUserPermDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.TEduSuperviseUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdInterfaceColumnsDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.EduBdUserDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdBriKitStoveDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdMailSrvDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdMsgNoticeDo;
import com.tfit.BdBiProcSrvYgwcSchOmc.dao.domain.edubd.TEduBdNoticeStatusDo;

public interface Db2Service {	
    //从数据源ds2的数据表t_edu_supervise_user中查找用户名和密码（sha1字符串）以用户名（账号）
    TEduSuperviseUserDo getUserNamePassByUserName(String userName);
    
    //更新生成的token到数据源ds2的数据表t_edu_supervise_user表中
    boolean updateUserTokenToTEduSuperviseUser(String userName, String password, String token);
    
    //从数据源ds2的数据表t_edu_supervise_user中查找授权码以当前授权码
    String getAuthCodeByCurAuthCode(String token);
    
    //从数据源ds2的数据表t_edu_bd_user中查找用户信息
    TEduBdUserDo getBdUserInfoByUserName(String userName);
    
    //从数据源ds2的数据表t_edu_supervise_user中查找授权码以当前授权码
    TEduBdUserDo getBdUserInfoByCurAuthCode(String token);
    
    //从数据源ds2的数据表t_edu_bd_user中查找用户信息以单位ID
    List<TEduBdUserDo> getBdUserInfoByUserOrg(String orgId);
    
    //从数据源ds2的数据表t_edu_bd_user中查找授权码以当前授权码
    String getAuthCodeByCurAuthCode2(String token);
    
    //从数据源ds2的数据表t_edu_bd_user中查找所有用户信息
    List<TEduBdUserDo> getAllBdUserInfo();
    
    //从数据源ds2的数据表t_edu_bd_user中查找所有用户信息以父账户
    List<TEduBdUserDo> getAllBdUserInfoByParentId(String parentId);
    
    //插入记录到数据源ds2的数据表t_edu_bd_user中
    boolean InsertBdUserInfo(TEduBdUserDo tebuDo);
    
    //更新记录到数据源ds2的数据表t_edu_bd_user中
    boolean UpdateBdUserInfo(TEduBdUserDo tebuDo, String token);
    
    //更新记录到数据源ds2的数据表t_edu_bd_user中以输入字段
    boolean UpdateBdUserInfoByField(TEduBdUserDo tebuDo, String fieldName, String fieldVal);
    
    //删除数据源ds2的数据表t_edu_bd_user中记录以用户名
    boolean DeleteBdUserInfoByUserName(String userName);
    
    //从数据源ds2的数据表t_edu_bd_user中查找用户信息以授权码token
    TEduBdUserDo getBdUserInfoByToken(String token);
    
    //插入记录到数据源ds2的数据表t_edu_bd_role中
    boolean InsertBdRoleInfo(TEduBdRoleDo tebrDo);
    
    //更新记录到数据源ds2的数据表t_edu_bd_role中以输入字段
    boolean UpdateBdRoleInfoByField(TEduBdRoleDo tebrDo, String fieldName, String fieldVal);
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以id
    TEduBdRoleDo getBdRoleInfoByRoleId(String id);
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    TEduBdRoleDo getBdRoleInfoByRoleName(String roleName);
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    List<TEduBdRoleDo> getBdRoleInfoByRoleName2(String roleName);
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    TEduBdRoleDo getBdRoleInfoByRoleName3(int roleType, String roleName);
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色信息以角色名称
    List<TEduBdRoleDo> getBdRoleInfoByRoleName4(int roleType, String roleName);
    
    //从数据源ds2的数据表t_edu_bd_role中查找所有角色名称
    List<TEduBdRoleDo> getBdRoleInfoAllRoleNames();
    
    //从数据源ds2的数据表t_edu_bd_role中查找角色名称以角色类型
    List<TEduBdRoleDo> getBdRoleInfoRoleNamesByRoleType(int roleType);
    
    //从数据源ds2的数据表t_edu_bd_role中查找所有角色信息
    List<TEduBdRoleDo> getAllBdRoleInfo();
    
    //删除数据源ds2的数据表t_edu_bd_role中记录以角色名
    boolean DeleteBdRoleInfoByRoleName(String roleName);
    
    //插入记录到数据源ds2的数据表t_edu_bd_user_perm中
    boolean InsertBdUserPermInfo(TEduBdUserPermDo tebrpDo);
    
    //插入记录到数据源ds2的数据表t_edu_bd_user_perm中
    boolean InsertBdUserPermInfo(List<TEduBdUserPermDo> tebrpDoList);
    
    //更新记录到数据源ds2的数据表t_edu_bd_user_perm中以输入字段
    boolean UpdateBdRolePermInfoByField(TEduBdUserPermDo tebrpDo, String fieldName, String fieldVal);
    
    //删除数据源ds2的数据表t_edu_bd_user_perm中记录以用户名
    boolean DeleteBdUserPermInfoByUserId(String userId);
    
    //从数据源ds2的数据表t_edu_bd_user_perm中查找所有用户权限信息
    List<TEduBdUserPermDo> getAllBdUserPermInfo(String userId, int permType);
    
    //从数据源ds2的数据表t_edu_bd_menu中查找菜单信息以菜单级别
    List<TEduBdMenuDo> getBdMenuInfoByLevel(int level);
    
    //从数据源ds2的数据表t_edu_bd_menu中查找菜单信息以菜单级别和父菜单ID
    List<TEduBdMenuDo> getBdMenuInfoByLevel(int level, String parentId);
    
    //插入消息通知记录
    int insertMsgNotice(TEduBdMsgNoticeDo tebmnDo);
    
    //获取所有用户名、单位ID、单位名称记录信息
    List<EduBdUserDo> getAllUserInfos();
    
    //插入消息通知状态记录
    int insertMsgNoticeStatus(TEduBdNoticeStatusDo tebnsDo);
    
    //查询消息通知状态记录列表以接收用户名
    List<TEduBdNoticeStatusDo> getMsgNoticeStatusByRcvUserName(String rcvUserName);
    
    //查询消息通知记录以通知id
    TEduBdMsgNoticeDo getMsgNoticeById(String id);
    
    //查询消息通知状态记录列表以接收用户名
    List<TEduBdNoticeStatusDo> getMsgNoticeStatusBySendUserName(String sendUserName);
    
    //查询消息通知状态记录列表以通知ID和发布用户名
    List<TEduBdNoticeStatusDo> getMsgNoticeStatusBybIdSendUser(String bulletinId, String sendUserName);
    
    //查询消息通知状态记录列表以通知id和接收用户名
    TEduBdNoticeStatusDo getMsgNoticeStatusBybIdRcvUserName(String bulletinId, String rcvUserName);
  	
  	//更新阅读次数
    int updateReadCountInMsgNotice(String bulletinId, String rcvUserName, int readCount);
    
    //查询消息通知当前上一条记录以当前通知id
    TEduBdMsgNoticeDo getPreMsgNoticeById(String id);
    
    //查询消息通知当前下一条记录以当前通知id
    TEduBdMsgNoticeDo getNextMsgNoticeById(String id);
    
    //查询所有子用户记录信息以父用户id
    List<EduBdUserDo> getAllSubUserInfosByParentId(String parentId);
    
    //查询用户记录信息以用户id
    EduBdUserDo getUserInfoByUserId(String id);
    
    //查询消息通知当前上一条记录以当前通知id和接收用户名（接收用户名字串前后添加%）
  	TEduBdMsgNoticeDo getPreMsgNoticeByIdRcvUserName(String id, String rcvUserName);
  	
  	//查询消息通知当前下一条记录以当前通知id和接收用户名（接收用户名字串前后添加%）
  	TEduBdMsgNoticeDo getNextMsgNoticeByIdRcvUserName(String id, String rcvUserName);  	
  	
  	//查询消息通知当前上一条记录以当前通知id和接收用户名（发送用户名）
  	TEduBdMsgNoticeDo getPreMsgNoticeByIdSendUserName(String id, String sendUserName);
  			
  	//查询消息通知当前下一条记录以当前通知id和接收用户名（发送用户名）
  	TEduBdMsgNoticeDo getNextMsgNoticeByIdSendUserName(String id, String sendUserName);
  	
  	//查询邮件服务记录以用户名
  	TEduBdMailSrvDo getMailSrvInfoByUserName(String userName);
  	
  	//插入邮件服务记录
  	int insertMailSrv(TEduBdMailSrvDo tebmsDo);
  	
  	//更新邮件服务记录
  	boolean updateMailSrv(TEduBdMailSrvDo tebmsDo);
  	
  	//查询学校视频监控记录信息以学校id
    List<TEduBdBriKitStoveDo> getSchVidSurvInfosBySchId(String schoolId);
    
    //查询所有学校视频监控记录信息
    List<TEduBdBriKitStoveDo> getAllSchVidSurvInfos();
    
    List<TEduBdBriKitStoveDo> getSchVidSurvInfosByDistId(String regionId);
    
    /**
     * 插入用户设置动态列
     * @param record
     * @return
     */
    public int addUserInterfaceColums(EduBdInterfaceColumnsDo record) ;

    /**
     * 根据主键修改用户设置的动态列
     * @param record
     * @return
     */
    public int updateUserInterfaceColumsByPrimaryKey(EduBdInterfaceColumnsDo record);
    
    
    /**
     * 根据接口名称查询对应的列设置
     * @param interfaceName
     * @return
     */
    public EduBdInterfaceColumnsDo getByInterfaceName(String userId,String interfaceName);
    
    /**
     * 获取t_edu_bd_interface_columns最大的编号
     * @return
     */
    public Integer getInterfaceColumnsMaxId();
    
}
