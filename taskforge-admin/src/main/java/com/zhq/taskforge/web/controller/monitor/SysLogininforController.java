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
import com.zhq.taskforge.system.domain.SysLogininfor;
import com.zhq.taskforge.system.service.ISysLogininforService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 登录日志（由 SysLoginService + AsyncFactory 写入，这里只负责查询/删除）
 */
@RestController
@RequestMapping("/monitor/logininfor")
@Tag(name = "登录日志")
public class SysLogininforController {

    @Autowired
    private ISysLogininforService logininforService;

    @GetMapping("/list")
    @Operation(summary = "查询登录日志列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.LOGININFOR_LIST + "')")
    public R<Page<SysLogininfor>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysLogininfor logininfor) {
        return R.ok(logininforService.selectLogininforList(pageNum, pageSize, logininfor));
    }

    @GetMapping("/{infoId}")
    @Operation(summary = "查询登录日志详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.LOGININFOR_QUERY + "')")
    public R<SysLogininfor> getInfo(@PathVariable Long infoId) {
        return R.ok(logininforService.selectLogininforById(infoId));
    }

    /** 字面量 /clean 放在 /{infoIds} 前面，避免被当成 id */
    @DeleteMapping("/clean")
    @Operation(summary = "清空登录日志")
    @Log(title = "登录日志", businessType = BusinessType.CLEAN)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.LOGININFOR_REMOVE + "')")
    public R<Void> clean() {
        logininforService.cleanLogininfor();
        return R.ok();
    }

    @DeleteMapping("/{infoIds}")
    @Operation(summary = "删除登录日志")
    @Log(title = "登录日志", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.LOGININFOR_REMOVE + "')")
    public R<Void> remove(@PathVariable Long[] infoIds) {
        if (ObjectUtils.isEmpty(infoIds)) {
            throw new ServiceException("删除主键不能为空");
        }
        logininforService.deleteLogininforByIds(infoIds);
        return R.ok();
    }
}
