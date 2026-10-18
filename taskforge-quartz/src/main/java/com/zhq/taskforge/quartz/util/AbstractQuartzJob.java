package com.zhq.taskforge.quartz.util;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.constants.ScheduleConstants;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.common.utils.spring.SpringUtils;
import com.zhq.taskforge.quartz.domain.SysJob;
import com.zhq.taskforge.quartz.domain.SysJobLog;
import com.zhq.taskforge.quartz.service.ISysJobLogService;

public abstract class AbstractQuartzJob implements Job {

    private static final Logger log = LoggerFactory.getLogger(AbstractQuartzJob.class);

    // 利用线程池来记录开始时间，因为存在并发的情况，所以需要让每个线程间不会相互影响。
    private ThreadLocal<Date> threadLocal = new ThreadLocal<>();

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // 从 JobDataMap 中取出 SysJob
        // getMergedJobDataMap(): 合并 JobDetail 与 Trigger；Trigger 中的会覆盖 JobDetail 中的
        SysJob sysJob = new SysJob();
        Object prop = context.getMergedJobDataMap().get(ScheduleConstants.TASK_PROPERTIES);
        if (prop == null) {
            log.error("任务数据为空，跳过执行");
            return;
        }
        BeanUtils.copyProperties(prop, sysJob);
        try {
            // 记录开始时间
            before(context, sysJob);
            // 执行具体的业务逻辑
            doExecute(context, sysJob);
            // 写日志到数据库中
            after(context, sysJob, null);
        } catch (Exception e) {
            log.error("任务执行异常  - ：", e);
            after(context, sysJob, e);
        }

    }

    private void before(JobExecutionContext context, SysJob job) {
        threadLocal.set(new Date());
    }

    private void after(JobExecutionContext context, SysJob job, Exception e) {
        Date date = threadLocal.get();
        // 防止内存泄露问题，需要进行手动的删除
        threadLocal.remove();

        SysJobLog sysJobLog = new SysJobLog();
        sysJobLog.setJobName(job.getJobName());
        sysJobLog.setJobGroup(job.getJobGroup());
        sysJobLog.setInvokeTarget(job.getInvokeTarget());
        sysJobLog.setStartTime(date);
        sysJobLog.setStopTime(new Date());
        Long runMs = sysJobLog.getStopTime().getTime() - sysJobLog.getStartTime().getTime();
        sysJobLog.setJobMessage(sysJobLog.getJobName() + "总共耗时：" + runMs + "毫秒");
        if (e != null) {
            sysJobLog.setStatus(Constants.FAIL);
            String errMessage = StringUtils.substring(e.getMessage(), 0, 2000);
            sysJobLog.setExceptionInfo(errMessage);
        } else {
            sysJobLog.setStatus(Constants.SUCCESS);
        }

        // 写入到数据库中，这里需要手动的添加，因为Quartz的job不在Spring容器里面，无法使用@Autowired
        SpringUtils.getBean(ISysJobLogService.class).addJobLog(sysJobLog);

    }

    protected abstract void doExecute(JobExecutionContext context, SysJob sysJob) throws Exception;

}
