package com.zhq.taskforge.quartz.service;

import org.quartz.SchedulerException;

import com.zhq.taskforge.quartz.domain.SysJob;

public interface ISysJobService {
    /** 立即触发一次（验收用） */
    boolean run(SysJob job) throws SchedulerException;

}
