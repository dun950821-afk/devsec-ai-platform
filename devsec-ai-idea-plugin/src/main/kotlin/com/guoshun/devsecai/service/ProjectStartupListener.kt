package com.guoshun.devsecai.service

import com.guoshun.devsecai.config.DevSecAISettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity

/**
 * 项目启动监听器（后台执行）
 * 使用 ProjectActivity (StartupActivity.Background) 在项目打开后后台执行握手认证、启动心跳和策略拉取
 * 避免在 projectOpened 中同步执行网络请求导致 IDE 卡顿
 */
class ProjectStartupListener : ProjectActivity {

    private val logger = Logger.getInstance(ProjectStartupListener::class.java)

    override suspend fun execute(project: Project) {
        val settings = DevSecAISettings.getInstance()
        if (!settings.isConfigured()) {
            logger.info("DevSecAI not configured, skipping startup")
            return
        }

        // 所有网络操作在后台协程中执行，不阻塞 UI
        try {
            // 1. 握手认证
            val client = DevSecAIClient()
            val handshakeResponse = client.handshake()
            if (handshakeResponse.code == 200) {
                settings.connected = true
                logger.info("Handshake successful: ${handshakeResponse.data}")

                // 2. 启动心跳（后台 Timer，不阻塞）
                val heartbeatService = HeartbeatService.getInstance(project)
                heartbeatService.start()

                // 3. 拉取策略（后台 HTTP 请求）
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
            logger.warn("Startup initialization error: ${e.message}")
        }
    }
}
