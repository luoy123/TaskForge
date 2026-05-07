package com.zhq.taskforge.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 接受前端的请求参数
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String userName;
    @NotBlank(message = "密码不能为空")
    private String password;


}
