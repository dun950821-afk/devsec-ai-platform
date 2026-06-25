package com.guoshun.devsecai.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.*

@Service(Service.Level.PROJECT)
class HeartbeatService(private val project: Project) {

    private val logger = Logger.getInstance(HeartbeatService::class.java)
    private var timer: Timer? = null
    private val client = DevSecAIClient()

    fun start() {
        stop()
        timer = Timer("DevSecAI-Heartbeat", true)
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    val settings = com.guoshun.devsecai.config.DevSecAISettings.getInstance()
                    val activeModules = mutableListOf<String>()
                    if (settings.enableSAST) activeModules.add("SAST")
                    if (settings.enableSecrets) activeModules.add("SECRETS")
                    if (settings.enableSCA) activeModules.add("SCA")
                    if (settings.enableBaseline) activeModules.add("BASELINE")

                    val success = client.heartbeat("ONLINE", activeModules)
                    if (success) {
                        logger.info("Heartbeat sent successfully")
                    } else {
                        logger.warn("Heartbeat send failed")
                    }
                } catch (e: Exception) {
                    logger.warn("Heartbeat error: ${e.message}")
                }
            }
        }, 0, 5 * 60 * 1000L) // every 5 minutes
        logger.info("Heartbeat service started")
    }

    fun stop() {
        timer?.cancel()
        timer = null
        logger.info("Heartbeat service stopped")
    }

    fun sendHeartbeat() {
        try {
            val settings = com.guoshun.devsecai.config.DevSecAISettings.getInstance()
            val activeModules = mutableListOf<String>()
            if (settings.enableSAST) activeModules.add("SAST")
            if (settings.enableSecrets) activeModules.add("SECRETS")
            if (settings.enableSCA) activeModules.add("SCA")
            if (settings.enableBaseline) activeModules.add("BASELINE")
            client.heartbeat("ONLINE", activeModules)
        } catch (e: Exception) {
            logger.warn("Manual heartbeat error: ${e.message}")
        }
    }
}
