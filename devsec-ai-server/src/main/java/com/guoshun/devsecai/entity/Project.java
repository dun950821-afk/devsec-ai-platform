package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String name;           // 项目名称
    
    private String code;           // 项目代码/仓库名
    
    private String repoUrl;        // 仓库地址
    
    private String description;    // 描述
    
    private String branch;         // 默认分支
    
    private Long userId;           // 创建人ID
    
    private Integer scanEnabled;   // 是否启用扫描 0-否 1-是
    
    private Long policyId;         // 绑定的策略ID
    
    private Integer status;        // 0-正常 1-暂停 2-异常
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
