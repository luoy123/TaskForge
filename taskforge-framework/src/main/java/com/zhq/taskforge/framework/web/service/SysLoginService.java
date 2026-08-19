package com.zhq.taskforge.framework.web.service;

import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginBody;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.framework.manager.AsyncManager;
import com.zhq.taskforge.framework.manager.factory.AsyncFactory;
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
        // 1.取出用户名，失败的时候需要记录登录日志
        String username = loginBody.getUsername();

        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    loginBody.getUsername(), loginBody.getPassword());
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (BadCredentialsException e) {
            // 2.密码失败，则先异步记录失败日志，在给前端抛出异常
            AsyncManager.me.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "用户密码错误"));
            throw new ServiceException("用户密码错误");
        } catch (Exception e) {
            // 3.其他异常，同样记录失败
            AsyncManager.me.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
            throw new ServiceException(e.getMessage());
        }

        // 4.认证成功，记录成功日志
        AsyncManager.me.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, "登录成功"));

        // 5.发送jwt令牌
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return tokenService.createToken(loginUser);
    }

    public String loginSso(LoginUser loginUser) {
        return tokenService.createLongTimeToken(loginUser);
    }
}
