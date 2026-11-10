package com.zhq.taskforge.project.domain.vo.task;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data 
public class TaskLogVO {

    private String id;
    private  Integer logType;
    private String operateType;
    private String content;
    private String remark;
    private Long userId;
    private String createdBy;
    private String nickName;
    private String logTypeName; 
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;



}
