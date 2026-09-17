package com.zhq.taskforge.web.controller.monitor;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.quartz.domain.SysJob;
import com.zhq.taskforge.quartz.service.ISysJobService;

/**
 * I5a 最小管理接口：只做「立即执行」。完整 CRUD 留给 I5b。
 */
@RestController
@RequestMapping("/monitor/job")
public class SysJobController {

    @Autowired
    private ISysJobService jobService;

    /**
     * 立即触发一次。body 例：{"jobId": 11}
     * <p>接口成功只表示已交给 Scheduler，业务是否跑完要看日志或查库（triggerJob 异步）。
     */
    @PreAuthorize("@ss.hasPermi('" + PermissionConstants.MONITOR_JOB_RUN + "')")
    @PutMapping("/run")
    public R<Void> run(@RequestBody SysJob job) throws SchedulerException {
        boolean ok = jobService.run(job);
        return ok ? R.ok() : R.fail("任务不存在或未加载到调度器");
    }
}
