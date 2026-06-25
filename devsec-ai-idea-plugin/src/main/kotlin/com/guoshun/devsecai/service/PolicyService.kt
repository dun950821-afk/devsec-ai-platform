package com.guoshun.devsecai.service

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.model.PolicyResponse
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * 策略管理服务
 * 负责从管理平台拉取策略配置，并缓存到本地
 *
 * 设计原则：
 * - getXxxEnabled() 方法可能触发网络请求（用于非热路径场景）
 * - getXxxEnabledCached() 方法仅读缓存，绝不触发网络请求（用于 Inspection/PSI 热路径）
 */
class PolicyService(private val project: Project) {

    private val logger = Logger.getInstance(PolicyService::class.java)
    private val client = DevSecAIClient()
    private var cachedPolicy: PolicyResponse? = null
    private var lastFetchTime: Long = 0

    /**
     * 获取策略（优先缓存，5分钟过期）
     * 可能触发网络请求，不适合在 PSI/Inspection 热路径中调用
     */
    fun getPolicy(): PolicyResponse? {
        val settings = DevSecAISettings.getInstance()
        if (!settings.isConfigured()) return null

        val now = System.currentTimeMillis()
        if (cachedPolicy != null && now - lastFetchTime < 5 * 60 * 1000) {
            return cachedPolicy
        }

        return try {
            val policy = client.getPolicy()
            cachedPolicy = policy
            lastFetchTime = now
            val data = policy.data
            if (data != null) {
                logger.info("Policy fetched: ${data.policyName} v${data.policyVersion}")
            }
            policy
        } catch (e: Exception) {
            logger.warn("Failed to fetch policy: ${e.message}")
            cachedPolicy
        }
    }

    /**
     * 强制刷新策略（后台任务中使用）
     */
    fun refreshPolicy(): PolicyResponse? {
        lastFetchTime = 0
        cachedPolicy = null
        return getPolicy()
    }

    // ==================== 缓存优先的能力开关判断（用于 PSI/Inspection 热路径） ====================
    // 这些方法仅读取本地缓存，绝不触发网络 I/O
    // 缓存为空时默认返回 true（启用检测），避免策略未加载时漏检

    fun isSastEnabledCached(): Boolean {
        val cached = cachedPolicy
        return cached?.data?.enabledModules?.sast ?: true
    }

    fun isSecretsEnabledCached(): Boolean {
        val cached = cachedPolicy
        return cached?.data?.enabledModules?.secrets ?: true
    }

    fun isScaEnabledCached(): Boolean {
        val cached = cachedPolicy
        return cached?.data?.enabledModules?.sca ?: true
    }

    fun isBaselineEnabledCached(): Boolean {
        val cached = cachedPolicy
        return cached?.data?.enabledModules?.baseline ?: true
    }

    fun isAiEnabledCached(): Boolean {
        val cached = cachedPolicy
        return cached?.data?.enabledModules?.ai ?: true
    }

    // ==================== 可触发网络请求的能力开关（用于非热路径场景） ====================

    fun isScaEnabled(): Boolean = getPolicy()?.data?.enabledModules?.sca == true
    fun isSastEnabled(): Boolean = getPolicy()?.data?.enabledModules?.sast == true
    fun isSecretsEnabled(): Boolean = getPolicy()?.data?.enabledModules?.secrets == true
    fun isBaselineEnabled(): Boolean = getPolicy()?.data?.enabledModules?.baseline == true
    fun isAiEnabled(): Boolean = getPolicy()?.data?.enabledModules?.ai == true

    fun shouldBlockCritical(): Boolean = getPolicy()?.data?.blockingRules?.critical == true
    fun shouldBlockHigh(): Boolean = getPolicy()?.data?.blockingRules?.high == true
    fun shouldBlockMedium(): Boolean = getPolicy()?.data?.blockingRules?.medium == true

    fun isManualScanEnabled(): Boolean = getPolicy()?.data?.scanTriggers?.manualScan == true
    fun isOnSaveScanEnabled(): Boolean = getPolicy()?.data?.scanTriggers?.onSave == true
    fun isOnCommitScanEnabled(): Boolean = getPolicy()?.data?.scanTriggers?.onCommit == true

    fun isCodeSnippetUploadAllowed(): Boolean = getPolicy()?.data?.aiPolicy?.allowCodeSnippetUpload == true
    fun shouldMaskSecrets(): Boolean = getPolicy()?.data?.aiPolicy?.maskSecrets == true
    fun getMaxContextLines(): Int = getPolicy()?.data?.aiPolicy?.maxContextLines ?: 80

    companion object {
        fun getInstance(project: Project): PolicyService =
            project.getService(PolicyService::class.java)
    }
}
