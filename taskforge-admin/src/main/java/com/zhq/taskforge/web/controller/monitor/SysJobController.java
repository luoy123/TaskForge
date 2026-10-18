package com.zhq.taskforge.web.controller.monitor;

import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.exception.job.TaskException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.quartz.domain.SysJob;
import com.zhq.taskforge.quartz.service.ISysJobService;
import com.zhq.taskforge.quartz.util.CronUtils;
import com.zhq.taskforge.quartz.util.ScheduleUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 定时任务管理（对齐 pmhub SysJobController；返回体用本仓库 R）
 */
@RestController
@RequestMapping("/monitor/job")
@Tag(name = "定时任务")
public class SysJobController {

    @Autowired
    private ISysJobService jobService;

    @GetMapping("/list")
    @Operation(summary = "查询定时任务列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_LIST + "')")
    public R<List<SysJob>> list(SysJob job) {
        return R.ok(jobService.selectJobList(job));
    }

    @GetMapping("/{jobId}")
    @Operation(summary = "查询定时任务详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_QUERY + "')")
    public R<SysJob> getInfo(@PathVariable Long jobId) {
        return R.ok(jobService.selectJobById(jobId));
    }

    @PostMapping
    @Operation(summary = "新增定时任务")
    @Log(title = "定时任务", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_ADD + "')")
    public R<Void> add(@RequestBody SysJob job) throws SchedulerException, TaskException {
        validateJob(job, true);
        job.setCreateBy(SecurityUtils.getUsername());
        return jobService.insertJob(job) > 0 ? R.ok() : R.fail("新增失败");
    }

    @PutMapping
    @Operation(summary = "修改定时任务")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_EDIT + "')")
    public R<Void> edit(@RequestBody SysJob job) throws SchedulerException, TaskException {
        validateJob(job, false);
        job.setUpdateBy(SecurityUtils.getUsername());
        return jobService.updateJob(job) > 0 ? R.ok() : R.fail("修改失败或任务不存在");
    }

    /**
     * 启停。body 例：{"jobId":11,"status":"1"} // 0正常 1暂停
     */
    @PutMapping("/changeStatus")
    @Operation(summary = "修改定时任务状态")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_CHANGE_STATUS + "')")
    public R<Void> changeStatus(@RequestBody SysJob job) throws SchedulerException {
        SysJob db = jobService.selectJobById(job.getJobId());
        if (db == null) {
            return R.fail("任务不存在");
        }
        db.setStatus(job.getStatus());
        return jobService.changeStatus(db) > 0 ? R.ok() : R.fail("状态修改失败");
    }

    /**
     * 立即执行一次。成功只表示已交给 Scheduler（异步）。
     */
    @PutMapping("/run")
    @Operation(summary = "立即执行一次")
    @Log(title = "定时任务", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_CHANGE_STATUS + "')")
    public R<Void> run(@RequestBody SysJob job) throws SchedulerException {
        boolean ok = jobService.run(job);
        return ok ? R.ok() : R.fail("任务不存在或未加载到调度器");
    }

    @DeleteMapping("/{jobIds}")
    @Operation(summary = "删除定时任务")
    @Log(title = "定时任务", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_REMOVE + "')")
    public R<Void> remove(@PathVariable Long[] jobIds) throws SchedulerException {
        if (ObjectUtils.isEmpty(jobIds)) {
            throw new ServiceException("删除主键不能为空");
        }
        jobService.deleteJobByIds(jobIds);
        return R.ok();
    }

    private void validateJob(SysJob job, boolean isAdd) {
        String action = isAdd ? "新增" : "修改";
        if (!CronUtils.isValid(job.getCronExpression())) {
            throw new ServiceException(action + "任务'" + job.getJobName() + "'失败，Cron表达式不正确");
        }
        if (StringUtils.containsIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_RMI)) {
            throw new ServiceException(action + "任务'" + job.getJobName() + "'失败，目标字符串不允许'rmi'调用");
        }
        if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.LOOKUP_LDAP, Constants.LOOKUP_LDAPS)) {
            throw new ServiceException(action + "任务'" + job.getJobName() + "'失败，目标字符串不允许'ldap(s)'调用");
        }
        if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.HTTP, Constants.HTTPS)) {
            throw new ServiceException(action + "任务'" + job.getJobName() + "'失败，目标字符串不允许'http(s)'调用");
        }
        if (StringUtils.containsAnyIgnoreCase(job.getInvokeTarget(), Constants.JOB_ERROR_STR)) {
            throw new ServiceException(action + "任务'" + job.getJobName() + "'失败，目标字符串存在违规");
        }
        if (!ScheduleUtils.whiteList(job.getInvokeTarget())) {
            throw new ServiceException(action + "任务'" + job.getJobName() + "'失败，目标字符串不在白名单内");
        }
    }
}
