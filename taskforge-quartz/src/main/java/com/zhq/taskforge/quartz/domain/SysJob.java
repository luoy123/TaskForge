package com.zhq.taskforge.quartz.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zhq.taskforge.common.annotation.Excel;
import com.zhq.taskforge.common.annotation.Excel.ColumnType;
import com.zhq.taskforge.common.core.domain.BaseEntity;

import lombok.Data;

@Data
@TableName("sys_job")
public class SysJob extends BaseEntity
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "job_id", type = IdType.AUTO)
    // 单元格的类型是数字类型，从而可以进行求和等数值计算
    @Excel(name = "任务序号", cellType = ColumnType.NUMERIC)
    private Long jobId;

    @Excel(name = "任务名称")
    private String jobName;
    @Excel(name = "任务分组")
    /** 任务组，默认 DEFAULT */
    private String jobGroup;
    @Excel(name = "调用目标字符串")
    /** 调用目标，例：taskOverdueTask.scanOverdue() */
    private String invokeTarget;
    @Excel(name = "执行表达式")
    private String cronExpression;
    @Excel(name = "计划策略", readConverterExp = "0=默认,1=立即触发执行,2=触发一次执行,3=不触发立即执行")
    /** 见 ScheduleConstants misfire 说明；1立即执行 2执行一次 3放弃执行 */
    private String misfirePolicy;
    @Excel(name = "并发执行", readConverterExp = "0=允许并发 1=禁止")
    /** 0允许并发 1禁止；I5a 建议一律当「禁止」处理 */
    private String concurrent;
    /** 0正常 1暂停 */
    @Excel(name = "任务状态", readConverterExp = "0=正常,1=暂停")
    private String status;
}
