package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.dto.DashboardVO;
import com.guoshun.devsecai.entity.Plugin;
import com.guoshun.devsecai.entity.Project;
import com.guoshun.devsecai.entity.ScanResult;
import com.guoshun.devsecai.mapper.PluginMapper;
import com.guoshun.devsecai.mapper.ProjectMapper;
import com.guoshun.devsecai.mapper.ScanResultMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DashboardService {
    
    private final ProjectMapper projectMapper;
    private final PluginMapper pluginMapper;
    private final ScanResultMapper scanResultMapper;
    
    public DashboardService(ProjectMapper projectMapper, PluginMapper pluginMapper,
                           ScanResultMapper scanResultMapper) {
        this.projectMapper = projectMapper;
        this.pluginMapper = pluginMapper;
        this.scanResultMapper = scanResultMapper;
    }
    
    public DashboardVO getDashboard() {
        DashboardVO vo = new DashboardVO();
        
        // 项目统计
        vo.setTotalProjects(projectMapper.selectCount(null));
        vo.setActiveProjects(projectMapper.selectCount(
            new LambdaQueryWrapper<Project>().eq(Project::getStatus, 0)
        ));
        
        // 插件统计
        vo.setTotalPlugins(pluginMapper.selectCount(null));
        vo.setActivePlugins(pluginMapper.selectCount(
            new LambdaQueryWrapper<Plugin>().eq(Plugin::getStatus, 1)
        ));
        
        // 今日扫描统计
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LambdaQueryWrapper<ScanResult> wrapper = new LambdaQueryWrapper<ScanResult>()
            .ge(ScanResult::getCreateTime, startOfDay);
        vo.setTodayScans(scanResultMapper.selectCount(wrapper));
        
        // 问题统计
        Page<ScanResult> page = new Page<>(1, 1000);
        scanResultMapper.selectPage(page, new LambdaQueryWrapper<ScanResult>()
            .eq(ScanResult::getStatus, 1).orderByDesc(ScanResult::getCreateTime));
        
        long totalIssues = 0, critical = 0, high = 0, medium = 0, low = 0;
        for (ScanResult result : page.getRecords()) {
            totalIssues += result.getTotalIssues() != null ? result.getTotalIssues() : 0;
            critical += result.getCriticalCount() != null ? result.getCriticalCount() : 0;
            high += result.getHighCount() != null ? result.getHighCount() : 0;
            medium += result.getMediumCount() != null ? result.getMediumCount() : 0;
            low += result.getLowCount() != null ? result.getLowCount() : 0;
        }
        
        vo.setTotalIssues(totalIssues);
        vo.setCriticalIssues(critical);
        vo.setHighIssues(high);
        vo.setMediumIssues(medium);
        vo.setLowIssues(low);
        
        return vo;
    }
}
