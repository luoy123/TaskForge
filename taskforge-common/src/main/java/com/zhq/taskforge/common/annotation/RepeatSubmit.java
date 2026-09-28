package com.zhq.taskforge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 防重复提交：同一用户标识 + URL 在 interval 毫秒内只能进入一次。
 */
@Inherited
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RepeatSubmit {

    /** 间隔时间（毫秒），小于此间隔视为重复提交 */
    int interval() default 5000;

    String message() default "不允许重复提交，请稍候再试";
}
