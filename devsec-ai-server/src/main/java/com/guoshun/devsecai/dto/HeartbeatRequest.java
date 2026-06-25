package com.guoshun.devsecai.dto;

import lombok.Data;
import java.util.List;

@Data
public class HeartbeatRequest {
    private String pluginId;        // 插件实例唯一标识
    private String status;          // 当前状态
    private List<String> activeModules; // 活跃模块列表
    private Integer scanCount;      // 扫描次数
    private Integer findingCount;   // 发现数量
}
