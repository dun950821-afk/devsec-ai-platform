package com.guoshun.devsecai.dto;

import lombok.Data;
import java.util.Map;

@Data
public class PolicyResponse {
    private String policyId;
    private String policyName;
    private String policyVersion;
    private Map<String, Boolean> enabledModules;
    private Map<String, Boolean> scanTriggers;
    private Map<String, Boolean> blockingRules;
    private Map<String, Object> aiPolicy;
}
