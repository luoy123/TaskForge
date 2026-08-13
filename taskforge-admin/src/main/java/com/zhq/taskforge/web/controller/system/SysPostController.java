package com.zhq.taskforge.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.domain.SysPost;
import com.zhq.taskforge.system.service.ISysPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/system/post")
@Tag(name = "岗位管理")
public class SysPostController {

    @Autowired
    private ISysPostService sysPostService;

    @GetMapping("/list")
    @Operation(summary = "分页查询列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_LIST + "')")
    public R<Page<SysPost>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysPost sysPost
    ) {
        Page<SysPost> sysPostPage = sysPostService.selectPostList(pageNum, pageSize, sysPost);
        return R.ok(sysPostPage);
    }

    @GetMapping("/{postId}")
    @Operation(summary = "根据id查询岗位详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_QUERY + "')")
    public R<SysPost> getInfo(@PathVariable Long postId) {
        SysPost sysPost = sysPostService.selectPostById(postId);
        return R.ok(sysPost);
    }

    @PostMapping()
    @Operation(summary = "添加岗位")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_ADD + "')")
    public R<Void> add(@RequestBody SysPost sysPost) {
        sysPost.setCreateBy(SecurityUtils.getUsername());
        sysPostService.insertPost(sysPost);
        return R.ok();
    }

    @PutMapping()
    @Operation(summary = "修改岗位")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_EDIT + "')")
    public R<Void> update(@RequestBody SysPost sysPost) {
        sysPost.setUpdateBy(SecurityUtils.getUsername());
        sysPostService.updatePost(sysPost);
        return R.ok();
    }

    @DeleteMapping("/{postIds}")
    @Operation(summary = "删除岗位")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_REMOVE + "')")
    public R<Void> remove(@PathVariable List<Long> postIds) {
        sysPostService.deletePostById(postIds);
        return R.ok();
    }
}
