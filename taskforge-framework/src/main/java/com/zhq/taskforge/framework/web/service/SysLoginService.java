package com.zhq.taskforge.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.core.domain.model.LoginBody;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.core.redis.RedisCache;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.framework.manager.AsyncManager;
import com.zhq.taskforge.framework.manager.factory.AsyncFactory;
import com.zhq.taskforge.system.service.ISysConfigService;
import com.zhq.taskforge.system.service.ISysUserService;

@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    public String login(LoginBody loginBody) {
        // 1.取出用户名，失败的时候需要记录登录日志
        String username = loginBody.getUsername();

        // 2. 验证码校验必须在密码认证之前
        String captchaEnabledStr = configService.selectSysConfigByKey("sys.account.captchaEnabled");
        boolean captchaEnabled = "true".equalsIgnoreCase(captchaEnabledStr);
        if (captchaEnabled) {
            validateCaptcha(username, loginBody.getCode(), loginBody.getUuid());
        }

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

    private void validateCaptcha(String username, String code, String uuid) {

        // 前端没传验证码
        if (StringUtils.isEmpty(code) || StringUtils.isEmpty(uuid)) {
            AsyncManager.me.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "验证码不能为空"));
            throw new ServiceException("验证码不能为空");
        }
        // 和 CaptchaController 存的时候拼法保持一致
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        String captcha = redisCache.getCacheObject(verifyKey);

        // 不管对错，都进行删除，防止接口刷新
        redisCache.deleteCacheObject(verifyKey);

        // 过期或者不存在，则报错
        if (captcha == null) {
            AsyncManager.me.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "验证码已失效"));
            throw new ServiceException("验证码已失效");
        }

        // 忽略大小写比较
        if (!code.equalsIgnoreCase(captcha)) {
            AsyncManager.me.execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, "验证码错误"));
            throw new ServiceException("验证码错误");
        }

    }
}
