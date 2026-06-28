package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysDictData;
import com.zhq.taskforge.model.system.entity.SysDictType;
import com.zhq.taskforge.model.system.mapper.SysDictDataMapper;
import com.zhq.taskforge.model.system.mapper.SysDictTypeMapper;
import com.zhq.taskforge.model.system.service.SysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysDictTypeServiceImpl implements SysDictTypeService {

    @Autowired
    private SysDictTypeMapper sysDictTypeMapper;

    @Autowired
    private SysDictDataMapper sysDictDataMapper;



    @Override
    public Page<SysDictType> selectDictTypeList(Long pageNum, Long pageSize, SysDictType dictType) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> qw = new LambdaQueryWrapper<>();

        //1.字典名称模糊查询
        if(!StringUtils.isEmpty(dictType.getDictName())){
            qw.like(SysDictType::getDictName, dictType.getDictName());
        }
        //2.字典类型
        if(!StringUtils.isEmpty(dictType.getDictType())){
            qw.like(SysDictType::getDictType, dictType.getDictType());
        }
        //3.字典状态
        if(dictType.getStatus() != null){
            qw.eq(SysDictType::getStatus, dictType.getStatus());
        }

        qw.orderByAsc(SysDictType::getDictId);
        return sysDictTypeMapper.selectPage(page, qw);
    }

    @Override
    public SysDictType selectDictTypeById(Long dictTypeId) {
        return sysDictTypeMapper.selectById(dictTypeId);
    }

    @Override
    public int insertDictType(SysDictType dictType) {
        // 1. 校验字典类型唯一
        if (UserConstants.NOT_UNIQUE.equals(checkDictTypeUnique(dictType))) {
            throw new BusinessException("新增字典'" + dictType.getDictName() + "'失败，字典类型已存在");
        }
        dictType.setCreateTime(LocalDateTime.now());
        return sysDictTypeMapper.insert(dictType);
    }

    @Override
    public int updateDictType(SysDictType dictType) {
        // 1. 校验字典类型唯一
        if (UserConstants.NOT_UNIQUE.equals(checkDictTypeUnique(dictType))) {
            throw new BusinessException("更新字典'" + dictType.getDictName() + "'失败，字典类型已存在");
        }
        dictType.setUpdateTime(LocalDateTime.now());
        return sysDictTypeMapper.updateById(dictType);
    }

    @Override
    public int deleteDictTypeById(List<Long> dictTypeIds) {
        // 1. 检查字典类型下是否有字典数据
        for (Long dictTypeId : dictTypeIds) {
            SysDictType dictType = sysDictTypeMapper.selectById(dictTypeId);
            if (dictType != null) {
                // 查询 sys_dict_data 表，检查字典类型下是否有数据
                LambdaQueryWrapper<SysDictData> qw = new LambdaQueryWrapper<>();
                qw.eq(SysDictData::getDictType, dictType.getDictType());
                Long count = sysDictDataMapper.selectCount(qw);
                if (count > 0) {
                    throw new BusinessException("字典'" + dictType.getDictName() + "'已分配，不能删除");
                }
            }
        }
        // 2. 执行删除
        return sysDictTypeMapper.deleteBatchIds(dictTypeIds);
    }


    @Override
    public String checkDictTypeUnique(SysDictType dictType) {
        Long dictId = dictType.getDictId() == null ? -1L : dictType.getDictId();

        LambdaQueryWrapper<SysDictType> qw = new LambdaQueryWrapper<>();
        qw.eq(SysDictType::getDictType, dictType.getDictType());

        SysDictType info = sysDictTypeMapper.selectOne(qw);

        if (info != null && !info.getDictId().equals(dictId)) {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }


}
