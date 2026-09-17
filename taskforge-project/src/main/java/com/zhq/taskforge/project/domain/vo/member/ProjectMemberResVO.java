package com.zhq.taskforge.project.domain.vo.member;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ProjectMemberResVO {
    private Long userId;
    private String nickName;
    private String userName;
    private String email;
    private String deptName;
    private String roleName;
    /** 是否为项目的创建者，1：是 */
    private Integer creator;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime joinedTime;
}
