package com.guoshun.devsecai.service

import com.guoshun.devsecai.config.DevSecAISettings
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProjectStartupListener : ProjectActivity {

    private val logger = Logger.getInstance(ProjectStartupListener::class.java)

    override suspend fun execute(project: Project) {
        logger.info("DevSecAI plugin starting for project: ${project.name}")

        val cs = CoroutineScope(Dispatchers.IO)
        cs.launch {
            try {
                // Step 1: Handshake
                val settings = DevSecAISettings.getInstance()
                if (settings.serverUrl.isEmpty() || settings.accessToken.isEmpty()) {
                    logger.warn("Server URL or Access Token not configured, skipping handshake")
                    return@launch
                }

                val client = DevSecAIClient()
                val ideName = "IntelliJ IDEA"
                val ideVersion = getIdeVersion()
                val machineId = getMachineId()
                val developer = System.getProperty("user.name") ?: "unknown"

                val response = client.handshake(ideName, ideVersion, machineId, developer)
                if (response != null) {
                    logger.info("Handshake successful, project: ${response.projectName}")
                } else {
                    logger.warn("Handshake failed")
                }

                // Step 2: Fetch policy
                val policyService = project.getService(PolicyService::class.java)
                policyService.refreshPolicy()

                // Step 3: Start heartbeat
                val heartbeatService = project.getService(HeartbeatService::class.java)
                heartbeatService.start()

                // Step 4: Start finding collector
                val findingCollector = project.getService(FindingCollector::class.java)
                findingCollector.start()

                logger.info("DevSecAI plugin initialized successfully")
            } catch (e: Exception) {
                logger.error("DevSecAI plugin initialization failed: ${e.message}", e)
            }
        }
    }

    private fun getIdeVersion(): String {
        return try {
            val buildNumber = com.intellij.openapi.application.ApplicationInfo.getInstance().build
            buildNumber.asString()
        } catch (e: Exception) {
            "unknown"
        }
    }

    private fun getMachineId(): String {
        return try {
            val userHome = System.getProperty("user.home") ?: "/tmp"
            val path = java.io.File(userHome, ".devsecai-machine-id")
            if (path.exists()) {
                path.readText().trim()
            } else {
                val id = java.util.UUID.randomUUID().toString()
                path.writeText(id)
                id
            }
        } catch (e: Exception) {
            java.util.UUID.randomUUID().toString()
        }
    }
}
