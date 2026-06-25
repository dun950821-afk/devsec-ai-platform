package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.PluginCapabilityPolicy;
import com.guoshun.devsecai.service.CapabilityPolicyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/capability-policy")
public class CapabilityPolicyController {

    private final CapabilityPolicyService policyService;

    public CapabilityPolicyController(CapabilityPolicyService policyService) {
        this.policyService = policyService;
    }

    @GetMapping("/list")
    public Result<Page<PluginCapabilityPolicy>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(policyService.getPage(current, size, keyword, status));
    }

    @GetMapping("/all")
    public Result<List<PluginCapabilityPolicy>> getAll() {
        return Result.success(policyService.getAll());
    }

    @GetMapping("/{id}")
    public Result<PluginCapabilityPolicy> getById(@PathVariable Long id) {
        return Result.success(policyService.getById(id));
    }

    @PostMapping
    public Result<PluginCapabilityPolicy> create(@RequestBody PluginCapabilityPolicy policy) {
        PluginCapabilityPolicy created = policyService.create(policy);
        return Result.success("创建成功", created);
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PluginCapabilityPolicy policy) {
        policy.setId(id);
        policyService.update(policy);
        return Result.success("更新成功", null);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        policyService.delete(id);
        return Result.success("删除成功", null);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        policyService.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }
}
