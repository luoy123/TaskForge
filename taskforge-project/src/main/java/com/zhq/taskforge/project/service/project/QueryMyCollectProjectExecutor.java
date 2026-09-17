package com.zhq.taskforge.project.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;
import com.zhq.taskforge.project.mapper.ProjectMapper;

/**
 * 当前用户收藏的项目
 */
@Service("queryMyCollectProjectExecutor")
public class QueryMyCollectProjectExecutor extends QueryAbstractExecutor {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public IPage<ProjectResVO> query(Page<ProjectResVO> page, ProjectReqVO req) {
        return projectMapper.selectMyCollectProjectList(page, SecurityUtils.getUserId(), req);
    }

}
