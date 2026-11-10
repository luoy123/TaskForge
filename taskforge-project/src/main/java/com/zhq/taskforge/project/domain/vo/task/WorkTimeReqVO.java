package com.zhq.taskforge.project.domain.vo.task;

import java.math.BigDecimal;

import lombok.Data;
/**
 * 工作时间请求VO
 * 不放入userid,默认就是去当前登录用户
 * projectId不通过前端传递，使用taskId查出数据，然后校验是否存在，
 * 再用task.getProejctId(),既可以校验任务是否存在，也不出出现任务属于a
 * 但工时记录到了B项目中。
 * workTimeReqVO
 */
@Data
public class WorkTimeReqVO {
    private String taskId;
    private BigDecimal workTime;
    private String workTimeId;

    private Long pageNum;
    private Long pageSize;

}
