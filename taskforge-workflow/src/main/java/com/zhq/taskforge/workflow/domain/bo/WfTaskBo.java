package com.zhq.taskforge.workflow.domain.bo;

import java.util.Map;

import lombok.Data;

/**
 * 办理任务入参（对照 pmhub WfTaskBo，字段可按需再扩）。
 */
@Data
public class WfTaskBo {

    /** Flowable 用户任务 id */
    private String taskId;

    /** 流程实例 id */
    private String procInsId;

    /** 办理意见 */
    private String comment;

    /** 流程变量（可选） */
    private Map<String, Object> variables;
}
