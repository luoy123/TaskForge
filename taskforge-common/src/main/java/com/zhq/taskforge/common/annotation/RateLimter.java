package com.zhq.taskforge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.enums.LimitType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimter {

    String key() default CacheConstants.RATE_LIMIT_KEY;

    /** 时间窗口， 秒 */
    int time() default 60;

    /** 最大请求次数 */
    int count() default 100;

    LimitType limitType() default LimitType.DEFAULT;
}
