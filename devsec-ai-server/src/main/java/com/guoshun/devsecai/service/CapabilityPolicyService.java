package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.dto.PolicyResponse;
import com.guoshun.devsecai.entity.PluginCapabilityPolicy;
import com.guoshun.devsecai.entity.PluginInstance;
import com.guoshun.devsecai.mapper.PluginCapabilityPolicyMapper;
import com.guoshun.devsecai.mapper.PluginInstanceMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CapabilityPolicyService {

    private final PluginCapabilityPolicyMapper policyMapper;
    private final PluginInstanceMapper instanceMapper;

    public CapabilityPolicyService(PluginCapabilityPolicyMapper policyMapper,
                                    PluginInstanceMapper instanceMapper) {
        this.policyMapper = policyMapper;
        this.instanceMapper = instanceMapper;
    }

    public Page<PluginCapabilityPolicy> getPage(Integer current, Integer size, String keyword, Integer status) {
        Page<PluginCapabilityPolicy> page = new Page<>(current, size);
        LambdaQueryWrapper<PluginCapabilityPolicy> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(PluginCapabilityPolicy::getPolicyName, keyword);
        }
        if (status != null) {
            wrapper.eq(PluginCapabilityPolicy::getStatus, status);
        }
        wrapper.orderByDesc(PluginCapabilityPolicy::getCreateTime);
        return policyMapper.selectPage(page, wrapper);
    }

    public List<PluginCapabilityPolicy> getAll() {
        return policyMapper.selectList(new LambdaQueryWrapper<PluginCapabilityPolicy>()
            .eq(PluginCapabilityPolicy::getStatus, 1)
            .orderByDesc(PluginCapabilityPolicy::getCreateTime));
    }

    public PluginCapabilityPolicy getById(Long id) {
        return policyMapper.selectById(id);
    }

    public PluginCapabilityPolicy create(PluginCapabilityPolicy policy) {
        if (policy.getStatus() == null) {
            policy.setStatus(1);
        }
        setDefaults(policy);
        policyMapper.insert(policy);
        return policy;
    }

    public void update(PluginCapabilityPolicy policy) {
        policyMapper.updateById(policy);
    }

    public void delete(Long id) {
        policyMapper.deleteById(id);
    }

    public void updateStatus(Long id, Integer status) {
        PluginCapabilityPolicy policy = new PluginCapabilityPolicy();
        policy.setId(id);
        policy.setStatus(status);
        policyMapper.updateById(policy);
    }

    public PolicyResponse getPolicyForPlugin(String pluginId) {
        PluginInstance instance = instanceMapper.selectOne(
            new LambdaQueryWrapper<PluginInstance>().eq(PluginInstance::getPluginId, pluginId));
        if (instance == null) {
            throw new RuntimeException("插件实例不存在: " + pluginId);
        }

        PluginCapabilityPolicy policy = null;
        if (instance.getPolicyId() != null) {
            policy = policyMapper.selectById(instance.getPolicyId());
        }
        if (policy == null) {
            // 查找全局策略（projectId为空的）
            policy = policyMapper.selectOne(
                new LambdaQueryWrapper<PluginCapabilityPolicy>()
                    .eq(PluginCapabilityPolicy::getStatus, 1)
                    .orderByAsc(PluginCapabilityPolicy::getId)
                    .last("LIMIT 1"));
        }
        if (policy == null) {
            throw new RuntimeException("没有可用的策略配置");
        }

        PolicyResponse response = new PolicyResponse();
        response.setPolicyId(policy.getId().toString());
        response.setPolicyName(policy.getPolicyName());
        response.setPolicyVersion(policy.getPolicyVersion());

        Map<String, Boolean> modules = new LinkedHashMap<>();
        modules.put("sca", Boolean.TRUE.equals(policy.getScaEnabled()));
        modules.put("sast", Boolean.TRUE.equals(policy.getSastEnabled()));
        modules.put("secrets", Boolean.TRUE.equals(policy.getSecretsEnabled()));
        modules.put("baseline", Boolean.TRUE.equals(policy.getBaselineEnabled()));
        modules.put("ai", Boolean.TRUE.equals(policy.getAiEnabled()));
        response.setEnabledModules(modules);

        Map<String, Boolean> triggers = new LinkedHashMap<>();
        triggers.put("manualScan", true);
        triggers.put("onSave", true);
        triggers.put("onCommit", Boolean.TRUE.equals(policy.getGitCommitCheck()));
        response.setScanTriggers(triggers);

        Map<String, Boolean> blocking = new LinkedHashMap<>();
        blocking.put("critical", Boolean.TRUE.equals(policy.getBlockCritical()));
        blocking.put("high", Boolean.TRUE.equals(policy.getBlockHigh()));
        blocking.put("medium", Boolean.TRUE.equals(policy.getBlockMedium()));
        blocking.put("low", Boolean.TRUE.equals(policy.getBlockLow()));
        response.setBlockingRules(blocking);

        Map<String, Object> aiPolicy = new HashMap<>();
        aiPolicy.put("allowCodeSnippetUpload", Boolean.TRUE.equals(policy.getAllowCodeSnippet()));
        aiPolicy.put("maskSecrets", Boolean.TRUE.equals(policy.getMaskSecrets()));
        aiPolicy.put("maxContextLines", policy.getMaxContextLines() != null ? policy.getMaxContextLines() : 80);
        response.setAiPolicy(aiPolicy);

        return response;
    }

    private void setDefaults(PluginCapabilityPolicy policy) {
        if (policy.getScaEnabled() == null) policy.setScaEnabled(true);
        if (policy.getSastEnabled() == null) policy.setSastEnabled(true);
        if (policy.getSecretsEnabled() == null) policy.setSecretsEnabled(true);
        if (policy.getBaselineEnabled() == null) policy.setBaselineEnabled(false);
        if (policy.getAiEnabled() == null) policy.setAiEnabled(false);
        if (policy.getAiVulnExplain() == null) policy.setAiVulnExplain(false);
        if (policy.getAiFixSuggestion() == null) policy.setAiFixSuggestion(false);
        if (policy.getAiPatchGenerate() == null) policy.setAiPatchGenerate(false);
        if (policy.getGitCommitCheck() == null) policy.setGitCommitCheck(true);
        if (policy.getBlockCritical() == null) policy.setBlockCritical(true);
        if (policy.getBlockHigh() == null) policy.setBlockHigh(true);
        if (policy.getBlockMedium() == null) policy.setBlockMedium(false);
        if (policy.getBlockLow() == null) policy.setBlockLow(false);
        if (policy.getAutoUpload() == null) policy.setAutoUpload(true);
        if (policy.getAllowCodeSnippet() == null) policy.setAllowCodeSnippet(true);
        if (policy.getMaskSecrets() == null) policy.setMaskSecrets(true);
        if (policy.getMaxContextLines() == null) policy.setMaxContextLines(80);
    }
}
