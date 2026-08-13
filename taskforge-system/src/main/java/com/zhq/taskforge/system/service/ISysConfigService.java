package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysConfig;

import java.util.List;

public interface ISysConfigService {

    Page<SysConfig> selectSysConfigList(Long pageNum,Long pageSize,SysConfig sysConfig);

    SysConfig selectSysConfigById(Long configId);

    String selectSysConfigByKey(String configkey);

    int insertSysConfig(SysConfig sysConfig);

    int updateSysConfig(SysConfig sysConfig);

    int deleteSysConfigByIds(List<Long> ids);

    String checkConfigKeyUnique(SysConfig sysConfig);

    void resetConfigCache();
}
