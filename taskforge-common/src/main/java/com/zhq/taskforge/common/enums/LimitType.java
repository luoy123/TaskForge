package com.zhq.taskforge.common.enums;

/**
 * 限流维度
 * LimitType
 */
public enum LimitType {

    // 全局限流：同一个接口用同一个计数器
    DEFAULT,
    // 按照Ip
    IP,
    // 按照登录的用户
    USER
}
