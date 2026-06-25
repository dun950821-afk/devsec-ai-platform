package com.guoshun.devsecai.dto;

import lombok.Data;

import java.util.List;

@Data
public class FindingUploadRequest {
    private String pluginId;
    private String module;          // SCA / SAST / Secrets / Baseline
    
    // 单条上送时使用这些字段
    private String ruleId;
    private String severity;        // CRITICAL / HIGH / MEDIUM / LOW / INFO
    private String title;
    private String description;
    private String filePath;
    private Integer startLine;
    private Integer endLine;
    // SCA特有
    private String componentName;
    private String currentVersion;
    private String fixedVersion;
    private String vulnerabilityId;
    // SAST特有
    private String confidence;
    private String recommendation;
    private String cwe;
    private String owasp;
    private String status;          // OPEN / FIXED / IGNORED
    
    // 批量上送时使用
    private List<FindingItem> findings;
    
    @Data
    public static class FindingItem {
        private String ruleId;
        private String severity;
        private String title;
        private String description;
        private String filePath;
        private Integer startLine;
        private Integer endLine;
        private String componentName;
        private String currentVersion;
        private String fixedVersion;
        private String vulnerabilityId;
        private String confidence;
        private String recommendation;
        private String cwe;
        private String owasp;
        private String status;
    }
}
