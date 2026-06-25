package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.dto.FindingUploadRequest;
import com.guoshun.devsecai.entity.PluginFinding;
import com.guoshun.devsecai.service.FindingService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/finding")
public class FindingController {

    private final FindingService findingService;

    public FindingController(FindingService findingService) {
        this.findingService = findingService;
    }

    @GetMapping("/list")
    public Result<Page<PluginFinding>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String status) {
        return Result.success(findingService.getPage(current, size, keyword, severity, module, status));
    }

    @GetMapping("/{id}")
    public Result<PluginFinding> getById(@PathVariable Long id) {
        return Result.success(findingService.getById(id));
    }

    @PostMapping("/upload")
    public Result<java.util.List<PluginFinding>> upload(@RequestBody FindingUploadRequest request) {
        java.util.List<PluginFinding> findings = findingService.upload(request);
        return Result.success("上送成功", findings);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        findingService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @GetMapping("/severity-counts")
    public Result<Map<String, Long>> getSeverityCounts() {
        return Result.success(findingService.getSeverityCounts());
    }
}
