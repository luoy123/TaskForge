package com.zhq.taskforge.project.domain;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 任务提醒记录（已提醒过的去重记录），表 pmhub_project_task_notify。
 * <p>提醒配置在项目上：msg_notify（开关）+ notify_day（提前天数）。
 */
@Data
@TableName("pmhub_project_task_notify")
public class ProjectTaskNotify {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String taskId;

    private Long userId;

    private String userWxName;

    private String projectId;

    /** 是否逾期 0-待逾期提醒 1-已逾期提醒 */
    private Integer overdue;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeTime;

    private String taskName;
}
