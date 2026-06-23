package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.Plugin;
import com.guoshun.devsecai.mapper.PluginMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PluginService {
    
    private final PluginMapper pluginMapper;
    
    public PluginService(PluginMapper pluginMapper) {
        this.pluginMapper = pluginMapper;
    }
    
    public Page<Plugin> getPage(Integer current, Integer size, String keyword, String category, Integer status) {
        Page<Plugin> page = new Page<>(current, size);
        LambdaQueryWrapper<Plugin> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Plugin::getName, keyword).or().like(Plugin::getPluginKey, keyword);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(Plugin::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Plugin::getStatus, status);
        }
        wrapper.orderByDesc(Plugin::getCreateTime);
        return pluginMapper.selectPage(page, wrapper);
    }
    
    public Plugin getById(Long id) {
        return pluginMapper.selectById(id);
    }
    
    public Plugin getByPluginKey(String pluginKey) {
        return pluginMapper.selectOne(new LambdaQueryWrapper<Plugin>().eq(Plugin::getPluginKey, pluginKey));
    }
    
    public Plugin create(Plugin plugin) {
        if (plugin.getPluginKey() == null || plugin.getPluginKey().isEmpty()) {
            plugin.setPluginKey("PLUGIN_" + System.currentTimeMillis());
        }
        if (plugin.getStatus() == null) {
            plugin.setStatus(1);
        }
        pluginMapper.insert(plugin);
        return plugin;
    }
    
    public void update(Plugin plugin) {
        pluginMapper.updateById(plugin);
    }
    
    public void delete(Long id) {
        pluginMapper.deleteById(id);
    }
    
    public void updateStatus(Long id, Integer status) {
        Plugin plugin = new Plugin();
        plugin.setId(id);
        plugin.setStatus(status);
        pluginMapper.updateById(plugin);
    }
    
    public void heartbeat(String pluginKey) {
        Plugin plugin = getByPluginKey(pluginKey);
        if (plugin != null) {
            plugin.setLastHeartbeat(LocalDateTime.now());
            plugin.setStatus(2); // 运行中
            pluginMapper.updateById(plugin);
        }
    }
    
    public List<Plugin> getEnabledPlugins() {
        return pluginMapper.selectList(new LambdaQueryWrapper<Plugin>().eq(Plugin::getStatus, 1));
    }
    
    public void updateConfig(Long id, String config) {
        Plugin plugin = new Plugin();
        plugin.setId(id);
        plugin.setConfig(config);
        pluginMapper.updateById(plugin);
    }
}
