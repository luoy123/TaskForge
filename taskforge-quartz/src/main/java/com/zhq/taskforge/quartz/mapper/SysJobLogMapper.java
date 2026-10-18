package com.zhq.taskforge.quartz.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.zhq.taskforge.quartz.domain.SysJobLog;

@Mapper
public interface SysJobLogMapper {

    /**
     * 获取调度器日志的执行计划，带查询条件
     */
    public List<SysJobLog> selectJobLogList(SysJobLog sysJobLog);

    /**
     * 查询所有的日志的执行计划
     */
    public List<SysJobLog> selectJobLogAll();

    /**
     * 根据日志Id查询日志信息
     * 
     * @param sysJobLog
     * @return
     */
    public SysJobLog selectJobLogById(Long jobLogId);

    /**
     * 新增日志
     */
    public int insertJobLog(SysJobLog sysJobLog);

    /**
     * 根据id删除单条
     */
    public int deleteJobLogById(Long jobLogId);

    /**
     * 根据id列表删除多条
     */
    public int deleteJobLogByIds(Long[] ids);

    /**
     * 清空任务日志
     */
    public void cleanJobLog();

}
