package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("PLUGIN_CAPABILITY_POLICY")
public class PluginCapabilityPolicy {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String policyName;      // 策略名称

    private String policyVersion;   // 策略版本

    // === 检测能力组 ===
    private Boolean scaEnabled;     // SCA 开源组件检测

    private Boolean sastEnabled;    // SAST 代码安全检测

    private Boolean secretsEnabled; // Secrets 敏感信息检测

    private Boolean baselineEnabled;// Baseline 企业基线检测

    // === AI能力组 ===
    private Boolean aiEnabled;      // AI总开关

    private Boolean aiVulnExplain;  // AI漏洞解释

    private Boolean aiFixSuggestion;// AI修复建议

    private Boolean aiPatchGenerate;// AI Patch生成

    // === 提交控制组 ===
    private Boolean gitCommitCheck; // Git Commit检查

    private Boolean blockCritical;  // 高危阻断提交-Critical

    private Boolean blockHigh;      // 高危阻断提交-High

    private Boolean blockMedium;    // 中危阻断-Medium

    private Boolean blockLow;       // 低危阻断-Low

    // === 数据策略组 ===
    private Boolean autoUpload;     // 结果自动上送

    private Boolean allowCodeSnippet; // 代码片段上传

    private Boolean maskSecrets;    // 脱敏策略

    private Integer maxContextLines; // 最大上下文行数

    private Long projectId;         // 绑定项目ID（可为空表示全局策略）

    private Integer status;         // 0-禁用 1-启用

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
