package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhq.taskforge.model.system.entity.SysUser;
import com.zhq.taskforge.model.system.mapper.SysUserMapper;
import com.zhq.taskforge.model.system.service.SysUserService;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl
    extends ServiceImpl<SysUserMapper,SysUser>
        implements SysUserService {

    @Override
    public SysUser getUserByName(String name) {
        return this.lambdaQuery().eq(SysUser::getUserName,name).one();//返回一天数据
    }
}
