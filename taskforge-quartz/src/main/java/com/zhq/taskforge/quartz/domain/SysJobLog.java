package com.zhq.taskforge.quartz.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.zhq.taskforge.common.annotation.Excel;
import com.zhq.taskforge.common.core.domain.BaseEntity;

import lombok.Data;

/**
 * 定时任务调度日志 sys_job_log
 * <p>
 * startTime / stopTime 仅内存使用（算耗时写进 jobMessage），表中无对应列。
 */
@Data
@TableName("sys_job_log")
public class SysJobLog extends BaseEntity
        implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "job_log_id", type = IdType.AUTO)
    @Excel(name = "日志序号")
    private Long jobLogId;

    @Excel(name = "任务名称")
    private String jobName;

    @Excel(name = "任务组名")
    private String jobGroup;

    @Excel(name = "调用目标字符串")
    private String invokeTarget;

    @Excel(name = "日志信息")
    private String jobMessage;

    /** 0正常 1失败 */
    @Excel(name = "执行状态", readConverterExp = "0=正常,1=失败")
    private String status;

    @Excel(name = "异常信息")
    private String exceptionInfo;

    private Date startTime;
    private Date stopTime;

}
