package com.zhq.taskforge.security;


import com.zhq.taskforge.model.system.entity.SysMenu;
import com.zhq.taskforge.model.system.service.SysMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component("ss")
public class PermissionService {
    @Autowired
    SysMenuService sysMenuService;

    /**
     * 判断当亲用户是否拥有某个权限
     * @param permi
     * @return
     */
    public boolean hasPermi(String permi){
        //1.获取当前登录用户
        Long userId = SecurityUtils.getUserId();
        if(userId == null){
            return false;
        }
        //2.管理员获取所有权限
        if(Long.valueOf(1).equals(userId)){
            return true;
        }
        //3.获取当前用户的权限列表
        List<SysMenu> sysMenus = sysMenuService.selectMenuTreeByUserId(userId);
        Set<String> collect = sysMenus.stream().map(SysMenu::getPerms)
                .filter(p -> !StringUtils.isEmpty(p))
                .collect(Collectors.toSet());
        //4.判断permi是否在权限列表中!
        return collect.contains(permi);
    }

    /**
     * 是否拥有任意一个权限
     * @param permi
     * @return
     */
    public boolean hasPermiOr(String... permi){
        for(String perm : permi){
            if(hasPermi(perm)){
                return true;
            }
        }
        return false;
    }

    /**
     * 是否拥有所有权限
     * @param permi
     * @return
     */
    public boolean hasPermiAnd(String... permi){
        for(String perm : permi){
            if(!hasPermi(perm)){
                return false;
            }
        }
        return true;
    }

}
