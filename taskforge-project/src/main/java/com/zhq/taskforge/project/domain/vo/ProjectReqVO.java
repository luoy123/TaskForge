package com.zhq.taskforge.project.domain.vo;

import lombok.Data;

@Data
public class ProjectReqVO {

    private Long pageNum;
    private Long pageSize;

    private String keyword;
    private String stageCode;
    private String status;
    private String published;
    private String projectType;

}
