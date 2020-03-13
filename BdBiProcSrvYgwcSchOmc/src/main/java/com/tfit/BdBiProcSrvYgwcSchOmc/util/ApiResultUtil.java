package com.tfit.BdBiProcSrvYgwcSchOmc.util;

import com.tfit.BdBiProcSrvYgwcSchOmc.dto.IOTRspType;
import com.tfit.BdBiProcSrvYgwcSchOmc.dto.PageInfo;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.ApiExceptionResult;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.ApiResult;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.ExceptionContent;
import com.tfit.BdBiProcSrvYgwcSchOmc.model.vo.base.PagedList;

/**
 * @Descritpion：接口返回状态工具类
 * @author: tianfang_infotech
 * @date: 2019/1/3 13:33
 */
public class ApiResultUtil {

    /**
     * Api调用成功
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> ApiResult<T> success(T t) {
        return new ApiResult<>(t);
    }

    /**
     * Api调用成功
     *
     * @param t
     * @param <T>
     * @return
     */
    public static <T> ApiResult<T> success(T t, PageInfo pageInfo) {
        return new ApiResult<>(t, pageInfo);
    }

    public static <T> ApiResult<T> success(PagedList<T> pagedList) {
        return new ApiResult<>((T) pagedList.getData(), pagedList.getPageInfo());
    }

    /**
     * Api调用失败
     *
     * @param iotRspType
     * @param message
     * @return
     */
    public static ApiExceptionResult<ExceptionContent> failure(IOTRspType iotRspType, String message) {

        return new ApiExceptionResult<>(new ExceptionContent(iotRspType, message));
    }
}
