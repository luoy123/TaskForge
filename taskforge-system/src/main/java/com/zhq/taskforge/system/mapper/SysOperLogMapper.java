package com.zhq.taskforge.system.mapper;

import com.zhq.taskforge.system.domain.SysOperLog;

import java.util.List;

public interface SysOperLogMapper {

    public void insertOperlog(SysOperLog operLog);

    public List<SysOperLog> selectOperLogList(SysOperLog operLog);

    public int deleteOperLogByIds(Long[] operIds);

    public SysOperLog selectOperLogById(Long operId);

    public void cleanOperLog();
}
