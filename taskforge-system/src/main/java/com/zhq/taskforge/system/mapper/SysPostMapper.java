package com.zhq.taskforge.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zhq.taskforge.system.domain.SysPost;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SysPostMapper extends BaseMapper<SysPost> {
    /**
     * 查询岗位人数
     * @param postId
     * @return
     */
    int countUserPostById(Long postId);

    /**
     * 查询岗位列表通过用户id
     */
    List<Long> selectPostListByUserId(Long userid);


}
