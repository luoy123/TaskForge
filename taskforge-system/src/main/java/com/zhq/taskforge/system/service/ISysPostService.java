package com.zhq.taskforge.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.system.domain.SysPost;

import java.util.List;

public interface ISysPostService {

    Page<SysPost> selectPostList(Long pageNum,Long pageSize,SysPost sysPost);

    SysPost selectPostById(Long id);

    int insertPost(SysPost sysPost);

    int updatePost(SysPost sysPost);

    int deletePostById(List<Long> postIds);

    String checkPostNameUnique(SysPost sysPost);

    String checkPostCodeUnique(SysPost sysPost);

    List<SysPost> selectPostAll();

}
