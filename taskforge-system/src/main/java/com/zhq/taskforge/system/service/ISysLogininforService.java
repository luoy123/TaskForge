package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysLogininfor;

public interface ISysLogininforService {

    public void insertLogininfor(SysLogininfor logininfor);

    public Page<SysLogininfor> selectLogininforList(Long pageNum, Long pageSize, SysLogininfor logininfor);

    public int deleteLogininforByIds(Long[] infoIds);

    public SysLogininfor selectLogininforById(Long infoId);

    public void cleanLogininfor();
}
