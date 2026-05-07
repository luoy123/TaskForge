package com.zhq.taskforge.model.system.controller;

import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.ResultCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "用户模块")
public class UserController {

    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户的信息")
    public Result getCurrentUserInfo(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return new Result(ResultCode.SUCCESS, "用户未登录");
        }
        return new Result(ResultCode.SUCCESS,userDetails);
    }

}
