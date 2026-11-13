package com.zhq.taskforge.web.controller.monitor;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.framework.web.domain.Server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 服务监控：CPU / 内存 / JVM / 磁盘
 */
@RestController
@RequestMapping("/monitor/server")
@Tag(name = "服务监控")
public class ServerController {

    @Autowired 
    private DataSource dataSource;

    /** CPU 要隔 1 秒采样两次，所以这个接口至少耗时 1 秒 */
    @GetMapping
    @Operation(summary = "服务器信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_SERVER_LIST + "')")
    public R<Server> getInfo() {
        Server server = new Server();
        server.copyTo();
        return R.ok(server);
    }

    @GetMapping("/pool")
    @Operation(summary = "数据库连接池信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_SERVER_LIST + "')")
    public R<Map<String,Object>> getPoolInfo(){
        HikariDataSource hikariDataSource;
        if(dataSource instanceof HikariDataSource){
          hikariDataSource = (HikariDataSource) dataSource;
        }else{
            throw new ServiceException("当前数据源不是HiKari,无法读取连接池指标");
        }

        //2.获取MaxBean
        HikariPoolMXBean pool = hikariDataSource.getHikariPoolMXBean();
        if(pool == null){
            throw new ServiceException("当前连接池还没有初始化成功,请稍后重试");
        }
        LinkedHashMap<String,Object> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("active",pool.getActiveConnections()); // 活跃连接数
        linkedHashMap.put("idle",pool.getIdleConnections()); // 空闲连接数
        linkedHashMap.put("total",pool.getTotalConnections()); // 总连接数
        linkedHashMap.put("waiting",pool.getThreadsAwaitingConnection()); // 等待连接数
        linkedHashMap.put("max",hikariDataSource.getMaximumPoolSize()); // 最大连接数
        return R.ok(linkedHashMap);
    }
}
