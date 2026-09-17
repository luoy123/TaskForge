package com.zhq.taskforge.workflow.service;

import java.util.Map;

import com.zhq.taskforge.workflow.domain.bo.WfTaskBo;

/**
 * 用户任务办理。
 */
public interface IWfTaskService {

    /**
     * 通过并完成任务；通过后应回写业务 approved / 状态（H4，留给你实现）。
     */
    void complete(WfTaskBo taskBo);

    /**
     * 驳回：本 Sprint 建议「结束实例 + 放开 approved」。
     * 策略写死一种即可，不要做复杂退回。
     */
    void reject(WfTaskBo taskBo);

    /**
     * 若首节点为发起人，则自动 complete（对照 pmhub startFirstTask）。
     */
    void startFirstTask(String processInstanceId, Map<String, Object> variables);
}
