package com.zhq.taskforge.model.system.service.impl;

import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysDept;
import com.zhq.taskforge.model.system.mapper.SysDeptMapper;
import com.zhq.taskforge.model.system.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysDeptServiceImpl implements SysDeptService {

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Override
    public List<SysDept> selectDeptList(SysDept dept) {
       //1.获取所有合法部门
        LambdaQueryWrapper<SysDept> qw = new LambdaQueryWrapper<>();
        qw.eq(SysDept::getDelFlag,0)
                .orderByAsc(SysDept::getOrderNum);
        //2.传递了部门名称，进行模糊查询
        if(!StringUtils.isEmpty(dept.getDeptName())){
            qw.like(SysDept::getDeptName,dept.getDeptName());
        }
        //3.进行状态查询
        if(dept.getStatus() != null){
            qw.eq(SysDept::getStatus,dept.getStatus());
        }
        List<SysDept> sysDepts = sysDeptMapper.selectList(qw);
        return buildDeptTree(sysDepts);
    }

    private  List<SysDept> buildDeptTree(List<SysDept> sysDepts) {
        List<SysDept> returnList = new ArrayList<>();
        //1.获取所有部门id
        List<Long> deptIds = sysDepts.stream()
                .map(SysDept::getDeptId)
                .collect(Collectors.<Long>toList());

        for(SysDept sysDept : sysDepts){
            //如果是顶级节点，就递归设置子节点
            if(!deptIds.contains(sysDept.getParentId())){
                recursionFn(sysDepts,sysDept);
                returnList.add(sysDept);
            }
        }
        //全部都是顶级节点
        if(returnList.isEmpty()){
            returnList = sysDepts;
        }
        return returnList;

    }

    private void recursionFn(List<SysDept> sysDepts, SysDept sysDept) {
        List<SysDept> childList = getChildrenList(sysDepts,sysDept);
        sysDept.setChildren(childList);
        for(SysDept sysChild : childList) {
            if (hasChild(sysDepts, sysChild)) {
                recursionFn(sysDepts, sysChild);
            }
        }
    }

    private List<SysDept> getChildrenList(List<SysDept> sysDepts, SysDept sysDept) {
        List<SysDept> tList = new ArrayList<>();
        for(SysDept n: sysDepts){
            if(n.getParentId() != null && n.getParentId().equals(sysDept.getDeptId())){
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
        //1.查询父部门，设置祖级列表
        SysDept sysParentDept = sysDeptMapper.selectById(dept.getParentId());
        if(UserConstants.DEPT_NORMAL.equals(sysParentDept.getStatus())){
            throw new BusinessException("部门停用，无法进行新增");
        }

        //3.设置祖级列表
        dept.setAncestors(sysParentDept.getAncestors() + "," + dept.getParentId());
        dept.setCreateTime(LocalDateTime.now());

        return sysDeptMapper.insert(dept);
    }

    @Override
    public int updateDept(SysDept dept) {
        //1.获取到新的祖先列表以及旧的部门id
        SysDept newParentDept = sysDeptMapper.selectById(dept.getParentId());
        SysDept oldDept = sysDeptMapper.selectById(dept.getDeptId());

        //2.计算新的Ancestors
        if(newParentDept != null && oldDept != null){
            //获取新的ancestor
            String newAncestors = newParentDept.getAncestors() + "," + dept.getAncestors();
            //获取旧的ancestors
            String oldAncestors = oldDept.getAncestors();

            //更新当前dept
            dept.setAncestors(newAncestors);

            //更新所有子部门的ancestors,
            updateDeptChildren(dept.getDeptId(),newAncestors,oldAncestors);
        }

        int result = sysDeptMapper.updateById(dept);

        //4.如果部门开启就需要启用所有的上级部门
        if(UserConstants.DEPT_NORMAL.equals(newParentDept.getStatus())
            && dept.getAncestors() != null && !"0".equals(dept.getAncestors()))
        {
            updateParentDeptStatusNormal(dept);
        }
        return result;
    }

    private void updateDeptChildren(Long deptId, String newAncestors, String oldAncestors) {
        //1.查询所有子部门
        List<SysDept> children = sysDeptMapper.selectChildrenDeptById(deptId);

        //2.替换每个子部门的ancestors
        for(SysDept child : children){
            child.setAncestors(child.getAncestors().replaceFirst(oldAncestors, newAncestors));
        }

        //3.批量更新
        if(!children.isEmpty()){
            sysDeptMapper.updateDeptChildren(children);
        }
    }

    private void updateParentDeptStatusNormal(SysDept dept) {
        //1.获取当前部门的 ancestors
        String ancestors = dept.getAncestors();

        //2.转换成Long数组
        Long[] deptIds = Convert.toLongArray(ancestors);

        //3.批量启用
        sysDeptMapper.updateDeptStatusNormal(deptIds);
    }


    @Override
    public int deleteDeptById(Long deptId) {
        return sysDeptMapper.deleteById(deptId);
    }

    @Override
    public boolean checkDeptExistUser(Long deptId) {
       int result =  sysDeptMapper.checkDeptExistUser(deptId);
       return result > 0;
    }

    @Override
    public boolean hasChildByDeptId(Long deptId) {
        int result = sysDeptMapper.hasChildByDeptId(deptId);
        return result > 0;
    }
}
