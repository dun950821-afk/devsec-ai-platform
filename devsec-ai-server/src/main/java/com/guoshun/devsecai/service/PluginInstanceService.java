package com.guoshun.devsecai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.dto.HandshakeRequest;
import com.guoshun.devsecai.dto.HandshakeResponse;
import com.guoshun.devsecai.dto.HeartbeatRequest;
import com.guoshun.devsecai.entity.PluginCapabilityPolicy;
import com.guoshun.devsecai.entity.PluginInstance;
import com.guoshun.devsecai.entity.PluginToken;
import com.guoshun.devsecai.mapper.PluginCapabilityPolicyMapper;
import com.guoshun.devsecai.mapper.PluginInstanceMapper;
import com.guoshun.devsecai.mapper.PluginTokenMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PluginInstanceService {

    private final PluginInstanceMapper pluginInstanceMapper;
    private final PluginTokenMapper pluginTokenMapper;
    private final PluginCapabilityPolicyMapper capabilityPolicyMapper;

    public PluginInstanceService(PluginInstanceMapper pluginInstanceMapper,
                                 PluginTokenMapper pluginTokenMapper,
                                 PluginCapabilityPolicyMapper capabilityPolicyMapper) {
        this.pluginInstanceMapper = pluginInstanceMapper;
        this.pluginTokenMapper = pluginTokenMapper;
        this.capabilityPolicyMapper = capabilityPolicyMapper;
    }

    public Page<PluginInstance> getPage(Integer current, Integer size, String keyword, String status, String ideName) {
        Page<PluginInstance> page = new Page<>(current, size);
        LambdaQueryWrapper<PluginInstance> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(PluginInstance::getPluginId, keyword)
                    .or().like(PluginInstance::getDeveloper, keyword));
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(PluginInstance::getStatus, status);
        }
        if (ideName != null && !ideName.isEmpty()) {
            wrapper.like(PluginInstance::getIdeName, ideName);
        }
        wrapper.orderByDesc(PluginInstance::getCreateTime);
        return pluginInstanceMapper.selectPage(page, wrapper);
    }

    public PluginInstance getById(Long id) {
        return pluginInstanceMapper.selectById(id);
    }

    public PluginInstance getByPluginId(String pluginId) {
        return pluginInstanceMapper.selectOne(
            new LambdaQueryWrapper<PluginInstance>().eq(PluginInstance::getPluginId, pluginId));
    }

    public HandshakeResponse handshake(HandshakeRequest request) {
        // 1. 校验Token
        PluginToken token = pluginTokenMapper.selectOne(
            new LambdaQueryWrapper<PluginToken>()
                .eq(PluginToken::getToken, request.getAccessToken())
                .eq(PluginToken::getStatus, "ACTIVE"));
        if (token == null) {
            throw new RuntimeException("无效或已过期的访问Token");
        }

        // 检查Token过期
        if (token.getExpireTime() != null && token.getExpireTime().isBefore(LocalDateTime.now())) {
            token.setStatus("EXPIRED");
            pluginTokenMapper.updateById(token);
            throw new RuntimeException("访问Token已过期");
        }

        // 2. 查找或创建插件实例
        PluginInstance instance = getByPluginId(request.getPluginId());
        if (instance == null) {
            instance = new PluginInstance();
            instance.setPluginId(request.getPluginId());
            instance.setAccessToken(request.getAccessToken());
        }
        instance.setPluginVersion(request.getPluginVersion());
        instance.setIdeName(request.getIdeName());
        instance.setIdeVersion(request.getIdeVersion());
        instance.setMachineId(request.getMachineId());
        instance.setStatus("ONLINE");
        instance.setLastHeartbeat(LocalDateTime.now());
        instance.setDeveloper(instance.getDeveloper() != null ? instance.getDeveloper() : "未知");

        // 绑定Token到插件
        if (token.getBindPluginId() == null || token.getBindPluginId().isEmpty()) {
            token.setBindPluginId(request.getPluginId());
            pluginTokenMapper.updateById(token);
        }

        if (instance.getId() == null) {
            pluginInstanceMapper.insert(instance);
        } else {
            pluginInstanceMapper.updateById(instance);
        }

        // 3. 查找策略
        PluginCapabilityPolicy policy = null;
        if (instance.getPolicyId() != null) {
            policy = capabilityPolicyMapper.selectById(instance.getPolicyId());
        }
        if (policy == null) {
            // 使用第一个启用的策略
            policy = capabilityPolicyMapper.selectOne(
                new LambdaQueryWrapper<PluginCapabilityPolicy>()
                    .eq(PluginCapabilityPolicy::getStatus, 1)
                    .orderByAsc(PluginCapabilityPolicy::getId)
                    .last("LIMIT 1"));
        }

        // 4. 构建响应
        HandshakeResponse response = new HandshakeResponse();
        response.setProjectId(instance.getProjectId() != null ? instance.getProjectId().toString() : "");
        response.setProjectName(instance.getProjectName() != null ? instance.getProjectName() : request.getProjectName());
        response.setUserId("admin");

        if (policy != null) {
            response.setPolicyId(policy.getId().toString());
            response.setPolicyVersion(policy.getPolicyVersion());

            List<String> modules = new ArrayList<>();
            if (Boolean.TRUE.equals(policy.getScaEnabled())) modules.add("SCA");
            if (Boolean.TRUE.equals(policy.getSastEnabled())) modules.add("SAST");
            if (Boolean.TRUE.equals(policy.getSecretsEnabled())) modules.add("SECRETS");
            if (Boolean.TRUE.equals(policy.getBaselineEnabled())) modules.add("BASELINE");
            if (Boolean.TRUE.equals(policy.getAiEnabled())) modules.add("AI");
            response.setEnabledModules(modules);

            instance.setPolicyId(policy.getId());
            instance.setPolicyName(policy.getPolicyName());
            instance.setEnabledModules(String.join(",", modules));
            pluginInstanceMapper.updateById(instance);
        } else {
            response.setEnabledModules(Arrays.asList("SCA", "SAST"));
        }
        response.setRulePackVersion("2026.06.23");

        return response;
    }

    public void heartbeat(HeartbeatRequest request) {
        PluginInstance instance = getByPluginId(request.getPluginId());
        if (instance == null) {
            throw new RuntimeException("插件实例不存在: " + request.getPluginId());
        }
        instance.setLastHeartbeat(LocalDateTime.now());
        if (request.getStatus() != null && !request.getStatus().isEmpty()) {
            instance.setStatus(request.getStatus());
        } else {
            instance.setStatus("ONLINE");
        }
        pluginInstanceMapper.updateById(instance);
    }

    public void updateStatus(Long id, String status) {
        PluginInstance instance = pluginInstanceMapper.selectById(id);
        if (instance != null) {
            instance.setStatus(status);
            pluginInstanceMapper.updateById(instance);
        }
    }

    public void assignPolicy(Long id, Long policyId) {
        PluginInstance instance = pluginInstanceMapper.selectById(id);
        if (instance == null) {
            throw new RuntimeException("插件实例不存在");
        }
        PluginCapabilityPolicy policy = capabilityPolicyMapper.selectById(policyId);
        if (policy == null) {
            throw new RuntimeException("策略不存在");
        }
        instance.setPolicyId(policyId);
        instance.setPolicyName(policy.getPolicyName());
        pluginInstanceMapper.updateById(instance);
    }

    public Map<String, Long> getStatusCounts() {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("ONLINE", 0L);
        counts.put("OFFLINE", 0L);
        counts.put("ERROR", 0L);
        counts.put("DISABLED", 0L);
        counts.put("UPGRADE_REQUIRED", 0L);
        counts.put("UNAUTHENTICATED", 0L);
        List<PluginInstance> all = pluginInstanceMapper.selectList(null);
        for (PluginInstance inst : all) {
            String status = inst.getStatus();
            if (status != null && counts.containsKey(status)) {
                counts.put(status, counts.get(status) + 1);
            }
        }
        return counts;
    }
}
