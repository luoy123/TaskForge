package com.zhq.taskforge.web.controller.system;

import com.zhq.taskforge.system.domain.vo.UserInfoResponseVo;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
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
public class SysUserController {

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
}
