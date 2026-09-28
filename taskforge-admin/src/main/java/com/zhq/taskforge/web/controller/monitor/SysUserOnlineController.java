package com.zhq.taskforge.web.controller.monitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.CacheConstants;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.core.domain.model.LoginUser;
import com.zhq.taskforge.common.core.redis.RedisCache;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.system.domain.SysUserOnline;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 在线用户：扫 Redis login_tokens:* ；强退 = 删对应 key
 */
@RestController
@RequestMapping("/monitor/online")
@Tag(name = "在线用户")
public class SysUserOnlineController {

    @Autowired
    private RedisCache redisCache;

    /**
     * 在线用户列表。
     * 可选查询参数：ipaddr、userName（都空则返回全部）
     */
    @GetMapping("/list")
    @Operation(summary = "在线用户列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ONLINE_LIST + "')")
    public R<List<SysUserOnline>> list(String ipaddr, String userName) {
        Collection<String> keys = redisCache.keys(CacheConstants.LOGIN_TOKEN_KEY + "*");
        List<SysUserOnline> onlineList = new ArrayList<>();

        if (keys != null) {
            for (String key : keys) {
                LoginUser user = redisCache.getCacheObject(key);
                if (user == null) {
                    continue;
                }

                if (StringUtils.isNotEmpty(ipaddr) && StringUtils.isNotEmpty(userName)) {
                    if (ipaddr.equals(user.getIpaddr()) && userName.equals(user.getUsername())) {
                        addIfPresent(onlineList, user);
                    }
                } else if (StringUtils.isNotEmpty(ipaddr)) {
                    if (ipaddr.equals(user.getIpaddr())) {
                        addIfPresent(onlineList, user);
                    }
                } else if (StringUtils.isNotEmpty(userName)) {
                    if (userName.equals(user.getUsername())) {
                        addIfPresent(onlineList, user);
                    }
                } else {
                    addIfPresent(onlineList, user);
                }
            }
        }

        Collections.reverse(onlineList);
        return R.ok(onlineList);
    }

    /**
     * 强退：删 Redis 会话。JWT 仍可能有效，但过滤器从 Redis 取不到 LoginUser → 401。
     * path 里的 tokenId = LoginUser.token（不是整段 JWT）
     */
    @DeleteMapping("/{tokenId}")
    @Operation(summary = "强退用户")
    @Log(title = "在线用户", businessType = BusinessType.FORCE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.ONLINE_FORCE_LOGOUT + "')")
    public R<Void> forceLogout(@PathVariable String tokenId) {
        redisCache.deleteCacheObject(CacheConstants.LOGIN_TOKEN_KEY + tokenId);
        return R.ok();
    }

    private void addIfPresent(List<SysUserOnline> onlineList, LoginUser user) {
        SysUserOnline online = toOnline(user);
        if (online != null) {
            onlineList.add(online);
        }
    }

    /** LoginUser → 列表行（SysUser 当前无 dept 关联字段，deptName 留空） */
    private SysUserOnline toOnline(LoginUser user) {
        if (user == null || user.getUser() == null) {
            return null;
        }
        SysUserOnline online = new SysUserOnline();
        online.setTokenId(user.getToken());
        online.setUserName(user.getUsername());
        online.setIpaddr(user.getIpaddr());
        online.setLoginLocation(user.getLoginLocation());
        online.setBrowser(user.getBrowser());
        online.setOs(user.getOs());
        online.setLoginTime(user.getLoginTime());
        return online;
    }
}
