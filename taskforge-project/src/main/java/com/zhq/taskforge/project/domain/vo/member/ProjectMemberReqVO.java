package com.zhq.taskforge.project.domain.vo.member;

import lombok.Data;

@Data
public class ProjectMemberReqVO {

    private String projectId;
    /** nickname or email */
    private String keyword;
    private Long pageNum;
    private Long pageSize;
}
