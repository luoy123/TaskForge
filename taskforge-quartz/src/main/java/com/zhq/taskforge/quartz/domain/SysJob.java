package com.zhq.taskforge.quartz.domain;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("sys_job")
public class SysJob implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "job_id", type = IdType.AUTO)
    private Long jobId;

    private String jobName;

    /** 任务组，默认 DEFAULT */
    private String jobGroup;

    /** 调用目标，例：taskOverdueTask.scanOverdue() */
    private String invokeTarget;

    private String cronExpression;

    /** 见 ScheduleConstants misfire 说明；常用 3=放弃补跑 */
    private String misfirePolicy;

    /** 0允许并发 1禁止；I5a 建议一律当「禁止」处理 */
    private String concurrent;
    /** 0正常 1暂停 */
    private String status;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
    private String remark;
}
