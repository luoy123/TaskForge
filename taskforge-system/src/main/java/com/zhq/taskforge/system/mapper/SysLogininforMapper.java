package com.zhq.taskforge.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysLogininfor;
import org.apache.ibatis.annotations.Param;

public interface SysLogininforMapper {

    public void insertLogininfor(SysLogininfor logininfor);

    public IPage<SysLogininfor> selectLogininforList(Page<SysLogininfor> page, @Param("logininfor") SysLogininfor logininfor);

    public int deleteLogininforByIds(Long[] infoIds);

    public SysLogininfor selectLogininforById(Long infoId);

    public void cleanLogininfor();
}
