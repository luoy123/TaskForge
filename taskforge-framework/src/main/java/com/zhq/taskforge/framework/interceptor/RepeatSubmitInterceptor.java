package com.zhq.taskforge.framework.interceptor;

import java.lang.reflect.Method;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import com.alibaba.fastjson2.JSON;
import com.zhq.taskforge.common.annotation.RepeatSubmit;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.utils.ServletUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 方法上有 {@link RepeatSubmit} 时交给子类判断是否重复；重复则直接写 R 失败响应。
 */
public abstract class RepeatSubmitInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();
        RepeatSubmit annotation = method.getAnnotation(RepeatSubmit.class);
        if (annotation != null && this.isRepeatSubmit(request, annotation)) {
            // 拦截器阶段进不了 @RestControllerAdvice，直接写出 R
            ServletUtils.renderString(response, JSON.toJSONString(R.fail(annotation.message())));
            return false;
        }
        return true;
    }

    /** true = 判定为重复提交，应拦截 */
    public abstract boolean isRepeatSubmit(HttpServletRequest request, RepeatSubmit annotation);
}
