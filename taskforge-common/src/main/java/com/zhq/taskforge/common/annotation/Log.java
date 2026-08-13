package com.zhq.taskforge.common.annotation;

import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.enums.OperatorType;

import java.lang.annotation.*;

/**
 * 自定义Log注解，能够在方法以及参数上生效
 */
@Target({ElementType.PARAMETER,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    //模块
    public String title() default "";

    //操作类型
    public BusinessType businessType() default BusinessType.OTHER;

    //操作人类型
    public OperatorType operatorType() default OperatorType.MANAGE;

    //是否保留请求参数
    public boolean isSaveRequestData() default true;

    //是否保留响应的参数
    public boolean isSaveResponseData() default true;

}
