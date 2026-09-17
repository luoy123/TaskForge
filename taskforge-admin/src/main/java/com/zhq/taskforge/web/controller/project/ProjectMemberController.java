package com.zhq.taskforge.web.controller.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.project.domain.vo.ProjectVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberReqVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberResVO;
import com.zhq.taskforge.project.service.IProjectMemberService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/project/member")
@Tag(name = "项目成员")
public class ProjectMemberController {

    @Autowired
    private IProjectMemberService projectMemberService;

    @PostMapping("/list")
    @Operation(summary = "项目成员列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_MEMBER_LIST + "')")
    public R<IPage<ProjectMemberResVO>> list(@RequestBody ProjectMemberReqVO req) {
        return R.ok(projectMemberService.list(req));
    }

    @PostMapping("/add")
    @Operation(summary = "邀请成员")
    @Log(title = "项目成员", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_MEMBER_ADD + "')")
    public R<Void> add(@RequestBody ProjectVO vo) {
        projectMemberService.add(vo);
        return R.ok();
    }

    @PostMapping("/remove")
    @Operation(summary = "移除成员")
    @Log(title = "项目成员", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_MEMBER_REMOVE + "')")
    public R<Void> remove(@RequestBody ProjectVO vo) {
        projectMemberService.remove(vo);
        return R.ok();
    }
}
