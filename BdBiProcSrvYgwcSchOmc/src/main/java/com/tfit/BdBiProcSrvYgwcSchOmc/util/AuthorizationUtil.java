package com.tfit.BdBiProcSrvYgwcSchOmc.util;

import com.tfit.BdBiProcSrvYgwcSchOmc.context.UserContextHandler;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTRspType;
import com.tfit.BdBiProcSrvYgwcSchOmc.exception.BusinessException;
import org.springframework.util.StringUtils;

/**
 * @Descritpion：身份验证工具类
 * @author: tianfang_infotech
 * @date: 2019/1/3 10:59
 */
public class AuthorizationUtil {

    /**
     * 身份验证处理(没有token信息，则身份验证失败)
     *
     * @return
     */
    public static void authCheckProcess() {
        if (StringUtils.isEmpty(UserContextHandler.getToken())) {
            throw new BusinessException(IOTRspType.AUTHCODE_CHKERR);
        }
    }

    /**
     * Mock身份验证处理
     *
     * @param token
     * @return
     */
    public static void mockAuthCheckProcess(String token) {

    }
}
