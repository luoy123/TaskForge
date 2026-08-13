package com.zhq.taskforge.framework.web.exception;

import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public R<Void> handleServiceException(ServiceException e) {
        log.error("业务异常：{}", e.getMessage());
        return R.fail(e.getCode() != null ? e.getCode() : 500, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("System exception", e);
        // Surface root message so API clients can diagnose (still HTTP 200 + business code 500)
        String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        return R.fail("系统内部错误: " + detail);
    }
}
