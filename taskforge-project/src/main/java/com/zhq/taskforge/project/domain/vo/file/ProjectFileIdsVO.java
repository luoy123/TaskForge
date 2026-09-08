package com.zhq.taskforge.project.domain.vo.file;

import java.util.List;

import lombok.Data;

/**
 * 批量删除入参（G3）。
 */
@Data
public class ProjectFileIdsVO {

    /** 要删的文件记录 id 列表 */
    private List<String> fileIds;
}
