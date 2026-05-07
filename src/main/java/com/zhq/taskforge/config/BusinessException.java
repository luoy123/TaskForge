package com.zhq.taskforge.config;

import com.zhq.taskforge.common.ResultCode;
import lombok.Getter;

/**
 * 自定义异常
 */
@Getter
public class BusinessException extends RuntimeException{

    private final Integer code;
    private final String message;

    public BusinessException(String message){
        super(message);
        this.code = ResultCode.ERROR.getCode();
        this.message = message;
    }

}
