package com.zhq.taskforge.web.controller.project;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.annotation.RepeatSubmit;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.project.domain.vo.ProjectStageVO;
import com.zhq.taskforge.project.domain.vo.ProjectVO;
import com.zhq.taskforge.project.service.IProjectStageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/project/stage")
@Tag(name = "项目阶段")
public class ProjectStageController {

    @Autowired
    private IProjectStageService projectStageService;

    @PostMapping("/list")
    @Operation(summary = "项目阶段列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_STAGE_LIST + "')")
    public R<List<ProjectStageVO>> list(@RequestBody ProjectVO projectVO) {
        return R.ok(projectStageService.list(projectVO.getProjectId()));
    }

    @RepeatSubmit(interval = 3000, message = "请勿重复提交阶段")
    @PostMapping("/add")
    @Operation(summary = "新增项目阶段")
    @Log(title = "项目阶段", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_STAGE_ADD + "')")
    public R<Void> add(@RequestBody ProjectStageVO projectStageVO) {
        projectStageService.add(projectStageVO);
        return R.ok();
    }

    @PostMapping("/edit")
    @Operation(summary = "修改项目阶段名称")
    @Log(title = "项目阶段", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_STAGE_EDIT + "')")
    public R<Void> edit(@RequestBody ProjectStageVO projectStageVO) {
        projectStageService.edit(projectStageVO);
        return R.ok();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目阶段")
    @Log(title = "项目阶段", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_STAGE_DELETE + "')")
    public R<Void> delete(@RequestBody ProjectStageVO projectStageVO) {
        projectStageService.delete(projectStageVO.getStageId());
        return R.ok();
    }

    @PostMapping("/setCurrent")
    @Operation(summary = "设为项目当前阶段")
    @Log(title = "项目阶段", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_EDIT + "')")
    public R<Void> setCurrent(@RequestBody ProjectStageVO projectStageVO) {
        projectStageService.setCurrent(projectStageVO.getStageId(), projectStageVO.getProjectId());
        return R.ok();
    }
}
