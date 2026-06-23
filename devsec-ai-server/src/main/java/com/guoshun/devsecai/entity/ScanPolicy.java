package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("scan_policy")
public class ScanPolicy {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;           // 策略名称
    
    private String description;    // 描述
    
    private String ruleIds;        // 启用的规则ID列表，逗号分隔
    
    private String pluginIds;      // 启用的插件ID列表，逗号分隔
    
    private Integer scanType;      // 0-实时扫描 1-定时扫描 2-手动触发
    
    private String cronExpression; // Cron表达式（定时扫描时使用）
    
    private Long userId;           // 创建人ID
    
    private Integer isDefault;     // 是否为默认策略 0-否 1-是
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
