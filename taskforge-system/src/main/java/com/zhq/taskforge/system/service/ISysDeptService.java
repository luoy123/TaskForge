package com.zhq.taskforge.system.service;

import com.zhq.taskforge.common.core.domain.entity.SysDept;

import java.util.List;

public interface ISysDeptService {

    List<SysDept> selectDeptList(SysDept dept);

    SysDept selectDeptById(Long deptId);

    int insertDept(SysDept dept);

    int updateDept(SysDept dept);

    int deleteDeptById(Long deptId);

    boolean checkDeptExistUser(Long  deptId);

    boolean hasChildByDeptId(Long deptId);
}
