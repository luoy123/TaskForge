package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.system.domain.SysDictData;
import com.zhq.taskforge.system.domain.SysDictType;
import com.zhq.taskforge.system.mapper.SysDictDataMapper;
import com.zhq.taskforge.system.mapper.SysDictTypeMapper;
import com.zhq.taskforge.system.service.ISysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysDictTypeServiceImpl implements ISysDictTypeService {

    @Autowired
    private SysDictTypeMapper sysDictTypeMapper;

    @Autowired
    private SysDictDataMapper sysDictDataMapper;

    @Override
    public Page<SysDictType> selectDictTypeList(Long pageNum, Long pageSize, SysDictType dictType) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictType> qw = new LambdaQueryWrapper<>();

        if(!StringUtils.isEmpty(dictType.getDictName())){
            qw.like(SysDictType::getDictName, dictType.getDictName());
        }
        if(!StringUtils.isEmpty(dictType.getDictType())){
            qw.like(SysDictType::getDictType, dictType.getDictType());
        }
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
        if (UserConstants.NOT_UNIQUE.equals(checkDictTypeUnique(dictType))) {
            throw new ServiceException("新增字典'" + dictType.getDictName() + "'失败，字典类型已存在");
        }
        dictType.setCreateTime(LocalDateTime.now());
        return sysDictTypeMapper.insert(dictType);
    }

    @Override
    public int updateDictType(SysDictType dictType) {
        if (UserConstants.NOT_UNIQUE.equals(checkDictTypeUnique(dictType))) {
            throw new ServiceException("更新字典'" + dictType.getDictName() + "'失败，字典类型已存在");
        }
        dictType.setUpdateTime(LocalDateTime.now());
        return sysDictTypeMapper.updateById(dictType);
    }

    @Override
    public int deleteDictTypeById(List<Long> dictTypeIds) {
        for (Long dictTypeId : dictTypeIds) {
            SysDictType dictType = sysDictTypeMapper.selectById(dictTypeId);
            if (dictType != null) {
                LambdaQueryWrapper<SysDictData> qw = new LambdaQueryWrapper<>();
                qw.eq(SysDictData::getDictType, dictType.getDictType());
                Long count = sysDictDataMapper.selectCount(qw);
                if (count > 0) {
                    throw new ServiceException("字典'" + dictType.getDictName() + "'已分配，不能删除");
                }
            }
        }
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
