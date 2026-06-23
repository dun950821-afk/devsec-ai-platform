package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.ScanRule;
import com.guoshun.devsecai.service.RuleService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rule")
public class RuleController {
    
    private final RuleService ruleService;
    
    public RuleController(RuleService ruleService) {
        this.ruleService = ruleService;
    }
    
    @GetMapping("/list")
    public Result<Page<ScanRule>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String category) {
        return Result.success(ruleService.getPage(current, size, keyword, severity, category));
    }
    
    @GetMapping("/{id}")
    public Result<ScanRule> getById(@PathVariable Long id) {
        return Result.success(ruleService.getById(id));
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody ScanRule rule) {
        ruleService.create(rule);
        return Result.success("创建成功", null);
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ScanRule rule) {
        rule.setId(id);
        ruleService.update(rule);
        return Result.success("更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ruleService.delete(id);
        return Result.success("删除成功", null);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        ruleService.updateStatus(id, status);
        return Result.success("更新成功", null);
    }
}
