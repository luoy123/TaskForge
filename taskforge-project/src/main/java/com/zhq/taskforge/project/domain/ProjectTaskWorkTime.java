package com.zhq.taskforge.project.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("pmhub_project_task_work_time")
public class ProjectTaskWorkTime {

    @TableId(type=IdType.ASSIGN_UUID)
    private String id;
    /** 任务id */
    private String projectTaskId;
    /** 数据库中时String类型，但整个系统的userId都是Long类型，再插入数据的时候，会自动的变成varchar类型 */
    private Long userId;
    /**BigDecimal类型避免浮点精度问题 */
    private BigDecimal workTime;
    private String projectId;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String createdBy;
    private String updatedBy;
    
}
