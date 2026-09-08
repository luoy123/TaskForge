package com.zhq.taskforge.web.controller.project;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.zhq.taskforge.common.annotation.Log;
import com.zhq.taskforge.common.constants.PermissionConstants;
import com.zhq.taskforge.common.core.domain.R;
import com.zhq.taskforge.common.enums.BusinessType;
import com.zhq.taskforge.common.exception.ServiceException;
import com.zhq.taskforge.project.domain.ProjectLog;
import com.zhq.taskforge.project.domain.vo.task.TaskExcelVO;
import com.zhq.taskforge.project.domain.vo.task.TaskExportVO;
import com.zhq.taskforge.project.domain.vo.task.TaskReqVO;
import com.zhq.taskforge.project.domain.vo.task.TaskResVO;
import com.zhq.taskforge.project.service.IProjectTaskService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 任务管理。
 */
@RestController
@RequestMapping("/project/task")
@Tag(name = "任务管理")
public class ProjectTaskController {

    @Autowired
    private IProjectTaskService projectTaskService;

    @PostMapping("/add")
    @Operation(summary = "新增任务")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_ADD + "')")
    public R<String> add(@RequestBody TaskReqVO req) {
        return R.ok(projectTaskService.add(req));
    }

    @PostMapping("/detail")
    @Operation(summary = "任务详情")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_DETAIL + "')")
    public R<TaskResVO> detail(@RequestBody TaskReqVO req) {
        return R.ok(projectTaskService.detail(req.getTaskId()));
    }

    @PostMapping("/list")
    @Operation(summary = "项目任务列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_LIST + "')")
    public R<IPage<TaskResVO>> list(@RequestBody TaskReqVO req) {
        return R.ok(projectTaskService.list(req));
    }

    @PostMapping("/edit")
    @Operation(summary = "编辑任务")
    @Log(title = "任务管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_EDIT + "')")
    public R<Void> edit(@RequestBody TaskReqVO req) {
        projectTaskService.edit(req);
        return R.ok();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除任务")
    @Log(title = "任务管理", businessType = BusinessType.DELETE)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_DELETE + "')")
    public R<Void> delete(@RequestBody TaskReqVO req) {
        projectTaskService.delete(req.getTaskId());
        return R.ok();
    }

    @PostMapping("/addChildTask")
    @Operation(summary = "新增子任务")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_ADD_CHILD + "')")
    public R<String> addChildTask(@RequestBody TaskReqVO req) {
        return R.ok(projectTaskService.addChildTask(req));
    }

    @PostMapping("/queryChildTask")
    @Operation(summary = "查询子任务列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_DETAIL + "')")
    public R<List<TaskResVO>> queryChildTask(@RequestBody TaskReqVO req) {
        return R.ok(projectTaskService.queryChildTask(req.getTaskId()));
    }

    @PostMapping("/addComment")
    @Operation(summary = "添加任务评论")
    @Log(title = "任务管理", businessType = BusinessType.INSERT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_ADD_COMMENT + "')")
    public R<Void> addComment(@RequestBody TaskReqVO req) {
        projectTaskService.addComment(req);
        return R.ok();
    }

    @PostMapping("/log/list")
    @Operation(summary = "任务动态/评论列表")
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_LOG_LIST + "')")
    public R<List<ProjectLog>> logList(@RequestBody TaskReqVO req) {
        return R.ok(projectTaskService.queryTaskLogList(req));
    }

    @PostMapping("/export")
    @Operation(summary = "按 id 导出任务 Excel")
    @Log(title = "任务管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_LIST + "')")
    public void export(@RequestBody TaskReqVO req, HttpServletResponse response) throws Exception {
        List<TaskExportVO> list = projectTaskService.export(req.getTaskIds());
        writeExcel(response, "任务导出", list);
    }

    @PostMapping("/exportAll")
    @Operation(summary = "导出我参与的任务 Excel")
    @Log(title = "任务管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_LIST + "')")
    public void exportAll(HttpServletResponse response) throws Exception {
        List<TaskExportVO> list = projectTaskService.exportAll();
        writeExcel(response, "我的任务", list);
    }

    @PostMapping("/import")
    @Operation(summary = "导入任务 Excel")
    @Log(title = "任务管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("hasAnyAuthority('" + PermissionConstants.PROJECT_TASK_IMPORT + "')")
    public R<String> importTask(@RequestParam("file") MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ServiceException("上传文件不能为空");
        }
        List<TaskExcelVO> rows = EasyExcel.read(file.getInputStream())
                .head(TaskExcelVO.class)
                .sheet()
                .doReadSync();
        return R.ok(projectTaskService.importTask(rows));
    }

    private void writeExcel(HttpServletResponse response, String sheetName, List<TaskExportVO> list)
            throws Exception {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode(sheetName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), TaskExportVO.class)
                .sheet(sheetName)
                .doWrite(list);
    }
}
