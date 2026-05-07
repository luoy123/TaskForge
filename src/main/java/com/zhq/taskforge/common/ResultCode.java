package com.zhq.taskforge.common;

import lombok.Getter;

@Getter
public enum ResultCode {


    SUCCESS(200,"操作成功"),
    ERROR(500,"操作失败"),
    NOT_FOUND(404,"资源不存在");


    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message){
        this.code = code;
        this.message = message;
    }
}
