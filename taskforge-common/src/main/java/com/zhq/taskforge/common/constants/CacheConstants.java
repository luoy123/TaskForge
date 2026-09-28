package com.zhq.taskforge.common.constants;

/**
 * 缓存变量
 */
public class CacheConstants {
    public static final String SYS_CONFIG_KEY = "sys_config";
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    public static final String CAPTCHA_CODE_KEY = "captcha_codes:";

    /** 限流 redis key 前缀 */
    public static final String RATE_LIMIT_KEY = "rate_limit:";

    /** 防重提交 redis key 前缀（J4 也用） */
    public static final String REPEAT_SUBMIT_KEY = "repeat_submit:";

}
