package com.zhq.taskforge.system.service;

import com.zhq.taskforge.system.domain.SysOperLog;

import java.util.List;

public interface ISysOperLogService {

    public void insertOperlog(SysOperLog operLog);

    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    public int deleteOperLogByIds(Long[] operIds);

    public SysOperLog selectOperLogById(Long operId);

    public void cleanOperLog();
}
