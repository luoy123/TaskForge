package com.zhq.taskforge.framework.aspectj;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import com.zhq.taskforge.common.annotation.RateLimter;
import com.zhq.taskforge.common.enums.LimitType;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.common.utils.ServletUtils;
import com.zhq.taskforge.common.utils.ip.IpUtils;

@Aspect
@Component
public class RateLimiterAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private RedisScript<Long> limitScript;

    @Before("@annotation(rateLimter)")
    public void doBefore(JoinPoint joinPoint, RateLimter rateLimter) {
        int time = rateLimter.time();
        int count = rateLimter.count();

        String combineKey = getCombineKey(rateLimter, joinPoint);
        List<String> keys = Collections.singletonList(combineKey);
        try {
            Long number = redisTemplate.execute(limitScript, keys, count, time);
            // number 是当前窗口内第几次；大于 count 说明超限
            if (number == null || number.intValue() > count) {
                throw new ServiceException("访问过于频繁，请稍候再试");
            }
            log.info("限流 '{}' 次/{}秒, 当前第 {} 次, key={}", count, time, number, combineKey);
        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new ServiceException("服务器限流异常，请稍候再试");
        }
    }

    /**
     * 拼接redis key
     */
    private String getCombineKey(RateLimter rateLimter, JoinPoint joinPoint) {
        StringBuilder sb = new StringBuilder(rateLimter.key());
        if (rateLimter.limitType() == LimitType.IP) {
            sb.append(IpUtils.getIpAddr(ServletUtils.getRequest())).append("-");
        } else if (rateLimter.limitType() == LimitType.USER) {
            try {
                sb.append(SecurityUtils.getUserId()).append("-");
            } catch (Exception e) {
                // 未登录接口（如 login）回退 IP
                sb.append(IpUtils.getIpAddr(ServletUtils.getRequest())).append("-");
            }
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        sb.append(method.getDeclaringClass().getName()).append("-").append(method.getName());
        return sb.toString();
    }

}
