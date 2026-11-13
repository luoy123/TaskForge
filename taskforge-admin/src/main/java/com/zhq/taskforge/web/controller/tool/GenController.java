package com.zhq.taskforge.web.controller.tool;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.generator.domain.GenTable;
import com.zhq.taskforge.generator.service.IGenTableService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 代码生成（对照 pmhub GenController；只做 zip 下载，不写本地路径）
 */
@RestController
@RequestMapping("/tool/gen")
@Tag(name = "代码生成")
public class GenController {

    @Autowired
    private IGenTableService genTableService;

    @GetMapping("/list")
    @Operation(summary = "已导入的表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_LIST + "')")
    public R<Page<GenTable>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            GenTable query) {
        return R.ok(genTableService.selectGenTablePage(pageNum, pageSize, query));
    }

    @GetMapping("/db/list")
    @Operation(summary = "数据库中可导入的表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_LIST + "')")
    public R<Page<GenTable>> dbList(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            GenTable query) {
        return R.ok(genTableService.selectDbTablePage(pageNum, pageSize, query));
    }

    @GetMapping("/{tableId}")
    @Operation(summary = "表生成配置（含列）")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_QUERY + "')")
    public R<GenTable> getInfo(@PathVariable Long tableId) {
        return R.ok(genTableService.selectGenTableById(tableId));
    }

    /**
     * body 例：["sys_notice", "sys_post"]
     */
    @PostMapping("/importTable")
    @Operation(summary = "导入表结构")
    @Log(title = "代码生成", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_IMPORT + "')")
    public R<Void> importTable(@RequestBody List<String> tableNames) {
        genTableService.importGenTable(tableNames, SecurityUtils.getUsername());
        return R.ok();
    }

    @PutMapping
    @Operation(summary = "修改生成配置")
    @Log(title = "代码生成", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_EDIT + "')")
    public R<Void> edit(@RequestBody GenTable genTable) {
        genTableService.updateGenTable(genTable, SecurityUtils.getUsername());
        return R.ok();
    }

    @DeleteMapping("/{tableIds}")
    @Operation(summary = "删除导入记录")
    @Log(title = "代码生成", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_REMOVE + "')")
    public R<Void> remove(@PathVariable Long[] tableIds) {
        genTableService.deleteGenTableByIds(tableIds);
        return R.ok();
    }

    @GetMapping("/preview/{tableId}")
    @Operation(summary = "预览代码")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_PREVIEW + "')")
    public R<Map<String, String>> preview(@PathVariable Long tableId) {
        return R.ok(genTableService.previewCode(tableId));
    }

    @GetMapping("/download/{tableId}")
    @Operation(summary = "下载代码 zip")
    @Log(title = "代码生成", businessType = BusinessType.GENCODE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.GEN_CODE + "')")
    public void download(@PathVariable Long tableId, HttpServletResponse response) throws IOException {
        GenTable table = genTableService.selectGenTableById(tableId);
        byte[] data = genTableService.downloadCode(tableId);
        String fileName = URLEncoder.encode(table.getTableName() + ".zip", StandardCharsets.UTF_8);
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName);
        response.setContentLength(data.length);
        response.getOutputStream().write(data);
    }
}
