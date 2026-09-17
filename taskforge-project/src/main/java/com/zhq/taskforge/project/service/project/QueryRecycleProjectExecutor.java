package com.zhq.taskforge.project.service.project;

import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;
import com.zhq.taskforge.project.mapper.ProjectMapper;

/**
 * 回收站：type = recycle（软删 deleted=1）。
 * 暂不按用户过滤，与 Mapper 签名一致。
 */
@Service("queryMyRecycleProjectExecutor")
public class QueryRecycleProjectExecutor extends QueryAbstractExecutor {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public IPage<ProjectResVO> query(Page<ProjectResVO> page, ProjectReqVO req) {
        return projectMapper.selectRecycleProjectList(page, req);
    }

}
