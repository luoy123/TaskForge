package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysLogininfor;
import com.zhq.taskforge.system.mapper.SysLogininforMapper;
import com.zhq.taskforge.system.service.ISysLogininforService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysLogininforServiceImpl implements ISysLogininforService {
    @Autowired
    private SysLogininforMapper logininforMapper;

    @Override
    public void insertLogininfor(SysLogininfor logininfor) {
        logininforMapper.insertLogininfor(logininfor);
    }

    @Override
    public Page<SysLogininfor> selectLogininforList(Long pageNum, Long pageSize, SysLogininfor logininfor) {
        Page<SysLogininfor> page = new Page<>(pageNum, pageSize);
        return (Page<SysLogininfor>) logininforMapper.selectLogininforList(page, logininfor);
    }

    @Override
    public int deleteLogininforByIds(Long[] infoIds) {
        return logininforMapper.deleteLogininforByIds(infoIds);
    }

    @Override
    public SysLogininfor selectLogininforById(Long infoId) {
        return logininforMapper.selectLogininforById(infoId);
    }

    @Override
    public void cleanLogininfor() {
        logininforMapper.cleanLogininfor();
    }
}
