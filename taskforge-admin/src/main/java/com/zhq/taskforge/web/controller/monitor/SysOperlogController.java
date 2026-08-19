package com.zhq.taskforge.web.controller.monitor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.system.domain.SysOperLog;
import com.zhq.taskforge.system.service.ISysOperLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 操作日志（由 @Log + LogAspect 写入，这里只负责查询/删除）
 */
@RestController
@RequestMapping("/monitor/operlog")
@Tag(name = "操作日志")
public class SysOperlogController {

    @Autowired
    private ISysOperLogService operLogService;

    @GetMapping("/list")
    @Operation(summary = "查询操作日志列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.OPERLOG_LIST + "')")
    public R<Page<SysOperLog>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysOperLog operLog) {
        return R.ok(operLogService.selectOperLogList(pageNum, pageSize, operLog));
    }

    @GetMapping("/{operId}")
    @Operation(summary = "查询操作日志详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.OPERLOG_QUERY + "')")
    public R<SysOperLog> getInfo(@PathVariable Long operId) {
        return R.ok(operLogService.selectOperLogById(operId));
    }

    /** 字面量 /clean 放在 /{operIds} 前面，避免被当成 id */
    @DeleteMapping("/clean")
    @Operation(summary = "清空操作日志")
    @Log(title = "操作日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.OPERLOG_REMOVE + "')")
    public R<Void> clean() {
        operLogService.cleanOperLog();
        return R.ok();
    }

    @DeleteMapping("/{operIds}")
    @Operation(summary = "删除操作日志")
    @Log(title = "操作日志", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.OPERLOG_REMOVE + "')")
    public R<Void> remove(@PathVariable Long[] operIds) {
        if (ObjectUtils.isEmpty(operIds)) {
            throw new ServiceException("删除主键不能为空");
        }
        operLogService.deleteOperLogByIds(operIds);
        return R.ok();
    }
}
