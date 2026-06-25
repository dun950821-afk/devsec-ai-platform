package com.guoshun.devsecai.service

import com.guoshun.devsecai.model.*
import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project

@Service(Service.Level.PROJECT)
class PolicyService(private val project: Project) {

    private val logger = Logger.getInstance(PolicyService::class.java)
    private val client = DevSecAIClient()
    private var cachedPolicy: PolicyData? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_TTL = 5 * 60 * 1000L // 5 minutes

    fun refreshPolicy(): PolicyData? {
        try {
            val policy = client.fetchPolicy()
            if (policy != null) {
                cachedPolicy = policy
                cacheTimestamp = System.currentTimeMillis()
                logger.info("Policy refreshed: ${policy.policyId}")
            }
            return policy
        } catch (e: Exception) {
            logger.warn("Failed to refresh policy: ${e.message}")
            return cachedPolicy
        }
    }

    private fun getPolicy(): PolicyData? {
        val now = System.currentTimeMillis()
        if (cachedPolicy == null || (now - cacheTimestamp) > CACHE_TTL) {
            refreshPolicy()
        }
        return cachedPolicy
    }

    // Cached-only methods for use in PSI/Inspection paths (no network I/O)
    // Returns true by default if no cached policy is available (fail-open)

    fun isSastEnabledCached(): Boolean {
        return cachedPolicy?.enabledModules?.sast ?: true
    }

    fun isSecretsEnabledCached(): Boolean {
        return cachedPolicy?.enabledModules?.secrets ?: true
    }

    fun isScaEnabledCached(): Boolean {
        return cachedPolicy?.enabledModules?.sca ?: false
    }

    fun isBaselineEnabledCached(): Boolean {
        return cachedPolicy?.enabledModules?.baseline ?: false
    }

    fun isAiEnabledCached(): Boolean {
        return cachedPolicy?.enabledModules?.ai ?: false
    }

    fun getBlockingRulesCached(): BlockingRules {
        return cachedPolicy?.blockingRules ?: BlockingRules()
    }

    fun getAiPolicyCached(): AiPolicy {
        return cachedPolicy?.aiPolicy ?: AiPolicy()
    }

    fun getScanTriggersCached(): ScanTriggers {
        return cachedPolicy?.scanTriggers ?: ScanTriggers()
    }
}
