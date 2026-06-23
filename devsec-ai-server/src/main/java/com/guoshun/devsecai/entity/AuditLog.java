package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;          // 用户ID
    
    private String username;       // 用户名
    
    private String module;         // 模块
    
    private String operation;      // 操作类型：CREATE、UPDATE、DELETE、LOGIN、LOGOUT、SCAN等
    
    private String description;    // 操作描述
    
    private String method;         // 请求方法
    
    private String url;           // 请求URL
    
    private String param;         // 请求参数
    
    private String ip;           // IP地址
    
    private Integer status;       // 0-失败 1-成功
    
    private String errorMsg;     // 错误信息
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableLogic
    private Integer deleted;
}
