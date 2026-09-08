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