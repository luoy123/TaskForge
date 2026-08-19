package com.zhq.taskforge.system.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysOperLog;
import org.apache.ibatis.annotations.Param;

public interface SysOperLogMapper {

    public void insertOperlog(SysOperLog operLog);

    public IPage<SysOperLog> selectOperLogList(Page<SysOperLog> page, @Param("operLog") SysOperLog operLog);

    public int deleteOperLogByIds(Long[] operIds);

    public SysOperLog selectOperLogById(Long operId);

    public void cleanOperLog();
}
