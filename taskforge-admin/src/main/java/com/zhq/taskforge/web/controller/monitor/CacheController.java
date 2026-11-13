package com.zhq.taskforge.web.controller.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.exception.ServiceException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
/**
 * 缓存监控，展示redis基本信息和业务前缀key的数量
 */
@RestController
@RequestMapping("/monitor/cache")
@Tag(name="缓存监控")
public class CacheController {

    @Autowired
    private RedisTemplate<String,Object> redisTemplate;

    //统计缓存的前缀映射，同时需要保证顺序性
    private static final LinkedHashMap<String,String> CACHE_KEY_PREFIX_MAP = new LinkedHashMap<>();

    static{
        //知道又多少人在线
        CACHE_KEY_PREFIX_MAP.put(CacheConstants.LOGIN_TOKEN_KEY, "登录令牌");
        //防止恶意刷接口更新
        CACHE_KEY_PREFIX_MAP.put(CacheConstants.CAPTCHA_CODE_KEY,"验证码");
        //接口限流计数器
        CACHE_KEY_PREFIX_MAP.put(CacheConstants.RATE_LIMIT_KEY,"接口限流计数器");
    }

    @Operation(summary = "获取缓存信息")
    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_CACHE_LIST + "')")
    public R<Map<String,Object>> getInfo() {

        //1.获取redis底层连接，执行info命令来获取服务器指标
       Properties info =  redisTemplate.execute((RedisCallback<Properties>) connection -> connection.serverCommands().info());

       //2.挑选redis中同名的info属性
       Map<String,Object> basic = new LinkedHashMap<String,Object>();
       if(info != null){
        basic.put("redis_version",info.getProperty("redis_version"));
        basic.put("used_memory_human",info.getProperty("used_memory_human"));
        basic.put("connected_clients",info.getProperty("connected_clients"));
        basic.put("uptime_in_days",info.getProperty("uptime_in_days"));
       }

       //3.执行dbsize命令来获取当前redis中的key的总数
       Long dbSize = redisTemplate.execute(
        (RedisCallback<Long>) connection -> connection.serverCommands().dbSize()
       );

       //4.统计三个前缀的数量
       List<Map<String,Object>> keyStats = new ArrayList<Map<String,Object>>();
       for(Map.Entry<String,String> entry : CACHE_KEY_PREFIX_MAP.entrySet()){
        String prefix = entry.getKey();
        String value = entry.getValue();

        //调用私有方法，来进行游标扫描统计
        long count = countByPrefix(prefix);

        LinkedHashMap<String,Object> item = new LinkedHashMap<>(3);
        item.put("prefix",prefix);
        item.put("name",value);
        item.put("count",count);
        keyStats.add(item);
    }
    //组装结果
    Map<String,Object> result = new LinkedHashMap<>(3);
    result.put("info",basic);
    result.put("dbSize",dbSize);
    result.put("keyStats",keyStats);
    return R.ok(result);    
    }
    
    //统计指定前缀的数量
    private long countByPrefix(String prefix){
        //1.构造扫描参数
        /** 匹配一prefix开头的key,每次扫描100个key
         * 采用Scan主要是未来解决keys *的问题。
         */
        ScanOptions scanOptions = ScanOptions.scanOptions().match(prefix + "*").count(100).build();
        
        //2.执行扫描
        /** 使用try-with-resources语句来确保游标被自动关闭*/
        try(Cursor<String> cursor = redisTemplate.scan(scanOptions)){
            //3.统计数量
            long count = 0;
            while(cursor.hasNext()){
                cursor.next();
                count++;
            }
            return count;
        }
    
    }


    @DeleteMapping ("/clear")
    @Operation(summary = "按前缀清空缓存")
    @PreAuthorize ("hasAnyAuthority('" + PermissionConstants.MONITOR_CACHE_REMOVE + "')")
    @Log(title = "缓存监控",businessType = BusinessType.CLEAN)
    //我们这里采用的是requestParam来接收前缀，主要是我们的前缀携带了冒号，如果直接用路径参数的方式在解析的时候会出错。
    public R<Long> clear(@RequestParam String prefix){
        //1.检查前缀，防止通配符注入。
        //用containsKey,必须完全相同才匹配，不用startsWith
        if(!CACHE_KEY_PREFIX_MAP.containsKey(prefix)){
            throw new ServiceException("不允许清楚改前缀：" + prefix);
        }
        //2.清空缓存，并返回删除的数量
        Long count = clearByPrefix(prefix);
        return R.ok(count);
        
    }

    private Long clearByPrefix(String prefix){
        //1.构造参数
        ScanOptions scanOptions = ScanOptions.scanOptions().match(prefix + "*").count(100).build();
        long delete = 0;
        List<String> batch = new ArrayList<>(100);

        //2.执行扫描
        try(Cursor<String> cursor = redisTemplate.scan(scanOptions)){
            while(cursor.hasNext()){
                //cursor.next()返回的是一个完整的key
                batch.add(cursor.next());
                if(batch.size() >= 100){
                    Long count = redisTemplate.delete(batch);
                    if(count  != null){
                        delete += count;
                    }
                    //清空槽位
                    batch.clear();
                }
            }
        }
            //3.处理剩余的key
            if(!batch.isEmpty()){
                Long count = redisTemplate.delete(batch);
                if(count != null){
                    delete += count;
                }
            }
            //4.返回删除的总数
            return delete;
            }




}
