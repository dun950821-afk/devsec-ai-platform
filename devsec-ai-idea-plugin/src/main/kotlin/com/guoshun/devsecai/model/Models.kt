package com.guoshun.devsecai.model

import com.google.gson.annotations.SerializedName

data class PolicyConfig(
    val enabledModules: EnabledModules? = null,
    val scanTriggers: ScanTriggers? = null,
    val blockingRules: BlockingRules? = null,
    val aiPolicy: AiPolicy? = null
)

data class EnabledModules(
    val sca: Boolean = false,
    val sast: Boolean = true,
    val secrets: Boolean = true,
    val baseline: Boolean = false,
    val ai: Boolean = false
)

data class ScanTriggers(
    val onFileSave: Boolean = true,
    val onFileOpen: Boolean = false,
    val onCommit: Boolean = true,
    val manualOnly: Boolean = false
)

data class BlockingRules(
    val blockCritical: Boolean = true,
    val blockHigh: Boolean = true,
    val blockMedium: Boolean = false,
    val blockLow: Boolean = false,
    val allowOverride: Boolean = true
)

data class AiPolicy(
    val vulnExplain: Boolean = false,
    val fixSuggestion: Boolean = false,
    val patchGenerate: Boolean = false,
    val allowCodeSnippet: Boolean = false,
    val maxContextLines: Int = 10
)

data class HandshakeRequest(
    val pluginId: String,
    val pluginVersion: String,
    val ideName: String,
    val ideVersion: String,
    val machineId: String,
    val developer: String,
    val accessToken: String
)

data class HandshakeResponse(
    val projectId: String? = null,
    val projectName: String? = null,
    val userId: String? = null,
    val policyId: String? = null,
    val policyVersion: String? = null,
    val rulePackVersion: String? = null,
    val enabledModules: List<String>? = null
)

data class HeartbeatRequest(
    val pluginId: String,
    val status: String = "ONLINE",
    val activeModules: List<String>? = null
)

data class PolicyResponse(
    val code: Int? = null,
    val message: String? = null,
    val data: PolicyData? = null
)

data class PolicyData(
    val policyId: String? = null,
    val policyName: String? = null,
    val policyVersion: String? = null,
    val enabledModules: EnabledModules? = null,
    val scanTriggers: ScanTriggers? = null,
    val blockingRules: BlockingRules? = null,
    val aiPolicy: AiPolicy? = null
)

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
    val startLine: Int,
    val endLine: Int,
    val componentName: String? = null,
    val currentVersion: String? = null,
    val fixedVersion: String? = null,
    val vulnerabilityId: String? = null,
    val confidence: String = "HIGH",
    val recommendation: String? = null,
    val cwe: String? = null,
    val owasp: String? = null
)

data class LocalFinding(
    val ruleId: String,
    val severity: String,
    val title: String,
    val description: String,
    val filePath: String,
    val startLine: Int,
    val endLine: Int,
    val module: String,
    val componentName: String? = null,
    val currentVersion: String? = null,
    val fixedVersion: String? = null,
    val vulnerabilityId: String? = null,
    val confidence: String = "HIGH",
    val recommendation: String? = null,
    val cwe: String? = null,
    val owasp: String? = null
)

enum class FindingSeverity(val display: String, val color: String) {
    CRITICAL("严重", "#FF0000"),
    HIGH("高危", "#FF6600"),
    MEDIUM("中危", "#FFAA00"),
    LOW("低危", "#66AAFF"),
    INFO("提示", "#888888")
}

enum class FindingStatus(val display: String) {
    OPEN("待处理"),
    FIXED("已修复"),
    IGNORED("已忽略")
}
