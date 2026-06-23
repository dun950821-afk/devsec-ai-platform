package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.Project;
import com.guoshun.devsecai.mapper.ProjectMapper;
import org.springframework.stereotype.Service;

@Service
public class ProjectService {
    
    private final ProjectMapper projectMapper;
    
    public ProjectService(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }
    
    public Page<Project> getPage(Integer current, Integer size, String keyword, Integer status) {
        Page<Project> page = new Page<>(current, size);
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Project::getName, keyword).or().like(Project::getCode, keyword);
        }
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        wrapper.orderByDesc(Project::getCreateTime);
        return projectMapper.selectPage(page, wrapper);
    }
    
    public Project getById(Long id) {
        return projectMapper.selectById(id);
    }
    
    public void create(Project project, Long userId) {
        project.setUserId(userId);
        project.setStatus(0);
        projectMapper.insert(project);
    }
    
    public void update(Project project) {
        projectMapper.updateById(project);
    }
    
    public void delete(Long id) {
        projectMapper.deleteById(id);
    }
    
    public void bindPolicy(Long projectId, Long policyId) {
        Project project = new Project();
        project.setId(projectId);
        project.setPolicyId(policyId);
        projectMapper.updateById(project);
    }
    
    public void toggleScan(Long projectId, Integer enabled) {
        Project project = new Project();
        project.setId(projectId);
        project.setScanEnabled(enabled);
        projectMapper.updateById(project);
    }
}
