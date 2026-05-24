package com.zhq.taskforge.model.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.ResultCode;
import com.zhq.taskforge.model.system.entity.SysRole;
import com.zhq.taskforge.model.system.service.SysRoleService;
import com.zhq.taskforge.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@Tag(name = "用户角色模块")
public class SysRoleController {

    @Autowired
    SysRoleService sysRoleService;

    @PostMapping()
    @Operation(summary = "添加角色")
    public Result<Void> add(@RequestBody  SysRole sysRole) {
        sysRole.setCreateBy(SecurityUtils.getUserName());
        sysRoleService.addRole(sysRole);
        return new Result<>(ResultCode.SUCCESS, null);
    }

    @PutMapping()
    @Operation(summary = "修改角色")
    public Result<Void> updateRole(@RequestBody SysRole sysRole) {
        sysRole.setUpdateBy(SecurityUtils.getUserName());
        sysRoleService.updateRole(sysRole);
        return new Result<>(ResultCode.SUCCESS, null);
    }

    @DeleteMapping("/{roleIds}")
    @Operation(summary = "删除角色")
    public Result<Void> remove(@PathVariable  List<Long> roleIds) {
        sysRoleService.deleteRole(roleIds);
        return new Result<>(ResultCode.SUCCESS, null);
    }

    @PutMapping("/changeStatus")
    @Operation(summary = "修改角色状态")
    public Result<Void> changeStatus(@RequestBody SysRole sysRole) {
        sysRole.setUpdateBy(SecurityUtils.getUserName());
        sysRoleService.changeStatus(sysRole);
        return new Result<>(ResultCode.SUCCESS, null);
    }

    @GetMapping("/{roleId}")
    @Operation(summary = "查询角色详情")
    public  Result<SysRole> getInfo(@PathVariable Long roleId){
        SysRole sysRole = sysRoleService.getDetailsById(roleId);
        return new Result<>(ResultCode.SUCCESS, sysRole);
    }


    /**
     * 给用户分配角色用的，就是获取角色框的下拉列表。
     * @param roleId
     * @return
     */
    @GetMapping("/optionselect")
    @Operation(summary = "查看下拉列表")
    public Result<List<SysRole>> optionSelect(){
        List<SysRole> sysRoles = sysRoleService.optionSelect();
        return new Result<>(ResultCode.SUCCESS,sysRoles);
    }

    /**
     * 给后台角色管理用，因为该页面可能存在条件查询，eg:角色名称输入框，状态下拉框
     * 注意：这里的SysRole没有加上@RequestBody,但是spring　mvc会自动的将同名的属性给role的属性。
     * @return
     */
    @GetMapping("/list")
    @Operation(summary = "查询角色列表")
    public Result<Page<SysRole>> list(@RequestParam(defaultValue = "1") Long pageNum, @RequestParam(defaultValue = "10") Long pageSize,  SysRole role){
        Page<SysRole> sysRolePage = sysRoleService.list(pageNum,pageSize,role);
        return new Result<>(ResultCode.SUCCESS,sysRolePage);
    }

}
