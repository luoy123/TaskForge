package com.zhq.taskforge.project.domain.vo.file;

import lombok.Data;

/**
 * 文件查询 / 重命名入参（G3）。
 */
@Data
public class ProjectFileReqVO {

    /** 业务 id：列表时按 type 含义不同；见 list 注释 */
    private String id;

    /** project / task / template；空则查项目下 project+task */
    private String type;

    /** 文件记录 id（rename / download） */
    private String fileId;

    /** 新文件名（rename） */
    private String fileName;

    private Long pageNum;

    private Long pageSize;
}
