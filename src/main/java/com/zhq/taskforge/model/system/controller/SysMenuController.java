package com.zhq.taskforge.model.system.controller;

import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.ResultCode;
import com.zhq.taskforge.common.TreeSelect;
import com.zhq.taskforge.model.system.entity.SysMenu;
import com.zhq.taskforge.model.system.service.SysMenuService;
import com.zhq.taskforge.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/system/menu")
@Tag(name="菜单模块")
public class SysMenuController {

    @Autowired
    SysMenuService sysMenuService;

    @GetMapping("/treeselect")
    @Operation(summary = "获取菜单下拉树列表")
    public Result<List<TreeSelect>> treeSelect(SysMenu sysMenu){
        //1.先查出所有的菜单项
        List<SysMenu> sysMenuList =  sysMenuService.selectMenuList(sysMenu);
        //2.渲染成树形结构
        List<TreeSelect> treeSelectList = sysMenuService.buildMenuTreeSelect(sysMenuList);

        return new Result<>(ResultCode.SUCCESS,treeSelectList);
    }

    /**
     * 修改角色时数据回显使用的。
     */
    @GetMapping("/roleMenuTreeselect/{roleId}")
    @Operation(summary = "获取角色菜单选择树")
    public Result<Map<String,Object>> roleMenuTreeSelect(@PathVariable Long roleId){
        //1.获取所有菜单列表
        List<SysMenu> menu = sysMenuService.selectMenuList(new SysMenu());
        //2.转变成TreeSelect类型父子结构的菜单树。
        List<TreeSelect> treeSelectList = sysMenuService.buildMenuTreeSelect(menu);
        //3.返回选择的menuid
        List<Long> checkedKeys = sysMenuService.selectMenuListByRoleId(roleId);
        Map<String,Object> data = new HashMap<>();
        data.put("checkedKeys",checkedKeys);
        data.put("menus",treeSelectList);
        return new Result<>(ResultCode.SUCCESS,data);
    }

    @GetMapping("/list")
    @Operation(summary = "查询菜单列表")
    public Result<List<SysMenu>> list(SysMenu sysMenu){
        List<SysMenu> sysMenuList = sysMenuService.selectMenuList(sysMenu);
        return new Result<>(ResultCode.SUCCESS,sysMenuList);
    }

    /**
     * 修改某一个菜单项，回显使用
     * @param menuId
     * @return
     */
    @GetMapping("/{menuId}")
    @Operation(summary = "根据菜单id获取菜单详情")
    public Result<SysMenu> selectMenuById(@PathVariable  Long menuId){
        SysMenu sysMenu = sysMenuService.selectMenuById(menuId);
        return new Result<>(ResultCode.SUCCESS,sysMenu);
    }

    @PostMapping()
    @Operation(summary = "添加菜单项")
    public Result<Void> add(@RequestBody SysMenu sysMenu){
        sysMenu.setCreateBy(SecurityUtils.getUserName());
        sysMenuService.addMenu(sysMenu);
        return new Result<>(ResultCode.SUCCESS,null);
    }

    @PutMapping()
    @Operation(summary = "修改菜单")
    public Result<Void> update(@RequestBody SysMenu sysMenu){
        sysMenu.setUpdateBy(SecurityUtils.getUserName());
        sysMenuService.updateMenu(sysMenu);
        return new Result<>(ResultCode.SUCCESS,null);
    }

    @DeleteMapping("/{menuId}")
    @Operation(summary = "删除菜单")
    public Result<Void> delete(@PathVariable Long menuId){
        sysMenuService.deleteMenu(menuId);
        return new Result<>(ResultCode.SUCCESS,null);
    }
}

