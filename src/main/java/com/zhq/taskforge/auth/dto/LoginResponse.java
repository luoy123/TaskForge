package com.zhq.taskforge.auth.dto;

import lombok.Data;

/**
 * 返回给前端的数据
 */
@Data
public class LoginResponse {

    private String token;

    private Long userId;

    private String userName;

    private String nickName;
}
