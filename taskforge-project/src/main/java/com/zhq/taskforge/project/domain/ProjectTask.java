package com.zhq.taskforge.project.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目任务，表 pmhub_project_task。
 * <p>软删建议走 Mapper softDelete，勿依赖全局 @TableLogic（与项目侧一致）。
 */
@Data
@TableName("pmhub_project_task")
public class ProjectTask implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    private String taskName;

    private String projectId;

    /** 优先级，见 ProjectTaskPriorityEnum */
    private Integer taskPriority;

    /** 执行人 */
    private Long userId;

    private String projectStageId;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime beginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;

    /** 父任务 id；顶级任务为空 */
    private String taskPid;

    private String assignTo;

    /** 任务状态，见 ProjectTaskStatusEnum */
    private Integer status;

    private Integer executeStatus;

    private BigDecimal taskProcess;

    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    private String taskFlow;

    private String taskTypeId;
}