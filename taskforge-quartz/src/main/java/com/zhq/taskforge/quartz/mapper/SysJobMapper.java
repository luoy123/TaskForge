package com.zhq.taskforge.quartz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.zhq.taskforge.quartz.domain.SysJob;

@Mapper
public interface SysJobMapper {

    /**
     * 启动时加载所有任务到Schedule
     */
    List<SysJob> selectJobAll();

    /**
     * 立刻执行按id取最新的配置
     */
    SysJob selectJobById(Long jobId);

    /**
     * 通过调度id删除调度任务信息
     */
    public int deleteJobById(Long jobId);

    /**
     * 通过id列表删除调度任务
     */
    public int deleteByIds(Long[] ids);

    /**
     * 新增调度信息
     */
    public int insertJob(SysJob job);

    /**
     * 修改调度信息
     */
    public int updateJob(SysJob job);

    /**
     * 查询调度任务的日志集合
     */
    public List<SysJob> selectJobList(SysJob job);

}
