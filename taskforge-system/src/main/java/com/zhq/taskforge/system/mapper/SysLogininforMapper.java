package com.zhq.taskforge.system.mapper;

import com.zhq.taskforge.system.domain.SysLogininfor;

import java.util.List;

public interface SysLogininforMapper {

    public void insertLogininfor(SysLogininfor logininfor);

    public List<SysLogininfor> selectLogininforList(SysLogininfor logininfor);

    public int deleteLogininforByIds(Long[] infoIds);

    public SysLogininfor selectLogininforById(Long infoId);

    public void cleanLogininfor();
}
