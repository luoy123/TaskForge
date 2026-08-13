package com.zhq.taskforge.web.controller.system;

import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.entity.SysDept;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.service.ISysDeptService;
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
    private ISysDeptService sysDeptService;

    @GetMapping("/list")
    @Operation(summary = "查询部门列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_LIST + "')")
    public R<List<SysDept>> list(SysDept sysDept) {
        List<SysDept> sysDepts = sysDeptService.selectDeptList(sysDept);
        return R.ok(sysDepts);
    }

    @GetMapping("/{deptId}")
    @Operation(summary = "根据id查询部门详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_QUERY + "')")
    public R<SysDept> getInfo(@PathVariable Long deptId) {
        SysDept sysDept = sysDeptService.selectDeptById(deptId);
        return R.ok(sysDept);
    }

    @PostMapping()
    @Operation(summary = "添加部门")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_ADD + "')")
    public R<Void> add(@RequestBody SysDept sysDept) {
        sysDept.setCreateBy(SecurityUtils.getUsername());
        sysDeptService.insertDept(sysDept);
        return R.ok();
    }

    @PutMapping()
    @Operation(summary = "修改部门")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_EDIT + "')")
    public R<Void> update(@RequestBody SysDept sysDept) {
        sysDept.setUpdateBy(SecurityUtils.getUsername());
        sysDeptService.updateDept(sysDept);
        return R.ok();
    }

    @DeleteMapping("/{deptId}")
    @Operation(summary = "删除部门")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DEPT_REMOVE + "')")
    public R<Void> remove(@PathVariable Long deptId) {
        if (sysDeptService.hasChildByDeptId(deptId)) {
            return R.fail("存在下级部门，不允许删除");
        }
        if (sysDeptService.checkDeptExistUser(deptId)) {
            return R.fail("部门下存在用户，不允许删除");
        }
        sysDeptService.deleteDeptById(deptId);
        return R.ok();
    }
}
