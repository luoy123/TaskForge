package com.zhq.taskforge.project.service.project;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;

/**
 * 项目列表查询执行器基类
 * 子类负责查询某一种类型，分页参数从外面传入
 * QueryAbstractExecutor
 */
public abstract class QueryAbstractExecutor {

    public abstract IPage<ProjectResVO> query(Page<ProjectResVO> page, ProjectReqVO req);

}
