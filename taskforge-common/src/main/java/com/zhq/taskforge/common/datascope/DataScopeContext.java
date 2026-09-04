package com.zhq.taskforge.common.datascope;

/**
 * 存放本次请求中 DataScopeAspect 拼好的过滤条件。
 * 用 ThreadLocal：每个请求线程各有一份，互不干扰。
 * 放在 common，供 system（Service）和 framework（Aspect）共同使用。
 */
public final class DataScopeContext {

    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    private DataScopeContext() {
    }

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
