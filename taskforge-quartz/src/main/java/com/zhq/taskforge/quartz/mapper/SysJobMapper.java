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
}
