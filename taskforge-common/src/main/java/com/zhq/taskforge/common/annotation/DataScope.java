package com.zhq.taskforge.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataScope {

    /**
     * 部门表别名
     * 
     * @return
     */
    public String deptAlias() default "";

    /**
     * 用户表别名
     * 
     * @return
     */
    public String userAlias() default "";

    /**
     * 权限字符，多个权限，用逗号分隔
     */
    public String permission() default "";

}
