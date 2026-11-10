package com.zhq.taskforge.project.domain.vo.task;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data 
public class WorkTimeResVO {

    private String workTimeId;
    private String taskId;
    private BigDecimal workTime;
    private Long userId;
    private String nickName;
    @JsonFormat (pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
