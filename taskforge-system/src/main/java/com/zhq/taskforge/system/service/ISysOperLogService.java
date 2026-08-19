package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysOperLog;

public interface ISysOperLogService {

    public void insertOperlog(SysOperLog operLog);

    public Page<SysOperLog> selectOperLogList(Long pageNum, Long pageSize, SysOperLog operLog);

    public int deleteOperLogByIds(Long[] operIds);

    public SysOperLog selectOperLogById(Long operId);

    public void cleanOperLog();
}
