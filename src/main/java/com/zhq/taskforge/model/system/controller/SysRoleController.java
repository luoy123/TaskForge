package com.zhq.taskforge.model.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.model.system.entity.SysRole;
import com.zhq.taskforge.model.system.service.SysRoleService;
import com.zhq.taskforge.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@Tag(name = "用户角色模块")
public class SysRoleController {

    @Autowired
    private SysRoleService sysRoleService;

    /**
     * 添加角色
     */
    @PostMapping()
    @Operation(summary = "添加角色")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_ADD + "')")
    public Result<Void> add(@RequestBody SysRole sysRole) {
        sysRole.setCreateBy(SecurityUtils.getUserName());
        sysRoleService.addRole(sysRole);
        return Result.success();
    }

    /**
     * 修改角色
     */
    @PutMapping()
    @Operation(summary = "修改角色")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_EDIT + "')")
    public Result<Void> updateRole(@RequestBody SysRole sysRole) {
        sysRole.setUpdateBy(SecurityUtils.getUserName());
        sysRoleService.updateRole(sysRole);
        return Result.success();
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{roleIds}")
    @Operation(summary = "删除角色")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_REMOVE + "')")
    public Result<Void> remove(@PathVariable List<Long> roleIds) {
        sysRoleService.deleteRole(roleIds);
        return Result.success();
    }

    /**
     * 修改角色状态
     */
    @PutMapping("/changeStatus")
    @Operation(summary = "修改角色状态")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_EDIT + "')")
    public Result<Void> changeStatus(@RequestBody SysRole sysRole) {
        sysRole.setUpdateBy(SecurityUtils.getUserName());
        sysRoleService.changeStatus(sysRole);
        return Result.success();
    }

    /**
     * 查询角色详情
     */
    @GetMapping("/{roleId}")
    @Operation(summary = "查询角色详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_QUERY + "')")
    public Result<SysRole> getInfo(@PathVariable Long roleId) {
        SysRole sysRole = sysRoleService.getDetailsById(roleId);
        return Result.success(sysRole);
    }

    /**
     * 给用户分配角色用的，就是获取角色框的下拉列表
     */
    @GetMapping("/optionselect")
    @Operation(summary = "查看下拉列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_QUERY + "')")
    public Result<List<SysRole>> optionSelect() {
        List<SysRole> sysRoles = sysRoleService.optionSelect();
        return Result.success(sysRoles);
    }

    /**
     * 查询角色列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询角色列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_LIST + "')")
    public Result<Page<SysRole>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysRole role) {
        Page<SysRole> sysRolePage = sysRoleService.list(pageNum, pageSize, role);
        return Result.success(sysRolePage);
    }
}
