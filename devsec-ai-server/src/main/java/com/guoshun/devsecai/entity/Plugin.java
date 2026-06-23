package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("plugin")
public class Plugin {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;           // 插件名称
    
    private String pluginKey;      // 插件唯一标识
    
    private String version;        // 版本号
    
    private String description;    // 描述
    
    private String category;       // 类别：code_scanner代码扫描、dependency_dependency依赖检查、secret_detection密钥检测
    
    private Integer status;       // 0-未启用 1-启用 2-运行中 3-异常
    
    private String config;         // 插件配置JSON
    
    private String endpoint;       // 插件服务地址
    
    private LocalDateTime lastHeartbeat;  // 最后心跳时间
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
