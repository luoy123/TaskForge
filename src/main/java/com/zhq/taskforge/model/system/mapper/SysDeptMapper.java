package com.zhq.taskforge.model.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.model.system.entity.SysDept;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysDeptMapper extends BaseMapper<SysDept> {

    /**
     * 查询所有子部门
     */

    List<SysDept> selectChildrenDeptById( Long deptId);

    /**
     * 批量更新子部门的租组件列表
     */
    int updateDeptChildren(List<SysDept> depts);

    /**
     * 批量启用部门
     */
    void updateDeptStatusNormal(Long[] deptIds);

    /**
     * 检查是否又子部门
     */
    int hasChildByDeptId(Long deptId);

    /**
     * 检查部门下是否有用户
     */
    int checkDeptExistUser(Long  deptId);
}
