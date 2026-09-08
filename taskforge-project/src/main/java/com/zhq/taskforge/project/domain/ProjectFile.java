package com.zhq.taskforge.project.domain;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 项目/任务附件，表 pmhub_project_file。
 * <p>软删建议自定义 softDelete，勿依赖 @TableLogic。
 */
@Data
@TableName("pmhub_project_file")
public class ProjectFile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** project / task / cover / template */
    private String type;

    private String ptId;

    private String projectId;

    private Long userId;

    private String fileName;

    private String extension;

    private String fileUrl;

    private String pathName;

    /** KB */
    private BigDecimal fileSize;

    private Integer deleted;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deletedTime;

    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;

    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedTime;
}
