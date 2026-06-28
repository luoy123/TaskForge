package com.zhq.taskforge.model.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.model.system.entity.SysConfig;

import java.util.List;

public interface SysConfigService {

    /**
     * 分页查询
     */
    Page<SysConfig> selectSysConfigList(Long pageNum,Long pageSize,SysConfig sysConfig);

    /**
     * 根据id查询
     */
    SysConfig selectSysConfigById(Long configId);

    /**
     * 根据key来查询config_value
     */
    String selectSysConfigByKey(String configkey);

    /**
     * 新增参数
     */
    int insertSysConfig(SysConfig sysConfig);


    /**
     * 修改
     */
    int updateSysConfig(SysConfig sysConfig);

    /**
     * 删除
     */
    int deleteSysConfigByIds(List<Long> ids);

    /**
     * 校验参数键名是否唯一
     */
    String checkConfigKeyUnique(SysConfig sysConfig);

    /**
     * 刷新缓存参数
     */
    void resetConfigCache();
}
