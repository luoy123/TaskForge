package com.zhq.taskforge.model.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zhq.taskforge.model.system.entity.SysUser;

public interface SysUserService extends IService<SysUser> {

    public SysUser getUserByName(String name);
}
