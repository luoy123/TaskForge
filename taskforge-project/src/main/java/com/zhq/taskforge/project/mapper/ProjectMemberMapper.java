package com.zhq.taskforge.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.project.domain.ProjectMember;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberReqVO;
import com.zhq.taskforge.project.domain.vo.member.ProjectMemberResVO;

import org.apache.ibatis.annotations.Param;

public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {

    /**
     * 项目成员分页
     * 
     * @param page
     * @param req
     * @return
     */
    IPage<ProjectMemberResVO> selectMemberPage(Page<ProjectMemberResVO> page,
            @Param("req") ProjectMemberReqVO req);
}
