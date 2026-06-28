package com.zhq.taskforge.auth.controller;

import com.zhq.taskforge.auth.dto.LoginRequest;
import com.zhq.taskforge.auth.dto.LoginResponse;
import com.zhq.taskforge.auth.service.AuthService;
import com.zhq.taskforge.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录模块的登录接口
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "认证登录模块")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = authService.login(loginRequest);
        return Result.success(loginResponse);
    }
}
