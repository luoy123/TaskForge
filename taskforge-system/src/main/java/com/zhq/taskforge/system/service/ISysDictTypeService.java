package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysDictType;

import java.util.List;

public interface ISysDictTypeService {

    Page<SysDictType> selectDictTypeList(Long pageNum,Long pageSize,SysDictType dictType);

    SysDictType selectDictTypeById(Long dictTypeId);

    int insertDictType(SysDictType dictType);

    int updateDictType(SysDictType dictType);

    int deleteDictTypeById(List<Long> dictTypeIds);

    String checkDictTypeUnique(SysDictType dictType);
}
