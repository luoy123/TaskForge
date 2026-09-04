package com.zhq.taskforge.common.constants;

/**
 * 权限常量类
 * 统一管理系统权限字符串
 */
public class PermissionConstants {

    /**
     * 用户模块权限
     */
    public static final String USER_LIST = "system:user:list";
    public static final String USER_QUERY = "system:user:query";
    public static final String USER_ADD = "system:user:add";
    public static final String USER_EDIT = "system:user:edit";
    public static final String USER_REMOVE = "system:user:remove";
    public static final String USER_RESET_PWD = "system:user:resetPwd";

    /**
     * 角色模块权限
     */
    public static final String ROLE_LIST = "system:role:list";
    public static final String ROLE_QUERY = "system:role:query";
    public static final String ROLE_ADD = "system:role:add";
    public static final String ROLE_EDIT = "system:role:edit";
    public static final String ROLE_REMOVE = "system:role:remove";

    /**
     * 菜单模块权限
     */
    public static final String MENU_LIST = "system:menu:list";
    public static final String MENU_QUERY = "system:menu:query";
    public static final String MENU_ADD = "system:menu:add";
    public static final String MENU_EDIT = "system:menu:edit";
    public static final String MENU_REMOVE = "system:menu:remove";

    /**
     * 部门模块权限
     */
    public static final String DEPT_LIST = "system:dept:list";
    public static final String DEPT_QUERY = "system:dept:query";
    public static final String DEPT_ADD = "system:dept:add";
    public static final String DEPT_EDIT = "system:dept:edit";
    public static final String DEPT_REMOVE = "system:dept:remove";

    /**
     * 岗位模块权限
     */
    public static final String POST_LIST = "system:post:list";
    public static final String POST_QUERY = "system:post:query";
    public static final String POST_ADD = "system:post:add";
    public static final String POST_EDIT = "system:post:edit";
    public static final String POST_REMOVE = "system:post:remove";

    /**
     * 字典模块权限
     */
    public static final String DICT_LIST = "system:dict:list";
    public static final String DICT_QUERY = "system:dict:query";
    public static final String DICT_ADD = "system:dict:add";
    public static final String DICT_EDIT = "system:dict:edit";
    public static final String DICT_REMOVE = "system:dict:remove";

    /**
     * 参数设置模块权限
     */
    public static final String CONFIG_LIST = "system:config:list";
    public static final String CONFIG_QUERY = "system:config:query";
    public static final String CONFIG_ADD = "system:config:add";
    public static final String CONFIG_EDIT = "system:config:edit";
    public static final String CONFIG_REMOVE = "system:config:remove";

    /**
     * 操作日志权限
     */
    public static final String OPERLOG_LIST = "monitor:operlog:list";
    public static final String OPERLOG_QUERY = "monitor:operlog:query";
    public static final String OPERLOG_REMOVE = "monitor:operlog:remove";

    /**
     * 登录日志权限
     */
    public static final String LOGININFOR_LIST = "monitor:logininfor:list";
    public static final String LOGININFOR_QUERY = "monitor:logininfor:query";
    public static final String LOGININFOR_REMOVE = "monitor:logininfor:remove";

    /**
     * 公共模块
     */
    public static final String NOTICE_LIST = "system:notice:list";
    public static final String NOTICE_QUERY = "system:notice:query";
    public static final String NOTICE_ADD = "system:notice:add";
    public static final String NOTICE_EDIT = "system:notice:edit";
    public static final String NOTICE_REMOVE = "system:notice:remove";
    
    /**
     * 项目模块
     */
    public static final String PROJECT_LIST = "project:manage:list";
    public static final String PROJECT_QUERY = "project:manage:query";
    public static final String PROJECT_DETAIL = "project:manage:detail";
    public static final String PROJECT_ADD = "project:manage:add";
    public static final String PROJECT_EDIT = "project:manage:edit";
    /** 与菜单 perms 对齐：project:manage:delete */
    public static final String PROJECT_REMOVE = "project:manage:delete";
    public static final String PROJECT_ARCHIVE = "project:manage:archive";
    public static final String PROJECT_CANCEL_ARCHIVE = "project:manage:cancelArchive";
    public static final String PROJECT_QUIT = "project:manage:quit";
    public static final String PROJECT_COLLECT = "project:manage:collect";
    public static final String PROJECT_CANCEL_COLLECT = "project:manage:cancelCollect";
}
