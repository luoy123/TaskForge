package com.zhq.taskforge.workflow.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.workflow.domain.WfTaskProcess;
import com.zhq.taskforge.workflow.domain.bo.WfTaskBo;
import com.zhq.taskforge.workflow.factory.FlowServiceFactory;
import com.zhq.taskforge.workflow.mapper.WfTaskProcessMapper;
import com.zhq.taskforge.workflow.service.IWfTaskService;

/**
 * 用户任务办理。
 * <p>
 * 对照 pmhub：{@code WfTaskServiceImpl#complete}、{@code #startFirstTask}。
 */
@Service
public class WfTaskServiceImpl extends FlowServiceFactory implements IWfTaskService {

    @Autowired
    private WfTaskProcessMapper wfTaskProcessMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void complete(WfTaskBo taskBo) {
        if (taskBo.getTaskId() == null || taskBo.getTaskId().isBlank()) {
            throw new ServiceException("任务ID不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(taskBo.getTaskId()).singleResult();
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        if (!String.valueOf(SecurityUtils.getUserId()).equals(task.getAssignee())) {
            throw new ServiceException("用户不匹配");
        }
        Map<String, Object> var = taskBo.getVariables();
        if (var != null && !var.isEmpty()) {
            taskService.complete(taskBo.getTaskId(), var);
        } else {
            taskService.complete(taskBo.getTaskId());
        }

        String processInstanceId = task.getProcessInstanceId();
        long left = taskService.createTaskQuery()
                .processInstanceId(processInstanceId)
                .count();
        if (left == 0) {
            WfTaskProcess selectOne = wfTaskProcessMapper.selectOne(
                    new LambdaQueryWrapper<WfTaskProcess>()
                            .eq(WfTaskProcess::getInstanceId, processInstanceId)
                            .last("limit 1"));
            if (selectOne != null) {
                selectOne.setApproved("1");
                selectOne.setInstanceId(null);
                selectOne.setUpdatedBy(String.valueOf(SecurityUtils.getUserId()));
                selectOne.setUpdatedTime(LocalDateTime.now());
                wfTaskProcessMapper.updateById(selectOne);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void reject(WfTaskBo taskBo) {
        String taskId = taskBo.getTaskId();
        if (taskId == null || taskId.isBlank()) {
            throw new ServiceException("任务ID不能为空");
        }
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new ServiceException("任务不存在");
        }
        String processInstanceId = task.getProcessInstanceId();
        runtimeService.deleteProcessInstance(processInstanceId, "reject:" + taskBo.getComment());

        WfTaskProcess process = wfTaskProcessMapper.selectOne(
                new LambdaQueryWrapper<WfTaskProcess>()
                        .eq(WfTaskProcess::getInstanceId, processInstanceId)
                        .last("limit 1"));
        if (process != null) {
            process.setApproved("2");
            process.setInstanceId(null);
            process.setUpdatedBy(String.valueOf(SecurityUtils.getUserId()));
            process.setUpdatedTime(LocalDateTime.now());
            wfTaskProcessMapper.updateById(process);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void startFirstTask(String processInstanceId, Map<String, Object> variables) {
        List<Task> tasks = taskService.createTaskQuery()
                .processInstanceId(processInstanceId).list();
        String initiator = variables == null ? null : (String) variables.get("initiator");
        for (Task task : tasks) {
            if (initiator != null && initiator.equals(task.getAssignee())) {
                taskService.complete(task.getId(), variables);
                return;
            }
        }
    }
}
