package com.guoshun.devsecai.dto;

import lombok.Data;

@Data
public class HandshakeRequest {
    private String pluginId;        // 插件实例唯一标识
    private String pluginVersion;   // 插件版本号
    private String ideName;         // IDE名称
    private String ideVersion;      // IDE版本
    private String projectName;     // 项目名称
    private String projectGitUrl;   // 项目Git地址
    private String machineId;       // 机器码
    private String accessToken;     // 访问Token
}
