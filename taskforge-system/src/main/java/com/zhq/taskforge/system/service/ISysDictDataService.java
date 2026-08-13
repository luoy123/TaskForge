package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysDictData;

import java.util.List;

public interface ISysDictDataService {

    Page<SysDictData> selectDictDataList(Long pageNum, Long pageSize, SysDictData dictData);

    List<SysDictData> selectDictDataByType(String dictType);

    String selectDictLabel(String dictType, String dictValue);

    SysDictData selectDictDataByDictCode(Long dictCode);

    int insertDictData(SysDictData dictData);

    int updateDictData(SysDictData dictData);

    int deleteDictDataById(List<Long> dictDataIds);

}
