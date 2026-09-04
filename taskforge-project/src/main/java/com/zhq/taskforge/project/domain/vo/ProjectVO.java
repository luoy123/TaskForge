package com.zhq.taskforge.project.domain.vo;

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
}
