package com.goody.nus.se.gomoku.common.exception;

import com.goody.nus.se.gomoku.common.errorcode.ErrorCodeEnum;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Business Exception
 *
 * @author Goody
 * @version 1.0, 2022/11/9 20:06
 * @since 1.0.0
 */
@Getter
@Setter
@ToString
public class BizException extends RuntimeException {

    private ErrorCodeEnum errorCode;
    private Object[] args;

    public BizException(ErrorCodeEnum errorCode, Object... args) {
        super(errorCode.getErrorCode() + "", null, true, true);
        this.errorCode = errorCode;
        this.args = args;
    }
}
