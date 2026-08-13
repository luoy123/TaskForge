package com.zhq.taskforge.common.core.domain;

import com.zhq.taskforge.common.constant.HttpStatus;

import java.io.Serializable;

public class R<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final int SUCCESS = HttpStatus.SUCCESS;
    public static final int FAIL = HttpStatus.ERROR;

    private int code;
    private String message;
    private T data;

    public R() {
    }

    public R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> R<T> ok() {
        return restResult(null, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, SUCCESS, "操作成功");
    }

    public static <T> R<T> ok(String message, T data) {
        return restResult(data, SUCCESS, message);
    }

    public static <T> R<T> fail() {
        return restResult(null, FAIL, "操作失败");
    }

    public static <T> R<T> fail(String message) {
        return restResult(null, FAIL, message);
    }

    public static <T> R<T> fail(int code, String message) {
        return restResult(null, code, message);
    }

    private static <T> R<T> restResult(T data, int code, String message) {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMessage(message);
        return apiResult;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
