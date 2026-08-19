package com.zhq.taskforge.web.controller.system;

import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.entity.SysMenu;
import com.zhq.taskforge.common.core.domain.entity.SysRole;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.domain.vo.RouterVo;
import com.zhq.taskforge.system.service.ISysMenuService;
import com.zhq.taskforge.system.service.ISysRoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Tag(name = "用户登录模块")
@RequestMapping
public class SysLoginController {

    @Autowired
    private ISysMenuService sysMenuService;

    @Autowired
    private ISysRoleService iSysRoleService;

    @GetMapping("/getRouters")
    @Operation(summary = "获取返回给前端的路由数据")
    @PreAuthorize("isAuthenticated()")
    public R<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new ServiceException("用户未登录");
        }
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(userId);
        List<RouterVo> routerVoList = sysMenuService.buildMenus(menus);
        return R.ok(routerVoList);
    }

    @GetMapping("/getInfo")
    @Operation(summary = "获取用户信息")
    @PreAuthorize("isAuthenticated()")
    public R<Map<String, Object>> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        if (loginUser == null || loginUser.getUser() == null) {
            throw new ServiceException("用户未登录");
        }

        SysUser user = loginUser.getUser();
        user.setPassword(null);

        Set<String> permissions = loginUser.getPermissions();
        if (permissions == null) {
            permissions = new HashSet<>();
        }

        Long userId = user.getUserId() != null ? user.getUserId() : loginUser.getUserId();
        Set<String> roles = new HashSet<>();
        // 超级管理员通常不配 sys_user_role，与权限逻辑一致，直接返回 admin
        if (Long.valueOf(1L).equals(userId)) {
            roles.add("admin");
        } else {
            List<SysRole> roleList = iSysRoleService.selectRolesByUserId(userId);
            roles = roleList.stream()
                    .map(SysRole::getRoleKey)
                    .filter(key -> key != null && !key.isBlank())
                    .collect(Collectors.toSet());
        }

        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("roles", roles);
        data.put("permissions", permissions);
        return R.ok(data);
    }
}
