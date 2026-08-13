package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.system.mapper.SysUserMapper;
import com.zhq.taskforge.system.service.ISysUserService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements ISysUserService {

    @Autowired
    SysUserMapper sysUserMapper;

    @Override
    public SysUser getUserByName(String name) {
        return this.lambdaQuery().eq(SysUser::getUserName, name).one();
    }

    @Override
    public Page<SysUser> selectUserPage(Long PageNum, Long PageSizes, SysUser sysUser) {
        Page<Object> page = new Page<>(PageNum, PageSizes);
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();

    }

    @Override
    public SysUser selectUserById(Long userId) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserId, userId);
        SysUser selectOne = sysUserMapper.selectOne(qw);
        ;
        return selectOne;
    }

    @Override
    public boolean checkUserNameUnique(SysUser user) {
        String userName = user.getUserName();
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>().eq(SysUser::getUserName, userName);
        Long selectCount = sysUserMapper.selectCount(qw);
        if (selectCount > 0) {
            return false;
        }
        return true;

    }

    @Override
    public boolean checkPhoneUnique(SysUser user) {
        String phonenumber = user.getPhonenumber();
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>().eq(SysUser::getPhonenumber, phonenumber);
        Long selectCount = sysUserMapper.selectCount(qw);
        if (selectCount > 0) {
            return false;
        }
        return true;
    }

    @Override
    public boolean checkUserAllowed(SysUser user) {

    }

    @Override
    public void insetUser(SysUser user) {

    }

    @Override
    public SysUser updateUser(SysUser user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUser'");
    }

    @Override
    public void deleteUserByUserIds(List<Long> ids) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteUserByUserIds'");
    }

    @Override
    public SysUser updateUserStatus(SysUser user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'updateUserStatus'");
    }

    @Override
    public void resetPwd(SysUser user) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'resetPwd'");
    }
}
