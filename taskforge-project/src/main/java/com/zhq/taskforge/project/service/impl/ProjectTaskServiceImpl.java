package com.zhq.taskforge.project.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.enums.LogTypeEnum;
import com.zhq.taskforge.common.enums.ProjectStatusEnum;
import com.zhq.taskforge.common.enums.ProjectTaskPriorityEnum;
import com.zhq.taskforge.common.enums.ProjectTaskStatusEnum;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.ProjectMember;
import com.zhq.taskforge.project.domain.ProjectStage;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.domain.vo.task.TaskExcelVO;
import com.zhq.taskforge.project.domain.vo.task.TaskExportVO;
import com.zhq.taskforge.project.domain.vo.task.TaskReqVO;
import com.zhq.taskforge.project.domain.vo.task.TaskResVO;
import com.zhq.taskforge.project.mapper.ProjectLogMapper;
import com.zhq.taskforge.project.mapper.ProjectMapper;
import com.zhq.taskforge.project.mapper.ProjectMemberMapper;
import com.zhq.taskforge.project.mapper.ProjectStageMapper;
import com.zhq.taskforge.project.mapper.ProjectTaskMapper;
import com.zhq.taskforge.project.service.IProjectTaskService;

/**
 * 项目任务（Sprint F / G4）。
 */
@Service
public class ProjectTaskServiceImpl implements IProjectTaskService {

    @Autowired
    private ProjectTaskMapper projectTaskMapper;
    @Autowired
    private ProjectMapper projectMapper;
    @Autowired
    private ProjectMemberMapper projectMemberMapper;
    @Autowired
    private ProjectLogMapper projectLogMapper;
    @Autowired
    private ProjectStageMapper projectStageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String add(TaskReqVO req) {
        if (req.getProjectId() == null || req.getProjectId().isBlank()) {
            throw new ServiceException("项目ID不能为空");
        }
        if (req.getTaskName() == null || req.getTaskName().isBlank()) {
            throw new ServiceException("任务名称不能为空");
        }
        Project project = projectMapper.selectById(req.getProjectId());
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (ProjectStatusEnum.PAUSE.getStatus().equals(project.getStatus())) {
            throw new ServiceException("项目已暂停");
        }

        LocalDateTime now = LocalDateTime.now();
        ProjectTask task = new ProjectTask();
        task.setProjectId(req.getProjectId());
        task.setTaskName(req.getTaskName());
        task.setDescription(req.getDescription());
        task.setTaskPid(req.getTaskPid());
        Long executorId = req.getUserId() != null ? req.getUserId() : SecurityUtils.getUserId();
        task.setUserId(executorId);
        String stageId = req.getProjectStageId() != null ? req.getProjectStageId() : project.getProjectStageId();
        task.setProjectStageId(stageId);
        Integer priority = req.getTaskPriority() != null ? req.getTaskPriority()
                : ProjectTaskPriorityEnum.STAGE_2.getStatus();
        task.setTaskPriority(priority);
        task.setStatus(ProjectTaskStatusEnum.NO_STARTED.getStatus());
        task.setExecuteStatus(ProjectTaskStatusEnum.NO_STARTED.getStatus());
        task.setDeleted(0);
        task.setTaskProcess(BigDecimal.ZERO);
        task.setCreatedBy(SecurityUtils.getUsername());
        task.setCreatedTime(now);
        task.setUpdatedBy(SecurityUtils.getUsername());
        task.setUpdatedTime(now);
        projectTaskMapper.insert(task);

        insertMember(task.getId(), 1, SecurityUtils.getUserId());
        saveLog("addTask", task.getId(), req.getProjectId(), req.getTaskName(), "参与了任务", null);
        if (executorId != null && !executorId.equals(SecurityUtils.getUserId())) {
            insertMember(task.getId(), 0, executorId);
            saveLog("invitePartakeTask", task.getId(), req.getProjectId(), req.getTaskName(), "邀请执行人参与任务",
                    executorId);
        }
        return task.getId();
    }

    private void insertMember(String taskId, int creator, Long userId) {
        LocalDateTime now = LocalDateTime.now();
        ProjectMember member = new ProjectMember();
        member.setPtId(taskId);
        member.setUserId(userId);
        member.setJoinedTime(now);
        member.setCreatedBy(SecurityUtils.getUsername());
        member.setCreatedTime(now);
        member.setUpdatedBy(SecurityUtils.getUsername());
        member.setUpdatedTime(now);
        member.setType("task");
        member.setCreator(creator);
        projectMemberMapper.insert(member);
    }

