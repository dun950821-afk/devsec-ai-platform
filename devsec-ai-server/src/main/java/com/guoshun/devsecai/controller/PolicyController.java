package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.ScanPolicy;
import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.service.PolicyService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policy")
public class PolicyController {
    
    private final PolicyService policyService;
    
    public PolicyController(PolicyService policyService) {
        this.policyService = policyService;
    }
    
    @GetMapping("/list")
    public Result<Page<ScanPolicy>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword) {
        return Result.success(policyService.getPage(current, size, keyword));
    }
    
    @GetMapping("/{id}")
    public Result<ScanPolicy> getById(@PathVariable Long id) {
        return Result.success(policyService.getById(id));
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody ScanPolicy policy, @AuthenticationPrincipal User user) {
        policyService.create(policy, user.getId());
        return Result.success("创建成功", null);
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ScanPolicy policy) {
        policy.setId(id);
        policyService.update(policy);
        return Result.success("更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        policyService.delete(id);
        return Result.success("删除成功", null);
    }
    
    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        policyService.setDefault(id);
        return Result.success("设置默认成功", null);
    }
}
