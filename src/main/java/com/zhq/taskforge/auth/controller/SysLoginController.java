package com.zhq.taskforge.auth.controller;

import com.zhq.taskforge.auth.vo.RouterVo;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysMenu;
import com.zhq.taskforge.model.system.service.SysMenuService;
import com.zhq.taskforge.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "用户登录模块")
@RequestMapping
public class SysLoginController {

    @Autowired
    private SysMenuService sysMenuService;

    /**
     * 获取返回给前端的路由数据
     */
    @GetMapping("/getRouters")
    @Operation(summary = "获取返回给前端的路由数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_LIST + "')")
    public Result<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        List<SysMenu> menus = sysMenuService.selectMenuTreeByUserId(userId);
        List<RouterVo> routerVoList = sysMenuService.buildMenus(menus);
        return Result.success(routerVoList);
    }
}
