package com.zhq.taskforge.common;

import lombok.Getter;

/**
 * 返回状态码枚举
 *
 * @author zhq
 */
@Getter
public enum ResultCode {

    // ========== 成功状态码 ==========
    SUCCESS(200, "操作成功"),

    // ========== 客户端错误 4xx ==========
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有权限"),
    NOT_FOUND(404, "资源不存在"),

    // ========== 服务端错误 5xx ==========
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),

    // ========== 业务错误码 1xxx ==========
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_DISABLED(1002, "用户已被禁用"),
    PASSWORD_ERROR(1003, "密码错误"),
    USERNAME_EXISTS(1004, "用户名已存在"),

    DEPT_HAS_CHILDREN(1101, "存在下级部门，不允许删除"),
    DEPT_HAS_USERS(1102, "部门下存在用户，不允许删除"),
    DEPT_NAME_EXISTS(1103, "部门名称已存在"),

    ROLE_HAS_USERS(1201, "角色下存在用户，不允许删除"),
    ROLE_NAME_EXISTS(1202, "角色名称已存在"),
    ROLE_KEY_EXISTS(1203, "角色标识已存在"),

    MENU_HAS_CHILDREN(1301, "存在下级菜单，不允许删除"),
    MENU_NAME_EXISTS(1302, "菜单名称已存在"),

    DICT_TYPE_EXISTS(1401, "字典类型已存在"),
    DICT_DATA_EXISTS(1402, "字典数据已存在"),
    DICT_IS_SYSTEM(1403, "系统内置字典，不允许删除"),

    CONFIG_IS_SYSTEM(1501, "系统内置参数，不允许删除"),
    CONFIG_KEY_EXISTS(1502, "参数键名已存在");

    /** 状态码 */
    private final Integer code;

    /** 消息 */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
