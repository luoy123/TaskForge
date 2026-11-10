package com.zhq.taskforge.project.domain.vo;

import lombok.Data;

@Data 
public class ProjectStageVO {

    private String stageId;
    private String projectId;
    private String stageName;
    private Integer stageCode;
}
