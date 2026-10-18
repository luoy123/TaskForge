package com.zhq.taskforge.quartz.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zhq.taskforge.quartz.domain.SysJobLog;
import com.zhq.taskforge.quartz.mapper.SysJobLogMapper;
import com.zhq.taskforge.quartz.service.ISysJobLogService;

@Service
public class SysJobLogServiceImpl implements ISysJobLogService {

    @Autowired
    private SysJobLogMapper jobLogMapper;

    @Override
    public List<SysJobLog> selectJobLogList(SysJobLog jobLog) {
        return jobLogMapper.selectJobLogList(jobLog);
    }

    @Override
    public SysJobLog selectJobLogById(Long jobLogId) {
        return jobLogMapper.selectJobLogById(jobLogId);
    }

    @Override
    public void addJobLog(SysJobLog jobLog) {
        jobLogMapper.insertJobLog(jobLog);
    }

    @Override
    public int deleteJobLogByIds(Long[] logIds) {
        return jobLogMapper.deleteJobLogByIds(logIds);
    }

    @Override
    public int deleteJobLogById(Long jobLogId) {
        return jobLogMapper.deleteJobLogById(jobLogId);
    }

    @Override
    public void cleanJobLog() {
        jobLogMapper.cleanJobLog();
    }
}
