package com.zhq.taskforge.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.workflow.domain.WfTaskProcess;
import com.zhq.taskforge.workflow.domain.bo.WfStartProcessBo;
import com.zhq.taskforge.workflow.domain.vo.WfTaskVo;
import com.zhq.taskforge.workflow.factory.FlowServiceFactory;
import com.zhq.taskforge.workflow.mapper.WfTaskProcessMapper;
import com.zhq.taskforge.workflow.service.IWfProcessService;
import com.zhq.taskforge.workflow.service.IWfTaskService;

/**
 * 流程部署 / 启动。
 * <p>
 * 已实现：内置 BPMN 部署、按 key 查定义（H1/H2 基础）。
 * 留给你：startProcess / todoList / finishedList / startTaskApprove /
 * startProjectApprove。
 * 对照 pmhub：{@code WfProcessServiceImpl#startProcess}、{@code #startTaskProcess}。
 */
@Service
public class WfProcessServiceImpl extends FlowServiceFactory implements IWfProcessService {

    @Lazy
    @Autowired
    private IWfTaskService wfTaskService;
    @Autowired
    private WfTaskProcessMapper wfTaskProcessMapper;

    /** 与 resources/bpmn/task-approve.bpmn20.xml 中 process id 一致 */
    public static final String TASK_APPROVE_KEY = "task_approve";

    public static final String TYPE_TASK = "task";
    public static final String TYPE_PROJECT = "project";
    /** 审批中 */
    public static final String APPROVED_IN_PROGRESS = "0";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String deployBuiltinTaskApprove() {
        Deployment deployment = repositoryService.createDeployment()
                .name("builtin-task-approve")
                .addClasspathResource("bpmn/task-approve.bpmn20.xml")
                .deploy();
        return deployment.getId();
    }

