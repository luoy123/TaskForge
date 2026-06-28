package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.model.system.entity.SysDictData;
import com.zhq.taskforge.model.system.mapper.SysDictDataMapper;
import com.zhq.taskforge.model.system.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysDictDataServiceImpl implements SysDictDataService {

    @Autowired
    private SysDictDataMapper sysDictDataMapper;
    @Override
    public Page<SysDictData> selectDictDataList(Long pageNum, Long pageSize, SysDictData dictData) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysDictData> qw = new LambdaQueryWrapper<>();

        if(StringUtils.hasText(dictData.getDictType())){
            qw.eq(SysDictData::getDictType, dictData.getDictType());
        }
        if(StringUtils.hasText(dictData.getDictValue())){
            qw.eq(SysDictData::getDictValue, dictData.getDictValue());
        }
        if(StringUtils.hasText(dictData.getDictLabel())){
            qw.like(SysDictData::getDictLabel, dictData.getDictLabel());
        }
        if(dictData.getStatus()!=null){
            qw.eq(SysDictData::getStatus, dictData.getStatus());
        }
        qw.orderByAsc(SysDictData::getDictSort);
        return sysDictDataMapper.selectPage(page,qw);
    }

    @Override
    public List<SysDictData> selectDictDataByType(String dictType) {
        LambdaQueryWrapper<SysDictData> qw = new LambdaQueryWrapper<>();
        qw.eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, "0")
                .orderByAsc(SysDictData::getDictSort);
        return sysDictDataMapper.selectList(qw);
    }

    @Override
    public String selectDictLabel(String dictType, String dictValue) {
        LambdaQueryWrapper<SysDictData> qw = new LambdaQueryWrapper<>();
        qw.eq(SysDictData::getDictType, dictType)
                .eq(SysDictData::getStatus, "0")
                .eq(SysDictData::getDictValue, dictValue)
                .select(SysDictData::getDictLabel);
        SysDictData sysDictData = sysDictDataMapper.selectOne(qw);
        return sysDictData != null ? sysDictData.getDictLabel():null;
    }

    @Override
    public SysDictData selectDictDataByDictCode(Long dictCode) {
        return sysDictDataMapper.selectById(dictCode);
    }

    @Override
    public int insertDictData(SysDictData dictData) {
        dictData.setCreateTime(LocalDateTime.now());
        return sysDictDataMapper.insert(dictData);
    }

    @Override
    public int updateDictData(SysDictData dictData) {
        dictData.setUpdateTime(LocalDateTime.now());
        return sysDictDataMapper.updateById(dictData);
    }

    @Override
    public int deleteDictDataById(List<Long> dictDataIds) {
        return sysDictDataMapper.deleteBatchIds(dictDataIds);
    }
}
