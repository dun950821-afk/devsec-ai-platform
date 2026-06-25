package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("PLUGIN_INSTANCE")
public class PluginInstance {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String pluginId;        // 插件实例唯一标识，如 IDEA-PLG-000128

    private String pluginVersion;   // 插件版本号，如 0.9.6

    private String ideName;         // IDE名称，如 IntelliJ IDEA

    private String ideVersion;      // IDE版本，如 2025.2

    private String developer;       // 开发者/使用者

    private String machineId;       // 机器码

    private Long projectId;         // 绑定项目ID

    private String projectName;     // 绑定项目名称

    private Long policyId;          // 当前策略ID

    private String policyName;      // 当前策略名称

    private String status;          // 状态：ONLINE/OFFLINE/ERROR/UPGRADE_REQUIRED/DISABLED/UNAUTHENTICATED

    private LocalDateTime lastHeartbeat;  // 最近心跳时间

    private String accessToken;     // 绑定的访问Token

    private String enabledModules;  // 启用模块，逗号分隔，如 SCA,SAST,SECRETS,BASELINE,AI

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
