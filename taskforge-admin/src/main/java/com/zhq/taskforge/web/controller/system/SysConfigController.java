package com.zhq.taskforge.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.domain.SysConfig;
import com.zhq.taskforge.system.service.ISysConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/config")
@Tag(name = "参数设置")
public class SysConfigController {

    @Autowired
    private ISysConfigService sysConfigService;

    @GetMapping("/list")
    @Operation(summary = "查询列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_LIST + "')")
    // @PreAuthorize("@ss.hasPermi('system:config:list')")
    public R<Page<SysConfig>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysConfig sysConfig
    ) {
        Page<SysConfig> sysConfigPage = sysConfigService.selectSysConfigList(pageNum, pageSize, sysConfig);
        return R.ok(sysConfigPage);
    }

    @GetMapping("/{configId}")
    @Operation(summary = "查询参数详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_QUERY + "')")
    public R<SysConfig> query(@PathVariable Long configId) {
        SysConfig sysConfig = sysConfigService.selectSysConfigById(configId);
        return R.ok(sysConfig);
    }

    @GetMapping("/configKey/{configKey}")
    @Operation(summary = "根据key查询configValue")
    public R<String> queryByConfigKey(@PathVariable String configKey) {
        String configValue = sysConfigService.selectSysConfigByKey(configKey);
        return R.ok(configValue);
    }

    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_ADD + "')")
    public R<Void> add(@RequestBody SysConfig sysConfig) {
        sysConfig.setCreateBy(SecurityUtils.getUsername());
        sysConfigService.insertSysConfig(sysConfig);
        return R.ok();
    }

    @PutMapping
    @Operation(summary = "修改")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_EDIT + "')")
    public R<Void> update(@RequestBody SysConfig sysConfig) {
        sysConfig.setUpdateBy(SecurityUtils.getUsername());
        sysConfigService.updateSysConfig(sysConfig);
        return R.ok();
    }

    @DeleteMapping("/{configIds}")
    @Operation(summary = "删除")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_REMOVE + "')")
    public R<Void> remove(@PathVariable List<Long> configIds) {
        sysConfigService.deleteSysConfigByIds(configIds);
        return R.ok();
    }

    @DeleteMapping("/refresheCache")
    @Operation(summary = "刷新参数缓存")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_REMOVE + "')")
    public R<Void> refreshCache() {
        sysConfigService.resetConfigCache();
        return R.ok();
    }
}
