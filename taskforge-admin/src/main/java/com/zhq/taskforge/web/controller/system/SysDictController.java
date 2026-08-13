package com.zhq.taskforge.web.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.system.domain.SysDictData;
import com.zhq.taskforge.system.domain.SysDictType;
import com.zhq.taskforge.system.service.ISysDictDataService;
import com.zhq.taskforge.system.service.ISysDictTypeService;
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
    private ISysDictTypeService sysDictTypeService;
    @Autowired
    private ISysDictDataService sysDictDataService;

    @GetMapping("/type/list")
    @Operation(summary = "分页查询字典类型列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_LIST + "')")
    public R<Page<SysDictType>> listDictType(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysDictType sysDictType) {
        Page<SysDictType> page = sysDictTypeService.selectDictTypeList(pageNum, pageSize, sysDictType);
        return R.ok(page);
    }

    @GetMapping("/type/{dictId}")
    @Operation(summary = "根据id获取详情信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_QUERY + "')")
    public R<SysDictType> queryDictType(@PathVariable Long dictId) {
        SysDictType sysDictType = sysDictTypeService.selectDictTypeById(dictId);
        return R.ok(sysDictType);
    }

    @PostMapping("/type")
    @Operation(summary = "添加字典类型")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_ADD + "')")
    public R<Void> addDictType(@RequestBody SysDictType sysDictType) {
        sysDictTypeService.insertDictType(sysDictType);
        return R.ok();
    }

    @PutMapping("/type")
    @Operation(summary = "修改字典类型")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_EDIT + "')")
    public R<Void> updateDictType(@RequestBody SysDictType sysDictType) {
        sysDictTypeService.updateDictType(sysDictType);
        return R.ok();
    }

    @DeleteMapping("/type/{dictIds}")
    @Operation(summary = "删除字典类型")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_REMOVE + "')")
    public R<Void> removeDictType(@PathVariable List<Long> dictIds) {
        sysDictTypeService.deleteDictTypeById(dictIds);
        return R.ok();
    }

    @GetMapping("/data/list")
    @Operation(summary = "分页查询字典数据列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_LIST + "')")
    public R<Page<SysDictData>> listDictData(
            @RequestParam(defaultValue = "1") Long pageNum,
            @RequestParam(defaultValue = "10") Long pageSize,
            SysDictData sysDictData) {
        Page<SysDictData> page = sysDictDataService.selectDictDataList(pageNum, pageSize, sysDictData);
        return R.ok(page);
    }

    @GetMapping("/data/{dictCode}")
    @Operation(summary = "根据dictCode获取详情信息")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_QUERY + "')")
    public R<SysDictData> queryDictData(@PathVariable Long dictCode) {
        SysDictData sysDictData = sysDictDataService.selectDictDataByDictCode(dictCode);
        return R.ok(sysDictData);
    }

    @PostMapping("/data")
    @Operation(summary = "添加字典数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_ADD + "')")
    public R<Void> addDictData(@RequestBody SysDictData sysDictData) {
        sysDictDataService.insertDictData(sysDictData);
        return R.ok();
    }

    @PutMapping("/data")
    @Operation(summary = "修改字典数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_EDIT + "')")
    public R<Void> updateDictData(@RequestBody SysDictData sysDictData) {
        sysDictDataService.updateDictData(sysDictData);
        return R.ok();
    }

    @DeleteMapping("/data/{dictCodes}")
    @Operation(summary = "删除字典数据")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.DICT_REMOVE + "')")
    public R<Void> removeDictData(@PathVariable List<Long> dictCodes) {
        sysDictDataService.deleteDictDataById(dictCodes);
        return R.ok();
    }

    @GetMapping("/data/type/{dictType}")
    @Operation(summary = "根据字典类型查询字典数据")
    public R<List<SysDictData>> dictType(@PathVariable String dictType) {
        List<SysDictData> sysDictData = sysDictDataService.selectDictDataByType(dictType);
        return R.ok(sysDictData != null ? sysDictData : new ArrayList<>());
    }
}
