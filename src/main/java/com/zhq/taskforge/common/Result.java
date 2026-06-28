package com.zhq.taskforge.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一返回结果
 *
 * @author zhq
 */
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码 */
    private Integer code;

    /** 消息 */
    private String message;

    /** 数据 */
    private T data;

    /** 时间戳 */
    private Long timestamp;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    // ========== 构造方法 ==========

    public Result(Integer code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(ResultCode resultCode, T data) {
        this();
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.data = data;
    }

    public Result(ResultCode resultCode) {
        this(resultCode, null);
    }

    // ========== 静态方法 - 成功 ==========

    /**
     * 成功（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS);
    }

    /**
     * 成功（有数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS, data);
    }

    /**
     * 成功（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    // ========== 静态方法 - 失败 ==========

    /**
     * 失败（默认消息）
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.INTERNAL_ERROR);
    }

    /**
     * 失败（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.INTERNAL_ERROR.getCode(), message, null);
    }

    /**
     * 失败（指定状态码）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode);
    }

    /**
     * 失败（指定状态码和消息）
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message, null);
    }

    /**
     * 失败（自定义状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}
