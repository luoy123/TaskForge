package com.zhq.taskforge.config;

import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

//    未捕获的异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("System exception",e);
        return new Result<>(ResultCode.ERROR,null);
    }

    //捕获业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException businessException)
    {
        log.error("业务异常：{}",businessException.getMessage());
        return new Result<>(businessException.getCode(),businessException.getMessage(),null);
    }

}
