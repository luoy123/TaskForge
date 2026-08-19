package com.zhq.taskforge.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.framework.web.service.TokenService;
import com.zhq.taskforge.system.service.ISysUserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.util.StringUtils;

@RestController
@RequestMapping("/system/user/profile")
@Tag(name = "个人资料模块")
public class SysProfileController {

    @Autowired
    private ISysUserService iSysUserService;

    @Autowired
    private TokenService tokenService;

    @GetMapping()
    @Operation(summary = "获取个人资料")
    public R<SysUser> getProfile() {
        Long userId = SecurityUtils.getUserId();
        SysUser user = iSysUserService.selectUserById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        return R.ok(user);
    }

    @PutMapping()
    @Operation(summary = "修改个人资料")
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    public R<Void> updateProfile(@RequestBody SysUser sysUser) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUser current = loginUser.getUser();

        // 只能改自己；敏感字段由 Service 白名单忽略，不依赖「置 null」
        sysUser.setUserId(current.getUserId());

        if (StringUtils.hasText(sysUser.getPhonenumber()) && !iSysUserService.checkPhoneUnique(sysUser)) {
            return R.fail("修改手机号失败，手机号已存在");
        }
        if (StringUtils.hasText(sysUser.getEmail()) && !iSysUserService.checkEmailUnique(sysUser)) {
            return R.fail("修改邮箱失败，邮箱已存在");
        }

        if (iSysUserService.updateProfile(sysUser) > 0) {
            if (StringUtils.hasText(sysUser.getNickName())) {
                current.setNickName(sysUser.getNickName());
            }
            if (StringUtils.hasText(sysUser.getPhonenumber())) {
                current.setPhonenumber(sysUser.getPhonenumber());
            }
            if (StringUtils.hasText(sysUser.getEmail())) {
                current.setEmail(sysUser.getEmail());
            }
            if (StringUtils.hasText(sysUser.getSex())) {
                current.setSex(sysUser.getSex());
            }
            tokenService.setLoginUser(loginUser);
            return R.ok();
        }
        return R.fail("修改个人信息失败，联系管理员");
    }

    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    @Operation(summary = "修改个人密码")
    public R<Void> updatePwd(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        String encrypted = iSysUserService.updateUserPassword(
                loginUser.getUserId(), oldPassword, newPassword);

        // 与数据库使用同一份哈希更新缓存
        loginUser.getUser().setPassword(encrypted);
        tokenService.setLoginUser(loginUser);
        return R.ok();
    }
}
