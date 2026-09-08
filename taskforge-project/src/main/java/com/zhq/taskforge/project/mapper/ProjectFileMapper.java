package com.zhq.taskforge.project.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zhq.taskforge.project.domain.ProjectFile;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileReqVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileResVO;

public interface ProjectFileMapper extends BaseMapper<ProjectFile> {

    int softDelete(@Param("id") String id,
            @Param("deletedTime") LocalDateTime deletedTime,
            @Param("updatedBy") String updatedBy,
            @Param("updatedTime") LocalDateTime updatedTime);

    /**
     * 文件列表（G3）。SQL 已写好；筛选规则见 XML 注释。
     */
    IPage<ProjectFileResVO> queryFileList(Page<ProjectFileResVO> page, @Param("data") ProjectFileReqVO data);
}
