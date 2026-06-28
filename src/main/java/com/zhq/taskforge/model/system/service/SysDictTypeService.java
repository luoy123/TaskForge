package com.zhq.taskforge.model.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.model.system.entity.SysDictType;

import java.util.List;

public interface SysDictTypeService {

    /**
     * 分页查询
     */
    Page<SysDictType> selectDictTypeList(Long pageNum,Long pageSize,SysDictType dictType);

    /**
     * 获取详情
     */
    SysDictType selectDictTypeById(Long dictTypeId);

    /**
     * 添加
     */
    int insertDictType(SysDictType dictType);

    /**
     * 修改
     */
    int updateDictType(SysDictType dictType);

    /**
     * 删除
     */
    int deleteDictTypeById(List<Long> dictTypeIds);

    /**
     * 检测字典类型的唯一性
     */
    String checkDictTypeUnique(SysDictType dictType);
}
