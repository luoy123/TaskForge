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
 * 当前用户作为成员参与的项目
 * bean的名称必须和枚举类里面的保持一致
 * QueryMyProjectExecutor
 */
@Service("queryMyProjectExecutor")
public class QueryMyProjectExecutor extends QueryAbstractExecutor {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public IPage<ProjectResVO> query(Page<ProjectResVO> page, ProjectReqVO req) {
        return projectMapper.selectMyProjectList(page, SecurityUtils.getUserId(), req);
    }

}
