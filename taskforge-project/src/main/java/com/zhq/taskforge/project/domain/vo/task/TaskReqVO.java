package com.zhq.taskforge.project.domain.vo.task;

import lombok.Data;

/**
 * 任务入参。
 */
@Data
public class TaskReqVO {

    private String taskId;

    private String projectId;

    private String taskName;

    private String description;

    /** 执行人 */
    private Long userId;

    private String projectStageId;

    private Integer taskPriority;

    private Integer status;

    private Integer executeStatus;

    /** 父任务 id；子任务时传入 */
    private String taskPid;

    /** 评论内容（F4 addComment） */
    private String comment;

    /** 日志类型筛选，可选：1 动态 / 3 评论（F4 logList） */
    private Integer logType;

    /** 导出用：任务 id 列表（G4 export） */
    private java.util.List<String> taskIds;

    private Long pageNum;

    private Long pageSize;

    /**
     * 我的任务列表类型
     * 1 = 我执行的（user_id = 当前用户）
     * 3 = 我创建的 (create_by = 当前用户名)
     * 不传默认为1
     */
    private Integer type;
}
