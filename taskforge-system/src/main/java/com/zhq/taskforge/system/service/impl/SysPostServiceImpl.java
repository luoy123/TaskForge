package com.zhq.taskforge.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.system.domain.SysPost;
import com.zhq.taskforge.system.mapper.SysPostMapper;
import com.zhq.taskforge.system.service.ISysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysPostServiceImpl implements ISysPostService {

    @Autowired
    private SysPostMapper sysPostMapper;

    @Override
    public Page<SysPost> selectPostList(Long pageNum, Long pageSize, SysPost sysPost) {
        Page<SysPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysPost>  qw= new LambdaQueryWrapper<>();

        if(!StringUtils.isEmpty(sysPost.getPostCode())){
            qw.like(SysPost::getPostCode, sysPost.getPostCode());
        }
        if(!StringUtils.isEmpty(sysPost.getPostName())){
            qw.like(SysPost::getPostName, sysPost.getPostName());
        }
        if(sysPost.getStatus() != null){
            qw.eq(SysPost::getStatus, sysPost.getStatus());
        }
        qw.orderByAsc(SysPost::getPostId);
        return sysPostMapper.selectPage(page, qw);
    }

    @Override
    public SysPost selectPostById(Long id) {
        return sysPostMapper.selectById(id);
    }

    @Override
    public int insertPost(SysPost sysPost) {
        if(UserConstants.NOT_UNIQUE.equals(checkPostNameUnique(sysPost))){
            throw new ServiceException("添加" + sysPost.getPostName() +"岗位失败，名称已存在");
        }
        if(UserConstants.NOT_UNIQUE.equals(checkPostCodeUnique(sysPost))){
            throw new ServiceException("添加" + sysPost.getPostCode() + "失败，编码已存在");
        }
        sysPost.setCreateTime(LocalDateTime.now());
        return sysPostMapper.insert(sysPost);
    }

    @Override
    public int updatePost(SysPost sysPost) {
        if(UserConstants.NOT_UNIQUE.equals(checkPostNameUnique(sysPost))){
            throw new ServiceException("添加" + sysPost.getPostName() +"岗位失败，名称已存在");
        }
        if(UserConstants.NOT_UNIQUE.equals(checkPostCodeUnique(sysPost))){
            throw new ServiceException("添加" + sysPost.getPostCode() + "失败，编码已存在");
        }
        sysPost.setUpdateTime(LocalDateTime.now());
        return sysPostMapper.updateById(sysPost);
    }

    @Override
    public int deletePostById(List<Long> postIds) {
        for(Long postId : postIds){
            if(sysPostMapper.countUserPostById(postId) > 0){
                SysPost sysPost = selectPostById(postId);
                throw new ServiceException("岗位" + sysPost.getPostName() + "无法删除，已分配给用户");
            }
        }

        return sysPostMapper.deleteBatchIds(postIds);
    }

    @Override
    public String checkPostNameUnique(SysPost sysPost) {
       Long postId = sysPost.getPostId()==null?-1L:sysPost.getPostId();
        LambdaQueryWrapper<SysPost> qw = new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostName, sysPost.getPostName());

        SysPost info = sysPostMapper.selectOne(qw);

        if(info !=null && !info.getPostId().equals(postId)){
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public String checkPostCodeUnique(SysPost sysPost) {
        Long postId = sysPost.getPostId()==null?-1L:sysPost.getPostId();
        LambdaQueryWrapper<SysPost> qw = new LambdaQueryWrapper<SysPost>()
                .eq(SysPost::getPostCode, sysPost.getPostCode());

        SysPost info = sysPostMapper.selectOne(qw);

        if(info !=null && !info.getPostId().equals(postId)){
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    @Override
    public List<SysPost> selectPostAll() {
        LambdaQueryWrapper<SysPost> qw = new LambdaQueryWrapper<>();
        qw.orderByAsc(SysPost::getPostSort);
        return sysPostMapper.selectList(qw);
    }
}
