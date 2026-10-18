package com.zhq.taskforge.common.constants;

public class Constants {
    public static final String TOKEN = "token";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String LOGIN_USER_KEY = "login_user_key";
    public static final String LOGOUT = "Logout";
    public static final String LOGIN_SUCCESS = "Success";
    public static final String LOGIN_FAIL = "Error";
    public static final String REGISTER = "Register";
    public static final String HTTP = "http://";
    public static final String HTTPS = "https://";
    public static final String UTF8 = "UTF-8";
    public static final String GBK = "GBK";
    public static final String SUCCESS = "0";
    public static final String FAIL = "1";
    public static final Integer CAPTCHA_EXPIRATION = 2;// 分钟有效期

    /** RMI 远程方法调用 */
    public static final String LOOKUP_RMI = "rmi:";
    /** LDAP 远程方法调用 */
    public static final String LOOKUP_LDAP = "ldap:";
    /** LDAPS 远程方法调用 */
    public static final String LOOKUP_LDAPS = "ldaps:";

    /** 定时任务白名单包名 */
    public static final String[] JOB_WHITELIST_STR = { "com.zhq.taskforge" };

    /** 定时任务违规字符 */
    public static final String[] JOB_ERROR_STR = {
            "java.net.URL", "javax.naming.InitialContext", "org.yaml.snakeyaml",
            "org.springframework", "org.apache", "com.zhq.taskforge.common.utils"
    };
}
