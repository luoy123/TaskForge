package com.zhq.taskforge.common.core.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis JSON 序列化时忽略 UserDetails 派生方法，只缓存业务字段。
 * GenericJackson2JsonRedisSerializer 序列化 getAuthorities() 等会导致登录写 Redis 失败。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginUser implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long deptId;
    private String token;
    private Long loginTime;
    private Long expireTime;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
    private Set<String> permissions;
    private SysUser user;
    private List<String> menuPermissions;

    public LoginUser() {
    }

    public LoginUser(SysUser user, Set<String> permissions) {
        this.user = user;
        this.permissions = permissions;
    }

    public LoginUser(Long userId, Long deptId, SysUser user, Set<String> permissions) {
        this.userId = userId;
        this.deptId = deptId;
        this.user = user;
        this.permissions = permissions;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return user != null ? user.getPassword() : null;
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return user != null ? user.getUserName() : null;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return user != null && user.getStatus() != null && user.getStatus() == 0;
    }
}
