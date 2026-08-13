package com.zhq.taskforge.web.controller.system;

import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.model.LoginBody;
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

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<Map<String, Object>> login(@RequestBody LoginBody loginBody) {
        String token = authService.login(loginBody);
        Map<String, Object> result = new HashMap<>();
        result.put(Constants.TOKEN, token);
        return R.ok(result);
    }
}
