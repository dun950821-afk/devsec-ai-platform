package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.entity.ScanResult;
import com.guoshun.devsecai.mapper.ScanResultMapper;
import org.springframework.stereotype.Service;

@Service
public class ResultService {
    
    private final ScanResultMapper resultMapper;
    
    public ResultService(ScanResultMapper resultMapper) {
        this.resultMapper = resultMapper;
    }
    
    public Page<ScanResult> getPage(Integer current, Integer size, Long projectId, Integer status) {
        Page<ScanResult> page = new Page<>(current, size);
        LambdaQueryWrapper<ScanResult> wrapper = new LambdaQueryWrapper<>();
        if (projectId != null) {
            wrapper.eq(ScanResult::getProjectId, projectId);
        }
        if (status != null) {
            wrapper.eq(ScanResult::getStatus, status);
        }
        wrapper.orderByDesc(ScanResult::getCreateTime);
        return resultMapper.selectPage(page, wrapper);
    }
    
    public ScanResult getById(Long id) {
        return resultMapper.selectById(id);
    }
    
    public void create(ScanResult result) {
        resultMapper.insert(result);
    }
    
    public void update(ScanResult result) {
        resultMapper.updateById(result);
    }
    
    public void finishScan(Long id, Integer status, Integer criticalCount, Integer highCount, 
                          Integer mediumCount, Integer lowCount, Integer infoCount, String summary) {
        ScanResult result = new ScanResult();
        result.setId(id);
        result.setStatus(status);
        result.setCriticalCount(criticalCount);
        result.setHighCount(highCount);
        result.setMediumCount(mediumCount);
        result.setLowCount(lowCount);
        result.setInfoCount(infoCount);
        result.setTotalIssues(criticalCount + highCount + mediumCount + lowCount + infoCount);
        result.setSummary(summary);
        result.setEndTime(java.time.LocalDateTime.now());
        resultMapper.updateById(result);
    }
    
    public void updateStatus(Long id, Integer status) {
        ScanResult result = new ScanResult();
        result.setId(id);
        result.setStatus(status);
        resultMapper.updateById(result);
    }
}
