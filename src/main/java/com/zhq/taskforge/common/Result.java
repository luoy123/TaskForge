package com.zhq.taskforge.common;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

//    设置时间戳为了接口调试，以及日志输出时查看时间等信息，同时LocalDataTime 比long类型的时间戳可读性好。
    private LocalDateTime timestamp;


    private Result(){
        this.timestamp = LocalDateTime.now();
    }

    public Result(Integer code, String message,T data){
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(ResultCode resultCode, T data){
        this();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

}
