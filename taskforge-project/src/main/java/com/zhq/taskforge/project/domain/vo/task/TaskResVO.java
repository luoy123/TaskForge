package com.zhq.taskforge.project.domain.vo.task;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 任务出参（F1 骨架；F2 详情/列表再补齐）。
 */
@Data
public class TaskResVO {

    private String taskId;

    private String projectId;

    /** 所属项目名（我的任务列表等） */
    private String projectName;

    private String taskName;

    private String description;

    private Long userId;

    private String nickName;

    private String projectStageId;

    private String stageName;

    private Integer taskPriority;

    private String priorityName;

    private Integer status;

    private String statusName;

    private Integer executeStatus;

    /** 审批态（pmhub_project_task_process.approved）：0 审批中 / 1 通过 / 2 驳回；未发起过为 null */
    private String approved;

    private BigDecimal taskProcess;

    private String taskPid;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}