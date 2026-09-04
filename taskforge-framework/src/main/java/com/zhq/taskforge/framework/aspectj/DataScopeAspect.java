package com.zhq.taskforge.framework.aspectj;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import com.zhq.taskforge.common.annotation.DataScope;
import com.zhq.taskforge.common.core.domain.entity.SysRole;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.common.datascope.DataScopeContext;
import com.zhq.taskforge.framework.security.context.PermissionContextHolder;

@Aspect
@Component
public class DataScopeAspect {

    /** 全部数据权限 */
    public static final String DATA_SCOPE_ALL = "1";

    /** 自定数据权限 */
    public static final String DATA_SCOPE_CUSTOM = "2";

    /** 部门数据权限 */
    public static final String DATA_SCOPE_DEPT = "3";

    /** 部门及以下数据权限 */
    public static final String DATA_SCOPE_DEPT_AND_CHILD = "4";

    /** 仅本人数据权限 */
    public static final String DATA_SCOPE_SELF = "5";

    @Before("@annotation(dataScope)")
    public void doBefore(JoinPoint joinPoint, DataScope dataScope) {
        DataScopeContext.clear();
        handleDataScope(joinPoint, dataScope);
    }

    @After("@annotation(dataScope)")
    public void doAfter(JoinPoint joinPoint, DataScope dataScope) {
        DataScopeContext.clear();
    }

    public void handleDataScope(JoinPoint joinPoint, DataScope dataScope) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            return;
        }
        SysUser user = loginUser.getUser();

        // 超级管理员跳过
        if (SecurityUtils.isAdmin(user.getUserId())) {
            return;
        }

        // 注解优先；空则用 hasPermi 写入的 Context
        String permission = dataScope.permission();
        if (StringUtils.isEmpty(permission) || "null".equals(permission)) {
            permission = PermissionContextHolder.getContext();
        }
        if ("null".equals(permission)) {
            permission = "";
        }

        dataScopeFilter(user, dataScope.deptAlias(), dataScope.userAlias(), permission);
    }

    public void dataScopeFilter(SysUser user, String deptAlias, String userAlias, String permission) {
        StringBuilder sql = new StringBuilder();
        // 同类型 dataScope 去重（自定义除外，不同 roleId 都要拼）
        List<String> conditions = new ArrayList<>();

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return;
        }

        for (SysRole role : user.getRoles()) {
            String dataScope = role.getDataScope();

            // 非自定义且同类已拼过 → 跳过
            if (!DATA_SCOPE_CUSTOM.equals(dataScope) && conditions.contains(dataScope)) {
                continue;
            }

            // permission 过滤：只让拥有该权限字符的角色参与
            if (StringUtils.isNotEmpty(permission)
                    && StringUtils.isNotEmpty(role.getPermissions())
                    && !StringUtils.containsAny(role.getPermissions(), permission.split(","))) {
                continue;
            }

            if (DATA_SCOPE_ALL.equals(dataScope)) {
                sql = new StringBuilder();
                break;
            } else if (DATA_SCOPE_CUSTOM.equals(dataScope)) {
                sql.append(StringUtils.format(
                        " OR {} IN ( SELECT dept_id FROM sys_role_dept WHERE role_id = {} ) ",
                        col(deptAlias, "dept_id"), role.getRoleId()));
            } else if (DATA_SCOPE_DEPT.equals(dataScope)) {
                sql.append(StringUtils.format(
                        " OR {} = {} ",
                        col(deptAlias, "dept_id"), user.getDeptId()));
            } else if (DATA_SCOPE_DEPT_AND_CHILD.equals(dataScope)) {
                sql.append(StringUtils.format(
                        " OR {} IN ( SELECT dept_id FROM sys_dept WHERE dept_id = {} OR find_in_set( {} , ancestors ) ) ",
                        col(deptAlias, "dept_id"), user.getDeptId(), user.getDeptId()));
            } else if (DATA_SCOPE_SELF.equals(dataScope)) {
                if (StringUtils.isNotEmpty(userAlias)) {
                    sql.append(StringUtils.format(
                            " OR {} = {} ",
                            col(userAlias, "user_id"), user.getUserId()));
                } else if (StringUtils.isNotEmpty(permission) && permission.contains("system:user:")) {
                    // 单表用户列表：别名为空时仍按 user_id 过滤（仅本人）
                    sql.append(StringUtils.format(" OR user_id = {} ", user.getUserId()));
                } else {
                    // 部门等无 user 列的查询：故意查不到
                    sql.append(StringUtils.format(
                            " OR {} = 0 ",
                            col(deptAlias, "dept_id")));
                }
            }

            conditions.add(dataScope);
        }

        if (StringUtils.isNotEmpty(sql.toString())) {
            // substring(4) 去掉开头的 " OR "
            DataScopeContext.set("(" + sql.substring(4) + ")");
        }
    }

    private static String col(String alias, String column) {
        if (StringUtils.isEmpty(alias)) {
            return column;
        }
        return alias + "." + column;
    }
}
