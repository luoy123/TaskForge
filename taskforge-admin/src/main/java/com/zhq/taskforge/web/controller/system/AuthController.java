package com.zhq.taskforge.web.controller.system;

import com.zhq.taskforge.common.annotation.RateLimter;
import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.model.LoginBody;
import com.zhq.taskforge.common.enums.LimitType;
import com.zhq.taskforge.framework.web.service.SysLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "认证登录模块")
public class AuthController {

    @Autowired
    private SysLoginService authService;

    /** J3 试点：同一 IP 60 秒内最多 20 次登录（压测可临时改小 count） */
    @RateLimter(time = 60, count = 20, limitType = LimitType.IP)
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<Map<String, Object>> login(@RequestBody LoginBody loginBody) {
        String token = authService.login(loginBody);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.TOKEN, token);
        return R.ok(result);
    }
}
