package com.zhq.taskforge.web.controller.monitor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.quartz.domain.SysJobLog;
import com.zhq.taskforge.quartz.service.ISysJobLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 定时任务执行日志（对齐 pmhub SysJobLogController）
 */
@RestController
@RequestMapping("/monitor/jobLog")
@Tag(name = "定时任务日志")
public class SysJobLogController {

    @Autowired
    private ISysJobLogService jobLogService;

    @GetMapping("/list")
    @Operation(summary = "查询调度日志列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_LIST + "')")
    public R<List<SysJobLog>> list(SysJobLog jobLog) {
        return R.ok(jobLogService.selectJobLogList(jobLog));
    }

    @GetMapping("/{jobLogId}")
    @Operation(summary = "查询调度日志详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_QUERY + "')")
    public R<SysJobLog> getInfo(@PathVariable Long jobLogId) {
        return R.ok(jobLogService.selectJobLogById(jobLogId));
    }

    /** /clean 必须写在 /{ids} 前面，否则 clean 会被当成 id */
    @DeleteMapping("/clean")
    @Operation(summary = "清空调度日志")
    @Log(title = "定时任务日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_REMOVE + "')")
    public R<Void> clean() {
        jobLogService.cleanJobLog();
        return R.ok();
    }

    @DeleteMapping("/{jobLogIds}")
    @Operation(summary = "删除调度日志")
    @Log(title = "定时任务日志", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.MONITOR_JOB_REMOVE + "')")
    public R<Void> remove(@PathVariable Long[] jobLogIds) {
        if (ObjectUtils.isEmpty(jobLogIds)) {
            throw new ServiceException("删除主键不能为空");
        }
        return jobLogService.deleteJobLogByIds(jobLogIds) > 0 ? R.ok() : R.fail("删除失败");
    }
}
