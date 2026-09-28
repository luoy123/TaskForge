package com.zhq.taskforge.web.controller.common;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.Constants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.redis.RedisCache;
import com.zhq.taskforge.system.service.ISysConfigService;

import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping
@Tag(name = "验证码模块")
public class CaptchaController {

    @Autowired
    private ISysConfigService configService;

    @Autowired
    RedisCache redisCache;

    @GetMapping("/captchaImage")
    @Operation(summary = "生成图片验证码")
    public R<Map<String, Object>> generateCaptchaImage() {
        HashMap<String, Object> data = new HashMap<String, Object>();
        // 1.检验查是否开启了验证码开关
        String captchaEnableStr = configService.selectSysConfigByKey("sys.account.captchaEnabled");
        boolean captchaEnabled = "true".equals(captchaEnableStr);
        data.put("captchaEnabled", captchaEnabled);

        // 2. 不需要验证，直接返回
        if (!captchaEnabled) {
            return R.ok(data);
        }
        // 3.生成uuid
        String uuid = IdUtil.simpleUUID();
        // 4.生成验证码图：160×60，干扰线适中（过小会被前端拉伸发糊）
        LineCaptcha lineCaptcha = new LineCaptcha(160, 60, 4, 40);
        // 获取code字符串
        String code = lineCaptcha.getCode();
        // 5.存入到redis中
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + uuid;
        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 6.组装响应
        data.put("uuid", uuid);
        data.put("img", lineCaptcha.getImageBase64());

        return R.ok(data);
    }

}
