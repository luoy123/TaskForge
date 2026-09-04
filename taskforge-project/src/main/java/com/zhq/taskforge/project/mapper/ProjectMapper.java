package com.zhq.taskforge.project.mapper;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;

public interface ProjectMapper extends BaseMapper<Project> {

    /**
     * 项目详情（join 当前阶段 + 负责人昵称）。
     */
    ProjectResVO detail(@Param("projectId") String projectId);

    /**
     * 项目列表(查询我是成员的项目)
     */
    IPage<ProjectResVO> selectMyProjectList(Page<ProjectResVO> page,
        @Param("userId") Long userId,
         @Param("req") ProjectReqVO req);

    /**
     * 软删（显式写 deleted，绕过 @TableLogic 对 updateById 的字段屏蔽）。
     */
    int softDelete(@Param("id") String id,
                   @Param("deletedTime") java.time.LocalDateTime deletedTime,
                   @Param("updatedBy") String updatedBy,
                   @Param("updatedTime") java.time.LocalDateTime updatedTime);
}
