package com.zhq.taskforge.workflow.domain;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 业务与 Flowable 实例关联表（复用 pmhub_project_task_process）。
 * approved = "0" 表示审批中，业务侧禁止手改状态。
 */
@Data
@TableName("pmhub_project_task_process")
public class WfTaskProcess implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** 业务 id：任务 id / 项目 id */
    private String extraId;

    /** "0" = 审批中；其它 = 可按原规则编辑 */
    private String approved;

    @TableField(value = "instance_id", updateStrategy = FieldStrategy.IGNORED)
    private String instanceId;

    @TableField(value = "deployment_id", updateStrategy = FieldStrategy.IGNORED)
    private String deploymentId;

    @TableField(value = "definition_id", updateStrategy = FieldStrategy.IGNORED)
    private String definitionId;

    /** 类型：task / project */
    private String type;

    /** Flowable 当前用户任务 id（可选） */
    @TableField(value = "task_id", updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    @TableField(value = "url", updateStrategy = FieldStrategy.IGNORED)
    private String url;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
