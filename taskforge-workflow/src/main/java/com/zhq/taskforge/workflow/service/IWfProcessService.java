package com.zhq.taskforge.workflow.service;

import java.util.List;
import java.util.Map;

import com.zhq.taskforge.workflow.domain.bo.WfStartProcessBo;
import com.zhq.taskforge.workflow.domain.vo.WfTaskVo;

/**
 * 流程定义部署 / 启动实例。
 */
public interface IWfProcessService {

    /**
     * 部署 classpath 内置 BPMN（task-approve.bpmn20.xml）。
     *
     * @return deploymentId
     */
    String deployBuiltinTaskApprove();

    /**
     * 按 processKey 启动流程实例（纯 Flowable，不写业务关联表）。
     *
     * @return processInstanceId
     */
    String startProcess(WfStartProcessBo bo);

    /**
     * 启动「任务审批」并写 pmhub_project_task_process（H4 核心，留给你实现）。
     *
     * @param taskId   项目任务 id
     * @param approver 审批人用户 id 字符串
     * @return processInstanceId
     */
    String startTaskApprove(String taskId, String approver);

    /**
     * 启动「项目审批」（可与任务同构，后置）。
     */
    String startProjectApprove(String projectId, String approver);

    /**
     * 当前用户待办。
     */
    List<WfTaskVo> todoList();

    /**
     * 当前用户已办（可简化）。
     */
    List<WfTaskVo> finishedList();

    /**
     * 按 key 查最新流程定义是否存在（调试用）。
     */
    Map<String, Object> latestDefinition(String processKey);
}
