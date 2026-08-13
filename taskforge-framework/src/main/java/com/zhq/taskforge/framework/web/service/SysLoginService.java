package com.zhq.taskforge.framework.web.service;

import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginBody;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ISysUserService userService;

    public String login(LoginBody loginBody) {
        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(loginBody.getUsername(), loginBody.getPassword());
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            throw new ServiceException("用户密码错误");
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
    }

    public String loginSso(LoginUser loginUser) {
        return tokenService.createLongTimeToken(loginUser);
    }
}
