package com.zhq.taskforge.framework.interceptor.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.zhq.taskforge.common.annotation.RepeatSubmit;
import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.core.redis.RedisCache;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.framework.interceptor.RepeatSubmitInterceptor;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 规则：同一「Authorization（或空）+ URL」在 interval 毫秒内只能成功进入一次。
 * 不同登录用户互不影响；未登录接口则按 URL 维度短窗口防连点。
 */
@Component
public class SameUrlDataInterceptor extends RepeatSubmitInterceptor {

    @Value("${token.header:Authorization}")
    private String header;

    @Autowired
    private RedisCache redisCache;

    @Override
    public boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation) {
        String url = request.getRequestURI();
        String submitKey = StringUtils.trimToEmpty(request.getHeader(header));
        String cacheKey = CacheConstants.REPEAT_SUBMIT_KEY + url + ":" + submitKey;

        Object exists = redisCache.getCacheObject(cacheKey);
        if (exists != null) {
            return true;
        }

        // RedisCache 的 timeout 是 Integer；interval 默认 5000ms，足够
        redisCache.setCacheObject(cacheKey, "1", annotation.interval(), TimeUnit.MILLISECONDS);
        return false;
    }
}
