package com.zhq.taskforge.web.controller.system;

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
import com.zhq.taskforge.system.domain.SysNotice;
import com.zhq.taskforge.system.service.ISysNoticeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/system/notice")
@Tag(name = "公告模块")
public class SysNoticeController {

    @Autowired
    private ISysNoticeService sysNoticeService;

    @GetMapping("/list")
    @Operation(summary = "查询公告列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.NOTICE_LIST + "')")
    public R<Page<SysNotice>> getNoticeList(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysNotice sysNotice) {
        Page<SysNotice> page = sysNoticeService.selectNoticelist(pageNum, pageSize, sysNotice);
        return R.ok(page);
    }

    @GetMapping("/{noticeId}")
    @Operation(summary = "根据id查询公告")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.NOTICE_QUERY + "')")
    public R<SysNotice> getNoticeById(@PathVariable Long noticeId) {
        SysNotice notice = sysNoticeService.getById(noticeId);
        return R.ok(notice);
    }

    @PostMapping()
    @Operation(summary = "添加公告")
    @Log(title = "公告模块", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.NOTICE_ADD + "')")
    public R<Void> add(@RequestBody SysNotice sysNotice) {
        sysNoticeService.add(sysNotice);
        return R.ok();
    }

    @DeleteMapping("/{noticeIds}")
    @Operation(summary = "删除公告")
    @Log(title = "公告模块", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.NOTICE_REMOVE + "')")
    public R<Void> remove(@PathVariable Long[] noticeIds) {
        sysNoticeService.remove(noticeIds);
        return R.ok();
    }

    @PutMapping()
    @Operation(summary = "修改公告")
    @Log(title = "公告模块", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.NOTICE_EDIT + "')")
    public R<Void> update(@RequestBody SysNotice sysNotice) {
        sysNoticeService.update(sysNotice);
        return R.ok();
    }

}
