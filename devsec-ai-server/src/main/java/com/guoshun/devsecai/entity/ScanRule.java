package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scan_rule")
public class ScanRule {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;           // 规则名称
    
    private String ruleKey;        // 规则唯一标识
    
    private String severity;       // 严重程度：critical-严重 high-高 medium-中 low-低 info-提示
    
    private String category;       // 类别：code_quality代码质量、security安全、best_practice最佳实践
    
    private String description;    // 描述
    
    private String pattern;        // 检测模式/正则表达式
    
    private String solution;       // 修复建议
    
    private Integer status;        // 0-禁用 1-启用
    
    private Long pluginId;         // 所属插件ID
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
