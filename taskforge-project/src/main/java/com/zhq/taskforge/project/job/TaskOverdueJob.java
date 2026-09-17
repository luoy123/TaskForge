package com.zhq.taskforge.project.job;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zhq.taskforge.common.enums.ProjectTaskStatusEnum;
import com.zhq.taskforge.project.domain.ProjectTask;
import com.zhq.taskforge.project.mapper.ProjectTaskMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 逾期扫描：由 Quartz 通过 invoke_target 调用。
 * 口径与 pmhub / statistics 一致：看 closeTime（截止时间），不是 endTime（预计结束）。
 * sys_job 示例：taskOverdueTask.scanOverdue()
 */
@Component("taskOverdueTask")
@Slf4j
public class TaskOverdueJob {

    /** 避免单次 IN 过长；量再大可再调 */
    private static final int UPDATE_BATCH_SIZE = 500;

    @Autowired
    private ProjectTaskMapper projectTaskMapper;

    @Transactional(rollbackFor = Exception.class)
    public void scanOverdue() {
        LocalDateTime now = LocalDateTime.now();
        // 只扫「该完成却还没完成」的状态
        List<Integer> canOverdue = Arrays.asList(
                ProjectTaskStatusEnum.NO_STARTED.getStatus(),
                ProjectTaskStatusEnum.DOING.getStatus(),
                ProjectTaskStatusEnum.NO_CLAIMED.getStatus());

        List<ProjectTask> list = projectTaskMapper.selectList(new LambdaQueryWrapper<ProjectTask>()
                .eq(ProjectTask::getDeleted, 0)
                .isNotNull(ProjectTask::getCloseTime)
                .lt(ProjectTask::getCloseTime, now)
                .in(ProjectTask::getStatus, canOverdue)
                // 审批中(approved=0)不改状态，避免覆盖审批流
                .notInSql(ProjectTask::getId,
                        "select extra_id from pmhub_project_task_process where type = 'task' and approved = '0'"));

        if (list == null || list.isEmpty()) {
            log.info("逾期扫描：无需更新");
            return;
        }

        List<String> idList = list.stream().map(ProjectTask::getId).toList();
        for (int from = 0; from < idList.size(); from += UPDATE_BATCH_SIZE) {
            int to = Math.min(from + UPDATE_BATCH_SIZE, idList.size());
            List<String> batch = idList.subList(from, to);
            projectTaskMapper.update(null, new LambdaUpdateWrapper<ProjectTask>()
                    .in(ProjectTask::getId, batch)
                    .set(ProjectTask::getStatus, ProjectTaskStatusEnum.OVERDUE.getStatus())
                    .set(ProjectTask::getUpdatedTime, now));
        }
        log.info("逾期扫描：已置逾期 {} 条, ids={}", idList.size(), idList);
    }
}
