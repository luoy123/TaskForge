package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.core.redis.RedisCache;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.domain.SysConfig;
import com.zhq.taskforge.system.mapper.SysConfigMapper;
import com.zhq.taskforge.system.service.ISysConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Service
public class SysConfigServiceImpl implements ISysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private RedisCache redisCache;

    @PostConstruct
    public void init() {
        loadingConfigCache();
    }

    @Override
    public Page<SysConfig> selectSysConfigList(Long pageNum, Long pageSize, SysConfig sysConfig) {
        Page<SysConfig> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();

        if(!StringUtils.isEmpty(sysConfig.getConfigName())){
            qw.like(SysConfig::getConfigName, sysConfig.getConfigName());
        }
        if(!StringUtils.isEmpty(sysConfig.getConfigValue())){
            qw.like(SysConfig::getConfigValue, sysConfig.getConfigValue());
        }
        if(!StringUtils.isEmpty(sysConfig.getConfigType())){
            qw.eq(SysConfig::getConfigType, sysConfig.getConfigType());
        }
        qw.orderByAsc(SysConfig::getConfigId);

        return sysConfigMapper.selectPage(page, qw);
    }

    @Override
    public SysConfig selectSysConfigById(Long configId) {
        return sysConfigMapper.selectById(configId);
    }

    @Override
    public String selectSysConfigByKey(String configKey) {
        String value = (String)redisCache.getCacheObject(getCachekey(configKey));
        if(!StringUtils.isEmpty(value)){
            return value;
        }
        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(SysConfig::getConfigKey, configKey);
        SysConfig sysConfig = sysConfigMapper.selectOne(qw);
        if(sysConfig != null){
            redisCache.setCacheObject(getCachekey(configKey), sysConfig.getConfigValue());
        }
        return sysConfig == null ? null : sysConfig.getConfigValue();
    }

    @Override
    public int insertSysConfig(SysConfig sysConfig) {
        if (UserConstants.NOT_UNIQUE.equals(checkConfigKeyUnique(sysConfig))) {
            throw new ServiceException("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        sysConfig.setCreateBy(SecurityUtils.getUsername());
        int result = sysConfigMapper.insert(sysConfig);
        if(result > 0){
            redisCache.setCacheObject(getCachekey(sysConfig.getConfigKey()), sysConfig.getConfigValue());
        }
        return result;
    }

    @Override
    public int updateSysConfig(SysConfig sysConfig) {
        if (UserConstants.NOT_UNIQUE.equals(checkConfigKeyUnique(sysConfig))) {
            throw new ServiceException("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        sysConfig.setUpdateBy(SecurityUtils.getUsername());
        int result = sysConfigMapper.updateById(sysConfig);
        if(result > 0){
            redisCache.setCacheObject(getCachekey(sysConfig.getConfigKey()), sysConfig.getConfigValue());
        }
        return result;
    }

    @Override
    public int deleteSysConfigByIds(List<Long> ids) {
        for(Long configId : ids){
            SysConfig sysConfig = sysConfigMapper.selectById(configId);
            if(sysConfig != null && "Y".equals(sysConfig.getConfigType())){
                throw new RuntimeException("内置参数" + sysConfig.getConfigKey() + "无法被删除");
            }
        }
        int result = sysConfigMapper.deleteBatchIds(ids);
        if(result > 0){
            for(Long configId : ids){
                SysConfig sysConfig = sysConfigMapper.selectById(configId);
                if(sysConfig != null ){
                    redisCache.deleteCacheObject(getCachekey(sysConfig.getConfigKey()));
                }
            }
        }
        return result;
    }

    public void loadingConfigCache(){
        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();
        List<SysConfig> configList = sysConfigMapper.selectList(qw);
        for(SysConfig sysConfig : configList){
            redisCache.setCacheObject(getCachekey(sysConfig.getConfigKey()), sysConfig.getConfigValue());
        }
    }

    public String getCachekey(String configKey){
        return CacheConstants.SYS_CONFIG_KEY  + configKey;
    }

    public void clearConfigCache(){
        Collection<String> keys = redisCache.keys(getCachekey("*"));
        if(keys != null && !keys.isEmpty()){
            redisCache.deleteAllCacheObject(keys);
        }
    }

    public void resetConfigCache(){
        clearConfigCache();
        loadingConfigCache();
    }
    @Override
    public String checkConfigKeyUnique(SysConfig config) {
        Long configId = config.getConfigId() == null ? -1L : config.getConfigId();

        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();
        qw.eq(SysConfig::getConfigKey, config.getConfigKey());

        SysConfig info = sysConfigMapper.selectOne(qw);

        if (info != null && !info.getConfigId().equals(configId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
