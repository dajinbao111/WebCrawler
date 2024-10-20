package com.github.dajinbao.apiserver.web;

import com.github.dajinbao.apiserver.common.model.Page;
import com.github.dajinbao.apiserver.common.model.RestResult;
import com.github.dajinbao.apiserver.entity.TaskResp;
import com.github.dajinbao.apiserver.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "任务管理")
@RestController
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @Operation(summary = "创建任务")
    @Parameters({
            @Parameter(name = "taskType", description = "任务类型（yahoo-product|yahoo-seller|mercari-product|mercari-search）", required = true),
            @Parameter(name = "taskUrl", description = "任务地址", required = true)
    })
    @PostMapping("/v1/tasks/create")
    public RestResult<Void> create(@RequestParam String taskType, @RequestParam String taskUrl) {
        service.create(taskType, taskUrl);
        return RestResult.success();
    }

    @Operation(summary = "任务重试")
    @PostMapping("/v1/tasks/retry")
    public RestResult<Void> retry(@RequestParam String taskId) {
        service.retry(taskId);
        return RestResult.success();
    }

    @Operation(summary = "任务删除")
    @DeleteMapping("/v1/tasks/delete")
    public RestResult<Void> delete(@RequestParam String taskId) {
        service.delete(taskId);
        return RestResult.success();
    }

    @Operation(summary = "更新抓取配置")
    @PostMapping("/v1/tasks/config/upsert")
    public RestResult<Void> upsert(@RequestParam("file") MultipartFile file) throws IOException {
        service.upsert(file);
        return RestResult.success();
    }

    @Operation(summary = "任务状态")
    @GetMapping("/v1/tasks/status")
    public RestResult<Map<String, Object>> status() {
        return RestResult.success(service.status());
    }

    @Operation(summary = "任务类型")
    @GetMapping("/v1/tasks/type")
    public RestResult<List<String>> type() {
        return RestResult.success(service.type());
    }


    @Operation(summary = "任务状态列表")
    @GetMapping("/v1/tasks/list")
    public RestResult<Page<TaskResp>> list(@RequestParam(required = false) String taskStatus, @RequestParam(defaultValue = "1") int pageNo, @RequestParam(defaultValue = "20") int pageSize) {
        return RestResult.success(service.list(taskStatus,pageNo, pageSize));
    }
}
