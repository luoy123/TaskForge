package com.zhq.taskforge.common.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class RedisCache {

    @Autowired
    public RedisTemplate<String,Object> redisTemplate;

    //设置缓存
    public<T> void setCacheObject(final String key, final T value) {
        redisTemplate.opsForValue().set(key, value);
    }

    //删除缓存
    public boolean deleteCacheObject(final String key) {
        return redisTemplate.delete(key);
    }

    //获取缓存
    public<T> T getCacheObject(final String key) {
        return (T)redisTemplate.opsForValue().get(key);
    }

    //批量删除
    public boolean deleteAllCacheObject(final Collection collection) {
        Long count = redisTemplate.delete(collection);
        return count !=null && count > 0;
    }

    //获取匹配的key
    public Collection<String> keys(final String pattern) {
        return redisTemplate.keys(pattern);
    }

}
