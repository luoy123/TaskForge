package com.zhq.taskforge.quartz.util;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import com.zhq.taskforge.common.constants.ScheduleConstants;
import com.zhq.taskforge.quartz.domain.SysJob;

public class ScheduleUtils {

    // 调度规则的定义
    public static TriggerKey getTriggerKey(Long jobId, String jobGroup) {
        return TriggerKey.triggerKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    // quartz中用jobkey来保证任务的唯一性，任务的定义
    public static JobKey getJobKey(Long jobId, String jobGroup) {
        return JobKey.jobKey(ScheduleConstants.TASK_CLASS_NAME + jobId, jobGroup);
    }

    // 添加任务
    public static void createScheduleJob(Scheduler scheduler, SysJob sysJob) throws SchedulerException {
        // 1.获取id
        Long jobId = sysJob.getJobId();
        String jobGroup = sysJob.getJobGroup();

        // 2.创建JobDetail对象
        JobDetail jobDetail = JobBuilder.newJob(QuartzDisallowConcurrentExecution.class)
                .withIdentity(getJobKey(jobId, jobGroup))
                .build();
        // 3.Cron对象
        CronScheduleBuilder cronBuilder = CronScheduleBuilder.cronSchedule(sysJob.getCronExpression())
                .withMisfireHandlingInstructionDoNothing();
        // 4.Trigger对象
        CronTrigger cronTrigger = TriggerBuilder.newTrigger()
                .withIdentity(getTriggerKey(jobId, jobGroup))
                .withSchedule(cronBuilder)
                .build();
        // 5.将sysJob存放到JobDetail中的Datamap中
        jobDetail.getJobDataMap().put(ScheduleConstants.TASK_PROPERTIES, sysJob);
        // 6.若存在job，则采用先删除再添加的方式
        if (scheduler.checkExists(getJobKey(jobId, jobGroup))) {
            scheduler.deleteJob(getJobKey(jobId, jobGroup));
        }
        scheduler.scheduleJob(jobDetail, cronTrigger);
        // 7.若任务原本时暂停的状态，则任务重新注册后恢复暂停状态
        if (ScheduleConstants.Status.PAUSE.getValue().equals(sysJob.getStatus())) {
            scheduler.pauseJob(getJobKey(jobId, jobGroup));
        }
    }
}
