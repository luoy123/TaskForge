package com.zhq.taskforge.project.domain.vo.task;

import com.alibaba.excel.annotation.ExcelProperty;

import lombok.Data;

/**
 * 任务导入行（G4）。Excel 表头需与 @ExcelProperty 一致。
 */
@Data
public class TaskExcelVO {

    @ExcelProperty("项目编码(必填)")
    private String projectCode;

    @ExcelProperty("任务名称(必填)")
    private String taskName;

    /** 数字字符串，如 2=普通，对照 ProjectTaskPriorityEnum */
    @ExcelProperty("优先级(必填)")
    private String taskPriority;

    /** 登录用户名 user_name */
    @ExcelProperty("执行人(必填)")
    private String username;

    @ExcelProperty("开始时间")
    private String beginTime;

    @ExcelProperty("结束时间")
    private String endTime;

    @ExcelProperty("截止时间")
    private String closeTime;
}
