package com.zhq.taskforge.project.service;

import java.util.List;

import com.zhq.taskforge.project.domain.vo.ProjectStageVO;

public interface IProjectStageService {

    /**
     * 获取项目阶段列表
     */
    List<ProjectStageVO> list(String projectId);

    /**
     * 添加阶段
     */
    void add(ProjectStageVO projectStageVO);

    /**
     * 修改阶段
     */
    void edit(ProjectStageVO projectStageVO);

    /**
     * 删除阶段
     */
    void delete(String stageId);


    void setCurrent(String stageId,String projectId);

}
