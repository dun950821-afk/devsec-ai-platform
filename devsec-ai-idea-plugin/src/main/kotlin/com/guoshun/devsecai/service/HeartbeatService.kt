package com.guoshun.devsecai.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.text.SimpleDateFormat
import java.util.*

@Service(Service.Level.PROJECT)
class HeartbeatService(private val project: Project) {

    private val logger = Logger.getInstance(HeartbeatService::class.java)
    private var timer: Timer? = null
    private var running = false
    private var lastHeartbeatTime: String? = null

    fun start() {
        stop()
        running = true
        timer = Timer("DevSecAI-Heartbeat", true)
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                try {
                    if (project.isDisposed) {
                        stop()
                        return
                    }
                    val client = DevSecAIClient(project)
                    val settings = com.guoshun.devsecai.config.DevSecAISettings.getInstance()
                    val activeModules = mutableListOf<String>()
                    if (settings.enableSAST) activeModules.add("SAST")
                    if (settings.enableSecrets) activeModules.add("SECRETS")
                    if (settings.enableSCA) activeModules.add("SCA")
                    if (settings.enableBaseline) activeModules.add("BASELINE")

                    val result = client.heartbeat()
                    if (result.successful) {
                        lastHeartbeatTime = SimpleDateFormat("HH:mm:ss").format(Date())
                        logger.info("Heartbeat sent successfully")
                    } else {
                        logger.warn("Heartbeat send failed: ${result.message}")
                    }
                } catch (e: Exception) {
                    logger.warn("Heartbeat error: ${e.message}")
                }
            }
        }, 0, 5 * 60 * 1000L) // every 5 minutes
        logger.info("Heartbeat service started")
    }

    fun stop() {
        running = false
        timer?.cancel()
        timer = null
        logger.info("Heartbeat service stopped")
    }

    fun isRunning(): Boolean = running

    fun getLastHeartbeatTime(): String? = lastHeartbeatTime

    fun sendHeartbeat() {
        try {
            val client = DevSecAIClient(project)
            val result = client.heartbeat()
            if (result.successful) {
                lastHeartbeatTime = SimpleDateFormat("HH:mm:ss").format(Date())
                logger.info("Manual heartbeat sent successfully")
            } else {
                logger.warn("Manual heartbeat failed: ${result.message}")
            }
        } catch (e: Exception) {
            logger.warn("Manual heartbeat error: ${e.message}")
        }
    }
}
