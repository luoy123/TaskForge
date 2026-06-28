package com.zhq.taskforge.model.system.controller;

import com.zhq.taskforge.auth.vo.UserInfoResponseVo;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.security.LoginUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {

    /**
     * 获取当前登录用户的信息
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户的信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.USER_LIST + "')")
    public Result<UserInfoResponseVo> getCurrentUserInfo(@AuthenticationPrincipal LoginUser userDetails) {
        if (userDetails == null) {
            return Result.error("用户未登录");
        }
        UserInfoResponseVo userInfoResponseVo = new UserInfoResponseVo();
        userInfoResponseVo.setName(userDetails.getUsername());
        userInfoResponseVo.setNickName(userDetails.getNickName());
        userInfoResponseVo.setUserId(userDetails.getUserId());
        return Result.success(userInfoResponseVo);
    }
}
