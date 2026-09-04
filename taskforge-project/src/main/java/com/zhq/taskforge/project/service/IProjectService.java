package com.zhq.taskforge.project.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.project.domain.Project;
import com.zhq.taskforge.project.domain.vo.ProjectReqVO;
import com.zhq.taskforge.project.domain.vo.ProjectResVO;

public interface IProjectService {

    /**
     * 创建项目（含阶段 / 创建者成员 / 日志副作用）。
     */
    void saveProject(Project project);

    /**
     * 项目详情；私有项目非成员应拒绝。
     */
    ProjectResVO detail(String projectId);

    /**
     * 项目列表
     */
    IPage<ProjectResVO> list(ProjectReqVO req);

    /**
     * 编辑项目
     */
    void edit(Project project);

    /**
     * 删除项目
     */
    void delete(String projectId);

    /**
     * 归档项目
     */
    void archived(String projectId);

    /**
     * 取消归档项目
     */
    void unarchived(String projectId);

    /**
     * 退出：当前用户从成员表中删除调子，但是创建人是无法退出。
     */
    void quit(String projectId);

    /**
     * 收藏项目
     */
    void collect(String projectId);

    /**
     * 取消收藏
     */
    void uncollect(String projectId);


}
