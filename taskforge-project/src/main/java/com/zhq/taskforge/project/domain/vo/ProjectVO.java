package com.zhq.taskforge.project.domain.vo;

import java.util.List;

import lombok.Data;

/**
 * 项目请求入参（详情等）。
 */
@Data
public class ProjectVO {

    private String projectId;

    private String projectName;

    private String cover;

    private Integer status;

    /** 状态中文名（doing / select 出参用） */
    private String statusName;

    // 邀请或者移除
    private List<Long> userIdList;
}
