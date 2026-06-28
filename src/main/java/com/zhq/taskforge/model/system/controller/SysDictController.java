package com.zhq.taskforge.model.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.Result;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.model.system.entity.SysDictData;
import com.zhq.taskforge.model.system.entity.SysDictType;
import com.zhq.taskforge.model.system.service.SysDictDataService;
import com.zhq.taskforge.model.system.service.SysDictTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/system/dict")
@Tag(name = "字典管理")
public class SysDictController {

    @Autowired
    private SysDictTypeService sysDictTypeService;
    @Autowired
    private SysDictDataService sysDictDataService;

    // ==================== 字典类型管理 ====================

    @GetMapping("/type/list")
    @Operation(summary = "分页查询字典类型列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_LIST + "')")
    public Result<Page<SysDictType>> listDictType(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysDictType sysDictType) {
        Page<SysDictType> page = sysDictTypeService.selectDictTypeList(pageNum, pageSize, sysDictType);
        return Result.success(page);
    }

    @GetMapping("/type/{dictId}")
    @Operation(summary = "根据id获取详情信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_QUERY + "')")
    public Result<SysDictType> queryDictType(@PathVariable Long dictId) {
        SysDictType sysDictType = sysDictTypeService.selectDictTypeById(dictId);
        return Result.success(sysDictType);
    }

    @PostMapping("/type")
    @Operation(summary = "添加字典类型")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_ADD + "')")
    public Result<Void> addDictType(@RequestBody SysDictType sysDictType) {
        sysDictTypeService.insertDictType(sysDictType);
        return Result.success();
    }

    @PutMapping("/type")
    @Operation(summary = "修改字典类型")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_EDIT + "')")
    public Result<Void> updateDictType(@RequestBody SysDictType sysDictType) {
        sysDictTypeService.updateDictType(sysDictType);
        return Result.success();
    }

    @DeleteMapping("/type/{dictIds}")
    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_REMOVE + "')")
    public Result<Void> removeDictType(@PathVariable List<Long> dictIds) {
        sysDictTypeService.deleteDictTypeById(dictIds);
        return Result.success();
    }

    // ==================== 字典数据管理 ====================

    @GetMapping("/data/list")
    @Operation(summary = "分页查询字典数据列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_LIST + "')")
    public Result<Page<SysDictData>> listDictData(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysDictData sysDictData) {
        Page<SysDictData> page = sysDictDataService.selectDictDataList(pageNum, pageSize, sysDictData);
        return Result.success(page);
    }

    @GetMapping("/data/{dictCode}")
    @Operation(summary = "根据dictCode获取详情信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_QUERY + "')")
    public Result<SysDictData> queryDictData(@PathVariable Long dictCode) {
        SysDictData sysDictData = sysDictDataService.selectDictDataByDictCode(dictCode);
        return Result.success(sysDictData);
    }

    @PostMapping("/data")
    @Operation(summary = "添加字典数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_ADD + "')")
    public Result<Void> addDictData(@RequestBody SysDictData sysDictData) {
        sysDictDataService.insertDictData(sysDictData);
        return Result.success();
    }

    @PutMapping("/data")
    @Operation(summary = "修改字典数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_EDIT + "')")
    public Result<Void> updateDictData(@RequestBody SysDictData sysDictData) {
        sysDictDataService.updateDictData(sysDictData);
        return Result.success();
    }

    @DeleteMapping("/data/{dictCodes}")
    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_REMOVE + "')")
    public Result<Void> removeDictData(@PathVariable List<Long> dictCodes) {
        sysDictDataService.deleteDictDataById(dictCodes);
        return Result.success();
    }

    /**
     * 根据字典类型查询字典数据（用于界面下拉框的展示）
     */
    @GetMapping("/data/type/{dictType}")
    @Operation(summary = "根据字典类型查询字典数据")
    public Result<List<SysDictData>> dictType(@PathVariable String dictType) {
        List<SysDictData> sysDictData = sysDictDataService.selectDictDataByType(dictType);
        return Result.success(sysDictData != null ? sysDictData : new ArrayList<>());
    }
}
