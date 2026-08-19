package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysOperLog;
import com.zhq.taskforge.system.mapper.SysOperLogMapper;
import com.zhq.taskforge.system.service.ISysOperLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysOperLogServiceImpl implements ISysOperLogService {
    @Autowired
    private SysOperLogMapper operLogMapper;

    @Override
    public void insertOperlog(SysOperLog operLog) {
        operLogMapper.insertOperlog(operLog);
    }

    @Override
    public Page<SysOperLog> selectOperLogList(Long pageNum, Long pageSize, SysOperLog operLog) {
        Page<SysOperLog> page = new Page<>(pageNum, pageSize);
        return (Page<SysOperLog>) operLogMapper.selectOperLogList(page, operLog);
    }

    @Override
    public int deleteOperLogByIds(Long[] operIds) {
        return operLogMapper.deleteOperLogByIds(operIds);
    }

    @Override
    public SysOperLog selectOperLogById(Long operId) {
        return operLogMapper.selectOperLogById(operId);
    }

    @Override
    public void cleanOperLog() {
        operLogMapper.cleanOperLog();
    }
}
