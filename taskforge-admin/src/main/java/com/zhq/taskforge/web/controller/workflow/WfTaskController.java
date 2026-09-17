package com.zhq.taskforge.web.controller.workflow;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.workflow.domain.bo.WfTaskBo;
import com.zhq.taskforge.workflow.service.IWfProcessService;
import com.zhq.taskforge.workflow.service.IWfTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 待办办理 + 业务侧启动审批入口。
 */
@RestController
@RequestMapping("/workflow/task")
@Tag(name = "流程任务")
public class WfTaskController {

    @Autowired
    private IWfTaskService wfTaskService;

    @Autowired
    private IWfProcessService wfProcessService;

    @PostMapping("/complete")
    @Operation(summary = "同意办理")
    @Log(title = "流程办理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_TASK_COMPLETE + "')")
    public R<Void> complete(@RequestBody WfTaskBo taskBo) {
        wfTaskService.complete(taskBo);
        return R.ok();
    }

    @PostMapping("/reject")
    @Operation(summary = "驳回")
    @Log(title = "流程驳回", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_TASK_REJECT + "')")
    public R<Void> reject(@RequestBody WfTaskBo taskBo) {
        wfTaskService.reject(taskBo);
        return R.ok();
    }

    /**
     * 启动任务审批。body: { "taskId": "...", "approver": "1" }
     * 校验任务是否存在建议你在实现里补，或拆到 ProjectTaskController 编排。
     */
    @PostMapping("/startTaskApprove")
    @Operation(summary = "启动任务审批")
    @Log(title = "任务审批", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_APPROVE + "')")
    public R<String> startTaskApprove(@RequestBody Map<String, String> body) {
        String taskId = body.get("taskId");
        String approver = body.get("approver");
        return R.ok(wfProcessService.startTaskApprove(taskId, approver));
    }

    @PostMapping("/startProjectApprove")
    @Operation(summary = "启动项目审批")
    @Log(title = "项目审批", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_APPROVE + "')")
    public R<String> startProjectApprove(@RequestBody Map<String, String> body) {
        String projectId = body.get("projectId");
        String approver = body.get("approver");
        return R.ok(wfProcessService.startProjectApprove(projectId, approver));
    }
}
