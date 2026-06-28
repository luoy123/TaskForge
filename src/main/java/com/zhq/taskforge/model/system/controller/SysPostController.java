package com.zhq.taskforge.model.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.model.system.entity.SysPost;
import com.zhq.taskforge.model.system.service.SysPostService;
import com.zhq.taskforge.security.SecurityUtils;
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
    private SysPostService sysPostService;

    /**
     * 分页查询岗位列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_LIST + "')")
    public Result<Page<SysPost>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysPost sysPost
    ) {
        Page<SysPost> sysPostPage = sysPostService.selectPostList(pageNum, pageSize, sysPost);
        return Result.success(sysPostPage);
    }

    /**
     * 根据id查询岗位详情
     */
    @GetMapping("/{postId}")
    @Operation(summary = "根据id查询岗位详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_QUERY + "')")
    public Result<SysPost> getInfo(@PathVariable Long postId) {
        SysPost sysPost = sysPostService.selectPostById(postId);
        return Result.success(sysPost);
    }

    /**
     * 添加岗位
     */
    @PostMapping()
    @Operation(summary = "添加岗位")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_ADD + "')")
    public Result<Void> add(@RequestBody SysPost sysPost) {
        sysPost.setCreateBy(SecurityUtils.getUserName());
        sysPostService.insertPost(sysPost);
        return Result.success();
    }

    /**
     * 修改岗位
     */
    @PutMapping()
    @Operation(summary = "修改岗位")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_EDIT + "')")
    public Result<Void> update(@RequestBody SysPost sysPost) {
        sysPost.setUpdateBy(SecurityUtils.getUserName());
        sysPostService.updatePost(sysPost);
        return Result.success();
    }

    /**
     * 删除岗位
     */
    @DeleteMapping("/{postIds}")
    @Operation(summary = "删除岗位")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.POST_REMOVE + "')")
    public Result<Void> remove(@PathVariable List<Long> postIds) {
        sysPostService.deletePostById(postIds);
        return Result.success();
    }
}
