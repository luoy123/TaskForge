package com.zhq.taskforge.project.domain.vo.file;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 文件列表出参（G3）。
 */
@Data
public class ProjectFileResVO {

    private String fileId;

    private String projectId;

    private String ptId;

    private String type;

    private String fileName;

    private String extension;

    private String fileUrl;

    private String pathName;

    private BigDecimal fileSize;

    private Long userId;

    private String nickName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdTime;
}
