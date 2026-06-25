package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("PLUGIN_FINDING")
public class PluginFinding {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String pluginId;        // 来源插件实例ID

    private String module;          // 检测模块：SCA/SAST/Secrets/Baseline

    private String ruleId;          // 规则ID

    private String severity;        // 严重等级：CRITICAL/HIGH/MEDIUM/LOW/INFO

    private String title;           // 漏洞标题

    private String description;     // 漏洞描述

    private String filePath;        // 文件路径

    private Integer startLine;      // 起始行

    private Integer endLine;        // 结束行

    // SCA特有字段
    private String componentName;   // 组件名称

    private String currentVersion;  // 当前版本

    private String fixedVersion;    // 修复版本

    private String vulnerabilityId; // 漏洞编号，如 CVE-2021-44228

    // SAST特有字段
    private String confidence;      // 置信度：HIGH/MEDIUM/LOW

    private String recommendation;  // 修复建议

    private String cwe;             // CWE编号

    private String owasp;           // OWASP分类

    private String status;          // 状态：OPEN/FIXED/IGNORED/CONFIRMED

    private Long projectId;         // 所属项目ID

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
