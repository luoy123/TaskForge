package com.zhq.taskforge.framework.web.service;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;
import cn.hutool.jwt.signers.JWTSigner;
import cn.hutool.jwt.signers.JWTSignerUtil;
import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.core.redis.RedisCache;
import com.zhq.taskforge.common.utils.ServletUtils;
import com.zhq.taskforge.common.utils.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TokenService {
    protected static final long MILLIS_SECOND = 1000;
    protected static final long MILLIS_MINUTE = 60 * MILLIS_SECOND;
    private static final Long MILLIS_MINUTE_TEN = 20 * 60 * 1000L;
    private static final Long MILLIS_DAY = 24 * 60 * MILLIS_MINUTE;

    @Value("${token.header:Authorization}")
    private String header;

    @Value("${token.secret:my-very-long-and-random-secret-key-2026}")
    private String secret;

    @Value("${token.expireTime:30}")
    private int expireTime;

    @Autowired
    private RedisCache redisCache;

    private static JWTSigner signer;

    private JWTSigner getSigner() {
        if (signer == null) {
            signer = JWTSignerUtil.hs256(secret.getBytes());
        }
        return signer;
    }

    public LoginUser getLoginUser(HttpServletRequest request) {
        String token = getToken(request);
        if (StringUtils.isNotEmpty(token)) {
            try {
                JWT jwt = JWTUtil.parseToken(token).setSigner(getSigner());
                if (!jwt.verify()) return null;
                String uuid = (String) jwt.getPayload("uuid");
                String userKey = getTokenKey(uuid);
                return redisCache.getCacheObject(userKey);
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void setLoginUser(LoginUser loginUser) {
        if (StringUtils.isNotNull(loginUser) && StringUtils.isNotEmpty(loginUser.getToken())) {
            refreshToken(loginUser);
        }
    }

    public void delLoginUser(String token) {
        if (StringUtils.isNotEmpty(token)) {
            String userKey = getTokenKey(token);
            redisCache.deleteCacheObject(userKey);
        }
    }

    public String createToken(LoginUser loginUser) {
        String token = IdUtil.fastUUID();
        loginUser.setToken(token);
        refreshToken(loginUser);
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", token);
        return JWTUtil.createToken(claims, getSigner());
    }

    public String createLongTimeToken(LoginUser loginUser) {
        String token = IdUtil.fastUUID();
        loginUser.setToken(token);
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + 7 * MILLIS_DAY);
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, 7 * 24 * 60, TimeUnit.MINUTES);
        Map<String, Object> claims = new HashMap<>();
        claims.put("uuid", token);
        return JWTUtil.createToken(claims, getSigner());
    }

    public void verifyToken(LoginUser loginUser) {
        long expireTime = loginUser.getExpireTime();
        long currentTime = System.currentTimeMillis();
        if (expireTime - currentTime <= MILLIS_MINUTE_TEN) {
            refreshToken(loginUser);
        }
    }

    public void refreshToken(LoginUser loginUser) {
        loginUser.setLoginTime(System.currentTimeMillis());
        loginUser.setExpireTime(loginUser.getLoginTime() + expireTime * MILLIS_MINUTE);
        String userKey = getTokenKey(loginUser.getToken());
        redisCache.setCacheObject(userKey, loginUser, expireTime, TimeUnit.MINUTES);
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(header);
        if (StringUtils.isNotEmpty(token) && token.startsWith(Constants.TOKEN_PREFIX)) {
            token = token.replace(Constants.TOKEN_PREFIX, "");
        }
        return token;
    }

    private String getTokenKey(String uuid) {
        return CacheConstants.LOGIN_TOKEN_KEY + uuid;
    }
}
