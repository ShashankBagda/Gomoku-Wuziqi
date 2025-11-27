package com.goody.nus.se.gomoku.web.base.response;

import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通用返回值包装类
 *
 * @param <T> 返回值类型
 * @author liujingcheng
 * @version 1.0, 2020/3/6 15:41
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming
public class ApiResult<T> implements Serializable {
    private boolean success;
    private int errorCode;
    private T data;
    private String errorMsg;

    public static <T> ApiResult<T> success() {
        return new ApiResult<>(true, ErrorCodeEnum.OK.getErrorCode(), null, ErrorCodeEnum.OK.getMessage());
    }

    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(true, ErrorCodeEnum.OK.getErrorCode(), data, ErrorCodeEnum.OK.getMessage());
    }

    public static <T> ApiResult<T> failed(ErrorCodeEnum errorCode) {
        return new ApiResult<>(false, errorCode.getErrorCode(), null, errorCode.getMessage());
    }

    public static <T> ApiResult<T> failed(int errorCode, String errorMsg) {
        return new ApiResult<>(false, errorCode, null, errorMsg);
    }
}
