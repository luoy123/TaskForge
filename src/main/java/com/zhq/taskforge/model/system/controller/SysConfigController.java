package com.zhq.taskforge.model.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.model.system.entity.SysConfig;
import com.zhq.taskforge.model.system.service.SysConfigService;
import com.zhq.taskforge.security.SecurityUtils;
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
    private SysConfigService sysConfigService;

    /**
     * 查询参数列表
     */
    @GetMapping("/list")
    @Operation(summary = "查询列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_LIST + "')")
    public Result<Page<SysConfig>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysConfig sysConfig
    ) {
        Page<SysConfig> sysConfigPage = sysConfigService.selectSysConfigList(pageNum, pageSize, sysConfig);
        return Result.success(sysConfigPage);
    }

    /**
     * 查询参数详情
     */
    @GetMapping("/{configId}")
    @Operation(summary = "查询参数详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_QUERY + "')")
    public Result<SysConfig> query(@PathVariable Long configId) {
        SysConfig sysConfig = sysConfigService.selectSysConfigById(configId);
        return Result.success(sysConfig);
    }

    /**
     * 根据key查询configValue
     */
    @GetMapping("/configKey/{configKey}")
    @Operation(summary = "根据key查询configValue")
    public Result<String> queryByConfigKey(@PathVariable String configKey) {
        String configValue = sysConfigService.selectSysConfigByKey(configKey);
        return Result.success(configValue);
    }

    /**
     * 新增参数
     */
    @PostMapping
    @Operation(summary = "新增")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_ADD + "')")
    public Result<Void> add(@RequestBody SysConfig sysConfig) {
        sysConfig.setCreateBy(SecurityUtils.getUserName());
        sysConfigService.insertSysConfig(sysConfig);
        return Result.success();
    }

    /**
     * 修改参数
     */
    @PutMapping
    @Operation(summary = "修改")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_EDIT + "')")
    public Result<Void> update(@RequestBody SysConfig sysConfig) {
        sysConfig.setUpdateBy(SecurityUtils.getUserName());
        sysConfigService.updateSysConfig(sysConfig);
        return Result.success();
    }

    /**
     * 删除参数
     */
    @DeleteMapping("/{configIds}")
    @Operation(summary = "删除")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_REMOVE + "')")
    public Result<Void> remove(@PathVariable List<Long> configIds) {
        sysConfigService.deleteSysConfigByIds(configIds);
        return Result.success();
    }

    @DeleteMapping("/refresheCache")
    @Operation(summary = "刷新参数缓存")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.CONFIG_REMOVE + "')")
    public Result<Void> refreshCache() {
        sysConfigService.resetConfigCache();
        return Result.success();
    }
}
