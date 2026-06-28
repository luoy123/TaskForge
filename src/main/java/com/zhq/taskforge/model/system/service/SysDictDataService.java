package com.zhq.taskforge.model.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.model.system.entity.SysDictData;

import java.util.List;

public interface SysDictDataService {
    /**
     * 分页查询
     */
    Page<SysDictData> selectDictDataList(Long pageNum, Long pageSize, SysDictData dictData);

    /**
     * 根据字典类型获取详情，用户前端下拉框展示
     */
    List<SysDictData> selectDictDataByType(String dictType);

    /**
     * 根据值 + 类型查询标签
     */
    String selectDictLabel(String dictType, String dictValue);

    /**
     * 根据id查询详情
     */
    SysDictData selectDictDataByDictCode(Long dictCode);

    /**
     * 添加
     */
    int insertDictData(SysDictData dictData);

    /**
     * 修改
     */
    int updateDictData(SysDictData dictData);

    /**
     * 删除
     */
    int deleteDictDataById(List<Long> dictDataIds);

}
