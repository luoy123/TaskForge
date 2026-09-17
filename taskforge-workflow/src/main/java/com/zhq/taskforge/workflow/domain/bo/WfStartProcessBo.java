package com.zhq.taskforge.workflow.domain.bo;

import java.util.Map;

import lombok.Data;

/**
 * 启动流程入参。
 */
@Data
public class WfStartProcessBo {

    /** 流程定义 key，内置任务审批为 task_approve */
    private String processKey;

    /** 业务主键（任务 id / 项目 id），写入 Flowable businessKey */
    private String businessKey;

    /** 流程变量，至少建议带 approver（审批人用户 id 字符串） */
    private Map<String, Object> variables;
}
