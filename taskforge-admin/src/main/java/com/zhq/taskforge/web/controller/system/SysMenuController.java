package com.zhq.taskforge.web.controller.system;

import com.zhq.taskforge.common.core.domain.TreeSelect;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.entity.SysMenu;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.service.ISysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
@Tag(name = "菜单模块")
public class SysMenuController {

    @Autowired
    private ISysMenuService sysMenuService;

    @GetMapping("/treeselect")
    @Operation(summary = "获取菜单下拉树列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_LIST + "')")
    public R<List<TreeSelect>> treeSelect(SysMenu sysMenu) {
        List<SysMenu> sysMenuList = sysMenuService.selectMenuList(sysMenu);
        List<TreeSelect> treeSelectList = sysMenuService.buildMenuTreeSelect(sysMenuList);
        return R.ok(treeSelectList);
    }

    @GetMapping("/roleMenuTreeselect/{roleId}")
    @Operation(summary = "获取角色菜单选择树")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_LIST + "')")
    public R<Map<String, Object>> roleMenuTreeSelect(@PathVariable Long roleId) {
        List<SysMenu> menu = sysMenuService.selectMenuList(new SysMenu());
        List<TreeSelect> treeSelectList = sysMenuService.buildMenuTreeSelect(menu);
        List<Long> checkedKeys = sysMenuService.selectMenuListByRoleId(roleId);
        Map<String, Object> data = new HashMap<>();
        data.put("checkedKeys", checkedKeys);
        data.put("menus", treeSelectList);
        return R.ok(data);
    }

    @GetMapping("/list")
    @Operation(summary = "查询菜单列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_LIST + "')")
    public R<List<SysMenu>> list(SysMenu sysMenu) {
        List<SysMenu> sysMenuList = sysMenuService.selectMenuList(sysMenu);
        return R.ok(sysMenuList);
    }

    @GetMapping("/{menuId}")
    @Operation(summary = "根据菜单id获取菜单详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_QUERY + "')")
    public R<SysMenu> selectMenuById(@PathVariable Long menuId) {
        SysMenu sysMenu = sysMenuService.selectMenuById(menuId);
        return R.ok(sysMenu);
    }

    @PostMapping()
    @Operation(summary = "添加菜单项")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_ADD + "')")
    public R<Void> add(@RequestBody SysMenu sysMenu) {
        sysMenu.setCreateBy(SecurityUtils.getUsername());
        sysMenuService.addMenu(sysMenu);
        return R.ok();
    }

    @PutMapping()
    @Operation(summary = "修改菜单")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_EDIT + "')")
    public R<Void> update(@RequestBody SysMenu sysMenu) {
        sysMenu.setUpdateBy(SecurityUtils.getUsername());
        sysMenuService.updateMenu(sysMenu);
        return R.ok();
    }

    @DeleteMapping("/{menuId}")
    @Operation(summary = "删除菜单")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MENU_REMOVE + "')")
    public R<Void> delete(@PathVariable Long menuId) {
        sysMenuService.deleteMenu(menuId);
        return R.ok();
    }
}
