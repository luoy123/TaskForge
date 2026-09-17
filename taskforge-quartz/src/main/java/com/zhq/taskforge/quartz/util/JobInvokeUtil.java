package com.zhq.taskforge.quartz.util;

import java.lang.reflect.Method;

import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.common.utils.spring.SpringUtils;
import com.zhq.taskforge.quartz.domain.SysJob;

/**
 * 将sys_job.invoke_target 解析成取Bean，调用方法
 * JobInvokeUtil
 */
public class JobInvokeUtil {

    public static void invokeMethod(SysJob sysJob) throws Exception {
        String invokeTarget = sysJob.getInvokeTarget();
        String beanName = getBeanName(invokeTarget);
        String methodName = getMethodName(invokeTarget);

        Object bean = SpringUtils.getBean(beanName);
        Method method = bean.getClass().getMethod(methodName);
        method.invoke(bean);
    }

    public static String getBeanName(String invokeTarget) {
        String beforeBean = StringUtils.substringBefore(invokeTarget, "(");
        String beanName = StringUtils.substringBeforeLast(beforeBean, ".");
        return beanName;
    }

    public static String getMethodName(String invokeTarget) {
        String beforeBean = StringUtils.substringBefore(invokeTarget, "(");
        String methodName = StringUtils.substringAfterLast(beforeBean, ".");
        return methodName;
    }

}
