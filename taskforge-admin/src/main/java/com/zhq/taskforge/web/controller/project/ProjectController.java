package com.zhq.taskforge.web.controller.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;
import com.zhq.taskforge.project.domain.vo.ProjectVO;
import com.zhq.taskforge.project.service.IProjectService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/project")
@Tag(name = "项目管理")
public class ProjectController {

    @Autowired
    private IProjectService projectService;

    @PostMapping("/add")
    @Operation(summary = "新增项目")
    @Log(title = "项目管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_ADD + "')")
    public R<Void> add(@RequestBody Project project) {
        projectService.saveProject(project);
        return R.ok();
    }

    @PostMapping("/detail")
    @Operation(summary = "项目详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_DETAIL + "')")
    public R<ProjectResVO> detail(@RequestBody ProjectVO projectVO) {
        return R.ok(projectService.detail(projectVO.getProjectId()));
    }

    @PostMapping("/list")
    @Operation(summary = "我的项目列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_LIST + "')")
    public R<IPage<ProjectResVO>> list(@RequestBody ProjectReqVO req) {
        return R.ok(projectService.list(req));
    }

    @PostMapping("/edit")
    @Operation(summary = "修改项目")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_EDIT + "')")
    public R<Void> edit(@RequestBody Project project) {
        projectService.edit(project);
        return R.ok();
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除项目")
    @Log(title = "项目管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_REMOVE + "')")
    public R<Void> delete(@RequestBody ProjectVO projectVO) {
        projectService.delete(projectVO.getProjectId());
        return R.ok();
    }

    @PostMapping("/archive")
    @Operation(summary = "项目归档")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_ARCHIVE + "')")
    public R<Void> archive(@RequestBody ProjectVO projectVO) {
        projectService.archived(projectVO.getProjectId());
        return R.ok();
    }

    @PostMapping("/cancelArchive")
    @Operation(summary = "取消项目归档")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_CANCEL_ARCHIVE + "')")
    public R<Void> cancelArchive(@RequestBody ProjectVO projectVO) {
        projectService.unarchived(projectVO.getProjectId());
        return R.ok();
    }

    @PostMapping("/quit")
    @Operation(summary = "退出项目")
    @Log(title = "项目管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_QUIT + "')")
    public R<Void> quit(@RequestBody ProjectVO projectVO) {
        projectService.quit(projectVO.getProjectId());
        return R.ok();
    }

    @PostMapping("/collect")
    @Operation(summary = "收藏项目")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_COLLECT + "')")
    public R<Void> collect(@RequestBody ProjectVO projectVO) {
        projectService.collect(projectVO.getProjectId());
        return R.ok();
    }

    @PostMapping("/cancelCollect")
    @Operation(summary = "取消收藏项目")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_CANCEL_COLLECT + "')")
    public R<Void> cancelCollect(@RequestBody ProjectVO projectVO) {
        projectService.uncollect(projectVO.getProjectId());
        return R.ok();
    }
}
