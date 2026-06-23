package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.ScanResult;
import com.guoshun.devsecai.service.ResultService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/result")
public class ResultController {
    
    private final ResultService resultService;
    
    public ResultController(ResultService resultService) {
        this.resultService = resultService;
    }
    
    @GetMapping("/list")
    public Result<Page<ScanResult>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long projectId,
            @RequestParam(required = false) Integer status) {
        return Result.success(resultService.getPage(current, size, projectId, status));
    }
    
    @GetMapping("/{id}")
    public Result<ScanResult> getById(@PathVariable Long id) {
        return Result.success(resultService.getById(id));
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody ScanResult result) {
        resultService.create(result);
        return Result.success("创建成功", null);
    }
    
    @PutMapping("/{id}/finish")
    public Result<Void> finishScan(@PathVariable Long id, @RequestBody ScanResult result) {
        resultService.finishScan(id, result.getStatus(), result.getCriticalCount(),
            result.getHighCount(), result.getMediumCount(), result.getLowCount(), 
            result.getInfoCount(), result.getSummary());
        return Result.success("扫描完成", null);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        resultService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }
}
