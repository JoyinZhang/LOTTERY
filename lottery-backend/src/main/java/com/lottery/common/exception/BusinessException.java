package com.lottery.common.exception;

import com.lottery.common.constant.ResultCode;
import lombok.Getter;

/**
 * 业务异常
 *
 * @author lottery
 * @since 2024-12-16
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.SYSTEM_ERROR.getCode();
    }
}
