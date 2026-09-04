package com.zhq.taskforge.project.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("pmhub_project")
@Data
public class Project implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String projectCode;

    private String projectName;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeBeginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closeEndTime;

    private String cover;

    /** 项目阶段编码，默认 0 */
    private Integer stageCode;

    /** 是否私有：0-公开 1-私有 */
    private Integer type;

    private String prefix;

    private Integer openPrefix;

    @TableLogic
    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    /** 是否归档：0-否 1-归档 */
    private Integer archived;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archivedTime;

    /** 是否发布：0-否 1-发布 */
    private Integer published;

    private BigDecimal projectProcess;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;

    private Long userId;

    /** 项目状态，默认 0-未开始 */
    private Integer status;

    private Integer autoUpdateProcess;

    private Integer openBeginTime;

    private Integer openTaskPrivate;

    private Integer msgNotify;

    private Integer notifyDay;

    private String projectStageId;

    @TableField(exist = false)
    private String projectId;

    @TableField(exist = false)
    private Integer projectType;
}
