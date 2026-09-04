package com.zhq.taskforge.system.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.common.annotation.DataScope;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.common.core.domain.entity.SysDept;
import com.zhq.taskforge.common.datascope.DataScopeContext;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.common.utils.StringUtils;
import com.zhq.taskforge.system.mapper.SysDeptMapper;
import com.zhq.taskforge.system.service.ISysDeptService;

import cn.hutool.core.convert.Convert;

@Service
public class SysDeptServiceImpl implements ISysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    @DataScope(deptAlias="",userAlias="",permission="system:dept:list")
    public List<SysDept> selectDeptList(SysDept dept) {
        LambdaQueryWrapper<SysDept> qw = new LambdaQueryWrapper<>();
        String scope = DataScopeContext.get();
        if(StringUtils.isNotEmpty(scope)){
            qw.apply(scope);
        }
        qw.eq(SysDept::getDelFlag, 0)
                .orderByAsc(SysDept::getOrderNum);
        if (!StringUtils.isEmpty(dept.getDeptName())) {
            qw.like(SysDept::getDeptName, dept.getDeptName());
        }
        if (dept.getStatus() != null) {
            qw.eq(SysDept::getStatus, dept.getStatus());
        }
        List<SysDept> sysDepts = sysDeptMapper.selectList(qw);
        return buildDeptTree(sysDepts);
    }

    private List<SysDept> buildDeptTree(List<SysDept> sysDepts) {
        List<SysDept> returnList = new ArrayList<>();
        List<Long> deptIds = sysDepts.stream()
                .map(SysDept::getDeptId)
                .collect(Collectors.<Long>toList());

        for (SysDept sysDept : sysDepts) {
            if (!deptIds.contains(sysDept.getParentId())) {
                recursionFn(sysDepts, sysDept);
                returnList.add(sysDept);
            }
        }
        if (returnList.isEmpty()) {
            returnList = sysDepts;
        }
        return returnList;

    }

    private void recursionFn(List<SysDept> sysDepts, SysDept sysDept) {
        List<SysDept> childList = getChildrenList(sysDepts, sysDept);
        sysDept.setChildren(childList);
        for (SysDept sysChild : childList) {
            if (hasChild(sysDepts, sysChild)) {
                recursionFn(sysDepts, sysChild);
            }
        }
    }

    private List<SysDept> getChildrenList(List<SysDept> sysDepts, SysDept sysDept) {
        List<SysDept> tList = new ArrayList<>();
        for (SysDept n : sysDepts) {
            if (n.getParentId() != null && n.getParentId().equals(sysDept.getDeptId())) {
                tList.add(n);
            }
        }
        return tList;
    }

    private Boolean hasChild(List<SysDept> sysDepts, SysDept sysDept) {
        return getChildrenList(sysDepts, sysDept).size() > 0;
    }

    @Override
    public SysDept selectDeptById(Long deptId) {
        return sysDeptMapper.selectById(deptId);
    }

    @Override
    public int insertDept(SysDept dept) {
        SysDept sysParentDept = sysDeptMapper.selectById(dept.getParentId());
        if (UserConstants.DEPT_NORMAL.equals(sysParentDept.getStatus())) {
            throw new ServiceException("部门停用，无法进行新增");
        }

        dept.setAncestors(sysParentDept.getAncestors() + "," + dept.getParentId());
        dept.setCreateTime(LocalDateTime.now());

        return sysDeptMapper.insert(dept);
    }

    @Override
    public int updateDept(SysDept dept) {
        SysDept newParentDept = sysDeptMapper.selectById(dept.getParentId());
        SysDept oldDept = sysDeptMapper.selectById(dept.getDeptId());

        if (newParentDept != null && oldDept != null) {
            String newAncestors = newParentDept.getAncestors() + "," + dept.getAncestors();
            String oldAncestors = oldDept.getAncestors();

            dept.setAncestors(newAncestors);

            updateDeptChildren(dept.getDeptId(), newAncestors, oldAncestors);
        }

        int result = sysDeptMapper.updateById(dept);

        if (UserConstants.DEPT_NORMAL.equals(newParentDept.getStatus())
                && dept.getAncestors() != null && !"0".equals(dept.getAncestors())) {
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    private void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        List<SysDept> children = sysDeptMapper.selectChildrenDeptById(deptId);

        for (SysDept child : children) {
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }

        if (!children.isEmpty()) {
            sysDeptMapper.updateDeptChildren(children);
        }
    }

    private void updateParentDeptStatusNormal(SysDept dept) {
        String ancestors = dept.getAncestors();

        Long[] deptIds = Convert.toLongArray(ancestors);

        sysDeptMapper.updateDeptStatusNormal(deptIds);
    }

    @Override
    public int deleteDeptById(Long deptId) {
        return sysDeptMapper.deleteById(deptId);
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
        int result = sysDeptMapper.checkDeptExistUser(deptId);
        return result > 0;
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        int result = sysDeptMapper.hasChildByDeptId(deptId);
        return result > 0;
    }
}
