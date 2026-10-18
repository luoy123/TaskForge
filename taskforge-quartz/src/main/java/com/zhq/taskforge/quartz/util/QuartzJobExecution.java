package com.zhq.taskforge.quartz.util;

import org.quartz.JobExecutionContext;

import com.zhq.taskforge.quartz.domain.SysJob;

public class QuartzJobExecution extends AbstractQuartzJob {

    @Override
    protected void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception {
        JobInvokeUtil.invokeMethod(sysJob);
    }

}
