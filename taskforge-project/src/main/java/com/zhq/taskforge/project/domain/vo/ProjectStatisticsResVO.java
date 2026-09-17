package com.zhq.taskforge.project.domain.vo;

import lombok.Data;

@Data
public class ProjectStatisticsResVO {
    private Long projectNum;
    private Long taskNum;
    private Long todayTaskNum;
    private Long overdueTaskNum;

}
