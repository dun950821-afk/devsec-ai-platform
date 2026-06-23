package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.entity.Plugin;
import com.guoshun.devsecai.service.PluginService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plugin")
public class PluginController {
    
    private final PluginService pluginService;
    
    public PluginController(PluginService pluginService) {
        this.pluginService = pluginService;
    }
    
    @GetMapping("/list")
    public Result<Page<Plugin>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        return Result.success(pluginService.getPage(current, size, keyword, category, status));
    }
    
    @GetMapping("/{id}")
    public Result<Plugin> getById(@PathVariable Long id) {
        return Result.success(pluginService.getById(id));
    }
    
    @GetMapping("/enabled")
    public Result<?> getEnabled() {
        return Result.success(pluginService.getEnabledPlugins());
    }
    
    @PostMapping
    public Result<Void> create(@RequestBody Plugin plugin) {
        pluginService.create(plugin);
        return Result.success("创建成功", null);
    }
    
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Plugin plugin) {
        plugin.setId(id);
        pluginService.update(plugin);
        return Result.success("更新成功", null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        pluginService.delete(id);
        return Result.success("删除成功", null);
    }
    
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        pluginService.updateStatus(id, status);
        return Result.success("更新成功", null);
    }
    
    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestParam String pluginKey) {
        pluginService.heartbeat(pluginKey);
        return Result.success("心跳成功", null);
    }
    
    @PutMapping("/{id}/config")
    public Result<Void> updateConfig(@PathVariable Long id, @RequestBody String config) {
        pluginService.updateConfig(id, config);
        return Result.success("配置更新成功", null);
    }
}
