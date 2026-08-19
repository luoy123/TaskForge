package com.zhq.taskforge.framework.datascope;

/**
 * 存放本次请求中 DataScopeAspect 拼好的过滤条件。
 * 用 ThreadLocal：每个请求线程各有一份，互不干扰。
 */
public class DataScopeContext {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String sql) {
        CONTEXT.set(sql);
    }

    public static String get() {
        return CONTEXT.get();
    }

    /** 请求结束后必须清掉，否则线程池复用时会串到下一个请求 */
    public static void clear() {
        CONTEXT.remove();
    }
}
