package com.zhq.taskforge.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.project.domain.vo.ProjectVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberReqVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberResVO;

public interface IProjectMemberService {

    IPage<ProjectMemberResVO> list(ProjectMemberReqVO req);

    void add(ProjectVO vo);

    void remove(ProjectVO vo);

}
