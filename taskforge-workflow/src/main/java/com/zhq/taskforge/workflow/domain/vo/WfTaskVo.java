package com.zhq.taskforge.workflow.domain.vo;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 待办 / 已办任务展示（对照 pmhub WfTaskVo，MVP 精简字段）。
 */
@Data
public class WfTaskVo {

    private String taskId;

    private String taskName;

    private String procInsId;

    private String procDefId;

    private String procDefName;

    private String businessKey;

    private String assignee;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishTime;
}
