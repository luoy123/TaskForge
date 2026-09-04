package com.zhq.taskforge.system.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.core.domain.entity.SysRole;

public interface ISysRoleService {
    void addRole(SysRole role);

    void updateRole(SysRole sysRole);

    void changeStatus(SysRole sysRole);

    SysRole getDetailsById(Long roleId);

    void deleteRole(List<Long> roleIds);

    List<SysRole> optionSelect();

    Page<SysRole> list(Long PageNum, Long PageSizes, SysRole sysRole);

    List<SysRole> selectRolesByUserId(Long userId);

    void authDataScope(SysRole role);

}
