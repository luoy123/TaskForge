package com.zhq.taskforge.framework.web.domain.server;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;

/**
 * 物理内存。字段存字节，getter 换算成 GB。
 */
@Data
public class Mem {

    private static final double GB = 1024 * 1024 * 1024;

    /** 总内存（字节） */
    private double total;

    /** 已用（字节） */
    private double used;

    /** 可用（字节） */
    private double free;

    public double getTotal() {
        return NumberUtil.div(total, GB, 2);
    }

    public double getUsed() {
        return NumberUtil.div(used, GB, 2);
    }

    public double getFree() {
        return NumberUtil.div(free, GB, 2);
    }

    /** 使用率 % */
    public double getUsage() {
        if (total <= 0) {
            return 0;
        }
        return NumberUtil.round(used / total * 100, 2).doubleValue();
    }
}
