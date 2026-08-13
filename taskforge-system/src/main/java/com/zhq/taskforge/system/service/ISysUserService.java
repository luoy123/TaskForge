package com.zhq.taskforge.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zhq.taskforge.common.core.domain.entity.SysUser;

public interface ISysUserService extends IService<SysUser> {

    public SysUser getUserByName(String name);

    // 查询
    public Page<SysUser> selectUserPage(Long PageNum, Long PageSizes, SysUser sysUser);

    public SysUser selectUserById(Long userId);

    // 校验
    public boolean checkUserNameUnique(SysUser user);

    public boolean checkPhoneUnique(SysUser user);

    public boolean checkUserAllowed(SysUser user);

    // 写操作
    public void insetUser(SysUser user);

    public SysUser updateUser(SysUser user);

    public void deleteUserByUserIds(List<Long> ids);

    public SysUser updateUserStatus(SysUser user);

    public void resetPwd(SysUser user);

}
