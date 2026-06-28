package com.zhq.taskforge.model.system.controller;

import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.ResultCode;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.model.system.entity.SysDept;
import com.zhq.taskforge.model.system.service.SysDeptService;
import com.zhq.taskforge.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/dept")
@Tag(name = "部门管理")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 查询部门列表(树形结构)
     */
    @GetMapping("/list")
    @Operation(summary = "查询部门列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_LIST + "')")
    public Result<List<SysDept>> list(SysDept sysDept) {
        List<SysDept> sysDepts = sysDeptService.selectDeptList(sysDept);
        return Result.success(sysDepts);
    }

    /**
     * 根据id查询部门详情
     */
    @GetMapping("/{deptId}")
    @Operation(summary = "根据id查询部门详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_QUERY + "')")
    public Result<SysDept> getInfo(@PathVariable Long deptId) {
        SysDept sysDept = sysDeptService.selectDeptById(deptId);
        return Result.success(sysDept);
    }

    /**
     * 添加部门
     */
    @PostMapping()
    @Operation(summary = "添加部门")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_ADD + "')")
    public Result<Void> add(@RequestBody SysDept sysDept) {
        sysDept.setCreateBy(SecurityUtils.getUserName());
        sysDeptService.insertDept(sysDept);
        return Result.success();
    }

    /**
     * 修改部门
     */
    @PutMapping()
    @Operation(summary = "修改部门")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_EDIT + "')")
    public Result<Void> update(@RequestBody SysDept sysDept) {
        sysDept.setUpdateBy(SecurityUtils.getUserName());
        sysDeptService.updateDept(sysDept);
        return Result.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{deptId}")
    @Operation(summary = "删除部门")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_REMOVE + "')")
    public Result<Void> remove(@PathVariable Long deptId) {
        // 1. 判断是否存在子部门
        if (sysDeptService.hasChildByDeptId(deptId)) {
            return Result.error(ResultCode.DEPT_HAS_CHILDREN);
        }
        // 2. 判断部门下是否有用户
        if (sysDeptService.checkDeptExistUser(deptId)) {
            return Result.error(ResultCode.DEPT_HAS_USERS);
        }
        // 3. 执行删除
        sysDeptService.deleteDeptById(deptId);
        return Result.success();
    }
}
