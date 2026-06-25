package com.guoshun.devsecai.dto;

import lombok.Data;
import java.util.List;

@Data
public class HandshakeResponse {
    private String projectId;
    private String projectName;
    private String userId;
    private String policyId;
    private String policyVersion;
    private String rulePackVersion;
    private List<String> enabledModules;
}
