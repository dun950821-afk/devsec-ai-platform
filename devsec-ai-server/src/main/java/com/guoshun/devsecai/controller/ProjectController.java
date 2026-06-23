package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.Project;
import com.guoshun.devsecai.entity.User;
import com.guoshun.devsecai.service.ProjectService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/project")
public class ProjectController {
    
    private final ProjectService projectService;
    
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }
    
    @GetMapping("/list")
    public Result<Page<Project>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(projectService.getPage(current, size, keyword, status));
    }
    
    @GetMapping("/{id}")
    public Result<Project> getById(@PathVariable Long id) {
        return Result.success(projectService.getById(id));
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody Project project, @AuthenticationPrincipal User user) {
        projectService.create(project, user.getId());
        return Result.success("创建成功", null);
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Project project) {
        project.setId(id);
        projectService.update(project);
        return Result.success("更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectService.delete(id);
        return Result.success("删除成功", null);
    }
    
    @PutMapping("/{id}/policy")
    public Result<Void> bindPolicy(@PathVariable Long id, @RequestParam Long policyId) {
        projectService.bindPolicy(id, policyId);
        return Result.success("策略绑定成功", null);
    }
    
    @PutMapping("/{id}/scan")
    public Result<Void> toggleScan(@PathVariable Long id, @RequestParam Integer enabled) {
        projectService.toggleScan(id, enabled);
        return Result.success("扫描设置成功", null);
    }
}
