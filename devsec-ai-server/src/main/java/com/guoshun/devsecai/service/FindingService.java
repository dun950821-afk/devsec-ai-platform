package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.dto.FindingUploadRequest;
import com.guoshun.devsecai.entity.PluginFinding;
import com.guoshun.devsecai.mapper.PluginFindingMapper;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class FindingService {

    private final PluginFindingMapper findingMapper;

    public FindingService(PluginFindingMapper findingMapper) {
        this.findingMapper = findingMapper;
    }

    public Page<PluginFinding> getPage(Integer current, Integer size, String keyword, String severity, String module, String status) {
        Page<PluginFinding> page = new Page<>(current, size);
        LambdaQueryWrapper<PluginFinding> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(PluginFinding::getTitle, keyword)
                    .or().like(PluginFinding::getRuleId, keyword)
                    .or().like(PluginFinding::getFilePath, keyword));
        }
        if (severity != null && !severity.isEmpty()) {
            wrapper.eq(PluginFinding::getSeverity, severity);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(PluginFinding::getModule, module);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PluginFinding::getStatus, status);
        }
        wrapper.orderByDesc(PluginFinding::getCreateTime);
        return findingMapper.selectPage(page, wrapper);
    }

    public PluginFinding getById(Long id) {
        return findingMapper.selectById(id);
    }

    public List<PluginFinding> upload(FindingUploadRequest request) {
        java.util.List<PluginFinding> results = new java.util.ArrayList<>();
        
        if (request.getFindings() != null && !request.getFindings().isEmpty()) {
            // 批量上送模式
            for (FindingUploadRequest.FindingItem item : request.getFindings()) {
                PluginFinding finding = new PluginFinding();
                finding.setPluginId(request.getPluginId());
                finding.setModule(request.getModule());
                finding.setRuleId(item.getRuleId());
                finding.setSeverity(item.getSeverity());
                finding.setTitle(item.getTitle());
                finding.setDescription(item.getDescription());
                finding.setFilePath(item.getFilePath());
                finding.setStartLine(item.getStartLine());
                finding.setEndLine(item.getEndLine());
                finding.setComponentName(item.getComponentName());
                finding.setCurrentVersion(item.getCurrentVersion());
                finding.setFixedVersion(item.getFixedVersion());
                finding.setVulnerabilityId(item.getVulnerabilityId());
                finding.setConfidence(item.getConfidence());
                finding.setRecommendation(item.getRecommendation());
                finding.setCwe(item.getCwe());
                finding.setOwasp(item.getOwasp());
                finding.setStatus(item.getStatus() != null ? item.getStatus() : "OPEN");
                findingMapper.insert(finding);
                results.add(finding);
            }
        } else {
            // 单条上送模式
            PluginFinding finding = new PluginFinding();
            finding.setPluginId(request.getPluginId());
            finding.setModule(request.getModule());
            finding.setRuleId(request.getRuleId());
            finding.setSeverity(request.getSeverity());
            finding.setTitle(request.getTitle());
            finding.setDescription(request.getDescription());
            finding.setFilePath(request.getFilePath());
            finding.setStartLine(request.getStartLine());
            finding.setEndLine(request.getEndLine());
            finding.setComponentName(request.getComponentName());
            finding.setCurrentVersion(request.getCurrentVersion());
            finding.setFixedVersion(request.getFixedVersion());
            finding.setVulnerabilityId(request.getVulnerabilityId());
            finding.setConfidence(request.getConfidence());
            finding.setRecommendation(request.getRecommendation());
            finding.setCwe(request.getCwe());
            finding.setOwasp(request.getOwasp());
            finding.setStatus(request.getStatus() != null ? request.getStatus() : "OPEN");
            findingMapper.insert(finding);
            results.add(finding);
        }
        return results;
    }

    public void updateStatus(Long id, String status) {
        PluginFinding finding = new PluginFinding();
        finding.setId(id);
        finding.setStatus(status);
        findingMapper.updateById(finding);
    }

    public Map<String, Long> getSeverityCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("CRITICAL", 0L);
        counts.put("HIGH", 0L);
        counts.put("MEDIUM", 0L);
        counts.put("LOW", 0L);
        List<PluginFinding> all = findingMapper.selectList(null);
        for (PluginFinding f : all) {
            String sev = f.getSeverity();
            if (sev != null && counts.containsKey(sev)) {
                counts.put(sev, counts.get(sev) + 1);
            }
        }
        return counts;
    }
}
