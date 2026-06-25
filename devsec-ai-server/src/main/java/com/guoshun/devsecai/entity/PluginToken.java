package com.guoshun.devsecai.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("PLUGIN_TOKEN")
public class PluginToken {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String token;           // 访问Token，如 plt_xxxxxxxxxxxx

    private String name;            // Token名称，如 支付团队Token

    private Long userId;            // 创建者ID

    private String bindPluginId;    // 绑定的插件实例ID

    private String status;          // 状态：ACTIVE/REVOKED/EXPIRED

    private LocalDateTime expireTime; // 过期时间

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
