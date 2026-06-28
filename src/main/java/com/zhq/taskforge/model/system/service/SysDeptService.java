package com.zhq.taskforge.model.system.service;

import com.zhq.taskforge.model.system.entity.SysDept;

import java.util.List;

public interface SysDeptService {

    /**
     *     查询部门列表
     */
    List<SysDept> selectDeptList(SysDept dept);

    /**
     * 根据ID查询部门详情
     */
    SysDept selectDeptById(Long deptId);

    /**
     * 新增部门
     */
    int insertDept(SysDept dept);

    /**
     * 修改部门
     * @param dept
     * @return
     */
    int updateDept(SysDept dept);

    /**
     * 删除部门
     * @param deptId
     * @return
     */
    int deleteDeptById(Long deptId);

    /**
     * 部门下是否存在用户
     * @param deptId
     * @return
     */
    boolean checkDeptExistUser(Long  deptId);

    /**
     * 部门下是否存在子部门
     * @param deptId
     * @return
     */
    boolean hasChildByDeptId(Long deptId);
}
