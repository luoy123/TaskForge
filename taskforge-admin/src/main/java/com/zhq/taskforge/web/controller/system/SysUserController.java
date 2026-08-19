package com.zhq.taskforge.web.controller.system;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
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
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.system.domain.vo.UserInfoResponseVo;
import com.zhq.taskforge.system.service.ISysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/system/user")
@Tag(name = "用户模块")
public class SysUserController {

    @Autowired
    ISysUserService iSysUserService;

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户的信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_LIST + "')")
    public R<UserInfoResponseVo> getCurrentUserInfo(@AuthenticationPrincipal LoginUser userDetails) {
        if (userDetails == null) {
            return R.fail("用户未登录");
        }
        UserInfoResponseVo userInfoResponseVo = new UserInfoResponseVo();
        userInfoResponseVo.setName(userDetails.getUsername());
        userInfoResponseVo.setNickName(userDetails.getUser().getNickName());
        userInfoResponseVo.setUserId(userDetails.getUserId());
        return R.ok(userInfoResponseVo);
    }

    @GetMapping("/list")
    @Operation(summary = "获取用户列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_LIST + "')")
    public R<Page<SysUser>> list(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysUser sysUser) {
        Page<SysUser> page = iSysUserService.selectUserPage(pageNum, pageSize, sysUser);
        return R.ok(page);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "根据id查询用户")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_QUERY + "')")
    public R<SysUser> getUser(@PathVariable Long userId) {
        SysUser byId = iSysUserService.selectUserById(userId);
        return R.ok(byId);
    }

    @PostMapping()
    @Operation(summary = "添加用户")
    @Log(title = "用户管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_ADD + "')")
    public R<Void> add(@RequestBody SysUser user) {
        if (!iSysUserService.checkUserNameUnique(user)) {
            return R.fail("登录账号已经存在");
        }
        if (StringUtils.hasText(user.getPhonenumber()) && !iSysUserService.checkPhoneUnique(user)) {
            return R.fail("手机号已存在");
        }
        user.setCreateBy(SecurityUtils.getUsername());
        iSysUserService.insertUser(user);
        return R.ok();
    }

    @DeleteMapping("/{userIds}")
    @Operation(summary = "删除用户")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_REMOVE + "')")
    public R<Void> remove(@PathVariable List<Long> userIds) {
        iSysUserService.deleteUserByUserIds(userIds);
        return R.ok();
    }

    @PutMapping()
    @Operation(summary = "编辑用户")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_EDIT + "')")
    public R<SysUser> update(@RequestBody SysUser user) {
        if (!iSysUserService.checkUserNameUnique(user)) {
            return R.fail("用户名已存在");
        }
        if (StringUtils.hasText(user.getPhonenumber()) && !iSysUserService.checkPhoneUnique(user)) {
            return R.fail("手机号已存在");
        }
        user.setUpdateBy(SecurityUtils.getUsername());
        SysUser updateUser = iSysUserService.updateUser(user);
        return R.ok(updateUser);
    }

    @PutMapping("/changeStatus")
    @Operation(summary = "修改用户状态")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_EDIT + "')")
    public R<Void> updateStatus(@RequestBody SysUser user) {
        iSysUserService.updateUserStatus(user);
        return R.ok();
    }

    @PutMapping("/resetPwd")
    @Operation(summary = "修改用户密码")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_RESET_PWD + "')")
    public R<Void> updatePwd(@RequestBody SysUser user) {
        iSysUserService.resetPwd(user);
        return R.ok();
    }

}
