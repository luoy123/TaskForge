package com.zhq.taskforge.quartz.util;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zhq.taskforge.common.constants.ScheduleConstants;
import com.zhq.taskforge.quartz.domain.SysJob;

public abstract class AbstractQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 从 JobDataMap 中取出 SysJob
        // getMergedJobDataMap(): 合并 JobDetail 与 Trigger；Trigger 中的会覆盖 JobDetail 中的
        SysJob sysJob = (SysJob) context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES);

        try {
            if (sysJob != null) {
                doExecute(context, sysJob);
            }
        } catch (Exception e) {
            log.error("定时任务执行异常, jobId={}, target={}",
                    sysJob != null ? sysJob.getJobId() : null,
                    sysJob != null ? sysJob.getInvokeTarget() : null, e);
            throw new JobExecutionException(e);
        }

    }

    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;

}
