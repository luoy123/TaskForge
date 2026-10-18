package com.zhq.taskforge.quartz.util;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.constants.ScheduleConstants;
import com.zhq.taskforge.common.exception.job.TaskException;
import com.zhq.taskforge.common.exception.job.TaskException.Code;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.common.utils.spring.SpringUtils;
import com.zhq.taskforge.quartz.domain.SysJob;

public class ScheduleUtils {

    /**
     * 得到Quartz的任务类
     */
    public static Class<? extends Job> getQuartzJobClass(SysJob sysJob) {
        boolean isConcurrent = "0".equals(sysJob.getConcurrent());
        return isConcurrent ? QuartzJobExecution.class : QuartzDisallowConcurrentExecution.class;
    }

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
        // 7.判断任务是否过期
        if (StringUtils.isNotNull(CronUtils.getNextValidTimeAfter(sysJob.getCronExpression()))) {
            scheduler.scheduleJob(jobDetail, cronTrigger);
        }
        // 8.若任务原本时暂停的状态，则任务重新注册后恢复暂停状态
        if (ScheduleConstants.Status.PAUSE.getValue().equals(sysJob.getStatus())) {
            scheduler.pauseJob(getJobKey(jobId, jobGroup));
        }
    }

    /**
     * 设置定时任务策略
     */
    public static CronScheduleBuilder handleCronScheduleMisfirePolicy(SysJob job, CronScheduleBuilder cb)
            throws TaskException {
        switch (job.getMisfirePolicy()) {
            case ScheduleConstants.MISFIRE_DEFAULT:
                return cb;
            case ScheduleConstants.MISFIRE_IGNORE_MISFIRES:
                return cb.withMisfireHandlingInstructionIgnoreMisfires();
            case ScheduleConstants.MISFIRE_FIRE_AND_PROCEED:
                return cb.withMisfireHandlingInstructionFireAndProceed();
            case ScheduleConstants.MISFIRE_DO_NOTHING:
                return cb.withMisfireHandlingInstructionDoNothing();
            default:
                throw new TaskException("The task misfire policy '" + job.getMisfirePolicy()
                        + "' cannot be used in cron schedule tasks", Code.CONFIG_ERROR);
        }
    }

    /**
     * 检查包名是否为白名单判断
     */
    public static boolean whiteList(String invokeTarget) {
        String packageName = StringUtils.substringBefore(invokeTarget, "(");
        int count = StringUtils.countMatches(packageName, ".");
        if(count > 1){
            return StringUtils.containsAnyIgnoreCase(invokeTarget, Constants.JOB_WHITELIST_STR);
        }
        // beanName.method 形式：取 Bean 的真实包名做白名单判断
        Object obj = SpringUtils.getBean(StringUtils.split(invokeTarget, ".")[0]);
        return StringUtils.containsAnyIgnoreCase(obj.getClass().getPackage().getName(), Constants.JOB_WHITELIST_STR);
    }
}
