package com.guoshun.devsecai.model

import com.google.gson.annotations.SerializedName

/**
 * 握手认证请求
 */
data class HandshakeRequest(
    val pluginId: String,
    val pluginVersion: String,
    val ideName: String,
    val ideVersion: String,
    val machineId: String,
    val developer: String,
    val accessToken: String
)

/**
 * 握手认证响应
 */
data class HandshakeResponse(
    val code: Int,
    val message: String,
    val data: HandshakeData?
)

data class HandshakeData(
    val projectId: String,
    val projectName: String?,
    val userId: String,
    val policyId: String,
    val policyVersion: String,
    val rulePackVersion: String,
    val enabledModules: List<String>
)

/**
 * 心跳请求
 */
data class HeartbeatRequest(
    val pluginId: String
)

/**
 * 通用API响应
 */
data class ApiResponse(
    val code: Int,
    val message: String,
    val data: Any?
)

/**
 * 策略配置响应
 */
data class PolicyResponse(
    val code: Int,
    val message: String,
    val data: PolicyData?
)

data class PolicyData(
    val policyId: String,
    val policyName: String,
    val policyVersion: String,
    val enabledModules: EnabledModules,
    val scanTriggers: ScanTriggers,
    val blockingRules: BlockingRules,
    val aiPolicy: AiPolicy
)

data class EnabledModules(
    val sca: Boolean,
    val sast: Boolean,
    val secrets: Boolean,
    val baseline: Boolean,
    val ai: Boolean
)

data class ScanTriggers(
    val manualScan: Boolean,
    val onSave: Boolean,
    val onCommit: Boolean
)

data class BlockingRules(
    val critical: Boolean,
    val high: Boolean,
    val medium: Boolean,
    val low: Boolean
)

data class AiPolicy(
    val maskSecrets: Boolean,
    val maxContextLines: Int,
    val allowCodeSnippetUpload: Boolean
)

/**
 * Finding上送请求
 */
data class FindingUploadRequest(
    val pluginId: String,
    val module: String,
    val findings: List<FindingItem>
)

data class FindingItem(
    val ruleId: String,
    val severity: String,
    val title: String,
    val description: String,
    val filePath: String,
    val startLine: Int? = null,
    val endLine: Int? = null,
    val componentName: String? = null,
    val currentVersion: String? = null,
    val fixedVersion: String? = null,
    val vulnerabilityId: String? = null,
    val cwe: String? = null,
    val owasp: String? = null,
    val confidence: String? = null,
    val recommendation: String? = null
)

/**
 * Finding数据模型（本地使用）
 */
data class LocalFinding(
    val ruleId: String,
    val module: String,         // SAST / SCA / SECRETS
    val severity: String,       // CRITICAL / HIGH / MEDIUM / LOW
    val title: String,
    val description: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val componentName: String? = null,
    val currentVersion: String? = null,
    val fixedVersion: String? = null,
    val vulnerabilityId: String? = null,
    val recommendation: String? = null,
    var status: FindingStatus = FindingStatus.OPEN,
    var uploaded: Boolean = false
)

enum class FindingStatus(val display: String) {
    OPEN("待处理"),
    FIXED("已修复"),
    IGNORED("已忽略")
}

enum class Severity(val display: String, val priority: Int) {
    CRITICAL("严重", 0),
    HIGH("高危", 1),
    MEDIUM("中危", 2),
    LOW("低危", 3);

    companion object {
        fun fromValue(value: String): Severity =
            values().find { it.name.equals(value, ignoreCase = true) } ?: LOW
    }
}
