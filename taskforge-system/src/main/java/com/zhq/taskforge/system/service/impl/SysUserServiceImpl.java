package com.zhq.taskforge.system.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zhq.taskforge.common.annotation.DataScope;
import com.zhq.taskforge.common.core.domain.entity.SysUser;
import com.zhq.taskforge.common.datascope.DataScopeContext;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.SecurityUtils;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.system.domain.SysUserRole;
import com.zhq.taskforge.system.mapper.SysUserMapper;
import com.zhq.taskforge.system.mapper.SysUserRoleMapper;
import com.zhq.taskforge.system.service.ISysUserService;


@Service
public class SysUserServiceImpl
        extends ServiceImpl<SysUserMapper, SysUser>
        implements ISysUserService {

    private final SysConfigServiceImpl sysConfigServiceImpl;

    private final SysRoleServiceImpl sysRoleServiceImpl;

    @Autowired
    SysUserMapper sysUserMapper;

    @Autowired
    SysUserRoleMapper sysUserRoleMapper;

    SysUserServiceImpl(SysRoleServiceImpl sysRoleServiceImpl, SysConfigServiceImpl sysConfigServiceImpl) {
        this.sysRoleServiceImpl = sysRoleServiceImpl;
        this.sysConfigServiceImpl = sysConfigServiceImpl;
    }

    @Override
    public SysUser getUserByName(String name) {
        return this.lambdaQuery().eq(SysUser::getUserName, name).one();
    }

    @Override
    @DataScope(deptAlias = "",userAlias ="",permission = "system:user:list")
    public Page<SysUser> selectUserPage(Long PageNum, Long PageSizes, SysUser sysUser) {
        Page<SysUser> page = new Page<>(PageNum, PageSizes);
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<>();
        String scope = DataScopeContext.get();
        if(StringUtils.isNotEmpty(scope)){
            qw.apply(scope);
        }

        if (sysUser != null) {
            // 根据用户名查询
            if (StringUtils.isNotEmpty(sysUser.getUserName())) {
                qw.like(SysUser::getUserName, sysUser.getUserName());
            }
            // 根据状态查询
            if (sysUser.getStatus() != null) {
                qw.eq(SysUser::getStatus, sysUser.getStatus());
            }
            // 根据部门查询
            if (sysUser.getDeptId() != null) {
                qw.eq(SysUser::getDeptId, sysUser.getDeptId());
            }
        }

        qw.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> selectPage = sysUserMapper.selectPage(page, qw);
        selectPage.getRecords().forEach(u -> u.setPassword(null));
        return selectPage;
    }

    @Override
    public boolean checkUserNameUnique(SysUser user) {
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUserName, user.getUserName());
        if (user.getUserId() != null) {
            qw.ne(SysUser::getUserId, user.getUserId());
        }
        return sysUserMapper.selectCount(qw) == 0;
    }

    @Override
    public boolean checkPhoneUnique(SysUser user) {
        if (!StringUtils.isNotEmpty(user.getPhonenumber())) {
            return true;
        }
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhonenumber, user.getPhonenumber());
        if (user.getUserId() != null) {
            qw.ne(SysUser::getUserId, user.getUserId());
        }
        return sysUserMapper.selectCount(qw) == 0;
    }

    @Override
    public boolean checkUserAllowed(SysUser user) {
        // 无法对超级管理员进行操作
        if (user.getUserId() != null && user.getUserId() == 1L) {
            throw new ServiceException("无法对super admin 进行操作");
        }
        return true;
    }

    @Override
    @Transactional
    public void insertUser(SysUser user) {
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        sysUserMapper.insert(user);
        insertUserRole(user.getUserId(), user.getRoleIds());
    }

    private void insertUserRole(Long userId, Long[] roleIds) {
        if (roleIds == null)
            return;
        for (Long roleId : roleIds) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setRoleId(roleId);
            sysUserRole.setUserId(userId);
            sysUserRoleMapper.insert(sysUserRole);
        }
    }

    @Override
    @Transactional
    public SysUser updateUser(SysUser user) {
        checkUserAllowed(user);
        // 改密码我们只能通过resetPwd来进行修改，编辑时我们不进行修改
        user.setPassword(null);

        user.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(user);
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getUserId()));

        insertUserRole(user.getUserId(), user.getRoleIds());

        // 返回更新后的user
        user.setPassword(null);
        return user;

    }

    @Override
    @Transactional
    public void deleteUserByUserIds(List<Long> ids) {

        // 1.判断是否为超级管理员
        for (Long id : ids) {
            SysUser checkUser = new SysUser();
            checkUser.setUserId(id);
            checkUserAllowed(checkUser);
        }

        // 2.批量删除user_role
        sysUserRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>()
                .in(SysUserRole::getUserId, ids));

        // 3.调用mapper层方法，走逻辑删除
        this.removeByIds(ids);
    }

    @Override
    public SysUser updateUserStatus(SysUser user) {
        checkUserAllowed(user);
        SysUser update = new SysUser();
        update.setUserId(user.getUserId());
        update.setStatus(user.getStatus());
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(update);
        return update;
    }

    @Override
    public void resetPwd(SysUser user) {
        checkUserAllowed(user);
        SysUser update = new SysUser();
        update.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        update.setUserId(user.getUserId());
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(update);
    }

    private Long[] selectRoleIdsByUserId(Long userId) {
        List<SysUserRole> list = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        return list.stream().map(SysUserRole::getRoleId).toArray(Long[]::new);
    }

    @Override
    public SysUser selectUserById(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        user.setRoleIds(selectRoleIdsByUserId(userId));
        user.setPassword(null);
        return user;
    }

    @Override
    public boolean checkEmailUnique(SysUser user) {
        if (!StringUtils.isNotEmpty(user.getEmail())) {
            return true;
        }
        LambdaQueryWrapper<SysUser> qw = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getEmail, user.getEmail());
        if (user.getUserId() != null) {
            qw.ne(SysUser::getUserId, user.getUserId());
        }
        return sysUserMapper.selectCount(qw) == 0;
    }

    @Override
    public int updateProfile(SysUser sysUser) {
        // 只拼允许改的字段；空串/null 不写库，避免 Swagger 只改手机号时把邮箱等刷空
        SysUser update = new SysUser();
        update.setUserId(sysUser.getUserId());
        if (StringUtils.isNotEmpty(sysUser.getNickName())) {
            update.setNickName(sysUser.getNickName());
        }
        if (StringUtils.isNotEmpty(sysUser.getEmail())) {
            update.setEmail(sysUser.getEmail());
        }
        if (StringUtils.isNotEmpty(sysUser.getPhonenumber())) {
            update.setPhonenumber(sysUser.getPhonenumber());
        }
        if (StringUtils.isNotEmpty(sysUser.getSex())) {
            update.setSex(sysUser.getSex());
        }
        update.setUpdateTime(LocalDateTime.now());
        return sysUserMapper.updateById(update);
    }

    @Override
    public String updateUserPassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null) {
            throw new ServiceException("用户未登录");
        }
        if (!StringUtils.isNotEmpty(oldPassword) || !StringUtils.isNotEmpty(newPassword)) {
            throw new ServiceException("旧密码和新密码不能为空");
        }

        // 以数据库为准做校验（selectUserById 会清空密码，这里直接查库）
        SysUser dbUser = sysUserMapper.selectById(userId);
        if (dbUser == null) {
            throw new ServiceException("用户不存在");
        }

        String passwordHash = dbUser.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, passwordHash)) {
            throw new ServiceException("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, passwordHash)) {
            throw new ServiceException("修改密码失败，新密码不能和旧密码相同");
        }

        // 只加密一次，库与缓存共用
        String encrypted = SecurityUtils.encryptPassword(newPassword);
        SysUser update = new SysUser();
        update.setUserId(userId);
        update.setPassword(encrypted);
        update.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(update);
        return encrypted;
    }

}
