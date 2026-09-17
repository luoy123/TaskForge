package com.zhq.taskforge.project.domain.vo.task;

import lombok.Data;

@Data
public class TaskStatusStatsVO {
    // 任务总数
    private Integer total;
    // 分配者
    private Integer toBeAssign;
    // 未完成
    private Integer unDone;
    // 完成
    private Integer done;
    // 逾期
    private Integer overdue;
    // 今日过期
    private Integer expireToday;
    // 无过期时间
    private Integer timeUndetermined;
}
