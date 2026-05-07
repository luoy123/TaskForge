package com.zhq.taskforge.auth.service;

import com.zhq.taskforge.auth.dto.LoginRequest;
import com.zhq.taskforge.auth.dto.LoginResponse;

public interface AuthService  {

    /**
     * 用户登录
     * @param loginRequest
     * @return
     */
    public LoginResponse login(LoginRequest loginRequest);
}
