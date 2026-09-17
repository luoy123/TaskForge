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

    /**
     * 列表类型
     * my = 我参与的项目（default)
     * collect = 我收藏的
     * recycle = 回收站
     */
    private String type;

}
