package com.zhq.taskforge.model.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.model.system.entity.SysPost;

import java.util.List;

public interface SysPostService {

    /**
     * 分页查询岗位列表
     * @param pageNum
     * @param pageSize
     * @param sysPost
     * @return
     */
    Page<SysPost> selectPostList(Long pageNum,Long pageSize,SysPost sysPost);

    /**
     * 根据id查询岗位详情
     * @param id
     * @return
     */
    SysPost selectPostById(Long id);

    /**
     * 添加岗位
     */
    int insertPost(SysPost sysPost);

    /**
     * 修改岗位
     */
    int updatePost(SysPost sysPost);

    /**
     * 删除岗位
     */
    int deletePostById(List<Long> postIds);

    /**
     * 检查岗位名称的唯一性
     */
    String checkPostNameUnique(SysPost sysPost);

    /**
     * 检查岗位编码的唯一性
     */
    String checkPostCodeUnique(SysPost sysPost);

    /**
     * 查询所有岗位
     */
    List<SysPost> selectPostAll();


}
