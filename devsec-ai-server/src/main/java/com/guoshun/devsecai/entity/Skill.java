package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("skill")
public class Skill {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;           // Skill名称
    
    private String skillKey;       // Skill唯一标识
    
    private String description;    // 描述
    
    private String type;           // 类型：analyzer分析器、fixer修复器、reviewer审查器
    
    private String provider;       // 提供者：openai、claude、local等
    
    private String model;          // 模型名称
    
    private String config;        // 配置JSON
    
    private Integer status;       // 0-禁用 1-启用
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
