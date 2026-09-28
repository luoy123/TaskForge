package com.zhq.taskforge.system.domain;

import lombok.Data;

@Data
/** 用于前端展示 */
public class SysUserOnline {
    /** 会话编号 = LoginUser.token = Redis key 里的 uuid */
    private String tokenId;
    private String userName;
    private String deptName;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    /** 登录时间（毫秒时间戳，和 LoginUser.loginTime 一致） */
    private Long loginTime;
}
