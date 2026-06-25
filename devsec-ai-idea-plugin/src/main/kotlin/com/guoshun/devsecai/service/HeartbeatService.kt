package com.guoshun.devsecai.service

import com.guoshun.devsecai.config.DevSecAISettings
import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import java.util.*
import kotlin.concurrent.schedule

/**
 * 心跳上报服务
 * 定时向管理平台上报插件存活状态
 */
class HeartbeatService(private val project: Project) : Disposable {

    private val logger = Logger.getInstance(HeartbeatService::class.java)
    private val client = DevSecAIClient()
    private var timer: TimerTask? = null
    private var started = false

    @Synchronized
    fun start() {
        if (started) return
        started = true
        scheduleNext()
        logger.info("HeartbeatService started")
    }

    @Synchronized
    fun stop() {
        timer?.cancel()
        timer = null
        started = false
        logger.info("HeartbeatService stopped")
    }

    private fun scheduleNext() {
        if (!started) return
        val settings = DevSecAISettings.getInstance()
        val intervalMs = settings.heartbeatIntervalMinutes * 60 * 1000L
        timer = Timer().schedule(intervalMs) {
            try {
                doHeartbeat()
            } catch (e: Exception) {
                logger.warn("Heartbeat failed: ${e.message}")
            } finally {
                scheduleNext()
            }
        }
    }

    private fun doHeartbeat() {
        val settings = DevSecAISettings.getInstance()
        if (!settings.isConfigured()) return

        try {
            val response = client.heartbeat()
            if (response.code == 200) {
                settings.connected = true
                logger.debug("Heartbeat success")
            } else {
                settings.connected = false
                logger.warn("Heartbeat returned code: ${response.code}, message: ${response.message}")
            }
        } catch (e: Exception) {
            settings.connected = false
            logger.warn("Heartbeat error: ${e.message}")
        }
    }

    override fun dispose() {
        stop()
    }

    companion object {
        fun getInstance(project: Project): HeartbeatService =
            project.getService(HeartbeatService::class.java)
    }
}
