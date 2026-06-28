package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.common.redis.RedisCache;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysConfig;
import com.zhq.taskforge.model.system.mapper.SysConfigMapper;
import com.zhq.taskforge.model.system.service.SysConfigService;
import com.zhq.taskforge.security.SecurityUtils;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.List;

@Service
public class SysConfigServiceImpl implements SysConfigService {

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存里面
     */
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
        //1.先从缓存中获取
        String value = (String)redisCache.getCacheObject(getCachekey(configKey));
        if(!StringUtils.isEmpty(value)){
            return value;
        }
        //2.缓存中不存在
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
        // 1. 校验参数键名唯一
        if (UserConstants.NOT_UNIQUE.equals(checkConfigKeyUnique(sysConfig))) {
            throw new BusinessException("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        sysConfig.setCreateBy(SecurityUtils.getUserName());
        int result = sysConfigMapper.insert(sysConfig);
        if(result > 0){
            //放入缓存中
            redisCache.setCacheObject(getCachekey(sysConfig.getConfigKey()), sysConfig.getConfigValue());
        }
        return result;
    }

    @Override
    public int updateSysConfig(SysConfig sysConfig) {
        // 1. 校验参数键名唯一
        if (UserConstants.NOT_UNIQUE.equals(checkConfigKeyUnique(sysConfig))) {
            throw new BusinessException("新增参数'" + sysConfig.getConfigName() + "'失败，参数键名已存在");
        }
        sysConfig.setUpdateBy(SecurityUtils.getUserName());
        int result = sysConfigMapper.updateById(sysConfig);
        if(result > 0){
            //2.更新缓存
            redisCache.setCacheObject(getCachekey(sysConfig.getConfigKey()), sysConfig.getConfigValue());
        }
        return result;
    }

    @Override
    public int deleteSysConfigByIds(List<Long> ids) {
        //1.删除前需要检查是否需要内置参数
        for(Long configId : ids){
            SysConfig sysConfig = sysConfigMapper.selectById(configId);
            if(sysConfig != null && "Y".equals(sysConfig.getConfigType())){
                throw new RuntimeException("内置参数" + sysConfig.getConfigKey() + "无法被删除");
            }
        }
        int result = sysConfigMapper.deleteBatchIds(ids);
        if(result > 0){
            //2.删除缓存
            for(Long configId : ids){
                SysConfig sysConfig = sysConfigMapper.selectById(configId);
                if(sysConfig != null ){
                    redisCache.deleteCacheObject(getCachekey(sysConfig.getConfigKey()));
                }
            }
        }
        return result;
    }

    /**
     * 加载缓存
     */
    public void loadingConfigCache(){
        LambdaQueryWrapper<SysConfig> qw = new LambdaQueryWrapper<>();
        List<SysConfig> configList = sysConfigMapper.selectList(qw);
        for(SysConfig sysConfig : configList){
            redisCache.setCacheObject(getCachekey(sysConfig.getConfigKey()), sysConfig.getConfigValue());
        }
    }

    /**
     * 获取key
     * @param configKey
     * @return
     */
    public String getCachekey(String configKey){
        return CacheConstants.SYS_CONFIG_KEY  + configKey;
    }

    /**
     * 清空参数缓存
     */
    public void clearConfigCache(){
        Collection<String> keys = redisCache.keys(getCachekey("*"));
        if(keys != null && !keys.isEmpty()){
            redisCache.deleteAllCacheObject(keys);
        }
    }


    /**
     * 重置缓存
     */
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
