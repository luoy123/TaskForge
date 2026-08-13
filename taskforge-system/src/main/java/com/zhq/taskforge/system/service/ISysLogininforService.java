package com.zhq.taskforge.system.service;

import com.zhq.taskforge.system.domain.SysLogininfor;

import java.util.List;

public interface ISysLogininforService {

    public void insertLogininfor(SysLogininfor logininfor);

    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    public int deleteLogininforByIds(Long[] infoIds);

    public SysLogininfor selectLogininforById(Long infoId);

    public void cleanLogininfor();
}
