package com.guoshun.devsecai.service

import com.guoshun.devsecai.config.DevSecAISettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

/**
 * 项目生命周期监听器
 * 项目打开时执行握手认证、启动心跳和策略拉取
 */
class ProjectStartupListener : ProjectManagerListener {

    private val logger = Logger.getInstance(ProjectStartupListener::class.java)

    override fun projectOpened(project: Project) {
        val settings = DevSecAISettings.getInstance()
        if (!settings.isConfigured()) {
            logger.info("DevSecAI not configured, skipping startup")
            return
        }

        // 1. 握手认证
        val client = DevSecAIClient()
        try {
            val handshakeResponse = client.handshake()
            if (handshakeResponse.code == 200) {
                settings.connected = true
                logger.info("Handshake successful: ${handshakeResponse.data}")

                // 2. 启动心跳
                val heartbeatService = HeartbeatService.getInstance(project)
                heartbeatService.start()

                // 3. 拉取策略
                val policyService = PolicyService.getInstance(project)
                policyService.refreshPolicy()

                // 4. 启动 Finding 收集器
                val findingCollector = FindingCollector.getInstance(project)
                findingCollector.start()
            } else {
                settings.connected = false
                logger.warn("Handshake failed: ${handshakeResponse.message}")
            }
        } catch (e: Exception) {
            settings.connected = false
            logger.warn("Handshake error: ${e.message}")
        }
    }

    override fun projectClosing(project: Project) {
        val heartbeatService = HeartbeatService.getInstance(project)
        heartbeatService.stop()

        val findingCollector = FindingCollector.getInstance(project)
        findingCollector.stop()
    }
}
