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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.constants.ScheduleConstants;
import com.zhq.taskforge.quartz.domain.SysJob;
import com.zhq.taskforge.quartz.mapper.SysJobMapper;
import com.zhq.taskforge.quartz.service.ISysJobService;
import com.zhq.taskforge.common.exception.job.TaskException;
import com.zhq.taskforge.quartz.util.CronUtils;
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

    @Override
    public List<SysJob> selectJobList(SysJob job) {
        return sysJobMapper.selectJobList(job);
    }

    @Override
    public SysJob selectJobById(Long jobId) {
        return sysJobMapper.selectJobById(jobId);
    }

    @Override
    public int pauseJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus((ScheduleConstants.Status.PAUSE.getValue()));
        int rows = sysJobMapper.updateJob(job);
        if (rows > 0) {
            scheduler.pauseJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    @Override
    public int resumeJob(SysJob job) throws SchedulerException {
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        job.setStatus((ScheduleConstants.Status.NORMAL.getValue()));
        int rows = sysJobMapper.updateJob(job);
        if (rows > 0) {
            scheduler.resumeJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    @Override
    public int deleteJob(SysJob job) throws SchedulerException {
        // 删除任务，同时对应的trigger也要进行删除
        Long jobId = job.getJobId();
        String jobGroup = job.getJobGroup();
        int rows = sysJobMapper.deleteJobById(jobId);
        if (rows > 0) {
            scheduler.deleteJob(ScheduleUtils.getJobKey(jobId, jobGroup));
        }
        return rows;
    }

    @Override
    public void deleteJobByIds(Long[] jobIds) throws SchedulerException {
        for (Long id : jobIds) {
            SysJob db = sysJobMapper.selectJobById(id);
            if (db != null) {
                deleteJob(db);
            }
        }
    }

    @Override
    public int changeStatus(SysJob job) throws SchedulerException {
        int rows = 0;
        String status = job.getStatus();
        if (ScheduleConstants.Status.NORMAL.getValue().equals(status)) {
            rows = resumeJob(job);
        } else if (ScheduleConstants.Status.PAUSE.getValue().equals(status)) {
            rows = pauseJob(job);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertJob(SysJob job) throws SchedulerException, TaskException {
        job.setStatus(ScheduleConstants.Status.PAUSE.getValue());
        int rows = sysJobMapper.insertJob(job);
        if (rows > 0) {
            ScheduleUtils.createScheduleJob(scheduler, job);
        }
        return rows;
    }

    @Override
    public int updateJob(SysJob job) throws SchedulerException, TaskException {
        SysJob properties = sysJobMapper.selectJobById(job.getJobId());
        int rows = sysJobMapper.updateJob(job);
        if (rows > 0) {
            // 修改quartz的存储，要用的是旧的任务组来进行拼接，否则旧任务还是继续按照计划执行。
            updateSchedulerJob(job, properties.getJobGroup());
        }
        return rows;
    }

    private void updateSchedulerJob(SysJob job, String jobGroup) throws SchedulerException, TaskException {
        Long jobId = job.getJobId();
        // 判断是否存在
        JobKey jobKey = ScheduleUtils.getJobKey(jobId, jobGroup);
        if (scheduler.checkExists(jobKey)) {
            // 存在则删除原本的
            scheduler.deleteJob(jobKey);
        }
        ScheduleUtils.createScheduleJob(scheduler, job);
    }

    @Override
    public boolean checkCronExpressionIsValid(String cronExpression) {
        return CronUtils.isValid(cronExpression);
    }
}
