package com.zhq.taskforge.project.domain.vo.task;

import lombok.Data;

@Data
public class BurnDownChartVO {
    private String date;
    private Integer taskNum;
    private Integer unDoneTaskNum;
}
