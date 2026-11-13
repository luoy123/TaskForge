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
    /** I4：首页统计 / 进行中 / 下拉（对齐 sys_menu） */
    public static final String PROJECT_STATISTICS = "project:manage:statistics";
    public static final String PROJECT_DOING = "project:manage:doing";
    public static final String PROJECT_SELECT = "project:manage:select";

    /**
     * 任务模块（对齐 sys_menu perms）
     */
    public static final String PROJECT_TASK_ADD = "project:task:add";
    public static final String PROJECT_TASK_DETAIL = "project:task:detail";
    public static final String PROJECT_TASK_LIST = "project:task:list";
    public static final String PROJECT_TASK_EDIT = "project:task:edit";
    public static final String PROJECT_TASK_DELETE = "project:task:delete";
    public static final String PROJECT_TASK_ADD_CHILD = "project:task:addChildTask";
    public static final String PROJECT_TASK_ADD_COMMENT = "project:task:addComment";
    public static final String PROJECT_TASK_LOG_LIST = "project:task:logList";
    public static final String PROJECT_TASK_IMPORT = "project:task:import";
    /** I3：我的任务 / 概况 / 燃尽（对齐 sys_menu；burnDown 菜单可后补） */
    public static final String PROJECT_TASK_MY_LIST = "project:task:queryMyTaskList";
    public static final String PROJECT_TASK_SITUATION = "project:task:situation";
    public static final String PROJECT_TASK_BURNDOWN = "project:task:burnDownChart";

    /**
     * 项目阶段（对齐 sys_menu perms；设为当前阶段复用 PROJECT_EDIT）
     */
    public static final String PROJECT_STAGE_LIST = "project:stage:list";
    public static final String PROJECT_STAGE_ADD = "project:stage:add";
    public static final String PROJECT_STAGE_EDIT = "project:stage:edit";
    public static final String PROJECT_STAGE_DELETE = "project:stage:delete";

    /**
     * 项目文件（对齐 sys_menu perms）
     */
    public static final String PROJECT_FILE_UPLOAD = "project:file:upload";
    public static final String PROJECT_FILE_LIST = "project:file:queryFileList";
    public static final String PROJECT_FILE_RENAME = "project:file:rename";
    public static final String PROJECT_FILE_DELETE = "project:file:delete";

    /**
     * 工作流（Sprint H；菜单可后补，先保证权限字常量存在）
     */
    public static final String WORKFLOW_PROCESS_LIST = "workflow:process:list";
    public static final String WORKFLOW_PROCESS_DEPLOY = "workflow:process:deploy";
    public static final String WORKFLOW_PROCESS_START = "workflow:process:start";
    public static final String WORKFLOW_TASK_COMPLETE = "workflow:task:complete";
    public static final String WORKFLOW_TASK_REJECT = "workflow:task:reject";
    public static final String PROJECT_TASK_APPROVE = "project:task:approve";
    public static final String PROJECT_APPROVE = "project:manage:approve";

    /**
     * 项目成员（对齐 sys_menu / pmhub perms）
     */
    public static final String PROJECT_MEMBER_LIST = "project:member:list";
    public static final String PROJECT_MEMBER_ADD = "project:member:inviteMemberList";
    public static final String PROJECT_MEMBER_REMOVE = "project:member:removeMemberList";

    /**
     * 定时任务（Sprint K）
     */
    public static final String MONITOR_JOB_LIST = "monitor:job:list";
    public static final String MONITOR_JOB_QUERY = "monitor:job:query";
    public static final String MONITOR_JOB_ADD = "monitor:job:add";
    public static final String MONITOR_JOB_EDIT = "monitor:job:edit";
    public static final String MONITOR_JOB_REMOVE = "monitor:job:remove";
    /** 启停 / 立即执行 */
    public static final String MONITOR_JOB_CHANGE_STATUS = "monitor:job:changeStatus";
    /** @deprecated 使用 MONITOR_JOB_CHANGE_STATUS */
    public static final String MONITOR_JOB_RUN = MONITOR_JOB_CHANGE_STATUS;

    /**
     * 在线用户
     */
    public static final String ONLINE_LIST = "monitor:online:list";
    public static final String ONLINE_FORCE_LOGOUT = "monitor:online:forceLogout";

    /**
     * 服务 / 缓存监控（Sprint N）
     */
    public static final String MONITOR_SERVER_LIST = "monitor:server:list";
    public static final String MONITOR_CACHE_LIST = "monitor:cache:list";
    public static final String MONITOR_CACHE_REMOVE = "monitor:cache:remove";

    /**
     * 代码生成（Sprint O；对齐 sys_menu 116 及按钮 1055～1060）
     */
    public static final String GEN_LIST = "tool:gen:list";
    public static final String GEN_QUERY = "tool:gen:query";
    public static final String GEN_EDIT = "tool:gen:edit";
    public static final String GEN_REMOVE = "tool:gen:remove";
    public static final String GEN_IMPORT = "tool:gen:import";
    public static final String GEN_PREVIEW = "tool:gen:preview";
    public static final String GEN_CODE = "tool:gen:code";

}
