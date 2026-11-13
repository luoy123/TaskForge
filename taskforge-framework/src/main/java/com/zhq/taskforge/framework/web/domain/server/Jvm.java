package com.zhq.taskforge.framework.web.domain.server;

import java.lang.management.ManagementFactory;
import java.util.Date;

import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import lombok.Data;

/**
 * JVM 信息。字段存字节，getter 换算成 MB。
 */
@Data
public class Jvm {

    private static final double MB = 1024 * 1024;

    /** JVM 当前向操作系统申请到的内存（字节） */
    private double total;

    /** JVM 最多能申请的内存，即 -Xmx（字节） */
    private double max;

    /** total 里还没用的部分（字节） */
    private double free;

    /** java的版本 */
    private String version;

    /** java的安装路径 */
    private String home;

    public double getTotal() {
        return NumberUtil.div(total, MB, 2);
    }

    public double getMax() {
        return NumberUtil.div(max, MB, 2);
    }

    public double getFree() {
        return NumberUtil.div(free, MB, 2);
    }

    public double getUsed() {
        return NumberUtil.div(total - free, MB, 2);
    }

    /** 使用率 %（相对 total，不是相对 max） */
    public double getUsage() {
        if (total <= 0) {
            return 0;
        }
        return NumberUtil.round((total - free) / total * 100, 2).doubleValue();
    }

    public String getName() {
        return ManagementFactory.getRuntimeMXBean().getVmName();
    }

    public String getStartTime() {
        return DateUtil.formatDateTime(new Date(ManagementFactory.getRuntimeMXBean().getStartTime()));
    }

    public String getRunTime() {
        return DateUtil.formatBetween(ManagementFactory.getRuntimeMXBean().getUptime(), BetweenFormatter.Level.MINUTE);
    }

    public String getInputArgs() {
        return ManagementFactory.getRuntimeMXBean().getInputArguments().toString();
    }
}
