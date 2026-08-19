package com.zhq.taskforge.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.entity.SysRole;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.service.ISysRoleService;
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
    private ISysRoleService sysRoleService;

    @PostMapping()
    @Operation(summary = "添加角色")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_ADD + "')")
    public R<Void> add(@RequestBody SysRole sysRole) {
        sysRole.setCreateBy(SecurityUtils.getUsername());
        sysRoleService.addRole(sysRole);
        return R.ok();
    }

    @PutMapping()
    @Operation(summary = "修改角色")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_EDIT + "')")
    public R<Void> updateRole(@RequestBody SysRole sysRole) {
        sysRole.setUpdateBy(SecurityUtils.getUsername());
        sysRoleService.updateRole(sysRole);
        return R.ok();
    }

    @DeleteMapping("/{roleIds}")
    @Operation(summary = "删除角色")
    @Log(title = "角色管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_REMOVE + "')")
    public R<Void> remove(@PathVariable List<Long> roleIds) {
        sysRoleService.deleteRole(roleIds);
        return R.ok();
    }

    @PutMapping("/changeStatus")
    @Operation(summary = "修改角色状态")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_EDIT + "')")
    public R<Void> changeStatus(@RequestBody SysRole sysRole) {
        sysRole.setUpdateBy(SecurityUtils.getUsername());
        sysRoleService.changeStatus(sysRole);
        return R.ok();
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "查询角色详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_QUERY + "')")
    public R<SysRole> getInfo(@PathVariable Long roleId) {
        SysRole sysRole = sysRoleService.getDetailsById(roleId);
        return R.ok(sysRole);
    }

    @GetMapping("/optionselect")
    @Operation(summary = "查看下拉列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_QUERY + "')")
    public R<List<SysRole>> optionSelect() {
        List<SysRole> sysRoles = sysRoleService.optionSelect();
        return R.ok(sysRoles);
    }

    @GetMapping("/list")
    @Operation(summary = "查询角色列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ROLE_LIST + "')")
    public R<Page<SysRole>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysRole role) {
        Page<SysRole> sysRolePage = sysRoleService.list(pageNum, pageSize, role);
        return R.ok(sysRolePage);
    }
}
