package com.zhq.taskforge.framework.web.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.framework.security.context.PermissionContextHolder;
import com.zhq.taskforge.system.service.ISysMenuService;

@Component("ss")
public class PermissionService {
    @Autowired
    ISysMenuService sysMenuService;

    public boolean hasPermi(String permi) {

        //防止没有显视写@DataScope注解的值，导致数据权限不生效,此时从PermissionContextHolder中获取当前的权限
        PermissionContextHolder.setContext(permi);
        // 当前permi为null 或者 "" ,直接返回false
        if (!StringUtils.hasText(permi)) {
            return false;
        }
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            return false;
        }
        if (Long.valueOf(1).equals(userId)) {
            return true;
        }
        // //方法1：丛数据库中查询数据，
        // List<SysMenu> sysMenus = sysMenuService.selectMenuTreeByUserId(userId);
        // Set<String> collect = sysMenus.stream().map(SysMenu::getPerms)
        // .filter(p -> !StringUtils.isEmpty(p))
        // .collect(Collectors.toSet());
        // return collect.contains(permi);
        // 2.方法2：直接从SecurityUtils中获取当前的loginUser
        Set<String> permissios = SecurityUtils.getLoginUser().getPermissions();
        // 当permissionswe为null时，null.contains(permi)会报空指针异常，所以需要先判断permissions是否为null
        if (permissios == null || permissios.isEmpty()) {
            return false;
        }
        // 如果permissions不为null，则判断permissions是否包含permi
        return permissios.contains(permi);
    }

    public boolean hasPermiOr(String... permi) {
        for (String perm : permi) {
            if (hasPermi(perm)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPermiAnd(String... permi) {
        for (String perm : permi) {
            if (!hasPermi(perm)) {
                return false;
            }
        }
        return true;
    }

}