    @Override
    public Map<String, Object> latestDefinition(String processKey) {
        String key = (processKey == null || processKey.isBlank()) ? TASK_APPROVE_KEY : processKey;
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
        Map<String, Object> map = new HashMap<>();
        if (def == null) {
            map.put("exists", false);
            return map;
        }
        map.put("exists", true);
        map.put("id", def.getId());
        map.put("key", def.getKey());
        map.put("name", def.getName());
        map.put("version", def.getVersion());
        map.put("deploymentId", def.getDeploymentId());
        map.put("suspended", def.isSuspended());
        return map;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProcess(WfStartProcessBo bo) {
        // TODO(H3): 对照 pmhub WfProcessServiceImpl#startProcess
        // 1. 校验 processKey / 流程定义存在且未挂起
        // 2. identityService.setAuthenticatedUserId(当前用户 id 字符串)
        // 3. variables 放入 initiator
        // 4. runtimeService.startProcessInstanceByKey(key, businessKey, variables)
        // 5. 可选：调用 IWfTaskService.startFirstTask 自动完成发起人节点
        // 6. return processInstance.getId()
        String key = bo.getProcessKey();
        if (key == null) {
            key = TASK_APPROVE_KEY;
        }
        ProcessDefinition def = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(key)
                .latestVersion()
                .singleResult();
        if (def == null) {
            throw new ServiceException("流程未部署，请先调用deployBuiltin()方法");
        }
        if (def.isSuspended()) {
            throw new ServiceException("流程已经挂起");
        }
        String userId = String.valueOf(SecurityUtils.getUserId());
        identityService.setAuthenticatedUserId(userId);
        Map vars = bo.getVariables() == null ? new HashMap<>()
                : new HashMap<>(bo.getVariables());
        vars.put("initiator", userId);
        ProcessInstance pInstance = runtimeService.startProcessInstanceByKey(key, bo.getBusinessKey(), vars);
        wfTaskService.startFirstTask(pInstance.getId(), vars);
        return pInstance.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startTaskApprove(String taskId, String approver) {
        // 1. 参数校验（任务实体是否存在：MVP 先不校验，避免依赖 project）
        if (taskId == null || taskId.isBlank() || approver == null || approver.isBlank()) {
            throw new ServiceException("taskId 或 approver 不能为空");
        }

        // 2. 查关联表：已在审批中则拒绝
        // 注意 type 要用 TYPE_TASK（"task"），不是 approver
        WfTaskProcess existing = wfTaskProcessMapper.selectOne(
                new LambdaQueryWrapper<WfTaskProcess>()
                        .eq(WfTaskProcess::getExtraId, taskId)
                        .eq(WfTaskProcess::getType, TYPE_TASK)
                        .last("limit 1"));
        if (existing != null && APPROVED_IN_PROGRESS.equals(existing.getApproved())) {
            throw new ServiceException("该任务已在审批中，请勿重复发起");
        }

        // 3. 组装启动参数：approver 放进变量；initiator 由 startProcess 补
        Map<String, Object> variables = new HashMap<>();
        variables.put("approver", approver);

        WfStartProcessBo bo = new WfStartProcessBo();
        bo.setProcessKey(TASK_APPROVE_KEY);
        bo.setBusinessKey(taskId);
        bo.setVariables(variables);

        // 4. 启动流程（内部会 startFirstTask，待办落到审批人）
        String instanceId = startProcess(bo);

        // 5. 写/更新业务关联表 approved=0
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        String userId = String.valueOf(SecurityUtils.getUserId());
        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            existing = new WfTaskProcess();
            existing.setExtraId(taskId);
            existing.setType(TYPE_TASK);
            existing.setCreatedBy(userId);
            existing.setCreatedTime(now);
        }
        existing.setApproved(APPROVED_IN_PROGRESS);
        existing.setInstanceId(instanceId);
        if (pi != null) {
            existing.setDefinitionId(pi.getProcessDefinitionId());
            existing.setDeploymentId(pi.getDeploymentId());
        }
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(now);

        if (existing.getId() == null) {
            wfTaskProcessMapper.insert(existing);
        } else {
            wfTaskProcessMapper.updateById(existing);
        }

        // 6. 返回流程实例 id
        return instanceId;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String startProjectApprove(String projectId, String approver) {
        // TODO(H4 后置): 与 startTaskApprove 同构，type=project
        // 1. 参数校验（任务实体是否存在：MVP 先不校验，避免依赖 project）
        if (projectId == null || projectId.isBlank() || approver == null || approver.isBlank()) {
            throw new ServiceException("projectId 或 approver 不能为空");
        }

        WfTaskProcess existing = wfTaskProcessMapper.selectOne(
                new LambdaQueryWrapper<WfTaskProcess>()
                        .eq(WfTaskProcess::getExtraId, projectId)
                        .eq(WfTaskProcess::getType, TYPE_PROJECT)
                        .last("limit 1"));
        if (existing != null && APPROVED_IN_PROGRESS.equals(existing.getApproved())) {
            throw new ServiceException("该项目已在审批中，请勿重复发起");
        }

        // 3. 组装启动参数：approver 放进变量；initiator 由 startProcess 补
        Map<String, Object> variables = new HashMap<>();
        variables.put("approver", approver);

        WfStartProcessBo bo = new WfStartProcessBo();
        bo.setProcessKey(TASK_APPROVE_KEY);
        bo.setBusinessKey(projectId);
        bo.setVariables(variables);

        // 4. 启动流程（内部会 startFirstTask，待办落到审批人）
        String instanceId = startProcess(bo);

        // 5. 写/更新业务关联表 approved=0
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceId(instanceId)
                .singleResult();
        String userId = String.valueOf(SecurityUtils.getUserId());
        LocalDateTime now = LocalDateTime.now();

        if (existing == null) {
            existing = new WfTaskProcess();
            existing.setExtraId(projectId);
            existing.setType(TYPE_PROJECT);
            existing.setCreatedBy(userId);
            existing.setCreatedTime(now);
        }
        existing.setApproved(APPROVED_IN_PROGRESS);
        existing.setInstanceId(instanceId);
        if (pi != null) {
            existing.setDefinitionId(pi.getProcessDefinitionId());
            existing.setDeploymentId(pi.getDeploymentId());
        }
        existing.setUpdatedBy(userId);
        existing.setUpdatedTime(now);

        if (existing.getId() == null) {
            wfTaskProcessMapper.insert(existing);
        } else {
            wfTaskProcessMapper.updateById(existing);
        }

        // 6. 返回流程实例 id
        return instanceId;
    }

    @Override
    public List<WfTaskVo> todoList() {
        String userId = String.valueOf(SecurityUtils.getUserId());
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).orderByTaskCreateTime().desc().list();
        return tasks.stream().map(task -> {
            WfTaskVo vo = new WfTaskVo();
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getName());
            vo.setProcInsId(task.getProcessInstanceId());
            vo.setProcDefId(task.getProcessDefinitionId());
            ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (pi != null) {
                vo.setBusinessKey(pi.getBusinessKey());
            }
            vo.setAssignee(task.getAssignee());
            vo.setCreateTime(task.getCreateTime());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WfTaskVo> finishedList() {
        String userId = String.valueOf(SecurityUtils.getUserId());
        List<HistoricTaskInstance> tasks = historyService.createHistoricTaskInstanceQuery().finished()
                .taskAssignee(userId)
                .orderByHistoricTaskInstanceEndTime().desc()
                .list();
        return tasks.stream().map(task -> {
            WfTaskVo vo = new WfTaskVo();
            vo.setTaskId(task.getId());
            vo.setTaskName(task.getName());
            vo.setProcInsId(task.getProcessInstanceId());
            vo.setProcDefId(task.getProcessDefinitionId());
            HistoricProcessInstance hi = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(task.getProcessInstanceId())
                    .singleResult();
            if (hi != null) {
                vo.setBusinessKey(hi.getBusinessKey());
            }
            vo.setAssignee(task.getAssignee());
            vo.setCreateTime(task.getCreateTime());
            vo.setFinishTime(task.getEndTime());
            return vo;
        }).collect(Collectors.toList());
    }
}
