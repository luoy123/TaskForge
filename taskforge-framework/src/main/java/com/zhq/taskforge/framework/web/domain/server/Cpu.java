package com.zhq.taskforge.framework.web.domain.server;

import cn.hutool.core.util.NumberUtil;
import lombok.Data;

/**
 * CPU 信息。字段存两次采样之间的 tick 差值，getter 换算成百分比返回给前端。
 */
@Data
public class Cpu {

    /** 逻辑核数 */
    private int cpuNum;

    /** 总 tick */
    private double total;

    /** 系统态 tick */
    private double sys;

    /** 用户态 tick */
    private double used;

    /** IO 等待 tick */
    private double wait;

    /** 空闲 tick */
    private double free;

    public double getSys() {
        return percent(sys);
    }

    public double getUsed() {
        return percent(used);
    }

    public double getWait() {
        return percent(wait);
    }

    public double getFree() {
        return percent(free);
    }

    /** total 为 0 时（采样间隔内没有 tick）返回 0，避免除零 */
    private double percent(double part) {
        if (total <= 0) {
            return 0;
        }
        return NumberUtil.round(part / total * 100, 2).doubleValue();
    }
}
