package com.zhq.taskforge.quartz.service.impl;

import java.util.List;

import org.quartz.JobDataMap;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.zhq.taskforge.common.constants.ScheduleConstants;
import com.zhq.taskforge.quartz.domain.SysJob;
import com.zhq.taskforge.quartz.mapper.SysJobMapper;
import com.zhq.taskforge.quartz.service.ISysJobService;
import com.zhq.taskforge.quartz.util.ScheduleUtils;

import jakarta.annotation.PostConstruct;

@Service
public class SysJobServiceImpl implements ISysJobService {

    private static final Logger log = LoggerFactory.getLogger(SysJobServiceImpl.class);

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SysJobMapper sysJobMapper;

    /**
     * 启动加载：单条失败只打日志，不拖垮整个应用。
     */
    @PostConstruct
    public void init() throws SchedulerException {
        scheduler.clear();
        List<SysJob> jobList = sysJobMapper.selectJobAll();
        for (SysJob job : jobList) {
            try {
                if (!StringUtils.hasText(job.getCronExpression())) {
                    log.warn("跳过定时任务：cron 为空, jobId={}, target={}",
                            job.getJobId(), job.getInvokeTarget());
                    continue;
                }
                ScheduleUtils.createScheduleJob(scheduler, job);
            } catch (Exception e) {
                log.error("加载定时任务失败, jobId={}, cron={}, target={}",
                        job.getJobId(), job.getCronExpression(), job.getInvokeTarget(), e);
            }
        }
    }

    @Override
    public boolean run(SysJob job) throws SchedulerException {
        SysJob selectJobById = sysJobMapper.selectJobById(job.getJobId());
        if (selectJobById == null) {
            return false;
        }
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put(ScheduleConstants.TASK_PROPERTIES, selectJobById);

        JobKey jobKey = ScheduleUtils.getJobKey(selectJobById.getJobId(), selectJobById.getJobGroup());
        if (!scheduler.checkExists(jobKey)) {
            return false;
        }
        // 异步触发：返回 true 不代表 scanOverdue 等业务已执行完
        scheduler.triggerJob(jobKey, jobDataMap);
        return true;
    }
}
