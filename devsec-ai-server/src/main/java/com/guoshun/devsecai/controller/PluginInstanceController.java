package com.guoshun.devsecai.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guoshun.devsecai.common.Result;
import com.guoshun.devsecai.dto.HandshakeRequest;
import com.guoshun.devsecai.dto.HandshakeResponse;
import com.guoshun.devsecai.dto.HeartbeatRequest;
import com.guoshun.devsecai.dto.PolicyResponse;
import com.guoshun.devsecai.entity.PluginInstance;
import com.guoshun.devsecai.service.CapabilityPolicyService;
import com.guoshun.devsecai.service.PluginInstanceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plugin-instance")
public class PluginInstanceController {

    private final PluginInstanceService pluginInstanceService;
    private final CapabilityPolicyService capabilityPolicyService;

    public PluginInstanceController(PluginInstanceService pluginInstanceService,
                                     CapabilityPolicyService capabilityPolicyService) {
        this.pluginInstanceService = pluginInstanceService;
        this.capabilityPolicyService = capabilityPolicyService;
    }

    @GetMapping("/list")
    public Result<Page<PluginInstance>> getList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String ideName) {
        return Result.success(pluginInstanceService.getPage(current, size, keyword, status, ideName));
    }

    @GetMapping("/{id}")
    public Result<PluginInstance> getById(@PathVariable Long id) {
        return Result.success(pluginInstanceService.getById(id));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        pluginInstanceService.updateStatus(id, body.get("status"));
        return Result.success("状态更新成功", null);
    }

    @PutMapping("/{id}/policy")
    public Result<Void> assignPolicy(@PathVariable Long id, @RequestBody java.util.Map<String, Long> body) {
        pluginInstanceService.assignPolicy(id, body.get("policyId"));
        return Result.success("策略下发成功", null);
    }

    @GetMapping("/status-counts")
    public Result<?> getStatusCounts() {
        return Result.success(pluginInstanceService.getStatusCounts());
    }

    // === 插件握手认证 ===
    @PostMapping("/handshake")
    public Result<HandshakeResponse> handshake(@RequestBody HandshakeRequest request) {
        try {
            HandshakeResponse response = pluginInstanceService.handshake(request);
            return Result.success("握手成功", response);
        } catch (RuntimeException e) {
            return Result.error(401, e.getMessage());
        }
    }

    // === 插件心跳上报 ===
    @PostMapping("/heartbeat")
    public Result<Void> heartbeat(@RequestBody HeartbeatRequest request) {
        try {
            pluginInstanceService.heartbeat(request);
            return Result.success("心跳成功", null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    // === 策略下发（插件拉取） ===
    @GetMapping("/policy")
    public Result<PolicyResponse> getPolicy(@RequestParam String pluginId) {
        try {
            PolicyResponse response = capabilityPolicyService.getPolicyForPlugin(pluginId);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
