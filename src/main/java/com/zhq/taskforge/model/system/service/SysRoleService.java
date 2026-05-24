package com.zhq.taskforge.model.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.model.system.entity.SysRole;

import java.util.List;

public interface SysRoleService  {
    void addRole(SysRole role);

    void updateRole(SysRole sysRole);

    void changeStatus(SysRole sysRole);

    SysRole getDetailsById(Long roleId);

    void deleteRole(List<Long> roleIds);

    List<SysRole> optionSelect();
    Page<SysRole> list(Long PageNum,Long PageSizes,SysRole sysRole);
}
