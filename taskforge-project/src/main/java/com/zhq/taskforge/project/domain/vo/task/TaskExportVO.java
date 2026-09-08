package com.zhq.taskforge.project.domain.vo.task;

import java.time.LocalDateTime;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;

import lombok.Data;

/**
 * 任务导出行（G4）。
 */
@Data
public class TaskExportVO {

    @ExcelProperty("任务名称")
    private String taskName;

    @ExcelProperty("所属项目")
    private String projectName;

    @ExcelProperty("所处阶段")
    private String stageName;

    @ExcelProperty("执行人")
    private String executor;

    @ExcelIgnore
    private Integer status;

    @ExcelIgnore
    private Integer executeStatus;

    @ExcelIgnore
    private Integer taskPriority;

    @ExcelProperty("任务状态")
    private String statusName;

    @ExcelProperty("执行状态")
    private String executeStatusName;

    @ExcelProperty("优先级")
    private String taskPriorityName;

    @ExcelProperty("创建人")
    private String createdBy;

    @ExcelProperty("创建时间")
    @DateTimeFormat("yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
