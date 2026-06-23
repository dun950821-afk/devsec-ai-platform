package com.guoshun.devsecai.dto;

import lombok.Data;

@Data
public class DashboardVO {
    private Long totalProjects;
    private Long activeProjects;
    private Long totalPlugins;
    private Long activePlugins;
    private Long todayScans;
    private Long totalIssues;
    private Long criticalIssues;
    private Long highIssues;
    private Long mediumIssues;
    private Long lowIssues;
}
