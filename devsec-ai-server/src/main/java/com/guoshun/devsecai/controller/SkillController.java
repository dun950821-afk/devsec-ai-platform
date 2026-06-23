package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.Skill;
import com.guoshun.devsecai.service.SkillService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skill")
public class SkillController {
    
    private final SkillService skillService;
    
    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }
    
    @GetMapping("/list")
    public Result<Page<Skill>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type) {
        return Result.success(skillService.getPage(current, size, keyword, type));
    }
    
    @GetMapping("/{id}")
    public Result<Skill> getById(@PathVariable Long id) {
        return Result.success(skillService.getById(id));
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody Skill skill) {
        skillService.create(skill);
        return Result.success("创建成功", null);
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Skill skill) {
        skill.setId(id);
        skillService.update(skill);
        return Result.success("更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skillService.delete(id);
        return Result.success("删除成功", null);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        skillService.updateStatus(id, status);
        return Result.success("更新成功", null);
    }
}
