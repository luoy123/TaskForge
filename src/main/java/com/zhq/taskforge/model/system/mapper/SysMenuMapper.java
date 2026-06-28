package com.zhq.taskforge.model.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.model.system.entity.SysMenu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysMenuMapper extends BaseMapper<SysMenu> {


    /**
     * 通过用户id查询权限列表
     * @param userId
     * @return
     */
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    /**
     * 查询所有权限列表（管理员使用）
     * @return
     */
    List<String> selectPermsAll();

    List<SysMenu> selectMenuTreeByUserId(@Param("userId") Long userId);

    List<SysMenu> selectMenuTreeAll();

}
