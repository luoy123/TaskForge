package com.zhq.taskforge.project.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目详情/列表出参。
 */
@Data
public class ProjectResVO {

    private String projectId;

    private String projectCode;

    private String projectName;

    private Integer stageCode;

    private String stageName;

    private Integer status;

    private String statusName;

    /** 对应表字段 type：0 公开 / 1 私有 */
    private Integer projectType;

    private String projectTypeName;

    private Integer published;

    private String publishedName;

    private BigDecimal projectProcess;

    private Long userId;

    private String nickName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeBeginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeEndTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    private String description;

    private String cover;

    private Boolean collected;

    private Integer openPrefix;

    private String prefix;

    private Integer autoUpdateProcess;

    private Integer openTaskPrivate;

    private Integer msgNotify;

    private Integer notifyDay;
}
