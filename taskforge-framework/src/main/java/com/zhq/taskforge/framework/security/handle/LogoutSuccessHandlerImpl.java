package com.zhq.taskforge.framework.security.handle;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.utils.ServletUtils;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.framework.web.service.TokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler {
    @Autowired
    private TokenService tokenService;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        LoginUser loginUser = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginUser)) {
            tokenService.delLoginUser(loginUser.getToken());
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("code", 200);
        result.put("message", "退出成功");
        ServletUtils.renderString(response, new ObjectMapper().writeValueAsString(result));
    }
}
