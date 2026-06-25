package com.guoshun.devsecai.service

import com.guoshun.devsecai.config.DevSecAISettings
import com.guoshun.devsecai.model.PolicyResponse
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

/**
 * 策略管理服务
 * 负责从管理平台拉取策略配置，并缓存到本地
 */
class PolicyService(private val project: Project) {

    private val logger = Logger.getInstance(PolicyService::class.java)
    private val client = DevSecAIClient()
    private var cachedPolicy: PolicyResponse? = null
    private var lastFetchTime: Long = 0

    /**
     * 获取策略（优先缓存，5分钟过期）
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
     * 强制刷新策略
     */
    fun refreshPolicy(): PolicyResponse? {
        lastFetchTime = 0
        cachedPolicy = null
        return getPolicy()
    }

    // ==================== 能力开关判断 ====================

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