    private void saveLog(String operateType, String taskId, String projectId, String taskName, String remark,
            Long toUserId) {
        LocalDateTime now = LocalDateTime.now();
        ProjectLog log = new ProjectLog();
        log.setLogType(LogTypeEnum.TRENDS.getStatus());
        log.setOperateType(operateType);
        log.setType(ProjectStatusEnum.TASK.getStatusName());
        log.setPtId(taskId);
        log.setProjectId(projectId);
        log.setUserId(SecurityUtils.getUserId());
        log.setRemark(remark);
        log.setContent(taskName);
        if (toUserId != null) {
            log.setToUserId(toUserId);
        }
        log.setCreatedTime(now);
        log.setCreatedBy(SecurityUtils.getUsername());
        log.setUpdatedTime(now);
        log.setUpdatedBy(SecurityUtils.getUsername());
        projectLogMapper.insert(log);
    }

    @Override
    public TaskResVO detail(String taskId) {
        TaskResVO detail = projectTaskMapper.detail(taskId);
        if (detail == null) {
            throw new ServiceException("任务不存在");
        }
        detail.setStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(detail.getStatus()));
        detail.setPriorityName(ProjectTaskPriorityEnum.getStatusNameByStatus(detail.getTaskPriority()));
        return detail;
    }

    @Override
    public IPage<TaskResVO> list(TaskReqVO req) {
        if (req.getProjectId() == null) {
            throw new ServiceException("项目ID不能为空");
        }
        long pageNum = req.getPageNum() == null ? 1L : req.getPageNum();
        long pageSize = req.getPageSize() == null ? 10L : req.getPageSize();
        Page<TaskResVO> page = new Page<>(pageNum, pageSize);
        IPage<TaskResVO> result = projectTaskMapper.selectTaskList(page, req);
        for (TaskResVO task : result.getRecords()) {
            task.setStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(task.getStatus()));
            task.setPriorityName(ProjectTaskPriorityEnum.getStatusNameByStatus(task.getTaskPriority()));
        }
        return result;
    }

    @Override
    public void edit(TaskReqVO req) {
        if (req.getTaskId() == null || req.getTaskId().isBlank()) {
            throw new ServiceException("任务ID不能为空");
        }
        ProjectTask old = projectTaskMapper.selectById(req.getTaskId());
        if (old == null || Integer.valueOf(1).equals(old.getDeleted())) {
            throw new ServiceException("任务不存在");
        }
        Project project = projectMapper.selectById(old.getProjectId());
        if (project == null) {
            throw new ServiceException("项目不存在");
        }
        if (ProjectStatusEnum.PAUSE.getStatus().equals(project.getStatus())) {
            throw new ServiceException("项目已暂停,无法操作任务");
        }
        ProjectTask task = new ProjectTask();
        task.setId(req.getTaskId());
        if (req.getTaskName() != null) {
            task.setTaskName(req.getTaskName());
        }
        if (req.getDescription() != null) {
            task.setDescription(req.getDescription());
        }
        if (req.getTaskPriority() != null) {
            task.setTaskPriority(req.getTaskPriority());
        }
        if (req.getStatus() != null) {
            task.setStatus(req.getStatus());
        }
        if (req.getExecuteStatus() != null) {
            task.setExecuteStatus(req.getExecuteStatus());
        }
        if (req.getUserId() != null) {
            task.setUserId(req.getUserId());
        }
        if (req.getProjectStageId() != null) {
            task.setProjectStageId(req.getProjectStageId());
        }
        task.setUpdatedBy(SecurityUtils.getUsername());
        task.setUpdatedTime(LocalDateTime.now());
        projectTaskMapper.updateById(task);
        saveLog("editTask", req.getTaskId(), old.getProjectId(),
                req.getTaskName() != null ? req.getTaskName() : old.getTaskName(), "编辑了任务", null);
    }

    @Override
    public void delete(String taskId) {
        if (taskId == null || taskId.isBlank()) {
            throw new ServiceException("任务ID不能为空");
        }
        ProjectTask task = projectTaskMapper.selectById(taskId);
        if (task == null || (task.getDeleted() != null && task.getDeleted() == 1)) {
            throw new ServiceException("任务不存在");
        }
        LambdaQueryWrapper<ProjectTask> qw = new LambdaQueryWrapper<>();
        qw.eq(ProjectTask::getTaskPid, taskId).eq(ProjectTask::getDeleted, 0);
        if (projectTaskMapper.selectCount(qw) > 0) {
            throw new ServiceException("任务下存在子任务，不能删除");
        }
        LocalDateTime now = LocalDateTime.now();
        int rows = projectTaskMapper.softDelete(taskId, now, SecurityUtils.getUsername(), now);
        if (rows == 0) {
            throw new ServiceException("任务不存在或已删除");
        }
        saveLog("deleteTask", taskId, task.getProjectId(), task.getTaskName(), "删除了任务", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addChildTask(TaskReqVO req) {
        if (req.getTaskId() == null || req.getTaskId().isBlank()) {
            throw new ServiceException("父任务ID不能为空");
        }
        ProjectTask parent = projectTaskMapper.selectById(req.getTaskId());
        if (parent == null || Integer.valueOf(1).equals(parent.getDeleted())) {
            throw new ServiceException("父任务不存在");
        }
        req.setTaskPid(parent.getId());
        if (req.getProjectId() == null || req.getProjectId().isBlank()) {
            req.setProjectId(parent.getProjectId());
        }
        req.setTaskId(null);
        return add(req);
    }

    @Override
    public List<TaskResVO> queryChildTask(String parentTaskId) {
        if (parentTaskId == null || parentTaskId.isBlank()) {
            throw new ServiceException("父任务ID不能为空");
        }
        List<TaskResVO> list = projectTaskMapper.queryChildTask(parentTaskId);
        for (TaskResVO task : list) {
            task.setStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(task.getStatus()));
            task.setPriorityName(ProjectTaskPriorityEnum.getStatusNameByStatus(task.getTaskPriority()));
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(TaskReqVO req) {
        if (req.getTaskId() == null || req.getTaskId().isBlank() || req.getComment() == null
                || req.getComment().isBlank()) {
            throw new ServiceException("任务ID和评论不能为空");
        }
        ProjectTask task = projectTaskMapper.selectById(req.getTaskId());
        if (task == null || Integer.valueOf(1).equals(task.getDeleted())) {
            throw new ServiceException("任务不存在");
        }
        String projectId = req.getProjectId() != null ? req.getProjectId() : task.getProjectId();
        LocalDateTime now = LocalDateTime.now();
        ProjectLog log = new ProjectLog();
        log.setLogType(LogTypeEnum.COMMENT.getStatus());
        log.setOperateType("comment");
        log.setType(ProjectStatusEnum.TASK.getStatusName());
        log.setPtId(req.getTaskId());
        log.setProjectId(projectId);
        log.setUserId(SecurityUtils.getUserId());
        log.setContent(req.getComment());
        log.setRemark("添加了评论");
        log.setCreatedBy(SecurityUtils.getUsername());
        log.setCreatedTime(now);
        log.setUpdatedBy(SecurityUtils.getUsername());
        log.setUpdatedTime(now);
        projectLogMapper.insert(log);
    }

    @Override
    public List<ProjectLog> queryTaskLogList(TaskReqVO req) {
        if (req.getTaskId() == null || req.getTaskId().isBlank()) {
            throw new ServiceException("任务ID不能为空");
        }
        LambdaQueryWrapper<ProjectLog> qw = new LambdaQueryWrapper<>();
        qw.eq(ProjectLog::getPtId, req.getTaskId())
                .eq(ProjectLog::getType, ProjectStatusEnum.TASK.getStatusName());
        if (req.getLogType() != null) {
            qw.eq(ProjectLog::getLogType, req.getLogType());
        }
        qw.orderByDesc(ProjectLog::getCreatedTime);
        return projectLogMapper.selectList(qw);
    }

    @Override
    public List<TaskExportVO> export(List<String> taskIds) {
        // TODO G4 export:
        // 1. taskIds 为空 → ServiceException
        if (taskIds == null || taskIds.isEmpty()) {
            throw new ServiceException("taskIds 不能为空");
        }
        // 2. List<TaskExportVO> list = projectTaskMapper.export(taskIds);
        List<TaskExportVO> list = projectTaskMapper.export(taskIds);
        // 3. for 每条补 statusName / executeStatusName / taskPriorityName（用枚举
        for (TaskExportVO task : list) {
            task.setStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(task.getStatus()));
            task.setExecuteStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(task.getExecuteStatus()));
            task.setTaskPriorityName(ProjectTaskPriorityEnum.getStatusNameByStatus(task.getTaskPriority()));
        }
        // 4. return list;
        return list;
    }

    @Override
    public List<TaskExportVO> exportAll() {
        // TODO G4 exportAll:
        // 1. list = projectTaskMapper.exportAll(SecurityUtils.getUserId());
        List<TaskExportVO> list = projectTaskMapper.exportAll(SecurityUtils.getUserId());
        // 2. 同样补三种 Name
        for (TaskExportVO task : list) {
            task.setStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(task.getStatus()));
            task.setExecuteStatusName(ProjectTaskStatusEnum.getStatusNameByStatus(task.getExecuteStatus()));
            task.setTaskPriorityName(ProjectTaskPriorityEnum.getStatusNameByStatus(task.getTaskPriority()));
        }
        // 3. return list;
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importTask(List<TaskExcelVO> rows) {
        // TODO G4 importTask — 计划规则：
        // 1. rows 空 → 抛「导入任务数据不能为空」
        if (rows == null || rows.isEmpty()) {
            throw new ServiceException("导入任务数据不能为空");
        }
        // 2. int ok=0, skip=0; for (TaskExcelVO row : rows) {
        // Long userId = projectTaskMapper.selectUserIdByUsername(row.getUsername());
        // if (userId == null) { skip++; continue; }
        // Project 按 projectCode 查；没有则 skip
        // 执行人须是该项目 type=project 的成员；否则 skip
        // 阶段：projectStageMapper 按 projectId orderByAsc stageCode，取第一条
        // insert ProjectTask（优先级 Integer.valueOf(row.getTaskPriority())；时间可先不解析或简单解析）
        // insertMember 创建者 + 必要时执行人；saveLog("importTask", ...)
        // ok++;
        // }

        int ok = 0, skip = 0;
        for (TaskExcelVO row : rows) {
            Long userId = projectTaskMapper.selectUserIdByUsername(row.getUsername());
            if (userId == null) {
                skip++;
                continue;
            }
            Project project = projectMapper
                    .selectOne(new LambdaQueryWrapper<Project>().eq(Project::getProjectCode, row.getProjectCode()));
            if (project == null) {
                skip++;
                continue;
            }
            ProjectMember projectMember = projectMemberMapper
                    .selectOne(new LambdaQueryWrapper<ProjectMember>().eq(ProjectMember::getPtId, project.getId())
                            .eq(ProjectMember::getUserId, userId).eq(ProjectMember::getType, "project"));
            if (projectMember == null) {
                skip++;
                continue;
            }
            ProjectStage stage = projectStageMapper.selectOne(new LambdaQueryWrapper<ProjectStage>()
                    .eq(ProjectStage::getProjectId, project.getId())
                    .orderByAsc(ProjectStage::getStageCode)
                    .last("LIMIT 1"));
            if (stage == null) {
                skip++;
                continue;
            }
            Integer priority;
            try {
                priority = Integer.valueOf(row.getTaskPriority());
            } catch (Exception e) {
                skip++;
                continue;
            }
            LocalDateTime now = LocalDateTime.now();
            ProjectTask projectTask = new ProjectTask();
            projectTask.setProjectId(project.getId());
            projectTask.setTaskName(row.getTaskName());
            projectTask.setTaskPriority(priority);
            projectTask.setTaskPid(null);
            projectTask.setUserId(userId);
            projectTask.setProjectStageId(stage.getId());
            projectTask.setStatus(ProjectTaskStatusEnum.NO_STARTED.getStatus());
            projectTask.setExecuteStatus(ProjectTaskStatusEnum.NO_STARTED.getStatus());
            projectTask.setDeleted(0);
            projectTask.setTaskProcess(BigDecimal.ZERO);
            projectTask.setCreatedBy(SecurityUtils.getUsername());
            projectTask.setCreatedTime(now);
            projectTask.setUpdatedBy(SecurityUtils.getUsername());
            projectTask.setUpdatedTime(now);
            projectTaskMapper.insert(projectTask);
            insertMember(projectTask.getId(), 1, SecurityUtils.getUserId());
            saveLog("importTask", projectTask.getId(), project.getId(), row.getTaskName(), "导入任务", null);
            if (!userId.equals(SecurityUtils.getUserId())) {
                insertMember(projectTask.getId(), 0, userId);
                saveLog("invitePartakeTask", projectTask.getId(), project.getId(), row.getTaskName(), "邀请执行人参与任务",
                        userId);
            }
            ok++;
        }
        // 3. return "成功 " + ok + " 条，跳过 " + skip + " 条";
        // 注意：单行失败用 continue，不要让整批因一行炸；查不到用户/项目用 continue
        return "成功 " + ok + " 条，跳过 " + skip + " 条";
    }
}
