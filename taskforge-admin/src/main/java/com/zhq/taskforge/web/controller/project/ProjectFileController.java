package com.zhq.taskforge.web.controller.project;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.project.domain.vo.file.FileVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileIdsVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileReqVO;
import com.zhq.taskforge.project.domain.vo.file.ProjectFileResVO;
import com.zhq.taskforge.project.service.IProjectFileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 项目文件（G2 上传；G3 列表/改名/删/下载）。
 */
@RestController
@RequestMapping("/project/file")
@Tag(name = "项目文件")
public class ProjectFileController {

    @Autowired
    private IProjectFileService projectFileService;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    @Log(title = "项目文件", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_FILE_UPLOAD + "')")
    public R<FileVO> upload(@RequestParam("file") MultipartFile file,
            @RequestParam("id") String id,
            @RequestParam("type") String type) throws Exception {
        return R.ok(projectFileService.upload(file, id, type));
    }

    @PostMapping("/list")
    @Operation(summary = "文件列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_FILE_LIST + "')")
    public R<IPage<ProjectFileResVO>> list(@RequestBody ProjectFileReqVO req) {
        return R.ok(projectFileService.list(req));
    }

    @PostMapping("/rename")
    @Operation(summary = "重命名文件")
    @Log(title = "项目文件", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_FILE_RENAME + "')")
    public R<Void> rename(@RequestBody ProjectFileReqVO req) {
        projectFileService.rename(req);
        return R.ok();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除文件")
    @Log(title = "项目文件", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_FILE_DELETE + "')")
    public R<Void> delete(@RequestBody ProjectFileIdsVO req) {
        projectFileService.delete(req);
        return R.ok();
    }

    @GetMapping("/download")
    @Operation(summary = "下载文件")
    @PreAuthorize("isAuthenticated()")
    public void download(@RequestParam("fileId") String fileId, HttpServletResponse response) throws Exception {
        projectFileService.download(fileId, response);
    }
}
