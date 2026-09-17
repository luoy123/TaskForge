package com.zhq.taskforge.web.controller.workflow;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.workflow.domain.bo.WfStartProcessBo;
import com.zhq.taskforge.workflow.domain.vo.WfTaskVo;
import com.zhq.taskforge.workflow.service.IWfProcessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 流程部署 / 启动 / 待办列表。
 */
@RestController
@RequestMapping("/workflow/process")
@Tag(name = "流程实例")
public class WfProcessController {

    @Autowired
    private IWfProcessService wfProcessService;

    @PostMapping("/deployBuiltin")
    @Operation(summary = "部署内置任务审批 BPMN")
    @Log(title = "流程部署", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_PROCESS_DEPLOY + "')")
    public R<String> deployBuiltin() {
        return R.ok(wfProcessService.deployBuiltinTaskApprove());
    }

    @GetMapping("/definition/{processKey}")
    @Operation(summary = "查询最新流程定义")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_PROCESS_LIST + "')")
    public R<Map<String, Object>> latestDefinition(@PathVariable String processKey) {
        return R.ok(wfProcessService.latestDefinition(processKey));
    }

    @PostMapping("/start")
    @Operation(summary = "按 key 启动流程（纯 Flowable，不含业务关联）")
    @Log(title = "启动流程", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_PROCESS_START + "')")
    public R<String> start(@RequestBody WfStartProcessBo bo) {
        return R.ok(wfProcessService.startProcess(bo));
    }

    @GetMapping("/todoList")
    @Operation(summary = "我的待办")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_PROCESS_LIST + "')")
    public R<List<WfTaskVo>> todoList() {
        return R.ok(wfProcessService.todoList());
    }

    @GetMapping("/finishedList")
    @Operation(summary = "我的已办")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.WORKFLOW_PROCESS_LIST + "')")
    public R<List<WfTaskVo>> finishedList() {
        return R.ok(wfProcessService.finishedList());
    }
}
