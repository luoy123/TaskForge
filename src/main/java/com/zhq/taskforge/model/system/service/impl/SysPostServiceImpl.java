package com.zhq.taskforge.model.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.common.constants.UserConstants;
import com.zhq.taskforge.config.BusinessException;
import com.zhq.taskforge.model.system.entity.SysPost;
import com.zhq.taskforge.model.system.mapper.SysPostMapper;
import com.zhq.taskforge.model.system.service.SysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SysPostServiceImpl implements SysPostService {

    @Autowired
    private SysPostMapper sysPostMapper;

    @Override
    public Page<SysPost> selectPostList(Long pageNum, Long pageSize, SysPost sysPost) {
        Page<SysPost> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysPost>  qw= new LambdaQueryWrapper<>();

        //1.模糊查询部门编号，eg:ceo等
        if(!StringUtils.isEmpty(sysPost.getPostCode())){
            qw.like(SysPost::getPostCode, sysPost.getPostCode());
        }
        //2.模糊查询部门名称
        if(!StringUtils.isEmpty(sysPost.getPostName())){
            qw.like(SysPost::getPostName, sysPost.getPostName());
        }
        //3.查询状态
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
        //1.岗位名校验
        if(UserConstants.NOT_UNIQUE.equals(checkPostNameUnique(sysPost))){
            throw new BusinessException("添加" + sysPost.getPostName() +"岗位失败，名称已存在");
        }
        if(UserConstants.NOT_UNIQUE.equals(checkPostCodeUnique(sysPost))){
            throw new BusinessException("添加" + sysPost.getPostCode() + "失败，编码已存在");
        }
        sysPost.setCreateTime(LocalDateTime.now());
        return sysPostMapper.insert(sysPost);
    }

    @Override
    public int updatePost(SysPost sysPost) {
        //1.岗位名校验
        if(UserConstants.NOT_UNIQUE.equals(checkPostNameUnique(sysPost))){
            throw new BusinessException("添加" + sysPost.getPostName() +"岗位失败，名称已存在");
        }
        if(UserConstants.NOT_UNIQUE.equals(checkPostCodeUnique(sysPost))){
            throw new BusinessException("添加" + sysPost.getPostCode() + "失败，编码已存在");
        }
        sysPost.setUpdateTime(LocalDateTime.now());
        return sysPostMapper.updateById(sysPost);
    }

    @Override
    public int deletePostById(List<Long> postIds) {
        for(Long postId : postIds){
            //判断当前岗位是否分配给用户
            if(sysPostMapper.countUserPostById(postId) > 0){
                SysPost sysPost = selectPostById(postId);
                throw new BusinessException("岗位" + sysPost.getPostName() + "无法删除，已分配给用户");
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

        //如果两个post不同，则说明用户名被占用了，
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

        //如果两个post不同，则说明用户名被占用了，
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
